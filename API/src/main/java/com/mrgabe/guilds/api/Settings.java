package com.mrgabe.guilds.api;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * A class representing a Settings object that stores information about guild settings in the Guilds plugin.
 */

@Data
public class Settings {

    /**
     * Creates a new Settings object with default values.
     */
    private String motd = "";

    private boolean chat, locked = true;

    private Set<GuildRank> ranksSettings = new HashSet<>();
}
