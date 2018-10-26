package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityTurtle;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDWater;
import net.pl3x.bukkit.ridables.util.ItemUtil;

public class RidableTurtle extends EntityTurtle implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASD wasdControllerLand;
    private ControllerWASDWater wasdControllerWater;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    public RidableTurtle(World world) {
        super(world);
        persistent = true;
        aiController = moveController;
        wasdControllerLand = new ControllerWASD(this);
        wasdControllerWater = new ControllerWASDWater(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.TURTLE;
    }

    @Override
    public boolean isTypeNotPersistent() {
        return false;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return true;
    }

    protected void mobTick() {
        Q = Config.TURTLE_STEP_HEIGHT;
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
            if (isInWater()) {
                motY += 0.005D;
            }
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
        return 0.4F;
    }

    public float getSpeed() {
        float speed = (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        return speed * (isInWater() ? Config.TURTLE_SPEED_WATER * 0.5F : Config.TURTLE_SPEED_LAND * 0.075F);
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
        if (isInWater()) {
            if (moveController != wasdControllerWater) {
                moveController = wasdControllerWater;
                lookController = blankLookController;
            }
        } else {
            if (moveController != wasdControllerLand) {
                moveController = wasdControllerLand;
                lookController = blankLookController;
            }
        }
    }

    // processInteract
    public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        if (passengers.isEmpty() && !entityhuman.isPassenger() && !entityhuman.isSneaking() && ItemUtil.isEmptyOrSaddle(entityhuman)) {
            return enumhand == EnumHand.MAIN_HAND && tryRide(entityhuman);
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }
}
