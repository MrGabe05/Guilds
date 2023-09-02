package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import org.bukkit.command.CommandSender;

public class CommandHelp extends GCommand {

    public CommandHelp() {
        super("help", "guilds.command.help");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Lang.GUILD_HELP.send(sender);
    }
}
