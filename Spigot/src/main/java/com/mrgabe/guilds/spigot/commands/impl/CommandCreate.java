package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.Settings;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CommandCreate class represents the command to create a guild.
 */
public class CommandCreate extends GCommand {

    /**
     * Initializes a new CommandCreate instance.
     */
    public CommandCreate() {
        super("create", "guild.command.create");
    }

    /**
     * Executes the 'create' command to create a guild.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            // Check if the player is already in a guild.
            if (guild != null) {
                Lang.GUILD_ALREADY_HAVE.send(player);
                return;
            }

            // Calculate the ID for the new guild (you can adjust this logic as needed).
            int id = MySQL.getMySQL().getGuildDataSize() + 1;

            // Get the GuildPlayer associated with the player.
            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();

            // Create a new guild with the calculated ID, GuildPlayer, and default settings.
            Guild newGuild = new Guild(id, new Settings());
            newGuild.setName(player.getName());
            newGuild.setTag(player.getName());
            newGuild.setOwner(guildPlayer);
            newGuild.saveGuild();

            // Inform the player that the guild has been created.
            Lang.GUILD_CREATED.send(player);
        });
    }
}

