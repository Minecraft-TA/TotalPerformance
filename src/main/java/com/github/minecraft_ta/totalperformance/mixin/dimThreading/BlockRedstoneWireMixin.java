package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;

@Mixin(BlockRedstoneWire.class)
public abstract class BlockRedstoneWireMixin {

    private final ThreadLocal<Set<BlockPos>> blocksNeedingUpdate = ThreadLocal.withInitial(HashSet::new);
    private final ThreadLocal<Boolean> canProvidePower = ThreadLocal.withInitial(() -> true);

    @Redirect(method = {"calculateCurrentChanges"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockRedstoneWire;canProvidePower:Z", opcode = Opcodes.PUTFIELD))
    public void redirectCanProvidePowerToThreadLocal(BlockRedstoneWire instance, boolean value) {
        this.canProvidePower.set(value);
    }

    @Redirect(method = {"getWeakPower", "getStrongPower", "canProvidePower"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockRedstoneWire;canProvidePower:Z", opcode = Opcodes.GETFIELD))
    public boolean redirectCanProvidePowerToThreadLocal(BlockRedstoneWire instance) {
        return this.canProvidePower.get();
    }

    @Redirect(method = {"updateSurroundingRedstone", "calculateCurrentChanges"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockRedstoneWire;blocksNeedingUpdate:Ljava/util/Set;", opcode = Opcodes.GETFIELD))
    public Set<BlockPos> redirectBlocksNeedingUpdateToThreadLocal(BlockRedstoneWire instance) {
        return this.blocksNeedingUpdate.get();
    }
}
