package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;


@Mixin(EventBus.class)
public abstract class MixinEventBus {

    @Inject(at = @At("HEAD"), method = "register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;)V", remap = false, cancellable = true)
    public void register(Class<?> eventType, Object target, Method method, ModContainer owner, CallbackInfo ci) {
        Class<?> targetClass = target.getClass() == Class.class ? (Class<?>) target : target.getClass();
        Type genericParameterType = method.getGenericParameterTypes()[0];

        List<String> whitelistedAttachCapabilityHandlers = TotalPerformance.CONFIG.whitelistedAttachCapabilityHandlers;
        if (!whitelistedAttachCapabilityHandlers.isEmpty() &&
            eventType.equals(AttachCapabilitiesEvent.class) &&
            genericParameterType instanceof ParameterizedType &&
            ((ParameterizedType) genericParameterType).getActualTypeArguments()[0].equals(ItemStack.class) &&
            !whitelistedAttachCapabilityHandlers.contains(targetClass.getName())) {
            ci.cancel();
        }
    }
}
