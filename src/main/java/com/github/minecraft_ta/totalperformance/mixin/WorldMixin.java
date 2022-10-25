package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Final
    @Shadow
    public WorldProvider provider;

    @Redirect(method = "getFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;getFogColor(FF)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d getFogColor(WorldProvider instance, float angle, float partialTicks) {
        if (TotalPerformance.CONFIG.disableEndRendering && instance.getDimension() == 1) {
            float f = MathHelper.cos(angle * ((float) Math.PI * 2F)) * 2.0F + 0.5F;
            f = MathHelper.clamp(f, 0.0F, 1.0F);
            float f1 = 0.7529412F;
            float f2 = 0.84705883F;
            float f3 = 1.0F;
            f1 = f1 * (f * 0.94F + 0.06F);
            f2 = f2 * (f * 0.94F + 0.06F);
            f3 = f3 * (f * 0.91F + 0.09F);
            return new Vec3d(f1, f2, f3);
        }

        return instance.getFogColor(angle, partialTicks);
    }

    @Redirect(method = "getCelestialAngle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;calculateCelestialAngle(JF)F"))
    private float redirectCelestialAngle(WorldProvider instance, long worldTime, float partialTicks) {
        if (TotalPerformance.CONFIG.forcedDayTime != -1) {
            int i = TotalPerformance.CONFIG.forcedDayTime;
            float f = ((float) i + partialTicks) / 24000.0F - 0.25F;

            if (f < 0.0F) {
                ++f;
            }

            if (f > 1.0F) {
                --f;
            }

            float f1 = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0D) / 2.0D);
            f = f + (f1 - f) / 3.0F;
            return f;
        }

        return instance.calculateCelestialAngle(worldTime, partialTicks);
    }

    @Inject(method = "getLightBrightness", at = @At(value = "HEAD"), cancellable = true)
    private void injectGetLightBrightness(BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (TotalPerformance.CONFIG.disableEndRendering && this.provider.getDimension() == 1) {
            cir.setReturnValue(1.0F);
        }
    }
}
