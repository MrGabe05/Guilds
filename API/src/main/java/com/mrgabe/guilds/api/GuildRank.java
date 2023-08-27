package com.mrgabe.guilds.api;

import lombok.Data;

/*
 * Guild Rank object class
 * Class where all the information of the guild rank.
 */

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
