package com.mrgabe.guilds.bungee.commands;

import com.google.common.collect.Lists;
import com.mrgabe.guilds.bungee.Guilds;
import com.mrgabe.guilds.bungee.commands.impl.*;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * A command manager class responsible for handling and executing Guilds plugin commands.
 */
public class GManager extends Command {

    private final List<GCommand> commandList = new ArrayList<>();

    /**
     * Constructs a GManager instance and registers the plugin's commands.
     *
     * @param guilds The Guilds plugin instance.
     */

    public GManager(Guilds guilds) {
        super("guilds");

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
                new CommandLeave(),
                new CommandList(),
                new CommandMute(),
                new CommandOnline(),
                new CommandPromote(),
                new CommandRename(),
                new CommandToggle(),
                new CommandTransfer(),
                new CommandUnmute()));

        guilds.getProxy().getPluginManager().registerCommand(guilds, this);
    }

    /**
     * Executes the Guilds plugin command.
     *
     * @param commandSender The command sender.
     * @param strings       The command arguments.
     */

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        GCommand gCommand = getByArgs(strings[0]);
        if (gCommand != null) {
            if(gCommand.isOnlyPlayer() && (commandSender instanceof ProxiedPlayer)) {
                Lang.PLAYER_ONLY.send(commandSender);
                return;
            }

            if(Utils.isValidString(gCommand.getPermissions()) && !commandSender.hasPermission(gCommand.getPermissions())) {
                Lang.PLAYER_NOT_PERMISSIONS.send(commandSender);
                return;
            }

            String[] args = new String[0];
            System.arraycopy(strings, 1, args, 0, strings.length);
            gCommand.onCommand(commandSender, args);
            return;
        }

        Lang.UNKNOWN_ARGS.send(commandSender);
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

