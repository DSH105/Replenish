package com.dsh105.replenish;

import com.dsh105.dshutils.DSHPlugin;
import com.dsh105.replenish.commands.ReplenishCommand;
import com.dsh105.replenish.commands.util.CommandManager;
import com.dsh105.replenish.commands.util.DynamicPluginCommand;
import com.dsh105.replenish.config.ConfigOptions;
import com.dsh105.replenish.listeners.BlockListener;
import com.dsh105.replenish.util.InfoStorage;
import com.dsh105.replenish.util.Lang;
import com.dsh105.replenish.util.ReplenishLogger;
import com.dsh105.dshutils.Metrics;
import com.dsh105.dshutils.Updater;
import com.dsh105.dshutils.config.YAMLConfig;
import com.dsh105.dshutils.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class ReplenishPlugin extends DSHPlugin {

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

    public void onEnable() {
        super.onEnable();
        Logger.initiate(this, "Replenish", "[Replenish]");

        COMMAND_MANAGER = new CommandManager(this);

        PluginManager manager = getServer().getPluginManager();

        String[] header = {
                "Replenish By DSH105",
                "---------------------",
                "Plugin Requested By Fire_Feather",
                "---------------------",
        };
        try {
            config = this.getConfigManager().getNewConfig("config.yml", header);
            config.reloadConfig();
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.WARNING, "Configuration File [config.yml] generation failed.", e, true);
        }

        ChatColor colour1 = ChatColor.getByChar(this.getConfig(ConfigType.MAIN).getString("primaryChatColour", "3"));
        if (colour1 != null) {
            this.primaryColour = colour1;
        }
        ChatColor colour2 = ChatColor.getByChar(this.getConfig(ConfigType.MAIN).getString("secondaryChatColour", "b"));
        if (colour2 != null) {
            this.secondaryColour = colour2;
        }

        try {
            dataConfig = this.getConfigManager().getNewConfig("data.yml");
            dataConfig.reloadConfig();
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.WARNING, "Configuration File [data.yml] generation failed.", e, true);
        }

        options = new ConfigOptions(config);

        String[] langHeader = {"Replenish By DSH105", "---------------------",
                "Language Configuration File"};
        try {
            langConfig = this.getConfigManager().getNewConfig("lang.yml", langHeader);
            try {
                for (Lang l : Lang.values()) {
                    String[] desc = l.getDescription();
                    langConfig.set(l.getPath(), langConfig.getString(l.getPath(), l.toString_()
                            .replace("&3", "&" + this.primaryColour.getChar())
                            .replace("&b", "&" + this.secondaryColour.getChar())),
                            desc);
                }
                langConfig.saveConfig();
                langConfig.reloadConfig();
            } catch (Exception e) {
                Logger.log(Logger.LogLevel.WARNING, "Configuration File [lang.yml] generation failed.", e, true);
            }

        } catch (Exception e) {
            Logger.log(Logger.LogLevel.WARNING, "Configuration File [lang.yml] generation failed.", e, true);
        }


        this.prefix = Lang.PREFIX.toString();

        DynamicPluginCommand petCmd = new DynamicPluginCommand(this.cmdString, new String[0], "Create blocks that automagically restore themselves and drop custom items.", "Use /" + this.cmdString + " help to see the command list.", new ReplenishCommand(this.cmdString), null, this);
        petCmd.setPermission("replenish.replenish");
        COMMAND_MANAGER.register(petCmd);

        manager.registerEvents(new BlockListener(), this);

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :(
        }

        this.checkUpdates();
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
                        ReplenishLogger.log(ChatColor.GOLD + "An update is available: " + name);
                        ReplenishLogger.log(ChatColor.GOLD + "Type /replenish update to update.");
                        if (!updateChecked) {
                            updateChecked = true;
                        }
                    }
                }
            });
        }
    }

    public void onDisable() {
        for (Entry<Location, Integer> entry : BlockListener.getInstance().getRestoreProcesStorage().entrySet()) {
            Location loc = entry.getKey();
            int blockTypeId = entry.getValue();
            Block block = loc.getBlock();
            block.setTypeId(blockTypeId);
        }
        super.onDisable();
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
        return (ReplenishPlugin) getPluginInstance();
    }

    public static enum ConfigType {
        MAIN, DATA, LANG
    }
}
