package com.mrgabe.guilds.api;

import lombok.Data;

@Data
public class GuildRank {

    private final int id;

    private final String tag, displayName;

    private boolean
            broadcast,
            modifyMotd,
            kickMembers,
            guildParty,
            modifyName,
            disableChat,
            modifyTag,
            auditLog,
            officerChat,
            muteMembers,
            changeRanks,
            viewStats,
            notes = false;
}
