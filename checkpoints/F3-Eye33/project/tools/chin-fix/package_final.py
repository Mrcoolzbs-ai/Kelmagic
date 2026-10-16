from pathlib import Path
import json,zipfile,hashlib,shutil
M=Path(__file__).resolve().parents[2];R=Path('/data/reports/kelmagic-f3-hair7-chin-fix');E=M/'reports/F3-hair7-chin-fix';D=R/'deliverables';jar=M/'build/libs/kelmagic-1.24.2-f3-hair7-chin-fix.jar';G=M/'src/main/resources/assets/kelmagic/geo/red_mage.geo.json';old=json.loads((E/'before.geo.json').read_text())['minecraft:geometry'][0]['bones'];new=json.loads(G.read_text())['minecraft:geometry'][0]['bones'];assert len(old)==len(new)
by={b['name']:b for b in new};changed=[b['name'] for a,b in zip(old,new) if a!=b]
for a,b in zip(old,new):
 if a!=b:assert b['name']=='head' or b['name'].startswith('hair6_')
for side in ['left','right']:
 for tier in ['base','under','over']:
  n='hair6_front_'+side+'_'+tier;chain=[]
  while n in by and 'parent' in by[n]:n=by[n]['parent'];chain.append(n)
  assert 'head' not in chain
floor=json.loads((E/'changes.json').read_text())['new_head_bottom'];mesh=json.loads((E/'mesh.json').read_text());assert all(v[1]>=floor-1e-6 for e in mesh if e['parent']=='head' for q in e['faces'] for v in q)
with zipfile.ZipFile(jar) as z:
 names=z.namelist();assert z.read('assets/kelmagic/geo/red_mage.geo.json')==G.read_bytes();assert not any('/Qa' in s and s.endswith('.class') for s in names)
 with zipfile.ZipFile('/data/.agent-service/files/892d00be-258a-4ff2-94b9-067b113ce8f9/Hair7-neck-position-fix.zip') as before:
  pngs=[s for s in before.namelist() if s.startswith('project/src/main/resources/assets/kelmagic/textures/') and s.endswith('.png')];assert all(before.read(s)==z.read(s.removeprefix('project/src/main/resources/')) for s in pngs)
log=(R/'native-final.log').read_text(errors='replace');assert 'HAIR7_COMPLETE states=7' in log
facts={'changed_geo_descriptors':changed,'new_bones':0,'unchanged_existing_PNGs':len(pngs),'chin_ratio':0.2,'new_head_bottom_y':floor,'neck_lift_pixels':1,'chest_no_head_parent':True,'all_head_hair_meshes_above_new_jaw_line':True,'exact_current_geo_in_JAR':True,'QA_classes_in_JAR':False,'native_states':7,'native_frames':len(list((R/'native/screenshots').glob('*.png'))),'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest()};(E/'focused-check.json').write_text(json.dumps(facts,ensure_ascii=False,indent=2))
readme='''# KelMagic — Hair7: подбородок и соединение волос
Версия: 1.24.2-f3-hair7-chin-fix

## Последние правки ВКЛЮЧЕНЫ в этот JAR
1. Неподвижные плоские поверхности вокруг шеи подняты ровно на один модельный пиксель относительно их предыдущей позиции у поверхности тела. Внутрь тела их больше не оставляли. Это геометрическое смещение, а не изменение текстуры/шейдера.
2. Верх обеих грудных прядей подведён к новой нижней границе подбородка. Нижние участки сохраняют прежнюю форму и направление. Пряди остаются под chest_left/right → body → root; связи с head нет.
3. Участок подбородка под нижним краем PNG-рта уменьшен до20% прежней высоты: 1.51 →0.302 модельного пикселя. Нижняя граница головы поднята сY24 доY25.208. Глаза, нос и PNG-рот не масштабировались и не переносились.
4. По зелёной разметке также срезана узкая нижняя полоса головной части волос сбоку и возле затылка до той же линии. Нижние поверхности объёмных подложек закрыты. Верхняя часть причёски, чёлка, макушка и длинная масса сзади не перестраивались.
Все существующие PNG сохранены побайтно. Логика лица, артикуляции, прищура, дыхания и прочих анимаций не менялась. Новых костей/объёмных прядей на шею нет. Статичные поверхности шеи не имеют костей и анимационных каналов; они рисуются в локальных координатах персонажа.

Работа выполнена в отдельной восстановленной копии. Main, D, E и предыдущие варианты не изменялись. Коммитов нет.

## Игровые показы
proof/01-standard-views.png: фронт, камера45°, бок, спина, голова45° и90°.
proof/02-before-after.png: прежние сохранённые игровые кадры → окончательная версия, фронт и бок.
proof/03-neck-lift.png: дополнительный игровой QA-ракурс с временно скрытыми головой и задними волосами, открывающий переход вокруг шеи.
native/: финальные14 исходных PNG, семь состояний. Кропы без ретуши. 90° — dev-only стресс-тест; обычный игровой ограничитель не изменён. Перед финальным прогоном был один такой же короткий просмотр, на котором обнаружен оставшийся нижний край волос головы; после исправления повторены те же состояния. Промежуточные кадры в выдачу не включены.
Это ограниченная визуальная проверка, не доказательство отсутствия всех видов clipping/мерцания во всех анимациях. Визуальное утверждение оставлено пользователю.

## Установка
Minecraft1.21.1, NeoForge21.1.256; GeckoLib4.9.3 для NeoForge.
Замените прежний KelMagic.jar в mods этим JAR. Не оставляйте одновременно две версии KelMagic.
Qa*-классы в передаваемый JAR НЕ включены. Показанная окончательная геометрия совпадает с ресурсом внутри JAR, что проверено по байтам.

## Архив исходников и воспроизведение
project/ — текущий проект, исходники/ресурсы/Gradle и зависимости GeckoLib.
evidence/ — параметры правок, точечные проверки, manifest quad-геометрии.
Сборка: JAVA_HOME=/path/to/jdk21 bash gradlew -I tools/binary-build.init.gradle jar
Правки из сохранённой исходной точки: python tools/chin-fix/apply.py, затем python tools/chin-fix/finish_head_rim.py.
Не запускайте старые генераторы hair6/hair7: они восстанавливают предыдущую конструкцию.
Стенд: bash gradlew -I tools/binary-build.init.gradle -I tools/native-hair7.init.gradle runClient (нужен QA-мир KelMagicHairQA).
Игровая среда после восстановления была настроена заново; ресурсы мода и оригинальные кадры до изменения восстановлены из сохранённого архива, а не воссозданы по картинкам.

Передача завершена. Дальнейшие изменения — после визуальной оценки пользователя.
'''
(D/'README-RU.md').write_text(readme);shutil.copy2(jar,D/jar.name)
archive=D/'KelMagic-hair7-chin-fix-source-and-proof.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 z.write(D/'README-RU.md','README-RU.md');z.write(jar,'jar/'+jar.name)
 for p in D.glob('*.png'):z.write(p,'proof/'+p.name)
 for p in (R/'native/screenshots').glob('*.png'):z.write(p,'native/'+p.name)
 z.write(R/'native-final.log','native/native-final.log')
 for p in E.glob('*.json'):z.write(p,'evidence/'+p.name)
 for folder in ['src','gradle','libs']:
  for p in (M/folder).rglob('*'):
   if p.is_file():z.write(p,'project/'+str(p.relative_to(M)))
 for n in ['build.gradle','settings.gradle','gradle.properties','gradlew','gradlew.bat']:z.write(M/n,'project/'+n)
 for p in (M/'tools/chin-fix').glob('*.py'):z.write(p,'project/'+str(p.relative_to(M)))
 for n in ['binary-build.init.gradle','native-hair7.init.gradle','nbt19.py']:z.write(M/'tools'/n,'project/tools/'+n)
 for folder in ['reports/F3-hair7-chin-fix','reports/F3-hair7-neck-fix']:
  for p in (M/folder).rglob('*'):
   if p.is_file():z.write(p,'project/'+str(p.relative_to(M)))
print('Final JAR bytes',jar.stat().st_size,'SHA256',facts['jar_sha256']);print('Source/proof ZIP MB',round(archive.stat().st_size/1e6,2));print('Final native states',facts['native_states'],'frames',facts['native_frames'])
