package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHorseSkeleton;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.listener.RidableListener;
import net.pl3x.bukkit.ridables.util.ItemUtil;

public class EntityRidableSkeletonHorse extends EntityHorseSkeleton implements RidableEntity {
    private EntityPlayer rider;

    public EntityRidableSkeletonHorse(World world) {
        super(world);
    }

    public RidableType getType() {
        return RidableType.SKELETON_HORSE;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return true;
    }

    public void mobTick() {
        updateRider();
        super.mobTick();
    }

    public void setRotation(float newYaw, float newPitch) {
    }

    public EntityPlayer getRider() {
        return rider;
    }

    public EntityPlayer updateRider() {
        if (passengers == null || passengers.isEmpty()) {
            rider = null;
        } else {
            Entity entity = passengers.get(0);
            rider = entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
        }
        return rider;
    }

    public void useAIController() {
    }

    public void useWASDController() {
    }

    // processInteract
    public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        if (passengers.isEmpty() && !entityhuman.isPassenger() && !entityhuman.isSneaking() && ItemUtil.isEmptyOrSaddle(entityhuman)) {
            return enumhand == EnumHand.MAIN_HAND && tryRide(entityhuman);
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }

    // mountTo
    protected void g(EntityHuman entityhuman) {
        RidableListener.TP_OVERRIDE.add(entityhuman.getUniqueID());
        super.g(entityhuman);
        RidableListener.TP_OVERRIDE.remove(entityhuman.getUniqueID());
    }

    public boolean isTamed() {
        return true;
    }

    // addSwimmingPathfinder
    protected void dJ() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
    }
}
