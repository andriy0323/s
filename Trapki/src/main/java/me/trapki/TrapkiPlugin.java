package me.trapki;

import me.trapki.commands.TrapCommand;
import me.trapki.listeners.TrapPlaceListener;
import me.trapki.managers.TrapItemManager;
import me.trapki.managers.TrapManager;
import me.trapki.models.TrapType;
import me.trapki.tasks.TrapTickTask;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.Map;

public class TrapkiPlugin extends JavaPlugin {

    private TrapManager trapManager;
    private final Map<TrapType, Long> prices = new EnumMap<>(TrapType.class);
    private long scrapPrice = 500L;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        TrapItemManager.init(this);

        trapManager = new TrapManager(this);
        reloadPricesFromConfig();

        getServer().getPluginManager().registerEvents(new TrapPlaceListener(this), this);
        getCommand("trap").setExecutor(new TrapCommand(this));
        // алиас /trapki также маппится через aliases в plugin.yml

        new TrapTickTask(this).runTaskTimer(this, 20L, 5L);

        getLogger().info("Trapki: загружено " + TrapType.values().length + " типов трапок");
    }

    public TrapManager getTrapManager() { return trapManager; }

    public long priceOf(TrapType type) {
        return prices.getOrDefault(type, type.defaultPrice());
    }

    public long scrapPrice() { return scrapPrice; }

    public void reloadPricesFromConfig() {
        prices.clear();
        ConfigurationSection sec = getConfig().getConfigurationSection("prices.traps");
        for (TrapType t : TrapType.values()) {
            long price = sec != null ? sec.getLong(t.id(), t.defaultPrice()) : t.defaultPrice();
            prices.put(t, price);
        }
        scrapPrice = getConfig().getLong("prices.scrap", 500L);
    }
}
