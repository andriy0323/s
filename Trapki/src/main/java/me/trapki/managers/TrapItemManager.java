package me.trapki.managers;

import me.trapki.TrapkiPlugin;
import me.trapki.models.TrapType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/** Создаёт и распознаёт ItemStack'и трапок и обломков незерита. */
public final class TrapItemManager {

    public static final String PDC_TRAP_TYPE_KEY    = "trap_type";
    public static final String PDC_NETHERITE_ITEM   = "trapki_scrap";

    private static TrapkiPlugin plugin;
    private static NamespacedKey trapKey;
    private static NamespacedKey scrapKey;

    private TrapItemManager() {}

    public static void init(TrapkiPlugin pl) {
        plugin = pl;
        trapKey  = new NamespacedKey(pl, PDC_TRAP_TYPE_KEY);
        scrapKey = new NamespacedKey(pl, PDC_NETHERITE_ITEM);
    }

    public static NamespacedKey trapKey()  { return trapKey; }
    public static NamespacedKey scrapKey() { return scrapKey; }

    /** Создаёт ItemStack-трапку. */
    public static ItemStack createTrap(TrapType type, int amount) {
        ItemStack item = new ItemStack(type.icon(), amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(legacyComponent(type.displayName())
            .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        for (String line : type.description().split("\n")) {
            lore.add(Component.text("  " + line)
                .color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("  Зона: ").color(TextColor.color(0x888888))
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(type.size() + "×" + type.size() + " блоков")
                .color(TextColor.color(0xFFCC55))
                .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(Component.text("  ► ПКМ по блоку, чтобы поставить.")
            .color(TextColor.color(0x55FF55))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  ► Нужен 1 обломок незерита.")
            .color(TextColor.color(0xFFAA33))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(separator());
        meta.lore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(trapKey, PersistentDataType.STRING, type.id());

        item.setItemMeta(meta);
        return item;
    }

    /** Создаёт ItemStack «Обломок незерита» (ванильный материал, но с RU именем/lore). */
    public static ItemStack createScrap(int amount) {
        ItemStack item = new ItemStack(Material.NETHERITE_SCRAP, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Component.text("✦ Обломок незерита ✦")
            .color(TextColor.color(0x9B6B41))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));

        meta.lore(List.of(
            separator(),
            Component.text("  Используется для активации").color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false),
            Component.text("  любой трапки. Расходуется").color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false),
            Component.text("  при установке (1 шт.).").color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false),
            Component.empty(),
            Component.text("  Падает из премиум-китов")
                .color(TextColor.color(0xFFAA33))
                .decoration(TextDecoration.ITALIC, false),
            Component.text("  или покупается в /shop.")
                .color(TextColor.color(0xFFAA33))
                .decoration(TextDecoration.ITALIC, false),
            separator()
        ));

        meta.getPersistentDataContainer().set(scrapKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /** Если item — трапка, возвращает её тип, иначе null. */
    public static TrapType trapTypeOf(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String id = meta.getPersistentDataContainer().get(trapKey, PersistentDataType.STRING);
        return TrapType.byId(id);
    }

    /** Любой ванильный NETHERITE_SCRAP считается ингредиентом. */
    public static boolean isScrap(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_SCRAP;
    }

    private static Component separator() {
        return Component.text("                                ")
            .color(TextColor.color(0x444444))
            .decoration(TextDecoration.STRIKETHROUGH, true)
            .decoration(TextDecoration.ITALIC, false);
    }

    private static Component legacyComponent(String legacy) {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
            .legacySection().deserialize(legacy);
    }
}
