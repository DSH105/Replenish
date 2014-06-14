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

package com.dsh105.replenish;

import com.dsh105.commodus.config.YAMLConfig;
import com.dsh105.commodus.config.YAMLConfigManager;
import com.dsh105.commodus.data.Metrics;
import com.dsh105.commodus.data.Updater;
import com.dsh105.commodus.logging.Log;
import com.dsh105.replenish.commands.ReplenishCommand;
import com.dsh105.replenish.commands.util.CommandManager;
import com.dsh105.replenish.commands.util.DynamicPluginCommand;
import com.dsh105.replenish.config.ConfigOptions;
import com.dsh105.replenish.listeners.BlockListener;
import com.dsh105.replenish.util.InfoStorage;
import com.dsh105.replenish.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class ReplenishPlugin extends JavaPlugin {

    private static ReplenishPlugin INSTANCE;
    public static Log LOG;

    private YAMLConfigManager configManager;
    private YAMLConfig config;
    private YAMLConfig dataConfig;
    private YAMLConfig langConfig;
    private ConfigOptions options;

    // Update data
    public boolean update = false;
    public String name = "";
    public long size = 0;
    public boolean updateChecked = false;

    public ChatColor primaryColour = ChatColor.GREEN;
    public ChatColor secondaryColour = ChatColor.YELLOW;
    public String prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Replenish" + ChatColor.GOLD + "] " + ChatColor.RESET;
    public String cmdString = "replenish";

    public HashMap<String, InfoStorage> infoStorage = new HashMap<String, InfoStorage>();
    private CommandManager COMMAND_MANAGER;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOG = new Log("Replenish");

        COMMAND_MANAGER = new CommandManager(this);

        config = configManager.getNewConfig("config.yml", new String[]{"Replenish By DSH105", "---------------------", "Plugin Requested By Fire_Feather", "---------------------",});
        config.reloadConfig();

        ChatColor colour1 = ChatColor.getByChar(this.getConfig(ConfigType.MAIN).getString("primaryChatColour", "3"));
        if (colour1 != null) {
            this.primaryColour = colour1;
        }
        ChatColor colour2 = ChatColor.getByChar(this.getConfig(ConfigType.MAIN).getString("secondaryChatColour", "b"));
        if (colour2 != null) {
            this.secondaryColour = colour2;
        }

        dataConfig = configManager.getNewConfig("data.yml");
        dataConfig.reloadConfig();

        options = new ConfigOptions(config);

        langConfig = configManager.getNewConfig("lang.yml", new String[]{"Replenish By DSH105", "---------------------", "Language Configuration File"});
        for (Lang l : Lang.values()) {
            String[] desc = l.getDescription();
            langConfig.set(l.getPath(), langConfig.getString(l.getPath(), l.toString_()
                    .replace("&3", "&" + this.primaryColour.getChar())
                    .replace("&b", "&" + this.secondaryColour.getChar())),
                    desc);
        }
        langConfig.saveConfig();
        langConfig.reloadConfig();

        this.prefix = Lang.PREFIX.toString();

        DynamicPluginCommand petCmd = new DynamicPluginCommand(this.cmdString, new String[0], "Create blocks that automagically restore themselves and drop custom items.", "Use /" + this.cmdString + " help to see the command list.", new ReplenishCommand(this.cmdString), null, this);
        petCmd.setPermission("replenish.replenish");
        COMMAND_MANAGER.register(petCmd);

        getServer().getPluginManager().registerEvents(new BlockListener(), this);

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :(
        }

        this.checkUpdates();
    }

    @Override
    public void onDisable() {
        for (Entry<Location, Integer> entry : BlockListener.getInstance().getRestoreProcesStorage().entrySet()) {
            Location loc = entry.getKey();
            int blockTypeId = entry.getValue();
            Block block = loc.getBlock();
            block.setTypeId(blockTypeId);
        }
        INSTANCE = null;
    }

    protected void checkUpdates() {
        if (this.getConfig(ConfigType.MAIN).getBoolean("checkForUpdates", true)) {
            final File file = this.getFile();
            final Updater.UpdateType updateType = this.getConfig(ConfigType.MAIN).getBoolean("autoUpdate", false) ? Updater.UpdateType.DEFAULT : Updater.UpdateType.NO_DOWNLOAD;
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    Updater updater = new Updater(getInstance(), 51750, file, updateType, false);
                    update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                    if (update) {
                        name = updater.getLatestName();
                        LOG.console(ChatColor.GOLD + "An update is available: " + name);
                        LOG.console(ChatColor.GOLD + "Type /replenish update to update.");
                        if (!updateChecked) {
                            updateChecked = true;
                        }
                    }
                }
            });
        }
    }

    public File file() {
        return this.getFile();
    }

    public YAMLConfig getConfig(ConfigType type) {
        if (type == ConfigType.MAIN) {
            return this.config;
        } else if (type == ConfigType.DATA) {
            return this.dataConfig;
        } else if (type == ConfigType.LANG) {
            return this.langConfig;
        }
        return null;
    }

    public static ReplenishPlugin getInstance() {
        return INSTANCE;
    }

    public static enum ConfigType {
        MAIN, DATA, LANG
    }
}
