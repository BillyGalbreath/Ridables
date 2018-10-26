package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.DragonControllerPhase;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityComplexPart;
import net.minecraft.server.v1_13_R2.EntityEnderDragon;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.EnumMoveType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.Vec3D;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlying;
import net.pl3x.bukkit.ridables.util.ItemUtil;

public class RidableEnderDragon extends EntityEnderDragon implements RidableEntity {
    private EntityPlayer rider;
    private boolean dirty;

    public RidableEnderDragon(World world) {
        super(world);
        moveController = new ControllerWASDFlying(this);
    }

    public RidableType getType() {
        return RidableType.ENDER_DRAGON;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return false;
    }

    // onLivingUpdate
    @Override
    public void k() {
        EntityPlayer rider = updateRider();
        if (rider != null) {
            if (!dirty) {
                dirty = true;
                noclip = false;
                setSize(4.0F, 2.0F);
            }

            setRotation(rider.yaw, rider.pitch);

            moveController.a(); // ender dragon doesnt use the controller so call manually

            a(-bh, bi, -bj, getSpeed() * 0.1F); // moveRelative
            move(EnumMoveType.PLAYER, motX, motY, motZ);

            motX *= 0.9F;
            motY *= 0.9F;
            motZ *= 0.9F;

            // control wing flap speed
            getDragonControllerManager().setControllerPhase(motX * motX + motZ * motZ < 0.005F ? DragonControllerPhase.k : DragonControllerPhase.a);

            // need to tick the body parts so they are damageable
            // this copies the parent method, but strips away a lot of
            // unwanted logic (like self movement, destroying blocks on touch, etc)
            super_k();
            return;
        } else if (dirty) {
            dirty = false;
            noclip = true;
            setSize(16.0F, 8.0F);
            getDragonControllerManager().setControllerPhase(DragonControllerPhase.a); // HoldingPattern
        }
        super.k();
    }

    public void setRotation(float yaw, float pitch) {
        setYawPitch(lastYaw = this.yaw = yaw - 180, this.pitch = pitch * 0.5F);
        aS = aQ = this.yaw;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.DRAGON_SPEED;
    }

    public EntityPlayer getRider() {
        return rider;
    }

    public EntityPlayer updateRider() {
        return rider = RideableEntity.updateRider(passengers);
    }

    public void useAIController() {
    }

    public void useWASDController() {
    }

    public boolean onSpacebar() {
        // TODO flames!
        return true;
    }

    // processInteract
    public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
        if (passengers.isEmpty() && !entityhuman.isPassenger() && !entityhuman.isSneaking() && ItemUtil.isEmptyOrSaddle(entityhuman)) {
            return enumhand == EnumHand.MAIN_HAND && tryRide(entityhuman);
        }
        return passengers.isEmpty() && super.a(entityhuman, enumhand);
    }

    // onLivingUpdate (modified from super class)
    private void super_k() {
        bL = bM; // prevAnimTime = animTime
        if (getHealth() <= 0.0F) {
            return;
        }
        bM += (0.2F / (MathHelper.sqrt(motX * motX + motZ * motZ) * 10.0F + 1.0F)) * (float) Math.pow(2.0D, motY); // animTime
        yaw = MathHelper.g(yaw);
        if (c < 0) { // ringBufferIndex
            for (int i = 0; i < b.length; ++i) {
                b[i][0] = (double) yaw;
                b[i][1] = locY;
            }
        }
        if (++c == b.length) {
            c = 0; // ringBufferIndex
        }
        b[c][0] = (double) yaw;
        b[c][1] = locY;
        aQ = yaw;
        bD.width = 1.0F; // head
        bD.length = 1.0F;
        bE.width = 3.0F; // neck
        bE.length = 3.0F;
        bG.width = 2.0F; // tail1
        bG.length = 2.0F;
        bH.width = 2.0F; // tail2
        bH.length = 2.0F;
        bI.width = 2.0F; // tail3
        bI.length = 2.0F;
        bF.length = 3.0F; // body
        bF.width = 5.0F;
        bJ.length = 2.0F; // wing1
        bJ.width = 4.0F;
        bK.length = 3.0F; // wing2
        bK.width = 4.0F;
        Vec3D[] avec3d = new Vec3D[children.length];
        for (int j = 0; j < children.length; ++j) {
            avec3d[j] = new Vec3D(children[j].locX, children[j].locY, children[j].locZ);
        }
        float f2 = (float) (a(5, 1.0F)[1] - a(10, 1.0F)[1]) * 10.0F * 0.017453292F;
        float f8 = MathHelper.cos(f2);
        float f9 = MathHelper.sin(f2);
        float f10 = yaw * 0.017453292F;
        float f11 = MathHelper.sin(f10);
        float f12 = MathHelper.cos(f10);
        bF.tick(); // body
        bF.setPositionRotation(locX + (double) (f11 * 0.5F), locY, locZ - (double) (f12 * 0.5F), 0.0F, 0.0F);
        bJ.tick(); // wing1
        bJ.setPositionRotation(locX + (double) (f12 * 4.5F), locY + 2.0D, locZ + (double) (f11 * 4.5F), 0.0F, 0.0F);
        bK.tick(); // wing2
        bK.setPositionRotation(locX - (double) (f12 * 4.5F), locY + 2.0D, locZ - (double) (f11 * 4.5F), 0.0F, 0.0F);
        double[] adouble = a(5, 1.0F);
        float f13 = MathHelper.sin(yaw * 0.017453292F - bk * 0.01F);
        float f14 = MathHelper.cos(yaw * 0.017453292F - bk * 0.01F);
        bD.tick(); // head
        bE.tick(); // neck
        float f3 = (float) (a(5, 1.0F)[1] - a(0, 1.0F)[1]);
        bD.setPositionRotation(locX + (double) (f13 * 6.5F * f8), locY + (double) f3 + (double) (f9 * 6.5F), locZ - (double) (f14 * 6.5F * f8), 0.0F, 0.0F);
        bE.setPositionRotation(locX + (double) (f13 * 5.5F * f8), locY + (double) f3 + (double) (f9 * 5.5F), locZ - (double) (f14 * 5.5F * f8), 0.0F, 0.0F);
        for (int k = 0; k < 3; ++k) {
            EntityComplexPart entitycomplexpart = null;
            if (k == 0) entitycomplexpart = bG;
            if (k == 1) entitycomplexpart = bH;
            if (k == 2) entitycomplexpart = bI;
            double[] adouble1 = a(12 + k * 2, 1.0F);
            float f15 = yaw * 0.017453292F + ((float) MathHelper.g(adouble1[0] - adouble[0])) * 0.017453292F;
            float f16 = MathHelper.sin(f15);
            float f17 = MathHelper.cos(f15);
            float f4 = (float) (k + 1) * 2.0F;
            entitycomplexpart.tick();
            entitycomplexpart.setPositionRotation(locX - (double) ((f11 * 1.5F + f16 * f4) * f8), locY + (adouble1[1] - adouble[1]) - (double) ((f4 + 1.5F) * f9) + 1.5D, locZ + (double) ((f12 * 1.5F + f17 * f4) * f8), 0.0F, 0.0F);
        }
        for (int k = 0; k < children.length; ++k) {
            children[k].lastX = avec3d[k].x;
            children[k].lastY = avec3d[k].y;
            children[k].lastZ = avec3d[k].z;
        }
    }
}
