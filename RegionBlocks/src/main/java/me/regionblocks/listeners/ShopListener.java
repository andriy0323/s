package me.regionblocks.listeners;

import me.arisdonate.managers.KitManager;
import me.arisdonate.models.Sphere;
import me.regionblocks.RegionBlocks;
import me.regionblocks.integration.ArisDonateBridge;
import me.regionblocks.managers.ArisItemManager;
import me.regionblocks.managers.LegendaryItemManager;
import me.regionblocks.managers.MinecartItemManager;
import me.regionblocks.managers.TntItemManager;
import me.regionblocks.models.MinecartType;
import me.regionblocks.models.RegionTier;
import me.regionblocks.models.TntType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopListener implements Listener {

    private static final String TITLE_PRIVATES  = "✦ Магазин — Приваты ✦";
    private static final String TITLE_TNT       = "✦ Магазин — ТНТ ✦";
    private static final String TITLE_MINECART  = "✦ Магазин — Вагонетки ✦";
    private static final String TITLE_SPHERES   = "✦ Магазин — Сферы ✦";
    private static final String TITLE_BALLS     = "✦ Магазин — Шары ✦";
    private static final String TITLE_KITS      = "✦ Магазин — Киты ✦";

    private static final int[] SPHERE_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19};
    private static final int[] BALL_SLOTS   = {11, 12, 13, 14, 15};
    private static final int[] KIT_SLOTS    = {10, 11, 12, 13, 14, 15, 16,
                                               19, 20, 21, 24};

    private final RegionBlocks plugin;

    public ShopListener(RegionBlocks plugin) {
        this.plugin = plugin;
    }

    // ══ Открытие разделов ════════════════════════════════════════════════════

    public void openShop(Player player) { openPrivatesTab(player); }

    // Удобные геттеры для тестов / других классов
    public void openSpheres(Player p)  { openSpheresTab(p); }
    public void openBalls(Player p)    { openBallsTab(p); }
    public void openKits(Player p)     { openKitsTab(p); }

    public void openPrivatesTab(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(TITLE_PRIVATES).color(TextColor.color(0xFFAA00)));
        fillBorder(inv);
        long bal = plugin.getArisManager().getBalance(player.getName());

        inv.setItem(10, shopItem(RegionTier.COMMON,    bal, 100));
        inv.setItem(12, shopItem(RegionTier.RARE,      bal, 300));
        inv.setItem(14, shopItem(RegionTier.EPIC,      bal, 700));
        inv.setItem(16, shopItem(RegionTier.MYTHIC,    bal, 1500));
        inv.setItem(29, shopItem(RegionTier.LEGENDARY, bal, 4000));
        inv.setItem(33, arisShopItem(bal, 10000));
        inv.setItem(31, balanceDisplay(bal));

        addNavRow(inv, TabKind.PRIVATES);
        player.openInventory(inv);
    }

    public void openTntTab(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(TITLE_TNT).color(TextColor.color(0xFF4422)));
        fillBorder(inv);
        long bal = plugin.getArisManager().getBalance(player.getName());

        int[] slots = {11, 13, 15, 20};
        TntType[] types = TntType.values();
        for (int i = 0; i < types.length && i < slots.length; i++)
            inv.setItem(slots[i], tntShopItem(types[i], bal));

        inv.setItem(29, rgbItem(Material.RED_DYE,   "§cКрасный порошок",  "Усиливает взрыв"));
        inv.setItem(31, rgbItem(Material.GREEN_DYE, "§aЗелёный порошок",  "Расширяет радиус"));
        inv.setItem(33, rgbItem(Material.BLUE_DYE,  "§9Синий порошок",    "Пробивает защиту"));
        inv.setItem(22, balanceDisplay(bal));

        addNavRow(inv, TabKind.TNT);
        player.openInventory(inv);
    }

    public void openMinecartTab(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(TITLE_MINECART).color(TextColor.color(0xFF8800)));
        fillBorder(inv);
        long bal = plugin.getArisManager().getBalance(player.getName());

        int[] slots = {11, 13, 15, 20};
        MinecartType[] types = MinecartType.values();
        for (int i = 0; i < types.length && i < slots.length; i++)
            inv.setItem(slots[i], minecartShopItem(types[i], bal));

        inv.setItem(22, balanceDisplay(bal));

        addNavRow(inv, TabKind.MINECART);
        player.openInventory(inv);
    }

    // ── Новые вкладки: Сферы, Шары, Киты ─────────────────────────────────────

    public void openSpheresTab(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(TITLE_SPHERES).color(TextColor.color(0xFF55FF)));
        fillBorder(inv);
        long bal = plugin.getArisManager().getBalance(player.getName());

        if (!ArisDonateBridge.isAvailable()) {
            inv.setItem(22, missingArisDonateItem());
        } else {
            List<Sphere> list = ArisDonateBridge.regularSpheres();
            for (int i = 0; i < list.size() && i < SPHERE_SLOTS.length; i++) {
                inv.setItem(SPHERE_SLOTS[i], sphereShopItem(list.get(i), bal));
            }
            inv.setItem(31, balanceDisplay(bal));
            inv.setItem(22, infoItem("§d✦ Сферы",
                "Магические сферы с бафами/дебафами.",
                "Носи в шлеме или второй руке."));
        }

        addNavRow(inv, TabKind.SPHERES);
        player.openInventory(inv);
    }

    public void openBallsTab(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(TITLE_BALLS).color(TextColor.color(0xFF1493)));
        fillBorder(inv);
        long bal = plugin.getArisManager().getBalance(player.getName());

        if (!ArisDonateBridge.isAvailable()) {
            inv.setItem(22, missingArisDonateItem());
        } else {
            List<Sphere> list = ArisDonateBridge.premiumBalls();
            for (int i = 0; i < list.size() && i < BALL_SLOTS.length; i++) {
                inv.setItem(BALL_SLOTS[i], ballShopItem(list.get(i), bal));
            }
            inv.setItem(31, balanceDisplay(bal));
            inv.setItem(22, infoItem("§d✦ Шары (премиум)",
                "Премиум-сферы. Эксклюзив из китов,",
                "теперь можно купить за Арисы."));
        }

        addNavRow(inv, TabKind.BALLS);
        player.openInventory(inv);
    }

    public void openKitsTab(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(TITLE_KITS).color(TextColor.color(0xFFD700)));
        fillBorder(inv);
        long bal = plugin.getArisManager().getBalance(player.getName());

        if (!ArisDonateBridge.isAvailable()) {
            inv.setItem(22, missingArisDonateItem());
        } else {
            List<KitManager.Kit> list = ArisDonateBridge.allKits();
            for (int i = 0; i < list.size() && i < KIT_SLOTS.length; i++) {
                inv.setItem(KIT_SLOTS[i], kitShopItem(list.get(i), bal));
            }
            inv.setItem(40, balanceDisplay(bal));
            inv.setItem(22, infoItem("§6✦ Киты — очень дорогие",
                "Покупка кита выдаёт весь содержимый",
                "шалкер и предметы без кулдауна."));
        }

        addNavRow(inv, TabKind.KITS);
        player.openInventory(inv);
    }

    // ══ Обработка кликов ═════════════════════════════════════════════════════

    private enum TabKind { PRIVATES, TNT, MINECART, SPHERES, BALLS, KITS }

    private static TabKind tabFromTitle(String plain) {
        if (plain.contains("Приваты"))   return TabKind.PRIVATES;
        if (plain.contains("ТНТ"))       return TabKind.TNT;
        if (plain.contains("Вагонетки")) return TabKind.MINECART;
        if (plain.contains("Сферы"))     return TabKind.SPHERES;
        if (plain.contains("Шары"))      return TabKind.BALLS;
        if (plain.contains("Киты"))      return TabKind.KITS;
        return null;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String plain = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
            .plainText().serialize(e.getView().title());
        TabKind tab = tabFromTitle(plain);
        if (tab == null) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        int slot = e.getSlot();

        // Сначала проверяем кнопки навигации, общие для всех вкладок
        if (handleNavClick(player, slot)) return;

        switch (tab) {
            case PRIVATES -> {
                switch (slot) {
                    case 10 -> buy(player, RegionTier.COMMON,    100);
                    case 12 -> buy(player, RegionTier.RARE,      300);
                    case 14 -> buy(player, RegionTier.EPIC,      700);
                    case 16 -> buy(player, RegionTier.MYTHIC,    1500);
                    case 29 -> buy(player, RegionTier.LEGENDARY, 4000);
                    case 33 -> buyAris(player, 10000);
                }
            }
            case TNT -> {
                switch (slot) {
                    case 11 -> buyTnt(player, TntType.BASIC);
                    case 13 -> buyTnt(player, TntType.STRONG);
                    case 15 -> buyTnt(player, TntType.MEGA);
                    case 20 -> buyTnt(player, TntType.TITAN);
                }
            }
            case MINECART -> {
                switch (slot) {
                    case 11 -> buyMinecart(player, MinecartType.BASIC);
                    case 13 -> buyMinecart(player, MinecartType.STRONG);
                    case 15 -> buyMinecart(player, MinecartType.MEGA);
                    case 20 -> buyMinecart(player, MinecartType.TITAN);
                }
            }
            case SPHERES -> {
                List<Sphere> list = ArisDonateBridge.regularSpheres();
                int idx = indexOf(SPHERE_SLOTS, slot);
                if (idx >= 0 && idx < list.size()) buySphere(player, list.get(idx), false);
            }
            case BALLS -> {
                List<Sphere> list = ArisDonateBridge.premiumBalls();
                int idx = indexOf(BALL_SLOTS, slot);
                if (idx >= 0 && idx < list.size()) buySphere(player, list.get(idx), true);
            }
            case KITS -> {
                List<KitManager.Kit> list = ArisDonateBridge.allKits();
                int idx = indexOf(KIT_SLOTS, slot);
                if (idx >= 0 && idx < list.size()) buyKit(player, list.get(idx));
            }
        }
    }

    private static int indexOf(int[] arr, int v) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == v) return i;
        return -1;
    }

    private boolean handleNavClick(Player player, int slot) {
        return switch (slot) {
            case 45 -> { openPrivatesTab(player); yield true; }
            case 47 -> { openTntTab(player);      yield true; }
            case 49 -> { openMinecartTab(player); yield true; }
            case 50 -> { openSpheresTab(player);  yield true; }
            case 51 -> { openBallsTab(player);    yield true; }
            case 53 -> { openKitsTab(player);     yield true; }
            default -> false;
        };
    }

    // ══ Покупки ══════════════════════════════════════════════════════════════

    private void buy(Player player, RegionTier tier, long price) {
        if (!plugin.getArisManager().take(player.getName(), price)) { noMoney(player, price); return; }
        ItemStack item = (tier == RegionTier.LEGENDARY)
            ? LegendaryItemManager.createLegendaryBlock()
            : new ItemStack(tier.getBlockMaterial(), 1);
        player.getInventory().addItem(item);
        player.closeInventory();
        bought(player, tier.getDisplayName(), price);
    }

    private void buyAris(Player player, long price) {
        if (!plugin.getArisManager().take(player.getName(), price)) { noMoney(player, price); return; }
        player.getInventory().addItem(ArisItemManager.createArisBlock());
        player.closeInventory();
        bought(player, "§6Арис", price);
    }

    private void buyTnt(Player player, TntType type) {
        if (!plugin.getArisManager().take(player.getName(), type.getPrice())) { noMoney(player, type.getPrice()); return; }
        player.getInventory().addItem(TntItemManager.createTnt(type));
        player.closeInventory();
        bought(player, type.getDisplayName(), type.getPrice());
    }

    private void buyMinecart(Player player, MinecartType type) {
        if (!plugin.getArisManager().take(player.getName(), type.getPrice())) { noMoney(player, type.getPrice()); return; }
        player.getInventory().addItem(MinecartItemManager.createMinecart(type));
        player.closeInventory();
        bought(player, type.getDisplayName(), type.getPrice());
    }

    private void buySphere(Player player, Sphere sphere, boolean isPremium) {
        if (!ArisDonateBridge.isAvailable()) { player.sendMessage(arisDonateMissingMsg()); return; }
        long price = isPremium ? ArisDonateBridge.ballAris(sphere.id())
                               : ArisDonateBridge.sphereAris(sphere.id());
        if (price <= 0) return;
        if (!plugin.getArisManager().take(player.getName(), price)) { noMoney(player, price); return; }
        ItemStack item = sphere.toItem(ArisDonateBridge.get());
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
        player.closeInventory();
        bought(player, sphere.displayName(), price);
    }

    private void buyKit(Player player, KitManager.Kit kit) {
        if (!ArisDonateBridge.isAvailable()) { player.sendMessage(arisDonateMissingMsg()); return; }
        long price = ArisDonateBridge.kitAris(kit.id);
        if (price <= 0) return;
        if (!plugin.getArisManager().take(player.getName(), price)) { noMoney(player, price); return; }
        for (ItemStack item : kit.items) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
        player.closeInventory();
        bought(player, kit.displayName, price);
    }

    private Component arisDonateMissingMsg() {
        return Component.text("✗ Плагин ArisDonate не установлен — сферы/шары/киты недоступны.")
            .color(TextColor.color(0xFF4444));
    }

    private void noMoney(Player player, long price) {
        player.sendMessage(
            Component.text("✗ Недостаточно Арисов! Нужно: ").color(TextColor.color(0xFF4444))
            .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF8000))
                .decoration(TextDecoration.BOLD, true))
        );
        player.closeInventory();
    }

    private void bought(Player player, String name, long price) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("  ✦ Куплено: ").color(TextColor.color(0x55FF55))
            .append(Component.text(name + "§r")));
        player.sendMessage(Component.text("  Списано: ").color(TextColor.color(0xAAAAAA))
            .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF8000))
                .decoration(TextDecoration.BOLD, true)));
        player.sendMessage(Component.text("  Остаток: ").color(TextColor.color(0xAAAAAA))
            .append(Component.text(fmt(bal) + " ✦").color(TextColor.color(0xFFAA33))));
        player.sendMessage(Component.text(""));
    }

    // ══ Предметы-иконки ══════════════════════════════════════════════════════

    private ItemStack shopItem(RegionTier tier, long bal, long price) {
        ItemStack item = new ItemStack(tier.getBlockMaterial());
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= price;
        int s = tier.getSize();
        meta.displayName(Component.text(tier.getDisplayName() + " §r приват")
            .color(TextColor.color(tier.getColor()))
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text(""),
            Component.text("  Размер: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(s + "×" + s + "×" + s).color(TextColor.color(0xFFCC55)).decoration(TextDecoration.ITALIC, false)),
            Component.text(""),
            Component.text("  Цена: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF8000))
                    .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)),
            Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(fmt(bal) + " ✦")
                    .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                    .decoration(TextDecoration.ITALIC, false)),
            Component.text(""),
            can ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
                : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false),
            Component.text("")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack arisShopItem(long bal, long price) {
        ItemStack item = ArisItemManager.createArisBlock();
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= price;
        meta.displayName(
            Component.text("✦ ").color(TextColor.color(0xFF4400))
            .append(Component.text("А").color(TextColor.color(0xFF5500)))
            .append(Component.text("р").color(TextColor.color(0xFF6600)))
            .append(Component.text("и").color(TextColor.color(0xFF7700)))
            .append(Component.text("с").color(TextColor.color(0xFF8800)))
            .append(Component.text(" ✦").color(TextColor.color(0xFF9900)))
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true)
        );
        meta.lore(List.of(
            Component.text(""),
            Component.text("  Легендарный артефакт").color(TextColor.color(0xFFAA33)).decoration(TextDecoration.ITALIC, false),
            Component.text("  из запретных измерений.").color(TextColor.color(0xFF8C00)).decoration(TextDecoration.ITALIC, false),
            Component.text(""),
            Component.text("  Размер: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("52×52×52").color(TextColor.color(0xFFCC55)).decoration(TextDecoration.ITALIC, false)),
            Component.text(""),
            Component.text("  Цена: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF8000))
                    .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)),
            Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(fmt(bal) + " ✦")
                    .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                    .decoration(TextDecoration.ITALIC, false)),
            Component.text(""),
            can ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
                : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false),
            Component.text("")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack tntShopItem(TntType type, long bal) {
        ItemStack item = TntItemManager.createTnt(type);
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= type.getPrice();
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        lore.add(Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(bal) + " ✦")
                .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text(""));
        lore.add(can
            ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
            : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack minecartShopItem(MinecartType type, long bal) {
        ItemStack item = MinecartItemManager.createMinecart(type);
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= type.getPrice();
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        lore.add(Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(bal) + " ✦")
                .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text(""));
        lore.add(can
            ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
            : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack rgbItem(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text(""),
            Component.text("  " + desc).color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false),
            Component.text(""),
            Component.text("  (Декоративный ингредиент)").color(TextColor.color(0x555555)).decoration(TextDecoration.ITALIC, true)
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack tabButton(Material mat, String name, String desc) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text(""),
            Component.text("  " + desc).color(TextColor.color(0x55FFFF)).decoration(TextDecoration.ITALIC, false),
            Component.text("")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack sphereShopItem(Sphere sphere, long bal) {
        long price = ArisDonateBridge.sphereAris(sphere.id());
        ItemStack item = sphere.toItem(ArisDonateBridge.get());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        boolean can = bal >= price;
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        lore.add(Component.text(""));
        lore.add(Component.text("  Цена: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF8000))
                .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(bal) + " ✦")
                .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text(""));
        lore.add(can
            ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
            : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack ballShopItem(Sphere sphere, long bal) {
        long price = ArisDonateBridge.ballAris(sphere.id());
        ItemStack item = sphere.toItem(ArisDonateBridge.get());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        boolean can = bal >= price;
        List<Component> lore = new ArrayList<>(meta.lore() != null ? meta.lore() : List.of());
        lore.add(Component.text(""));
        lore.add(Component.text("  ★ Премиум-шар").color(TextColor.color(0xFF1493))
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        lore.add(Component.text("  Цена: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF8000))
                .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(bal) + " ✦")
                .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text(""));
        lore.add(can
            ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
            : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack kitShopItem(KitManager.Kit kit, long bal) {
        long price = ArisDonateBridge.kitAris(kit.id);
        Material icon = kitIcon(kit.id);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        boolean can = bal >= price;
        meta.displayName(me.arisdonate.util.Msg.parse(kit.displayName)
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        lore.add(Component.text("  Кит «" + kit.id + "» из системы донатов.")
            .color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Получишь весь содержимый шалкер,")
            .color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  предметы, броню и сферы.")
            .color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text(""));
        lore.add(Component.text("  Цена: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(price) + " ✦").color(TextColor.color(0xFF0000))
                .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("  Баланс: ").color(TextColor.color(0x888888)).decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(bal) + " ✦")
                .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text(""));
        lore.add(can
            ? Component.text("  ► Нажмите, чтобы купить").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false)
            : Component.text("  ✗ Недостаточно Арисов").color(TextColor.color(0xFF4444)).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Material kitIcon(String id) {
        return switch (id) {
            case "spark"    -> Material.LIME_SHULKER_BOX;
            case "luna"     -> Material.LIGHT_BLUE_SHULKER_BOX;
            case "stellar"  -> Material.CYAN_SHULKER_BOX;
            case "nova"     -> Material.MAGENTA_SHULKER_BOX;
            case "comet"    -> Material.WHITE_SHULKER_BOX;
            case "galaxy"   -> Material.PURPLE_SHULKER_BOX;
            case "nebula"   -> Material.PINK_SHULKER_BOX;
            case "cosmos"   -> Material.BLUE_SHULKER_BOX;
            case "phoenix"  -> Material.RED_SHULKER_BOX;
            case "aris"     -> Material.ORANGE_SHULKER_BOX;
            case "arisplus" -> Material.YELLOW_SHULKER_BOX;
            default          -> Material.SHULKER_BOX;
        };
    }

    private ItemStack infoItem(String name, String... loreLines) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name)
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(""));
        for (String l : loreLines) {
            lore.add(Component.text("  " + l).color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.text(""));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack missingArisDonateItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§c✗ Плагин ArisDonate не загружен")
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text(""),
            Component.text("  Раздел недоступен без ArisDonate.")
                .color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false),
            Component.text("  Установи ArisDonate.jar на сервер,")
                .color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false),
            Component.text("  затем выполни /reload или перезапусти.")
                .color(TextColor.color(0xAAAAAA)).decoration(TextDecoration.ITALIC, false),
            Component.text("")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private void addNavRow(Inventory inv, TabKind active) {
        // Нижний ряд: 45..53 — стекляшки + 6 кнопок навигации.
        // Расположение: 45=стекло, 46=стекло, 47..52 = 6 кнопок, 53=стекло.
        inv.setItem(45, navButton(Material.IRON_ORE,        "§6🏠 Приваты",     active == TabKind.PRIVATES));
        inv.setItem(47, navButton(Material.TNT,             "§c💣 ТНТ",         active == TabKind.TNT));
        inv.setItem(49, navButton(Material.TNT_MINECART,    "§6🚃 Вагонетки",   active == TabKind.MINECART));
        inv.setItem(50, navButton(Material.MAGMA_CREAM,     "§d✦ Сферы",        active == TabKind.SPHERES));
        inv.setItem(51, navButton(Material.HEART_OF_THE_SEA,"§d● Шары",         active == TabKind.BALLS));
        inv.setItem(53, navButton(Material.GOLDEN_APPLE,    "§6★ Киты",         active == TabKind.KITS));
    }

    private ItemStack navButton(Material mat, String name, boolean active) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name)
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        if (active) {
            meta.lore(List.of(
                Component.text(""),
                Component.text("  Текущий раздел").color(TextColor.color(0x55FF55)).decoration(TextDecoration.ITALIC, false),
                Component.text("")
            ));
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
                org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        } else {
            meta.lore(List.of(
                Component.text(""),
                Component.text("  ► Открыть раздел").color(TextColor.color(0x55FFFF)).decoration(TextDecoration.ITALIC, false),
                Component.text("")
            ));
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack balanceDisplay(long bal) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✦ Ваш баланс ✦").color(TextColor.color(0xFFDD00))
            .decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text(""),
            Component.text("  ").decoration(TextDecoration.ITALIC, false)
                .append(Component.text(fmt(bal) + " Арисов ✦")
                    .color(TextColor.color(0xFF8000))
                    .decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false)),
            Component.text("")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv) {
        ItemStack g = borderGlass();
        for (int i = 0; i < 9; i++)  inv.setItem(i, g);
        for (int i = 45; i < 54; i++) inv.setItem(i, g);
        for (int i = 9; i < 45; i += 9)  inv.setItem(i, g);
        for (int i = 17; i < 54; i += 9) inv.setItem(i, g);
    }

    private ItemStack borderGlass() {
        ItemStack g = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = g.getItemMeta();
        m.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        g.setItemMeta(m);
        return g;
    }

    private String fmt(long n) { return String.format("%,d", n).replace(',', ' '); }
}
