package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import net.md_5.bungee.api.CommandSender;

public class CommandHelp extends GCommand {

    public CommandHelp() {
        super("help", "guilds.command.help");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Lang.GUILD_HELP.send(sender);
    }
}
