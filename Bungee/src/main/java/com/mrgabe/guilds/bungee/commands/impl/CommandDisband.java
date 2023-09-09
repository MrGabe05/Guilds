package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * CommandDisband class represents the command to disband a guild.
 */
public class CommandDisband extends GCommand {

    /**
     * Initializes a new CommandDisband instance.
     */
    public CommandDisband() {
        super("disband", "guild.command.disband");
    }

    /**
     * Executes the 'disband' command to disband a guild.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            // Check if the player is in a guild.
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            // Check if the player is the guild owner.
            if (!guild.getOwner().getUuid().equals(player.getUniqueId())) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_DISBAND.send(player);
                return;
            }

            // Prompt the player with a confirmation menu for disbanding the guild.
            Redis.getRedis().publish("disband-confirm", guild.getId() + ":" + player.getUniqueId().toString());
        });
    }
}