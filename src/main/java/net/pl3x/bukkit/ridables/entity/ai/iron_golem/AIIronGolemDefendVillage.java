package net.pl3x.bukkit.ridables.entity.ai.iron_golem;

import net.minecraft.server.v1_13_R2.PathfinderGoalDefendVillage;
import net.pl3x.bukkit.ridables.entity.RidableIronGolem;

public class AIIronGolemDefendVillage extends PathfinderGoalDefendVillage {
    private final RidableIronGolem golem;

    public AIIronGolemDefendVillage(RidableIronGolem golem) {
        super(golem);
        this.golem = golem;
    }

    // shouldExecute
    public boolean a() {
        return golem.getRider() == null && super.a();
    }

    // shouldContinueExecuting
    public boolean b() {
        return golem.getRider() == null && super.b();
    }
}