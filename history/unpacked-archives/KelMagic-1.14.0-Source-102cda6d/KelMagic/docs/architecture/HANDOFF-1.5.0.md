# KelMagic 1.5.0 — передача проекта

Текущее описание: START_HERE.md. Полный промпт передачи: CONTINUATION_PROMPT.txt. Первоначальные требования сохранены в docs/requirements/.

Стабильная база: 1.21.1 / NeoForge 21.1.256 / Java 21, пакет dev.kelmagic. Не менять платформу и словарь без согласования.

Изменения 1.5.0:
- SpellSession.regenDue больше не требует active(); append/clear не сбрасывают regenTicks. MagicServer остаётся единственным серверным владельцем восстановления, интервал 200/100/60 ticks и ограничение максимумом сохранены.
- Garment geometry отделена от целочисленных UV: piece() создаёт целочисленный куб, bake() масштабирует его до исходного физического размера, включая начало координат. UV painter использует те же целые размеры. 43 части балахона / 68 частей лат. chin_guard удалён. Ядро маны не изменено.
- Новый авторский слиток без электронного орнамента.
- MagicFireBlock наследует FireBlock для обычных directional properties, survival, shape, дыма и ambient sound, но не вызывает ванильное portal onPlace. stateAt/updateShape всегда сохраняют собственный тип блока. Собственный тик: AGE+PULSE, 64 шага, default 600ticks; вечные fire-source основания не стареют; doFireTick=false останавливает старение/распространение. Сжигание соседей: flammability/70 каждые ~10ticks, максимум 6 соседей; распространение: локально 3×3×2, только загруженные чанки в worldborder. Использует onCaughtFire хуки. Сухие костры зажигаются. Предметы получают inFire damage, FIRE_RESISTANT исключён.
- ArcaneFire остаётся отдельным saved attachment и DamageType вне IS_FIRE для обхода Fire Resistance, защищает creative/spectator, вода гасит; vanilla fire ticks не выставляет, поэтому нет двойного урона/оранжевого огня. Следующие dust частицы удалены.
- BurnSnapshot сервер→клиент синхронизирует id и длительность при старте/окончании/каждые 20ticks/начале tracking/login/dimension. ArcaneFireVisuals отделяет миры и TTL. Два client-only Mixin используют штатный EntityRenderDispatcher.renderFlame и ScreenEffectRenderer.renderFire с собственными atlas sprites. Обычный огонь не меняется. Сетевой protocol revision 2; сервер и клиенты обновляются вместе.
- Зажигание соседнего костра отложено до первого fire tick: отменённое через Forge placement hook размещение не оставляет побочных изменений костра.

Проверки: финальная сборка BUILD SUCCESSFUL; 43 GameTests + 367 core checks. Native client проверен для UV/лица, фиолетового огня на сущности и first-person overlay, сравнения с обычным огнём. В тестовом сервере doFireTick включён в fixture (по умолчанию GameTestServer выключает его). Дополнительно есть тест отключения правила с немедленным восстановлением.

Генераторы: tools/generate_assets_1_5.py запускает обновлённый generate_stage3_assets.py (текстуры/модели/иконки/GUI), затем generate_revision15_fire.py (собственные фиолетовые спрайты и модели на стандартных Minecraft templates). Требуется Pillow и уже созданный Gradle artifact с vanilla resource jar. Не запускать исторические stage2/revision13 генераторы поверх текущих файлов. Мана-core намеренно не перегенерируется.

Осталось из полного проекта: расширение магического каталога после языкового аудита, получение рукописей в мире/структуры/loot tables, прогрессия исследования и баланс, более адаптивный кодекс на малых GUI scales, multiplayer-проверки с двумя настоящими клиентами, визуальный отзыв пользователя о новом UV и слитке. Не выдавать эти пункты за выполненные. Вода/дождь гасят магическое горение сразу: множитель 3 относится к естественной длительности.
