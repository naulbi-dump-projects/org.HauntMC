package hauntmc.HubCorePlugin;

import hauntmc.HubCorePlugin.utils.ServerUtils;
import hauntmc.HubCorePlugin.utils.config.DataConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.*;
import hauntmc.HubCorePlugin.listeners.*;

public class Main extends JavaPlugin {

    private DataConfig dataConfig;
    private ServerUtils serverUtils;

    @Override
    public void onLoad() {
        dataConfig = new DataConfig(this, serverUtils = new ServerUtils(this));
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new EventListener(dataConfig), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
