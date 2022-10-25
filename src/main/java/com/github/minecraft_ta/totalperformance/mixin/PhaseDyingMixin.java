package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.entity.boss.dragon.phase.PhaseDying;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.rong.mixinextras.injector.WrapWithCondition;

@Mixin(PhaseDying.class)
public abstract class PhaseDyingMixin {

    @WrapWithCondition(method = "doClientRenderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private boolean shouldSpawnParticle(PhaseDying instance, World world, EnumParticleTypes particleType, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int[] parameters) {
        return TotalPerformance.CONFIG.doDragonParticles;
    }

}
