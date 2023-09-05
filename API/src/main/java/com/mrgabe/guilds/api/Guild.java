package com.mrgabe.guilds.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.Redis;
import lombok.Data;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A class representing a Guild object that stores information about a guild in the Guilds plugin.
 */
@Data
public class Guild {

    private final int id;
    private final Settings settings;

    private GuildPlayer owner;

    private String name, tag = "";

    private int kills = 0;
    private int points = 0;
    private int color = 0;
    private int maxMembers = 10;

    private Date date = new Date(System.currentTimeMillis());

    private Set<UUID> invitations, mutedPlayers = new HashSet<>();

    /**
     * Gets the rank of a player within the guild.
     *
     * @param player The GuildPlayer for which to get the rank.
     * @return The GuildRank of the player within the guild.
     */
    public GuildRank getRank(GuildPlayer player) {
        return this.settings.getRanksSettings().stream().filter(guildRank -> guildRank.getId() == player.getRank()).findFirst().orElse(null);
    }

    public GuildRank getRank(int id) {
        return this.settings.getRanksSettings().stream().filter(guildRank -> guildRank.getId() == id).findFirst().orElse(null);
    }

    /**
     * Disbands the guild by removing it from the MySQL database and deleting it from Redis.
     */
    public void disband() {
        CompletableFuture<Void> removeGuildFuture = MySQL.getMySQL().removeGuild(this.id);
        CompletableFuture<Void> deleteRedisFuture = Redis.getRedis().delete(String.valueOf(this.id));

        // Combine both futures to perform these actions concurrently.
        CompletableFuture.allOf(removeGuildFuture, deleteRedisFuture).join();
    }

    /**
     * Checks if a player with the given UUID is a member of the guild.
     *
     * @param uuid The UUID of the player to check.
     * @return CompletableFuture<Boolean> representing whether the player is a member of the guild.
     */
    public CompletableFuture<Boolean> isMember(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> fetchMembers().join().contains(uuid));
    }

    /**
     * Fetches all members that are part of the guild.
     *
     * @return A CompletableFuture containing a List of UUIDs representing guild members.
     */
    public CompletableFuture<List<UUID>> fetchMembers() {
        return CompletableFuture.supplyAsync(() -> MySQL.getMySQL().getMembersFromGuild(this).join());
    }

    /**
     * Saves the guild's information in both Redis and MySQL databases.
     *
     * @return A CompletableFuture representing the save operation.
     */
    public CompletableFuture<Void> saveGuild() {
        return CompletableFuture.runAsync(() -> {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                Redis.getRedis().set(String.valueOf(this.id), objectMapper.writeValueAsString(this));
                Redis.getRedis().set(this.name, objectMapper.writeValueAsString(this));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            MySQL.getMySQL().saveGuild(this);
        });
    }

    /**
     * Retrieves a guild by a member's key (UUID or String).
     *
     * @param key The key to identify the member (UUID or String).
     * @return A CompletableFuture containing the Guild associated with the member.
     */
    public static CompletableFuture<Guild> getGuildByMember(Object key) {
        return CompletableFuture.supplyAsync(() -> {
            GuildPlayer guildPlayer = null;
            if (key instanceof UUID) {
                guildPlayer = GuildPlayer.getPlayerByUuid((UUID) key).join();
            } else if (key instanceof String) {
                guildPlayer = GuildPlayer.getPlayerByName((String) key).join();
            }

            if (guildPlayer != null && guildPlayer.getGuildId() > 0) {
                return getGuildById(guildPlayer.getGuildId()).join();
            }
            return null;
        });
    }

    /**
     * Retrieves a guild by its ID.
     *
     * @param id The ID of the guild to retrieve.
     * @return A CompletableFuture containing the Guild associated with the provided ID.
     */
    public static CompletableFuture<Guild> getGuildById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            String guildData = Redis.getRedis().get(String.valueOf(id)).join();
            ObjectMapper objectMapper = new ObjectMapper();

            Guild guild = null;

            try {
                guild = (guildData != null ? objectMapper.readValue(guildData, Guild.class) : MySQL.getMySQL().getGuildFromId(id).join());
                if (guild != null) guild.saveGuild();
            } catch (JsonProcessingException ignored) {
            }

            return guild;
        });
    }
}

