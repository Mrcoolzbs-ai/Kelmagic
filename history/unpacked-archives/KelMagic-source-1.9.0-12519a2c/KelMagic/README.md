# KelMagic 1.9.0

Minecraft Java1.21.1 · NeoForge21.1.256 · Java21.

180 слов по четырём редкостям; девять великих формул; стол архивариуса, профессия/семь костюмов и десять редких деревенских архивов; магический свет, сохранение кодекса, 32 достижения, кузнечная отделка и настоящее любование золотой шерстью.

- START_HERE.md — установка, рецепты, изменения и ограничения.
- docs/kel-language/CATALOG-1.9.0.md — каждое слово, редкость, эффект.
- docs/kel-language/RITUALS-1.9.0.md — формулы и правила высшей магии.
- docs/architecture/VERIFICATION-1.9.0.md — проверка конкретной сборки.
- HANDOFF.md / CONTINUATION_PROMPT.txt — передача проекта.

JDK21: `bash ./gradlew build runGameTestServer --max-workers=1`.
При малой памяти: `bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1`.
Ресурсы: `python tools/verify_revision_1_9.py` (Python + Pillow).
Ресурсный overlay: `python tools/generate_revision_1_9.py`; старые генераторы не запускать без этого overlay.

Wrapper включён; первая сборка требует интернет. Готовый JAR в dist/. Сервер и клиенты одной версии. Резервная копия мира обязательна. Новые структуры только в новых чанках. Копии сущностей сохраняют инвентари, взрывы/метеориты разрушают мир, трансцендентность действительно даёт краткий креатив. Настоящего RGB-освещения в vanilla нет. Нативный графический клиент здесь не проверен.
