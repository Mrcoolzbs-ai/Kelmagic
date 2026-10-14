# Wildfire's Female Gender Mod: изученный ориентир

Предоставленный пользователем архив: Wildfire-s-Female-Gender-Mod---Recode-fabric-1.21.11.zip. В README и fabric.mod.json заявлена LGPL-3.0; LICENSE содержит GNU LGPL version3.
Автор: WildfireRomeo; исходники, указанные в fabric.mod.json: https://github.com/WildfireRomeo/WildfireFemaleGenderMod/tree/master .

Изучены BreastPhysics.java, GenderLayer.java, WildfireModelRenderer.java, Breasts.java. Подход: раздельные объёмы4×5×3, наружный разворот/наклон, движение по двум осям и поворот, история положения и частичная интерполяция, влияние шага/позы/транспортного средства/брони.

В KelMagic реализован собственный аналог для одного взрослого NPC на NeoForge1.21.1, без переноса Fabric/API/mixins/исходного кода Wildfire's. Наша геометрия4×5×4, сглаженная ограниченная инерция и собственные параметры. Внешний мод не нужен. Сходный силуэт не означает полный порт всех настроек, брони и транспорта из Wildfire's.

При дальнейшем прямом переносе их кода необходимо сохранить авторские уведомления, LGPL/GPL и выполнить условия распространения соответствующего кода; одна надпись «open-source» не отменяет требований лицензии.
