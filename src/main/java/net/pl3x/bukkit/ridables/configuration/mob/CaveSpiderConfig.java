package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class CaveSpiderConfig extends MobConfig {
    public double BASE_SPEED = 0.3D;
    public double RIDE_SPEED = 1.0D;
    public double MAX_HEALTH = 12.0D;
    public float JUMP_POWER = 0.5F;
    public boolean RIDABLE_IN_WATER = true;
    public boolean CLIMB_WALLS = true;
    public float CLIMB_SPEED = 1.0F;
    public double AI_ATTACK_DAMAGE = 2.0D;
    public double AI_FOLLOW_RANGE = 16.0D;

    public CaveSpiderConfig() {
        super("cave_spider.yml");
        reload();
    }

    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("base-speed", BASE_SPEED);
            addDefault("ride-speed", RIDE_SPEED);
            addDefault("max-health", MAX_HEALTH);
            addDefault("jump-power", JUMP_POWER);
            addDefault("ride-in-water", RIDABLE_IN_WATER);
            addDefault("climb-walls", CLIMB_WALLS);
            addDefault("climb-speed", CLIMB_SPEED);
            addDefault("ai.attack-damage", AI_ATTACK_DAMAGE);
            addDefault("ai.follow-range", AI_FOLLOW_RANGE);
            save();
        }

        BASE_SPEED = getDouble("base-speed");
        RIDE_SPEED = getDouble("ride-speed");
        MAX_HEALTH = getDouble("max-health");
        JUMP_POWER = (float) getDouble("jump-power");
        RIDABLE_IN_WATER = getBoolean("ride-in-water");
        CLIMB_WALLS = getBoolean("climb-walls");
        CLIMB_SPEED = (float) getDouble("climb-speed");
        AI_ATTACK_DAMAGE = getDouble("ai.attack-damage");
        AI_FOLLOW_RANGE = getDouble("ai.follow-range");
    }
}
