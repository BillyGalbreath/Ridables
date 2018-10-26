package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityWither;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumMonsterType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.PathfinderGoalArrowAttack;
import net.minecraft.server.v1_13_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_13_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.projectile.CustomWitherSkull;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;

import java.util.function.Predicate;

public class RidableWither extends EntityWither implements RidableEntity {
    private static final Predicate<Entity> NOT_UNDEAD = (entity) -> entity instanceof EntityLiving &&
            ((EntityLiving) entity).getMonsterType() != EnumMonsterType.UNDEAD && ((EntityLiving) entity).df();

    private ControllerMove aiController;
    private ControllerWASDFlying wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;

    private PathfinderGoalArrowAttack goalArrowAttack;
    private PathfinderGoalRandomStrollLand goalRandomStroll;
    private PathfinderGoalLookAtPlayer goalLookAtPlayer;
    private PathfinderGoalRandomLookaround goalLookAround;
    private PathfinderGoalHurtByTarget goalHurtByTarget;
    private PathfinderGoalNearestAttackableTarget goalNearestTarget;
    private EntityPlayer rider;
    private int shootCooldown = 0;

    public RidableWither(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASDFlying(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.WITHER;
    }

    // initEntityAI
    protected void n() {
        goalArrowAttack = new PathfinderGoalArrowAttack(this, 1.0D, 40, 20.0F);
        goalRandomStroll = new PathfinderGoalRandomStrollLand(this, 1.0D);
        goalLookAtPlayer = new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F);
        goalLookAround = new PathfinderGoalRandomLookaround(this);
        goalHurtByTarget = new PathfinderGoalHurtByTarget(this, false, new Class[0]);
        goalNearestTarget = new PathfinderGoalNearestAttackableTarget(this, EntityInsentient.class, 0, false, false, NOT_UNDEAD);

        goalSelector.a(0, new DoNothing());
        addAI();
    }

    private void addAI() {
        goalSelector.a(2, goalArrowAttack);
        goalSelector.a(5, goalRandomStroll);
        goalSelector.a(6, goalLookAtPlayer);
        goalSelector.a(7, goalLookAround);
        targetSelector.a(1, goalHurtByTarget);
        targetSelector.a(2, goalNearestTarget);
    }

    private void removeAI() {
        goalSelector.a(goalArrowAttack);
        goalSelector.a(goalRandomStroll);
        goalSelector.a(goalLookAtPlayer);
        goalSelector.a(goalLookAround);
        targetSelector.a(goalHurtByTarget);
        targetSelector.a(goalNearestTarget);
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.WITHER_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            super.a(0, 0);
            super.a(1, 0);
            super.a(2, 0);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
        }
        super.mobTick();
    }

    public void a(float strafe, float vertical, float forward, float friction) {
        float speed = 1F;
        if (getRider() != null) {
            speed = 2;
            friction = 0.1F;
        }
        super.a(strafe / speed, vertical * speed, forward / speed, friction);
    }

    public void setRotation(float newYaw, float newPitch) {
        setYawPitch(lastYaw = yaw = newYaw, pitch = newPitch * 0.5F);
        aS = aQ = yaw;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.WITHER_SPEED;
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
            setNoGravity(false);
            addAI();
        }
    }

    public void useWASDController() {
        if (moveController != wasdController) {
            moveController = wasdController;
            lookController = blankLookController;
            removeAI();
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
        return shoot(getRider(), new int[]{0, 1, 2});
    }

    public boolean onClick(org.bukkit.entity.Entity entity, EnumHand hand) {
        return handleClick(hand);
    }

    public boolean onClick(Block block, BlockFace blockFace, EnumHand hand) {
        return handleClick(hand);
    }

    public boolean onClick(EnumHand hand) {
        return handleClick(hand);
    }

    private boolean handleClick(EnumHand hand) {
        return shoot(getRider(), hand == EnumHand.MAIN_HAND ? new int[]{1} : new int[]{2});
    }

    public boolean shoot(EntityPlayer rider, int[] heads) {
        if (shootCooldown > 0) {
            return false;
        }

        shootCooldown = Config.WITHER_SHOOT_COOLDOWN;
        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!hasShootPerm(player)) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Location loc = player.getTargetBlock(null, 120).getLocation();
        for (int head : heads) {
            shoot(head, loc.getX(), loc.getY(), loc.getZ(), rider);
        }

        return true;
    }

    public void shoot(int head, double x, double y, double z, EntityPlayer shooter) {
        world.a(null, 1024, new BlockPosition(this), 0);
        double headX = getHeadX(head);
        double headY = getHeadY(head);
        double headZ = getHeadZ(head);
        CustomWitherSkull skull = new CustomWitherSkull(world, this, shooter, x - headX, y - headY, z - headZ);
        skull.locY = headY;
        skull.locX = headX;
        skull.locZ = headZ;
        world.addEntity(skull);
    }

    public double getHeadX(int i) {
        return i <= 0 ? locX : locX + (double) MathHelper.cos((aQ + (float) (180 * (i - 1))) * 0.017453292F) * 1.3D;
    }

    public double getHeadY(int i) {
        return i <= 0 ? locY + 3.0D : locY + 2.2D;
    }

    public double getHeadZ(int i) {
        return i <= 0 ? locZ : locZ + (double) MathHelper.sin((aQ + (float) (180 * (i - 1))) * 0.017453292F) * 1.3D;
    }

    public int p(int i) {
        return getRider() != null ? 0 : super.p(i);
    }

    public void a(int i, int j) {
        if (getRider() == null) {
            super.a(i, j);
        }
    }

    private class DoNothing extends PathfinderGoal {
        DoNothing() {
            a(7);
        }

        public boolean a() {
            return dz() > 0;
        }
    }
}
