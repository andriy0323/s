package me.regionblocks.listeners;

import me.arisdonate.managers.KitManager;
import me.arisdonate.models.Sphere;
import me.regionblocks.RegionBlocks;
import me.regionblocks.integration.ArisDonateBridge;
import me.regionblocks.integration.ShopHeads;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopListener implements Listener {

    private static final String TITLE_PRIVATES = "✦ Магазин — Приваты ✦";
    private static final String TITLE_TNT      = "✦ Магазин — ТНТ ✦";
    private static final String TITLE_MINECART = "✦ Магазин — Вагонетки ✦";
    private static final String TITLE_SPHERES  = "✦ Магазин — Сферы ✦";
    private static final String TITLE_BALLS    = "✦ Магазин — Шары ✦";
    private static final String TITLE_KITS     = "✦ Магазин — Киты ✦";

    // Раскладка слотов (54 slot inv = 6 рядов 9×9).
    // Ряды:  0..8 верх | 9..17 | 18..26 | 27..35 | 36..44 серединная рамка | 45..53 нав-ряд.
    private static final int[] PRIVATE_SLOTS = {19, 21, 23, 25, 30, 32};        // 6 тиров
    private static final int[] TNT_SLOTS     = {20, 22, 24, 31};                 // 4 типа (titan по центру второго ряда)
    private static final int[] MINECART_SLOTS= {20, 22, 24, 31};                 // 4 типа
    private static final int[] SPHERE_SLOTS  = {19, 20, 21, 22, 23, 24, 25, 31}; // 8 сфер
    private static final int[] BALL_SLOTS    = {20, 22, 24, 30, 32};             // 5 шаров
    private static final int[] KIT_SLOTS     = {19, 20, 21, 22, 23, 24, 25,
                                                 28, 30, 32, 34};                 // 11 китов

    private static final int SLOT_INFO    = 4;   // верх по центру — заголовочная голова
    private static final int SLOT_BALANCE = 49;  // нав-ряд центральная позиция? — нет, кладём в верх-ряд правее
    // Баланс в верхнем ряду, слот 7. Заголовочная голова — слот 4 (по центру).
    private static final int SLOT_BALANCE_TOP = 7;
    private static final int SLOT_AD_TOP      = 1;  // лево-верх: подсказка
    private static final int SLOT_INFO_BOTTOM = 40; // серединная рамка, центр: краткая инфо
    private static final int[] NAV_SLOTS = {46, 47, 48, 50, 51, 52}; // 6 кнопок (45 и 53 — стекло)

    private final RegionBlocks plugin;

    public ShopListener(RegionBlocks plugin) {
        this.plugin = plugin;
    }

    // ══ Открытие разделов ════════════════════════════════════════════════════

    public void openShop(Player player) { openPrivatesTab(player); }

    public void openSpheres(Player p) { openSpheresTab(p); }
    public void openBalls(Player p)   { openBallsTab(p); }
    public void openKits(Player p)    { openKitsTab(p); }

    public void openPrivatesTab(Player player) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        Inventory inv = openFrame(player, TITLE_PRIVATES, 0xFFAA00, TabKind.PRIVATES, bal,
            "Приваты",
            "Защити свою территорию.",
            "Чем выше тир — тем больше зона."
        );

        RegionTier[] tiers = {
            RegionTier.COMMON, RegionTier.RARE, RegionTier.EPIC,
            RegionTier.MYTHIC, RegionTier.LEGENDARY
        };
        long[] prices = {100L, 300L, 700L, 1500L, 4000L};
        for (int i = 0; i < tiers.length; i++) {
            inv.setItem(PRIVATE_SLOTS[i], tierShopItem(tiers[i], bal, prices[i]));
        }
        inv.setItem(PRIVATE_SLOTS[5], arisShopItem(bal, 10000));

        player.openInventory(inv);
    }

    public void openTntTab(Player player) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        Inventory inv = openFrame(player, TITLE_TNT, 0xFF4422, TabKind.TNT, bal,
            "ТНТ",
            "Взрывчатка для добычи.",
            "Титановое ТНТ ломает приваты."
        );

        TntType[] types = TntType.values();
        for (int i = 0; i < types.length && i < TNT_SLOTS.length; i++) {
            inv.setItem(TNT_SLOTS[i], tntShopItem(types[i], bal));
        }

        player.openInventory(inv);
    }

    public void openMinecartTab(Player player) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        Inventory inv = openFrame(player, TITLE_MINECART, 0xFF8800, TabKind.MINECART, bal,
            "Вагонетки",
            "Подвижная взрывчатка.",
            "Активируется при движении."
        );

        MinecartType[] types = MinecartType.values();
        for (int i = 0; i < types.length && i < MINECART_SLOTS.length; i++) {
            inv.setItem(MINECART_SLOTS[i], minecartShopItem(types[i], bal));
        }

        player.openInventory(inv);
    }

    public void openSpheresTab(Player player) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        Inventory inv = openFrame(player, TITLE_SPHERES, 0xFF55FF, TabKind.SPHERES, bal,
            "Сферы",
            "Магические артефакты-сферы.",
            "Носи в шлеме или второй руке."
        );

        if (!ArisDonateBridge.isAvailable()) {
            inv.setItem(22, missingArisDonateItem());
        } else {
            List<Sphere> list = ArisDonateBridge.regularSpheres();
            for (int i = 0; i < list.size() && i < SPHERE_SLOTS.length; i++) {
                inv.setItem(SPHERE_SLOTS[i], sphereShopItem(list.get(i), bal));
            }
        }

        player.openInventory(inv);
    }

    public void openBallsTab(Player player) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        Inventory inv = openFrame(player, TITLE_BALLS, 0xFF1493, TabKind.BALLS, bal,
            "Шары — премиум",
            "Эксклюзивные сферы из китов.",
            "Сильнее и драгоценнее обычных."
        );

        if (!ArisDonateBridge.isAvailable()) {
            inv.setItem(22, missingArisDonateItem());
        } else {
            List<Sphere> list = ArisDonateBridge.premiumBalls();
            for (int i = 0; i < list.size() && i < BALL_SLOTS.length; i++) {
                inv.setItem(BALL_SLOTS[i], ballShopItem(list.get(i), bal));
            }
        }

        player.openInventory(inv);
    }

    public void openKitsTab(Player player) {
        long bal = plugin.getArisManager().getBalance(player.getName());
        Inventory inv = openFrame(player, TITLE_KITS, 0xFFD700, TabKind.KITS, bal,
            "Киты — очень дорого",
            "Полный комплект сразу.",
            "Без кулдауна — это разовая покупка."
        );

        if (!ArisDonateBridge.isAvailable()) {
            inv.setItem(22, missingArisDonateItem());
        } else {
            List<KitManager.Kit> list = ArisDonateBridge.allKits();
            for (int i = 0; i < list.size() && i < KIT_SLOTS.length; i++) {
                inv.setItem(KIT_SLOTS[i], kitShopItem(list.get(i), bal));
            }
        }

        player.openInventory(inv);
    }

    /** Создаёт инвентарь, заполняет рамку, кладёт инфо/баланс/нав-ряд и возвращает. */
    private Inventory openFrame(Player player, String title, int titleColor,
                                TabKind tab, long bal,
                                String sectionName, String... sectionLore) {
        Inventory inv = Bukkit.createInventory(null, 54,
            Component.text(title).color(TextColor.color(titleColor))
                .decoration(TextDecoration.BOLD, true));

        // Цветная рамка под тему вкладки.
        ItemStack border = borderGlass(tab);
        for (int i = 0; i < 9; i++)  inv.setItem(i, border);
        for (int i = 36; i < 45; i++) inv.setItem(i, border);
        for (int row = 1; row < 4; row++) {
            inv.setItem(row * 9,     border);
            inv.setItem(row * 9 + 8, border);
        }
        // Декоративные «уголки» нав-ряда.
        inv.setItem(45, border);
        inv.setItem(49, border);
        inv.setItem(53, border);

        // Голова-заголовок (большое название раздела).
        inv.setItem(SLOT_INFO, headerHead(sectionName, sectionLore));

        // Баланс игрока — головная иконка справа сверху.
        inv.setItem(SLOT_BALANCE_TOP, balanceDisplay(bal));

        // Подсказка про /a reload и формат цен (только админу полезно, но скрыть нельзя — пусть будет всем).
        inv.setItem(SLOT_AD_TOP, hintHead());

        // Центральная инфо-голова в серединной рамке.
        inv.setItem(SLOT_INFO_BOTTOM, sectionInfoHead(tab));

        // Кнопки навигации.
        addNavRow(inv, tab);
        return inv;
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

        if (handleNavClick(player, slot)) return;

        switch (tab) {
            case PRIVATES -> {
                int idx = indexOf(PRIVATE_SLOTS, slot);
                switch (idx) {
                    case 0 -> buy(player, RegionTier.COMMON,    100);
                    case 1 -> buy(player, RegionTier.RARE,      300);
                    case 2 -> buy(player, RegionTier.EPIC,      700);
                    case 3 -> buy(player, RegionTier.MYTHIC,    1500);
                    case 4 -> buy(player, RegionTier.LEGENDARY, 4000);
                    case 5 -> buyAris(player, 10000);
                    default -> {}
                }
            }
            case TNT -> {
                int idx = indexOf(TNT_SLOTS, slot);
                if (idx >= 0 && idx < TntType.values().length) {
                    buyTnt(player, TntType.values()[idx]);
                }
            }
            case MINECART -> {
                int idx = indexOf(MINECART_SLOTS, slot);
                if (idx >= 0 && idx < MinecartType.values().length) {
                    buyMinecart(player, MinecartType.values()[idx]);
                }
            }
            case SPHERES -> {
                if (!ArisDonateBridge.isAvailable()) return;
                int idx = indexOf(SPHERE_SLOTS, slot);
                List<Sphere> list = ArisDonateBridge.regularSpheres();
                if (idx >= 0 && idx < list.size()) {
                    buySphere(player, list.get(idx), false);
                }
            }
            case BALLS -> {
                if (!ArisDonateBridge.isAvailable()) return;
                int idx = indexOf(BALL_SLOTS, slot);
                List<Sphere> list = ArisDonateBridge.premiumBalls();
                if (idx >= 0 && idx < list.size()) {
                    buySphere(player, list.get(idx), true);
                }
            }
            case KITS -> {
                if (!ArisDonateBridge.isAvailable()) return;
                int idx = indexOf(KIT_SLOTS, slot);
                List<KitManager.Kit> list = ArisDonateBridge.allKits();
                if (idx >= 0 && idx < list.size()) {
                    buyKit(player, list.get(idx));
                }
            }
        }
    }

    /** Возвращает true, если клик был по кнопке навигации (и переключил вкладку). */
    private boolean handleNavClick(Player player, int slot) {
        switch (slot) {
            case 46 -> { openPrivatesTab(player); return true; }
            case 47 -> { openTntTab(player);      return true; }
            case 48 -> { openMinecartTab(player); return true; }
            case 50 -> { openSpheresTab(player);  return true; }
            case 51 -> { openBallsTab(player);    return true; }
            case 52 -> { openKitsTab(player);     return true; }
            default -> { return false; }
        }
    }

    // ══ Покупки ═══════════════════════════════════════════════════════════════

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

    // ══ Иконки-головы =========================================================

    private ItemStack tierShopItem(RegionTier tier, long bal, long price) {
        String key = switch (tier) {
            case COMMON    -> "privates.common";
            case RARE      -> "privates.rare";
            case EPIC      -> "privates.epic";
            case MYTHIC    -> "privates.mythic";
            case LEGENDARY -> "privates.legendary";
            case ARIS      -> "privates.aris";
        };
        ItemStack item = ShopHeads.head(key);
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= price;
        int s = tier.getSize();

        meta.displayName(Component.text(stripFmt(tier.getDisplayName()) + " приват")
            .color(TextColor.color(tier.getColor()))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        lore.add(Component.text("  Зона: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(s + "×" + s + "×" + s + " блоков")
                .color(yellow()).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(priceLine(price));
        lore.add(balanceLine(bal, can));
        lore.add(Component.empty());
        lore.add(actionLine(can));
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, can);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack arisShopItem(long bal, long price) {
        ItemStack item = ShopHeads.head("privates.aris");
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= price;

        meta.displayName(Component.text("✦ Арис ✦")
            .color(TextColor.color(0xFF8000))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        lore.add(Component.text("  Легендарный артефакт").color(TextColor.color(0xFFAA33))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  из запретных измерений.").color(TextColor.color(0xFF8C00))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("  Зона: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text("52×52×52 блоков")
                .color(yellow()).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(priceLine(price));
        lore.add(balanceLine(bal, can));
        lore.add(Component.empty());
        lore.add(actionLine(can));
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, can);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack tntShopItem(TntType type, long bal) {
        String key = "tnt." + type.name().toLowerCase();
        ItemStack item = ShopHeads.head(key);
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= type.getPrice();

        meta.displayName(Component.text(stripFmt(type.getDisplayName()))
            .color(type.isBreakPrivate() ? TextColor.color(0xFF3333) : TextColor.color(0xFF8800))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        for (String line : type.getDescription().split("\n")) {
            lore.add(Component.text("  " + line).color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("  Сила взрыва: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(String.format("%.1f", type.getPower()))
                .color(yellow()).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.text("  Радиус: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(type.getRadius() + " блоков")
                .color(yellow()).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(priceLine(type.getPrice()));
        lore.add(balanceLine(bal, can));
        lore.add(Component.empty());
        lore.add(actionLine(can));
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, can);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack minecartShopItem(MinecartType type, long bal) {
        String key = "minecart." + type.name().toLowerCase();
        ItemStack item = ShopHeads.head(key);
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= type.getPrice();

        meta.displayName(Component.text(stripFmt(type.getDisplayName()))
            .color(type.isBreakAris() ? TextColor.color(0xFF3333) : TextColor.color(0xFF8800))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        for (String line : type.getDescription().split("\n")) {
            lore.add(Component.text("  " + line).color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("  Радиус: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(type.getRadius() + " блоков")
                .color(yellow()).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(priceLine(type.getPrice()));
        lore.add(balanceLine(bal, can));
        lore.add(Component.empty());
        lore.add(actionLine(can));
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, can);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack sphereShopItem(Sphere sphere, long bal) {
        long price = ArisDonateBridge.sphereAris(sphere.id());
        return sphereLikeItem("spheres." + sphere.id(), sphere, price, bal, false);
    }

    private ItemStack ballShopItem(Sphere sphere, long bal) {
        long price = ArisDonateBridge.ballAris(sphere.id());
        return sphereLikeItem("balls." + sphere.id(), sphere, price, bal, true);
    }

    /** Общий конструктор для сфер и шаров — иконка-голова + lore из Sphere. */
    private ItemStack sphereLikeItem(String headKey, Sphere sphere, long price, long bal, boolean premium) {
        ItemStack head = ShopHeads.head(headKey);
        // Берём подготовленный sphere.toItem чисто ради красивой lore, потом переносим её на голову.
        ItemStack original = sphere.toItem(ArisDonateBridge.get());
        ItemMeta srcMeta = original.getItemMeta();
        ItemMeta meta = head.getItemMeta();
        boolean can = bal >= price;

        if (srcMeta != null && srcMeta.displayName() != null) {
            meta.displayName(srcMeta.displayName());
        } else {
            meta.displayName(Component.text(sphere.displayName())
                .color(TextColor.color(0xFF55FF))
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        }

        List<Component> lore = new ArrayList<>();
        if (srcMeta != null && srcMeta.lore() != null) {
            lore.addAll(srcMeta.lore());
        }
        if (premium) {
            lore.add(Component.text("  ★ Премиум-шар ★").color(TextColor.color(0xFF1493))
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        }
        lore.add(priceLine(price));
        lore.add(balanceLine(bal, can));
        lore.add(Component.empty());
        lore.add(actionLine(can));
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, can);
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack kitShopItem(KitManager.Kit kit, long bal) {
        long price = ArisDonateBridge.kitAris(kit.id);
        ItemStack item = ShopHeads.head("kits." + kit.id);
        ItemMeta meta = item.getItemMeta();
        boolean can = bal >= price;

        meta.displayName(me.arisdonate.util.Msg.parse(kit.displayName)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));

        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        lore.add(Component.text("  Полный донат-кит.").color(TextColor.color(0xAAAAAA))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  Получишь шалкер,").color(TextColor.color(0xAAAAAA))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("  броню, оружие и сферы.").color(TextColor.color(0xAAAAAA))
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("  Предметов: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(kit.items.size() + " шт.")
                .color(yellow()).decoration(TextDecoration.ITALIC, false)));
        lore.add(Component.empty());
        lore.add(priceLine(price));
        lore.add(balanceLine(bal, can));
        lore.add(Component.empty());
        lore.add(actionLine(can));
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, can);
        item.setItemMeta(meta);
        return item;
    }

    // ══ Декоративные иконки ===================================================

    private ItemStack headerHead(String name, String[] descLines) {
        ItemStack item = ShopHeads.head("info." + name.toLowerCase());
        if (item.getType() == Material.PLAYER_HEAD) {
            // нормально, оставим как есть
        }
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✦ " + name + " ✦")
            .color(TextColor.color(0xFFD700))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        for (String l : descLines) {
            lore.add(Component.text("  " + l).color(TextColor.color(0xAAAAAA))
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, false);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack sectionInfoHead(TabKind tab) {
        String key = "info." + tab.name().toLowerCase();
        ItemStack item = ShopHeads.head(key);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("ℹ Как пользоваться")
            .color(TextColor.color(0x55AAFF))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(separator());
        switch (tab) {
            case PRIVATES -> {
                lore.add(line("Поставь блок-приват — вокруг"));
                lore.add(line("него появится защищённая зона."));
                lore.add(line("Чем дороже тир, тем больше зона."));
            }
            case TNT -> {
                lore.add(line("Поставь ТНТ, подожги или ударь."));
                lore.add(line("Титановое ТНТ ломает чужие"));
                lore.add(line("приваты — используй осторожно."));
            }
            case MINECART -> {
                lore.add(line("Поставь вагонетку на рельсы,"));
                lore.add(line("дай ей разогнаться — взрыв"));
                lore.add(line("сработает на полном ходу."));
            }
            case SPHERES -> {
                lore.add(line("Надень сферу в шлем или"));
                lore.add(line("во вторую руку — пока носишь,"));
                lore.add(line("получаешь её эффекты."));
            }
            case BALLS -> {
                lore.add(line("То же, что и сферы, но мощнее."));
                lore.add(line("Раньше выпадали только из китов."));
                lore.add(line("Теперь — здесь, за Арисы."));
            }
            case KITS -> {
                lore.add(line("Покупка кита выдаёт весь набор"));
                lore.add(line("мгновенно. Это разовая покупка —"));
                lore.add(line("кулдаун /kit не учитывается."));
            }
        }
        lore.add(separator());
        meta.lore(lore);
        applyShopFlags(meta, false);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack hintHead() {
        ItemStack item = ShopHeads.head("ariscoin");
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("ℹ Подсказка")
            .color(TextColor.color(0x55AAFF))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            separator(),
            line("Все цены — в Aris-coins (/aris)."),
            line("Получить Арисы: майнинг,"),
            line("донаты, ивенты, /a give (админ)."),
            Component.empty(),
            line("Админ может менять цены в"),
            line("plugins/RegionBlocks/config.yml"),
            line("и применять через /a reload."),
            separator()
        ));
        applyShopFlags(meta, false);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack balanceDisplay(long bal) {
        ItemStack item = ShopHeads.head("balance");
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✦ Ваш баланс ✦")
            .color(TextColor.color(0xFFDD00))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            separator(),
            Component.text("  ").decoration(TextDecoration.ITALIC, false)
                .append(Component.text(fmt(bal) + " Арисов ✦")
                    .color(TextColor.color(0xFF8000))
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false)),
            separator()
        ));
        applyShopFlags(meta, false);
        item.setItemMeta(meta);
        return item;
    }

    private void addNavRow(Inventory inv, TabKind active) {
        TabKind[] tabs = {TabKind.PRIVATES, TabKind.TNT, TabKind.MINECART,
                          TabKind.SPHERES, TabKind.BALLS, TabKind.KITS};
        String[] names = {"Приваты", "ТНТ", "Вагонетки", "Сферы", "Шары", "Киты"};
        String[] keys  = {"nav.privates", "nav.tnt", "nav.minecart",
                          "nav.spheres", "nav.balls", "nav.kits"};
        for (int i = 0; i < NAV_SLOTS.length; i++) {
            inv.setItem(NAV_SLOTS[i], navButton(keys[i], names[i], tabs[i] == active));
        }
    }

    private ItemStack navButton(String headKey, String name, boolean active) {
        ItemStack item = ShopHeads.head(headKey);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text((active ? "✦ " : "» ") + name + (active ? " ✦" : ""))
            .color(active ? TextColor.color(0x55FF55) : TextColor.color(0x55FFFF))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (active) {
            lore.add(Component.text("  Текущий раздел")
                .color(TextColor.color(0x55FF55))
                .decoration(TextDecoration.ITALIC, false));
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        } else {
            lore.add(Component.text("  ► Открыть раздел")
                .color(TextColor.color(0x55FFFF))
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack missingArisDonateItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✗ Плагин ArisDonate не загружен")
            .color(TextColor.color(0xFF4444))
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            separator(),
            line("Раздел недоступен без ArisDonate."),
            line("Установи ArisDonate.jar на сервер,"),
            line("затем выполни /reload или перезапусти."),
            separator()
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack borderGlass(TabKind tab) {
        Material mat = switch (tab) {
            case PRIVATES -> Material.ORANGE_STAINED_GLASS_PANE;
            case TNT      -> Material.RED_STAINED_GLASS_PANE;
            case MINECART -> Material.YELLOW_STAINED_GLASS_PANE;
            case SPHERES  -> Material.MAGENTA_STAINED_GLASS_PANE;
            case BALLS    -> Material.PINK_STAINED_GLASS_PANE;
            case KITS     -> Material.YELLOW_STAINED_GLASS_PANE;
        };
        ItemStack g = new ItemStack(mat);
        ItemMeta m = g.getItemMeta();
        m.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        g.setItemMeta(m);
        return g;
    }

    // ══ Утилиты =================================================================

    private void applyShopFlags(ItemMeta meta, boolean can) {
        if (can) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
    }

    private Component priceLine(long price) {
        return Component.text("  Цена: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(price) + " ✦")
                .color(TextColor.color(0xFF8000))
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
    }

    private Component balanceLine(long bal, boolean can) {
        return Component.text("  Баланс: ").color(grey())
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(fmt(bal) + " ✦")
                .color(can ? TextColor.color(0x55FF55) : TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false));
    }

    private Component actionLine(boolean can) {
        return can
            ? Component.text("  ► Нажмите, чтобы купить")
                .color(TextColor.color(0x55FF55))
                .decoration(TextDecoration.ITALIC, false)
            : Component.text("  ✗ Недостаточно Арисов")
                .color(TextColor.color(0xFF4444))
                .decoration(TextDecoration.ITALIC, false);
    }

    private Component separator() {
        return Component.text("                                ")
            .color(TextColor.color(0x444444))
            .decoration(TextDecoration.STRIKETHROUGH, true)
            .decoration(TextDecoration.ITALIC, false);
    }

    private Component line(String text) {
        return Component.text("  " + text).color(TextColor.color(0xAAAAAA))
            .decoration(TextDecoration.ITALIC, false);
    }

    private TextColor grey()   { return TextColor.color(0x888888); }
    private TextColor yellow() { return TextColor.color(0xFFCC55); }

    private static int indexOf(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) if (arr[i] == value) return i;
        return -1;
    }

    /** Снимает Bukkit §-коды для использования внутри Component-displayName. */
    private static String stripFmt(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '§' && i + 1 < s.length()) { i++; continue; }
            out.append(c);
        }
        return out.toString();
    }

    private String fmt(long n) { return String.format("%,d", n).replace(',', ' '); }
}
