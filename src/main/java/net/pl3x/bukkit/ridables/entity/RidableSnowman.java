package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityLiving;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntitySnowman;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.data.MaterialSetTag;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class RidableSnowman extends EntitySnowman implements RidableEntity {
    public static final MaterialSetTag PUMPKIN = new MaterialSetTag()
            .add(Material.CARVED_PUMPKIN, Material.JACK_O_LANTERN, Material.PUMPKIN);

    private ControllerMove aiController;
    private ControllerWASD wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;

    public RidableSnowman(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASD(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.SNOWMAN;
    }

    public boolean isTypeNotPersistent() {
        return false;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return false;
    }

    protected void mobTick() {
        Q = Config.SNOWMAN_STEP_HEIGHT;
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
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
        return Config.SNOWMAN_JUMP_POWER;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.SNOWMAN_SPEED;
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
        net.minecraft.server.v1_13_R2.ItemStack itemstack = entityhuman.b(enumhand);
        if (!hasPumpkin() && PUMPKIN.isTagged(CraftItemStack.asCraftMirror(itemstack))) {
            setHasPumpkin(true);
            if (!entityhuman.abilities.canInstantlyBuild) {
                itemstack.subtract(1);
                return true;
            }
        } else if (hasPumpkin() && itemstack.getItem() == Items.SHEARS) {
            ((Entity) this).getBukkitEntity().getWorld().dropItemNaturally(getBukkitEntity().getLocation(),
                    new ItemStack(Material.CARVED_PUMPKIN));
            return true;
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }

    // onLivingUpdate
    public void k() {
        super.k();
        int x = MathHelper.floor(locX);
        int y = MathHelper.floor(locY);
        int z = MathHelper.floor(locZ);
        if (ap() && Config.SNOWMAN_DAMAGE_WHEN_WET) { // isWet
            damageEntity(DamageSource.DROWN, 1.0F);
        }
        if (world.getBiome(new BlockPosition(x, 0, z)).c(new BlockPosition(x, y, z)) > 1.0F && Config.SNOWMAN_DAMAGE_WHEN_HOT) { // biome.getTemperature(pos)
            damageEntity(CraftEventFactory.MELTING, 1.0F);
        }
        if (!(world.getGameRules().getBoolean("mobGriefing") && Config.SNOWMAN_LEAVE_SNOW_TRAIL)) {
            return; // not allowed to grief world (placing snow layers where walking)
        }
        IBlockData block = Blocks.SNOW.getBlockData();
        for (int l = 0; l < 4; ++l) {
            x = MathHelper.floor(locX + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
            y = MathHelper.floor(locY);
            z = MathHelper.floor(locZ + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
            BlockPosition pos = new BlockPosition(x, y, z);
            if (world.getType(pos).isAir() && world.getBiome(pos).c(pos) < 0.8F && block.canPlace(world, pos)) {
                CraftEventFactory.handleBlockFormEvent(world, pos, block, this);
            }
        }
    }

    // attackEntityWithRangedAttack
    public void a(EntityLiving target, float distanceFactor) {
        if (getRider() == null) {
            super.a(target, distanceFactor);
        }
    }
}
