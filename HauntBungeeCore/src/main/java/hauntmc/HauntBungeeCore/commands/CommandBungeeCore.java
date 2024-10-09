package hauntmc.HauntBungeeCore.commands;

import net.md_5.bungee.api.*;
import hauntmc.HauntBungeeCore.*;
import net.md_5.bungee.api.plugin.*;
import hauntmc.HauntBungeeCore.data.config.*;
import hauntmc.HauntBungeeCore.data.config.temp.*;

public class CommandBungeeCore extends Command {

    private DataConfig dataConfig;

    public CommandBungeeCore(DataConfig dataConfig) {
        super("bungeecore");
        this.dataConfig = dataConfig;
    }

    @Override
    public void execute(CommandSender s, String[] args) {
        if(args[0].equalsIgnoreCase("maintance")) {
            if(!s.hasPermission(PermissionConfiguration.COMMAND.getPermission())) {
                sendMessageUsage(s);
                return;
            }
            if(args.length == 1) {
                dataConfig.setUseMaintance(!dataConfig.isUseMaintance());
                s.sendMessage(MessageConfiguration.MAINTANCE_VALUE.getChatMessage());
            }else if(args.length == 2) {
                if(args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("true") || args[1].equals("1")) {
                    dataConfig.setUseMaintance(true);
                }else if(args[1].equalsIgnoreCase("disable") || args[1].equalsIgnoreCase("false") || args[1].equals("0")) {
                    dataConfig.setUseMaintance(false);
                }else{
                    s.sendMessage(MessageConfiguration.MESSAGE_CORE_USAGE.getChatMessage());
                }
            }
        }
    }

    private void sendMessageUsage(CommandSender s) {
        s.sendMessage(MessageConfiguration.SERVER_USAGE_CORE.getChatMessage());
    }
}
