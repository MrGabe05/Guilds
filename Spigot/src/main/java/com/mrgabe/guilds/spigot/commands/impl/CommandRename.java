package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.api.GuildRank;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The CommandRename class represents a command for renaming a guild.
 * It allows the guild owner to change the name of their guild.
 */
public class CommandRename extends GCommand {

    /**
     * Creates a new instance of the CommandRename class.
     */
    public CommandRename() {
        super("rename", "guild.command.rename");
    }

    /**
     * Executes the rename command.
     *
     * @param sender The command sender.
     * @param args   The command arguments (new guild name).
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(player.getUniqueId()).join();
            GuildRank guildRank = guild.getRank(guildPlayer);

            if (!guildRank.isModifyName()) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_RENAME.send(player);
                return;
            }

            if (args.length == 0) {
                player.sendMessage(Utils.color("You need to provide the new name for the Guild."));
                return;
            }

            String newName = args[0];
            guild.setName(newName);

            // Notify all guild members about the name change.
            guild.fetchMembers().join().forEach(uuid ->
                    Redis.getRedis().sendNotify(uuid, Lang.GUILD_NAME_UPDATED.get(
                            new Placeholders().set("%name%", newName).set("%player%", player.getName())
                    ))
            );
        });
    }
}
