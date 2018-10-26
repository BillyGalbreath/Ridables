package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityVex;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.util.ItemUtil;

public class RidableVex extends EntityVex implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASDVex wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    public RidableVex(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASDVex(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.VEX;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.VEX_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
            setNoGravity(true);
        }
        super.mobTick();
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
        return 0;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.VEX_SPEED;
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

    // fall
    public void c(float f, float f1) {
        // no fall damage
    }

    class ControllerWASDVex extends ControllerWASDFlying {
        ControllerWASDVex(EntityInsentient entity) {
            super(entity);
        }

        // onUpdate
        public void a() {
            EntityPlayer rider = ridable.getRider();
            if (rider == null) {
                ridable.useAIController();
                return;
            }

            // do not target anything while being ridden
            a.setGoalTarget(null, null, false);

            // rotation
            ridable.setRotation(rider.yaw, rider.pitch);

            // controls
            float forward = rider.bj;
            float vertical = forward == 0 ? 0 : -(rider.pitch / 45);
            float strafe = rider.bh;

            if (forward < 0) {
                forward *= 0.5;
                strafe *= 0.5;
                vertical *= -0.25;
            }

            // jump
            if (isJumping(rider)) {
                ridable.onSpacebar();
            }

            if (a.locY >= Config.FLYING_MAX_Y) {
                a.motY = -0.05F;
                vertical = 0;
                forward = 0;
                strafe = 0;
            }
            if (a.locY <= 0) {
                a.motY = +0.05F;
                vertical = 0;
                forward = 0;
                strafe = 0;
            }

            a.motX *= 0.95F;
            a.motY *= 0.9F;
            a.motZ *= 0.95F;

            float speed = ridable.getSpeed();
            if (a.onGround) {
                speed *= 0.05F;
            }

            a.o((float) (e = speed));
            a.s(vertical);
            a.t(strafe);
            a.r(forward);

            f = a.bj;
            g = a.bh;

            a.noclip = Config.VEX_NOCLIP;
        }
    }
}
