# KelMagic 1.8.0 — передача проекта

Начать с START_HERE.md, VERIFICATION-1.8.0.md, CATALOG-1.8.0.md и фактических исходников. Minecraft 1.21.1 / NeoForge21.1.256 / Java21.

Текущие 12 пунктов реализованы с явно описанными трактовками: 8 золотых нитей на рецепт; потребление скриптера только в конце записи кассеты; шаблон флага возвращается; общий защитный отклик для прежде неопределённых пар не уникален; sol требует опору рядом.

Код: MagicMaterials; audio/DecorateTapeRecipe; server/MagicProgress, NeutralMagic, WoolBarter, PairEffects; core/PairRules, ManaFormat; ConfigMigration. Mana теперь double (CODEC читает старые ints); SpellSnapshot writeDouble/readDouble, сеть версии5. Полный комплект +10 маны, ограничение сохранения1024. Не округлять снаряжение или расход до integer.

Пары: SpellParser.plan отбирает две базовые лексемы после muk/nis; PairRules.corePlan сохраняет именованные сочетания и обычные действия. Резонанс повторов ×2, rak rap gaze dash; неполные селекторы/необратимое no → защитный PairEffects. Все16290 unordered pairs проверены; не утверждать bespoke эффекты для каждой. WorldSpells масштабирует 16 именованных сочетаний, max96 edits и пределы формы.

NeutralMagic сохраняет kind/anger; installed goal transient. LivingChangeTargetEvent отменяет запретную смену цели; не setNewTarget(null), которое не отменяет другое назначение. Passive RetaliateGoal не использует отсутствующий у коровы AttackDamage; тикает2HP/20ticks. Мозговые ATTACK_TARGET/ANGRY_AT очищаются. Цель допускается только последний атакующий на200ticks.

WoolBarter только взрослый спокойный обычный Piglin, пустая offhand,5blocks,100tick delay; loot context PIGLIN_BARTER. Не имитирует стандартное любование золотом. Дроп собственный loot_table.

Кассеты: SPELL сохранён, vanilla DYED_COLOR корпуса, TAPE_PATTERN строка ID шаблона. ItemColors толькоlayer0; trimfixed;9вариантов наклейки через itemproperty. Customrecipe ровно1cassette+slime+optional1dye+optional1pattern; сохранение имени/record/components; reusablepattern. В деке consumption1scripter наcompleted record; отмена/выходoccupied безconsume.

18 impossiblecriteria advancements явно requirements[[done]]. MagicProgress даёт их по server gameplay. GameTest FakePlayer НЕ хранит advancements: TestPlayer теперь переопределяет getAdvancements реальным PlayerAdvancements в отдельномtestpath. Не лечить productionкод из-за stub FakePlayer. Secretcreativecodex не засчитывается.

Актуальный overlays generator tools/generate_revision_1_8.py. Он должен идти ПОСЛЕ старых генераторов, которые иначе вернут устаревшие рецепты/описания и tooltip. Аудит verify_revision_1_8.py. Перед будущимобновлением читать ADVANCEMENT_POLICY. Сохранить первые6noteindices, glyphfont, sin/nis, PvP, protectionevents, playerholderlock, serverauthoritative packets, magicfire специальные правила. Не создавать произвольные spell packets.

Офлайн превью: docs/architecture/offline-previews-1.8.0 (НЕ реальные скриншоты). Сборка, тесты, упаковка и ограничения в VERIFICATION. История START_HERE-1.7.0.md и HANDOFF-1.7.0.md. Не выдавать старые логи за новую проверку.
