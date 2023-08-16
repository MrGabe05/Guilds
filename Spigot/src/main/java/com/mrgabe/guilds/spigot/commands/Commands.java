package com.mrgabe.guilds.spigot.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(args.length == 0) {

        }

        if(args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "info" -> {

                }
                case "list" -> {

                }
                case "online" -> {

                }
                case "toggle" -> {

                }
            }
        }
        return false;
    }
}
