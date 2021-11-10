package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.dimThreading.ITeleportableEntity;
import com.github.minecraft_ta.totalperformance.dimThreading.IWorldAcceptsTeleport;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin implements IWorldAcceptsTeleport {

    private final List<Entity> teleportedEntities = new ArrayList<>();

    @Override
    public void queueEntityForTeleport(Entity entity) {
        synchronized (this.teleportedEntities) {
            this.teleportedEntities.add(entity);
        }
    }

    @Inject(method = "updateEntities", at = @At("HEAD"))
    public void placeTeleportedEntities(CallbackInfo callbackInfo) {
        synchronized (this.teleportedEntities) {
            for (Entity teleportedEntity : this.teleportedEntities) {
                ((ITeleportableEntity) teleportedEntity).finishTeleport((WorldServer) (Object) this);
            }
            this.teleportedEntities.clear();
        }
    }
}
