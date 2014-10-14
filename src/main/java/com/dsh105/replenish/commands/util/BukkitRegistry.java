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

import com.dsh105.commodus.reflection.Reflection;
import com.dsh105.replenish.ReplenishPlugin;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * From Influx: http://github.com/DSH105/Influx/
 */
public class BukkitRegistry {

    static {
        Bukkit.getHelpMap().registerHelpTopicFactory(InfluxCommand.class, new InfluxCommandHelpTopicFactory());
    }

    private static Field SERVER_COMMAND_MAP;

    private CommandMap commandMap;
    private final ArrayList<String> registeredCommands = new ArrayList<String>();

    public CommandMap getCommandMap() {
        if (commandMap == null) {
            try {
                if (SERVER_COMMAND_MAP == null) {
                    SERVER_COMMAND_MAP = Reflection.getField(Bukkit.getServer().getPluginManager().getClass(), "commandMap");
                }
                commandMap = (CommandMap) SERVER_COMMAND_MAP.get(Bukkit.getPluginManager());
            } catch (Exception e) {
                ReplenishPlugin.getInstance().getLogger().warning("Failed to retrieve CommandMap! Using fallback instead...");

                commandMap = new SimpleCommandMap(Bukkit.getServer());
                Bukkit.getPluginManager().registerEvents(new FallbackCommandListener(commandMap), ReplenishPlugin.getInstance());
            }
        }

        return commandMap;
    }

    public boolean register(InfluxCommand command) {
        Preconditions.checkNotNull(command, "Command must not be null.");
        if (registeredCommands.contains(command.getLabel())) {
            return false;
        }

        if (!getCommandMap().register(command.getPlugin().getName(), command) && !registeredCommands.add(command.getLabel())) {
            unregister(command);
            return false;
        }
        return true;
    }

    public boolean unregister(InfluxCommand command) {
        Preconditions.checkNotNull(command, "Command must not be null.");
        return command.unregister(getCommandMap());
    }

    public boolean unregister(String command) {
        if (registeredCommands.remove(command)) {
            org.bukkit.command.Command bukkitCommand = getCommandMap().getCommand(command);
            if (bukkitCommand != null && bukkitCommand instanceof InfluxCommand) {
                return unregister((InfluxCommand) bukkitCommand);
            }
        }
        return false;
    }
}