package me.arisdonate.commands;

import me.arisdonate.ArisDonatePlugin;
import me.arisdonate.util.BaseCommand;
import me.arisdonate.util.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * /shop — главное меню магазина: разделы (донаты, сферы, ссылки).
 */
public class ShopCommand extends BaseCommand {
    public ShopCommand(ArisDonatePlugin plugin) { super(plugin); }

    @Override
    protected void execute(CommandSender sender, Command command, String label, String[] args) {
        Player p = requirePlayer(sender);
        if (p == null) return;

        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "spheres", "sphere" -> { plugin.getSphereShopGui().open(p); return; }
                case "donate", "donates" -> { plugin.getDonateGui().open(p); return; }
                case "links" -> { printLinks(p); return; }
            }
        }

        Inventory inv = Bukkit.createInventory(null, 27,
                Msg.parse("<grad:#FFD700:#FF1493>★ МАГАЗИН СЕРВЕРА ★</grad>"));
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.displayName(Component.text(" "));
        pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        inv.setItem(11, sectionIcon(Material.NETHER_STAR,
                "<grad:#FFD700:#FF8C00>Донаты</grad>",
                "&7Купить ранг и крутые плюшки.",
                "&8(/shop donate)"));
        inv.setItem(13, sectionIcon(Material.MAGMA_CREAM,
                "<grad:#FF1493:#A020F0>Сферы</grad>",
                "&7Магические сферы с бафами/дебафами.",
                "&7Носятся в шлеме или второй руке.",
                "&8(/shop spheres)"));
        inv.setItem(15, sectionIcon(Material.WRITABLE_BOOK,
                "<grad:#00BFFF:#1E90FF>Discord / Сайт</grad>",
                "&7Ссылки на покупки и сайт.",
                "&8(/shop links)"));

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.displayName(Msg.parse("&c&lЗакрыть"));
        close.setItemMeta(cm);
        inv.setItem(22, close);

        p.openInventory(inv);
    }

    private ItemStack sectionIcon(Material mat, String name, String... loreLines) {
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        im.displayName(Msg.parse(name));
        List<Component> lore = new ArrayList<>();
        lore.add(Msg.parse("&8&m                            "));
        for (String l : loreLines) lore.add(Msg.parse(l));
        lore.add(Msg.parse("&8&m                            "));
        im.lore(lore);
        it.setItemMeta(im);
        return it;
    }

    private void printLinks(Player p) {
        List<String> shopUrls = plugin.getConfig().getStringList("shop-links");
        if (shopUrls.isEmpty()) {
            p.sendMessage(Msg.parse("&6Магазин сервера:"));
            p.sendMessage(Msg.parse("&7Свяжитесь с администрацией для покупки доната."));
            p.sendMessage(Component.text("[Открыть Discord]").color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl(plugin.getConfig().getString("discord-url", "https://discord.gg/"))));
            return;
        }
        for (String url : shopUrls) {
            p.sendMessage(Component.text("→ " + url).color(net.kyori.adventure.text.format.NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl(url)));
        }
    }
}
