package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "handleWaterMovement", at = @At("HEAD"), cancellable = true)
    private void handleWaterMovement(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof EntityDragon || entity instanceof MultiPartEntityPart) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
    private void isInLava(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof EntityDragon || entity instanceof MultiPartEntityPart) {
            cir.setReturnValue(false);
        }
    }

}
