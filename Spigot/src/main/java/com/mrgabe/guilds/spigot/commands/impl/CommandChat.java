package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CommandChat class represents the command to send a message to the guild chat.
 */
public class CommandChat extends GCommand {

    /**
     * Initializes a new CommandChat instance.
     */
    public CommandChat() {
        super("chat", "guild.command.chat");
    }

    /**
     * Executes the 'chat' command to send a message to the guild chat.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            // Check if the player is not in a guild.
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

        });
    }
}

