package com.mrgabe.guilds.bungee.listeners;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.utils.PluginLogger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Listeners implements Listener {

    /*
    * The data of the player and his guild is loaded from the database in case it has one and it will be saved in the Redis cache.
    * */
    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Guild.getGuildByMember(player.getUniqueId()).thenAccept(guild -> PluginLogger.info("Guild #" + guild.getId() + " Loaded correctly."));
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        GuildPlayer.getPlayerByUuid(player.getUniqueId()).thenAcceptAsync(guildPlayer -> {
            guildPlayer.setName(player.getName());
            guildPlayer.savePlayer();
        });
    }
}
