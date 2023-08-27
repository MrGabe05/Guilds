package com.mrgabe.guilds.bungee.listeners;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.utils.PluginLogger;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Guild.ofMember(player.getUniqueId()).thenAccept(guild -> PluginLogger.info("Guild #" + guild.getId() + " Loaded correctly."));
    }
}
