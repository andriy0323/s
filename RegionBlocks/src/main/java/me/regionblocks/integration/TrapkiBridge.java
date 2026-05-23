package me.regionblocks.integration;

import me.regionblocks.RegionBlocks;
import me.trapki.TrapkiPlugin;
import me.trapki.managers.TrapItemManager;
import me.trapki.models.TrapType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Безопасная обёртка над плагином Trapki. RegionBlocks softdepend'ит на Trapki —
 * если плагин не загружен, методы возвращают null/0 и магазин показывает плейсхолдер.
 */
public final class TrapkiBridge {

    private TrapkiBridge() {}

    public static boolean isAvailable() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("Trapki");
        return pl != null && pl.isEnabled();
    }

    public static TrapkiPlugin get() {
        Plugin pl = Bukkit.getPluginManager().getPlugin("Trapki");
        return (pl instanceof TrapkiPlugin tp) ? tp : null;
    }

    public static TrapType[] allTraps() {
        return TrapType.values();
    }

    public static long priceOf(TrapType type) {
        TrapkiPlugin pl = get();
        return pl != null ? pl.priceOf(type) : type.defaultPrice();
    }

    public static long scrapPrice() {
        TrapkiPlugin pl = get();
        return pl != null ? pl.scrapPrice() : 500L;
    }

    public static ItemStack createTrap(TrapType type) {
        return TrapItemManager.createTrap(type, 1);
    }

    public static ItemStack createScrap() {
        return TrapItemManager.createScrap(1);
    }
}
