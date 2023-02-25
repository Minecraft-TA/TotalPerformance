package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Redirect(method = "isInRangeToRender3d", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInRangeToRenderDist(D)Z"))
    private boolean modifyRenderDistance(Entity instance, double distance) {
        int customDistance = TotalPerformance.CONFIG.entityRenderDistance;
        if (customDistance != -1)
            return distance < customDistance * customDistance;

        return instance.isInRangeToRenderDist(distance);
    }
}
