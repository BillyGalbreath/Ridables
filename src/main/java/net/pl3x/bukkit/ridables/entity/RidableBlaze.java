package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityBlaze;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.SoundEffects;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Lang;
import net.pl3x.bukkit.ridables.configuration.mob.BlazeConfig;
import net.pl3x.bukkit.ridables.entity.ai.AIAttackNearest;
import net.pl3x.bukkit.ridables.entity.ai.AIHurtByTarget;
import net.pl3x.bukkit.ridables.entity.ai.AILookIdle;
import net.pl3x.bukkit.ridables.entity.ai.AIMoveTowardsRestriction;
import net.pl3x.bukkit.ridables.entity.ai.AIWanderAvoidWater;
import net.pl3x.bukkit.ridables.entity.ai.AIWatchClosest;
import net.pl3x.bukkit.ridables.entity.ai.blaze.AIBlazeFireballAttack;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASDFlyingWithSpacebar;
import net.pl3x.bukkit.ridables.entity.controller.LookController;
import net.pl3x.bukkit.ridables.entity.projectile.CustomFireball;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.util.Vector;

public class RidableBlaze extends EntityBlaze implements RidableEntity {
    public static final BlazeConfig CONFIG = new BlazeConfig();

    private int shootCooldown = 0;

    public RidableBlaze(World world) {
        super(world);
        moveController = new ControllerWASDFlyingWithSpacebar(this);
        lookController = new LookController(this);
    }

    public RidableType getType() {
        return RidableType.BLAZE;
    }

    // initAI - override vanilla AI
    protected void n() {
        goalSelector.a(4, new AIBlazeFireballAttack(this));
        goalSelector.a(5, new AIMoveTowardsRestriction(this, 1.0D));
        goalSelector.a(7, new AIWanderAvoidWater(this, 1.0D, 0.0F));
        goalSelector.a(8, new AIWatchClosest(this, EntityHuman.class, 8.0F));
        goalSelector.a(8, new AILookIdle(this));
        targetSelector.a(1, new AIHurtByTarget(this, true));
        targetSelector.a(2, new AIAttackNearest<>(this, EntityHuman.class, true));
    }


    // canBeRiddenInWater
    public boolean aY() {
        return CONFIG.RIDABLE_IN_WATER;
    }

    protected void mobTick() {
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        if (getRider() != null) {
            motY += bi > 0 ? 0.07F * CONFIG.VERTICAL : 0.04704F - CONFIG.GRAVITY;
        }
        super.mobTick();
    }

    public float getSpeed() {
        return CONFIG.SPEED;
    }

    // processInteract
    public boolean a(EntityHuman player, EnumHand hand) {
        return super.a(player, hand) || processInteract(player, hand);
    }

    // removePassenger
    public boolean removePassenger(Entity passenger) {
        return dismountPassenger(passenger.getBukkitEntity()) && super.removePassenger(passenger);
    }

    public boolean onClick(org.bukkit.entity.Entity entity, EnumHand hand) {
        return handleClick();
    }

    public boolean onClick(Block block, BlockFace blockFace, EnumHand hand) {
        return handleClick();
    }

    public boolean onClick(EnumHand hand) {
        return handleClick();
    }

    private boolean handleClick() {
        if (shootCooldown == 0) {
            EntityPlayer rider = getRider();
            if (rider != null) {
                return shoot(rider);
            }
        }
        return false;
    }

    public boolean shoot(EntityPlayer rider) {
        shootCooldown = CONFIG.SHOOT_COOLDOWN;

        if (rider == null) {
            return false;
        }

        CraftPlayer player = rider.getBukkitEntity();
        if (!hasShootPerm(player)) {
            Lang.send(player, Lang.SHOOT_NO_PERMISSION);
            return false;
        }

        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(25).add(new Vector(0, 1, 0));

        CustomFireball fireball = new CustomFireball(world, this, rider, direction.getX(), direction.getY(), direction.getZ(),
                CONFIG.SHOOT_SPEED, CONFIG.SHOOT_DAMAGE, CONFIG.SHOOT_GRIEF);
        world.addEntity(fireball);

        a(SoundEffects.ENTITY_BLAZE_SHOOT, 1.0F, 1.0F);

        return true;
    }
}
