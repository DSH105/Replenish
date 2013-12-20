package io.github.dsh105.replenish.config;

import io.github.dsh105.dshutils.config.YAMLConfig;
import io.github.dsh105.dshutils.config.options.Options;
import io.github.dsh105.dshutils.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class DataConfigOptions extends Options {

    public static DataConfigOptions instance;

    public DataConfigOptions(YAMLConfig config) {
        super(config);
        instance = this;
    }

    public ItemStack getStack(int id, int defMaterial, int defAmount) {
        Material mat = Material.getMaterial(this.config.getInt("drops." + id + ".material", defMaterial));
        int amount = this.config.getInt("drops." + id + ".amount", defAmount);
        String name = this.config.getString("drops." + id + ".name", "");
        String l = this.config.getString("drops." + id + ".lore", "");

        ItemStack i = new ItemStack(mat, amount);
        ItemMeta meta = i.getItemMeta();
        if (name != null && !name.equalsIgnoreCase("")) {
            meta.setDisplayName(StringUtil.replaceStringWithColours(name));
        }
        if (l != null && !l.equalsIgnoreCase("")) {
            if (l.contains(",")) {
                String[] s = l.split(",");
                ArrayList<String> list = new ArrayList<String>();
                for (String str : s) {
                    list.add(StringUtil.replaceStringWithColours(str));
                }
                meta.setLore(list);
            } else {
                meta.setLore(Arrays.asList(new String[] {StringUtil.replaceStringWithColours(l)}));
            }
        }
        i.setItemMeta(meta);
        return i;
    }

    @Override
    public void setDefaults() {}
}