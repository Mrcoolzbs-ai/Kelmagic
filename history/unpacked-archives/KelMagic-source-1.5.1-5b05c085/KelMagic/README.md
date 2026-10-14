# KelMagic 1.5.1

Minecraft Java 1.21.1 · NeoForge 21.1.256 · Java 21.

- Установка и управление: START_HERE.md
- Техническая передача: HANDOFF.md и CONTINUATION_PROMPT.txt
- Первоначальный запрос: docs/requirements/original-prompt.txt
- Полная спецификация: docs/requirements/extended-specification.md
- Каталог: docs/kel-language/SPELL_CATALOG.md
- Проверки: docs/architecture/VERIFICATION-1.5.1.md

Сборка: `bash ./gradlew build` (Windows: `gradlew.bat build`).
Серверные проверки: `bash ./gradlew runGameTestServer --max-workers=1`.
Gradle Wrapper included. Интернет нужен для первоначальной загрузки зависимостей; кэши и миры в исходный архив не включены.
Готовый JAR находится в dist/. Не устанавливайте две версии мода одновременно.

Текущий мод не реализует всю расширенную спецификацию: словарь пока из шести слов; рукописи пока без генерации добычи в мире. Проверяйте изменения на резервной копии мира.

Для среды с малым объёмом памяти: `bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1`. Это официальный бинарный режим подготовки Minecraft без декомпиляции; не меняет платформу мода.
