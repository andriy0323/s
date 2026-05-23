package me.trapki.managers;

import me.trapki.managers.TrapManager.Placed;
import me.trapki.models.TrapType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

/** Применяет эффект трапки к игрокам в зоне срабатывания. */
public final class TrapEffects {

    private TrapEffects() {}

    /** Применить эффект ко всем игрокам в зоне. Владелец иммунен. */
    public static void apply(Placed placed) {
        Location center = placed.location.clone().add(0.5, 0.5, 0.5);
        TrapType type = placed.type;
        int radius = type.radius();
        UUID owner = placed.owner;

        playSpawnFx(center, type);

        for (Player p : center.getWorld().getNearbyPlayers(center, radius + 0.4, 2.0, radius + 0.4)) {
            if (p.getUniqueId().equals(owner)) continue;
            if (Math.abs(p.getLocation().getBlockX() - placed.location.getBlockX()) > radius) continue;
            if (Math.abs(p.getLocation().getBlockZ() - placed.location.getBlockZ()) > radius) continue;
            if (Math.abs(p.getLocation().getBlockY() - placed.location.getBlockY()) > 2) continue;

            applySingle(p, placed, center);
        }
    }

    private static void applySingle(Player victim, Placed placed, Location center) {
        TrapType type = placed.type;
        switch (type) {
            case SPIKE     -> victim.damage(4.0);
            case SWAMP     -> victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 6 * 20, 2));
            case POISON    -> victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, 1));
            case SMOKE     -> victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 6 * 20, 0));
            case EMBER     -> {
                victim.setFireTicks(4 * 20);
                victim.damage(2.0);
            }
            case LIGHTNING -> {
                LightningStrike strike = center.getWorld().strikeLightningEffect(center);
                victim.damage(6.0);
            }
            case BOOM      -> {
                center.getWorld().createExplosion(center, 3.0f, false, false);
                victim.damage(10.0);
            }
            case WITHER    -> victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10 * 20, 1));
            case MAGNET    -> {
                Vector pull = center.toVector().subtract(victim.getLocation().toVector())
                    .normalize().multiply(1.4).setY(0.4);
                victim.setVelocity(pull);
            }
            case CHAOS     -> {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,  8 * 20, 1));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  8 * 20, 2));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 8 * 20, 0));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON,    8 * 20, 1));
            }
        }
    }

    private static void playSpawnFx(Location center, TrapType type) {
        Sound sound = switch (type) {
            case SPIKE     -> Sound.BLOCK_IRON_TRAPDOOR_CLOSE;
            case SWAMP     -> Sound.BLOCK_SLIME_BLOCK_FALL;
            case POISON    -> Sound.ENTITY_WITCH_DRINK;
            case SMOKE     -> Sound.ENTITY_BREEZE_INHALE;
            case EMBER     -> Sound.ITEM_FIRECHARGE_USE;
            case LIGHTNING -> Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
            case BOOM      -> Sound.ENTITY_GENERIC_EXPLODE;
            case WITHER    -> Sound.ENTITY_WITHER_HURT;
            case MAGNET    -> Sound.BLOCK_BEACON_ACTIVATE;
            case CHAOS     -> Sound.ENTITY_ENDER_DRAGON_GROWL;
        };
        center.getWorld().playSound(center, sound, 1.2f, 1.0f);

        Particle particle = switch (type) {
            case SPIKE     -> Particle.CRIT;
            case SWAMP     -> Particle.ITEM_SLIME;
            case POISON    -> Particle.WITCH;
            case SMOKE     -> Particle.LARGE_SMOKE;
            case EMBER     -> Particle.FLAME;
            case LIGHTNING -> Particle.ELECTRIC_SPARK;
            case BOOM      -> Particle.EXPLOSION;
            case WITHER    -> Particle.SCULK_SOUL;
            case MAGNET    -> Particle.PORTAL;
            case CHAOS     -> Particle.DRAGON_BREATH;
        };
        center.getWorld().spawnParticle(particle, center, 80, type.radius() + 0.3, 0.5, type.radius() + 0.3, 0.05);
    }
}
