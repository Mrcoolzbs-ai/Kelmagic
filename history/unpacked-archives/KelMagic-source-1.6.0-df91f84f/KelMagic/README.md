# KelMagic 1.6.0

Minecraft Java 1.21.1 · NeoForge 21.1.256 · Java 21.

180 слов, 180 рукописей, игровые эффекты и составные заклинания, повторяемые операторы muk/nis, «Жизнь в пламени», творческий заполненный кодекс и копирование кодексов.

- Установка и управление: START_HERE.md
- Передача проекта: HANDOFF.md и CONTINUATION_PROMPT.txt
- Полный новый каталог: docs/kel-language/CATALOG-1.6.0.md
- Проверки: docs/architecture/VERIFICATION-1.6.0.md
- Исходная спецификация: docs/requirements/extended-specification.md

Сборка: `bash ./gradlew build` (Windows: `gradlew.bat build`).
Проверки: `bash ./gradlew build runGameTestServer --max-workers=1`.
Для малой памяти: `bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1` — официальный бинарный режим подготовки Minecraft без декомпиляции.

Gradle Wrapper включён. Для первой сборки нужен интернет. JAR находится в dist/. Старый JAR удалите из mods; не устанавливайте две версии одновременно. Перед обновлением сделайте резервную копию мира.

Полная спецификация не завершена: структуры, естественная добыча рукописей и дальнейшая прогрессия пока не добавлены. Нативный клиент и два реальных multiplayer-клиента этой версии здесь не проверены.
