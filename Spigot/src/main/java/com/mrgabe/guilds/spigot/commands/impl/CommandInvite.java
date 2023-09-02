package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.GuildRank;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInvite extends GCommand {

    public CommandInvite() {
        super("invite", "guild.command.invite", true);
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

            if(!guildRank.isInviteMembers()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_INVITE.send(player);
                return;
            }

            if(args.length == 0) {
                Lang.PLAYER_NEED.send(player);
                return;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if(!target.isOnline()) {
                Lang.PLAYER_NOT_ONLINE.send(player);
                return;
            }

            Guild guildTarget = Guild.getGuildByMember(target.getUniqueId()).join();
            if(guildTarget != null) {
                Lang.PLAYER_HAS_GUILD.send(player);
                return;
            }

            if(guild.getInvitations().contains(target.getUniqueId())) {
                Lang.GUILD_ALREADY_INVITED.send(player);
                return;
            }

            this.sendInvite(player, target, guild);

            Lang.GUILD_PLAYER_INVITED.send(player, new Placeholders().set("%player%", player.getName()));
        });
    }

    private void sendInvite(Player from, Player to, Guild guild) {

    }
}
