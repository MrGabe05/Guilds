package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        Player player = (Player) sender;

        if (args.length == 0) {
            Lang.PLAYER_NEED.send(player);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);

        // Check if the target player is online.
        if (!target.isOnline()) {
            Lang.PLAYER_NOT_ONLINE.send(player);
            return;
        }

        Guild.getGuildByMember(target.getUniqueId()).thenAcceptAsync(targetGuild -> {
            Guild guild = Guild.getGuildByMember(player.getUniqueId()).join();

            // Check if the player already belongs to a guild.
            if (guild != null) {
                Lang.GUILD_ALREADY_HAVE.send(player);
                return;
            }

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