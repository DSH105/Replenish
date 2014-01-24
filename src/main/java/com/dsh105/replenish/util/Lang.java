package com.dsh105.replenish.util;

import com.dsh105.replenish.ReplenishPlugin;
import com.dsh105.replenish.config.ConfigOptions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Lang {

    PREFIX("prefix", "&7Replenish &r» "),

    NO_PERMISSION("no_permission", "&b%perm% &3permission needed to do that."),
    UPDATE_NOT_AVAILABLE("no_permission", "&3An update is not available."),
    COMMAND_ERROR("cmd_error", "&3Error for input string: &b%cmd%&3. Use &b/" + ReplenishPlugin.getInstance().cmdString + " help &3for help."),
    HELP_INDEX_TOO_BIG("help_index_too_big", "&3Page &b%index% &3does not exist."),
    IN_GAME_ONLY("in_game_only", "&3Please log in to do that."),
    STRING_ERROR("string_error", "&3Error parsing String: [&b%string%&3]. Please revise command arguments."),
    NULL_PLAYER("null_player", "&b%player% &3is not online. Please try a different Player."),
    INT_ONLY("int_only", "&b%string% &3(Arg &b%argNum%&3) needs to be an integer."),
    INVALID_WORLD("invalid_world", "&b%world% &3is an invalid world."),

    BLOCK_REMOVED("block_removed", "&3Block data removed (&b%loc%&3)."),
    BLOCK_CREATED("block_created", "&3Block data created (&b%loc%&3)."),
    BLOCK_NOT_EXISTS("block_not_exists", "&3Block data does not exist for &b%loc%&3."),
    BLOCK_EXISTS("block_exists", "&3Block already has data saved (&b%loc%&3). Use &b/replenish remove &3to remove this data."),

    WORLD_REMOVED("world_removed", "&3World data removed (&b%world%&3)."),
    WORLD_CREATED("world_created", "&3World data created (&b%world%&3)."),
    WORLD_NOT_EXISTS("world_not_exists", "&3World data does not exist for &b%world%&3."),
    WORLD_EXISTS("world_exists", "&3World already has data saved (&b%world%&3). Use &b/replenish remove <world_name> &3to remove this data."),

    WAND_DEACTIVATED("wand_deactivated", "&3Wand deactivated."),
    WAND_ACTIVATED("wand_activated", "&3Wand activated. Right click a block with the wand (" + ConfigOptions.instance.getConfig().getInt("wand") + ") to add the data."),
    WAND_ACTIVE("wand_activate", "&3Wand is already active for that data set."),
    WAND_BOUND("wand_bound", "&3Wand activated and bound. Right click blocks with the wand (" + ConfigOptions.instance.getConfig().getInt("wand") + ") to add the data."),
    WAND_UNBOUND("wand_unbound", "&3Wand unbound."),
    WAND_NOT_BOUND("wand_not_bound", "&3Wand is not currently bound."),;

    private String path;
    private String def;
    private String[] desc;

    Lang(String path, String def, String... desc) {
        this.path = path;
        this.def = def;
        this.desc = desc;
    }

    public String[] getDescription() {
        return this.desc;
    }

    public String getPath() {
        return this.path;
    }

    public static void sendTo(CommandSender sender, String msg) {
        if (msg != null || !msg.equalsIgnoreCase("") && !msg.equalsIgnoreCase(" ") && !msg.equalsIgnoreCase("none")) {
            sender.sendMessage(ReplenishPlugin.getInstance().prefix + msg);
        }
    }

    public static void sendTo(Player p, String msg) {
        if (msg != null && !msg.equalsIgnoreCase("") && !msg.equalsIgnoreCase(" ") && !(msg.equalsIgnoreCase("none"))) {
            p.sendMessage(ReplenishPlugin.getInstance().prefix + msg);
        }
    }

    @Override
    public String toString() {
        String result = ReplenishPlugin.getInstance().getConfig(ReplenishPlugin.ConfigType.LANG).getString(this.path, this.def);
        if (result != null && result != "" && result != "none") {
            return ChatColor.translateAlternateColorCodes('&', ReplenishPlugin.getInstance().getConfig(ReplenishPlugin.ConfigType.LANG).getString(this.path, this.def));
        } else {
            return "";
        }
    }

    public String toString_() {
        return ReplenishPlugin.getInstance().getConfig(ReplenishPlugin.ConfigType.LANG).getString(this.path, this.def);
    }
}