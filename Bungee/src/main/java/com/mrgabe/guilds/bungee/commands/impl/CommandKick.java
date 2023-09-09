package com.mrgabe.guilds.bungee.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.GuildRank;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.bungee.commands.GCommand;
import com.mrgabe.guilds.utils.Placeholders;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CommandKick extends GCommand {

    public CommandKick() {
        super("kick", "guild.command.kick");
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

            if(!guildRank.isKickMembers()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_KICK.send(player);
                return;
            }

            if(args.length == 0) {
                Lang.PLAYER_NEED.send(player);
                return;
            }

            GuildPlayer guildTarget = GuildPlayer.getPlayerByName(args[0]).join();
            if(guildTarget == null) {
                Lang.PLAYER_NOT_EXISTS.send(player);
                return;
            }

            if(guildTarget.getRank() > guildPlayer.getRank()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_KICK.send(player);
                return;
            }

            if(!guild.isMember(guildTarget.getUuid()).join()) {
                Lang.PLAYER_NOT_IN_GUILD.send(player);
                return;
            }

            this.kickPlayer(guildTarget);

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", guildTarget.getName());
            placeholders.set("%kicker%", player.getName());

            guild.fetchMembers().join().forEach(uuid -> Redis.getRedis().sendNotify(uuid, Lang.GUILD_PLAYER_KICKED.get(placeholders)));
        });
    }

    private void kickPlayer(GuildPlayer guildTarget) {
        guildTarget.setHasGuild(false);
        guildTarget.setGuildId(-1);
        guildTarget.setRank(1);
        guildTarget.setJoined(null);
        guildTarget.setInvited(null);
        guildTarget.savePlayer();
    }
}
