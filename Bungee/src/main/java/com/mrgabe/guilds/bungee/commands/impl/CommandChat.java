package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            // Check if the player is not in a guild.
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < args.length; i++) {
                if(builder.length() > 0) {
                    builder.append(" ");
                }
                builder.append(args[i]);
            }

            Redis.getRedis().sendChat(guild.getId(), false, builder.toString());
        });
    }
}

