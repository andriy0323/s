package me.trapki.models;

import org.bukkit.Material;

/**
 * 10 типов трапок. 5 малых (3×3, радиус 1) + 5 больших (5×5, радиус 2).
 *
 * Поля:
 *   id           — уникальный id (для config / pdc / команд)
 *   displayName  — RU название с §-кодами
 *   icon         — иконка в магазине / inventory
 *   size         — длина стороны зоны (3 или 5)
 *   colorHex     — основной цвет (для lore/градиента)
 *   description  — короткое описание эффекта (lore)
 *   defaultPrice — цена в Aris-coins по умолчанию (можно переопределить в config.yml)
 */
public enum TrapType {

    //          id              displayName                  icon                       size  colorHex   defaultPrice  description
    SPIKE     ("spike",     "§7Шиповой капкан",            Material.IRON_TRAPDOOR,     3, 0xAAAAAA, 500L,
               "Наносит 4 урона любому,\nкто заходит в зону 3×3."),
    SWAMP     ("swamp",     "§aБолотная ловушка",          Material.SLIME_BLOCK,       3, 0x55FF55, 700L,
               "Замедление III на 6 секунд\nна всех в зоне 3×3."),
    POISON    ("poison",    "§2Ядовитая ловушка",          Material.SPIDER_EYE,        3, 0x228B22, 900L,
               "Яд II на 5 секунд\nна всех в зоне 3×3."),
    SMOKE     ("smoke",     "§8Дымовая ловушка",           Material.GUNPOWDER,         3, 0x444444, 1100L,
               "Слепота на 6 секунд\nна всех в зоне 3×3."),
    EMBER     ("ember",     "§6Угольная ловушка",          Material.CAMPFIRE,          3, 0xFFA500, 1500L,
               "Поджигает на 4 секунды\n+ 2 урона. Зона 3×3."),

    LIGHTNING ("lightning", "§e§lГромоносная ловушка",     Material.LIGHTNING_ROD,     5, 0xFFFF55, 3000L,
               "Бьёт молнией в центр\n+ 6 урона. Зона 5×5."),
    BOOM      ("boom",      "§c§lВзрывная ловушка",        Material.TNT,               5, 0xFF4422, 4500L,
               "Взрыв силы 3 (без блоков)\n+ 10 урона. Зона 5×5."),
    WITHER    ("wither",    "§0§lВиземическая ловушка",    Material.WITHER_ROSE,       5, 0x222222, 6000L,
               "Wither II на 10 секунд\nна всех в зоне 5×5."),
    MAGNET    ("magnet",    "§9§lМагнитная ловушка",       Material.HEAVY_CORE,        5, 0x4444FF, 7500L,
               "Стягивает всех врагов\nк центру ловушки. Зона 5×5."),
    CHAOS     ("chaos",     "§d§lХаосная ловушка",         Material.NETHER_STAR,       5, 0xFF00FF, 10000L,
               "Слабость + Замедление + Слепота\n+ Яд одновременно, 8 секунд. Зона 5×5.");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final int size;
    private final int colorHex;
    private final long defaultPrice;
    private final String description;

    TrapType(String id, String displayName, Material icon, int size, int colorHex,
             long defaultPrice, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.size = size;
        this.colorHex = colorHex;
        this.defaultPrice = defaultPrice;
        this.description = description;
    }

    public String id()           { return id; }
    public String displayName()  { return displayName; }
    public Material icon()       { return icon; }
    public int size()            { return size; }
    public int radius()          { return (size - 1) / 2; }   // 3→1, 5→2
    public int colorHex()        { return colorHex; }
    public long defaultPrice()   { return defaultPrice; }
    public String description()  { return description; }
    public boolean isLarge()     { return size >= 5; }

    public static TrapType byId(String id) {
        if (id == null) return null;
        for (TrapType t : values()) if (t.id.equalsIgnoreCase(id)) return t;
        return null;
    }
}
