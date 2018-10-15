package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityAgeable;
import net.minecraft.server.v1_13_R2.EntityCow;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityMushroomCow;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.mob.MooshroomConfig;
import net.pl3x.bukkit.ridables.entity.ai.AIBreed;
import net.pl3x.bukkit.ridables.entity.ai.AIFollowParent;
import net.pl3x.bukkit.ridables.entity.ai.AILookIdle;
import net.pl3x.bukkit.ridables.entity.ai.AIPanic;
import net.pl3x.bukkit.ridables.entity.ai.AISwim;
import net.pl3x.bukkit.ridables.entity.ai.AITempt;
import net.pl3x.bukkit.ridables.entity.ai.AIWanderAvoidWater;
import net.pl3x.bukkit.ridables.entity.ai.AIWatchClosest;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;

public class RidableMooshroom extends EntityMushroomCow implements RidableEntity {
    public static final MooshroomConfig CONFIG = new MooshroomConfig();

    public RidableMooshroom(World world) {
        super(world);
        moveController = new ControllerWASD(this);
        lookController = new LookController(this);
    }

    public RidableType getType() {
        return RidableType.MOOSHROOM;
    }

    // initAI - override vanilla AI
    protected void n() {
        // from EntityCow
        goalSelector.a(0, new AISwim(this));
        goalSelector.a(1, new AIPanic(this, 2.0D));
        goalSelector.a(2, new AIBreed(this, 1.0D, EntityCow.class));
        goalSelector.a(3, new AITempt(this, 1.25D, false, RidableCow.TEMPTATION_ITEMS));
        goalSelector.a(4, new AIFollowParent(this, 1.25D));
        goalSelector.a(5, new AIWanderAvoidWater(this, 1.0D));
        goalSelector.a(6, new AIWatchClosest(this, EntityHuman.class, 6.0F));
        goalSelector.a(7, new AILookIdle(this));
    }

    // canBeRiddenInWater
    public boolean aY() {
        return CONFIG.RIDABLE_IN_WATER;
    }

    // getJumpUpwardsMotion
    protected float cG() {
        return CONFIG.JUMP_POWER;
    }

    protected void mobTick() {
        Q = CONFIG.STEP_HEIGHT;
        super.mobTick();
    }

    // processInteract
    public boolean a(EntityHuman player, EnumHand hand) {
        return super.a(player, hand) || processInteract(player, hand);
    }

    // removePassenger
    public boolean removePassenger(Entity passenger) {
        return dismountPassenger(passenger.getBukkitEntity()) && super.removePassenger(passenger);
    }

    public RidableMooshroom createChild(EntityAgeable entity) {
        return c(entity);
    }

    // createChild (bukkit's weird duplicate method)
    public RidableMooshroom b(EntityAgeable entity) {
        return c(entity);
    }

    // createChild (bukkit's weird triplicate method)
    public RidableMooshroom c(EntityAgeable entity) {
        return new RidableMooshroom(world);
    }
}
