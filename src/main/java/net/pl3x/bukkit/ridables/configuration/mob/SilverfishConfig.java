package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.MobConfig;

public class SilverfishConfig extends MobConfig {
    public float SPEED = 1.0F;
    public float JUMP_POWER = 0.5F;
    public float STEP_HEIGHT = 0.6F;
    public boolean RIDABLE_IN_WATER = true;

    public SilverfishConfig() {
        super("silverfish.yml");
        reload();
    }

    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;
            addDefault("speed", SPEED);
            addDefault("jump-power", JUMP_POWER);
            addDefault("step-height", STEP_HEIGHT);
            addDefault("ride-in-water", RIDABLE_IN_WATER);
            save();
        }

        SPEED = (float) getDouble("speed");
        JUMP_POWER = (float) getDouble("jump-power");
        STEP_HEIGHT = (float) getDouble("step-height");
        RIDABLE_IN_WATER = getBoolean("ride-in-water");
    }
}
