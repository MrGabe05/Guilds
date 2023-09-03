package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The CommandToggle class represents a command for toggling chat in a guild.
 * It allows a player to toggle their chat status within their guild.
 */
public class CommandToggle extends GCommand {

    /**
     * Creates a new instance of the CommandToggle class.
     */
    public CommandToggle() {
        super("toggle", "guild.command.toggle");
    }

    /**
     * Executes the toggle command.
     *
     * @param sender The command sender.
     * @param args   The command arguments (not used in this command).
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        GuildPlayer.getPlayerByUuid(player.getUniqueId()).thenAcceptAsync(guildPlayer -> {
            guildPlayer.setChat(!guildPlayer.isChat());

            // Send a message to the player indicating the new chat status.
            Lang.PLAYER_TOGGLE_CHAT.send(player, new Placeholders().set("%status%", guildPlayer.isChat()));
        });
    }
}
