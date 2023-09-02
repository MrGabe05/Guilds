package com.mrgabe.guilds.spigot.commands;

import com.google.common.collect.Lists;
import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.commands.impl.*;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A command manager class responsible for handling and executing Guilds plugin commands.
 */
public class GManager implements CommandExecutor {

    private final List<GCommand> commandList = new ArrayList<>();

    /**
     * Constructs a GManager instance and registers the plugin's commands.
     *
     * @param guilds The Guilds plugin instance.
     */
    public GManager(Guilds guilds) {
        guilds.getCommand("guilds").setExecutor(this);

        commandList.addAll(Lists.newArrayList(
                new CommandAccept(),
                new CommandChat(),
                new CommandCreate(),
                new CommandDemote(),
                new CommandDisband(),
                new CommandHelp(),
                new CommandInfo(),
                new CommandInvite(),
                new CommandKick(),
                new CommandLeave()));
    }

    /**
     * Executes the Guilds plugin command.
     *
     * @param commandSender The command sender.
     * @param command       The executed command.
     * @param s             The command label.
     * @param strings       The command arguments.
     * @return true if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        GCommand gCommand = getByArgs(command.getName());
        if (gCommand != null) {
            if(gCommand.isOnlyPlayer() && (commandSender instanceof Player)) {
                Lang.PLAYER_ONLY.send(commandSender);
                return true;
            }

            if(Utils.isValidString(gCommand.getPermissions()) && !commandSender.hasPermission(gCommand.getPermissions())) {
                Lang.PLAYER_NOT_PERMISSIONS.send(commandSender);
                return true;
            }

            String[] args = new String[0];
            System.arraycopy(strings, 1, args, 0, strings.length);
            gCommand.onCommand(commandSender, args);
            return true;
        }

        Lang.UNKNOWN_ARGS.send(commandSender);
        return true;
    }

    /**
     * Retrieves a GCommand instance based on the subcommand name.
     *
     * @param s The subcommand name.
     * @return The GCommand instance or null if not found.
     */
    public GCommand getByArgs(String s) {
        return commandList.stream().filter(gCommand -> gCommand.getSubcommand().equalsIgnoreCase(s)).findFirst().orElse(null);
    }
}

