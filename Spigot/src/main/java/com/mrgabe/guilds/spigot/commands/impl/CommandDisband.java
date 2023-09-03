package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.menus.impl.ConfirmMenu;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * CommandDisband class represents the command to disband a guild.
 */
public class CommandDisband extends GCommand {

    /**
     * Initializes a new CommandDisband instance.
     */
    public CommandDisband() {
        super("disband", "guild.command.disband");
    }

    /**
     * Executes the 'disband' command to disband a guild.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            // Check if the player is in a guild.
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            // Check if the player is the guild owner.
            if (!guild.getOwner().getUuid().equals(player.getUniqueId())) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_DISBAND.send(player);
                return;
            }

            // Prompt the player with a confirmation menu for disbanding the guild.
            new ConfirmMenu(response -> {
                if (response) {
                    this.disband(guild);
                }
            });
        });
    }

    /**
     * Disbands the specified guild, removing all members and broadcasting a message.
     *
     * @param guild The guild to disband.
     */
    private void disband(Guild guild) {
        Placeholders placeholders = new Placeholders();
        placeholders.set("%owner%", guild.getOwner().getName());
        placeholders.set("%name%", guild.getName());
        placeholders.set("%tag%", guild.getTag());
        placeholders.set("%id%", guild.getId());

        // Fetch guild members and perform cleanup.
        guild.fetchMembers().thenAcceptAsync(members -> {
            for (UUID uuid : members) {
                GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(uuid).join();
                guildPlayer.setHasGuild(false);
                guildPlayer.setGuildId(-1);
                guildPlayer.setRank(1);
                guildPlayer.setJoined(null);
                guildPlayer.savePlayer();

                Redis.getRedis().sendNotify(uuid, Lang.GUILD_DISBAND.get(placeholders));
            }

            // Disband the guild.
            guild.disband();
        });
    }
}