package me.regionblocks.integration;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.managers.KitManager;
import me.arisdonate.managers.SphereManager;
import me.arisdonate.models.Sphere;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Тонкая обёртка над плагином ArisDonate.
 * Используется ShopListener'ом, чтобы добавить в /shop разделы:
 *   - Сферы (обычные сферы из ArisDonate-конфига с price>0)
 *   - Шары  (премиум-сферы из ArisDonate-конфига с price=0 — теперь продаются)
 *   - Киты  (все киты из ArisDonate-конфига; покупаются очень дорого)
 *
 * Все цены — это цены в Aris-coins (валюта RegionBlocks), независимые от
 * Aris-coins-валюты ArisDonate (там, кажется, своя экономика).
 */
public final class ArisDonateBridge {

    /** Цены сфер (id → Aris-coins). */
    public static final Map<String, Long> SPHERE_PRICES = sphereMap();

    /** Цены шаров (премиум-сфер). */
    public static final Map<String, Long> BALL_PRICES = ballMap();

    /** Цены китов. Очень дорогие — по просьбе пользователя. */
    public static final Map<String, Long> KIT_PRICES = kitMap();

    private ArisDonateBridge() {}

    public static boolean isAvailable() {
        return get() != null;
    }

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

    /** Список сфер, попадающих в раздел «Сферы» (price>0 в конфиге ArisDonate). */
    public static List<Sphere> regularSpheres() {
        SphereManager sm = spheres();
        if (sm == null) return Collections.emptyList();
        return sm.all().stream()
                .filter(s -> SPHERE_PRICES.containsKey(s.id()))
                .toList();
    }

    /** Список сфер, попадающих в раздел «Шары» (премиум, изначально только из китов). */
    public static List<Sphere> premiumBalls() {
        SphereManager sm = spheres();
        if (sm == null) return Collections.emptyList();
        return sm.all().stream()
                .filter(s -> BALL_PRICES.containsKey(s.id()))
                .toList();
    }

    /** Список китов с привязанными ценами. */
    public static List<KitManager.Kit> allKits() {
        KitManager km = kits();
        if (km == null) return Collections.emptyList();
        return km.all().stream()
                .filter(k -> KIT_PRICES.containsKey(k.id))
                .toList();
    }

    public static long sphereAris(String id)  { return SPHERE_PRICES.getOrDefault(id, 0L); }
    public static long ballAris(String id)    { return BALL_PRICES.getOrDefault(id, 0L); }
    public static long kitAris(String id)     { return KIT_PRICES.getOrDefault(id, 0L); }

    private static Map<String, Long> sphereMap() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("bounce",   200L);
        m.put("ember",    250L);
        m.put("owl",      180L);
        m.put("tide",     350L);
        m.put("force",    500L);
        m.put("bulwark",  600L);
        m.put("gale",     450L);
        m.put("vitality", 700L);
        return Collections.unmodifiableMap(m);
    }

    private static Map<String, Long> ballMap() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("nebula",   2000L);
        m.put("cosmos",   2500L);
        m.put("phoenix",  3500L);
        m.put("phantom",  6000L);
        m.put("chaos",    9000L);
        return Collections.unmodifiableMap(m);
    }

    private static Map<String, Long> kitMap() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("spark",      5000L);
        m.put("luna",       8000L);
        m.put("stellar",   12000L);
        m.put("nova",      18000L);
        m.put("comet",     25000L);
        m.put("galaxy",    35000L);
        m.put("nebula",    50000L);
        m.put("cosmos",    70000L);
        m.put("phoenix",   90000L);
        m.put("aris",     120000L);
        m.put("arisplus", 150000L);
        return Collections.unmodifiableMap(m);
    }
}
