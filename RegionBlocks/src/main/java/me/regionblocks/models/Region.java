package me.regionblocks.models;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Region {

    private final String name;
    private final String owner;
    private final Location center;
    private final RegionTier tier;
    private final List<String> members = new ArrayList<>();

    public Region(String name, String owner, Location center, RegionTier tier) {
        this.name = name;
        this.owner = owner;
        this.center = center.clone();
        this.tier = tier;
    }

    public String getName()     { return name; }
    public String getOwner()    { return owner; }
    public Location getCenter() { return center; }
    public RegionTier getTier() { return tier; }
    public List<String> getMembers() { return members; }

    public void addMember(String nick)    { if (!members.contains(nick.toLowerCase())) members.add(nick.toLowerCase()); }
    public void removeMember(String nick) { members.remove(nick.toLowerCase()); }
    public boolean isMember(String nick)  { return members.contains(nick.toLowerCase()); }
    public boolean isOwner(String nick)   { return owner.equalsIgnoreCase(nick); }
    public boolean hasAccess(String nick) { return isOwner(nick) || isMember(nick); }

    /**
     * Куб размером size×size×size.
     * Центральный блок = угол «начала», регион простирается от center до center+(size-1) по каждой оси.
     * Иными словами: minX=cx, maxX=cx+(size-1), и аналогично Y, Z.
     * Так для size=3: cx, cx+1, cx+2 — ровно 3 блока.
     * Для size=6: cx..cx+5 — ровно 6 блоков.
     */
    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        int s = tier.getSize();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int lx = loc.getBlockX();
        int ly = loc.getBlockY();
        int lz = loc.getBlockZ();
        return lx >= cx && lx < cx + s &&
               ly >= cy && ly < cy + s &&
               lz >= cz && lz < cz + s;
    }
}
