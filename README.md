# RegionBlocks + ArisDonate

Двa плагина Paper 1.21.x, работающих в связке.

## Структура

- `RegionBlocks/` — система регионов через спецблоки и единый `/shop`
  (приваты, ТНТ, вагонетки, **сферы**, **шары**, **киты**).
- `ArisDonate/` — донат-система, /home, /warp, /tpa, /kit, /msg и 150+ команд.

## Сборка

Нужны Java 21 и Maven.

```bash
# сначала установить ArisDonate в локальный m2 (от него зависит RegionBlocks)
mvn -DskipTests -f ArisDonate/pom.xml clean install

# затем собрать RegionBlocks
mvn -DskipTests -f RegionBlocks/pom.xml clean package
```

Готовые jar'ы:

- `ArisDonate/target/ArisDonate-1.0.0.jar`
- `RegionBlocks/target/RegionBlocks-1.0.0.jar`

Оба плагина кидаются в `plugins/` на сервере. RegionBlocks мягко зависит
(`softdepend`) от ArisDonate — если ArisDonate не загружен, разделы
магазина "Сферы", "Шары", "Киты" покажут красное предупреждение, остальной
магазин (приваты/ТНТ/вагонетки) продолжит работать.

## Магазин (`/shop`)

Команда `/shop` зарегистрирована плагином RegionBlocks. У ArisDonate своя
`/shop` была удалена — единая точка входа теперь одна.

Вкладки:

| Вкладка   | Содержимое                                                 |
|-----------|------------------------------------------------------------|
| Приваты   | блоки регионов (Common…Aris)                               |
| ТНТ       | взрывные блоки (Basic…Titan)                               |
| Вагонетки | взрывные вагонетки (Basic…Titan)                           |
| Сферы     | обычные сферы (`bounce`, `ember`, `owl`, `tide`, `force`…) |
| Шары      | премиум-сферы (`nebula`, `cosmos`, `phoenix`, `phantom`, `chaos`) |
| Киты      | все донат-киты (`spark`…`arisplus`), очень дорогие         |

Цены вкладок «Сферы», «Шары», «Киты» хардкодятся в
`RegionBlocks/src/main/java/me/regionblocks/integration/ArisDonateBridge.java`.

Оплата — Aris-coins (валюта плагина RegionBlocks, не путать с одноимённой
валютой ArisDonate). Пополнение Aris-coins — `/a give <ник> <кол>`.
