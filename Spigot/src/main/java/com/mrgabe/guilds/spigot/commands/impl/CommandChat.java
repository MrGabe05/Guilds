package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.spigot.commands.GCommand;
import org.bukkit.command.CommandSender;

public class CommandChat extends GCommand {

    public CommandChat() {
        super("chat", "guild.command.chat");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {

    }
}
