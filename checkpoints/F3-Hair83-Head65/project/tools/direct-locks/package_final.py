from pathlib import Path
import json,zipfile,hashlib,shutil
M=Path(__file__).resolve().parents[2];R=Path('/data/reports/kelmagic-f3-hair7-direct-locks');E=M/'reports/F3-hair7-direct-locks';D=R/'deliverables';jar=M/'build/libs/kelmagic-1.24.4-f3-hair7-direct-locks.jar';prior=Path('/data/experiments/kelmagic-f3-hair7-head-seat/KelMagic-F3-hair7-head-seat/build/libs/kelmagic-1.24.3-f3-hair7-head-seat.jar')
old=json.loads((E/'before-mesh.json').read_text());new=json.loads((E/'mesh.json').read_text());changed=[b['bone'] for a,b in zip(old,new) if a!=b];assert len(changed)==6 and all(n.startswith('hair6_front_') for n in changed)
G=M/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json';a=json.loads((E/'before.geo.json').read_text())['minecraft:geometry'][0]['bones'];b=json.loads(G.read_text())['minecraft:geometry'][0]['bones'];assert len(a)==len(b);assert all(x==y for x,y in zip(a,b) if x['name'] not in changed)
by={q['name']:q for q in b}
for n in changed:
 assert by[n]['parent'] in ['chest_left','chest_right']
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(prior) as oldz:
 assert z.read('assets/kelmagic/geo/red_mage.geo.json')==G.read_bytes();assert z.read('dev/kelmagic/client/hair/HeadSeat.class')==oldz.read('dev/kelmagic/client/hair/HeadSeat.class');assert z.read('dev/kelmagic/client/hair/Hair7Neck.class')==oldz.read('dev/kelmagic/client/hair/Hair7Neck.class');assert not any('/Qa' in n and n.endswith('.class') for n in z.namelist())
 pngs=[n for n in oldz.namelist() if n.startswith('assets/kelmagic/textures/') and n.endswith('.png')];assert all(z.read(n)==oldz.read(n) for n in pngs)
log=(R/'native.log').read_text(errors='replace');assert 'HAIR7_COMPLETE states=6' in log
facts={'changed_mesh_groups':changed,'upper_connection_is_one_straight_segment':True,'parent_head_dependency':False,'head_placement_runtime_unchanged':True,'neck_runtime_unchanged':True,'all_existing_PNGs_unchanged':len(pngs),'new_bones':0,'native_states':6,'native_frames':len(list((R/'native/screenshots').glob('*.png'))),'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest()};(E/'focused-check.json').write_text(json.dumps(facts,ensure_ascii=False,indent=2))
readme='''# KelMagic 1.24.4-f3-hair7-direct-locks

Изменение: обе грудные пряди теперь напрямую соединяют верхний внешний угол груди с нижним краем подбородка. Промежуточная точка подъёма/перегиба возле плеча удалена. Между подбородком и углом груди — один прямой диагональный участок (объёмная неглубокая подложка + те же два прозрачных текстурных слоя). Угол груди взят из существующей геометрии chest_left/right с учётом её поворота, не выбран на глаз.
Нижние окончания прядей сохранены. Пряди по-прежнему следуют chest_left/right → body → root, не голове. Посадка головы и укороченный подбородок не менялись. Боковые UV, шляпа, прочие волосы, PNG, лицо и анимации прежние. Новых костей нет. Main, D и E не менялись; коммитов нет.

proof/: стандартные игровые ракурсы и сравнение45°/сбоку с прежней линией.
native/:12 исходных PNG шести состояний и журнал. 90° — QA-стресс-тест, игровой ограничитель прежний. Выборочная проверка не является доказательством отсутствия всех пересечений в любых движениях.

JAR уже содержит изменения. Установка: Minecraft1.21.1, NeoForge21.1.256, GeckoLib4.9.3 для NeoForge. Замените старый KelMagic.jar новым, не оставляя обе версии в mods. Qa*-классы исключены из JAR.

project/: текущие исходники, ресурсы и Gradle.
evidence/: координаты прямого соединения, manifest quad-геометрии, точечная проверка.
Сборка: JAVA_HOME=/path/to/jdk21 bash gradlew -I tools/binary-build.init.gradle jar
Повтор изменения из сохранённой исходной точки: python tools/direct-locks/apply.py
Не запускайте генераторы прежних стадий: они вернут предыдущую конструкцию.

Остановлено после игровых показов для визуальной оценки пользователя.
'''
(D/'README-RU.md').write_text(readme);shutil.copy2(jar,D/jar.name)
archive=D/'KelMagic-hair7-direct-locks-source-and-proof.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 z.write(D/'README-RU.md','README-RU.md');z.write(jar,'jar/'+jar.name)
 for p in D.glob('*.png'):z.write(p,'proof/'+p.name)
 for p in (R/'native/screenshots').glob('*.png'):z.write(p,'native/'+p.name)
 z.write(R/'native.log','native/native.log')
 for p in E.glob('*.json'):z.write(p,'evidence/'+p.name)
 for folder in ['src','gradle','libs']:
  for p in (M/folder).rglob('*'):
   if p.is_file():z.write(p,'project/'+str(p.relative_to(M)))
 for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']:z.write(M/n,'project/'+n)
 for p in (M/'tools/direct-locks').glob('*.py'):z.write(p,'project/'+str(p.relative_to(M)))
 for n in ['binary-build.init.gradle','native-hair7.init.gradle','nbt19.py']:z.write(M/'tools'/n,'project/tools/'+n)
 for p in E.rglob('*'):
  if p.is_file():z.write(p,'project/'+str(p.relative_to(M)))
 z.write(M/'reports/F3-hair7-head-seat/mesh.json','project/reports/F3-hair7-head-seat/mesh.json')
print('JAR SHA256',facts['jar_sha256']);print('ZIP MB',round(archive.stat().st_size/1e6,2),'final frames',facts['native_frames'])
