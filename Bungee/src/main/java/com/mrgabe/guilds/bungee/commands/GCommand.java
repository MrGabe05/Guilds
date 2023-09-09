package com.mrgabe.guilds.bungee.commands;

import lombok.Data;
import net.md_5.bungee.api.CommandSender;

/**
 * An abstract class representing a command in the Guilds plugin.
 */
@Data
public abstract class GCommand {

    private final String subcommand;
    private final String permissions;

    private final boolean onlyPlayer;

    /**
     * Constructs a GCommand instance with a subcommand name and required permissions.
     *
     * @param subcommand  The name of the subcommand.
     * @param permissions The required permissions to execute the subcommand.
     */
    public GCommand(String subcommand, String permissions) {
        this(subcommand, permissions, false);
    }

    public GCommand(String subcommand, String permissions, boolean onlyPlayer) {
        this.subcommand = subcommand;
        this.permissions = permissions;

        this.onlyPlayer = onlyPlayer;
    }

    /**
     * Abstract method to be implemented for command execution.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    protected abstract void onCommand(CommandSender sender, String[] args);
}

