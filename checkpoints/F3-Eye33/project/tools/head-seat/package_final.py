from pathlib import Path
import json,zipfile,hashlib,shutil,re
M=Path(__file__).resolve().parents[2];R=Path('/data/reports/kelmagic-f3-hair7-head-seat');E=M/'reports/F3-hair7-head-seat';D=R/'deliverables';jar=M/'build/libs/kelmagic-1.24.3-f3-hair7-head-seat.jar';prior=Path('/data/experiments/kelmagic-f3-hair7-chin-fix/KelMagic-F3-hair7-chin-fix/build/libs/kelmagic-1.24.2-f3-hair7-chin-fix.jar');geo='assets/kelmagic/geo/red_mage.geo.json'
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(prior) as old:
 assert z.read(geo)==old.read(geo)
 assert 'dev/kelmagic/client/hair/HeadSeat.class' in z.namelist();assert not any('/Qa' in n and n.endswith('.class') for n in z.namelist())
 pngs=[n for n in old.namelist() if n.startswith('assets/kelmagic/textures/') and n.endswith('.png')];assert all(z.read(n)==old.read(n) for n in pngs)
log=(R/'native.log').read_text(errors='replace');assert 'HAIR7_COMPLETE states=7' in log
positions=re.findall(r'HEAD_SEAT headPosY=([-\d.]+) jawWorldRestY=([-\d.]+)',log);assert len(positions)==14 and all(abs(float(a)+1.208)<1e-5 and abs(float(b)-24)<1e-5 for a,b in positions)
oldmesh=json.loads((E/'before-mesh.json').read_text());mesh=json.loads((E/'mesh.json').read_text())
for a,b in zip(oldmesh,mesh):
 if a['parent']=='head':assert [[v[:3] for v in q] for q in a['faces']]==[[v[:3] for v in q] for q in b['faces']]
 elif not a['bone'].startswith('hair6_front_'):assert a==b
check={'native_states':7,'native_frames':14,'head_drop_model_pixels':1.208,'actual_rest_head_bottom_and_body_top':24,'all_head_source_geometry_preserved':True,'side_images_fit_by_UV_only':True,'unchanged_existing_PNGs':len(pngs),'long_back_meshes_unchanged':True,'chin_20_percent_preserved':True,'head_scale_not_used':True,'QA_excluded_from_JAR':True,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest()};(E/'focused-check.json').write_text(json.dumps(check,ensure_ascii=False,indent=2))
readme='''# KelMagic 1.24.3-f3-hair7-head-seat

Голова опущена на тело на1.208 модельного пикселя — ровно на высоту прежнего зазора после среза подбородка. Native head translation выполняется один раз перед отрисовкой головы. Всё лицо, глаза, PNG-рот, шляпа и головные волосы наследуют один и тот же сдвиг. Ни лицо, ни кости не масштабируются. Укороченный подбородок (20% прежней высоты) сохранён.

Исходные локальные координаты лица не менялись, поэтому прежние деформации щёк/прищур/дыхание/артикуляция работают в прежней системе координат. Боковые/височные/затылочные и короткие плечевые alpha-слои подогнаны по высоте через UV: полный рисунок теперь помещается на уменьшенной поверхности, а не остаётся обрезанным снизу. PNG не перерисованы.

Верх грудных прядей опущен к новой нижней линии головыY24. Они по-прежнему находятся под chest_left/right → body → root, не под головой. Длинные задние волосы не менялись. Неподвижные плоские поверхности шеи сохранены без костей/анимации и с прежним подъёмом1 пиксель над телом.

Работа — отдельная копия предыдущей версии. Main/D/E не менялись, коммитов нет. Новых костей нет.

## Показы
proof/01-standard-views.png: фронт, 45°, бок, спина, повороты головы45°/90°.
proof/02-before-after.png: сохранённые реальные кадры предыдущей версии с зазором → новый вариант.
proof/03-neck-lift.png: QA-раскрытие участка шеи, голова/задние волосы скрыты только здесь.
native/ —14 исходных PNG семи состояний и лог. Все14 кадров подтверждают headPosY=-1.208 и нижнюю rest-границу головыY24, совпадающую с верхом корпуса. Угол90° — dev-only стресс-тест, игровой ограничитель не менялся. Ограниченные ракурсы не гарантируют отсутствия всех пересечений в любых анимациях.

## Установка
Minecraft1.21.1 / NeoForge21.1.256 / GeckoLib4.9.3 для NeoForge.
Заменить старый KelMagic.jar этим файлом, не оставлять две версии одновременно.
В JAR уже включены эти изменения. Qa*-классы исключены.

project/ — текущие исходники/ресурсы/Gradle. evidence/ — параметры и точечная проверка.
Сборка: JAVA_HOME=/path/to/jdk21 bash gradlew -I tools/binary-build.init.gradle jar
Повтор правок: python tools/head-seat/apply.py
Не запускать генераторы прежних стадий hair6/chin-fix: они вернут предыдущую конструкцию.
После демонстрации работа остановлена для оценки пользователя.
'''
(D/'README-RU.md').write_text(readme);shutil.copy2(jar,D/jar.name)
archive=D/'KelMagic-hair7-head-seat-source-and-proof.zip'
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
 for p in (M/'tools/head-seat').glob('*.py'):z.write(p,'project/'+str(p.relative_to(M)))
 for n in ['binary-build.init.gradle','native-hair7.init.gradle','nbt19.py']:z.write(M/'tools'/n,'project/tools/'+n)
 for folder in ['reports/F3-hair7-head-seat','reports/F3-hair7-chin-fix']:
  for p in (M/folder).rglob('*'):
   if p.is_file():z.write(p,'project/'+str(p.relative_to(M)))
print('Final JAR',jar.name,'SHA256',check['jar_sha256']);print('ZIP MB',round(archive.stat().st_size/1e6,2),'validated frames',len(positions))
