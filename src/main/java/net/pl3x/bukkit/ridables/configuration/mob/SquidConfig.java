package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class SquidConfig extends MobConfig {
    public double BASE_SPEED = 0.7D;
    public double MAX_HEALTH = 10.0D;
    public double AI_FOLLOW_RANGE = 16.0D;
    public double RIDING_SPEED = 0.7D;
    public int RIDING_INK_COOLDOWN = 100;
    public boolean RIDING_ENABLE_MOVE_EVENT = false;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;

    public SquidConfig() {
        super("squid.yml");
        reload();
    }

    @Override
    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("base-speed", BASE_SPEED);
            addDefault("max-health", MAX_HEALTH);
            addDefault("ai.follow-range", AI_FOLLOW_RANGE);
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.ink-cooldown", RIDING_INK_COOLDOWN);
            save();
        }

        BASE_SPEED = getDouble("base-speed");
        MAX_HEALTH = getDouble("max-health");
        AI_FOLLOW_RANGE = getDouble("ai.follow-range");
        RIDING_SPEED = getDouble("riding.speed");
        RIDING_INK_COOLDOWN = (int) getDouble("riding.ink-cooldown");
        RIDING_ENABLE_MOVE_EVENT = isSet("riding.enable-move-event") ? getBoolean("riding.enable-move-event") : Config.RIDING_ENABLE_MOVE_EVENT;
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
    }
}
