package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPhantom;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumMoveType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.SoundEffectType;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.entity.projectile.PhantomFlames;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.entity.Phantom;
import org.bukkit.util.Vector;

public class RidablePhantom extends EntityPhantom implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASDFlying wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    public RidablePhantom(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASDFlying(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.PHANTOM;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.PHANTOM_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
            if (rider.bj == 0) {
                motY -= Config.PHANTOM_GRAVITY;
            } else {
                fallDistance = 0;
            }
        } else {
            fallDistance = 0;
        }
        super.mobTick();
    }

    public void setRotation(float newYaw, float newPitch) {
        setYawPitch(lastYaw = yaw = newYaw, pitch = -newPitch * 0.75F);
        aS = aQ = yaw;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.PHANTOM_SPEED * (onGround ? 1 : 3F);
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
        if (passengers.isEmpty() && !entityhuman.isPassenger() && !entityhuman.isSneaking() && ItemUtil.isEmptyOrSaddle(entityhuman)) {
            return enumhand == EnumHand.MAIN_HAND && tryRide(entityhuman);
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }

    public boolean onSpacebar() {
        EntityPlayer rider = getRider();
        if (rider != null && hasShootPerm(rider.getBukkitEntity())) {
            shoot(getRider());
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        Location loc = ((Phantom) ((Entity) this).getBukkitEntity()).getEyeLocation();
        loc.setPitch(-loc.getPitch());
        Vector target = loc.getDirection().normalize().multiply(100).add(loc.toVector());

        PhantomFlames flames = new PhantomFlames(world, this, rider);
        flames.shoot(target.getX() - locX, target.getY() - locY, target.getZ() - locZ, 1.0F, 5.0F);
        world.addEntity(flames);
        return true;
    }

    // onLivingUpdate
    public void k() {
        super.k();
        if (!Config.PHANTOM_BURN_IN_SUNLIGHT) {
            extinguish(); // dont burn in sunlight
        }
    }

    // updateFallState
    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {
        if (flag) {
            if (Config.PHANTOM_FALL_DAMAGE && fallDistance > 0.0F) {
                iblockdata.getBlock().fallOn(world, blockposition, this, fallDistance);
            }
            fallDistance = 0.0F;
        } else if (d0 < 0.0D) {
            fallDistance = (float) ((double) fallDistance - (d0 / 2));
        }
    }

    // fall
    public void c(float f, float f1) {
        super.c(f, f1);
        int i = (int) (MathHelper.f((f - 3.0F) * f1) * -(motY));
        if (i > 0) {
            if (!damageEntity(DamageSource.FALL, (float) i)) {
                return;
            }
            a(m(i), 1.0F, 1.0F);
            int j = MathHelper.floor(locX);
            int k = MathHelper.floor(locY - 0.2D);
            int l = MathHelper.floor(locZ);
            IBlockData iblockdata = world.getType(new BlockPosition(j, k, l));
            if (!iblockdata.isAir()) {
                SoundEffectType soundeffecttype = iblockdata.getBlock().getStepSound();
                a(soundeffecttype.g(), soundeffecttype.a() * 0.5F, soundeffecttype.b() * 0.75F);
            }
        }
    }

    // travel
    public void a(float strafe, float vertical, float forward) {
        if (getRider() == null) {
            super.a(strafe, vertical, forward);
            return;
        }

        a(strafe, vertical, forward, 0.02F * getSpeed());
        move(EnumMoveType.PLAYER, motX, motY, motZ);

        motX *= 0.91F;
        motY *= 0.91F;
        motZ *= 0.91F;

        aI = aJ;
        double d0 = locX - lastX;
        double d1 = locZ - lastZ;
        float f = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        aJ += (f - aJ) * 0.4F;
        aK += aJ;
    }
}
