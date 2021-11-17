package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeCache;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link net.minecraft.world.biome.BiomeCache#getEntry(int, int)} may be called from other threads and can cause a CME
 * in {@link BiomeCache#cleanupCache()} which is called in {@link World#tick()}.
 */
@Mixin(BiomeCache.class)
public abstract class BiomeCacheMixin {

    private final ReentrantLock biomeCacheLock = new ReentrantLock();

    @Inject(method = "getEntry", at = @At(value = "FIELD", target = "Lnet/minecraft/world/biome/BiomeCache;cacheMap:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void lockGetEntry(int x, int z, CallbackInfoReturnable<BiomeCache.Block> cir) {
        this.biomeCacheLock.lock();
    }

    @Inject(method = "getEntry", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCurrentTimeMillis()J"))
    public void unlockGetEntry(int x, int z, CallbackInfoReturnable<BiomeCache.Block> cir) {
        this.biomeCacheLock.unlock();
    }

    @Inject(method = "cleanupCache", at = @At("HEAD"))
    public void lockCleanupCache(CallbackInfo ci) {
        this.biomeCacheLock.lock();
    }

    @Inject(method = "cleanupCache", at = @At("RETURN"))
    public void unlockCleanupCache(CallbackInfo ci) {
        this.biomeCacheLock.unlock();
    }
}
