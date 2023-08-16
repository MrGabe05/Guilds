package com.mrgabe.guilds.bungee.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.Guilds;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        Guilds.getInstance().getRedis().get(player.getUniqueId().toString()).thenAcceptAsync(data -> {
            ObjectMapper objectMapper = new ObjectMapper();

            GuildPlayer guildPlayer;

            if(data != null) {
                try {
                    guildPlayer = objectMapper.readValue(data, GuildPlayer.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                guildPlayer = Guilds.getInstance().getMySQL().getPlayer(player.getUniqueId()).join();

                try {
                    Guilds.getInstance().getRedis().set(player.getUniqueId().toString(), objectMapper.writeValueAsString(guildPlayer));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            if(guildPlayer.getGangId() > 0) {
                Guilds.getInstance().getRedis().get(String.valueOf(guildPlayer.getGangId())).thenAcceptAsync(guildData -> {
                    if(guildData == null) {
                        Guild guild = Guilds.getInstance().getMySQL().getGuildFromID(guildPlayer.getGangId()).join();

                        if(guild != null) {
                            try {
                                String guildString = objectMapper.writeValueAsString(guild);

                                Guilds.getInstance().getRedis().set(player.getUniqueId().toString(), guildString);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
            }
        });
    }
}
