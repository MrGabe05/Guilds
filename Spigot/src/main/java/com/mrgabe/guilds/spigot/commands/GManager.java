package com.mrgabe.guilds.spigot.commands;

import com.google.common.collect.Lists;
import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.commands.impl.CommandHelp;
import com.mrgabe.guilds.spigot.commands.impl.CommandInfo;
import com.mrgabe.guilds.spigot.lang.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class GManager implements CommandExecutor {

    private final List<GCommand> commandList = new ArrayList<>();

    public GManager(Guilds guilds) {
        guilds.getCommand("guilds").setExecutor(this);

        commandList.addAll(Lists.newArrayList(
                new CommandHelp(),
                new CommandInfo()));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        GCommand gCommand = getByArgs(command.getName());
        if(gCommand != null) {
            String[] args = new String[0];
            System.arraycopy(strings, 1, args, 0, strings.length);
            gCommand.onCommand(commandSender, args);
            return true;
        }

        Lang.UNKNOWN_ARGS.send(commandSender);
        return true;
    }

    public GCommand getByArgs(String s) {
        return commandList.stream().filter(gCommand -> gCommand.getSubcommand().equalsIgnoreCase(s)).findFirst().orElse(null);
    }
}
