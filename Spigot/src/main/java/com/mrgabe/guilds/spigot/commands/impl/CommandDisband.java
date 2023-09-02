package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.menus.impl.ConfirmMenu;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandDisband extends GCommand {

    public CommandDisband() {
        super("disband", "guild.command.disband");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            if(!guild.getOwner().getUuid().equals(player.getUniqueId())) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_DISBAND.send(player);
                return;
            }

            new ConfirmMenu(response -> {
                if(response) {
                    this.disband(guild);
                }
            });
        });
    }

    private void disband(Guild guild) {
        Placeholders placeholders = new Placeholders();
        placeholders.set("%owner%", guild.getOwner().getName());
        placeholders.set("%name%", guild.getName());
        placeholders.set("%tag%", guild.getTag());
        placeholders.set("%id%", guild.getId());

        guild.fetchMembers().thenAcceptAsync(members -> {
            for(UUID uuid : members) {
                GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(uuid).join();
                guildPlayer.setHasGuild(false);
                guildPlayer.setGuildId(-1);
                guildPlayer.setRank(1);
                guildPlayer.setJoined(null);
                guildPlayer.savePlayer();

                Redis.getRedis().sendMessage(uuid, Lang.GUILD_DISBAND.get(placeholders));
            }

            guild.disband();
        });
    }
}
