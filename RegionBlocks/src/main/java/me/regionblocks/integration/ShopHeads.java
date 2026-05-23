package me.regionblocks.integration;

import me.regionblocks.RegionBlocks;
import me.regionblocks.util.HeadUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Загружает текстуры голов из RegionBlocks/config.yml (секция heads:)
 * и выдаёт ItemStack-головы для иконок магазина.
 *
 * Если в config.yml стоит непустая строка → отдаётся PLAYER_HEAD с
 * кастомной текстурой. Иначе — vanilla mob-head (DRAGON_HEAD, CREEPER_HEAD,
 * ZOMBIE_HEAD, SKELETON_SKULL, WITHER_SKELETON_SKULL, PIGLIN_HEAD), чтобы
 * без настройки уже были визуально разные «головы» вместо обычных предметов.
 */
public final class ShopHeads {

    private static final Map<String, Material> FALLBACK = buildFallback();

    private static Map<String, String> textures = Collections.emptyMap();

    private ShopHeads() {}

    public static void reload(RegionBlocks plugin) {
        Map<String, String> flat = new HashMap<>();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("heads");
        if (root != null) collect(root, "", flat);
        textures = Collections.unmodifiableMap(flat);
    }

    private static void collect(ConfigurationSection sec, String prefix, Map<String, String> out) {
        for (String key : sec.getKeys(false)) {
            String full = prefix.isEmpty() ? key : (prefix + "." + key);
            Object v = sec.get(key);
            if (v instanceof ConfigurationSection child) {
                collect(child, full, out);
            } else if (v instanceof String s && !s.isBlank()) {
                out.put(full.toLowerCase(), s.trim());
            }
        }
    }

    /** Голова по ключу вида "spheres.bounce". */
    public static ItemStack head(String key) {
        String texture = textures.get(key.toLowerCase());
        if (texture != null && !texture.isEmpty()) {
            return HeadUtil.customHead(texture);
        }
        Material fallback = FALLBACK.getOrDefault(key.toLowerCase(), Material.PLAYER_HEAD);
        return new ItemStack(fallback);
    }

    private static Map<String, Material> buildFallback() {
        Map<String, Material> m = new HashMap<>();
        // ── Приваты ───────────────────────────────────────────────────────────
        m.put("privates.common",    Material.SKELETON_SKULL);
        m.put("privates.rare",      Material.ZOMBIE_HEAD);
        m.put("privates.epic",      Material.CREEPER_HEAD);
        m.put("privates.mythic",    Material.PIGLIN_HEAD);
        m.put("privates.legendary", Material.WITHER_SKELETON_SKULL);
        m.put("privates.aris",      Material.DRAGON_HEAD);

        // ── ТНТ ───────────────────────────────────────────────────────────────
        m.put("tnt.basic",  Material.CREEPER_HEAD);
        m.put("tnt.strong", Material.ZOMBIE_HEAD);
        m.put("tnt.mega",   Material.WITHER_SKELETON_SKULL);
        m.put("tnt.titan",  Material.DRAGON_HEAD);

        // ── Вагонетки ─────────────────────────────────────────────────────────
        m.put("minecart.basic",  Material.SKELETON_SKULL);
        m.put("minecart.strong", Material.ZOMBIE_HEAD);
        m.put("minecart.mega",   Material.PIGLIN_HEAD);
        m.put("minecart.titan",  Material.WITHER_SKELETON_SKULL);

        // ── Сферы ─────────────────────────────────────────────────────────────
        m.put("spheres.bounce",   Material.SKELETON_SKULL);
        m.put("spheres.ember",    Material.CREEPER_HEAD);
        m.put("spheres.owl",      Material.WITHER_SKELETON_SKULL);
        m.put("spheres.tide",     Material.ZOMBIE_HEAD);
        m.put("spheres.force",    Material.PIGLIN_HEAD);
        m.put("spheres.bulwark",  Material.SKELETON_SKULL);
        m.put("spheres.gale",     Material.CREEPER_HEAD);
        m.put("spheres.vitality", Material.PIGLIN_HEAD);

        // ── Шары ──────────────────────────────────────────────────────────────
        m.put("balls.nebula",   Material.ZOMBIE_HEAD);
        m.put("balls.cosmos",   Material.SKELETON_SKULL);
        m.put("balls.phoenix",  Material.CREEPER_HEAD);
        m.put("balls.phantom",  Material.WITHER_SKELETON_SKULL);
        m.put("balls.chaos",    Material.DRAGON_HEAD);

        // ── Киты ──────────────────────────────────────────────────────────────
        m.put("kits.spark",    Material.ZOMBIE_HEAD);
        m.put("kits.luna",     Material.SKELETON_SKULL);
        m.put("kits.stellar",  Material.PIGLIN_HEAD);
        m.put("kits.nova",     Material.CREEPER_HEAD);
        m.put("kits.comet",    Material.WITHER_SKELETON_SKULL);
        m.put("kits.galaxy",   Material.PIGLIN_HEAD);
        m.put("kits.nebula",   Material.ZOMBIE_HEAD);
        m.put("kits.cosmos",   Material.SKELETON_SKULL);
        m.put("kits.phoenix",  Material.CREEPER_HEAD);
        m.put("kits.aris",     Material.DRAGON_HEAD);
        m.put("kits.arisplus", Material.DRAGON_HEAD);

        // ── Навигация / инфо / баланс ─────────────────────────────────────────
        m.put("nav.privates",  Material.ZOMBIE_HEAD);
        m.put("nav.tnt",       Material.CREEPER_HEAD);
        m.put("nav.minecart",  Material.SKELETON_SKULL);
        m.put("nav.spheres",   Material.DRAGON_HEAD);
        m.put("nav.balls",     Material.PIGLIN_HEAD);
        m.put("nav.kits",      Material.WITHER_SKELETON_SKULL);

        m.put("info.privates", Material.PLAYER_HEAD);
        m.put("info.tnt",      Material.PLAYER_HEAD);
        m.put("info.minecart", Material.PLAYER_HEAD);
        m.put("info.spheres",  Material.PLAYER_HEAD);
        m.put("info.balls",    Material.PLAYER_HEAD);
        m.put("info.kits",     Material.PLAYER_HEAD);

        m.put("balance",       Material.PLAYER_HEAD);
        m.put("ariscoin",      Material.DRAGON_HEAD);

        return Collections.unmodifiableMap(m);
    }
}
