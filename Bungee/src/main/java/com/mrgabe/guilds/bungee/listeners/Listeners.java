package com.mrgabe.guilds.bungee.listeners;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.utils.PluginLogger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * A listener class for handling events related to player joining and disconnecting in the Guilds BungeeCord plugin.
 */
public class Listeners implements Listener {

    /**
     * Called when a player joins the server.
     * Loads the player's data and guild information from the database, if applicable, and caches it in Redis.
     *
     * @param event The PostLoginEvent triggered when a player joins.
     */
    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // Retrieve the guild information for the player from the database
        Guild.getGuildByMember(player.getUniqueId()).thenAccept(guild -> PluginLogger.info("Guild #" + guild.getId() + " loaded correctly."));
    }

    /**
     * Called when a player disconnects from the server.
     * Saves the player's data, such as their name and guild information, to the database.
     *
     * @param event The PlayerDisconnectEvent triggered when a player disconnects.
     */
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // Retrieve and update the player's data, then save it to the database asynchronously
        GuildPlayer.getPlayerByUuid(player.getUniqueId()).thenAcceptAsync(guildPlayer -> {
            guildPlayer.setName(player.getName());
            guildPlayer.savePlayer();
        });
    }
}

