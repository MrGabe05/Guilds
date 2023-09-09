package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.GuildRank;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.TimeUnit;

/**
 * The CommandMute class represents a command for muting guild members.
 * It allows guild leaders to mute other members for a specified duration or indefinitely.
 */
public class CommandMute extends GCommand {

    /**
     * Creates a new instance of the CommandMute class.
     */
    public CommandMute() {
        super("mute", "guild.command.mute");
    }

    /**
     * Executes the mute command.
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

            long time = 0L;
            if (args.length > 1) {
                if (!Utils.isNumber(args[1])) {
                    player.sendMessage(Utils.color("Time must be a number."));
                    return;
                }

                time = TimeUnit.MINUTES.toMillis(Long.parseLong(args[1]));
            }

            guildTarget.setMuted(true);
            guildTarget.setMutedTime(time);

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", player.getName());
            placeholders.set("%time%", (time > 0 ? time + "m" : "indefinitely"));

            Redis.getRedis().sendNotify(guildTarget.getUuid(), Lang.PLAYER_MUTED.get(placeholders));
        });
    }
}
