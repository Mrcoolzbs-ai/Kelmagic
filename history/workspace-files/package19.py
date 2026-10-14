from pathlib import Path
import zipfile,json,shutil,hashlib,re
r=Path('/data/KelMagic');a=r/'docs/architecture';log=Path('/data/test-1.9-final4.log').read_text();assets=Path('/data/assets-1.9-final.log').read_text()
assert 'All 122 required tests passed' in log and 'CORE TESTS PASSED: 66124 checks' in log and 'BUILD SUCCESSFUL' in log
assert 'PASSED: 20083' in assets
jar=r/'build/libs/kelmagic-1.9.0.jar'
with zipfile.ZipFile(jar) as z:
 assert z.testzip() is None
 assert '1.9.0' in z.read('META-INF/neoforge.mods.toml').decode()
 for c in ['core/WordRarity','core/GrandSpells','server/GrandEffects','archive/ArchiveTableBlock','archive/ArchiveTableEntity','archive/ArchiveTableMenu','MagicStudy','MagicLamps','MagicPortals','server/ArchivistTrades','mixin/SpectralFlightMixin','mixin/SpectralPlayerMixin','mixin/PiglinWoolMixin','mixin/ArchivistClothesMixin','network/CodexPosition','client/TrimAppearance']:
  assert 'dev/kelmagic/'+c+'.class' in z.namelist(),c
 assert len([x for x in z.namelist() if x.startswith('data/kelmagic/advancement/') and x.endswith('.json') and '/recipes/' not in x])==32
 assert len([x for x in z.namelist() if x.startswith('data/kelmagic/structure/archive/') and x.endswith('.nbt')])==10
 for p in (r/'src/main/resources').rglob('*'):
  if p.is_file():assert z.read(str(p.relative_to(r/'src/main/resources')))==p.read_bytes(),str(p)
 assert 'data/kelmagic/recipe/add_manuscript.json' not in z.namelist()
 assert 'data/kelmagic/recipe/copy_codex.json' not in z.namelist()
sha=hashlib.sha256(jar.read_bytes()).hexdigest()
(a/'VERIFICATION-1.9.0.md').write_text(f'''# Проверка KelMagic 1.9.0

Проверен конкретный JAR kelmagic-1.9.0.jar, SHA256 `{sha}`. Minecraft1.21.1 / NeoForge21.1.256 / Java21.

## Результаты
- **BUILD SUCCESSFUL**.
- **66 124 core checks**: прежняя грамматика/16290пар, все9великих формул, обязательные корни, минимумы/100wordlimit, порядок слов, mana/session/networklogic. Парные проверки — разбор, не16290отдельныхэффектов вмире.
- **122 required server GameTests**, все прошли. Сохранены103проверки1.8 (обновлены изменённые правила), добавлены19: rarity180pages; vidзаигроком/стеной; codexsavedposition/token; tablemerge/copy/workbenchdenial; ninegrandthresholds; actualno-elytraitemflight; actualislandmove; creativerestore/deadline/logout; temporaryportal100ticks; fixedmana+50/expiry; tenNBTloads/pools/realarchiveplacement; piglindirectadmiration/customreward; droppedwoolapproach/pickup/completion; entityfreshUUIDcopiedequipment; tablequickMoveexactconsumption/staleTake; vanillaSmithingTrimRecipe all8garments/name/dye/bonus; POIjobtag+allrarityreveal; personalenderinventorymenu; trueexplosionradiusincrease.
- **20 083 resource checks**: canonicalraritycoverage, JSON/models/textures,32criteria/requirements/locales,10NBTpalette/blockbounds/jigsaw/table/loot,5nativepoolswithalloriginalentriespreserved,7outfits,trimmable8items,noitem/craft/dropmagiclights,no golden thread woolloot.
- **46 отдельных офлайн PNG** просмотрены: 3статастола,7clothingUVproofs,20schematicexterior/cutawayarchives,3light/portaltextures,3raritypages,8trimmeditemcomposites,tablemodel,newcodexartifactpage. Обрезанныйракурсархивов и ошибочнаяUVразвёрткаодеждыисправлены передфинальнымпросмотром.
- JAR ZIP CRC; каждая src/main/resources запись совпадает сJAR.32top-leveladvancements,10structures,newclassesи отсутствиеобычныхcraftingJSONдляmerge/copyпроверены. SourceZIPCRC,distJARbyteequal,Wrapper и180слов проверены приупаковке.

## Воспроизведение
```bash
_JAVA_OPTIONS='-Xmx1800m -XX:ActiveProcessorCount=2' bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1 --console=plain
python tools/verify_revision_1_9.py
```
НуженJDK21. Генераторresources: tools/generate_revision_1_9.py (helper nbt19.py). Wordrarities.json — версияданных, неслучайнаяоценкакаждойзагрузки. Историческиегенераторыaloneмогутоткатитьизменения.

## Логи и доказательства
TEST-1.9.0.log, ASSETS-1.9.0.log, offline-previews-1.9.0/.
Офлайнрендеры **не скриншоты запущенногоMinecraft**: структурысхематические с упрощёнными кубами/материалами, одежда — UVfrontproof, UI — bitmaplayout, itemtrim — реальные PNGслои. Не использовать их как доказательство nativeclient/shader/worldlighting.

## Границы проверки
- Не запущен реальный графический клиент, не проверены GUIscales,2реальныхmultiplayer-клиента,shaderpacks инадетыеtrimpatternsнаcustomarmorUV.
- Переход живого игрока черезEndgate междуизмерениями не проверялся нативной сессией; Block+scheduledexpiry/APItransition проверены отдельно. Метеорнаятаймернаяочередь реализована, но отдельногоGameTestреального40tickmeteorimpactнет. Реальноезаселениепрофессииподдневнымрасписаниемжителя/многоseedvillagegenerationнеизмерялись; POI/tag/пулы/templatesзагружены,одинреальныйшаблонразмещён.
- RGBосвещение вvanillaотсутствует; нежно-фиолетовыетекстуры/частицы есть,цветокружениянеобещается.
- 5housespoolJSONoverrides могутконфликтоватьсдругимиdatapacks. Rarechance — вескаждогоhouseelement,нефиксированныйпроцентдеревень. Толькоnewchunks,незомбиvillagepools.
- Высшаямагия намеренноопасна: inventorydup/truecreative/explosions. Настройкизащитыпрочихмодовмогутпомешать; совместимостьсовсемиAI/защитойнепроверена.

FakePlayer поумолчаниюзаглушаетadvancements/menuopening иspawnWithNoFreeWillубираетповедениеAI. ВfixtureиспользованыrealPlayerAdvancements/servermenus/normalpigspawn; directbarterfixtureplayerвидендляvanillasensorиубираетсяпослетеста. Не выдаватьизменениеfixtureзаобходproductionусловий.
''')
for p in (r/'dist').glob('*.jar'):p.unlink()
shutil.copy2(jar,r/'dist'/jar.name);(r/'dist/SHA256SUMS.txt').write_text(sha+'  '+jar.name+'\n')
archive=Path('/data/KelMagic-source-1.9.0.zip')
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
 assert len(json.loads(z.read('KelMagic/docs/kel-language/CATALOG-1.9.0.json'))['words'])==180
 for spell in json.loads(z.read('KelMagic/docs/kel-language/RITUALS-1.9.0.json'))['rites']:assert len(spell['formula'])==spell['minimum']
print('RELEASE VERIFIED: JAR',jar.stat().st_size,'bytes; source ZIP',archive.stat().st_size,'bytes; SHA256',sha)
