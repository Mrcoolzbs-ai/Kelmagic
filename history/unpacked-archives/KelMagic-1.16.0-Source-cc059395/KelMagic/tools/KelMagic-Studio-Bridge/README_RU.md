# KelMagic Studio Bridge — Cline + ComfyUI + Blockbench + Pixelorama

Локальный MCP-мост для проекта `C:\Projects\KelMagic`. Генерация выполняется на локальном ComfyUI; мост не использует платные облачные API и не отправляет ваши файлы наружу.

## Что входит

- **ComfyUI:** проверка API, список/инспекция workflow, запуск workflow через `/prompt`, ожидание результата через `/history`, скачивание картинки и опциональная установка PNG прямо в ресурс-путь мода.
- **Blockbench:** открывает `.bbmodel`/Java-модели, а небольшой Desktop-плагин сообщает Cline сведения об активном проекте (имя, формат, кубы, группы, текстуры). Cline создаёт/редактирует проектные файлы; Blockbench открывает их для проверки.
- **Pixelorama:** открывает PNG/PXO и использует официальный CLI для пакетного экспорта `.pxo` в PNG/APNG/GIF.
- **KelMagic:** копирование артефактов с защитой от случайной перезаписи, резервные копии при `overwrite=true`, проверка JSON ресурсов и текстур.

Важно: Blockbench не предоставляет универсальную штатную REST API, которая превращает текстовое описание в модель. Интеграция здесь основана на плагине статуса и обмене файлами; Cline/локальная модель должны описать и создать геометрию, а Blockbench используется для открытия, визуальной проверки и экспорта. Файл `.bbmodel` — внутренний JSON-формат Blockbench, поэтому итоговую игровую модель нужно проверять в установленной версии приложения.

## Установка

1. Распакуйте ZIP в `C:\Projects\KelMagic\tools\`. В результате должна появиться папка `C:\Projects\KelMagic\tools\KelMagic-Studio-Bridge`, внутри которой находятся `server.js`, `setup.ps1`, `package.json`, `blockbench` и `workflows`.
2. Откройте PowerShell и выполните:

   ```powershell
   Set-Location C:\Projects\KelMagic\tools\KelMagic-Studio-Bridge
   powershell -ExecutionPolicy Bypass -File .\setup.ps1
   ```

   Скрипт создаст рабочие папки, скопирует workflow-шаблон, попробует найти приложения и установит небольшую зависимость MCP. Он не меняет существующие файлы мода. Если npm не может скачать пакет, покажите полный текст ошибки — не устанавливайте случайные серверы.
3. Откройте `art\bridge\cline-mcp-server-snippet.json`. В Cline выберите **MCP Servers → Configure** и объедините объект `mcpServers.kelmagicStudio` с существующим конфигом. Не заменяйте весь JSON: там могут быть другие MCP-серверы.
4. Перезапустите MCP-сервер `kelmagicStudio` в Cline. В списке инструментов должны появиться `kelmagic_status`, `comfyui_generate_asset`, `blockbench_status`, `pixelorama_open_file`, `pixelorama_export_project` и другие инструменты `kelmagic_*`.
5. Если `Blockbench`/`Pixelorama` не определились, укажите полные пути к `.exe` в `tools\KelMagic-Studio-Bridge\config.json`. Например путь можно получить из **Свойства** ярлыка приложения → **Объект**.
6. В Blockbench Desktop загрузите через интерфейс плагинов файл `tools\KelMagic-Studio-Bridge\blockbench\kelmagic_studio_bridge.js`. Имя файла должно точно совпадать с ID `kelmagic_studio_bridge`. Подтвердите доверие к локальному плагину. В меню **Tools** появится `KelMagic: Send Model Status`. Файл плагина обязательно должен называться `kelmagic_studio_bridge.js`: имя совпадает с ID в `Plugin.register`.

Подробная пошаговая инструкция для новичка находится в файле `НАСТРОЙКА_CLINE_ПОШАГОВО.md`.

## Быстрая проверка

В Cline в режиме Act напишите:

> Проверь через `kelmagic_status`, `comfyui_status`, `blockbench_status` и `kelmagic_validate_resources`, что весь локальный мост работает. Ничего не изменяй и не запускай генерацию пока не сообщишь результаты.

После этого генерацию можно попросить так:

> Сделай иконку предмета «кристалл пустоты» в стиле ванильного Minecraft. Используй `comfyui_generate_asset`; сохрани результат в `src/main/resources/assets/kelmagic/textures/item/void_crystal.png`. Перед записью проверь путь; существующий файл не перезаписывай без моего согласия. После генерации проверь JSON модели и сборку мода.

Пример инструмента, который агент может вызвать:
- `comfyui_generate_asset(prompt, negativePrompt, assetName, targetPath, width, height, overwrite)`
- `blockbench_open_model(path)`
- `blockbench_status()`
- `pixelorama_open_file(path)`
- `pixelorama_export_project(projectPath, outputPath, scale)`
- `kelmagic_copy_asset(sourcePath, targetPath, overwrite)`
- `kelmagic_validate_resources()`

## Текущие ограничения

- ComfyUI по умолчанию ожидается на `http://127.0.0.1:8188`, как проверили в этом проекте. Если он выключен, инструменты сообщат об ошибке.
- Встроенный workflow рассчитан на `DreamShaper8_LCM.safetensors`, который уже использовался для теста. Если модель называется иначе, передайте `modelName` или исправьте имя в workflow.
- Сгенерированные изображения сначала архивируются в `art/generated`. Установка в ресурс-путь делается только когда вызван инструмент с `targetPath`.
- Pixelorama CLI автоматизирует экспорт уже сохранённого `.pxo`; мост не делает вид, что Pixelorama сам рисует пиксель-арт из текста. Первичное редактирование может быть выполнено агентом через преобразования изображения/генерацию, а Pixelorama — для корректировки и экспорта.
- Плагин Blockbench посылает небольшой отчёт только на `127.0.0.1:18765`. Не меняйте адрес прослушивания на `0.0.0.0` и не пробрасывайте порт в сеть.
- Все изменяющие инструменты должны оставаться под подтверждением Cline. `overwrite=true` создаёт резервную копию в `art/approved/backups` перед заменой.

## Документация

- ComfyUI использует модель API-клиент/сервер и поддерживает запуск workflow через API: https://docs.comfy.org/essentials/core-concepts/links
- Pixelorama CLI и параметры экспорта: https://pixelorama.org/user_manual/cli/
- Создание Blockbench-плагинов: https://blockbench.net/wiki/docs/plugin/
- Технические ограничения `.bbmodel`: https://blockbench.net/wiki/docs/bbmodel/
- Cline MCP servers: https://github.com/cline/cline/blob/main/docs/mcp/mcp-overview.mdx
