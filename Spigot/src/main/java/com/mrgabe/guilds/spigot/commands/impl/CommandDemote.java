package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDemote extends GCommand {

    public CommandDemote() {
        super("demote", "guild.command.demote");
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
            if(!guild.getRank(guildPlayer).isChangeRanks()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_DEMOTE.send(player);
                return;
            }

            GuildPlayer guildTarget = GuildPlayer.getPlayerByName(player.getName()).join();
            if(guildTarget == null) {
                Lang.PLAYER_NOT_EXISTS.send(player);
                return;
            }

            if(guildTarget.getRank() >= guildPlayer.getRank()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_DEMOTE.send(player);
                return;
            }

            guildTarget.setRank(guildTarget.getRank() - 1);
            Lang.GUILD_PLAYER_DEMOTE.send(player, new Placeholders());
        });
    }
}
