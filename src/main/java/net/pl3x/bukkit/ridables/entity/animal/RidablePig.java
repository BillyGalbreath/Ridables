package net.pl3x.bukkit.ridables.entity.animal;

import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPig;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.Items;
import net.minecraft.server.v1_14_R1.PathfinderGoalBreed;
import net.minecraft.server.v1_14_R1.PathfinderGoalFloat;
import net.minecraft.server.v1_14_R1.PathfinderGoalFollowParent;
import net.minecraft.server.v1_14_R1.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_14_R1.PathfinderGoalPanic;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_14_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_14_R1.PathfinderGoalTempt;
import net.minecraft.server.v1_14_R1.RecipeItemStack;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.World;
import net.pl3x.bukkit.ridables.configuration.mob.PigConfig;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.entity.RidableType;
import net.pl3x.bukkit.ridables.entity.controller.ControllerWASD;
import net.pl3x.bukkit.ridables.entity.controller.LookController;

public class RidablePig extends EntityPig implements RidableEntity {
    private static final RecipeItemStack TEMPTATION_ITEMS = RecipeItemStack.a(Items.CARROT, Items.POTATO, Items.BEETROOT, Items.CARROT_ON_A_STICK);

    private static PigConfig config;

    private final ControllerWASD controllerWASD;

    public RidablePig(EntityTypes<? extends EntityPig> entitytypes, World world) {
        super(entitytypes, world);
        moveController = controllerWASD = new ControllerWASD(this);
        lookController = new LookController(this);

        if (config == null) {
            config = getConfig();
        }
    }

    @Override
    public RidableType getType() {
        return RidableType.PIG;
    }

    @Override
    public ControllerWASD getController() {
        return controllerWASD;
    }

    @Override
    public PigConfig getConfig() {
        return (PigConfig) getType().getConfig();
    }

    @Override
    public double getRidingSpeed() {
        return config.RIDING_SPEED;
    }

    @Override
    protected void initPathfinder() {
        goalSelector.a(0, new PathfinderGoalFloat(this));
        goalSelector.a(1, new PathfinderGoalPanic(this, 1.25D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(3, new PathfinderGoalBreed(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, false, TEMPTATION_ITEMS) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.1D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 1.0D) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
        goalSelector.a(8, new PathfinderGoalRandomLookaround(this) {
            public boolean a() { // shouldExecute
                return getRider() == null && super.a();
            }

            public boolean b() { // shouldContinueExecuting
                return getRider() == null && super.b();
            }
        });
    }

    // canBeRiddenInWater
    @Override
    public boolean be() {
        return config.RIDING_RIDE_IN_WATER;
    }

    // getJumpUpwardsMotion
    @Override
    protected float cW() {
        return getRider() == null ? super.cW() : config.RIDING_JUMP_POWER;
    }

    @Override
    protected void mobTick() {
        // this is moved down in travel method
        //K = getRider() == null ? 0.6F : config.RIDING_STEP_HEIGHT;
        super.mobTick();
    }

    // travel
    @Override
    public void e(Vec3D motion) {
        super.e(motion); // this sets either 0.5 or 1.0 step height
        if (getRider() != null && !dD()) { // canBeSteered
            // override with configured ride step height
            // only when not using carrot on a stick
            K = config.RIDING_STEP_HEIGHT;
        }
        checkMove();
    }

    // processInteract
    @Override
    public boolean a(EntityHuman entityhuman, EnumHand hand) {
        if (config.GET_SADDLE_BACK && entityhuman.isSneaking() && hasSaddle() && passengers.isEmpty()) {
            ItemStack item = entityhuman.b(hand);
            if (item.isEmpty() || item.getItem() == Items.SADDLE) {
                setSaddle(false);
                if (!entityhuman.abilities.canInstantlyBuild || item.getItem() != Items.SADDLE) {
                    ItemStack saddle = new ItemStack(Items.SADDLE);
                    if (!entityhuman.inventory.pickup(saddle)) {
                        entityhuman.drop(saddle, false);
                    }
                }
                return true; // handled
            }
        }
        if (super.a(entityhuman, hand)) {
            return true; // handled by vanilla action
        }
        if (hand == EnumHand.MAIN_HAND && !entityhuman.isSneaking() && passengers.isEmpty() && !entityhuman.isPassenger()) {
            if (!config.RIDING_BABIES && isBaby()) {
                return false; // do not ride babies
            }
            return tryRide(entityhuman, config.RIDING_SADDLE_REQUIRE, config.RIDING_SADDLE_CONSUME);
        }
        return false;
    }
}
