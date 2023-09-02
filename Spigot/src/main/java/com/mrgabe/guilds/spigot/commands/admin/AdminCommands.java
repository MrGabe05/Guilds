package com.mrgabe.guilds.spigot.commands.admin;

import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminCommands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!sender.hasPermission("gadmin.commands")) {
            Lang.PLAYER_NOT_PERMISSIONS.send(sender);
            return true;
        }

        if(args.length != 2) {
            sender.sendMessage(Utils.color(
                    "/gadmin deleteGuild <guildName> - requires confirmation.\n" +
                    "/gadmin renameGuild <guildName> - requires confirmation.\n" +
                    "/gadmin details <guildName> - Shows all information of the targeted guild."));
            return true;
        }

        return false;
    }
}
