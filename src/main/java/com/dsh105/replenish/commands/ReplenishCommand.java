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

package com.dsh105.replenish.commands;

import com.dsh105.commodus.GeneralUtil;
import com.dsh105.commodus.IdentUtil;
import com.dsh105.commodus.StringUtil;
import com.dsh105.commodus.config.YAMLConfig;
import com.dsh105.replenish.util.Updater;
import com.dsh105.commodus.paginator.StringPaginator;
import com.dsh105.replenish.ReplenishPlugin;
import com.dsh105.replenish.util.InfoStorage;
import com.dsh105.replenish.util.Lang;
import com.dsh105.replenish.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

// TODO: Migrate to Influx
public class ReplenishCommand implements CommandExecutor {

    public StringPaginator help;

    private static String[] CREATE_HELP = new String[]{
            ChatColor.AQUA + "/replenish create <id-when-mined> <item-drop> <restore-time>",
            ChatColor.DARK_AQUA + "- Mined ID " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): The ID the block will change to when it is mined by a player.",
            ChatColor.DARK_AQUA + "- Item Drop " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): The ID the block will drop when it is mined.",
            ChatColor.DARK_AQUA + "- Restore Time " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): Length of time (in seconds) the block takes to restore back to its original state.",
            ChatColor.AQUA + "/replenish create <listen-id> <id-when-mined> <item-drop> <restore-time> <world>",
            ChatColor.DARK_AQUA + "- Listen ID " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): ID of a block to listen to in a specific world. Entering " + ChatColor.AQUA + "ALL" + ChatColor.DARK_AQUA + " will replenish all broken blocks",
            ChatColor.DARK_AQUA + "- Mined ID " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): The ID the block will change to when it is mined by a player.",
            ChatColor.DARK_AQUA + "- Item Drop " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): The ID the block will drop when it is mined.",
            ChatColor.DARK_AQUA + "- Restore Time " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "Integer" + ChatColor.DARK_AQUA + "): Length of time (in seconds) the block takes to restore back to its original state.",
            ChatColor.DARK_AQUA + "- World Name " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "String" + ChatColor.DARK_AQUA + "): World affected by the Replenish data."
    };

    public ReplenishCommand() {
        this.help = this.generateHelp();
    }

    private StringPaginator generateHelp() {
        String[] s = new String[]{
                ChatColor.AQUA + "/replenish help <command>" + ChatColor.DARK_AQUA + " - View help for a command (e.g. /replenish help create).",
                ChatColor.AQUA + "/replenish bind <id-when-mined> <item-drop> <restore-time>" + ChatColor.DARK_AQUA + " - Bind the wand to continuously add Replenish data to blocks.",
                ChatColor.AQUA + "/replenish unbind" + ChatColor.DARK_AQUA + " - Unbind the Replenish wand.",
                ChatColor.AQUA + "/replenish create <id-when-mined> <item-drop> <restore-time>" + ChatColor.DARK_AQUA + " - Activate the wand for adding Replenish data to blocks.",
                ChatColor.AQUA + "/replenish remove" + ChatColor.DARK_AQUA + " - Activate the wand for removing Replenish data from blocks.",
                ChatColor.AQUA + "/replenish create <listen-id> <id-when-mined> <item-drop> <restore-time> <world-name>" + ChatColor.DARK_AQUA + " - Restore certain blocks in a world according to their ID.",
                ChatColor.AQUA + "/replenish remove <world_name>" + ChatColor.DARK_AQUA + " - Remove replenish data for a specific world."
        };
        return new StringPaginator(5, s);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            if (args.length == 1) {
                String[] help = this.help.getPage(1);
                sender.sendMessage(ChatColor.DARK_AQUA + "----------------" + ChatColor.AQUA + " Replenish Help 1/" + this.help.getPages() + "  " + ChatColor.DARK_AQUA + "----------------");
                sender.sendMessage(ChatColor.DARK_AQUA + "Parameters: <> = Required      [] = Optional");
                for (String s : help) {
                    sender.sendMessage(s);
                }
                sender.sendMessage(ChatColor.DARK_AQUA + "--------------------------------------------------");
                return true;
            } else if (args.length == 2) {
                if (GeneralUtil.isInt(args[1])) {
                    String[] help = this.help.getPage(Integer.parseInt(args[1]));
                    if (help == null) {
                        Lang.sendTo(sender, Lang.HELP_INDEX_TOO_BIG.toString().replace("%index%", args[1]));
                        return true;
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "----------------" + ChatColor.AQUA + " Replenish Help " + args[1] + "/" + this.help.getPages() + "  " + ChatColor.DARK_AQUA + "----------------");
                    for (String s : help) {
                        sender.sendMessage(s);
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "--------------------------------------------------");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "----------------" + ChatColor.AQUA + " Replenish Help " + ChatColor.DARK_AQUA + "----------------");
                    if (args[1].equalsIgnoreCase("create")) {
                        for (String s : CREATE_HELP) {
                            sender.sendMessage(s);
                        }
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        sender.sendMessage(ChatColor.AQUA + "/replenish remove" + ChatColor.DARK_AQUA);
                        sender.sendMessage(ChatColor.AQUA + "/replenish remove <world>" + ChatColor.DARK_AQUA);
                        sender.sendMessage(ChatColor.DARK_AQUA + "- World Name " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "String" + ChatColor.DARK_AQUA + "): World to remove the data from.");
                    } else if (args[1].equalsIgnoreCase("bind")) {
                        sender.sendMessage(ChatColor.AQUA + "/replenish create <id-when-mined> <item-drop> <restore-time>");
                        sender.sendMessage(ChatColor.DARK_AQUA + "- Binds the wand so that data can be continuously added to different blocks.");
                        sender.sendMessage(ChatColor.DARK_AQUA + "- See /replenish help create for information on the parameters.");
                    } else {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Help could not be found for \"" + ChatColor.AQUA + args[1] + ChatColor.DARK_AQUA + "\".");
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "--------------------------------------------------");
                    return true;
                }
            }
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("create")) {
                if (Perm.CREATE.hasPerm(sender, true, false)) {
                    Player p = (Player) sender;
                    if (this.getInfoStorage().containsKey(IdentUtil.getIdentificationForAsString(p))) {
                        if (!this.getInfoStorage().get(IdentUtil.getIdentificationForAsString(p)).getInfo().equals("remove")) {
                            this.getInfoStorage().remove(IdentUtil.getIdentificationForAsString(p));
                            Lang.sendTo(sender, Lang.WAND_DEACTIVATED.toString());
                            return true;
                        }
                    }
                    for (String s : CREATE_HELP) {
                        sender.sendMessage(s);
                    }
                    return true;
                } else return true;
            } else if (args[0].equalsIgnoreCase("unbind")) {
                if (Perm.UNBIND.hasPerm(sender, true, false)) {
                    Player p = (Player) sender;
                    if (this.getInfoStorage().containsKey(IdentUtil.getIdentificationForAsString(p)) && this.getInfoStorage().get(IdentUtil.getIdentificationForAsString(p)).isBound()) {
                        this.getInfoStorage().remove(IdentUtil.getIdentificationForAsString(p));
                        Lang.sendTo(sender, Lang.WAND_UNBOUND.toString());
                    } else {
                        Lang.sendTo(sender, Lang.WAND_NOT_BOUND.toString());
                    }
                    return true;
                } else return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (Perm.REMOVE.hasPerm(sender, true, false)) {
                    Player p = (Player) sender;
                    if (this.getInfoStorage().containsKey(p.getName())) {
                        if (this.getInfoStorage().get(IdentUtil.getIdentificationForAsString(p)).getInfo().equals("remove")) {
                            this.getInfoStorage().remove(IdentUtil.getIdentificationForAsString(p));
                            Lang.sendTo(sender, Lang.WAND_DEACTIVATED.toString());
                        }
                    } else {
                        this.getInfoStorage().put(IdentUtil.getIdentificationForAsString(p), new InfoStorage("remove", false));
                        Lang.sendTo(sender, Lang.WAND_ACTIVATED.toString());
                    }
                    return true;
                } else return true;
            }
            if (args[0].equalsIgnoreCase("update")) {
                if (Perm.UPDATE.hasPerm(sender, true, true)) {
                    if (ReplenishPlugin.getInstance().updateChecked) {
                        new Updater(ReplenishPlugin.getInstance(), 53655, ReplenishPlugin.getInstance().file(), Updater.UpdateType.NO_VERSION_CHECK, true);
                    } else {
                        Lang.sendTo(sender, Lang.UPDATE_NOT_AVAILABLE.toString());
                    }
                    return true;
                } else return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.DARK_AQUA + "--------------------" + ChatColor.AQUA + " Replenish Help " + ChatColor.DARK_AQUA + "--------------------");
                if (args[1].equalsIgnoreCase("create")) {
                    for (String s : CREATE_HELP) {
                        sender.sendMessage(s);
                    }
                } else if (args[1].equalsIgnoreCase("remove")) {
                    sender.sendMessage(ChatColor.AQUA + "/replenish remove" + ChatColor.DARK_AQUA);
                    sender.sendMessage(ChatColor.AQUA + "/replenish remove <world>" + ChatColor.DARK_AQUA);
                    sender.sendMessage(ChatColor.DARK_AQUA + "- World Name " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + "String" + ChatColor.DARK_AQUA + "): World to remove the data from.");
                } else if (args[1].equalsIgnoreCase("bind")) {
                    sender.sendMessage(ChatColor.AQUA + "/replenish create <id-when-mined> <item-drop> <restore-time>");
                    sender.sendMessage(ChatColor.DARK_AQUA + "- Binds the wand so that data can be continuously added to different blocks.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "- See /replenish help create for information on the parameters.");
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Help could not be found for \"" + ChatColor.AQUA + args[1] + ChatColor.DARK_AQUA + "\".");
                }
                sender.sendMessage(ChatColor.DARK_AQUA + "------------------------------------------------------------");
                return true;
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (Perm.REMOVE_WORLD.hasPerm(sender, true, true)) {
                    if (Bukkit.getWorld(args[1]) == null) {
                        Lang.sendTo(sender, Lang.INVALID_WORLD.toString().replace("%world%", args[1]));
                        return true;
                    }
                    World world = Bukkit.getWorld(args[1]);
                    YAMLConfig c = ReplenishPlugin.getInstance().getConfig(ReplenishPlugin.ConfigType.DATA);
                    if (c.get("worlds." + world.getName()) != null) {
                        c.set("worlds." + world.getName(), null);
                        c.saveConfig();
                        Lang.sendTo(sender, Lang.WORLD_REMOVED.toString().replace("%world%", world.getName()));
                    } else {
                        Lang.sendTo(sender, Lang.WORLD_NOT_EXISTS.toString().replace("%world%", world.getName()));
                    }
                    return true;
                } else return true;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("bind")) {
                boolean bound = args[0].equalsIgnoreCase("bind");
                if ((bound && Perm.BIND.hasPerm(sender, true, false)) || (!bound && Perm.CREATE.hasPerm(sender, true, false))) {
                    Player p = (Player) sender;
                    String s = "";
                    for (int i = 1; i <= 3; i++) {
                        if (i == 2 && args[i].contains("id;")) {
                            s = s + ((s.isEmpty()) ? "id;" + args[i].split(";")[1] : ":id;" + args[i].split(";")[1]);
                        } else if (GeneralUtil.isInt(args[i])) {
                            s = s + ((s.isEmpty()) ? "" + args[i] : ":" + args[i]);
                        } else {
                            Lang.sendTo(sender, Lang.INT_ONLY.toString().replace("%string%", args[i]).replace("%argNum%", "" + i));
                            return true;
                        }
                    }
                    if (this.getInfoStorage().containsKey(IdentUtil.getIdentificationForAsString(p)) && !this.getInfoStorage().get(IdentUtil.getIdentificationForAsString(p)).getInfo().equals(s)) {
                        Lang.sendTo(sender, Lang.WAND_ACTIVE.toString());
                    } else {
                        this.getInfoStorage().put(IdentUtil.getIdentificationForAsString(p), new InfoStorage(s, bound));
                        Lang.sendTo(sender, bound ? Lang.WAND_BOUND.toString() : Lang.WAND_ACTIVATED.toString());
                    }
                    return true;
                } else return true;
            }
        } else if (args.length == 6) {
            if (args[0].equalsIgnoreCase("create")) {
                if (Perm.CREATE_WORLD.hasPerm(sender, true, true)) {
                    String s = "";
                    for (int i = 1; i <= 4; i++) {
                        // Find saved stack by id
                        if (i == 3 && args[i].contains("id;")) {
                            s = s + ((s.isEmpty()) ? "id;" + args[i].split(";")[1] : ":id;" + args[i].split(";")[1]);
                        } else if (GeneralUtil.isInt(args[i])) {
                            s = s + ((s.isEmpty()) ? "" + args[i] : ":" + args[i]);
                        } else if (i == 1 && args[i].equalsIgnoreCase("all")) {
                            s = args[i].toLowerCase();
                        } else {
                            Lang.sendTo(sender, Lang.INT_ONLY.toString().replace("%string%", args[i]).replace("%argNum%", "" + i));
                            return true;
                        }
                    }
                    World world = Bukkit.getWorld(args[5]);
                    if (world == null) {
                        Lang.sendTo(sender, Lang.INVALID_WORLD.toString().replace("%world%", args[5]));
                        return true;
                    }
                    YAMLConfig c = ReplenishPlugin.getInstance().getConfig(ReplenishPlugin.ConfigType.DATA);
                    if (c.get("worlds." + world.getName()) == null) {
                        c.set("worlds." + world.getName(), s);
                        c.saveConfig();
                        Lang.sendTo(sender, Lang.WORLD_CREATED.toString().replace("%world%", world.getName()));
                    } else {
                        Lang.sendTo(sender, Lang.WORLD_EXISTS.toString().replace("%world%", world.getName()));
                    }
                    return true;
                } else return true;
            }
        }
        Lang.sendTo(sender, Lang.COMMAND_ERROR.toString()
                .replace("%cmd%", "/" + cmd.getLabel() + " " + (args.length == 0 ? "" : StringUtil.combineSplit(0, args, " "))));
        return true;
    }

    public HashMap<String, InfoStorage> getInfoStorage() {
        return ReplenishPlugin.getInstance().infoStorage;
    }
}