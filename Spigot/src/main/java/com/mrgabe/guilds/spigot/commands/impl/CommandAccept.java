package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Date;

public class CommandAccept extends GCommand {

    public CommandAccept() {
        super("accept", "guild.command.accept");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if(args.length == 0) {
            Lang.PLAYER_NEED.send(player);
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(!target.isOnline()) {
            Lang.PLAYER_NOT_ONLINE.send(player);
            return;
        }

        Guild.getGuildByMember(target.getUniqueId()).thenAcceptAsync(targetGuild -> {
            Guild guild = Guild.getGuildByMember(player.getUniqueId()).join();
            if(guild != null) {
                Lang.GUILD_ALREADY_HAVE.send(player);
                return;
            }

            if(targetGuild == null) {
                Lang.GUILD_NOT_EXISTS.send(player);
                return;
            }

            if(!targetGuild.getInvitations().contains(player.getUniqueId())) {
                Lang.GUILD_NOT_INVITED.send(player);
                return;
            }

            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();
            guildPlayer.setHasGuild(true);
            guildPlayer.setGuildId(guild.getId());
            guildPlayer.setRank(1);
            guildPlayer.setJoined(new Date(System.currentTimeMillis()));
            guildPlayer.savePlayer();

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", player.getName());

            guild.fetchMembers().thenAcceptAsync(members -> members.forEach(uuid -> Redis.getRedis().sendMessage(uuid, Lang.GUILD_PLAYER_JOINED.get(placeholders))));
        });
    }
}
