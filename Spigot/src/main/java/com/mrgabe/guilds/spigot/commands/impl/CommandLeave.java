package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandLeave extends GCommand {

    public CommandLeave() {
        super("leave", "guild.command.leave");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if(guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            this.leavePlayer(player);

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", player.getName());

            guild.fetchMembers().join().forEach(uuid -> Redis.getRedis().sendNotify(uuid, Lang.GUILD_PLAYER_LEAVE.get(placeholders)));
        });
    }

    private void leavePlayer(Player player) {
        GuildPlayer.getPlayerByUuid(player.getUniqueId()).thenAcceptAsync(guildPlayer -> {
            guildPlayer.setHasGuild(false);
            guildPlayer.setGuildId(-1);
            guildPlayer.setRank(1);
            guildPlayer.setJoined(null);
            guildPlayer.setInvited(null);
            guildPlayer.savePlayer();
        });
    }
}
