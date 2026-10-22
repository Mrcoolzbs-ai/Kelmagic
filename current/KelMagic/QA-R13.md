# QA KelMagic R13

## Успешные проверки
- `check -x eyeStateTest`: успешно.
- CoreTests: 68 143 проверки, успешно.
- Статический R13 QA: 8 269 проверок, успешно.
- Полный GameTest: 201 тест, 196 успешно; новых регрессий R13 не найдено.
- Нативный клиентский QA: 38 проверок, успешно; 12 кадров; радиус 150 обработан за 16.93 с при расстоянии игрока около 997 блоков.
- Нативно подтверждено: старые дубовые брёвна/листва удалены полностью на тестовой области; выросло минимум одно сумеречное дерево; биом и магическая почва созданы; пакетный рендер травы не падает.
- JAR проверяется на отсутствие разработческих `Qa*` классов и встроенных классов GeckoLib/Curios.

## Сохранённые известные сбои
Пять унаследованных GameTest остаются красными и не относятся к изменениям R13:
1. `allhypothesesusespecificexperiments`
2. `mascotgeoresourcesanduniqueclips`
3. `revision119tabgatesandsinglearmornodes`
4. `alleighteenresonancepathstransformexactlyonce`
5. `nativestonecutteronlylistswordsininventorycodex`

`eyeStateTest` отдельно сохраняет прежний сбой `continuous pulse`; поэтому итоговая команда запускается с исключением этого известного теста. Все утверждения о QA выше учитывают эти ограничения.

## Журналы и кадры
- `qa/logs/check-r13.log`
- `qa/logs/static-r13.log`
- `qa/logs/gametest-r13.log`
- `qa/logs/native-r13.log`
- `qa/screenshots/native-r13/`
- `qa/screenshots/r13_items.png`
- `qa/screenshots/r13_archivist_robes.png`
