package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

public class GlobalThreadLocals {

    public static final ThreadLocal<Boolean> blockFallingFallInstantly = ThreadLocal.withInitial(() -> false);
}
