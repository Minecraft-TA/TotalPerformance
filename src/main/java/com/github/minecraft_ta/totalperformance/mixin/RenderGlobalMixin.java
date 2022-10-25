package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.client.IRenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin {

	@ModifyConstant(method = "spawnParticle0(IZZDDDDDD[I)Lnet/minecraft/client/particle/Particle;", constant = @Constant(doubleValue = 1024.0D))
	private double modifyParticleSpawnRange(double particleSpawnRange) {
		return TotalPerformance.CONFIG.particleSpawnRange;
	}

    @Redirect(method = "renderSky(FI)V", at = @At(remap = false, value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;getSkyRenderer()Lnet/minecraftforge/client/IRenderHandler;"))
    public IRenderHandler redirectGetSkyRenderer(WorldProvider provider) {
        if (TotalPerformance.CONFIG.disableCustomSkyRenderer) {
            return null;
        }
        return provider.getSkyRenderer();
    }

    @Redirect(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/DimensionType;getId()I"))
    public int redirectGetId(DimensionType dimensionType) {
        if (TotalPerformance.CONFIG.disableEndRendering) {
            return 0;
        }
        return dimensionType.getId();
    }

    @Redirect(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;isSkyColored()Z"))
    public boolean redirectGetId(WorldProvider instance) {
        if (TotalPerformance.CONFIG.disableEndRendering) {
            return true;
        }
        return instance.isSkyColored();
    }

    @Redirect(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;isSurfaceWorld()Z"))
    private boolean redirectIsSurfaceWorld(WorldProvider provider) {
        if (TotalPerformance.CONFIG.forceOverworldSkyRender) {
            return true;
        }
        return provider.isSurfaceWorld();
    }
}
