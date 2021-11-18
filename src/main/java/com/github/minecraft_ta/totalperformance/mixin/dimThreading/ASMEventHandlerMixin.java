package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.IConcurrentEventHandler;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.locks.ReentrantLock;

@Mixin(ASMEventHandler.class)
public abstract class ASMEventHandlerMixin implements IConcurrentEventHandler {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public ReentrantLock getEventHandlerLock() {
        return this.lock;
    }
}
