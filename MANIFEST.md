# KelMagic — MANIFEST резервного переноса

## Назначение и состав
- KelMagic_FULL_BACKUP.zip: полный безопасный снимок всех найденных проектов/исходников и доступных исторических архивов, выводов сборок и старых QA-миров; это не образ всей ОС.
- KelMagic_TRANSFER.zip: исходный перенос без project .gradle, build, run и вложенных исторических архивов. Сохранены основные и экспериментальные исходники, libs, wrapper, ресурсы/UV/.bbmodel, генераторы, тесты, QA-code/init scripts, документация, доступные references и F2. Большие прежние ZIP и QA-миры остаются в FULL; см. historical-archive-index.json.
- KelMagic_EXPERIMENTS_F2.zip: независимый страхующий комплект D-final, E, F2 и ранних сравнительных геометрий/референсов; содержит полный исходный экспериментальный проект, не только модели, а также два исходных контрольных ZIP E.

Все три содержат корневые HANDOFF.md, MANIFEST.md, RECOVERY.md и metadata с хэшами/исключениями. Главный проект main/KelMagic не заменён экспериментом. Содержимое F2 находится вне assets и НЕ подключено.

## Исключения и безопасность
- Из обоих основных архивов исключены воспроизводимые .gradle/__pycache__/node_modules/.cache, JDK/JDK tarball, vanilla cache и decompiler download, временные PID/lock-файлы. Глобальная домашняя директория/браузер/credential store не захватывались.
- Учетные launcher/usercache/server-list/credentials/.env/private-key файлы исключаются по имени. Выполнен эвристический поиск секретоподобных значений в текстах и вложенных ZIP; обнаружений нет. Это не обещание невозможности любых неизвестных секретов; точный алгоритм и результат сохранены.
- Скрытые файлы не исключались массово: .github/.vscode/.gitignore/.gitattributes сохранены, если присутствовали.
- TRANSFER дополнительно исключает build/run и вложенные архивы. Последний E QA-мир отсутствовал уже в исходном контрольном ZIP. В FULL сохранены найденные другие старые QA-миры, не подмена последнего.
- Не включён весь .agent-service: перенесены только явно выбранные контрольные файлы/пользовательский screenshot, остальные локальные данные не требуются проекту.
- Полный пофайловый журнал исключений: backup-metadata/exclusions.json. Нулевой результат эвристики: backup-metadata/safety-review.json.

## Происхождение и оговорки полноты
E побайтно восстановлена из сохранённого контрольного ZIP, manifest 10546 файлов сверён. D-final из её frozen reference; ранний D хранится отдельно.
Исходный каталог F2 прошлого окружения недоступен. 19 спрайтов и два контактных листа восстановлены по сохранённым командам рисования/8× nearest-neighbor. Нынешние хэши доступны; сравнить с утраченными прежними байтами невозможно. Это восстановление существующего рисунка, не интеграция/новая версия дизайна. Подробности HANDOFF.md и RECOVERY.md.
Не все inline-картинки диалога доступны как файлы; сохраняется найденный набор concepts/refs/game evidence, без заявления о полноте всех первоначальных таргетов.
Git object database не обнаружена ни в каталогах, ни в сохранённых ZIP: bundle не создан. Доступна история снимков, не история коммитов.

## Контрольные суммы
Полный индекс имён/размеров/SHA256 исходных payload-файлов: backup-metadata/inventory.json. Ключевые source/model/texture/генераторы: backup-metadata/protected-files.json. Исторические ZIP: backup-metadata/historical-archive-index.json.
Каждый архив дополнительно содержит PAYLOAD_SHA256.json — имена, размеры и SHA256 всех включённых файлов, кроме самого этого индекса. HANDOFF.md и MANIFEST.md входят в индекс.
SHA256 самих архивов рассчитываются ПОСЛЕ закрытия ZIP и хранятся в отдельном ARCHIVE_CHECKSUMS.json, опубликованном рядом. Внутрь архива нельзя поместить его собственный конечный SHA256 без циклического изменения содержимого. Внешняя итоговая копия этого MANIFEST дополнена фактическими размерами/хэшами/проверками; внутренняя описывает состав и исходные контрольные суммы до запечатывания.

## Количество файлов до добавления общих документов/metadata
- KelMagic_FULL_BACKUP.zip: 93058 файлов, 2960313023 исходных байт.
- KelMagic_TRANSFER.zip: 71055 файлов, 1180199139 исходных байт.
- KelMagic_EXPERIMENTS_F2.zip: 32073 файлов, 247559903 исходных байт.

## Важные неизменённые артефакты (SHA256)
- `main/KelMagic/build.gradle`: `91c872270937f180d1ae8bb151488ab79b1a2d44ee4f33bc0f458b294e741768`
- `approved/D-final-lashes/red_mage.geo.json`: `c183a271efee3736148439d7d87670d3be0a8bb9d58647a6c57a306a2b7750cb`
- `approved/D-final-lashes/red_mage_gecko.bbmodel`: `e380091e2e04a3bc83d46263601154818ab8b491a8250aee9e1ddeeecdedd421`
- `approved/D-final-lashes/red_mage_geo.png`: `35418b8b032f5dc3ea613f48a5d3a254fcd0a9bddde55ec452dcb63b358f3692`
- `experiments/E/test-project/src/main/resources/assets/kelmagic/geo/red_mage.geo.json`: `5eae971793670e3f76d3082d91a5760db8b6d6d94e63012bac3e65633eb7b20f`
- `experiments/E/test-project/src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png`: `35418b8b032f5dc3ea613f48a5d3a254fcd0a9bddde55ec452dcb63b358f3692`
- `experiments/E/test-project/art/red-mage/red_mage_gecko.bbmodel`: `9961ae2585a696df497d62e1461692bbb309456384d95fb36063e6a285e4e482`
- `experiments/kelmagic-F2-art/art-F2/preview/F2-emotions-contact-8x.png`: `1c638350f542feb993d32e906d79ebaca9477ed5fc7ea7684ad14193d3900c04`
- `experiments/kelmagic-F2-art/art-F2/preview/F2-visemes-contact-8x.png`: `202ed82398cbd072f7fe720d953463a812e7280c83f98e8757b8972c1805c934`
