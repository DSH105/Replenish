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