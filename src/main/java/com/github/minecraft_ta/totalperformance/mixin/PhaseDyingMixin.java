package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.entity.boss.dragon.phase.PhaseDying;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PhaseDying.class)
public abstract class PhaseDyingMixin {

    @Redirect(method = "doClientRenderEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void spawnParticle(World world, EnumParticleTypes particleType, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int[] parameters) {
        if (TotalPerformance.CONFIG.doDragonParticles) {
            world.spawnParticle(particleType, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
        }
    }

}
