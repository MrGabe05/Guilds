package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.Settings;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCreate extends GCommand {

    public CommandCreate() {
        super("create", "guild.command.create");
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild != null) {
                Lang.GUILD_ALREADY_HAVE.send(player);
                return;
            }
            int id = MySQL.getMySQL().getGuildDataSize() + 1;

            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();

            new Guild(id, guildPlayer, new Settings()).saveGuild();

            Lang.GUILD_CREATED.send(player);
        });
    }
}
