from pathlib import Path
import json,shutil,zipfile,hashlib
r=Path('/data/KelMagic');arch=r/'docs/architecture';req=r/'docs/requirements'
for name,target in [('START_HERE.md',arch/'START_HERE-1.5.1.md'),('HANDOFF.md',arch/'HANDOFF-1.5.1.md'),('CONTINUATION_PROMPT.txt',req/'CONTINUATION-1.5.1.txt')]:
 if not target.exists():shutil.copy2(r/name,target)
old=(r/'START_HERE.md').read_text()
section='''## Новое в 1.6.0
- Каталог расширен до 180 слов из концепт-словарей, грамматики и согласованной замены nis. Все существующие глифы сохранены; новых букв не потребовалось.
- Для каждого слова зарегистрирована отдельная рукопись. Древняя рукопись случайно раскрывается в одно из всех 180 слов. Предметы доступны в творческой вкладке; естественный лут ещё не добавлен.
- 172 дополнительных игровых профиля: усиления/ослабления, лечение, урон, сытость, тушение обычного огня, очистка, толчок/притяжение, вращение, обнаружение, открытие/закрытие. Остальные восемь слов — старые селекторы/операторы/огонь и два новых оператора масштаба. Игровые эффекты — интерпретация, не перевод слов.
- muk умножает силу и время на 1,5; nis делит на 1,5. Повторы суммируются по разности их количества, предел ±6 ступеней. Каждый повтор оплачивается маной. Максимум длительности 3600 тиков; пределы отдельных эффектов приведены в исходниках. Остальные слова повторять нельзя.
- mina liv pir в любом порядке даёт «Жизнь в пламени» на 60 секунд: лечение 2 HP в секунду только при обычном или магическом горении и отмена урона этих видов огня. Горение не снимается. Прямой урон лавы и горячего пола, прочий урон и цена нехватки маны сохраняются. muk/nis меняют силу лечения и длительность.
- Творческий архивариусный кодекс сразу знает все 180 слов: `/give @s kelmagic:creative_archivists_codex`.
- Копия: непустой кодекс + книга + перо + обычные ИЛИ светящиеся чернила, без фиксированного расположения. Оригинал остаётся в сетке; копия сохраняет открытые слова, заметки и имя. Пустой кодекс не копируется. Работает и в сетке 2×2.
- Полный список слов, источников и эффектов: docs/kel-language/CATALOG-1.6.0.md и JSON рядом.
- Новые слова работают в чате и скриптере. Максимум восемь слов-действий в одном составном заклинании; селектор только один. Неподдерживаемое сочетание всё ещё опасно, как прежде.

'''
old=old.replace('# Kel Magic 1.5.1 — исправления огня и кодекса','# Kel Magic 1.6.0 — каталог и рукописи').replace('kelmagic-1.5.1.jar','kelmagic-1.6.0.jar').replace('mina, tuna, sila, pir, no, lu. Пустой','любые слова из каталога (например: mina, rap, muk). Пустой').replace('одного из шести существующих слов: mina, tuna, sila, pir, no, lu','одного из 180 зарегистрированных слов')
old=old.replace('## Магия\n',section+'## Магия\n')
old=old[:old.rfind('## Проверки')]+'''## Проверки
Финальная сборка: 1436 pure-Java core checks, 70 обязательных серверных GameTest, 2780 проверок ресурсов и сохранности старых глифов/текстур. Офлайн-проверка размещения страниц: 376 checks; восемь представительных страниц и листы всех рукописей просмотрены. BUILD SUCCESSFUL. Нативный графический клиент 1.6.0 и multiplayer с двумя реальными клиентами здесь не запускались. Эти проверки не заменяют игровой тест GUI, анимации и шейдеров. Подробности: docs/architecture/VERIFICATION-1.6.0.md.
'''
(r/'START_HERE.md').write_text(old)
(r/'README.md').write_text('''# KelMagic 1.6.0

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
''')
verification='''# Проверка KelMagic 1.6.0

Платформа не изменена: Minecraft 1.21.1, NeoForge 21.1.256, Java 21.

## Выполнено
- Финальный `build runGameTestServer` в официальном бинарном режиме: BUILD SUCCESSFUL.
- Pure-Java coreTest: 1436 проверок.
- Сервер Minecraft: все 70 обязательных GameTest прошли.
- `tools/verify_catalog_1_6.py` со сравнением резервной копии src 1.5.1: 2780 проверок.
- Офлайн-аудит размеров/переноса страниц: 376 проверок; восемь представительных страниц просмотрены.
- Просмотрены контактные листы всех 180 рукописей и иконка нового эффекта.

Серверные сценарии включают: старые заклинания и исправления огня, расширенный каталог и обучение, сохранение старых заметок, повторные muk/nis, permutations mina liv pir, лечение при обычном/магическом горении, отсутствие лечения без горения, сохранение урона лавы/горячего пола/мана-долга, истечение эффекта, оригинал/копия кодекса и заметки, оба вида чернил, пустые/лишние ингредиенты, чат/скриптер с новыми словами, двери с реальным raycast.

## Ограничения
Нативный графический клиент этой версии не запускался. Офлайн-Pillow превью не являются скриншотами Minecraft. Два реальных клиента, GUI scales в игре, анимация в движении и шейдеры не проверены. Естественный лут рукописей и структуры не реализованы.

Входные концепты: gram1.png, gram2.png, ez_word1.png, ez_word2.png и письменность Kel_write.png. sin — «выход», nis — согласованная пользователем замена zin. Несовместимые/неясные корни не переименованы автоматически. Все 180 слов и источники перечислены в CATALOG-1.6.0.json.

Лог финальной сборки: BUILD-1.6.0.log.
'''
(arch/'VERIFICATION-1.6.0.md').write_text(verification);shutil.copy2('/data/test-1.6-release-final.log',arch/'BUILD-1.6.0.log')
(r/'HANDOFF.md').write_text('''# KelMagic 1.6.0 — передача проекта

Платформа: Minecraft 1.21.1 / NeoForge 21.1.256 / Java 21; mod id kelmagic; пакет dev.kelmagic.

Прочитать START_HERE.md, docs/kel-language/CATALOG-1.6.0.md и docs/architecture/VERIFICATION-1.6.0.md.

Реализовано: 180 слов и предметов рукописей; 172 дополнительных профиля эффектов; парсер составных заклинаний с прежними комбинациями; muk/nis; отдельное условное лечение mina liv pir; заполненный творческий кодекс; копирование непустого кодекса с сохранением оригинала и компонентов. Все изменения 1.5.1 сохранены.

Старые первые шесть индексов слов сохранены ради записанных заметок. Формы глифов/шрифт не менялись. sin («выход») не переименован; nis вместо zin согласован пользователем. Не добавлять z/w и не возвращать спорные корни без согласования.

Ключевой код: core/SpellProfiles, core/SpellParser, server/ExpandedSpellEffects, server/FlameLife, item/FlameLifeEffect, item/CodexCopyRecipe, MagicArchive, client/CodexScreen. Данные: words.json/effects.json. Генератор: tools/generate_catalog_1_6.py; аудит: tools/verify_catalog_1_6.py.

Проверки: 1436 core + 70 GameTest + 2780 asset + 376 offline page bounds, BUILD SUCCESSFUL. Нативный клиент новой версии не запускался.

Не выполнены: естественная добыча рукописей, структуры, остальная прогрессия полной спецификации, баланс живого сервера, GUI/рендер в реальном клиенте и два реальных клиента. Не выдавать 180 слов за 180 уникальных визуальных эффектов: профили используют ограниченный набор реализованных действий.

История: docs/architecture/HANDOFF-1.5.1.md, docs/requirements/CONTINUATION-1.5.1.txt.
''')
(r/'CONTINUATION_PROMPT.txt').write_text('''ПРОМПТ ДЛЯ ПРОДОЛЖЕНИЯ KELMAGIC — 1.6.0

Продолжай существующий проект, не переписывай мод. Сначала прочитай START_HERE.md, HANDOFF.md, VERIFICATION-1.6.0.md, CATALOG-1.6.0.md и реальные исходники. Платформа неизменна: Minecraft 1.21.1, NeoForge 21.1.256, Java 21, kelmagic/dev.kelmagic.

Последняя задача выполнена: 180 подтверждённых слов, 180 рукописей, игровые профили, повторяемые muk/nis, mina liv pir, творческий полный кодекс и копирование. Пользователь согласовал nis вместо zin. sin уже означает выход, его нельзя занимать. Не выдумывай неподтверждённые корни или письменность. Эффекты отделены от переводов и являются игровой интерпретацией. Глифы 1.5.1 сохранены.

Сохраняй первые шесть индексов заметок, имена и компоненты кодексов, старые скриптеры, покраску, сердце, регенерацию маны, серверную авторитетность, защитные события/PvP. Магический огонь не тушится обычными новыми EXTInGUISH-профилями и не защищается Fire Resistance: только специальная комбинация, вода/дождь/ожидание. Прямой урон LAVA/HOT_FLOOR не отменять через общий IS_FIRE. Операторный масштаб ограничен ±6 степенями 1,5, длительность до 3600 тиков.

Сборка JDK21: bash ./gradlew build runGameTestServer --max-workers=1. При малой памяти официальный бинарный путь: bash ./gradlew -I tools/binary-build.init.gradle build runGameTestServer --max-workers=1; _JAVA_OPTIONS='-Xmx1800m -XX:ActiveProcessorCount=2'. Не компилируй при работающем runClient.

Финальные результаты: 1436 core checks, 70 GameTest, 2780 resource checks и 376 offline page bounds. Нативный клиент, два реальных клиента и шейдеры не проверены. Превью не называй скриншотами игры. Структуры/лут/прогрессия ещё не выполнены.

Текущий генератор новых ресурсов tools/generate_catalog_1_6.py. Исторические генераторы не запускать поверх актуальных без анализа. Полный предыдущий промпт сохранён в docs/requirements/CONTINUATION-1.5.1.txt.
''')
rows=json.loads((r/'docs/kel-language/CATALOG-1.6.0.json').read_text())['words']
cat='''# Каталог KelMagic 1.6.0 — 180 слов

Перевод взят из концептов; игровые эффекты — предложенная интерпретация. Исходные глифы сохранены. nis согласовано вместо zin, sin — выход.

Порядок слов не важен. muk ×1,5 и nis ÷1,5 повторяются; предел ±6 ступеней, длительность до 3600 тиков. Остальные повторы запрещены. mina/tuna/sila выбирают заклинателя/игрока/моба; lu — область; no обращает поддерживаемый эффект. Не более восьми слов-действий и одного селектора. Историческое lu mina pir остаётся неопределённым.

mina liv pir: 60 секунд условного лечения 2 HP/с при любом горении, защита от обычного и магического огненного урона; не тушит и не отменяет прямой урон лавы/горячего пола. С операторами сила/время масштабируются.

| Слово | Перевод | Игровое действие | Источник |
|---|---|---|---|
'''
for w in rows:cat+='| '+' | '.join(str(w[k]).replace('|','\\|').replace('\n',' ') for k in ['word','translation','description','source'])+' |\n'
(r/'docs/kel-language/CATALOG-1.6.0.md').write_text(cat)
p=r/'docs/kel-language/SPELL_CATALOG.md';s=p.read_text();p.write_text('> Исторический каталог этапа 1. Текущий каталог: CATALOG-1.6.0.md; ограничения повторов ниже относятся к старой версии.\n\n'+s)
# Include current release only in dist; do not ship stale ready-to-install jars.
for f in (r/'dist').glob('*.jar'):f.unlink()
jar=r/'build/libs/kelmagic-1.6.0.jar';shutil.copy2(jar,r/'dist'/jar.name)
archive=Path('/data/KelMagic-source-1.6.0.zip')
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for p in sorted(r.rglob('*')):
  if not p.is_file():continue
  rel=p.relative_to(r)
  if rel.parts[0] in {'build','.gradle','run','.git'} or '__pycache__' in rel.parts or '.cache' in rel.parts:continue
  z.write(p,'KelMagic/'+str(rel))
with zipfile.ZipFile(archive) as z:
 assert z.testzip() is None
 assert z.read('KelMagic/dist/'+jar.name)==jar.read_bytes()
 assert z.read('KelMagic/gradle/wrapper/gradle-wrapper.jar')
 assert len(json.loads(z.read('KelMagic/src/main/resources/data/kelmagic/magic/words.json'))['words'])==180
print('Archive validated:',archive.name,archive.stat().st_size,'bytes; JAR SHA256:',hashlib.sha256(jar.read_bytes()).hexdigest())
