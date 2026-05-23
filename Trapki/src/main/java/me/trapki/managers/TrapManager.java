package me.trapki.managers;

import me.trapki.TrapkiPlugin;
import me.trapki.models.TrapType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранит активные трапки на сервере. In-memory: при перезапуске сервера
 * незарядившиеся трапки забываются (предотвращает дюпы). Для долгого хранения
 * нужно было бы сохранять в файл, но для трапок-однострелов это излишне.
 */
public final class TrapManager {

    public static final class Placed {
        public final Location location;
        public final UUID owner;
        public final TrapType type;
        public final long placedAtMillis;
        public final long armedAtMillis;  // когда трапка становится «боевой»

        public Placed(Location location, UUID owner, TrapType type, long placedAtMillis, long armedAtMillis) {
            this.location = location;
            this.owner = owner;
            this.type = type;
            this.placedAtMillis = placedAtMillis;
            this.armedAtMillis = armedAtMillis;
        }

        public boolean isArmed() {
            return System.currentTimeMillis() >= armedAtMillis;
        }
    }

    /** Задержка до активации трапки (мс), чтобы хозяин успел отойти. */
    public static final long ARM_DELAY_MILLIS = 3_000L;

    private final TrapkiPlugin plugin;
    /** Ключ — округлённая локация блока, значение — трапка. */
    private final Map<Location, Placed> active = new ConcurrentHashMap<>();

    public TrapManager(TrapkiPlugin plugin) {
        this.plugin = plugin;
    }

    public Placed place(Location loc, Player owner, TrapType type) {
        Location key = blockKey(loc);
        long now = System.currentTimeMillis();
        Placed p = new Placed(key, owner.getUniqueId(), type, now, now + ARM_DELAY_MILLIS);
        active.put(key, p);
        return p;
    }

    public Placed get(Location loc) {
        return active.get(blockKey(loc));
    }

    public boolean remove(Location loc) {
        return active.remove(blockKey(loc)) != null;
    }

    public Map<Location, Placed> snapshot() {
        return new HashMap<>(active);
    }

    /** Ищет трапку, в зоне действия которой стоит игрок. Возвращает null если ничего не нашлось. */
    public Placed trapAtPlayer(Player player) {
        Location pLoc = player.getLocation();
        // быстрый перебор по квадрату 5×5 вокруг ног игрока (макс. размер трапки — 5×5)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    Location key = blockKey(pLoc.clone().add(dx, dy, dz));
                    Placed p = active.get(key);
                    if (p == null) continue;
                    int r = p.type.radius();
                    if (Math.abs(dx) <= r && Math.abs(dz) <= r && Math.abs(dy) <= 1) {
                        return p;
                    }
                }
            }
        }
        return null;
    }

    /** Удаляет трапки, которые больше 24 часов в памяти и не сработали. */
    public void cleanupOld() {
        long cutoff = System.currentTimeMillis() - 24L * 3600 * 1000;
        Iterator<Map.Entry<Location, Placed>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Location, Placed> e = it.next();
            if (e.getValue().placedAtMillis < cutoff) it.remove();
        }
    }

    public int size() { return active.size(); }

    private static Location blockKey(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
