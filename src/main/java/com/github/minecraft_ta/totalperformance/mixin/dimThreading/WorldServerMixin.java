package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.IConcurrentWorldServer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World implements IConcurrentWorldServer {

    /**
     * This lock prevents other threads from calling {@link World#getBlockState(BlockPos)} and
     * {@link World#setBlockState(BlockPos, IBlockState)}.
     */
    private final ReentrantLock blockStateLock = new ReentrantLock();

    /**
     * Guards all methods that access chunks.
     * Prevents for example loading chunks during {@link ChunkProviderServer#queueUnloadAll()}.
     */
    private final ReentrantLock chunkLock = new ReentrantLock();

    /**
     * {@link net.minecraft.world.chunk.storage.AnvilChunkLoader#loadEntities(World, NBTTagCompound, Chunk)} -> {@link World#scheduleBlockUpdate(BlockPos, Block, int, int)}
     * May happen during {@link WorldServer#tickUpdates(boolean)}. -> {@code throw new IllegalStateException("TickNextTick list out of synch");}
     */
    private final ReentrantLock blockUpdateLock = new ReentrantLock();

    /**
     * Prevents the tracker entries in {@link net.minecraft.entity.EntityTracker#entries} from being modified by
     * {@link net.minecraft.world.ServerWorldEventHandler#onEntityAdded(Entity)} during {@link EntityTracker#tick()} and
     * a few other methods.
     */
    private final ReentrantLock entityTrackerLock = new ReentrantLock();

    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "tickUpdates", at = @At(value = "FIELD", target = "Lnet/minecraft/world/WorldServer;pendingTickListEntriesTreeSet:Ljava/util/TreeSet;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void lockTickUpdates(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        //We lock on the block state lock as well to prevent a deadlock. This may happen when a chunk gets loaded (for
        // example by a getBlockState call from another thread) and the chunk load causes block updates to be scheduled.
        // The chunk load couldn't continue, because we own the block update lock. But we couldn't continue either
        // because the other thread that caused the chunk load may own the block state lock (because it called
        // getBlockState which we want to call as well).
        this.blockStateLock.lock();
        this.blockUpdateLock.lock();
    }

    @Inject(method = "tickUpdates", at = @At(value = "RETURN"))
    public void unlockTickUpdates(boolean runAllPending, CallbackInfoReturnable<Boolean> cir) {
        this.blockUpdateLock.unlock();
        this.blockStateLock.unlock();
    }

    @Inject(method = "scheduleBlockUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/world/WorldServer;pendingTickListEntriesHashSet:Ljava/util/Set;", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void lockScheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority, CallbackInfo ci) {
        this.blockUpdateLock.lock();
    }

    @Inject(method = "scheduleBlockUpdate", at = @At("TAIL"))
    public void unlockScheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority, CallbackInfo ci) {
        this.blockUpdateLock.unlock();
    }

    @Inject(method = "updateBlockTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z"))
    public void lockUpdateBlockTick(BlockPos pos, Block blockIn, int delay, int priority, CallbackInfo ci) {
        this.blockUpdateLock.lock();
    }

    @Inject(method = "updateBlockTick", at = @At(value = "TAIL"))
    public void unlockUpdateBlockTick(BlockPos pos, Block blockIn, int delay, int priority, CallbackInfo ci) {
        this.blockUpdateLock.unlock();
    }

    @Inject(method = "getPendingBlockUpdates(Lnet/minecraft/world/gen/structure/StructureBoundingBox;Z)Ljava/util/List;", at = @At("HEAD"))
    public void lockGetPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove, CallbackInfoReturnable<List<NextTickListEntry>> cir) {
        this.blockUpdateLock.lock();
    }

    @Inject(method = "getPendingBlockUpdates(Lnet/minecraft/world/gen/structure/StructureBoundingBox;Z)Ljava/util/List;", at = @At(value = "TAIL"))
    public void unlockGetPendingBlockUpdates(StructureBoundingBox structureBB, boolean remove, CallbackInfoReturnable<List<NextTickListEntry>> cir) {
        this.blockUpdateLock.unlock();
    }

    @Override
    public void forceUnlockAll() {
        //Releases all held locks to allow the server to properly shutdown
        if (this.chunkLock.isHeldByCurrentThread())
            this.chunkLock.unlock();
        if (this.blockStateLock.isHeldByCurrentThread())
            this.blockStateLock.unlock();
        if (this.blockUpdateLock.isHeldByCurrentThread())
            this.blockUpdateLock.unlock();
        if (this.entityTrackerLock.isHeldByCurrentThread())
            this.entityTrackerLock.unlock();
    }

    @Override
    public ReentrantLock getEntityTrackerLock() {
        return this.entityTrackerLock;
    }

    @Override
    public ReentrantLock getChunkLock() {
        return this.chunkLock;
    }
}
