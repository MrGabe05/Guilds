package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.spigot.menus.impl.ConfirmMenu;
import com.mrgabe.guilds.utils.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The CommandTransfer class represents a command for transferring guild ownership.
 * It allows the current guild owner to transfer ownership to another guild member.
 */
public class CommandTransfer extends GCommand {

    /**
     * Creates a new instance of the CommandTransfer class.
     */
    public CommandTransfer() {
        super("transfer", "guild.command.transfer");
    }

    /**
     * Executes the transfer command.
     *
     * @param sender The command sender.
     * @param args   The command arguments.
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
            if (!guild.getOwner().getUuid().equals(player.getUniqueId())) {
                Lang.GUILD_NOT_PERMISSIONS_FOR_TRANSFER.send(player);
                return;
            }

            if (args.length == 0) {
                Lang.PLAYER_NEED.send(player);
                return;
            }

            GuildPlayer guildTarget = GuildPlayer.getPlayerByName(args[0]).join();
            if (guildTarget == null) {
                Lang.PLAYER_NOT_EXISTS.send(player);
                return;
            }

            if (!guild.isMember(guildTarget.getUuid()).join()) {
                Lang.PLAYER_NOT_IN_GUILD.send(player);
                return;
            }

            // Prompt the player with a confirmation menu for transferring the guild.
            new ConfirmMenu(response -> {
                if (response) {
                    // Set the ranks and transfer ownership.
                    guildPlayer.setRank(1);
                    guildTarget.setRank(10);

                    guild.setOwner(guildTarget);
                    guild.saveGuild();

                    // Notify guild members about the ownership transfer.
                    Placeholders placeholders = new Placeholders();
                    placeholders.set("%new_owner%", guildTarget.getName());
                    placeholders.set("%old_owner%", guildPlayer.getName());

                    guild.fetchMembers().join().forEach(uuid -> Redis.getRedis().sendNotify(uuid, Lang.GUILD_OWNER_TRANSFER.get(placeholders)));
                }
            });
        });
    }
}
