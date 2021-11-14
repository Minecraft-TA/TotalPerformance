package com.github.minecraft_ta.totalperformance.dimThreading;

public class GlobalThreadLocals {

    public static final ThreadLocal<Boolean> blockFallingFallInstantly = ThreadLocal.withInitial(() -> false);
}
