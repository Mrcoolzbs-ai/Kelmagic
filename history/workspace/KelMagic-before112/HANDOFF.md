# Продолжение KelMagic1.10.0

Сначала START_HERE.md, COMMANDS.md, VERIFICATION-1.10.0.md. Исторический HANDOFF1.9 в docs/architecture.

Главные исправления: SpectralLocalPlayerMixin — дополнительный клиентский canElytraFly; ElytraLayerMixin настоящее тело крыльев. MagicTrimMixin заменяет ванильный UV pass для GarmentModel: trimTexture захватывается расширением getHumanoidArmorModel на каждом слоте. generate_revision_1_10.py читает локальный native-assets JAR:18 patterns×10palettes×8garments. Цвета на layer2 белые, потому что текстуры уже окрашены точной палитрой. Не запускать старые генераторы без overlay1.10.

MagicLamps wall torch+standingwall item; client LampRendering cutout; torch16×16. PortalGate собственный валидный BlockEntityType наследует TheEndPortalBlockEntity, native renderer, scheduled100ticks.

MagicCommands — op2admin ветка, bounds, persisted BASE_MANA_MAX=-1 default. Override задаёт общий постоянный максимум, одежда/сердце не суммируются, boost+50 сверху. locate ограничен известными загруженными structure starts, не fullworld locate. Не объявлять реализованной глобальную генерационную навигацию.

GrandEffects island snapshot transaction: irregular columns depth2–7, structures up32; noContainer/MenuProvider/ItemHandler inventories; noninventoryNBT preserved. Source unloaded columns skipped, destination unobstructed+loaded required. Break/place events/rollback remain. GameTest ставит невидимую barrierroof в tiny8×4×8 template; clearIslandTestRoof удаляет только тестовые barrier выше5 в собственной области. Нельзя удалять проверку препятствий production радиfixtures.

Destructive grandScale linear unbounded perrepeat underword100; other spellScale remains oldbounded1.5exponent. ManaTide holder status removalevents detachboost; classic MANA_BOOST_END stays savecompatible.

Qa110.java developer-only excluded from productionJAR. Native captures require local Xvfb+Minecraft client, optional init toenable kelmagic.visualQA. Allentities test actors only renderer, no personal data. Developer scripts have no effect in normalplay.

Futureupdates mustaddcommands+achievements where relevant and update tests/docs. Preserve serverauthority, inventorylocks, deathmessages, PvP/protection, rollback, original100-word overload and3sec creative restoration.
