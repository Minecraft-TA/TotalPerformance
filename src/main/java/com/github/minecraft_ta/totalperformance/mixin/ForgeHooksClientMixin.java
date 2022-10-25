package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ForgeHooksClient.class)
public abstract class ForgeHooksClientMixin {

    @Redirect(method = "getSkyBlendColour", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getSkyColorByTemp(F)I"))
    private static int redirectSkyColor(Biome instance, float currentTemperature) {
        if (TotalPerformance.CONFIG.disableEndRendering && instance instanceof BiomeEnd) {
            return 7907071;
        }

        return instance.getSkyColorByTemp(currentTemperature);
    }
}
