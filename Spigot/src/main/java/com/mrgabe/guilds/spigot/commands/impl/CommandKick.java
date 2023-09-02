package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.GuildRank;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandKick extends GCommand {

    public CommandKick() {
        super("kick", "guild.command.kick");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if(guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();
            GuildRank guildRank = guild.getRank(guildPlayer);

            if(!guildRank.isKickMembers()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_KICK.send(player);
                return;
            }

            if(args.length == 0) {
                Lang.PLAYER_NEED.send(player);
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if(!target.hasPlayedBefore()) {
                Lang.PLAYER_NOT_EXISTS.send(player);
                return;
            }

            if(!guild.isMember(target.getUniqueId()).join()) {
                Lang.PLAYER_NOT_IN_GUILD.send(player);
                return;
            }

            GuildPlayer guildTarget = GuildPlayer.getPlayerByUuid(target.getUniqueId()).join();
            if(guildTarget.getRank() > guildPlayer.getRank()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_KICK.send(player);
                return;
            }

            this.kickPlayer(player, target, guild);

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", target.getName());
            placeholders.set("%kicker%", player.getName());

            guild.fetchMembers().thenAcceptAsync(members -> members.forEach(uuid -> Redis.getRedis().sendMessage(uuid, Lang.GUILD_PLAYER_KICKED.get(placeholders))));
        });
    }

    private void kickPlayer(Player player, OfflinePlayer target, Guild guild) {

    }
}
