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

    private String name;

    private boolean online;

    private int guildId, rank = -1;

    private Date joined = null;
    private UUID invited = null;

    public CompletableFuture<Void> savePlayer() {
        return CompletableFuture.runAsync(() -> {
            try {
                Redis.getRedis().set(this.uuid.toString(), new ObjectMapper().writeValueAsString(this));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            MySQL.getMySQL().savePlayer(this);
        });
    }

    /*
    * The player will be obtained by his UUID.
    * */
    public static CompletableFuture<GuildPlayer> getPlayerByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String data = Redis.getRedis().get(uuid.toString()).join();

            GuildPlayer guildPlayer = new GuildPlayer(uuid);
            try {
                guildPlayer = (data != null ? new ObjectMapper().readValue(data, GuildPlayer.class) : MySQL.getMySQL().getPlayerByUuid(uuid).join());
                guildPlayer.savePlayer();
            } catch (JsonProcessingException ignored) {}

            return guildPlayer;
        });
    }

    /*
    * It will get the player by name.
    * */
    public static CompletableFuture<GuildPlayer> getPlayerByName(String name) {
        return CompletableFuture.supplyAsync(() -> MySQL.getMySQL().getPlayerByName(name).join());
    }
}
