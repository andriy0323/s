package me.regionblocks;

import me.regionblocks.commands.*;
import me.regionblocks.listeners.*;
import me.regionblocks.managers.ArisItemManager;
import me.regionblocks.managers.ArisManager;
import me.regionblocks.managers.LegendaryItemManager;
import me.regionblocks.managers.MinecartItemManager;
import me.regionblocks.managers.RegionManager;
import me.regionblocks.managers.TntItemManager;
import me.regionblocks.tasks.ArisAdvertiseTask;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionBlocks extends JavaPlugin {

    private RegionManager regionManager;
    private ArisManager   arisManager;
    private ShopListener  shopListener;

    @Override
    public void onEnable() {
        ArisItemManager.init(this);
        LegendaryItemManager.init(this);
        TntItemManager.init(this);
        MinecartItemManager.init(this);

        regionManager = new RegionManager(this);
        arisManager   = new ArisManager(this);
        shopListener  = new ShopListener(this);

        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this),       this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this),       this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this),       this);
        getServer().getPluginManager().registerEvents(shopListener,                       this);
        getServer().getPluginManager().registerEvents(new TntExplosionListener(this),     this);
        getServer().getPluginManager().registerEvents(new MinecartExplosionListener(this), this);

        getCommand("ps").setExecutor(new PsCommand(this));
        getCommand("rg").setExecutor(new RgCommand(this));
        getCommand("givearis").setExecutor(new GiveArisCommand());
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("aris").setExecutor(new ArisCommand(this));
        getCommand("a").setExecutor(new AdminArisCommand(this));

        new ArisAdvertiseTask().runTaskTimer(this, 18000L, 18000L);

        getLogger().info("RegionBlocks v1.0.0 включён!");
    }

    @Override
    public void onDisable() {
        if (regionManager != null) regionManager.save();
        if (arisManager   != null) arisManager.save();
        getLogger().info("RegionBlocks выключен, данные сохранены.");
    }

    public RegionManager getRegionManager() { return regionManager; }
    public ArisManager   getArisManager()   { return arisManager; }
    public ShopListener  getShopListener()  { return shopListener; }
}
