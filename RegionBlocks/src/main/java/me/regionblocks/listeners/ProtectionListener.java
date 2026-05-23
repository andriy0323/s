package me.regionblocks.listeners;

import me.regionblocks.RegionBlocks;
import me.regionblocks.models.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ProtectionListener implements Listener {

    private final RegionBlocks plugin;

    public ProtectionListener(RegionBlocks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("regionblocks.admin")) return;

        Region region = plugin.getRegionManager().getRegionAt(e.getBlock().getLocation());
        if (region == null) return;

        if (!region.hasAccess(player.getName())) {
            e.setCancelled(true);
            sendNoAccess(player, region);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("regionblocks.admin")) return;

        Region region = plugin.getRegionManager().getRegionAt(e.getBlock().getLocation());
        if (region == null) return;

        if (!region.hasAccess(player.getName())) {
            e.setCancelled(true);
            sendNoAccess(player, region);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        Player player = e.getPlayer();
        if (player.hasPermission("regionblocks.admin")) return;

        Region region = plugin.getRegionManager().getRegionAt(e.getClickedBlock().getLocation());
        if (region == null) return;

        if (!region.hasAccess(player.getName())) {
            e.setCancelled(true);
            sendNoAccess(player, region);
        }
    }

    private void sendNoAccess(Player player, Region region) {
        player.sendMessage(
            Component.text("✗ Это территория региона ")
                .color(TextColor.color(0xFF4444))
            .append(Component.text(region.getName())
                .color(TextColor.color(0xFFFFFF)))
            .append(Component.text(" [" + region.getTier().getDisplayName() + "§r]")
                .color(TextColor.color(region.getTier().getColor())))
        );
        player.sendMessage(
            Component.text("  Владелец: ").color(TextColor.color(0xAAAAAA))
            .append(Component.text(region.getOwner()).color(TextColor.color(0xFFCC55)))
        );
    }
}
