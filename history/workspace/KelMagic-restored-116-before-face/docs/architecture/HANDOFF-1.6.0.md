# KelMagic 1.6.0 — передача проекта

Платформа: Minecraft 1.21.1 / NeoForge 21.1.256 / Java 21; mod id kelmagic; пакет dev.kelmagic.

Прочитать START_HERE.md, docs/kel-language/CATALOG-1.6.0.md и docs/architecture/VERIFICATION-1.6.0.md.

Реализовано: 180 слов и предметов рукописей; 172 дополнительных профиля эффектов; парсер составных заклинаний с прежними комбинациями; muk/nis; отдельное условное лечение mina liv pir; заполненный творческий кодекс; копирование непустого кодекса с сохранением оригинала и компонентов. Все изменения 1.5.1 сохранены.

Старые первые шесть индексов слов сохранены ради записанных заметок. Формы глифов/шрифт не менялись. sin («выход») не переименован; nis вместо zin согласован пользователем. Не добавлять z/w и не возвращать спорные корни без согласования.

Ключевой код: core/SpellProfiles, core/SpellParser, server/ExpandedSpellEffects, server/FlameLife, item/FlameLifeEffect, item/CodexCopyRecipe, MagicArchive, client/CodexScreen. Данные: words.json/effects.json. Генератор: tools/generate_catalog_1_6.py; аудит: tools/verify_catalog_1_6.py.

Проверки: 1436 core + 70 GameTest + 2780 asset + 376 offline page bounds, BUILD SUCCESSFUL. Нативный клиент новой версии не запускался.

Не выполнены: естественная добыча рукописей, структуры, остальная прогрессия полной спецификации, баланс живого сервера, GUI/рендер в реальном клиенте и два реальных клиента. Не выдавать 180 слов за 180 уникальных визуальных эффектов: профили используют ограниченный набор реализованных действий.

История: docs/architecture/HANDOFF-1.5.1.md, docs/requirements/CONTINUATION-1.5.1.txt.
