package com.mrgabe.guilds.bungee.commands.admin;

import com.mrgabe.guilds.api.Guild;
import com.mrgabe.guilds.api.GuildPlayer;
import com.mrgabe.guilds.bungee.lang.Lang;
import com.mrgabe.guilds.database.MySQL;
import com.mrgabe.guilds.database.Redis;
import com.mrgabe.guilds.utils.Placeholders;
import com.mrgabe.guilds.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.UUID;

public class AdminCommands extends Command {

    public AdminCommands() {
        super("gadmin", "gadmin.commands");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check if the sender has the necessary permission.
        if (!sender.hasPermission("gadmin.commands")) {
            Lang.PLAYER_NOT_PERMISSIONS.send(sender);
            return;
        }

        // Handle the "/gadmin guilds" command to list all guilds.
        if (args.length == 1 && args[0].equalsIgnoreCase("guilds")) {
            MySQL.getMySQL().getGuildList().thenAcceptAsync(guilds -> {
                sender.sendMessage(Utils.color("&aGuilds list"));
                sender.sendMessage(Utils.color("&a"));
                for (Guild guild : guilds) {
                    sender.sendMessage(Utils.color("&7#" + guild.getId() + " &aName: " + guild.getName() + " Owner: " + guild.getOwner().getName()));
                }
                sender.sendMessage(Utils.color("&a"));
            });
            return;
        }

        // Handle other admin commands.
        if (args.length != 2 && args.length != 3) {
            // Display usage information if the command is not recognized.
            sender.sendMessage(Utils.color("&a/gadmin guilds &8- &7Show all guilds."));
            sender.sendMessage(Utils.color("&a/gadmin deleteGuild <guildId> &8- &7requires confirmation."));
            sender.sendMessage(Utils.color("&a/gadmin renameGuild <guildId> <new name> &8- &7requires confirmation."));
            sender.sendMessage(Utils.color("&a/gadmin details <guildId> &8- &7Shows all information of the targeted guild."));
            return;
        }

        // Handle the "/gadmin deleteguild" command to delete a guild.
        if (args[0].equalsIgnoreCase("deleteguild")) {
            if (!Utils.isInt(args[1])) {
                sender.sendMessage(Utils.color("&cIt must be a number."));
                return;
            }

            int id = Integer.parseInt(args[1]);

            Guild.getGuildById(id).thenAcceptAsync(guild -> {
                if (guild == null) {
                    Lang.GUILD_NOT_EXISTS.send(sender);
                    return;
                }
                if (sender instanceof ProxiedPlayer) {
                    // Prompt the player with a confirmation menu for disbanding the guild.
                    Redis.getRedis().publish("disband-confirm", guild.getId() + ":" + ((ProxiedPlayer)sender).getUniqueId().toString());
                } else {
                    this.disband(guild);
                }

                sender.sendMessage(Utils.color("&cThis guild was successfully removed."));
            });
            return;
        }

        // Handle the "/gadmin details" command to show detailed information about a guild.
        if (args[0].equalsIgnoreCase("details")) {
            if (!Utils.isInt(args[1])) {
                sender.sendMessage(Utils.color("&cIt must be a number."));
                return;
            }

            int id = Integer.parseInt(args[1]);

            Guild.getGuildById(id).thenAcceptAsync(guild -> {
                if (guild == null) {
                    Lang.GUILD_NOT_EXISTS.send(sender);
                    return;
                }

                // Format date.
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
                String dateFormat = format.format(guild.getDate());

                // Set placeholders and send guild information message.
                Placeholders placeholders = new Placeholders();
                placeholders.set("%id%", guild.getId());
                placeholders.set("%tag%", guild.getTag());
                placeholders.set("%owner%", guild.getOwner().getName());
                placeholders.set("%date%", dateFormat);
                placeholders.set("%kills%", guild.getKills());
                placeholders.set("%points%", guild.getPoints());

                Lang.GUILD_INFO.send(sender, placeholders);
            });
            return;
        }

        return;
    }

    /**
     * Disbands the specified guild, removing all members and broadcasting a message.
     *
     * @param guild The guild to disband.
     */
    private void disband(Guild guild) {
        Placeholders placeholders = new Placeholders();
        placeholders.set("%owner%", guild.getOwner().getName());
        placeholders.set("%name%", guild.getName());
        placeholders.set("%tag%", guild.getTag());
        placeholders.set("%id%", guild.getId());

        // Fetch guild members and perform cleanup.
        guild.fetchMembers().thenAcceptAsync(members -> {
            for (UUID uuid : members) {
                GuildPlayer guildPlayer = GuildPlayer.getPlayerByUuid(uuid).join();
                guildPlayer.setHasGuild(false);
                guildPlayer.setGuildId(-1);
                guildPlayer.setRank(1);
                guildPlayer.setJoined(null);
                guildPlayer.savePlayer();

                Redis.getRedis().sendNotify(uuid, Lang.GUILD_DISBAND.get(placeholders));
            }

            // Disband the guild.
            guild.disband();
        });
    }
}
