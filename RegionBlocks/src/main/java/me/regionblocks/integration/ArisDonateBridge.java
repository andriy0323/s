package me.regionblocks.integration;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.managers.KitManager;
import me.arisdonate.managers.SphereManager;
import me.arisdonate.models.Sphere;
import me.regionblocks.RegionBlocks;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Тонкая обёртка над плагином ArisDonate + загрузка прайс-листа магазина
 * из RegionBlocks/config.yml.
 *
 * Используется ShopListener'ом, чтобы добавить в /shop разделы:
 *   - Сферы (обычные сферы из ArisDonate-конфига)
 *   - Шары  (премиум-сферы из ArisDonate-конфига)
 *   - Киты  (все киты из ArisDonate-конфига; покупаются дорого)
 *
 * Цены — Aris-coins (валюта RegionBlocks), независимые от Aris-coins
 * самого ArisDonate.
 */
public final class ArisDonateBridge {

    private static Map<String, Long> spherePrices = Collections.emptyMap();
    private static Map<String, Long> ballPrices   = Collections.emptyMap();
    private static Map<String, Long> kitPrices    = Collections.emptyMap();

    private ArisDonateBridge() {}

    /** Перечитывает прайс-лист из RegionBlocks/config.yml. */
    public static void reloadPrices(RegionBlocks plugin) {
        spherePrices = loadSection(plugin, "shop.spheres");
        ballPrices   = loadSection(plugin, "shop.balls");
        kitPrices    = loadSection(plugin, "shop.kits");
    }

    private static Map<String, Long> loadSection(RegionBlocks plugin, String path) {
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection(path);
        if (sec == null) return Collections.emptyMap();
        Map<String, Long> m = new LinkedHashMap<>();
        for (String key : sec.getKeys(false)) {
            m.put(key.toLowerCase(), sec.getLong(key));
        }
        return Collections.unmodifiableMap(m);
    }

    public static boolean isAvailable() { return get() != null; }

    public static ArisDonatePlugin get() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("ArisDonate");
        if (pl instanceof ArisDonatePlugin ad && ad.isEnabled()) return ad;
        return null;
    }

    public static SphereManager spheres() {
        ArisDonatePlugin ad = get();
        return ad == null ? null : ad.getSphereManager();
    }

    public static KitManager kits() {
        ArisDonatePlugin ad = get();
        return ad == null ? null : ad.getKitManager();
    }

    /** Список сфер, попадающих в раздел «Сферы» (есть запись в config.yml). */
    public static List<Sphere> regularSpheres() {
        SphereManager sm = spheres();
        if (sm == null) return Collections.emptyList();
        return sm.all().stream()
                .filter(s -> spherePrices.containsKey(s.id()))
                .toList();
    }

    /** Список сфер, попадающих в раздел «Шары» (премиум). */
    public static List<Sphere> premiumBalls() {
        SphereManager sm = spheres();
        if (sm == null) return Collections.emptyList();
        return sm.all().stream()
                .filter(s -> ballPrices.containsKey(s.id()))
                .toList();
    }

    /** Список китов с привязанными ценами. */
    public static List<KitManager.Kit> allKits() {
        KitManager km = kits();
        if (km == null) return Collections.emptyList();
        return km.all().stream()
                .filter(k -> kitPrices.containsKey(k.id))
                .toList();
    }

    public static long sphereAris(String id) { return spherePrices.getOrDefault(id, 0L); }
    public static long ballAris(String id)   { return ballPrices.getOrDefault(id, 0L); }
    public static long kitAris(String id)    { return kitPrices.getOrDefault(id, 0L); }

    public static Map<String, Long> spherePrices() { return spherePrices; }
    public static Map<String, Long> ballPrices()   { return ballPrices; }
    public static Map<String, Long> kitPrices()    { return kitPrices; }
}
