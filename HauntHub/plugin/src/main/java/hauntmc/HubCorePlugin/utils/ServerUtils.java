package hauntmc.HubCorePlugin.utils;

import org.bukkit.*;

import java.util.List;
import java.util.logging.*;
import java.util.stream.Collectors;

import hauntmc.HubCorePlugin.*;

public class ServerUtils {

    private Main plugin;

    public ServerUtils(Main plugin) {
        this.plugin = plugin;
    }

    public String s(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public List<String> s(List<String> s) {
        return s.stream().map(this::s).collect(Collectors.toList());
    }

    public void Logger(String type, String message) {
        type = type.toUpperCase();
        type = type.equals("ERROR") ? "SEVERE" : (type.equals("WARN") ? "WARNING" : type);
        plugin.getLogger().log(Level.parse(type), s(message));
    }

    public boolean notNull(Object object) {
        if(object == null) return false;
        return true;
    }

}
