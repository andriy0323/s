package me.trapki.listeners;

import me.trapki.TrapkiPlugin;
import me.trapki.managers.TrapItemManager;
import me.trapki.models.TrapType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TrapPlaceListener implements Listener {

    private final TrapkiPlugin plugin;

    public TrapPlaceListener(TrapkiPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player player = e.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        TrapType type = TrapItemManager.trapTypeOf(hand);
        if (type == null) return;

        e.setCancelled(true);

        if (!player.hasPermission("trapki.place")) {
            player.sendMessage(Component.text("✗ У тебя нет права ставить трапки.")
                .color(TextColor.color(0xFF4444)));
            return;
        }

        // Должен быть netherite scrap в инвентаре.
        PlayerInventory inv = player.getInventory();
        int scrapSlot = findScrap(inv);
        if (scrapSlot < 0) {
            player.sendMessage(Component.text("✗ Нужен обломок незерита, чтобы поставить трапку.")
                .color(TextColor.color(0xFF4444)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.6f);
            return;
        }

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;
        BlockFace face = e.getBlockFace();
        if (face == null) face = BlockFace.UP;
        Location placeLoc = clicked.getRelative(face).getLocation();

        // Не ставим в воздух без под собой блока — нужна твёрдая основа.
        if (clicked.isPassable()) {
            player.sendMessage(Component.text("✗ Трапку ставить только на твёрдый блок.")
                .color(TextColor.color(0xFF4444)));
            return;
        }

        // Списать scrap и одну трапку.
        consumeOne(inv, scrapSlot);
        if (player.getGameMode() != GameMode.CREATIVE) {
            hand.setAmount(hand.getAmount() - 1);
            inv.setItemInMainHand(hand.getAmount() <= 0 ? null : hand);
        }

        plugin.getTrapManager().place(placeLoc, player, type);

        // Эффекты на постановку.
        placeLoc.getWorld().spawnParticle(Particle.ENCHANT, placeLoc.clone().add(0.5, 0.5, 0.5),
            40, 0.4, 0.2, 0.4, 0.5);
        placeLoc.getWorld().playSound(placeLoc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.4f);

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text("  ✦ Установлена ").color(TextColor.color(0x55FF55))
            .decoration(TextDecoration.ITALIC, false)
            .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(type.displayName())));
        player.sendMessage(Component.text("  Зона: ").color(TextColor.color(0xAAAAAA))
            .decoration(TextDecoration.ITALIC, false)
            .append(Component.text(type.size() + "×" + type.size() + " блоков")
                .color(TextColor.color(0xFFCC55)).decoration(TextDecoration.ITALIC, false)));
        player.sendMessage(Component.text("  Активация через 3 секунды.")
            .color(TextColor.color(0x888888))
            .decoration(TextDecoration.ITALIC, false));
        player.sendMessage(Component.empty());
    }

    private static int findScrap(PlayerInventory inv) {
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (TrapItemManager.isScrap(contents[i])) return i;
        }
        return -1;
    }

    private static void consumeOne(PlayerInventory inv, int slot) {
        ItemStack at = inv.getItem(slot);
        if (at == null) return;
        if (at.getAmount() <= 1) inv.setItem(slot, null);
        else { at.setAmount(at.getAmount() - 1); inv.setItem(slot, at); }
    }
}
