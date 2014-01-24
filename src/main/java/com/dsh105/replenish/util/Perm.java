package com.dsh105.replenish.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Perm {

    UPDATE("replenish.update"),
    BUILD("replenish.build"),
    CREATE("replenish.create"),
    REMOVE("replenish.remove"),
    BIND("replenish.bind"),
    UNBIND("replenish.unbind"),
    REMOVE_WORLD("replenish.world.remove"),
    CREATE_WORLD("replenish.world.create"),;

    String perm;

    Perm(String perm) {
        this.perm = perm;
    }

    public boolean hasPerm(CommandSender sender, boolean sendMessage, boolean allowConsole) {
        if (sender instanceof Player) {
            return hasPerm(((Player) sender), sendMessage);
        } else {
            if (!allowConsole && sendMessage) {
                Lang.sendTo(sender, Lang.IN_GAME_ONLY.toString());
            }
            return allowConsole;
        }
    }

    public boolean hasPerm(Player player, boolean sendMessage) {
        if (player.hasPermission(this.perm)) {
            return true;
        }
        if (sendMessage) {
            Lang.sendTo(player, Lang.NO_PERMISSION.toString().replace("%perm%", this.perm));
        }
        return false;
    }
}