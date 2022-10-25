package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderEnd;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;

@Mixin(WorldProviderEnd.class)
public abstract class WorldProviderEndMixin extends WorldProvider {

    /**
     * @author
     * @reason Inject fails with verify error
     */
    @Overwrite
    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        if (TotalPerformance.CONFIG.disableEndRendering) {
            return super.calcSunriseSunsetColors(celestialAngle, partialTicks);
        }

        return null;
    }
}
