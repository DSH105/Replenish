package io.github.dsh105.replenish;

import io.github.dsh105.dshutils.Metrics;
import io.github.dsh105.dshutils.Updater;
import io.github.dsh105.dshutils.command.CustomCommand;
import io.github.dsh105.dshutils.config.YAMLConfig;
import io.github.dsh105.dshutils.config.YAMLConfigManager;
import io.github.dsh105.dshutils.logger.ConsoleLogger;
import io.github.dsh105.dshutils.logger.Logger;
import io.github.dsh105.replenish.commands.ReplenishCommand;
import io.github.dsh105.replenish.config.ConfigOptions;
import io.github.dsh105.replenish.config.DataConfigOptions;
import io.github.dsh105.replenish.listeners.BlockListener;
import io.github.dsh105.replenish.util.Lang;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_7_R1.CraftServer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

public class ReplenishPlugin extends JavaPlugin implements Listener {

    private static ReplenishPlugin instance;
    private YAMLConfigManager configManager;
    private YAMLConfig config;
    private YAMLConfig dataConfig;
    private YAMLConfig langConfig;
    private ConfigOptions options;
    private DataConfigOptions dataOptions;

    // Update data
    public boolean update = false;
    public String name = "";
    public long size = 0;
    public boolean updateChecked = false;

    public ChatColor primaryColour = ChatColor.GREEN;
    public ChatColor secondaryColour = ChatColor.YELLOW;
    public String prefix = ChatColor.GOLD + "[" + ChatColor.YELLOW + "Replenish" + ChatColor.GOLD + "] " + ChatColor.RESET;
    public CommandMap CM;
    public String cmdString = "replenish";

    public HashMap<String, InfoStorage> infoStorage = new HashMap<String, InfoStorage>();

    public void onEnable() {
        instance = this;
        Logger.initiate(this, "Replenish", "[Replenish]");
        ConsoleLogger.initiate(this);

        PluginManager manager = getServer().getPluginManager();

        options = new ConfigOptions(config);
        dataOptions = new DataConfigOptions(dataConfig);

        configManager = new YAMLConfigManager(this);
        String[] header = {"Replenish By DSH105", "---------------------",
                "Plugin Requested By Fire_Feather"};
        try {
            config = configManager.getNewConfig("config.yml", header);
            config.reloadConfig();
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.WARNING, "Configuration File [config.yml] generation failed.", e, true);
        }

        ChatColor colour1 = ChatColor.getByChar(this.getConfig(ConfigType.MAIN).getString("primaryChatColour", "a"));
        if (colour1 != null) {
            this.primaryColour = colour1;
        }
        ChatColor colour2 = ChatColor.getByChar(this.getConfig(ConfigType.MAIN).getString("secondaryChatColour", "e"));
        if (colour2 != null) {
            this.secondaryColour = colour2;
        }

        try {
            dataConfig = configManager.getNewConfig("data.yml");
            dataConfig.reloadConfig();
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.WARNING, "Configuration File [data.yml] generation failed.", e, true);
        }

        String[] langHeader = {"Replenish By DSH105", "---------------------",
                "Language Configuration File"};
        try {
            langConfig = configManager.getNewConfig("lang.yml", langHeader);
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

        CustomCommand.initiate(this);
        try {
            if (Bukkit.getServer() instanceof CraftServer) {
                final Field f = CraftServer.class.getDeclaredField("commandMap");
                f.setAccessible(true);
                CM = (CommandMap) f.get(Bukkit.getServer());
            }
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.WARNING, "Registration of command failed.", e, true);
        }

        String cmdString = options.getConfig().getString("command-string");
        if (CM.getCommand(cmdString) != null) {
            ConsoleLogger.log(Logger.LogLevel.WARNING, "A command under the name " + ChatColor.RED + "/" + cmdString + ChatColor.YELLOW + " already exists. Command temporarily registered under " + ChatColor.RED + "/r:" + cmdString);
        }
        CustomCommand cmd = new CustomCommand(cmdString);
        CM.register("r", cmd);
        cmd.setExecutor(new ReplenishCommand(cmdString));
        //cmd.setTabCompleter(new CommandComplete());
        this.cmdString = cmdString;

        manager.registerEvents(this, this);

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
                    Updater updater = new Updater(instance, 53655, file, updateType, false);
                    update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
                    if (update) {
                        name = updater.getLatestName();
                        ConsoleLogger.log(ChatColor.GOLD + "An update is available: " + name);
                        ConsoleLogger.log(ChatColor.GOLD + "Type /replenish update to update.");
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
    }

    public File getFile() {
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
        return instance;
    }

    public static enum ConfigType {
        MAIN, DATA, LANG
    }
}
