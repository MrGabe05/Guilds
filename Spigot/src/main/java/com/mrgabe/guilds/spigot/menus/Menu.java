package com.mrgabe.guilds.spigot.menus;

import com.cryptomorin.xseries.XMaterial;
import com.mrgabe.guilds.spigot.Guilds;
import com.mrgabe.guilds.spigot.config.YamlConfig;
import com.mrgabe.guilds.spigot.utils.ItemBuilder;
import com.mrgabe.guilds.spigot.utils.Placeholders;
import com.mrgabe.guilds.spigot.utils.Utils;
import com.mrgabe.guilds.utils.PluginLogger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public abstract class Menu implements Listener {

    private final YamlConfig cfg;
    private final Inventory inventory;

    private Map<Character, List<Integer>> charSlots;

    private final Map<Integer, String> actions = new HashMap<>();
    private final Map<Integer, ItemBuilder> fillItems = new HashMap<>();

    public Menu(YamlConfig cfg, Placeholders textPlaceholders) {
        this.cfg = cfg;

        String title = textPlaceholders.parse(Utils.color(cfg.getString("Title")));

        InventoryType type = InventoryType.valueOf(cfg.getString("InventoryType", "CHEST"));
        if(type == InventoryType.CHEST || type == InventoryType.PLAYER) {
            this.inventory = Bukkit.createInventory(null, 9 * cfg.getStringList("Pattern").size(), title);
        } else {
            this.inventory = Bukkit.createInventory(null, type, title);
        }

        this.loadMenu();

        Bukkit.getPluginManager().registerEvents(this, Guilds.getInstance());
    }

    public void setItem(int i, ItemStack stack) {
        this.inventory.setItem(i, stack);
    }

    public void open(Player p) {
        p.openInventory(this.inventory);
    }

    private void loadMenu() {
        Map<Character, List<Integer>> charSlots = new HashMap<>();

        List<String> pattern = cfg.getStringList("Pattern");
        for(int row = 0; row < pattern.size(); row++) {
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for (int i = 0; i < patternLine.length(); i++) {
                char ch = patternLine.charAt(i);
                if (ch != ' ') {
                    ConfigurationSection section = cfg.getConfigurationSection("Items." + ch);
                    ItemBuilder itemBuilder = getItemStack(cfg.getFile().getName(), section);
                    if (itemBuilder != null) {
                        this.fillItems.put(slot, itemBuilder);

                        if (section.isSet("Action")) {
                            this.actions.put(slot, section.getString("Action").toLowerCase());
                        }
                    }

                    if(!charSlots.containsKey(ch))
                        charSlots.put(ch, new ArrayList<>());

                    charSlots.get(ch).add(slot);

                    slot++;
                }
            }
        }

        this.charSlots = charSlots;
    }

    protected static List<Integer> getSlots(YamlConfig section, String key, Map<Character, List<Integer>> charSlots) {
        if(!section.contains(key)) return new ArrayList<>();

        List<Character> chars = new ArrayList<>();
        for(char ch : section.getString(key).toCharArray())
            chars.add(ch);

        List<Integer> slots = new ArrayList<>();

        chars.stream().filter(charSlots::containsKey).forEach(ch -> slots.addAll(charSlots.get(ch)));

        return slots.isEmpty() ? Collections.singletonList(-1) : slots;
    }

    protected static ItemBuilder getItemStack(String fileName, ConfigurationSection section) {
        return getItemStack(fileName, section, new Placeholders());
    }

    protected static ItemBuilder getItemStack(String fileName, ConfigurationSection section, Placeholders textPlaceholders) {
        if(section == null || !section.contains("Type")) return null;

        XMaterial material;

        String color = null;

        String s = section.getString("Type", "BEDROCK");
        if(s.contains(":")) {
            material = XMaterial.matchXMaterial(s.split(":")[0]).orElse(XMaterial.BEDROCK);

            color = s.split(":")[1];
        } else {
            material = XMaterial.matchXMaterial(s).orElse(XMaterial.BEDROCK);
        }

        ItemBuilder itemBuilder = new ItemBuilder(Objects.requireNonNull(material.parseItem()));
        if(color != null) itemBuilder.setColor(color);
        if(section.contains("Name")) itemBuilder.withName(textPlaceholders.parse(section.getString("Name")));
        if(section.contains("Lore")) itemBuilder.withLore(section.getStringList("Lore"), textPlaceholders);
        if(section.contains("Enchants")) {
            for(String _enchantment : section.getConfigurationSection("Enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(_enchantment);
                } catch (Exception ex) {
                    PluginLogger.info("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("Enchants." + _enchantment));
            }
        }

        if(section.contains("Flags")) {
            for(String flag : section.getStringList("Flags")) itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if(section.getBoolean("Unbreakable", false)) itemBuilder.setUnbreakable();

        return itemBuilder;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().equals(this.inventory) && event.getPlayer() instanceof Player) this.onOpen(event);
    }

    @EventHandler
    public void itemMove(InventoryMoveItemEvent event) {
        if (event.getDestination().equals(this.inventory) || event.getSource().equals(this.inventory)) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(this.inventory) && event.getCurrentItem() != null && event.getWhoClicked() instanceof Player) {
            this.onClick(event);

            event.setCancelled(true);
        }
    }

    public abstract void onOpen(InventoryOpenEvent event);

    public abstract void onClick(InventoryClickEvent event);
}
