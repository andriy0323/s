package me.arisdonate.listeners;

import me.arisdonate.ArisDonatePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private final ArisDonatePlugin plugin;

    public PlayerJoinListener(ArisDonatePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getChatFormatter().applyTabPrefix(p);
        plugin.getVanishManager().refreshFor(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getBackManager().store(e.getPlayer());
    }
}
