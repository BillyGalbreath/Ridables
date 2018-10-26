package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityChicken;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityItem;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.SoundEffects;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityDropItemEvent;

public class RidableChicken extends EntityChicken implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASD wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;
    private int timeUntilNextEgg;

    public RidableChicken(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASD(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
        calculateNewTimeUntilNextEgg();
    }

    public RidableType getType() {
        return RidableType.CHICKEN;
    }

    public boolean isTypeNotPersistent() {
        return isChickenJockey();
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.CHICKEN_RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        Q = Config.CHICKEN_STEP_HEIGHT;
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
        }
        super.mobTick();
    }

    // onLivingUpdate
    public void k() {
        bI = 6000; // disable vanilla timeUntilNextEgg tick counter;
        if (rider == null || Config.CHICKEN_DROP_EGGS_WHILE_RIDING) {
            timeUntilNextEgg--;
        }
        if (!isBaby() && !isChickenJockey() && timeUntilNextEgg <= 0) {
            a(SoundEffects.ENTITY_CHICKEN_EGG, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            EntityItem egg = new EntityItem(world, locX, locY, locZ, new ItemStack(Items.EGG));
            egg.n(); // set 10 tick pickup delay
            EntityDropItemEvent event = new EntityDropItemEvent(getBukkitEntity(), (org.bukkit.entity.Item) egg.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                world.addEntity(egg);
            }
            calculateNewTimeUntilNextEgg();
        }
        super.k();
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
        return Config.CHICKEN_JUMP_POWER * 1.35F; // jump ~2.5 blocks
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.CHICKEN_SPEED;
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

    // readNBT
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKey("EggLayTime")) {
            timeUntilNextEgg = nbttagcompound.getInt("EggLayTime");
        }
    }

    // writeNBT
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("EggLayTime", timeUntilNextEgg);
    }

    private void calculateNewTimeUntilNextEgg() {
        timeUntilNextEgg = random.nextInt((Config.CHICKEN_EGG_DELAY_MAX - Config.CHICKEN_EGG_DELAY_MIN) + 1) + Config.CHICKEN_EGG_DELAY_MIN;
    }
}
