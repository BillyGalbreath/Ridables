package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.EnchantmentManager;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPig;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumMoveType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.MobEffects;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class RidablePig extends EntityPig implements RidableEntity {
    private static Field boosting;
    private static Field boostTime;
    private static Field totalBoostTime;

    static {
        try {
            boosting = EntityPig.class.getDeclaredField("bG");
            boosting.setAccessible(true);
            boostTime = EntityPig.class.getDeclaredField("bH");
            boostTime.setAccessible(true);
            totalBoostTime = EntityPig.class.getDeclaredField("bI");
            totalBoostTime.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
        }
    }

    private ControllerMove aiController;
    private ControllerWASD wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    public RidablePig(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASD(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.PIG;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.PIG_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        Q = Config.PIG_STEP_HEIGHT;
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
        }
        super.mobTick();
    }

    public void a(float strafe, float vertical, float forward) {
        if (isVehicle() && dh()) {
            if (Q < 1.0F) {
                Q = 1.0F; // always set to at least 1.0 when riding normally (with saddle and carrot on a stick)
            }
            aU = cK() * 0.1F; // jumpMovementFactor
            boolean boosting = isBoosting();
            int boostTime = incrementBoostTime();
            int totalBoostTime = getTotalBoostTime();
            if (boosting && boostTime > totalBoostTime) {
                disableBoosting();
            }
            if (bT()) { // canPassengerSteer
                float speed = (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * 0.225F;
                if (boosting) {
                    speed += speed * 1.15F * MathHelper.sin((float) boostTime / (float) totalBoostTime * 3.1415927F);
                }
                o(speed);
                super_a(0.0F, 0.0F, 1.0F);
            } else {
                motX = 0.0D;
                motY = 0.0D;
                motZ = 0.0D;
            }
        } else {
            //Q = 0.5F; // disable vanilla's step-height change
            aU = 0.02F; // jumpMovementFactor
            super_a(strafe, vertical, forward);
        }
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
        return Config.PIG_JUMP_POWER;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.PIG_SPEED;
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
        }
    }

    public void useWASDController() {
        if (moveController != wasdController) {
            moveController = wasdController;
            lookController = blankLookController;
        }
    }

    // processInteract
    public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        if (passengers.isEmpty() && !entityhuman.isPassenger()) {
            if (!entityhuman.isSneaking()) {
                if (ItemUtil.isEmptyOrSaddle(entityhuman)) {
                    return enumhand == EnumHand.MAIN_HAND && tryRide(entityhuman);
                }
            } else {
                if (Config.PIG_SADDLE_BACK && hasSaddle() && entityhuman.b(enumhand).getItem() != Items.SADDLE) {
                    setSaddle(false);
                    return !getBukkitEntity().getWorld().dropItemNaturally(getBukkitEntity().getLocation(), new ItemStack(Material.SADDLE)).isEmpty();
                }
            }
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }

    private boolean isBoosting() {
        try {
            return boosting.getBoolean(this);
        } catch (IllegalAccessException ignore) {
        }
        return false;
    }

    private void disableBoosting() {
        try {
            boosting.setBoolean(this, false);
        } catch (IllegalAccessException ignore) {
        }
    }

    private int incrementBoostTime() {
        try {
            boostTime.setInt(this, boostTime.getInt(this) + 1);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    private int getTotalBoostTime() {
        try {
            totalBoostTime.getInt(this);
        } catch (IllegalAccessException ignore) {
        }
        return 0;
    }

    // modified travel method from EntityLiving
    public void super_a(float strafe, float vertical, float forward) {
        double gravity = 0.08D;
        if (motY <= 0.0D && hasEffect(MobEffects.SLOW_FALLING)) {
            gravity = 0.01D;
            fallDistance = 0.0F;
        }
        if (isInWater()) {
            double oldY = locY;
            float speedSlowed = isSprinting() ? 0.9F : cJ();
            float speed = 0.02F;
            float depthStrider = (float) EnchantmentManager.e(this);
            if (depthStrider > 3.0F) {
                depthStrider = 3.0F;
            }
            if (!onGround) {
                depthStrider *= 0.5F;
            }
            if (depthStrider > 0.0F) {
                speedSlowed += (0.54600006F - speedSlowed) * depthStrider / 3.0F;
                speed += (cK() - speed) * depthStrider / 3.0F;
            }
            if (hasEffect(MobEffects.DOLPHINS_GRACE)) {
                speedSlowed = 0.96F;
            }
            a(strafe, vertical, forward, speed);
            move(EnumMoveType.SELF, motX, motY, motZ);
            motX *= (double) speedSlowed;
            motY *= 0.8D;
            motZ *= (double) speedSlowed;
            if (!isNoGravity() && !isSprinting()) {
                if (motY <= 0.0D && Math.abs(motY - 0.005D) >= 0.003D && Math.abs(motY - gravity / 16.0D) < 0.003D) {
                    motY = -0.003D;
                } else {
                    motY -= gravity / 16.0D;
                }
            }
            if (positionChanged && c(motX, motY + 0.6D - locY + oldY, motZ)) {
                motY = 0.3D;
            }
        } else if (ax()) { // isInLava()
            double oldY = locY;
            a(strafe, vertical, forward, 0.02F);
            move(EnumMoveType.SELF, motX, motY, motZ);
            motX *= 0.5D;
            motY *= 0.5D;
            motZ *= 0.5D;
            if (!isNoGravity()) {
                motY -= gravity / 4.0D;
            }
            if (positionChanged && c(motX, motY + 0.6D - locY + oldY, motZ)) {
                motY = 0.3D;
            }
        } else {
            try (BlockPosition.b pos = BlockPosition.b.d(locX, getBoundingBox().minY - 1.0D, locZ)) {
                float friction = onGround ? world.getType(pos).getBlock().n() * 0.91F : 0.91F;
                float speed = onGround ? cK() * (0.16277137F / (friction * friction * friction)) : aU;
                a(strafe, vertical, forward, speed);
                friction = onGround ? world.getType(pos.e(locX, getBoundingBox().minY - 1.0D, locZ)).getBlock().n() * 0.91F : 0.91F;
                if (z_()) { // isOnLadder
                    motX = MathHelper.a(motX, -0.15D, 0.15D);
                    motZ = MathHelper.a(motZ, -0.15D, 0.15D);
                    fallDistance = 0.0F;
                    if (motY < -0.15D) {
                        motY = -0.15D;
                    }
                }
                move(EnumMoveType.SELF, motX, motY, motZ);
                if (positionChanged && z_()) { // isOnLadder
                    motY = 0.2D;
                }
                if (hasEffect(MobEffects.LEVITATION)) {
                    motY += (0.05D * (double) (getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - motY) * 0.2D;
                    fallDistance = 0.0F;
                } else {
                    pos.e(locX, 0.0D, locZ); // setPos
                    if (world.isClientSide && (!world.isLoaded(pos) || !world.getChunkAtWorldCoords(pos).y())) {
                        motY = locY > 0.0D ? -0.1D : 0.0D;
                    } else if (!isNoGravity()) {
                        motY -= gravity;
                    }
                }
                motY *= 0.98D;
                motX *= (double) friction;
                motZ *= (double) friction;
            }
        }
        aI = aJ;
        double x = locX - lastX;
        double z = locZ - lastZ;
        float distance = MathHelper.sqrt(x * x + z * z) * 4.0F;
        if (distance > 1.0F) {
            distance = 1.0F;
        }
        aJ += (distance - aJ) * 0.4F;
        aK += aJ;
    }
}
