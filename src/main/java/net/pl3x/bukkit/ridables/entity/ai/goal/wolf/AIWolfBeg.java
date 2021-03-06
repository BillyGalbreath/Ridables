package net.pl3x.bukkit.ridables.entity.ai.goal.wolf;

import net.minecraft.server.v1_13_R2.PathfinderGoalBeg;
import net.pl3x.bukkit.ridables.entity.animal.RidableWolf;

public class AIWolfBeg extends PathfinderGoalBeg {
    private final RidableWolf wolf;

    public AIWolfBeg(RidableWolf wolf, float maxDistance) {
        super(wolf, maxDistance);
        this.wolf = wolf;
    }

    // shouldExecute
    @Override
    public boolean a() {
        return wolf.getRider() == null && super.a();
    }

    // shouldContinueExecuting
    @Override
    public boolean b() {
        return wolf.getRider() == null && super.b();
    }
}
