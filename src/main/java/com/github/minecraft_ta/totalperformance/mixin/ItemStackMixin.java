package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "forgeInit", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;gatherCapabilities(Lnet/minecraft/item/ItemStack;Lnet/minecraftforge/common/capabilities/ICapabilityProvider;)Lnet/minecraftforge/common/capabilities/CapabilityDispatcher;", shift = At.Shift.BEFORE), remap = false, cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void onForgeInit(CallbackInfo ci, Item item, ICapabilityProvider provider) {
        if (provider == null) {
            ci.cancel();
        }
    }

}
