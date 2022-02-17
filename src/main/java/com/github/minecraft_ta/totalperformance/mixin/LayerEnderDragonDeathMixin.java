package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonDeath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerEnderDragonDeath.class)
public class LayerEnderDragonDeathMixin {

    @Inject(method = "doRenderLayer(Lnet/minecraft/entity/boss/EntityDragon;FFFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void onRenderEnderDragonDeathLayer(CallbackInfo ci) {
        if (!TotalPerformance.CONFIG.doDragonParticles) {
            ci.cancel();
        }
    }
}
