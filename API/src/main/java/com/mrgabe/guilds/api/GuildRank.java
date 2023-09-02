package com.mrgabe.guilds.api;

import lombok.Data;

/**
 * A class representing a GuildRank object that stores information about a guild rank in the Guilds plugin.
 */

@Data
public class GuildRank {

    private final int id;

    private final String tag, displayName;

    private boolean
            broadcast,
            modifyMotd,
            kickMembers,
            inviteMembers,
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
