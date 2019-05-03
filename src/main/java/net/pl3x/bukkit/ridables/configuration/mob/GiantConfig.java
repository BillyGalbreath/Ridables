package net.pl3x.bukkit.ridables.configuration.mob;

import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.MobConfig;
import net.pl3x.bukkit.ridables.data.BiomeData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GiantConfig extends MobConfig {
    public double AI_SPEED = 0.2D;
    public double MAX_HEALTH = 100.0D;
    public boolean AI_ENABLED = true;
    public float AI_JUMP_POWER = 0.42F;
    public float AI_STEP_HEIGHT = 3.0F;
    public double AI_MELEE_DAMAGE = 50.0D;
    public double AI_FOLLOW_RANGE = 32.0D;
    public boolean AI_HOSTILE = true;
    public boolean AI_HOSTILE_TO_PLAYERS = true;
    public boolean AI_HOSTILE_TO_VILLAGERS = false;
    public boolean AI_HOSTILE_TO_IRON_GOLEMS = false;
    public boolean AI_HOSTILE_TO_TURTLES = false;
    public boolean AI_HOSTILE_TO_TURTLE_EGGS = false;
    public double RIDING_SPEED = 0.2D;
    public float RIDING_JUMP_POWER = 1.0F;
    public float RIDING_STEP_HEIGHT = 3.0F;
    public boolean RIDING_RIDE_IN_WATER = true;
    public boolean RIDING_ENABLE_MOVE_EVENT = false;
    public boolean RIDING_SADDLE_REQUIRE = false;
    public boolean RIDING_SADDLE_CONSUME = false;
    public boolean SPAWN_NATURALLY = false;

    public Set<BiomeData> SPAWN_BIOMES = Stream.of(
            new BiomeData("plains", 5, 1, 1),
            new BiomeData("sunflower_plains", 5, 1, 1),
            new BiomeData("desert", 5, 1, 1),
            new BiomeData("desert_hills", 5, 1, 1),
            new BiomeData("desert_lakes", 5, 1, 1),
            new BiomeData("mountains", 5, 1, 1),
            new BiomeData("gravelly_mountains", 5, 1, 1),
            new BiomeData("modified_gravelly_mountains", 5, 1, 1),
            new BiomeData("savanna", 5, 1, 1),
            new BiomeData("savanna_plateau", 5, 1, 1),
            new BiomeData("shattered_savanna", 5, 1, 1),
            new BiomeData("shattered_savanna_plateau", 5, 1, 1),
            new BiomeData("badlands", 5, 1, 1),
            new BiomeData("badlands_plateau", 5, 1, 1),
            new BiomeData("eroded_badlands", 5, 1, 1),
            new BiomeData("modified_badlands_plateau", 5, 1, 1)
    ).collect(Collectors.toSet());

    public GiantConfig() {
        super("giant.yml");
        reload();
    }

    @Override
    public void reload() {
        super.reload();

        if (firstLoad) {
            firstLoad = false;

            addDefault("max-health", MAX_HEALTH);
            addDefault("ai.enabled", AI_ENABLED);
            addDefault("ai.speed", AI_SPEED);
            addDefault("ai.jump-power", AI_JUMP_POWER);
            addDefault("ai.step-height", AI_STEP_HEIGHT);
            addDefault("ai.melee-damage", AI_MELEE_DAMAGE);
            addDefault("ai.follow-range", AI_FOLLOW_RANGE);
            addDefault("ai.hostile", AI_HOSTILE);
            addDefault("ai.hostile-to.players", AI_HOSTILE_TO_PLAYERS);
            addDefault("ai.hostile-to.villagers", AI_HOSTILE_TO_VILLAGERS);
            addDefault("ai.hostile-to.iron-golems", AI_HOSTILE_TO_IRON_GOLEMS);
            addDefault("ai.hostile-to.turtles", AI_HOSTILE_TO_TURTLES);
            addDefault("ai.hostile-to.turtle-eggs", AI_HOSTILE_TO_TURTLE_EGGS);
            addDefault("riding.speed", RIDING_SPEED);
            addDefault("riding.jump-power", RIDING_JUMP_POWER);
            addDefault("riding.step-height", RIDING_STEP_HEIGHT);
            addDefault("riding.ride-in-water", RIDING_RIDE_IN_WATER);
            addDefault("spawn.naturally", SPAWN_NATURALLY);

            if (!isSet("spawn.biomes")) {
                SPAWN_BIOMES.forEach(data -> {
                    addDefault("spawn.biomes." + data.getBiome() + ".weight", data.getWeight());
                    addDefault("spawn.biomes." + data.getBiome() + ".group-min", data.getMin());
                    addDefault("spawn.biomes." + data.getBiome() + ".group-max", data.getMax());
                });
            }

            save();
        }

        MAX_HEALTH = getDouble("max-health");
        AI_ENABLED = getBoolean("ai.enabled");
        AI_SPEED = getDouble("ai.speed");
        AI_JUMP_POWER = (float) getDouble("ai.jump-power");
        AI_STEP_HEIGHT = (float) getDouble("ai.step-height");
        AI_MELEE_DAMAGE = getDouble("ai.melee-damage");
        AI_FOLLOW_RANGE = getDouble("ai.follow-range");
        AI_HOSTILE = getBoolean("ai.hostile", true);
        AI_HOSTILE_TO_PLAYERS = getBoolean("ai.hostile-to.players", true);
        AI_HOSTILE_TO_VILLAGERS = getBoolean("ai.hostile-to.villagers", false);
        AI_HOSTILE_TO_IRON_GOLEMS = getBoolean("ai.hostile-to.iron-golems", false);
        AI_HOSTILE_TO_TURTLES = getBoolean("ai.hostile-to.turtles", false);
        AI_HOSTILE_TO_TURTLE_EGGS = getBoolean("ai.hostile-to.turtle-eggs", false);
        RIDING_SPEED = getDouble("riding.speed");
        RIDING_JUMP_POWER = (float) getDouble("riding.jump-power");
        RIDING_STEP_HEIGHT = (float) getDouble("riding.step-height");
        RIDING_RIDE_IN_WATER = getBoolean("riding.ride-in-water");
        RIDING_ENABLE_MOVE_EVENT = isSet("riding.enable-move-event") ? getBoolean("riding.enable-move-event") : Config.RIDING_ENABLE_MOVE_EVENT;
        RIDING_SADDLE_REQUIRE = isSet("riding.saddle.require") ? getBoolean("riding.saddle.require") : Config.RIDING_SADDLE_REQUIRE;
        RIDING_SADDLE_CONSUME = isSet("riding.saddle.consume") ? getBoolean("riding.saddle.consume") : Config.RIDING_SADDLE_CONSUME;
        SPAWN_NATURALLY = getBoolean("spawn.naturally", false);

        SPAWN_BIOMES.clear();
        ConfigurationSection biomes = getConfigurationSection("spawn.biomes");
        if (biomes != null) {
            biomes.getKeys(false).forEach(biome ->
                    SPAWN_BIOMES.add(new BiomeData(biome,
                            biomes.getInt(biome + ".weight"),
                            biomes.getInt(biome + ".group-min"),
                            biomes.getInt(biome + ".group-max")))
            );
        }
    }
}
