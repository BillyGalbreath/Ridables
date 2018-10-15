package net.pl3x.bukkit.ridables.entity.controller;

import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.pl3x.bukkit.ridables.configuration.Config;
import net.pl3x.bukkit.ridables.entity.RidableEntity;
import net.pl3x.bukkit.ridables.event.RidableSpacebarEvent;
import org.bukkit.Bukkit;

public class ControllerWASDFlying extends ControllerWASD {
    public ControllerWASDFlying(RidableEntity entity) {
        super(entity);
    }

    @Override
    public void tick(EntityPlayer rider) {
        float forward = Math.max(0, rider.bj);
        float vertical = forward == 0 ? 0 : -(rider.pitch / 45);
        float strafe = rider.bh;

        if (isJumping(rider)) {
            RidableSpacebarEvent event = new RidableSpacebarEvent(ridable);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled() && !event.isHandled()) {
                ridable.onSpacebar();
            }
        }

        if (a.locY >= Config.FLYING_MAX_Y) {
            a.motY = -0.05F;
            vertical = 0;
            forward = 0;
            strafe = 0;
        }

        a.setNoGravity(forward > 0);
        a.o((float) (e = ridable.getSpeed()));
        a.s(vertical);
        a.t(strafe);
        a.r(forward);

        f = a.bj;
        g = a.bh;
    }
}
