package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.DataWatcherObject;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityAreaEffectCloud;
import net.minecraft.server.v1_13_R2.EntityCreeper;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityOcelot;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.MobEffect;
import net.minecraft.server.v1_13_R2.PathfinderGoalAvoidTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_13_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_13_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_13_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_13_R2.PathfinderGoalSwell;
import net.minecraft.server.v1_13_R2.SoundEffects;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.Ridables;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.data.ServerType;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.lang.reflect.Field;
import java.util.Collection;

public class RidableCreeper extends EntityCreeper implements RidableEntity {
    private static Field ignited_field;
    private static Field fuseTicks_field;
    private static Field lastActive_field;

    static {
        try {
            ignited_field = EntityCreeper.class.getDeclaredField("c");
            ignited_field.setAccessible(true);
            fuseTicks_field = EntityCreeper.class.getDeclaredField("fuseTicks");
            fuseTicks_field.setAccessible(true);
            lastActive_field = EntityCreeper.class.getDeclaredField("bC");
            lastActive_field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private ControllerMove aiController;
    private ControllerWASD wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    private PathfinderGoalNearestAttackableTarget goalTargetPlayer;
    private PathfinderGoalHurtByTarget goalTargetHurtBy;

    public RidableCreeper(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASD(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.CREEPER;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.CREEPER_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        Q = Config.CREEPER_STEP_HEIGHT;
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
            if (rider.bj != 0 || rider.bh != 0) {
                disarm();
            }
        }
        super.mobTick();
    }

    public void tick() {
        if (isAlive()) {
            int fuseTicks = getFuseTicks();
            setLastActive(fuseTicks);
            if (isIgnited()) {
                a(1); // setSwellState
            }
            int state = dz(); // getSwellState
            if (state > 0 && fuseTicks == 0) {
                a(SoundEffects.ENTITY_CREEPER_PRIMED, 1.0F, 0.5F);
            }
            fuseTicks += state;
            if (fuseTicks < 0) {
                setFuseTicks(0);
            }
            if (fuseTicks >= maxFuseTicks) {
                setFuseTicks(maxFuseTicks);
                explode();
            }
        }
        super.tick();
    }

    // getJumpUpwardsMotion
    protected float cG() {
        return super.cG() * getJumpPower() * 2.2F;
    }

    public void setRotation(float newYaw, float newPitch) {
        setYawPitch(lastYaw = yaw = newYaw, pitch = newPitch * 0.5F);
        aS = aQ = yaw;
    }

    public float getJumpPower() {
        return isIgnited() ? 0 : Config.CREEPER_JUMP_POWER;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.CREEPER_SPEED;
    }

    public EntityPlayer getRider() {
        return rider;
    }

    public EntityPlayer updateRider() {
        return rider = RideableEntity.updateRider(passengers);
    }

    public void useAIController() {
        if (moveController != aiController) {
            moveController = aiController;
            lookController = defaultLookController;
            targetSelector.a(1, goalTargetPlayer);
            targetSelector.a(2, goalTargetHurtBy);
        }
    }

    public void useWASDController() {
        if (moveController != wasdController) {
            moveController = wasdController;
            lookController = blankLookController;
            targetSelector.a(goalTargetPlayer);
            targetSelector.a(goalTargetHurtBy);
            disarm();
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
        if (!isIgnited()) {
            EntityPlayer rider = getRider();
            if (rider != null && rider.bj == 0 && rider.bh == 0 && hasSpecialPerm(rider.getBukkitEntity())) {
                ignite(true);
                return true;
            }
        }
        return false;
    }

    // initEntityAI
    protected void n() {
        goalTargetPlayer = new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true);
        goalTargetHurtBy = new PathfinderGoalHurtByTarget(this, false, new Class[0]);

        goalSelector.a(1, new PathfinderGoalFloat(this));
        goalSelector.a(2, new PathfinderGoalSwell(this));
        goalSelector.a(3, new PathfinderGoalAvoidTarget(this, EntityOcelot.class, 6.0F, 1.0D, 1.2D));
        goalSelector.a(4, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 0.8D));
        goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        goalSelector.a(6, new PathfinderGoalRandomLookaround(this));

        targetSelector.a(1, goalTargetPlayer);
        targetSelector.a(2, goalTargetHurtBy);
    }

    public void disarm() {
        ignite(false);
        a(-1); // setSwellState
    }

    /**
     * Set ignited state of a ridable creeper
     *
     * @param ignited Ignited state to set
     */
    public void ignite(boolean ignited) {
        if (ignited) {
            dB();
        } else {
            try {
                getDataWatcher().set((DataWatcherObject<Boolean>) ignited_field.get(this), false);
            } catch (IllegalAccessException ignore) {
            }
        }
    }

    public int getFuseTicks() {
        try {
            return fuseTicks_field.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    public void setFuseTicks(int fuseTicks) {
        try {
            fuseTicks_field.set(this, fuseTicks);
        } catch (IllegalAccessException ignore) {
        }
    }

    public void setLastActive(int lastActive) {
        try {
            lastActive_field.set(this, lastActive);
        } catch (IllegalAccessException ignore) {
        }
    }

    public void explode() {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(((Entity) this).getBukkitEntity(), Config.CREEPER_EXPLOSION_RADIUS * (isPowered() ? 2.0F : 1.0F), false);
        world.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            aX = true; // duplicate of isDead
            boolean flag = getRider() == null ? world.getGameRules().getBoolean("mobGriefing") : Config.CREEPER_EXPLOSION_GRIEF;
            world.createExplosion(this, locX, locY, locZ, event.getRadius(), event.getFire(), flag);
            die();
            spawnCloud();
        } else {
            setFuseTicks(0);
            ignite(false);
        }
    }

    private void spawnCloud() {
        Collection<MobEffect> collection = getEffects();
        if (collection.isEmpty()) {
            return;
        }
        if (Ridables.getInstance().getServerType() == ServerType.PAPER && world.paperConfig.disableCreeperLingeringEffect) {
            return;
        }
        EntityAreaEffectCloud cloud = new EntityAreaEffectCloud(world, locX, locY, locZ);
        cloud.setSource(this);
        cloud.setRadius(2.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(cloud.getDuration() / 2);
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        for (MobEffect mobEffect : collection) {
            cloud.a(new MobEffect(mobEffect));
        }
        world.addEntity(cloud);
    }
}
