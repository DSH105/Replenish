package io.github.dsh105.replenish.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public enum Perm {

    UPDATE("replenish.update", "replenish.replenish", "replenish.*"),
    BUILD("replenish.build", "", "replenish.*"),
    CREATE("replenish.create", "replenish.replenish", "replenish.*"),
    REMOVE("replenish.remove", "replenish.replenish", "replenish.*"),
    BIND("replenish.bind", "replenish.replenish", "replenish.*"),
    UNBIND("replenish.unbind", "replenish.replenish", "replenish.*"),
    REMOVE_WORLD("replenish.world.remove", "replenish.replenish", "replenish.*", "replenish.world.*"),
    CREATE_WORLD("replenish.world.remove", "replenish.replenish", "replenish.*", "replenish.world.*"),
    ;

    String perm;
    String requiredPerm;
    ArrayList<String> hierarchy = new ArrayList<String>();

    Perm(String perm, String requiredPerm, String... hierarchy) {
        this.perm = perm;
        this.requiredPerm = requiredPerm;
        for (String s : hierarchy) {
            this.hierarchy.add(s);
        }
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
        boolean hasRequiredPerm = this.requiredPerm.equalsIgnoreCase("") ? true : player.hasPermission(this.requiredPerm);
        if (!(player.hasPermission(this.perm) && hasRequiredPerm)) {
            for (String s : this.hierarchy) {
                if (player.hasPermission(s)) {
                    return true;
                }
            }
            if (sendMessage) {
                Lang.sendTo(player, Lang.NO_PERMISSION.toString().replace("%perm%", this.perm));
            }
            return false;
        } else {
            return true;
        }
    }
}