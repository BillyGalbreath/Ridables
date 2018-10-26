package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityDolphin;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.Particles;
import net.minecraft.server.v1_13_R2.SoundEffects;
import net.minecraft.server.v1_13_R2.World;
import net.minecraft.server.v1_13_R2.WorldServer;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDWater;
import net.pl3x.bukkit.ridables.entity.projectile.DolphinSpit;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.util.Vector;

public class RidableDolphin extends EntityDolphin implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASDWater wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;
    private int bounceCounter = 0;
    private boolean bounceUp = false;
    private int spacebarCooldown = 0;
    private boolean dashing = false;
    private int dashCounter = 0;

    public RidableDolphin(World world) {
        super(world);
        if (Config.DOLPHIN_PERSISTENT) {
            persistent = true;
        }
        aiController = moveController;
        wasdController = new ControllerWASDWater(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.DOLPHIN;
    }

    public boolean isTypeNotPersistent() {
        return !Config.DOLPHIN_PERSISTENT;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return true;
    }

    protected void mobTick() {
        if (++bounceCounter > 10) {
            bounceCounter = 0;
            bounceUp = !bounceUp;
        }
        if (spacebarCooldown > 0) {
            spacebarCooldown--;
        }

        EntityPlayer rider = updateRider();
        if (rider != null && getAirTicks() > 150) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();

            if (dashing) {
                if (++dashCounter > Config.DOLPHIN_DASH_DURATION) {
                    dashCounter = 0;
                    dashing = false;
                }
            }

            if (isInWater()) {
                if (Config.DOLPHIN_BUBBLES) {
                    double velocity = motX * motX + motY * motY + motZ * motZ;
                    if (velocity > 0.2 || velocity < -0.2) {
                        int i = (int) (velocity * 5);
                        for (int j = 0; j < i; j++) {
                            ((WorldServer) world).sendParticles(null, Particles.e,
                                    lastX + random.nextFloat() / 2 - 0.25F,
                                    lastY + random.nextFloat() / 2 - 0.25F,
                                    lastZ + random.nextFloat() / 2 - 0.25F,
                                    1, 0, 0, 0, 0, true);
                        }
                    }
                }

                motY += 0.005D;
                if (Config.DOLPHIN_BOUNCE && rider.bj == 0) {
                    motY += bounceUp ? 0.005D : -0.005D;
                }
            }
        } else {
            useAIController();
        }
        super.mobTick();
    }

    // travel
    public void a(float strafe, float vertical, float forward) {
        EntityPlayer rider = getRider();
        if (rider != null && !isInWater()) {
            forward = rider.bj;
            strafe = rider.bh;
        }
        super.a(strafe, vertical, forward);
    }

    public void setRotation(float newYaw, float newPitch) {
        setYawPitch(lastYaw = yaw = newYaw, pitch = newPitch * 0.5F);
        aS = aQ = yaw;
    }

    public float getSpeed() {
        float speed = (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue();
        return speed * Config.DOLPHIN_SPEED * (dashing ? Config.DOLPHIN_DASH_BOOST : 1) * 0.2F;
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
        if (spacebarCooldown == 0 && Config.DOLPHIN_SPACEBAR_MODE != null) {
            EntityPlayer rider = getRider();
            if (rider != null) {
                if (Config.DOLPHIN_SPACEBAR_MODE.equalsIgnoreCase("shoot")) {
                    return shoot(rider);
                } else if (Config.DOLPHIN_SPACEBAR_MODE.equalsIgnoreCase("dash")) {
                    return dash(rider);
                }
            }
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        spacebarCooldown = Config.DOLPHIN_SHOOT_COOLDOWN;
        if (rider == null) {
            return false;
        }

        CraftPlayer player = (CraftPlayer) ((Entity) rider).getBukkitEntity();
        if (!hasShootPerm(player)) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Location loc = player.getEyeLocation();
        loc.setPitch(loc.getPitch() - 10);
        Vector target = loc.getDirection().normalize().multiply(10).add(loc.toVector());

        DolphinSpit spit = new DolphinSpit(world, this, rider);
        spit.shoot(target.getX() - locX, target.getY() - locY, target.getZ() - locZ, Config.DOLPHIN_SHOOT_SPEED, 5.0F);
        world.addEntity(spit);

        a(SoundEffects.ENTITY_DOLPHIN_ATTACK, 1.0F, 1.0F);
        return true;
    }

    public boolean dash(EntityPlayer rider) {
        spacebarCooldown = Config.DOLPHIN_DASH_COOLDOWN;
        if (!dashing) {
            if (rider != null && !hasSpecialPerm(rider.getBukkitEntity())) {
                return false;
            }

            dashing = true;
            dashCounter = 0;
            a(SoundEffects.ENTITY_DOLPHIN_JUMP, 1.0F, 1.0F);
            return true;
        }
        return false;
    }
}
