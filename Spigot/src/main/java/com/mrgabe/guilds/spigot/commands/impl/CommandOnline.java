package com.mrgabe.guilds.spigot.commands.impl;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.spigot.commands.GCommand;
import com.mrgabe.guilds.spigot.lang.Lang;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * The CommandOnline class represents a command for displaying online guild members.
 * It lists the online members of the player's guild along with their join date and rank.
 */
public class CommandOnline extends GCommand {

    /**
     * Creates a new instance of the CommandOnline class.
     */
    public CommandOnline() {
        super("online", "guild.command.online");
    }

    /**
     * Executes the online command.
     *
     * @param sender The command sender.
     * @param args   The command arguments (not used in this command).
     */
    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        Guild.getGuildByMember(player.getUniqueId()).thenAcceptAsync(guild -> {
            if (guild == null) {
                Lang.GUILD_NOT_HAVE.send(player);
                return;
            }

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");

            for (String m : Lang.GUILD_MEMBER_ONLINE.get(new Placeholders())) {
                if (m.startsWith("<members>")) {
                    for (UUID uuid : guild.fetchMembers().join()) {
                        GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(uuid).join();
                        if (!guildPlayer.isOnline()) continue;

                        Placeholders placeholders = new Placeholders();
                        placeholders.set("<members>", "");
                        placeholders.set("%player%", guildPlayer.getName());
                        placeholders.set("%joined_at%", format.format(guildPlayer.getJoined()));
                        placeholders.set("%rank%", guild.getRank(guildPlayer).getDisplayName());

                        player.sendMessage(Utils.color(placeholders.parse(m)));
                    }
                    continue;
                }

                player.sendMessage(Utils.color(m));
            }
        });
    }
}
