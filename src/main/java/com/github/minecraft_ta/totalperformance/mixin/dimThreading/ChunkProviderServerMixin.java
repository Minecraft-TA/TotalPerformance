package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.IConcurrentWorldServer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkProviderServer.class)
public abstract class ChunkProviderServerMixin {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    public WorldServer world;
    @Shadow
    @Final
    public IChunkLoader chunkLoader;
    @Shadow
    @Final
    public Long2ObjectMap<Chunk> loadedChunks;

    @Inject(method = {"queueUnloadAll"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadedChunks:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD))
    public void lockQueueUnloadAll(CallbackInfo ci) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"queueUnloadAll"}, at = @At("RETURN"))
    public void unlockQueueUnloadAll(CallbackInfo ci) {
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"getLoadedChunk"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadedChunks:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD))
    public void lockGetLoadedChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"getLoadedChunk"}, at = @At("RETURN"))
    public void unlockGetLoadedChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;"}, at = @At(value = "FIELD", target = "net/minecraft/world/gen/ChunkProviderServer.loadingChunks : Ljava/util/Set;", opcode = Opcodes.GETFIELD), remap = false)
    public void lockLoadChunk(int x, int z, Runnable r, CallbackInfoReturnable<Chunk> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;"}, at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z", shift = At.Shift.AFTER), remap = false)
    public void unlockLoadChunk(int x, int z, Runnable r, CallbackInfoReturnable<Chunk> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"provideChunk"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadedChunks:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD))
    public void lockProvideChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"provideChunk"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;onLoad()V"))
    public void unlockProvideChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"tick"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;droppedChunks:Ljava/util/Set;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void lockTick(CallbackInfoReturnable<Boolean> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/IChunkLoader;chunkTick()V"))
    public void unlockTick(CallbackInfoReturnable<Boolean> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"chunkExists"}, at = @At(value = "HEAD"), cancellable = true)
    public void lockChunkExists(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
        boolean value = this.loadedChunks.containsKey(ChunkPos.asLong(x, z));
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
        cir.setReturnValue(value);
    }

    @Inject(method = {"isChunkGeneratedAt"}, at = @At(value = "HEAD"), cancellable = true)
    public void lockIsChunkGeneratedAt(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        ((IConcurrentWorldServer) this.world).getChunkLock().lock();
        boolean value = this.loadedChunks.containsKey(ChunkPos.asLong(x, z)) || this.chunkLoader.isChunkGeneratedAt(x, z);
        ((IConcurrentWorldServer) this.world).getChunkLock().unlock();
        cir.setReturnValue(value);
    }
}
