package hauntmc.HauntBungeeCore;

import hauntmc.HauntBungeeCore.commands.CommandBungeeCore;
import hauntmc.HauntBungeeCore.data.config.temp.DataConfig;
import hauntmc.HauntBungeeCore.listeners.EventListener;
import net.md_5.bungee.api.plugin.*;
import hauntmc.HauntBungeeCore.utils.*;

public class Main extends Plugin {

    private static Main plugin;
    public ServerUtils serverUtils;
    public DataConfig dataConfig;

    @Override
    public void onEnable() {
        plugin = this;
        serverUtils = new ServerUtils(this);
        serverUtils.Logger("info", "&aЗапуск &7плагина &eHauntBungeeCore &7версии &e1.0 &7от &6NaulbiMIX&7!");
        dataConfig = new DataConfig(false);
        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerListener(this, new EventListener(this));
        pluginManager.registerCommand(this, new CommandBungeeCore(dataConfig));
    }

    @Override
    public void onDisable() {
        serverUtils.Logger("info", "&cВыключение &7плагина &eHauntBungeeCore &7версии &e1.0 &7от &6NaulbiMIX&7!");

    }

    public static Main getPlugin() {
        return plugin;
    }

    public DataConfig getDataConfig() {
        return dataConfig;
    }
}
