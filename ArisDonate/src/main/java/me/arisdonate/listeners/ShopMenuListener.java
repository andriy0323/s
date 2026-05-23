package me.arisdonate.listeners;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.util.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ShopMenuListener implements Listener {

    private final ArisDonatePlugin plugin;

    public ShopMenuListener(ArisDonatePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        Component title = e.getView().title();
        if (title == null) return;
        String t = Msg.stripFormatting(title);
        if (!t.contains("МАГАЗИН СЕРВЕРА")) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack it = e.getCurrentItem();
        if (it == null || it.getType().isAir()) return;
        if (it.getType() == Material.BARRIER) { p.closeInventory(); return; }
        if (it.getType() == Material.NETHER_STAR)  { plugin.getDonateGui().open(p); return; }
        if (it.getType() == Material.MAGMA_CREAM)  { plugin.getSphereShopGui().open(p); return; }
        if (it.getType() == Material.WRITABLE_BOOK) {
            p.closeInventory();
            p.performCommand("shop links");
        }
    }
}
