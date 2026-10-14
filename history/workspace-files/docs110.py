from pathlib import Path
import json,collections
P=Path('/data/KelMagic');D=P/'docs/architecture';D.mkdir(exist_ok=True)
for n in ['START_HERE.md','HANDOFF.md']:
 target=D/(n.replace('.md','-1.9.0.md'))
 if not target.exists():target.write_bytes((P/n).read_bytes())
commands='''# Команды KelMagic 1.10.0

Minecraft 1.21.1, NeoForge 21.1.256, Java 21. Все изменяющие мир и игрока команды требуют оператора уровня 2; работают из серверной консоли и командных блоков. `/kelmagic help` доступна всем. Для имени игрока можно использовать селекторы, где указано «игроки».

```mcfunction
/kelmagic admin mana maximum @s 50
/kelmagic admin mana current @s 50
/kelmagic admin mana maximum @s reset
/kelmagic admin heart @s true
/kelmagic admin heart @s false
/kelmagic admin status @s
/kelmagic admin cast @s wings
/kelmagic admin cast @s island
/kelmagic admin cast @s blast 8
/kelmagic admin cast @s duplicate
/kelmagic admin cast @s ender
/kelmagic admin cast @s mana_tide
/kelmagic admin cast @s meteor 8
/kelmagic admin cast @s transcend
/kelmagic admin cast @s end_gate
/kelmagic admin locate archive
/kelmagic admin locate archive 64
```

- `maximum` задаёт постоянный максимум 0–974 вместо базового значения, бонусов сердца и одежды; временный прилив прибавляется сверху. `reset` возвращает обычный расчёт с одеждой и сердцем. Настройка сохраняется после выхода и смерти.
- `current` принимает дробное число 0–1024, но ограничивает результат действующим максимумом. Изменение максимума не заполняет ману автоматически: для этого отдельная команда current.
- `heart true/false` изменяет именно улучшение сердца, а не выдаёт предмет. Регенерация следует этому улучшению; при ручном максимуме сердце не добавляет ещё 10 единиц.
- `cast` исполняет великое заклинание без расхода маны из позиции/взгляда выбранного игрока. Правила целей, защищённых блоков и общего лимита 100 слов остаются. Число после имени — дополнительные muk поверх минимальной формулы. transcend уже занимает 100 слов и дополнительных muk не принимает.
- `locate archive [чанки]` находит именно элемент `kelmagic:archive/...` в известных, загруженных началах деревень, а не любой поставленный вручную стол. По умолчанию радиус16 чанков, максимум64. Не генерирует новые чанки и не гарантирует поиск в неизученном мире. В найденном сообщении есть подсказка `/tp` по клику.
- В консоли замените @s именем игрока, поскольку консоль не является сущностью.

Политика следующих обновлений: новые управляемые механики получают команды/подсказки, серверную авторизацию, проверку диапазонов и испытания; новые достижения — серверные условия, корректные show_toast/announce_to_chat, без раскрытия формул.
'''
(P/'COMMANDS.md').write_text(commands)
release='''# KelMagic 1.10.0 — исправления после игрового тестирования

## Установка
Minecraft1.21.1 / NeoForge21.1.256 / Java21. Сделайте резервную копию мира, удалите старый JAR, установите kelmagic-1.10.0.jar на сервере и клиенте. Сохранения1.9 читаются; добавленные настройки имеют безопасные значения по умолчанию.

## Что изменено
1. Проверены объявления достижений: у всех игровых достижений включён announce_to_chat; корневая вкладка без объявления. «Узор мастера» теперь объявляется при новом получении. Уже полученное достижение повторно автоматически не выдаётся. Ванильное gamerule announceAdvancements также должно быть true.
2. У предмета стола архивариуса ванильные масштабы стандартного блока (первая рука0.4, третья0.375, GUI0.625).
3. Отделка:18 ванильных шаблонов ×10 материалов на8 частях экипировки; реальные ванильные узоры и восьмицветные палитры перенесены на нестандартные UV одеяния и лат. Не общий цветной контур. В инвентаре использован стандартный значок отделки: ванильный Minecraft тоже не показывает в значке разницу шаблонов. На надетой одежде шаблоны различаются.
4. Добавлено семейство `/kelmagic` — см. COMMANDS.md. Максимум, текущая мана, сердце, великие заклинания и поиск известного архива.
5. Крылья: исправлена дополнительная клиентская проверка LocalPlayer перед пакетом START_FALL_FLYING. Настоящая механика элитр без нагрудного предмета, два прыжка в воздухе для начала, ванильные фейерверки ускоряют полёт. Модель ElytraModel с призрачной фиолетовой поверхностью видна на спине; эффект не заменяет броню.
6. Остров: неправильный волнистый контур и неодинаковая глубина почвы; переносит постройки/стекло/прочие обычные блоки, а не только землю. Неподвижны инвентари (в том числе дека/стол), небьющиеся блоки. Поддерживаются неинвентарные block entities с NBT (например, вывески). Перенос ограничен радиусом3–8, до32 блоков над выбранной точкой; высота подъёма подбирается так, чтобы переносимые блоки не пересекались со своими исходниками. Защита, препятствия в месте назначения, границы мира и незагруженные чанки остаются.
7. Взрыв/метеорит: сняты прежние ограничения радиуса16/12 и усиления после шестого muk. Разрушительное усиление S=(1+0.5×muk)/(1+0.5×nis). Радиус взрыва2S, метеорита6√S. Каждый повтор влияет; единственный предел формулы —100 слов. Линейное S избегает астрономического 1.5^96 и зависания вычисления взрыва. Большие взрывы всё равно могут нагружать сервер и разрушать мир.
8. Прилив маны имеет видимый положительный статус/иконку на60 секунд; молоко и /effect clear снимают статус и дополнительный максимум. Повтор обновляет срок, а не суммирует +50.
9–11. Факел/светильник доступны в творческой вкладке и через /give, без крафта и добычи при ломании. Факел использует16×16 UV ванильного TorchBlock, прозрачный cutout-рендер и отдельную настенную модель. sol ставит его на поверхность, на которую смотрят; при взгляде в пустое небо не создаёт свет под игроком. Светильник основан на морском фонаре и сохраняет его5-кадровую анимацию, но в нежно-фиолетовой гамме. Окружающее освещение ванильного движка нейтральное, не RGB.
12. Временные врата используют настоящий многослойный анимированный рендер портала края (TheEndPortalRenderer), а не нарисованную звёздную текстуру.5-секундное исчезновение сохранено. Из Края ведут к мировому спавну Overworld, не к личной кровати.
13. После трансцендентности — две тёмно-красные реплики от трёхсимвольного обфусцированного имени «???», зловещий звук, дождь и две декоративные молнии вокруг игрока. Молнии не поджигают и не убивают; это возвращение, а не наказание. Реальная озвучка русских фраз голосом не добавлена: здесь оформленный отправитель в чате и игровой звук.
14. Одежда архивариуса исправлена по полной настоящей UV-развёртке VillagerModel: прозрачные лицо/нос/кисти/поля, непрерывные стороны длинного жакета, правильные рукава и7 биомных вариантов. Родная шапка типа деревни не просвечивает через профессиональную.
15. pir теперь эпическое слово и страница.

## Проверка
Серверные результаты и нативные клиентские снимки перечислены в docs/architecture/VERIFICATION-1.10.0.md. Не заменяйте это утверждением «протестировано на двух клиентах»: внешний мультиплеер/шейдеры отдельно не проверялись.
'''
(P/'START_HERE.md').write_text(release);(D/'RELEASE-1.10.0.md').write_text(release)
(P/'README.md').write_text('# KelMagic 1.10.0\n\nМагический язык Minecraft1.21.1/NeoForge21.1.256/Java21.\n\nСначала START_HERE.md и COMMANDS.md. Исходники, воспроизводимые ресурсы и испытания включены. История1.9 — docs/architecture/RELEASE-1.9.0.md.\n')
(P/'HANDOFF.md').write_text('''# Продолжение KelMagic1.10.0

Сначала START_HERE.md, COMMANDS.md, VERIFICATION-1.10.0.md. Исторический HANDOFF1.9 в docs/architecture.

Главные исправления: SpectralLocalPlayerMixin — дополнительный клиентский canElytraFly; ElytraLayerMixin настоящее тело крыльев. MagicTrimMixin заменяет ванильный UV pass для GarmentModel: trimTexture захватывается расширением getHumanoidArmorModel на каждом слоте. generate_revision_1_10.py читает локальный native-assets JAR:18 patterns×10palettes×8garments. Цвета на layer2 белые, потому что текстуры уже окрашены точной палитрой. Не запускать старые генераторы без overlay1.10.

MagicLamps wall torch+standingwall item; client LampRendering cutout; torch16×16. PortalGate собственный валидный BlockEntityType наследует TheEndPortalBlockEntity, native renderer, scheduled100ticks.

MagicCommands — op2admin ветка, bounds, persisted BASE_MANA_MAX=-1 default. Override задаёт общий постоянный максимум, одежда/сердце не суммируются, boost+50 сверху. locate ограничен известными загруженными structure starts, не fullworld locate. Не объявлять реализованной глобальную генерационную навигацию.

GrandEffects island snapshot transaction: irregular columns depth2–7, structures up32; noContainer/MenuProvider/ItemHandler inventories; noninventoryNBT preserved. Source unloaded columns skipped, destination unobstructed+loaded required. Break/place events/rollback remain. GameTest ставит невидимую barrierroof в tiny8×4×8 template; clearIslandTestRoof удаляет только тестовые barrier выше5 в собственной области. Нельзя удалять проверку препятствий production радиfixtures.

Destructive grandScale linear unbounded perrepeat underword100; other spellScale remains oldbounded1.5exponent. ManaTide holder status removalevents detachboost; classic MANA_BOOST_END stays savecompatible.

Qa110.java developer-only excluded from productionJAR. Native captures require local Xvfb+Minecraft client, optional init toenable kelmagic.visualQA. Allentities test actors only renderer, no personal data. Developer scripts have no effect in normalplay.

Futureupdates mustaddcommands+achievements where relevant and update tests/docs. Preserve serverauthority, inventorylocks, deathmessages, PvP/protection, rollback, original100-word overload and3sec creative restoration.
''')
(D/'COMMAND_POLICY.md').write_text(commands.split('Политика следующих обновлений:')[1])
# Update catalog/history-compatible new catalog, don't silently retain pir rare/old sol text.
a=json.loads((P/'src/main/resources/data/kelmagic/magic/rarities.json').read_text())['words'];e=json.loads((P/'src/main/resources/data/kelmagic/magic/effects.json').read_text())['effects'];words=json.loads((P/'src/main/resources/data/kelmagic/magic/words.json').read_text())['words']
rows=[{'word':w['word'],'meaning':w['translation'],'rarity':a[w['word']],'effect':e[w['word']]['description']} for w in words]
(P/'docs/kel-language/CATALOG-1.10.0.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2));(P/'docs/kel-language/CATALOG-1.10.0.md').write_text('# 180 слов KelMagic1.10.0\n\n| Слово | Значение | Редкость | Эффект |\n|---|---|---|---|\n'+'\n'.join('| '+' | '.join(str(row[k]).replace('|','\\|') for k in ['word','meaning','rarity','effect'])+' |' for row in rows))
print('1.10 docs ready; tiers',dict(collections.Counter(a.values())))
