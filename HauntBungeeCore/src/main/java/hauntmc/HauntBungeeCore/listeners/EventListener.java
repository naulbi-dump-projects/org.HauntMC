package hauntmc.HauntBungeeCore.listeners;

import hauntmc.HauntBungeeCore.data.config.MessageConfiguration;
import hauntmc.HauntBungeeCore.data.config.SettingsConfiguration;
import net.md_5.bungee.event.*;
import hauntmc.HauntBungeeCore.*;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.connection.*;
import net.md_5.bungee.api.plugin.*;
import net.md_5.bungee.api.connection.*;

public class EventListener implements Listener {

    public Main plugin;

    public EventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
        PendingConnection c = e.getConnection();
        String p = c.getName();
        String ip = c.getAddress().getHostName();

        if(c.getVersion() != 340) {
            e.setCancelled(true);
            e.setCancelReason(MessageConfiguration.VERSION_GAME.getChatMessage());
        }

        if(SettingsConfiguration.USE_MAINTANCE.isValue()) {
            e.setCancelled(true);
            e.setCancelReason();
        }
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent e) {
        InitialHandler handler = (InitialHandler) e.getConnection();
        if(!handler.getHandshake().getHost().toLowerCase().contains("hauntmc")) {
            e.setCancelled(true);
            e.setCancelReason(MessageConfiguration.HANDLER_NUMBER_VALUE.getChatMessage());
        }
    }

    @EventHandler
    public void onProxyPing(ProxyPingEvent e) {

    }

}
