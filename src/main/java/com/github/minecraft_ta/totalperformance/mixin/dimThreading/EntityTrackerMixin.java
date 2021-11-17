package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.IThreadedWorldServer;
import net.minecraft.entity.EntityTracker;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTracker.class)
public abstract class EntityTrackerMixin {

    @Shadow @Final private WorldServer world;

    @Inject(method = "tick", at = @At("HEAD"))
    public void lockTick(CallbackInfo ci) {
        ((IThreadedWorldServer)this.world).getEntityTrackerLock().lock();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void unlockTick(CallbackInfo ci) {
        ((IThreadedWorldServer)this.world).getEntityTrackerLock().unlock();
    }

    @Inject(method = "track(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    public void lockTrack(CallbackInfo ci) {
        ((IThreadedWorldServer)this.world).getEntityTrackerLock().lock();
    }

    @Inject(method = "track(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    public void unlockTrack(CallbackInfo ci) {
        ((IThreadedWorldServer)this.world).getEntityTrackerLock().unlock();
    }

    @Inject(method = "sendLeashedEntitiesInChunk", at = @At("HEAD"))
    public void lockSendLeashedEntitiesInChunk(CallbackInfo ci) {
        ((IThreadedWorldServer)this.world).getEntityTrackerLock().lock();
    }

    @Inject(method = "sendLeashedEntitiesInChunk", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1))
    public void unlockSendLeashedEntitiesInChunk(CallbackInfo ci) {
        ((IThreadedWorldServer)this.world).getEntityTrackerLock().unlock();
    }
}
