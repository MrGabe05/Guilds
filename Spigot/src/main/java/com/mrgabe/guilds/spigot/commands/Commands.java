package com.mrgabe.guilds.spigot.commands;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if(args.length == 0) {

        }

        if(args.length == 1) {
            Guild.ofMember(player.getUniqueId()).thenAccept(guild -> {
                if(guild == null) {
                    Lang.GUILD_NOT_EXISTS.send(player);
                    return;
                }

                switch (args[0].toLowerCase()) {
                    case "info" -> {
                        Placeholders placeholders = new Placeholders();
                        placeholders.set("%id%", guild.getId());
                        placeholders.set("%tag%", guild.getTag());
                        placeholders.set("%owner%", Bukkit.getOfflinePlayer(guild.getOwner()).getName());
                        placeholders.set("%date%", guild.getDate());
                        placeholders.set("%kills%", guild.getKills());
                        placeholders.set("%points%", guild.getPoints());

                        Lang.GUILD_INFO.send(player, placeholders);
                    }
                    case "list" -> {

                    }
                    case "online" -> {

                    }
                    case "toggle" -> {

                    }
                    case "leave" -> {

                    }
                    case "disband" -> {

                    }
                    case "menu" -> {

                    }
                }
            });
        }
        return false;
    }
}
