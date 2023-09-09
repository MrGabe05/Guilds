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

public class CommandInvite extends GCommand {

    public CommandInvite() {
        super("invite", "guild.command.invite", true);
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;

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

            GuildPlayer target = GuildPlayer.getPlayerByName(args[0]).join();
            if(!target.isOnline()) {
                Lang.PLAYER_NOT_ONLINE.send(player);
                return;
            }

            Guild guildTarget = Guild.getGuildByMember(target.getUuid()).join();
            if(guildTarget != null) {
                Lang.PLAYER_HAS_GUILD.send(player);
                return;
            }

            if(guild.getInvitations().contains(target.getUuid())) {
                Lang.GUILD_ALREADY_INVITED.send(player);
                return;
            }

            this.sendInvite(player, target, guild);

            guild.getInvitations().add(target.getUuid());

            Lang.GUILD_PLAYER_INVITED.send(player, new Placeholders().set("%player%", player.getName()));
        });
    }

    private void sendInvite(ProxiedPlayer from, GuildPlayer to, Guild guild) {
        Placeholders placeholders = new Placeholders();
        placeholders.set("%invited_by%", from.getName());
        placeholders.set("%guild_name%", guild.getName());
        placeholders.set("%guild_tag%", guild.getTag());

        Redis.getRedis().sendNotify(to.getUuid(), Lang.PLAYER_INVITATION.get(placeholders));
    }
}
