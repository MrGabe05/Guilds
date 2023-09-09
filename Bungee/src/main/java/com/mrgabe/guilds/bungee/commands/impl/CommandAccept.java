package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Date;

/**
 * CommandAccept class represents the command to accept a guild invitation.
 */
public class CommandAccept extends GCommand {

    /**
     * Initializes a new CommandAccept instance.
     */
    public CommandAccept() {
        super("accept", "guild.command.accept");
    }

    /**
     * Executes the 'accept' command to join a guild.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            Lang.PLAYER_NEED.send(player);
            return;
        }

        GuildPlayer.getPlayerByName(args[0]).thenAcceptAsync(guildTarget -> {
            if(!guildTarget.isOnline()) {
                Lang.PLAYER_NOT_ONLINE.send(player);
                return;
            }

            Guild guild = Guild.getGuildByMember(player.getUniqueId()).join();
            // Check if the player already belongs to a guild.
            if (guild != null) {
                Lang.GUILD_ALREADY_HAVE.send(player);
                return;
            }

            Guild targetGuild = Guild.getGuildById(guildTarget.getGuildId()).join();
            // Check if the targetGuild exists.
            if (targetGuild == null) {
                Lang.GUILD_NOT_EXISTS.send(player);
                return;
            }

            // Check if the player is invited to the guild.
            if (!targetGuild.getInvitations().contains(player.getUniqueId())) {
                Lang.GUILD_NOT_INVITED.send(player);
                return;
            }

            // Update player's guild-related data.
            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();
            guildPlayer.setHasGuild(true);
            guildPlayer.setGuildId(targetGuild.getId());
            guildPlayer.setRank(1);
            guildPlayer.setJoined(new Date(System.currentTimeMillis()));
            guildPlayer.savePlayer();

            // Notify guild members of the player joining.
            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", player.getName());

            guild.fetchMembers().join().forEach(uuid -> Redis.getRedis().sendNotify(uuid, Lang.GUILD_PLAYER_JOINED.get(placeholders)));
        });
    }
}