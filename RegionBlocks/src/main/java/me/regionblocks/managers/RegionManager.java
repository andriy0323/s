package me.regionblocks.managers;

import me.regionblocks.RegionBlocks;
import me.regionblocks.models.Region;
import me.regionblocks.models.RegionTier;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {

    private final RegionBlocks plugin;
    private final Map<String, Region> regions = new HashMap<>();
    private File dataFile;
    private FileConfiguration data;

    public RegionManager(RegionBlocks plugin) {
        this.plugin = plugin;
        load();
    }

    // ── создание ──────────────────────────────────────────────────────────────

    public String createRegion(String owner, Location corner, RegionTier tier) {
        String base = owner.toLowerCase() + "_" + tier.name().toLowerCase();
        String name = base;
        int i = 1;
        while (regions.containsKey(name)) name = base + "_" + (i++);

        if (overlapsAny(corner, tier.getSize())) return null;

        Region r = new Region(name, owner, corner, tier);
        regions.put(name, r);
        save();
        return name;
    }

    /**
     * Проверяет, пересекается ли новый куб (corner, size) с любым существующим регионом.
     */
    public boolean overlapsAny(Location corner, int size) {
        for (Region r : regions.values()) {
            if (!r.getCenter().getWorld().equals(corner.getWorld())) continue;
            if (overlaps(corner, size, r.getCenter(), r.getTier().getSize())) return true;
        }
        return false;
    }

    private boolean overlaps(Location a, int sA, Location b, int sB) {
        // AABB пересечение: куб A [ax, ax+sA) и куб B [bx, bx+sB)
        int ax = a.getBlockX(), ay = a.getBlockY(), az = a.getBlockZ();
        int bx = b.getBlockX(), by = b.getBlockY(), bz = b.getBlockZ();
        return ax < bx + sB && ax + sA > bx &&
               ay < by + sB && ay + sA > by &&
               az < bz + sB && az + sA > bz;
    }

    // ── поиск ─────────────────────────────────────────────────────────────────

    public Region getRegionAt(Location loc) {
        for (Region r : regions.values()) {
            if (r.contains(loc)) return r;
        }
        return null;
    }

    public Region getRegion(String name) { return regions.get(name.toLowerCase()); }

    public List<Region> getPlayerRegions(String nick) {
        List<Region> list = new ArrayList<>();
        for (Region r : regions.values()) {
            if (r.isOwner(nick)) list.add(r);
        }
        return list;
    }

    public void removeRegion(String name) {
        regions.remove(name.toLowerCase());
        save();
    }

    /** Возвращает все регионы (для перебора, например при взрыве ТНТ). */
    public java.util.Collection<Region> getAllRegions() {
        return regions.values();
    }

    // ── сохранение / загрузка ─────────────────────────────────────────────────

    private void load() {
        dataFile = new File(plugin.getDataFolder(), "regions.yml");
        dataFile.getParentFile().mkdirs();
        data = YamlConfiguration.loadConfiguration(dataFile);

        if (!data.isConfigurationSection("regions")) return;

        for (String key : data.getConfigurationSection("regions").getKeys(false)) {
            String path = "regions." + key + ".";
            String owner   = data.getString(path + "owner");
            String tierName = data.getString(path + "tier");
            String world   = data.getString(path + "world");
            double x = data.getDouble(path + "x");
            double y = data.getDouble(path + "y");
            double z = data.getDouble(path + "z");

            RegionTier tier;
            try { tier = RegionTier.valueOf(tierName); } catch (Exception e) { continue; }

            org.bukkit.World w = plugin.getServer().getWorld(world);
            if (w == null) continue;

            Region r = new Region(key, owner, new Location(w, x, y, z), tier);
            for (String m : data.getStringList(path + "members")) r.addMember(m);
            regions.put(key, r);
        }
    }

    public void save() {
        data = new YamlConfiguration();
        for (Region r : regions.values()) {
            String path = "regions." + r.getName() + ".";
            data.set(path + "owner",   r.getOwner());
            data.set(path + "tier",    r.getTier().name());
            data.set(path + "world",   r.getCenter().getWorld().getName());
            data.set(path + "x",       r.getCenter().getX());
            data.set(path + "y",       r.getCenter().getY());
            data.set(path + "z",       r.getCenter().getZ());
            data.set(path + "members", r.getMembers());
        }
        try { data.save(dataFile); }
        catch (IOException e) { plugin.getLogger().severe("Ошибка сохранения regions.yml: " + e.getMessage()); }
    }
}
