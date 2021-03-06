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

package com.dsh105.replenish.listeners;

import com.dsh105.commodus.GeneralUtil;
import com.dsh105.commodus.IdentUtil;
import com.dsh105.commodus.config.YAMLConfig;
import com.dsh105.commodus.logging.Level;
import com.dsh105.commodus.particle.Particle;
import com.dsh105.replenish.ReplenishPlugin;
import com.dsh105.replenish.config.ConfigOptions;
import com.dsh105.replenish.util.InfoStorage;
import com.dsh105.replenish.util.Lang;
import com.dsh105.replenish.util.Perm;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class BlockListener implements Listener {

    private static BlockListener instance;

    public BlockListener() {
        instance = this;
    }

    private HashMap<Location, Integer> restoreProcess = new HashMap<Location, Integer>();
    private YAMLConfig dataConfig = ReplenishPlugin.getInstance().getConfig(ReplenishPlugin.ConfigType.DATA);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (this.getInfoStorage().containsKey(IdentUtil.getIdentificationForAsString(player))) {
            if (player.getItemInHand().getTypeId() == ConfigOptions.instance.getConfig().getInt("wand")) {
                InfoStorage i = this.getInfoStorage().get(IdentUtil.getIdentificationForAsString(player));
                if (i.getInfo().equals("remove")) {
                    Block targetBlock = event.getClickedBlock();
                    int targetBlockX = targetBlock.getLocation().getBlockX();
                    int targetBlockY = targetBlock.getLocation().getBlockY();
                    int targetBlockZ = targetBlock.getLocation().getBlockZ();
                    World world = targetBlock.getWorld();
                    Location loc = new Location(world, targetBlockX, targetBlockY, targetBlockZ);
                    String sLoc = serialiseLocation(loc);
                    ConfigurationSection blockSection = this.dataConfig.getConfigurationSection("blocks");
                    if (blockSection != null) {
                        for (String key : blockSection.getKeys(false)) {
                            if (key != null) {
                                if (sLoc.equals(key)) {
                                    this.dataConfig.set("blocks." + sLoc, null);
                                    this.dataConfig.saveConfig();
                                    Lang.sendTo(player, Lang.BLOCK_REMOVED.toString().replace("%loc%", sLoc.replace(":", ", ")));
                                    if (!i.isBound()) {
                                        this.getInfoStorage().remove(IdentUtil.getIdentificationForAsString(player));
                                    }
                                    if (restoreProcess.containsKey(loc)) {
                                        targetBlock.setTypeId(restoreProcess.get(loc));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Block targetBlock = event.getClickedBlock();
                    int targetBlockX = targetBlock.getLocation().getBlockX();
                    int targetBlockY = targetBlock.getLocation().getBlockY();
                    int targetBlockZ = targetBlock.getLocation().getBlockZ();
                    World world = targetBlock.getWorld();
                    Location loc = new Location(world, targetBlockX, targetBlockY, targetBlockZ);
                    String sLoc = serialiseLocation(loc);
                    if (this.dataConfig.get(sLoc) == null) {
                        this.dataConfig.set("blocks." + sLoc, i.getInfo());
                        this.dataConfig.saveConfig();
                        Lang.sendTo(player, Lang.BLOCK_CREATED.toString().replace("%loc%", sLoc.replace(":", ", ")));
                        if (!i.isBound()) {
                            this.getInfoStorage().remove(IdentUtil.getIdentificationForAsString(player));
                        }
                    } else {
                        Lang.sendTo(player, Lang.BLOCK_EXISTS.toString().replace("%loc%", sLoc.replace(":", ", ")));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!ConfigOptions.instance.getConfig().getBoolean("allowBlockBreak", false)) {
            if (!Perm.BUILD.hasPerm(player, false, false)) {
                event.setCancelled(true);
            }
        }

        Block targetBlock = event.getBlock();
        Location loc = targetBlock.getLocation();
        String sLoc = serialiseLocation(loc);
        ConfigurationSection blockSection = this.dataConfig.getConfigurationSection("blocks");
        if (blockSection != null) {
            for (String key : blockSection.getKeys(false)) {
                if (key != null) {
                    if (sLoc.equals(key)) {
                        if (!(restoreProcess.containsKey(loc))) {
                            String blockData = this.dataConfig.getString("blocks." + key);
                            String[] split = blockData.split(":");
                            event.setCancelled(true);
                            this.replenish(player, targetBlock, Integer.parseInt(split[0]), split[1], Integer.parseInt(split[2]));
                        } else {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }

        ConfigurationSection worldSection = this.dataConfig.getConfigurationSection("worlds");
        if (worldSection != null) {
            for (String key : worldSection.getKeys(false)) {
                if (key != null) {
                    if (loc.getWorld().getName().equals(key)) {
                        if (!(restoreProcess.containsKey(loc))) {
                            String blockData = this.dataConfig.getString("worlds." + key);
                            String[] split = blockData.split(":");
                            int listenId;
                            if (split[0].equalsIgnoreCase("all")) {
                                // Means all blocks in the world will be replenished
                                listenId = -1;
                            } else {
                                // Otherwise, only listen in on this id
                                listenId = Integer.parseInt(split[0]);
                            }
                            if (listenId == -1 || targetBlock.getTypeId() == listenId) {
                                event.setCancelled(true);
                                this.replenish(player, targetBlock, Integer.parseInt(split[1]), split[2], Integer.parseInt(split[3]));
                            }
                        } else {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    public void replenish(Player p, Block targetBlock, int mined, String drop, int restore) {
        Location l = targetBlock.getLocation();
        int dropChance = ConfigOptions.instance.getConfig().getInt("drop.chance", 100);
        if (GeneralUtil.random().nextInt(99) < dropChance) {
            int minAmount = ConfigOptions.instance.getConfig().getInt("drop.minQuantity", 1);
            int maxAmount = ConfigOptions.instance.getConfig().getInt("drop.maxQuantity", 3);
            int i = GeneralUtil.random().nextInt(maxAmount - minAmount + 1);
            if (GeneralUtil.random().nextInt(3) > 0) {
                i /= 2;
            }
            int dropAmount = minAmount + i;

            ItemStack itemStack = null;
            if (drop.contains("id;")) {
                String id = drop.split(";")[1];
                itemStack = ConfigOptions.instance.getSavedStack(id, targetBlock.getTypeId(), dropAmount);
                if (itemStack == null) {
                    ReplenishPlugin.LOG.console(Level.WARNING, "Failed to find saved stack of ID " + id + ". Please check your configuration.");
                    return;
                }
            }

            if (itemStack == null) {
                itemStack = new ItemStack(Integer.parseInt(drop), dropAmount);
            }
            l.getWorld().dropItemNaturally(l, itemStack);
        }
        restoreProcess.put(l, targetBlock.getTypeId());
        scheduleRestore(l, targetBlock.getTypeId(), restore);
        targetBlock.setTypeId(mined);
        this.playEffect(p, l);
    }

    public static BlockListener getInstance() {
        return instance;
    }

    public HashMap<String, InfoStorage> getInfoStorage() {
        return ReplenishPlugin.getInstance().infoStorage;
    }

    public HashMap<Location, Integer> getRestoreProcesStorage() {
        return this.restoreProcess;
    }

    private void scheduleRestore(final Location loc, final int typeId, int restore) {
        new BukkitRunnable() {

            @Override
            public void run() {
                loc.getBlock().setTypeId(typeId);
                restoreProcess.remove(loc);
            }

        }.runTaskLater(ReplenishPlugin.getInstance(), 20 * restore);
    }

    private String serialiseLocation(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private void playEffect(Player player, Location l) {
        if (ConfigOptions.instance.getConfig().getBoolean("effect.play", true)) {
            boolean playerOnly = ConfigOptions.instance.getConfig().getBoolean("effect.onlyMinerCanSee", false);
            if (ConfigOptions.instance.getConfig().getBoolean("effect.permissionRestricted", false)) {
                if (!player.hasPermission("replenish.effect")) {
                    return;
                }
            }
            Particle p;
            String effect = ConfigOptions.instance.getConfig().getString("effect.type", "fire");
            if (GeneralUtil.isEnumType(Particle.class, effect.toUpperCase())) {
                p = Particle.valueOf(effect.toUpperCase());
            } else {
                p = Particle.FIRE;
            }

            if (playerOnly) {
                p.builder().withLocation(l.toVector()).show(player);
            } else {
                p.builder().withLocation(l.toVector()).show(l);
            }
        }
    }
}