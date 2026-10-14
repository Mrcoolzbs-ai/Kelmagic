# KelMagic 1.8.0

Minecraft Java 1.21.1 · NeoForge 21.1.256 · Java 21.

Золотые нити и прочная негорящая шерсть с пиглин-обменом; оформление кассет; 18 достижений; полная грамматика пар 180 слов; нейтральные мобы; масштаб сочетаний; дробная мана одеяния; дальность 48; sol и tok rak без далёкой цели.

- Установка, рецепты и ограничения: START_HERE.md
- Каталог: docs/kel-language/CATALOG-1.8.0.md
- Проверки: docs/architecture/VERIFICATION-1.8.0.md
- Продолжение: HANDOFF.md, CONTINUATION_PROMPT.txt
- Достижения следующих версий: docs/architecture/ADVANCEMENT_POLICY.md

JDK21: `bash ./gradlew build runGameTestServer --max-workers=1`.
Режим для малой памяти: `bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1`.
Ресурсы: `python tools/verify_revision_1_8.py` (Python + Pillow).
Gradle Wrapper включён, первая сборка требует интернет. Готовый JAR в dist/. Перед установкой резервная копия мира, сервер и клиенты одной версии.

Полная первоначальная спецификация ещё не завершена. Реальный графический клиент, multiplayer двух клиентов и шейдеры не проверены. Все пары определены грамматикой, не отдельным уникальным эффектом на каждую пару.
