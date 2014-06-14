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

import com.captainbern.minecraft.reflection.MinecraftReflection;
import com.captainbern.reflection.Reflection;
import com.dsh105.commodus.GeneralUtil;
import com.dsh105.commodus.GeometryUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

// TODO: Redo this, it's not very well done
public enum Particle {

    SMOKE("largesmoke", 0.2f, 20),
    RED_SMOKE("reddust", 0f, 40),
    RAINBOW_SMOKE("reddust", 1f, 100),
    FIRE("flame", 0.05f, 100),
    HEART("heart", 0f, 4),
    MAGIC_RUNES("enchantmenttable", 1f, 100),
    LAVA_SPARK("lava", 0f, 4),
    SPLASH("splash", 1f, 40),
    PORTAL("portal", 1f, 100),

    EXPLOSION("largeexplode", 0.1f, 1),
    HUGE_EXPLOSION("hugeexplosion", 0.1f, 1),
    CLOUD("explode", 0.1f, 10),
    CRITICAL("crit", 0.1f, 100),
    MAGIC_CRITIAL("magicCrit", 0.1f, 100),
    ANGRY_VILLAGER("angryVillager", 0f, 20),
    SPARKLE("happyVillager", 0f, 20),
    WATER_DRIP("dripWater", 0f, 100),
    LAVA_DRIP("dripLava", 0f, 100),
    WITCH_MAGIC("witchMagic", 1f, 20),

    SNOWBALL("snowballpoof", 1f, 20),
    SNOW_SHOVEL("snowshovel", 0.02f, 30),
    SLIME_SPLAT("slime", 1f, 30),
    BUBBLE("bubble", 0f, 50),
    SPELL_AMBIENT("mobSpellAmbient", 1f, 100),
    VOID("townaura", 1f, 100),

    BLOCK_BREAK("blockcrack", 0.1F, 100),
    BLOCK_DUST("blockdust", 0.1F, 100),;

    private static Random RANDOM = GeneralUtil.random();

    private String particleName;
    private float defaultSpeed;
    private int particleAmount;

    Particle(String particleName, float defaultSpeed, int particleAmount) {
        this.particleName = particleName;
        this.defaultSpeed = defaultSpeed;
        this.particleAmount = particleAmount;
    }

    public String getParticleName() {
        return this.particleName;
    }

    public int getParticleAmount() {
        return this.particleAmount;
    }

    public float getDefaultSpeed() {
        return this.defaultSpeed;
    }

    public static Object createPacket(String particleName, Location location, Vector v, float defaultSpeed, int particleAmount) {
        return new Reflection().reflect(MinecraftReflection.getMinecraftClass("PacketPlayOutWorldParticles"))
                .getSafeConstructor(String.class, Float.class, Float.class, Float.class, Float.class, Float.class, Float.class, Float.class, Integer.class)
                .getAccessor().invoke(particleName, (float) location.getX(), (float) location.getY(), (float) location.getZ(), (float) v.getX(), (float) v.getY(), (float) v.getZ(), defaultSpeed, particleAmount);
    }

    public void sendTo(Location l) {
        sendPacket(l, createPacket(this.particleName, l, new Vector(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()), this.defaultSpeed, this.particleAmount));
    }

    public void sendTo(Location l, Vector v, float speed, int particleAmount) {
        sendPacket(l, createPacket(this.particleName, l, v, speed, particleAmount));
    }

    public void sendToPlayer(Location l, Player p) {
        sendPacket(p, createPacket(this.particleName, l, new Vector(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()), this.defaultSpeed, this.particleAmount));
    }

    public void sendToPlayer(Location l, Player p, Vector v, float speed, int particleAmount) {
        sendPacket(p, createPacket(this.particleName, l, v, speed, particleAmount));
    }

    public void sendDataParticle(Location l, int blockId, int blockMeta) {
        sendPacket(l, createPacket(this.particleName + "_" + blockId + "_" + blockMeta, l, new Vector(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()), this.defaultSpeed, this.particleAmount));
    }

    public void sendDataParticle(Location l, Vector v, float speed, int particleAmount, int blockId, int blockMeta) {
        sendPacket(l, createPacket(this.particleName + "_" + blockId + "_" + blockMeta, l, v, speed, particleAmount));
    }

    public void sendDataParticleToPlayer(Location l, Player p, int blockId, int blockMeta) {
        sendPacket(p, createPacket(this.particleName + "_" + blockId + "_" + blockMeta, l, new Vector(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()), this.defaultSpeed, this.particleAmount));
    }

    public void sendDataParticleToPlayer(Location l, Player p, Vector v, float speed, int particleAmount, int blockId, int blockMeta) {
        sendPacket(p, createPacket(this.particleName + "_" + blockId + "_" + blockMeta, l, v, speed, particleAmount));
    }

    private static void sendPacket(Location l, Object packet) {
        sendPacket(l, packet, 20);
    }

    private static void sendPacket(Location l, Object packet, int radius) {
        if (!GeometryUtil.getNearbyEntities(l, radius).isEmpty()) {
            for (Entity e : GeometryUtil.getNearbyEntities(l, radius)) {
                if (e != null && e instanceof Player) {
                    Player p = (Player) e;
                    sendPacket(p, packet);
                }
            }
        }
    }

    private static void sendPacket(Player p, Object packet) {
        try {
            Object nmsPlayer = new Reflection().reflect(p.getClass()).getSafeMethod("getHandle", Player.class).getAccessor().invoke(p);
            Object con = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            new Reflection().reflect(con.getClass()).getSafeMethod("sendPacket", packet.getClass()).getAccessor().invoke(con, packet);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}