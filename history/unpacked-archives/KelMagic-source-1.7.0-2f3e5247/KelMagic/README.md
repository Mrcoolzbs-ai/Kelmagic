# KelMagic 1.7.0

Minecraft Java 1.21.1 · NeoForge 21.1.256 · Java 21.

180 слов; 172 различных игровых профиля и операторы; мировая магия и 16 особых сочетаний; исправления боевой магии; кассеты, кассетная дека и восьмислотовый плеер.

- Установка, управление и рецепты: START_HERE.md
- Новое в версии: docs/architecture/RELEASE-1.7.0.md
- Слова и сочетания: docs/kel-language/CATALOG-1.7.0.md
- Проверки: docs/architecture/VERIFICATION-1.7.0.md
- Передача проекта: HANDOFF.md и CONTINUATION_PROMPT.txt
- Полная исходная спецификация: docs/requirements/extended-specification.md

Сборка JDK21: `bash ./gradlew build` (Windows: `gradlew.bat build`).
Серверные тесты: `bash ./gradlew build runGameTestServer --max-workers=1`.
Для малой памяти: `bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1` — официальный бинарный режим без декомпиляции Minecraft.

Gradle Wrapper включён. Для первой сборки нужен интернет. Готовый JAR в dist/. Не устанавливайте две версии одновременно; перед обновлением сделайте резервную копию мира. Сервер и клиенты должны использовать одну версию.

Полная спецификация ещё не завершена: структуры и естественная добыча рукописей не добавлены. Нативный клиент и реальные multiplayer-клиенты новой версии здесь не проверены.
