package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.IConcurrentEventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(ASMEventHandler.class)
public abstract class ASMEventHandlerMixin implements IConcurrentEventHandler {

    private final ReentrantLock lock = new ReentrantLock();
    private Class<?> cl;

    @Inject(method = "<init>(Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;Z)V", at = @At("TAIL"))
    public void captureTargetClass(Object target, Method method, ModContainer owner, boolean isGeneric, CallbackInfo ci) {
        if (target instanceof Class<?>)
            this.cl = (Class<?>) target;
        else
            this.cl = target.getClass();
    }

    @Override
    public ReentrantLock getEventHandlerLock() {
        return this.lock;
    }

    public Class<?> getTargetClass() {
        return this.cl;
    }
}
