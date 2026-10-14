from pathlib import Path
import json,re,shutil,collections
r=Path('/data/KelMagic');a=r/'docs/architecture';k=r/'docs/kel-language'
for n,target in [('START_HERE.md','START_HERE-1.8.0.md'),('HANDOFF.md','HANDOFF-1.8.0.md'),('CONTINUATION_PROMPT.txt','CONTINUATION-1.8.0.txt')]:
 p=a/target if n!='CONTINUATION_PROMPT.txt' else r/'docs/requirements'/target
 if not p.exists():shutil.copy2(r/n,p)
rar=json.loads((r/'src/main/resources/data/kelmagic/magic/rarities.json').read_text())['words'];words=json.loads((r/'src/main/resources/data/kelmagic/magic/words.json').read_text())['words'];effects=json.loads((r/'src/main/resources/data/kelmagic/magic/effects.json').read_text())['effects'];src=(r/'src/main/java/dev/kelmagic/core/GrandSpells.java').read_text();rites=[{'id':i,'minimum':int(n),'formula':w.split()} for i,n,w in re.findall(r'r\("([a-z_]+)",(\d+),"([^"]+)"\)',src)];trans={'id':'transcend','minimum':100,'formula':'mina neb sap sol lun tem sav pen kan por liv mor sin rin tin log kel tuk bal bon'.split()*5};rites.insert(7,trans)
for rite in rites:
 while len(rite['formula'])<rite['minimum']:rite['formula'].append('muk')
labels={'common':'Обычное','uncommon':'Необычное','rare':'Редкое','epic':'Эпическое'};names={'wings':'Призрачные крылья','island':'Летающий остров','blast':'Взрыв','duplicate':'Копирование сущности','ender':'Карман Края','mana_tide':'Прилив маны','meteor':'Метеоритный удар','transcend':'Трансцендентность','end_gate':'Временные врата Края'}
info={'wings':'Настоящее планирование по физике элитр без нагрудного предмета: базово 60 секунд, muk/nis меняют время до 180 секунд. После прыжка нажмите прыжок ещё раз в воздухе. Грудной слот не заменяется; надетые элитры не изнашиваются за магический полёт. Вода, земля и левитация останавливают планирование по ванильным правилам. Это не свободный креативный полёт.', 'island':'Наведитесь на землю. Перемещает дисковый участок грунта вверх на 12 блоков: базовый радиус 4, глубина до 3; масштаб меняет радиус 3–5. Блоки внизу исчезают, наверху появляются те же состояния — не копия и не FallingBlock-анимация. Переносятся земля, природный камень, песок и гравий; блоки с инвентарями, здания, растения и существа не переносятся. Нужны загруженные и свободные клетки назначения; при отказе защиты изменения откатываются. Песок/гравий сохраняют обычную гравитацию.', 'blast':'Взрыв у точки прицела. Радиус 2 × масштаб, не выше 16. Формула ниже с одним muk даёт радиус 3; дополнительные muk увеличивают, nis уменьшают. Разрушает блоки и повреждает существ по правилам обычных TNT-взрывов.', 'duplicate':'Копирует первую подходящую сущность по лучу взгляда на расстоянии магии, со свежим UUID. Сохраняет NBT, снаряжение и содержимое инвентаря — дюп намеренно разрешён. Нужна свободная соседняя клетка. Игроки, части дракона, пассажиры и поводок не копируются. Может создавать опасных мобов; не применяйте без резервной копии мира.', 'ender':'Открывает личный сундук Края игрока, не создавая блока. Содержимое то же, что у обычного сундука Края; обычные правила доступа к меню сохраняются.', 'mana_tide':'На 1200 тиков (60 секунд при 20 TPS) увеличивает максимум маны выбранного игрока ровно на 50. Если подходящего игрока под прицелом нет, применяется к заклинателю. Не заполняет ману автоматически; повтор обновляет время, не складывает бонусы. После окончания избыток текущей маны ограничивается прежним максимумом.', 'meteor':'Наведитесь на блок: огненный след спускается сверху две секунды, затем происходит огненный взрыв. Радиус 6 × корень масштаба, максимум 12. Это визуально показанный удар со взрывом, не отдельная управляемая сущность метеорита. Максимум 16 ожидающих ударов; при выгрузке точки удар отменяется.', 'transcend':'Ровно 100 слов: двадцатисловная строфа ниже, повторённая пять раз. На 60 тиков (3 секунды) даёт настоящий креатив, лечение, свечение, ночное зрение и столб световых частиц над игроком. Затем возвращает прежний режим, даёт мягкое падение и две зловещие личные строки в чат. Выход из игры, вход после аварии и остановка сервера восстанавливают режим. Слово 101 по-прежнему вызывает перегруз. Полученные в креативе предметы не отбираются.', 'end_gate':'Создаёт проходимый магический блок в двух блоках перед игроком. Исчезает через 100 тиков (5 секунд); таймер сохраняется в очереди тиков мира. Вне Края отправляет в Край на платформу; в Краю — к общему спавну Верхнего мира, не к личной кровати. У существ действует стандартная задержка портала. Нет крафта и дропа.'}
ritualdoc='''# Великие заклинания KelMagic 1.9.0

Все слова канонические. Порядок внутри сочетания не важен, количество каждого слова важно. Произносите слова по одному в чат: одна длинная строка не распознаётся как отдельные магические слова. Enter на пустой строке подготовки подтверждает, Esc отменяет; прежнее управление не изменено. Можно записать законченную формулу скриптером и затем на кассету.

Порог — минимум общего числа слов. В обычных великих формулах можно добавлять muk/nis до общего предела 100; нельзя произвольно подменять обязательные корни. Модификаторы, уже записанные в примере, считаются словами и помогают достичь порога. Для трансцендентности нужна точная стословная формула, без места для дополнительных модификаторов.

Стоимость слов и кассет не снижалась специально для высшей магии: запаситесь маной, восстановлением и дополнительным здоровьем. Недостаток маны по-прежнему отнимает здоровье. Дублирование и временный креатив сознательно позволяют обходить обычную экономику выживания. На публичном сервере это нужно учитывать.

'''
for rite in rites:
 rid=rite['id'];ritualdoc+=f'## {names[rid]} — {rite["minimum"]} слов'+(' (ровно)' if rid=='transcend' else ' (минимум)')+'\n\n'+info[rid]+'\n\n'
 if rid=='transcend':ritualdoc+='Двадцать слов одной строфы; повторите **пять раз** без других слов:\n\n```text\n'+' '.join(rite['formula'][:20])+'\n```\n\nПолная стословная запись есть в RITUALS-1.9.0.json.\n\n'
 else:ritualdoc+='```text\n'+' '.join(rite['formula'])+'\n```\n\n'
(k/'RITUALS-1.9.0.md').write_text(ritualdoc);(k/'RITUALS-1.9.0.json').write_text(json.dumps({'version':'1.9.0','rites':rites},ensure_ascii=False,indent=2))
catalog='''# Слова KelMagic 1.9.0 — редкость и эффекты

Редкость относится к отдельному слову, не к словосочетанию. 180 слов: 77 обычных, 73 необычных, 18 редких и 12 эпических. Градация учитывает охват мира/число блоков, сложность, силу и ценность одиночного эффекта. Селекторы и базовые операторы остаются обычными, чтобы грамматика была доступна.

vak — обычное (один источник), riv — редкое (три), mar — эпическое (3×3, девять свободных клеток). Числа — базовая попытка размещения; занятые/защищённые клетки пропускаются. Масштаб и состояние мира могут менять фактический результат.

Древняя рукопись раскрывает только слово своего уровня: обычная из архивного сундука, необычная/редкая/эпическая у развитого архивариуса. Уровень виден по редкости предмета и в кодексе. Все старые слова и индексы заметок сохранены.

| Слово | Перевод | Редкость | Одиночный эффект |
|---|---|---|---|
'''
rows=[]
for w in words:
 word=w['word'];desc=effects.get(word,{}).get('description',w.get('description','Особое грамматическое слово.'));row={'word':word,'translation':w['translation'],'rarity':rar[word],'description':desc};rows.append(row);catalog+='| '+word+' | '+w['translation']+' | '+labels[rar[word]]+' | '+desc.replace('|','\\|')+' |\n'
catalog+='\n## Сочетания\nПолная парная грамматика 1.8 сохранена: все 16 290 неупорядоченных пар определены, но не каждая имеет уникальный результат. Девять великих формул и ограничения находятся в RITUALS-1.9.0.md. Редкость отдельных слов не присваивает автоматически редкость комбинациям.\n';(k/'CATALOG-1.9.0.md').write_text(catalog);(k/'CATALOG-1.9.0.json').write_text(json.dumps({'version':'1.9.0','words':rows,'grandRites':rites},ensure_ascii=False,indent=2))
guide='''# KelMagic 1.9.0 — архивы и высшая магия

Minecraft Java 1.21.1 · NeoForge 21.1.256 · Java 21.

## Установка
Резервная копия мира обязательна: новая высшая магия умеет разрушать местность, копировать инвентари и временно выдавать креатив. Удалите старый KelMagic JAR из mods и установите kelmagic-1.9.0.jar. Не держите две версии вместе; сервер и все клиенты должны обновиться. Сетевые сообщения кодекса изменены (версия 6).
Старые предметы, мана, кодексы, заметки, кассеты и сердце сохраняются. Новые архивы появляются только при генерации новых чанков. Мод не переустанавливает назначенные пользователем клавиши.

## Изменения по запросу
### 1. Редкость слов
Четыре уровня назначены всем 180 словам: обычное, необычное, редкое, эпическое. Это редкость **слова**, а не комбинации. Вода: vak обычное/1 источник, riv редкое/3 источника, mar эпическое/9 клеток. Редкость видна у рукописей и на странице слова. Полная таблица: docs/kel-language/CATALOG-1.9.0.md.
Обычная древняя рукопись теперь раскрывает обычное слово. Другие уровни приобретаются у архивариуса, чтобы словарь можно было завершить в выживании.

### 2. vid — вокруг игрока
vid ищет живых существ вокруг заклинателя, а не на луче взгляда. Сквозь препятствия тоже подсвечивает. Радиус и предел числа целей остаются ограниченными; lu/селекторы сохраняют соответствующие фильтры.

### 3. Кодекс помнит место
Сохраняются вкладка, страница и поисковая строка в конкретном предмете кодекса, включая перезапуск. Смена книги не должна переносить позицию: сетевой запрос привязан к токену книги. При копировании знания, заметки и позиция сохраняются, токен копии отдельный. Неизвестные слова не превращаются в изученные при просмотре.

### 4. Магические светильники
sol ставит магический факел (уровень света 14); lun и создаваемые магией морские фонари заменены магическим фонарём (15). Нежно-фиолетовая текстура и частицы, без рецептов, предметов и дропа даже с шёлковым касанием. Поэтому светильники нельзя фармить на продажу.
**Ограничение Minecraft:** ванильное освещение бесцветное. Окружающие блоки не получают настоящую RGB-подсветку от этого мода. Цвет окружающего света возможен только с отдельно поддерживающим эти блоки шейдером/модом; встроенной RGB-системы здесь нет.

### 5. Девять великих заклинаний
Полёт по физике элитр — от 10 слов; поднятие участка земли — от 15; взрыв — от 5; копия сущности — от 30; личный сундук Края — от 10; +50 к максимуму маны на минуту — от 25; метеорит — от 25; креатив на три секунды — ровно 100; временные врата Края — от 20.
**Точные формулы, стоимость, выбор цели и ограничения:** docs/kel-language/RITUALS-1.9.0.md. Стословная формула — двадцать слов, повторённых пять раз. Слово 101 не продлевает креатив, а вызывает обычный перегруз.

### 6. Стол архивариуса и профессия
Только на этом блоке можно дополнять и копировать кодекс. Обычный верстак/инвентарный крафт больше этого не делает.
Рецепт 3×3:
```
Б Л Б
М Л М
К К К
```
Б — книга; Л — обычный фонарь; М — ядро маны; К — кафедра. Выход 1 стол.
ПКМ открывает новый бирюзово-зелёный интерфейс. Четыре входа и один выход:
- Кодекс в первом входе + один новый лист в любом следующем → дополненный кодекс. Оба входа расходуются только при взятии результата. Повтор уже изученного слова не принимается.
- Непустой изученный кодекс + книга + перо + обычные/светящиеся чернила → копия. Оригинал остаётся, три материала расходуются. Пустой кодекс создаётся прежним рецептом/покупкой, а не копированием.
Входы сохраняются; при разрушении выпадают только реальные входы, не бесплатное превью результата. Shift-перенос результата проверен против повторного получения.
Стол — рабочее место нового жителя **архивариуса**. Он продаёт книги, кодексы, пустые кассеты, ядра, скриптеры; на высоких уровнях — древние рукописи других редкостей. У профессии отдельные варианты одежды для равнин, пустыни, тайги, снега, саванны, джунглей и болота.

### 7. Редкие деревенские архивы
10 вариантов: по два зала для каждой из пяти естественных ванильных деревень — равнины, пустыня, тайга, снежная, саванна. В джунглях и болотах обычных ванильных деревень нет; одежда жителей этих типов предусмотрена, новая генерация деревень там не добавлена.
Внутри есть стол, множество книжных полок, скамейки-ступени, фонари, сундук и житель, который может занять рабочее место. Сундук использует обычный лут дома соответствующей деревни плюс 35% шанс обычной древней рукописи.
Архивы включены в набор домов деревни с малым весом: два варианта по 1 против восьмикратно масштабированных исходных весов. Это примерно 0,3% на выбор элемента дома, **не фиксированный шанс на деревню**. Все исходные варианты домов сохранены. Старые чанки не перестраиваются. Зомби-деревни не изменены.
Пять JSON-пулов домов переопределены данными этого мода; другие моды/датапаки, переопределяющие те же файлы, могут конфликтовать. Все виды деревень и десять шаблонов загружаются; размещение архива со столом и сундуком проверено серверным тестом.

### 8. Достижения
Теперь 32 узла: 14 новых, включая работу со столом, копирование, покраску одеяния, отделку и высшую магию. Достижения не показывают формулы. Высшие испытания скрыты до открытия; прежние секретные кодекс/перегруз сохранены. Правило добавлять достижения в следующих версиях записано в docs/architecture/ADVANCEMENT_POLICY.md.

### 9. Отделка снаряжения
Все четыре магические доспеха и четыре одеяния принимают стандартную кузнечную отделку: шаблон отделки + вещь + материал на кузнечном столе. Цвет одеяния, имя, прочность и мана-бонус сохраняются. В инвентаре добавлена цветная окантовка материала; на надетом снаряжении используется ванильная система узоров. Нативный графический рендер надетой отделки отдельно не проверен.

### 10. Золотая шерсть у пиглинов
Шерсть стала любимым предметом и валютой: пиглины подходят к брошенному блоку, подбирают его в свободную руку, любуются примерно шесть секунд, затем бросают награду. Можно вручить ПКМ взрослому спокойному пиглину — тоже сначала думает. Во время любования вторую шерсть не принимает. Прежний мгновенный обмен/пятиисекундный кулдаун убраны; обычное золото не изменено.
Из собственного пула убраны золотые нити. Остались светящиеся чернильные мешки 1–3, аметист 4–8, кристаллы призмарина 2–5, осколок эха 1, магическая ткань 1–2 с относительными весами 25/25/20/5/5 (не прежними процентами после удаления нитей). Детёныши могут любоваться любимыми предметами по ванильным правилам, но не выдают взрослую награду. Брутальные пиглины не торгуют.

## Сохранилось из 1.8
Рецепт золотых нитей/негорящей шерсти, оформление кассет со слизью, расход скриптера после завершения записи в деке, 8 слотов плеера без кулдауна/прочности, 30% округлённой цены минимум 1, +2,5 маны каждой части одеяния, дальность 48, нейтральность mil/nak, масштаб muk/nis и мировой безопасный tok rak сохранены.

## Проверки
Результаты и воспроизводимые команды: docs/architecture/VERIFICATION-1.9.0.md. Серверные испытания и офлайн-проверка PNG не заменяют реальный графический клиент. Разные GUI scales, реальные два multiplayer-клиента, шейдеры, совместимость со всеми сторонними AI/датапаками не проверены. Историческая большая спецификация не считается автоматически завершённой только этим обновлением.
'''
(r/'START_HERE.md').write_text(guide);(a/'RELEASE-1.9.0.md').write_text(guide)
(r/'README.md').write_text('''# KelMagic 1.9.0

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
''')
(r/'HANDOFF.md').write_text('''# KelMagic 1.9.0 — передача проекта

Сначала START_HERE.md, VERIFICATION-1.9.0.md, CATALOG-1.9.0.md, RITUALS-1.9.0.md и текущие исходники. Платформа 1.21.1/NeoForge21.1.256/JDK21. Сеть6, старые сохранения читаются. История 1.8 в START_HERE-1.8.0.md/HANDOFF-1.8.0.md.

WordRarity читает magic/rarities.json (180;77/73/18/12). Редкость страниц через Properties; AncientManuscript фильтрует по DataComponents.RARITY (default COMMON). Высокие уровни торгов архивариуса дают другие уровни; не смешивать tiers с rarity combinations. riv теперь base3 источника, mar9.
Vid в ExpandedSpellEffects.targets: scan центр всегдаcaster, без LOS-фильтра; non-scan LOS сохранён. MagicLamps TORCH14/LANTERN15 noLoot/noItems; WorldSpells.state заменяет torch/sea_lantern. Purple dust иtexture, не RGB-light engine.

CodexOpen расширен section/page/query/token. CodexPosition server bounds+matching heldbooktoken; components persisted. Токен создаётся при открытии, копия с существующим токеном получает новый. Кодекс20артефактов. Не менять первые6индексов заметок и glyphfont.

Стол: archive/ArchiveTable* +MagicStudy. 4input+derivedoutput. Helpers ArchiveRecipe/CodexCopyRecipe остались, но их crafting JSON удалены — только стол. Выход не сохраняется и не выпадает; input сохранён. finish послеonTake потребляет1каждого input, оригинал остаётся приcopy; slotoutputremove может вызыватьrefresh, тест quickMove+duplicate packet обязателен. Пустые кодексы не копируются. Реальные menu fixtures проверены.

Профессия/Poi: MagicStudy и ArchivistTrades; обязательно minecraft:acquirable_job_site tag. Client ArchivistClothesMixin вtyped render(LivingEntity...) сохраняетbiome, getResourceLocation RETURN подменяет только kelmagic:archivist profession texture;7UV64x64 outfits + metadata hatpartial. Unknownbiome→plains fallback. NativeVillagerModel jacketUV0,38,front6,44..14,64: не путать сbody16,20. Компоненты ничего не знают о Notion.

10NBT archive/[biome]_[1,2].nbt сvanilla building_entrance jigsaw ичестомLootTable;5housespool overlays исходныеweights×8 +2archiveweight1. Этоdataoverride, не runtimeappend; конфликтcustompacks возможен. generate_revision_1_9.py +nbt19.py полностью воспроизводят структуры/resources;5nativehouse lootтаблиц+35%commonAncient. Zombiepool иjungle/swamp village generation не добавлялись.

GrandSpells parserGRAND дообщихпар/дубликатов. Exactmultiset обязательныхroots после удаления muk/nis,minimum count,MAX100. Transcend20rootphrase×5exact100. GrandEffects: true spectralelytra через2commonmixins canElytraFly/elytraFlightTick; terrainmove island radius3..5depth3lift12 atomic BlockSnapshots/BreakEvent+placeevents; blast bounded16; clonefreshUUID nonplayers includesNBTinventory removespassengers/leash; ownenderChestMenu;fixed+50max1200ticks;40tick meteor withcap16pending andloadedtarget;truecreative60tickleaseprevious+deadline persisted/copyOnDeath restorelogin/logout/stop/deadline. Послеdeath следующийplayertick restore; login lease всегдаrestore. Не оставлять creative послеexit/crash.

MagicPortals temporaryblock100scheduledticks usesDimensionTransition toEndplatform /globalOverworldspawn (неbed), standardcooldown. Noitems/recipe/drop. Не утверждать проверку реальной межмерной сессии игрока: таймер/регистрация проверены, API native。

WoolItem.isPiglinCurrency +piglin_loved; directWoolBarter sets offhand andADMIRING120ticks; vanilla droppedwalk/pickup. PiglinWoolMixin snapshots flag HEAD stopHoldingOffhand, getBarterResponseItems inject customloot, flagclearRETURN. Gold unchanged; golden thread removed custompool; weightsnow25/25/20/5/5. GameTest real pig brain usesh.spawn(), НЕ spawnWithNoFreeWill (удаляетповедение). Для directreward fixture требуется player inServerLevel.players: иначе vanilla randomthrow target может отсутствовать и тест flaky. FixtureaddNewPlayer creative onlytest, then discarded.

Armor: all8items trimmable tag, vanillaSmithingTrimRecipe сохраняетname/dye/manaBonus. Itempropertytrimmed+coloredlayer2 viaTrimAppearance;robe layer0dye/layer1fixed. Надетыеpatterns vanillaHumanoidArmorLayer; не утверждатьnativeUVvisualtest custom256garmentmodel. New14advancements total32,9rites hidden; descriptions noformulas. MagicProgress armor/inventory/craft checks.

GameTest FakePlayer stubsadvancements иopenMenu: fixture overridesreal PlayerAdvancements and serverMenuProvider create; не исправлять productionкод радиstub. 122requiredtests, core/resource/evidencecounts вVERIFICATION. Офлайн46PNG — simplified structureblocks, UVfrontproofoutfits, bitmapUI/itemlayers; НЕMinecraftscreenshots. Nativeclient/gui scale/two clients/shaders/worldseed village distribution not measured.

Сохранять PvP/protectionevents/selfdamage/menu-holderlocking/serverauth/savecompatibility/ArcaneFire rules. Не обещать16290bespoke pair effects. Старые генераторы возвратят старые рецепты/tooltip/loot: latestoverlay должен идти последним. docs/architecture/ADVANCEMENT_POLICY.md читать перед будущими обновлениями.
''')
(r/'CONTINUATION_PROMPT.txt').write_text('''Продолжай существующий KelMagic1.9.0, не переписывай. Сначала START_HERE, HANDOFF, VERIFICATION-1.9.0, CATALOG-1.9.0, RITUALS-1.9.0, ADVANCEMENT_POLICY ифактическийкод. Minecraft1.21.1/NeoForge21.1.256/JDK21;network6. Редкость180отдельныхслов77/73/18/12,vidaroundcaster,nativepurplevisualnotRGB,codexpersiststate,tableonlymerge/copy,POIprofession7outfits,10archiveNBT5pooloverlays,newchunks,32achievementsnoformulas,8trimmablegarments,woolvanillawalk/admire120tick/custompoolnothread.
9GRAND exactroots+minimums;wings10/island15/blast5/clone30/ender10/mana25/meteor25/trans100/end20. Creativelease60ticks mustalwaysrestore ondeadline/login/logout/stop; persistedprevious no permanent escalation. Trans20wordphrase×5;101overload. Cloneintendeddup freshUUID inventorypreserved noPlayer/passengers/leash. Islandactualmove atomic terrain only,notcopy. Portal100scheduledticks END↔Overworldglobalspawn. Manafixed+50max1200tick nofill/stack. Limits/protection/saveinteropretain.
Generators1.9+nbt19;oldoverlaygeneratorsmustnotrunlast. Nativegraphicalclient/MP2/shadersnotverified. FakePlayeropenMenu/advancementsstubs requirefixtures, productioncannotbechangedtoappeasestubs. PigAIfixture h.spawn notNoFreeWill, realLevelplayerforrewardtarget. OfflineproofsnotMinecraftscreenshots. Newachievementsineachversionpolicy. Package currentjaronly;allresourcesbyteequal+ZIPCRC beforedelivery. Do not presenthistoricalpasses aslatesttestresults.
''')
p=a/'ADVANCEMENT_POLICY.md';p.write_text(p.read_text()+'\n1.9.0: 32 узла, 14 новых; высшие формулы не раскрывать в описаниях достижений. Девять высших испытаний скрыты.\n')
e=a/'offline-previews-1.9.0';e.mkdir(exist_ok=True)
for p in Path('/data/qa-1.9').glob('*.png'):shutil.copy2(p,e/p.name)
print('1.9 guides, 180-row catalog, nine exact formulas and',len(list(e.glob('*.png'))),'offline proofs saved')
