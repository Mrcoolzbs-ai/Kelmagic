from pathlib import Path
import json,shutil,zipfile,hashlib,re
r=Path('/data/KelMagic');a=r/'docs/architecture';log=Path('/data/test-1.8-final.log').read_text();res=Path('/data/assets-1.8-final.log').read_text()
assert 'All 103 required tests passed' in log and 'CORE TESTS PASSED: 66088 checks' in log and 'BUILD SUCCESSFUL' in log
assert 'PASSED: 1109' in res and 'PASSED: 726' in res
for n in ['START_HERE.md','docs/architecture/RELEASE-1.8.0.md']:
 p=r/n;p.write_text(p.read_text().replace('призмариновые осколки 2–5','кристаллы призмарина 2–5'))
jar=r/'build/libs/kelmagic-1.8.0.jar'
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 assert '1.8.0' in z.read('META-INF/neoforge.mods.toml').decode()
 for cl in ['audio/DecorateTapeRecipe','server/NeutralMagic','server/WoolBarter','server/MagicProgress','core/PairRules','server/PairEffects','core/ManaFormat']:
  assert 'dev/kelmagic/'+cl+'.class' in z.namelist(),cl
 assert len([n for n in z.namelist() if n.startswith('data/kelmagic/advancement/') and n.endswith('.json') and '/recipes/' not in n])==18
 for p in (r/'src/main/resources').rglob('*'):
  if p.is_file():assert z.read(str(p.relative_to(r/'src/main/resources')))==p.read_bytes(),str(p)
sha=hashlib.sha256(jar.read_bytes()).hexdigest()
(a/'VERIFICATION-1.8.0.md').write_text(f'''# Проверки KelMagic 1.8.0

Проверен конкретный JAR kelmagic-1.8.0.jar (SHA256 `{sha}`), Minecraft1.21.1/NeoForge21.1.256/JDK21.

## Финальные результаты
- `BUILD SUCCESSFUL`.
- **66 088 core checks**: все 16 290 неупорядоченных пар, симметрия/определённость, масштаб, резонанс, дробная мана, прежняя грамматика и защитные правила. Это проверки разбора, не 16 290 отдельных испытаний эффектов в мире.
- **103 server GameTests** — все обязательные прошли. Предыдущие91 +12 новых: рецепты/золотая шерсть, оформление и сохранение компонентов, legacyint/fractionalNBT/packet, hostile/passive neutrality, реальная ответная атака коровы и persistedmarker, solпри взгляде в небо, мост у ног, большая стена, gaze dash, piglinbarter/cooldown/baby, полный обычный кодекс. Старый word101 дополнен проверкой реальной выдачи скрытого достижения до смерти.
- **1 835 resource checks**: 1 109 совместимых проверок +726 новых; JSON, runtimeactionhandlers, рецепты, текстуры/override-models,18advancements/parents/requirements/localizations, bartertable и tooltip.
- **20 отдельных офлайн-превью** просмотрены:9наклеек,4цвета,нить,шерсть,4страницыкодекса,плеер.8проверок границ новыхстраниц. Это композитные рендеры реальных PNG с bitmapfont, не скриншоты запущенного Minecraft.
- JAR ZIP CRC прошёл; каждая ресурсная запись совпала с текущимsrc/main/resources. SourceZIP CRC, текущийdistJAR и GradleWrapper проверены при упаковке.

## Команды
```bash
_JAVA_OPTIONS='-Xmx1800m -XX:ActiveProcessorCount=2' bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1 --console=plain
python tools/verify_revision_1_8.py
```
JDK21обязателен. Генератор overlay tools/generate_revision_1_8.py воспроизводит новые ресурсы; историческиегенераторыalone могут вернутьстарые рецепты/описания. Тестовые FakePlayers обычно имеют заглушку достижений — fixture здесь использует настоящий PlayerAdvancements. Это не обход игровых условий.

## Артефакты проверки
Полный финальный лог: TEST-1.8.0.log. Ресурсы: ASSETS-1.8.0.log. ОфлайнPNG: offline-previews-1.8.0/.

## Не проверено / известные границы
- Реальный графический клиент, разные GUIscales,2реальныхсетевыхклиента,шейдеры.
- Совместимость со всеми сторонними модами/AI/защитой территорий.
- Структуры/естественнаядобычарукописей из большой исходнойспецификации отсутствуют.
- Полнаяпарнаяграмматика имеет общий защитный fallback: разные пары не всегда означают уникальные результаты. Успех всёравно требует подходящую цель/опору/место.
- Скриптер расходуется после успешной записи в деке; шаблонфлага не расходуется;пиглинобмен мгновенный безванильной анимации.
''')
shutil.copy2('/data/test-1.8-final.log',a/'TEST-1.8.0.log');shutil.copy2('/data/assets-1.8-final.log',a/'ASSETS-1.8.0.log')
for p in (r/'dist').glob('*.jar'):p.unlink()
shutil.copy2(jar,r/'dist'/jar.name)
(r/'dist/SHA256SUMS.txt').write_text(sha+'  '+jar.name+'\n')
archive=Path('/data/KelMagic-source-1.8.0.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for p in sorted(r.rglob('*')):
  if not p.is_file():continue
  rel=p.relative_to(r)
  if rel.parts[0] in {'build','.gradle','run','.git'} or '__pycache__' in rel.parts or '.cache' in rel.parts:continue
  z.write(p,'KelMagic/'+str(rel))
with zipfile.ZipFile(archive) as z:
 assert z.testzip() is None
 assert z.read('KelMagic/dist/'+jar.name)==jar.read_bytes()
 assert z.read('KelMagic/gradle/wrapper/gradle-wrapper.jar')
 assert z.read('KelMagic/START_HERE.md')==(r/'START_HERE.md').read_bytes()
 assert len(json.loads(z.read('KelMagic/src/main/resources/data/kelmagic/magic/words.json'))['words'])==180
print('RELEASE VERIFIED: JAR',jar.stat().st_size,'bytes; sourceZIP',archive.stat().st_size,'bytes; SHA256',sha)
