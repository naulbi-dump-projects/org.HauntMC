package hauntmc.HauntBungeeCore.data.config;

import hauntmc.HauntBungeeCore.Main;
import hauntmc.HauntBungeeCore.data.config.temp.DataConfig;
import hauntmc.HauntBungeeCore.utils.*;
import net.md_5.bungee.api.chat.TextComponent;

public enum MessageConfiguration {
    CORE_VERSION("1.0", false),
    SERVER_USAGE_CORE("&fЭтот сервер использует &6HauntBungeeCore &fот &6NaulbiMIX &fверсии &6$version&f!", true),
    MESSAGE_CORE_USAGE("&fИспользование команды: &6/bungeecore maintance <true/false> &f- Включить/Выключить", false),
    MAINTANCE_VALUE("&fРежим технических работ был установлен в значени: &6$maintance&f!", false),
    VERSION_GAME("&cДля входа используйте версию игры - 1.12.2!", false),
    HANDLER_NUMBER_VALUE("&cПопытка взлома сервера!", false);

    private String message;
    private boolean placeholders;
    private DataConfig dataConfig = Main.getPlugin().getDataConfig();

    private MessageConfiguration(String message, boolean placeholders) {
        this.message = message;
        this.placeholders = placeholders;
    }

    public String getCustomMessage() {
        return message;
    }

    public String getMessage() {
        return ServerUtils.s((isPlaceholders() ? getReplacedMessage() : getCustomMessage()));
    }

    public TextComponent getChatMessage() {
        return new TextComponent(ServerUtils.s((isPlaceholders() ? getReplacedMessage() : getCustomMessage())));
    }

    public String getReplacedMessage() {
        if(message.contains("$version")) message.replace("$version", CORE_VERSION.getCustomMessage());
        if(message.contains("$maintance")) message.replace("$maintance", dataConfig.isUseMaintance() ? "&aВключены" : "&cОтключены");
        return message;
    }

    public boolean isPlaceholders() {
        return placeholders;
    }
}
