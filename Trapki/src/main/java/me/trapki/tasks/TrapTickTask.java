package me.trapki.tasks;

import me.trapki.TrapkiPlugin;
import me.trapki.managers.TrapEffects;
import me.trapki.managers.TrapManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Каждые 5 тиков сканирует онлайн-игроков: если кто-то стоит в зоне активной
 * трапки и НЕ её хозяин — применяем эффект и удаляем трапку.
 */
public class TrapTickTask extends BukkitRunnable {

    private final TrapkiPlugin plugin;
    private final TrapManager trapManager;
    private int cleanupTick = 0;

    public TrapTickTask(TrapkiPlugin plugin) {
        this.plugin = plugin;
        this.trapManager = plugin.getTrapManager();
    }

    @Override
    public void run() {
        if (trapManager.size() > 0) {
            Set<TrapManager.Placed> triggered = new HashSet<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.isDead()) continue;
                TrapManager.Placed placed = trapManager.trapAtPlayer(p);
                if (placed == null) continue;
                if (!placed.isArmed()) continue;
                if (p.getUniqueId().equals(placed.owner)) continue;
                triggered.add(placed);
            }
            for (TrapManager.Placed t : triggered) {
                TrapEffects.apply(t);
                trapManager.remove(t.location);
            }
        }

        cleanupTick++;
        if (cleanupTick >= 20 * 60 * 5) { // раз в 5 минут
            cleanupTick = 0;
            trapManager.cleanupOld();
        }
    }
}
