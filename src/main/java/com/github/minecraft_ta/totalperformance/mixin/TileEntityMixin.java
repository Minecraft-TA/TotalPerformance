package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TileEntity.class)
public abstract class TileEntityMixin {

    @ModifyConstant(method = "getMaxRenderDistanceSquared", constant = @Constant(doubleValue = 4096.0D))
    private double modifyRenderDistance(double original) {
        return TotalPerformance.CONFIG.maxTileEntityRenderDistanceSquared;
    }
}
