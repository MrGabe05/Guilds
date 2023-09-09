package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.GuildRank;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * The CommandUnmute class represents a command for unmuting players within a guild.
 * It allows guild members with the appropriate permissions to unmute other members.
 */
public class CommandUnmute extends GCommand {

    /**
     * Creates a new instance of the CommandUnmute class.
     */
    public CommandUnmute() {
        super("unmute", "guild.command.unmute");
    }

    /**
     * Executes the unmute command.
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
            GuildRank guildRank = guild.getRank(guildPlayer);

            if (!guildRank.isKickMembers()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_KICK.send(player);
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

            if (guildTarget.getRank() > guildPlayer.getRank()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_MUTE.send(player);
                return;
            }

            // Unmute the target player.
            guildTarget.setMuted(false);
            guildTarget.setMutedTime(0L);

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", player.getName());

            // Send a notification to the unmuted player.
            Redis.getRedis().sendNotify(guildTarget.getUuid(), Lang.PLAYER_UNMUTED.get(placeholders));
        });
    }
}
