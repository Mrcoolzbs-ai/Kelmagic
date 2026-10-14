# Проверка KelMagic 1.9.0

Проверен конкретный JAR kelmagic-1.9.0.jar, SHA256 `6a1f1c5e67b7170d2236d891b9ea774f165b7817003bb4c07909805b8b176ffa`. Minecraft1.21.1 / NeoForge21.1.256 / Java21.

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
