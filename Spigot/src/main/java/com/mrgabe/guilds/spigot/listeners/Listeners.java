package com.mrgabe.guilds.spigot.listeners;

import com.mrgabe.guilds.api.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * A listener class for handling events related to player deaths and guilds.
 */
public class Listeners implements Listener {

    /**
     * Handles the PlayerDeathEvent when a player dies.
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Check if the killer is not null (i.e., a player killed the entity)
        if (event.getEntity().getKiller() != null) return;

        // Get the player who died
        Player player = event.getEntity();

        // Retrieve the guild of the player by their unique ID asynchronously
        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild == null) return; // If the player is not in a guild, return

            // Increment the guild's kill count by 1
            guild.setKills(guild.getKills() + 1);

            // Save the updated guild information
            guild.saveGuild();
        });
    }
}

