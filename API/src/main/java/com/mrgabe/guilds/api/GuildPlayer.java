package com.mrgabe.guilds.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.Redis;
import lombok.Data;

import java.sql.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A class representing a GuildPlayer object that stores information about a player in the Guilds plugin.
 */
@Data
public class GuildPlayer {

    private final UUID uuid;

    private String name;

    private boolean online, hasGuild, chat, officier = false;

    private int rank = 1;
    private int guildId = -1;

    private Date joined = null;
    private UUID invited = null;

    private boolean muted = false;
    private long mutedTime = 0L;

    /**
     * Saves the player's information in both Redis and MySQL databases.
     *
     * @return A CompletableFuture representing the save operation.
     */
    public CompletableFuture<Void> savePlayer() {
        return CompletableFuture.runAsync(() -> {
            try {
                Redis.getRedis().set(this.uuid.toString(), new ObjectMapper().writeValueAsString(this));
            } catch (JsonProcessingException ignored) {}

            MySQL.getMySQL().savePlayer(this);
        });
    }

    /**
     * Retrieves a player by their UUID.
     *
     * @param uuid The UUID of the player to retrieve.
     * @return A CompletableFuture containing the GuildPlayer associated with the provided UUID.
     */
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

    /**
     * Retrieves a player by their name.
     *
     * @param name The name of the player to retrieve.
     * @return A CompletableFuture containing the GuildPlayer associated with the provided name.
     */
    public static CompletableFuture<GuildPlayer> getPlayerByName(String name) {
        return CompletableFuture.supplyAsync(() -> MySQL.getMySQL().getPlayerByName(name).join());
    }
}
