package hauntmc.HauntBungeeCore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.logging.*;
import net.md_5.bungee.api.*;
import hauntmc.HauntBungeeCore.*;

public class ServerUtils {

    public Main plugin;

    public ServerUtils(Main plugin) {
        this.plugin = plugin;
    }

    public static String s(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void Logger(String type, String message) {
        type = type.toUpperCase();
        type = type.equals("ERROR") ? "SEVERE" : (type.equals("WARN") ? "WARNING" : type);
        plugin.getLogger().log(Level.parse(type), s(message));
    }

    public static boolean isPremium(String username) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)(new URL("https://api.mojang.com/users/profiles/minecraft/" + username)).openConnection();
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Premium-Checker");
            if (connection.getResponseCode() == 200) {
                String response = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
                if (response != null && !response.equals("null") && !response.isEmpty()) return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(connection != null) connection.disconnect();
        }
        return false;
    }


}
