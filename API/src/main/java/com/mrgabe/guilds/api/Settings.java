package com.mrgabe.guilds.api;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class Settings {

    private String motd;

    public boolean chat, locked;

    private Map<String, GuildRank> ranksSettings;

    public Settings() {
        this.motd = "";
        this.chat = true;
        this.locked = true;

        this.ranksSettings = new HashMap<>();
    }
}
