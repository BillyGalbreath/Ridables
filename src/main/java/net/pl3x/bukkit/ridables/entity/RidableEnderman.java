package net.pl3x.bukkit.ridables.entity;

import net.minecraft.server.v1_13_R2.Block;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.Blocks;
import net.minecraft.server.v1_13_R2.ControllerLook;
import net.minecraft.server.v1_13_R2.ControllerMove;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityEnderman;
import net.minecraft.server.v1_13_R2.EntityEndermite;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EnumHand;
import net.minecraft.server.v1_13_R2.FluidCollisionOption;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.MathHelper;
import net.minecraft.server.v1_13_R2.MovingObjectPosition;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_13_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_13_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_13_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_13_R2.TagsBlock;
import net.minecraft.server.v1_13_R2.Vec3D;
import net.minecraft.server.v1_13_R2.World;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.controller.BlankLookController;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.util.ItemUtil;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R2.event.CraftEventFactory;

import java.util.function.Predicate;

public class RidableEnderman extends EntityEnderman implements RidableEntity {
    private ControllerMove aiController;
    private ControllerWASD wasdController;
    private ControllerLook defaultLookController;
    private BlankLookController blankLookController;
    private EntityPlayer rider;
    private boolean skipTP;

    public RidableEnderman(World world) {
        super(world);
        aiController = moveController;
        wasdController = new ControllerWASD(this);
        defaultLookController = lookController;
        blankLookController = new BlankLookController(this);
    }

    public RidableType getType() {
        return RidableType.ENDERMAN;
    }

    // canBeRiddenInWater
    public boolean aY() {
        return Config.ENDERMAN_RIDABLE_IN_WATER && !Config.ENDERMAN_EJECT_WHEN_WET;
    }

    protected void mobTick() {
        Q = Config.ENDERMAN_STEP_HEIGHT;
        EntityPlayer rider = updateRider();
        if (rider != null) {
            setGoalTarget(null, null, false);
            setRotation(rider.yaw, rider.pitch);
            useWASDController();
            if (ap()) { // isWet
                if (Config.ENDERMAN_EJECT_WHEN_WET) {
                    ejectPassengers();
                }
                if (Config.ENDERMAN_DAMAGE_WHEN_WET) {
                    damageEntity(DamageSource.DROWN, 1.0F);
                }
            }
            return;
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
        return Config.ENDERMAN_JUMP_POWER * 1.5F;
    }

    public float getSpeed() {
        return (float) getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() * Config.ENDERMAN_SPEED;
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

    public boolean onClick(org.bukkit.block.Block block, BlockFace blockFace, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            return false; // ignore left clicks
        }

        EntityPlayer rider = getRider();
        if (rider == null || !hasSpecialPerm(rider.getBukkitEntity())) {
            return false;
        }

        if (getCarried() == null) {
            return pickUpBlock(block.getX(), block.getY(), block.getZ());
        }

        block = block.getRelative(blockFace);
        return placeBlock(block.getX(), block.getY(), block.getZ());
    }

    public boolean pickUpBlock(int x, int y, int z) {
        BlockPosition pos = new BlockPosition(x, y, z);
        IBlockData state = world.getType(pos);
        Block block = state.getBlock();
        MovingObjectPosition movingobjectposition = world.rayTrace(new Vec3D((double) ((float) MathHelper.floor(locX) + 0.5F), (double) ((float) y + 0.5F), (double) ((float) MathHelper.floor(locZ) + 0.5F)), new Vec3D((double) ((float) x + 0.5F), (double) ((float) y + 0.5F), (double) ((float) z + 0.5F)), FluidCollisionOption.NEVER, true, false);
        boolean flag = movingobjectposition != null && movingobjectposition.a().equals(pos);
        if (block.a(TagsBlock.ENDERMAN_HOLDABLE) && flag) {
            if (!CraftEventFactory.callEntityChangeBlockEvent(this, pos, Blocks.AIR.getBlockData()).isCancelled()) {
                world.setAir(pos);
                setCarried(Block.b(state, world, pos));
                return true;
            }
        }
        return false;
    }

    public boolean placeBlock(int x, int y, int z) {
        BlockPosition pos = new BlockPosition(x, y, z);
        IBlockData state = world.getType(pos);
        IBlockData state1 = world.getType(pos.down());
        IBlockData state2 = Block.b(getCarried(), world, pos);
        if (state2 != null && state.isAir() && !state1.isAir() && state1.g() && state2.canPlace(world, pos)) {
            if (!CraftEventFactory.callEntityChangeBlockEvent(this, pos, state2).isCancelled()) {
                world.setTypeAndData(pos, state2, 3);
                setCarried(null);
                return true;
            }
        }
        return false;
    }

    // randomlyTeleport
    protected boolean dz() {
        return skipTP || super.dz();
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        skipTP = !Config.ENDERMAN_TELEPORT_WHEN_DAMAGED;
        boolean result = super.damageEntity(damagesource, f);
        skipTP = false;
        return result;
    }

    // initEntityAI
    protected void n() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.0D, false));
        goalSelector.a(7, new PathfinderGoalRandomStrollLand(this, 1.0D, 0.0F));
        goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        goalSelector.a(10, new PathfinderGoalEndermanPlaceBlock(this));
        goalSelector.a(11, new PathfinderGoalEndermanPickupBlock(this));
        targetSelector.a(1, new PathfinderGoalPlayerWhoLookedAtTarget(this));
        targetSelector.a(2, new PathfinderGoalHurtByTarget(this, false));
        targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityEndermite.class, 10, true, false, (Predicate<EntityEndermite>) EntityEndermite::l));
    }

    static class PathfinderGoalEndermanPickupBlock extends PathfinderGoal {
        private final RidableEnderman enderman;

        public PathfinderGoalEndermanPickupBlock(RidableEnderman enderman) {
            this.enderman = enderman;
        }

        public boolean a() {
            if (enderman.getRider() != null) {
                return false;
            } else if (enderman.getCarried() != null) {
                return false;
            } else if (!enderman.world.getGameRules().getBoolean("mobGriefing")) {
                return false;
            } else {
                return enderman.getRandom().nextInt(20) == 0;
            }
        }

        public void e() {
            int x = MathHelper.floor(enderman.locX - 2.0D + enderman.random.nextDouble() * 4.0D);
            int y = MathHelper.floor(enderman.locY + enderman.random.nextDouble() * 3.0D);
            int z = MathHelper.floor(enderman.locZ - 2.0D + enderman.random.nextDouble() * 4.0D);
            enderman.pickUpBlock(x, y, z);
        }
    }

    static class PathfinderGoalEndermanPlaceBlock extends PathfinderGoal {
        private final RidableEnderman enderman;

        public PathfinderGoalEndermanPlaceBlock(RidableEnderman enderman) {
            this.enderman = enderman;
        }

        public boolean a() {
            if (enderman.getRider() != null) {
                return false;
            } else if (enderman.getCarried() == null) {
                return false;
            } else if (!enderman.world.getGameRules().getBoolean("mobGriefing")) {
                return false;
            } else {
                return enderman.getRandom().nextInt(2000) == 0;
            }
        }

        public void e() {
            int x = MathHelper.floor(enderman.locX - 1.0D + enderman.random.nextDouble() * 2.0D);
            int y = MathHelper.floor(enderman.locY + enderman.random.nextDouble() * 2.0D);
            int z = MathHelper.floor(enderman.locZ - 1.0D + enderman.random.nextDouble() * 2.0D);
            enderman.placeBlock(x, y, z);
        }
    }

    static class PathfinderGoalPlayerWhoLookedAtTarget extends PathfinderGoalNearestAttackableTarget<EntityHuman> {
        private final RidableEnderman enderman;

        private EntityHuman player;
        private int aggroTime;
        private int teleportTime;

        public PathfinderGoalPlayerWhoLookedAtTarget(RidableEnderman enderman) {
            super(enderman, EntityHuman.class, false);
            this.enderman = enderman;
        }

        public boolean a() {
            if (enderman.getRider() != null) {
                return false;
            }
            double d0 = i();
            player = enderman.world.a(enderman.locX, enderman.locY, enderman.locZ, d0, d0, null, player -> player != null && shouldAttack(player));
            return player != null;
        }

        public void c() {
            aggroTime = 5;
            teleportTime = 0;
        }

        public void d() {
            player = null;
            super.d();
        }

        public boolean b() {
            if (enderman.getRider() != null) {
                return false;
            }
            if (player != null) {
                if (!shouldAttack(player)) {
                    return false;
                } else {
                    enderman.a(player, 10.0F, 10.0F);
                    return true;
                }
            } else {
                return d != null && d.isAlive() || super.b();
            }
        }

        public void e() {
            if (player != null) {
                if (--aggroTime <= 0) {
                    d = player;
                    player = null;
                    super.c();
                }
            } else {
                if (d != null) {
                    if (shouldAttack(d)) {
                        if (d.h(enderman) < 16.0D && tryEscape()) {
                            enderman.dz();
                        }
                        teleportTime = 0;
                    } else if (d.h(enderman) > 256.0D && teleportTime++ >= 30 && enderman.a((Entity) d)) {
                        teleportTime = 0;
                    }
                }
                super.e();
            }
        }

        private boolean tryEscape() {
            try {
                return new com.destroystokyo.paper.event.entity.EndermanEscapeEvent(
                        (org.bukkit.entity.Enderman) ((Entity) enderman).getBukkitEntity(),
                        com.destroystokyo.paper.event.entity.EndermanEscapeEvent.Reason.STARE)
                        .callEvent();
            } catch (NoClassDefFoundError ignore) {
                return true;
            }
        }

        private boolean shouldAttack(EntityHuman entityhuman) {
            boolean shouldAttack = shouldAttack_real(entityhuman);
            try {
                com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent event =
                        new com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent(
                                (org.bukkit.entity.Enderman) ((Entity) enderman).getBukkitEntity(),
                                (org.bukkit.entity.Player) ((Entity) entityhuman).getBukkitEntity());
                event.setCancelled(!shouldAttack);
                return event.callEvent();
            } catch (NoClassDefFoundError ignore) {
            }
            return shouldAttack;
        }

        private boolean shouldAttack_real(EntityHuman entityhuman) {
            net.minecraft.server.v1_13_R2.ItemStack itemstack = entityhuman.inventory.armor.get(3);
            if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.getItem()) {
                return false;
            } else {
                Vec3D vec3d = entityhuman.f(1.0F).a();
                Vec3D vec3d1 = new Vec3D(enderman.locX - entityhuman.locX, enderman.getBoundingBox().minY + (double) enderman.getHeadHeight() - (entityhuman.locY + (double) entityhuman.getHeadHeight()), enderman.locZ - entityhuman.locZ);
                double d0 = vec3d1.b();
                vec3d1 = vec3d1.a();
                double d1 = vec3d.b(vec3d1);
                return d1 > 1.0D - 0.025D / d0 && entityhuman.hasLineOfSight(enderman);
            }
        }
    }
}
