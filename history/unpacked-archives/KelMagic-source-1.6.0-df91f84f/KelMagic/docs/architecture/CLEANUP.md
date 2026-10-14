# Разрешённая очистка

Резервная копия до изменений: отдельный KelMagic-before-cleanup.tar.gz. Исходный пользовательский архив также сохранён.

Удалено:
- bin
- .clinerules
- tools/KelMagic-Studio-Bridge/node_modules
- src/main/java/dev/kelmagic/Config.java
- src/main/java/dev/kelmagic/KelMagicClient.java
- src/main/resources/assets/kelmagic/models/item/magic_crystal.json
- src/main/resources/assets/kelmagic/textures/item/magic_crystal.png
- run/hs_err_pid30080.log
- tools/KelMagic-Studio-Bridge/server.js.before-piskel-2026-10-09T19-18-02-113Z.bak
- art/generated/bridge-test/bridge_test.png.png

В KelMagic.java убраны регистрации example_block, example_item, magic_crystal и example_tab, demo handlers. Не удалялись исходники Studio Bridge, концепты, рабочие настройки Gradle и wrapper.
