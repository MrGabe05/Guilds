package com.mrgabe.guilds.spigot.lang;

import com.google.common.collect.Lists;
import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import com.mrgabe.guilds.spigot.utils.Utils;
import com.mrgabe.guilds.utils.PluginLogger;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


/*
* Lang system, supports multiple languages depending on how the player has configured the language of the minecraft client.
* */
public class Lang {

    private final Map<String, List<String>> langs = new HashMap<>();

    public static Lang
            GUILD_NOT_EXISTS,
            GUILD_INFO;

    public void addLang(String lang, String... text) {
        langs.put(lang.toLowerCase(Locale.ROOT), Arrays.stream(text).map(Utils::color).collect(Collectors.toList()));
    }

    public void send(Player player) {
        send(player, new Placeholders());
    }

    public void send(Player player, Placeholders placeholders) {
        String lang = Utils.getLocale(player).split("_")[0];

        List<String> stringList = langs.getOrDefault(lang.toLowerCase(Locale.ROOT), Lists.newArrayList(this.getClass().getName() + " is not setted"));

        for(String s : stringList) {
            s = placeholders.parse(s);

            player.sendMessage(Utils.color(s));
        }
    }

    public static void loadLangs() {
        File langFolder = new File(Guilds.getInstance().getDataFolder(), "/lang/");
        if(!langFolder.exists()) langFolder.mkdirs();

        new YamlConfig(Guilds.getInstance(), "Lang_en");

        Arrays.stream(Objects.requireNonNull(langFolder.listFiles())).filter(File::isFile).filter(file -> file.getPath().endsWith(".yml")).forEach(file -> {
            YamlConfig langConfig = new YamlConfig(Guilds.getInstance(), file);

            String lang = file.getName().split("_")[1].replace(".yml", "");

            for(Field field : Lang.class.getFields()) {
                field.setAccessible(true);

                if(field.getType() == Lang.class) {
                    if(!langConfig.isSet(field.getName().toLowerCase(Locale.ROOT))) langConfig.set(field.getName().toLowerCase(Locale.ROOT), field.getName().toLowerCase(Locale.ROOT) + " value not set");

                    try {
                        if(field.get(null) == null) {
                            field.set(field, new Lang());
                        }

                        Lang obj = (Lang) field.get(null);

                        if(langConfig.isList(field.getName().toLowerCase(Locale.ROOT))) {
                            obj.addLang(lang, langConfig.getStringList(field.getName().toLowerCase(Locale.ROOT)).toArray(new String[0]));
                        } else {
                            obj.addLang(lang, langConfig.getString(field.getName().toLowerCase(Locale.ROOT)));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            langConfig.save();

            PluginLogger.info("Lang " + lang + " loaded.");
        });
    }
}
