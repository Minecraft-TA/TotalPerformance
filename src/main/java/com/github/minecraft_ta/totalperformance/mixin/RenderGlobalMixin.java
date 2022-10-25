package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {

	@ModifyConstant(method = "spawnParticle0(IZZDDDDDD[I)Lnet/minecraft/client/particle/Particle;", constant = @Constant(doubleValue = 1024.0D))
	private double modifyParticleSpawnRange(double particleSpawnRange) {
		return TotalPerformance.CONFIG.particleSpawnRange;
	}

}
