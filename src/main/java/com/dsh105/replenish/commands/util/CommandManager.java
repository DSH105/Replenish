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

package com.dsh105.replenish.commands.util;

import com.captainbern.reflection.Reflection;
import com.captainbern.reflection.accessor.FieldAccessor;
import com.dsh105.replenish.ReplenishPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandManager {

    // This is a very ugly patch for PerWorldPlugins.
    protected static final FieldAccessor<CommandMap> SERVER_COMMAND_MAP = new Reflection().reflect(Bukkit.getPluginManager().getClass()/*<- ugly because of PWP*/).getSafeFieldByNameAndType("commandMap", CommandMap.class).getAccessor();
    protected static final FieldAccessor<Map<String, Command>> KNOWN_COMMANDS = (FieldAccessor<Map<String, Command>>) new Reflection().reflect(SimpleCommandMap.class).getSafeFieldByNameAndType("knownCommands", Map.class);
    private final Plugin plugin;
    private CommandMap fallback;

    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void register(DynamicPluginCommand command) {
        getCommandMap().register(this.plugin.getName(), command);
    }

    public boolean unregister() {
        CommandMap commandMap = getCommandMap();
        List<String> toRemove = new ArrayList<String>();
        Map<String, Command> knownCommands = KNOWN_COMMANDS.get(commandMap);
        if (knownCommands == null) {
            return false;
        }
        for (Iterator<Command> i = knownCommands.values().iterator(); i.hasNext(); ) {
            Command cmd = i.next();
            if (cmd instanceof DynamicPluginCommand) {
                i.remove();
                for (String alias : cmd.getAliases()) {
                    Command aliasCmd = knownCommands.get(alias);
                    if (cmd.equals(aliasCmd)) {
                        toRemove.add(alias);
                    }
                }
            }
        }
        for (String string : toRemove) {
            knownCommands.remove(string);
        }
        return true;
    }

    public CommandMap getCommandMap() {
        if (!(Bukkit.getPluginManager() instanceof SimplePluginManager)) {
            this.plugin.getLogger().warning("Seems like your server is using a custom PluginManager? Well let's try injecting our custom commands anyways...");
        }

        CommandMap map = null;

        try {
            map = SERVER_COMMAND_MAP.get(Bukkit.getPluginManager());

            if (map == null) {
                if (fallback != null) {
                    return fallback;
                } else {
                    fallback = map = new SimpleCommandMap(ReplenishPlugin.getInstance().getServer());
                    Bukkit.getPluginManager().registerEvents(new FallbackCommandRegistrationListener(fallback), this.plugin);
                }
            }
        } catch (Exception pie) {
            this.plugin.getLogger().warning("Failed to dynamically register the commands! Let's give it a last shot...");
            // Hmmm.... Pie...
            fallback = map = new SimpleCommandMap(ReplenishPlugin.getInstance().getServer());
            Bukkit.getPluginManager().registerEvents(new FallbackCommandRegistrationListener(fallback), this.plugin);
        }
        return map;
    }
}
