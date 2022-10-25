package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.SplashProgress;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashProgress.class)
public abstract class SplashProgressMixin {

    @Inject(method = "getMaxTextureSize", at = @At("HEAD"), remap = false, cancellable = true)
    private static void modifyMaxTextureSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(GlStateManager.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE));
    }
}
