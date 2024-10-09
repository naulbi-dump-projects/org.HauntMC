package hauntmc.HubCorePlugin.utils.config;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.*;
import hauntmc.HubCorePlugin.*;
import hauntmc.HubCorePlugin.utils.*;
import hauntmc.HubCorePlugin.utils.custom.EventListener;
import org.bukkit.configuration.file.*;
import hauntmc.HubCorePlugin.utils.config.manager.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

public class DataConfig extends ConfigManager {

    public Location spawn;
    public ServerUtils serverUtils;
    public List<String> joinEffects;
    public FileConfiguration configuration;
    public String serverName, spawnWorld, joinMessagePlayedBefore, joinMessageFirstPlay, joinTitlePlayedBefore, joinSubtitlePlayedBefore, joinTitleFirstPlay, joinSubtitleFirstPlay;

    public DataConfig(Main plugin, ServerUtils serverUtils) {
        super(plugin);
        this.serverUtils = serverUtils;
        this.configuration = getConfiguration();
        this.spawnWorld = getString("settings.spawn.world", "hub");
        this.serverName = getString("messages.serverName", "Хаб-1", false, false);
        this.joinMessageFirstPlay = getString("messages.onJoin.chat.firstPlay", "&7&l<&3&l!7&l> &7Добро пожаловать на сервер &3HauntMC Network&7!", true, true);
        this.joinMessagePlayedBefore = getString("messages.onJoin.chat.playedBefore", "&7&l<&3&l!7&l> &7С возвращением на сервер &3HauntMC Network&7!", true, true);
        this.joinTitleFirstPlay = getString("messages.onJoin.titles.firstPlay.title", "&3HauntMC Network", true, false);
        this.joinSubtitleFirstPlay = getString("messages.onJoin.titles.firstPlay.subtitle", "&7Добро пожаловать, &3$player&7!", true, false);
        this.joinTitlePlayedBefore = getString("messages.onJoin.titles.playedBefore.title", "&3HauntMC Network", true, false);
        this.joinSubtitlePlayedBefore = getString("messages.onJoin.titles.playedBefore.subtitle", "&7С возвращением, &3$player&7!", true, false);
        this.joinEffects = getStringList("messages.onJoin.effects", Arrays.asList("speed:2", "jump:2"));
        Server server = Bukkit.getServer();
        if(server.getWorld(spawnWorld) == null) {
            serverUtils.Logger("error", (serverUtils.notNull(spawnWorld) ? "&7Кодеон мир сделал &3невидимым" : ("&7Кодеон съел мир с названием &3" + spawnWorld)));
            spawnWorld = Bukkit.getWorlds().get(0).getName();
            new EventListener<>(plugin, PlayerJoinEvent.class, EventPriority.HIGHEST, (e) -> plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                e.getPlayer().sendMessage("&3Кодеон мир уебал, чек логи");
            }));
        }else{
            this.spawn = new Location(server.getWorld(spawnWorld),
                    configuration.getDouble("spawn.x", 0.5),
                    configuration.getDouble("spawn.y", 61),
                    configuration.getDouble("spawn.z", 0.5),
                    (float) configuration.getDouble("spawn.yaw", 90),
                    (float) configuration.getDouble("spawn.pitch", 0)
            );
        }
    }

    public String getString(String path, String defaultValue) {
        String string = configuration.getString(path);
        if(string == null) {
            configuration.set(path, defaultValue);
            return defaultValue;
        }
        return string;
    }

    public String getString(String path, String defaultValue, boolean usageServerName, boolean usageNewString) {
        String string = getString(path, defaultValue);
        if(usageServerName) string.replace("$serverName", serverName);
        if(usageNewString) string.replace("$new", String.valueOf("\n")); // construction String.valueOf usage fixed message replacer in chat
        return serverUtils.s(string);
    }

    public List<String> getStringList(String path, List<String> defaultValue) {
        List<String> list = configuration.getStringList(path);
        if(list == null || list.isEmpty()) {
            configuration.set(path, defaultValue);
            return serverUtils.s(defaultValue);
        }
        return serverUtils.s(list);
    }

}
