package com.mrgabe.guilds.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.Redis;
import lombok.Data;

import java.sql.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/*
 * Guild Player object class
 * Class where all the information of the player.
 */

@Data
public class GuildPlayer {

    private final UUID uuid;

    private boolean online;

    private int gangId, rank = -1;

    private Date joined = null;
    private UUID invited = null;

    public static CompletableFuture<GuildPlayer> of(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String data = Redis.getRedis().get(uuid.toString()).join();
            ObjectMapper objectMapper = new ObjectMapper();

            GuildPlayer guildPlayer = new GuildPlayer(uuid);
            try {
                guildPlayer = (data != null ? objectMapper.readValue(data, GuildPlayer.class) : MySQL.getMySQL().getPlayer(uuid).join());

                Redis.getRedis().set(uuid.toString(), objectMapper.writeValueAsString(guildPlayer));
            } catch (JsonProcessingException ignored) {}

            return guildPlayer;
        });
    }
}
