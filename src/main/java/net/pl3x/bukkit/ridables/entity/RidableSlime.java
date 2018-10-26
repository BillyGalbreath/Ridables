package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityIronGolem;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntitySlime;
import net.minecraft.server.v1_13_R2.EntityTypes;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.MobEffects;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.PathfinderGoalNearestAttackableTargetInsentient;
import net.minecraft.server.v1_13_R2.PathfinderGoalTargetNearestPlayer;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.util.ItemUtil;

public class RidableSlime extends EntitySlime implements RidableEntity {
    private ControllerWASD controllerWASD;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;
    private int spacebarCharge = 0;
    private int prevSpacebarCharge = 0;
    private float fallDistanceCharge = 0;

    public RidableSlime(World world) {
        this(EntityTypes.SLIME, world);
    }

    public RidableSlime(EntityTypes entityTypes, World world) {
        super(entityTypes, world);
        moveController = new SlimeController(this);
        controllerWASD = new ControllerWASD(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.SLIME;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.SLIME_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
        }
        super.mobTick();
    }

    public void k() {
        super.k();
        if (spacebarCharge == prevSpacebarCharge) {
            spacebarCharge = 0;
        }
        prevSpacebarCharge = spacebarCharge;
    }

    public float getJumpCharge() {
        float charge = 1F;
        if (rider != null && spacebarCharge > 0) {
            charge += 1F * (fallDistanceCharge = (spacebarCharge / 72F));
        } else {
            fallDistanceCharge = 0;
        }
        return charge;
    }

    // jump
    protected void cH() {
        motY = 0.42D * getJumpCharge();
        impulse = true;
    }

    // fall
    public void c(float distance, float damageMultiplier) {
        if (rider != null && fallDistanceCharge > 0) {
            distance = distance - fallDistanceCharge;
        }
        super.c(distance, damageMultiplier);
    }

    public void setRotation(float newYaw, float newPitch) {
        setYawPitch(lastYaw = yaw = newYaw, pitch = newPitch * 0.5F);
        aS = aQ = yaw;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.SLIME_SPEED;
    }

    public EntityPlayer getRider() {
        return rider;
    }

    public EntityPlayer updateRider() {
        return rider = RideableEntity.updateRider(passengers);
    }

    public void useAIController() {
        if (lookController != defaultLookController) {
            lookController = defaultLookController;
        }
    }

    public void useWASDController() {
        if (lookController != blankLookController) {
            lookController = blankLookController;
        }
    }

    // processInteract
    public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        if (passengers.isEmpty() && !entityhuman.isPassenger() && !entityhuman.isSneaking() && ItemUtil.isEmptyOrSaddle(entityhuman)) {
            return enumhand == EnumHand.MAIN_HAND && tryRide(entityhuman);
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }

    public boolean onSpacebar() {
        if (hasSpecialPerm(rider.getBukkitEntity())) {
            spacebarCharge++;
            if (spacebarCharge > 50) {
                spacebarCharge -= 2;
            }
        }
        return false;
    }

    // initEntityAI
    protected void n() {
        this.goalSelector.a(1, new PathfinderGoalSlimeRandomJump(this));
        this.goalSelector.a(2, new PathfinderGoalSlimeNearestPlayer(this));
        this.goalSelector.a(3, new PathfinderGoalSlimeRandomDirection(this));
        this.goalSelector.a(5, new PathfinderGoalSlimeIdle(this));
        this.targetSelector.a(1, new PathfinderGoalTargetNearestPlayer(this));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTargetInsentient(this, EntityIronGolem.class));
    }

    static class PathfinderGoalSlimeIdle extends PathfinderGoal {
        private final RidableSlime slime;

        public PathfinderGoalSlimeIdle(RidableSlime slime) {
            this.slime = slime;
            a(5);
        }

        public boolean a() {
            return true;
        }

        public void e() {
            ((SlimeController) slime.getControllerMove()).a(1.0D);
        }
    }

    static class PathfinderGoalSlimeRandomJump extends PathfinderGoal {
        private final RidableSlime slime;

        public PathfinderGoalSlimeRandomJump(RidableSlime slime) {
            this.slime = slime;
            a(5);
            slime.getNavigation().d(true);
        }

        public boolean a() {
            return slime.isInWater() || slime.ax();
        }

        public void e() {
            if (slime.getRandom().nextFloat() < 0.8F) {
                slime.getControllerJump().a();
            }
            ((SlimeController) slime.getControllerMove()).a(1.2D);
        }
    }

    static class PathfinderGoalSlimeRandomDirection extends PathfinderGoal {
        private final RidableSlime slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public PathfinderGoalSlimeRandomDirection(RidableSlime slime) {
            this.slime = slime;
            a(2);
        }

        public boolean a() {
            return slime.getGoalTarget() == null && (slime.onGround || slime.isInWater() || slime.ax() || slime.hasEffect(MobEffects.LEVITATION));
        }

        public void e() {
            if (--nextRandomizeTime <= 0) {
                nextRandomizeTime = 40 + slime.getRandom().nextInt(60);
                chosenDegrees = (float) slime.getRandom().nextInt(360);
            }
            ((SlimeController) slime.getControllerMove()).a(chosenDegrees, false);
        }
    }

    static class PathfinderGoalSlimeNearestPlayer extends PathfinderGoal {
        private final RidableSlime slime;
        private int growTimer;

        public PathfinderGoalSlimeNearestPlayer(RidableSlime slime) {
            this.slime = slime;
            a(2);
        }

        public boolean a() {
            EntityLiving entityliving = slime.getGoalTarget();
            if (entityliving == null) {
                return false;
            } else if (!entityliving.isAlive()) {
                return false;
            } else {
                return !(entityliving instanceof EntityHuman) || !((EntityHuman) entityliving).abilities.isInvulnerable;
            }
        }

        public void c() {
            growTimer = 300;
            super.c();
        }

        public boolean b() {
            EntityLiving entityliving = slime.getGoalTarget();
            if (entityliving == null) {
                return false;
            } else if (!entityliving.isAlive()) {
                return false;
            } else if (entityliving instanceof EntityHuman && ((EntityHuman) entityliving).abilities.canInstantlyBuild) {
                return false;
            } else {
                return --growTimer > 0;
            }
        }

        public void e() {
            slime.a(slime.getGoalTarget(), 10.0F, 10.0F);
            slime.getControllerMove().a(slime.yaw, slime.du());
        }
    }

    static class SlimeController extends ControllerMove {
        private float yRot;
        private int jumpDelay;
        private final RidableSlime slime;
        private boolean isAggressive;

        public SlimeController(RidableSlime slime) {
            super(slime);
            this.slime = slime;
            this.yRot = 180.0F * slime.yaw / 3.1415927F;
        }

        public void a(float yRot, boolean aggressive) {
            this.yRot = yRot;
            this.isAggressive = aggressive;
        }

        public void a(double speed) {
            e = speed;
            h = ControllerMove.Operation.MOVE_TO;
        }

        public void a() {
            EntityPlayer rider = slime.getRider();
            if (rider != null) {
                slime.controllerWASD.a();
                if (slime.bj != 0 || slime.bh != 0) {
                    if (jumpDelay > 10) {
                        jumpDelay = 6;
                    }
                } else {
                    jumpDelay = 20;
                }
            } else {
                a.yaw = a(a.yaw, yRot, 90.0F);
                a.aS = a.yaw;
                a.aQ = a.yaw;
            }
            if (rider == null && h != ControllerMove.Operation.MOVE_TO) {
                a.r(0.0F);
            } else {
                h = ControllerMove.Operation.WAIT;
                if (a.onGround) {
                    if (rider != null) {
                        if (slime.bj != 0 || slime.bh != 0) {
                            a.o((float) (e * slime.getSpeed()));
                        }
                    }
                    if (jumpDelay-- <= 0) {
                        jumpDelay = slime.dr();
                        if (isAggressive) {
                            jumpDelay /= 3;
                        }
                        slime.getControllerJump().a();
                        if (slime.dz()) {
                            slime.a(slime.dw(), slime.cD(), ((slime.getRandom().nextFloat() - slime.getRandom().nextFloat()) * 0.2F + 1.0F) * 0.8F);
                        }
                    } else {
                        slime.bh = 0.0F;
                        slime.bj = 0.0F;
                        a.o(0.0F);
                    }
                } else {
                    a.o((float) (e * (rider != null ? slime.getSpeed() : slime.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue())));
                }
            }
        }
    }
}
