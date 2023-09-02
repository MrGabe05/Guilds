package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.utils.Placeholders;
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

            this.leavePlayer(player, guild);

            Placeholders placeholders = new Placeholders();
            placeholders.set("%player%", player.getName());

            guild.fetchMembers().thenAcceptAsync(members -> members.forEach(uuid -> Redis.getRedis().sendMessage(uuid, Lang.GUILD_PLAYER_KICKED.get(placeholders))));
        });
    }

    private void leavePlayer(Player player, Guild guild) {

    }
}
