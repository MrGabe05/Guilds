package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * The CommandTransfer class represents a command for transferring guild ownership.
 * It allows the current guild owner to transfer ownership to another guild member.
 */
public class CommandTransfer extends GCommand {

    /**
     * Creates a new instance of the CommandTransfer class.
     */
    public CommandTransfer() {
        super("transfer", "guild.command.transfer");
    }

    /**
     * Executes the transfer command.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();
            if (!guild.getOwner().getUuid().equals(player.getUniqueId())) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_TRANSFER.send(player);
                return;
            }

            if (args.length == 0) {
                Lang.PLAYER_NEED.send(player);
                return;
            }

            GuildPlayer guildTarget = GuildPlayer.getPlayerByName(args[0]).join();
            if (guildTarget == null) {
                Lang.PLAYER_NOT_EXISTS.send(player);
                return;
            }

            if (!guild.isMember(guildTarget.getUuid()).join()) {
                Lang.PLAYER_NOT_IN_GUILD.send(player);
                return;
            }

            Redis.getRedis().publish("transfer-confirm", guild.getId() + ":" + player.getUniqueId().toString() + ":" + guildTarget.getUuid().toString());
        });
    }
}
