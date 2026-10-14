# Передача KelMagic1.14.0
Прочитать START_HERE,COMMANDS,VERIFICATION-1.14.0. Minecraft1.21.1/NeoForge21.1.256/Java21;протокол6.
- ItemLore:только WordManuscriptItem и ancient_manuscript. Никаких Shift/gameplay заметок в ItemLore/CodexScreen. Лор,переводы,рецепты,личные заметки сохранены. SpellProfiles.mechanics — внутренняя справка,не UI.
- MagicCopper:74 distinct GrandSpells.formula words×4ages×raw/wax=592. Родной WeatheringCopperFullBlock дляraw,обычный Block с native copied waxproperties дляwax. NeoForge block data_maps oxidizables222/waxables296,replace:false. Не модифицировать vanilla BiMap:патчи используют DataMapHooks. Топоры/молнии/соты нативные.
- generate_copper_1_14.py:canonical font atlas/glyph-map,222textures64×64,592models/recipes/selfdrops. Oxidized напрямую minecraft:block/oxidized_copper,безcustomизображения;невидимость не потеряID.
- generate_archives_1_14.py:15NBT,центрированные двери,зеркальный передний фасад/навес/ставни/полный порог,3стула+столикивкаждом. Старые чанки не менять. archiveweight3,vanillaвесаоригинальные.
- ArchiveSearch прежний асинхронный locate:2задачи/120с/target2мс. Mainthread,getChunkFutureMainThread invoker,безjoin/get;ближесначала внутрикольца,первый проверенный реальный архив,не globalnearest. archive_route не возвращать.35visibleadvancements=старые34+copper_word;14hiddenrecipe.
- 180слов/172эффекта/1440trimtextures/80itemtrimmodels/зеркальныеUV/оптимизации сохранены. Лор1.13 — пользовательская теория,не официальный канон.
- QaShowcase создаёт настоящий save,QaWorldInspect повторно открывает без генерации. Все Qa* исключены из JAR. Мир:15домов/20архивистскихстолов/45обеденных/592медныхэкспоната. Естественнаягенерациявыключена;locateне ищет экспонаты.
- Проверки:66 124core,144requiredGameTests,44 863resourcechecks. Native loot/cutter/maps/honeycomb/axe/randomticks/lightning/youngerneighbors/command/advancement. Не заявлять FPS ноутбука по llvmpipe.
Не запускать старые генераторы поверх1.14.4GBRAM:клиент иGameTestServer последовательно. ПередвыдачейCRC,JARресурсыпобайтно,excludedQa;мирzipтолькопослеcleanshutdown.
