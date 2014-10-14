/*
 * This file is part of Replenish.
 *
 * Replenish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Replenish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Replenish.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.replenish.config;

import com.dsh105.commodus.config.Options;
import com.dsh105.commodus.config.YAMLConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigOptions extends Options {

    public static ConfigOptions instance;

    public ConfigOptions(YAMLConfig config) {
        super(config);
        instance = this;
    }

    public ItemStack getSavedStack(String id, int defMaterial, int defAmount) {
        Material mat = Material.getMaterial(this.config.getInt("drops." + id + ".material", defMaterial));
        int amount = this.config.getInt("drops." + id + ".amount", defAmount);
        String name = this.config.getString("drops." + id + ".name", "");
        Object objectLore = this.config.get("drops." + id + ".lore");
        ArrayList<String> loreList = new ArrayList<String>();

        if (objectLore instanceof String) {
            String[] s = ((String) objectLore).split(",");
            for (String str : s) {
                loreList.add(ChatColor.translateAlternateColorCodes('&', str));
            }
        } else {
            List<String> lore = this.config.config().getStringList("drops." + id + ".lore");
            if (lore != null && !lore.isEmpty()) {
                for (String part : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', part));
                }
            }
        }

        ItemStack i = new ItemStack(mat, amount);
        ItemMeta meta = i.getItemMeta();
        if (name != null && !name.equalsIgnoreCase("")) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        if (!loreList.isEmpty()) {
            meta.setLore(loreList);
        }
        i.setItemMeta(meta);
        return i;
    }

    @Override
    public void setDefaults() {
        this.set("commandString", "replenish", "String to register the main command under");
        this.set("checkForUpdates", true, "Determines whether Replenish will connect to DBO", "to check for updates");
        this.set("autoUpdate", false, "Enables auto updating");

        this.set("wand", 280, "Wand used for selecting Replenish blocks");
        this.set("allowBlockBreak", false, "Disables breaking of blocks unless a player has the", "replenish.build permission");

        this.set("drop.chance", 100, "chance; Chance of dropping an item", "min and max quantity; Min and Max quantity for", "undefined item drops to contain");
        this.set("drop.minQuantity", 1);
        this.set("drop.maxQuantity", 3);

        this.set("effect.play", true, "play; If true, play an effect when a Replenish block is broken", "onlyMinerCanSee; If true, only plays effect to", "the miner\'s client", "permissionRestricted; Only plays effect if miner has the", "replenish.effect permission");
        this.set("effect.onlyMinerCanSee", false);
        this.set("effect.permissionRestricted", false);
        this.set("effect.type", "fire");

        this.set("drops.example.material", 264, "Example drops configuration");
        this.set("drops.example.amount", 2);
        this.set("drops.example.name", "&bSpecial Diamond");
        this.set("drops.example.lore", Arrays.asList("&3&oFancy Description", "&3&oEven has two lines!"));

        config.saveConfig();
    }
}