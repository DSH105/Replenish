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

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * From Influx: http://github.com/DSH105/Influx/
 */
public class InfluxCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    private Plugin plugin;
    private final CommandExecutor executor;

    public InfluxCommand(Plugin plugin, CommandExecutor executor, String commandName, String description, String usage, String... aliases) {
        super(commandName, description, usage, Arrays.asList(aliases));
        this.plugin = plugin;
        this.executor = executor;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return getPlugin().isEnabled() && getExecutor().onCommand(sender, this, commandLabel, args);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }
}