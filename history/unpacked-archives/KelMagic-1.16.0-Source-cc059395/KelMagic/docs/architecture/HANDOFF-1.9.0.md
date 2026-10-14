# KelMagic 1.9.0 — передача проекта

Сначала START_HERE.md, VERIFICATION-1.9.0.md, CATALOG-1.9.0.md, RITUALS-1.9.0.md и текущие исходники. Платформа 1.21.1/NeoForge21.1.256/JDK21. Сеть6, старые сохранения читаются. История 1.8 в START_HERE-1.8.0.md/HANDOFF-1.8.0.md.

WordRarity читает magic/rarities.json (180;77/73/18/12). Редкость страниц через Properties; AncientManuscript фильтрует по DataComponents.RARITY (default COMMON). Высокие уровни торгов архивариуса дают другие уровни; не смешивать tiers с rarity combinations. riv теперь base3 источника, mar9.
Vid в ExpandedSpellEffects.targets: scan центр всегдаcaster, без LOS-фильтра; non-scan LOS сохранён. MagicLamps TORCH14/LANTERN15 noLoot/noItems; WorldSpells.state заменяет torch/sea_lantern. Purple dust иtexture, не RGB-light engine.

CodexOpen расширен section/page/query/token. CodexPosition server bounds+matching heldbooktoken; components persisted. Токен создаётся при открытии, копия с существующим токеном получает новый. Кодекс20артефактов. Не менять первые6индексов заметок и glyphfont.

Стол: archive/ArchiveTable* +MagicStudy. 4input+derivedoutput. Helpers ArchiveRecipe/CodexCopyRecipe остались, но их crafting JSON удалены — только стол. Выход не сохраняется и не выпадает; input сохранён. finish послеonTake потребляет1каждого input, оригинал остаётся приcopy; slotoutputremove может вызыватьrefresh, тест quickMove+duplicate packet обязателен. Пустые кодексы не копируются. Реальные menu fixtures проверены.

Профессия/Poi: MagicStudy и ArchivistTrades; обязательно minecraft:acquirable_job_site tag. Client ArchivistClothesMixin вtyped render(LivingEntity...) сохраняетbiome, getResourceLocation RETURN подменяет только kelmagic:archivist profession texture;7UV64x64 outfits + metadata hatpartial. Unknownbiome→plains fallback. NativeVillagerModel jacketUV0,38,front6,44..14,64: не путать сbody16,20. Компоненты ничего не знают о Notion.

10NBT archive/[biome]_[1,2].nbt сvanilla building_entrance jigsaw ичестомLootTable;5housespool overlays исходныеweights×8 +2archiveweight1. Этоdataoverride, не runtimeappend; конфликтcustompacks возможен. generate_revision_1_9.py +nbt19.py полностью воспроизводят структуры/resources;5nativehouse lootтаблиц+35%commonAncient. Zombiepool иjungle/swamp village generation не добавлялись.

GrandSpells parserGRAND дообщихпар/дубликатов. Exactmultiset обязательныхroots после удаления muk/nis,minimum count,MAX100. Transcend20rootphrase×5exact100. GrandEffects: true spectralelytra через2commonmixins canElytraFly/elytraFlightTick; terrainmove island radius3..5depth3lift12 atomic BlockSnapshots/BreakEvent+placeevents; blast bounded16; clonefreshUUID nonplayers includesNBTinventory removespassengers/leash; ownenderChestMenu;fixed+50max1200ticks;40tick meteor withcap16pending andloadedtarget;truecreative60tickleaseprevious+deadline persisted/copyOnDeath restorelogin/logout/stop/deadline. Послеdeath следующийplayertick restore; login lease всегдаrestore. Не оставлять creative послеexit/crash.

MagicPortals temporaryblock100scheduledticks usesDimensionTransition toEndplatform /globalOverworldspawn (неbed), standardcooldown. Noitems/recipe/drop. Не утверждать проверку реальной межмерной сессии игрока: таймер/регистрация проверены, API native。

WoolItem.isPiglinCurrency +piglin_loved; directWoolBarter sets offhand andADMIRING120ticks; vanilla droppedwalk/pickup. PiglinWoolMixin snapshots flag HEAD stopHoldingOffhand, getBarterResponseItems inject customloot, flagclearRETURN. Gold unchanged; golden thread removed custompool; weightsnow25/25/20/5/5. GameTest real pig brain usesh.spawn(), НЕ spawnWithNoFreeWill (удаляетповедение). Для directreward fixture требуется player inServerLevel.players: иначе vanilla randomthrow target может отсутствовать и тест flaky. FixtureaddNewPlayer creative onlytest, then discarded.

Armor: all8items trimmable tag, vanillaSmithingTrimRecipe сохраняетname/dye/manaBonus. Itempropertytrimmed+coloredlayer2 viaTrimAppearance;robe layer0dye/layer1fixed. Надетыеpatterns vanillaHumanoidArmorLayer; не утверждатьnativeUVvisualtest custom256garmentmodel. New14advancements total32,9rites hidden; descriptions noformulas. MagicProgress armor/inventory/craft checks.

GameTest FakePlayer stubsadvancements иopenMenu: fixture overridesreal PlayerAdvancements and serverMenuProvider create; не исправлять productionкод радиstub. 122requiredtests, core/resource/evidencecounts вVERIFICATION. Офлайн46PNG — simplified structureblocks, UVfrontproofoutfits, bitmapUI/itemlayers; НЕMinecraftscreenshots. Nativeclient/gui scale/two clients/shaders/worldseed village distribution not measured.

Сохранять PvP/protectionevents/selfdamage/menu-holderlocking/serverauth/savecompatibility/ArcaneFire rules. Не обещать16290bespoke pair effects. Старые генераторы возвратят старые рецепты/tooltip/loot: latestoverlay должен идти последним. docs/architecture/ADVANCEMENT_POLICY.md читать перед будущими обновлениями.
