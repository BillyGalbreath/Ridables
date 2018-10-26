package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityBat;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlyingWithSpacebar;
import net.pl3x.bukkit.ridables.util.ItemUtil;

public class RidableBat extends EntityBat implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASDFlyingWithSpacebar wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    public RidableBat(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASDFlyingWithSpacebar(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.BAT;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.BAT_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
            motY += bi > 0 ? 0.07F * Config.BAT_VERTICAL : 0.04704F - Config.BAT_GRAVITY;
            return;
        }
        super.mobTick();
    }

    public void setRotation(float newYaw, float newPitch) {
        setYawPitch(lastYaw = yaw = newYaw, pitch = newPitch * 0.5F);
        aS = aQ = yaw;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.BAT_SPEED;
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
}
