package com.mrgabe.guilds.spigot.commands;

import lombok.Data;
import org.bukkit.command.CommandSender;

@Data
public abstract class GCommand {

    private final String subcommand;
    private final String permissions;

    public GCommand(String subcommand, String permissions) {
        this.subcommand = subcommand;
        this.permissions = permissions;
    }

    protected abstract void onCommand(CommandSender sender, String[] args);
}
