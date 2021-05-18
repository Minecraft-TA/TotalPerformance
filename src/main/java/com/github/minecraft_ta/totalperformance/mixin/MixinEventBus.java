package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@Mixin(EventBus.class)
public abstract class MixinEventBus {

    @Inject(at = @At("HEAD"), method = "register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;)V", remap = false, cancellable = true)
    public void register(Class<?> eventType, Object target, Method method, ModContainer owner, CallbackInfo ci) {
        Class<?> targetClass = target.getClass() == Class.class ? (Class<?>) target : target.getClass();
        Type genericParameterType = method.getGenericParameterTypes()[0];

        Map<String, Triple<Boolean, String, List<String>>> eventBlockMap = TotalPerformance.CONFIG.eventBlockMap;
        if (eventBlockMap.isEmpty())
            return;

        Triple<Boolean, String, List<String>> entry = eventBlockMap.get(eventType.getName());
        if (entry == null)
            return;

        boolean genericMatches = entry.getMiddle() == null ||
                                 (genericParameterType instanceof ParameterizedType &&
                                  ((ParameterizedType) genericParameterType).getActualTypeArguments()[0].getTypeName().equals(entry.getMiddle()));
        if (!genericMatches)
            return;

        boolean targetClassBlocked = entry.getRight().contains(targetClass.getName());
        if (!targetClassBlocked) { //no match
            if (entry.getLeft()) //block for whitelist
                ci.cancel();
        } else {
            if (!entry.getLeft()) //block for blacklist
                ci.cancel();
        }
    }
}
