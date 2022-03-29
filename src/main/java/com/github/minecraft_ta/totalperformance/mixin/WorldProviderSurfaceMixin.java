package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.world.WorldProviderSurface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProviderSurface.class)
public abstract class WorldProviderSurfaceMixin {

    @Inject(method = "canDropChunk", at = @At("HEAD"), cancellable = true)
    public void canDropChunk(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (TotalPerformance.CONFIG.loadSpawnChunks) {
            cir.setReturnValue(true);
        }
    }

}
