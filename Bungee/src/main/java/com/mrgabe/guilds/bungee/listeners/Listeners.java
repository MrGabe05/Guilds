package com.mrgabe.guilds.bungee.listeners;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.Guilds;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.PluginLogger;
import com.mrgabe.guilds.utils.Utils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
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
        GuildPlayer.getPlayerByUuid(player.getUniqueId()).thenAcceptAsync(guildPlayer -> {
            Guild guild = Guild.getGuildById(guildPlayer.getGuildId()).join();
            if(guild != null) {
                guildPlayer.setOnline(true);
                guildPlayer.setHasGuild(true);
                guildPlayer.savePlayer();

                guild.saveGuild();

                PluginLogger.info("Guild #" + guild.getId() + " loaded correctly.");
            }
        });
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
            guildPlayer.setOnline(false);
            guildPlayer.savePlayer();
        });
    }

    /**
     * Event handler for chat messages.
     *
     * @param event The ChatEvent to handle.
     */
    @EventHandler
    public void onChat(ChatEvent event) {
        // Check if the message starts with '/' (a command) or the sender is not a ProxiedPlayer
        if (event.getMessage().startsWith("/") || !(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        // Retrieve the GuildPlayer associated with the player's UUID
        GuildPlayer.getPlayerByUuid(player.getUniqueId()).thenAcceptAsync(guildPlayer -> {
            // Retrieve the Guild associated with the guild player's UUID
            Guild guild = Guild.getGuildByMember(guildPlayer.getUuid()).join();

            // If the player is not a member of a guild, return
            if (guild == null) return;

            if (guildPlayer.isChat()) {
                // Cancel the original chat message event
                event.setCancelled(true);

                // Create placeholders for message formatting
                Placeholders placeholders = new Placeholders();
                placeholders.set("%player%", player.getName());
                placeholders.set("%message%", event.getMessage());

                // Retrieve the chat format from the configuration and apply placeholders
                String format = Utils.color(placeholders.parse(Guilds.getInstance().getConfig().getString("Formats.Chat")));

                // Send the formatted chat message to the guild's chat
                Redis.getRedis().sendChat(guild.getId(), true, format);
            }
        });
    }

}

