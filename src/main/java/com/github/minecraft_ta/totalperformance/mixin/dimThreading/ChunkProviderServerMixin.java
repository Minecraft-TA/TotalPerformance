package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.IThreadedWorldServer;
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

    @Shadow @Final private static Logger LOGGER;
    @Shadow
    @Final
    public WorldServer world;

    @Inject(method = {"queueUnloadAll"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadedChunks:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD))
    public void lockQueueUnloadAll(CallbackInfo ci) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"queueUnloadAll"}, at = @At("RETURN"))
    public void unlockQueueUnloadAll(CallbackInfo ci) {
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"getLoadedChunk"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadedChunks:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD))
    public void lockGetLoadedChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"getLoadedChunk"}, at = @At("RETURN"))
    public void unlockGetLoadedChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
    }

    //TODO: No obfuscation mapping found
    @Inject(remap = false, method = {"loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadingChunks:Ljava/util/Set;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void lockLoadChunk(int x, int z, Runnable r, CallbackInfoReturnable<Chunk> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(remap = false, method = {"loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;"}, at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
    public void unlockLoadChunk(int x, int z, Runnable r, CallbackInfoReturnable<Chunk> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"provideChunk"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadedChunks:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.GETFIELD))
    public void lockProvideChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"provideChunk"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;onLoad()V"))
    public void unlockProvideChunk(int x, int z, CallbackInfoReturnable<Chunk> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"tick"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/ChunkProviderServer;droppedChunks:Ljava/util/Set;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void lockTick(CallbackInfoReturnable<Boolean> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
    }

    @Inject(method = {"tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/storage/IChunkLoader;chunkTick()V"))
    public void unlockTick(CallbackInfoReturnable<Boolean> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
    }

    @Inject(method = {"chunkExists"}, at = @At(value = "HEAD"), cancellable = true)
    public void lockChunkExists(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
        boolean value = this.loadedChunks.containsKey(ChunkPos.asLong(x, z));
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
        cir.setReturnValue(value);
    }

    @Inject(method = {"isChunkGeneratedAt"}, at = @At(value = "HEAD"), cancellable = true)
    public void lockIsChunkGeneratedAt(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        ((IThreadedWorldServer) this.world).getChunkLock().lock();
        boolean value = this.loadedChunks.containsKey(ChunkPos.asLong(x, z)) || this.chunkLoader.isChunkGeneratedAt(x, z);
        ((IThreadedWorldServer) this.world).getChunkLock().unlock();
        cir.setReturnValue(value);
    }

    @Shadow
    @Final
    public IChunkLoader chunkLoader;

    @Shadow
    @Final
    public Long2ObjectMap<Chunk> loadedChunks;
}
