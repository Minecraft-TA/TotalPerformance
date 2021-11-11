package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.block.BlockLeaves;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockLeaves.class)
public abstract class BlockLeavesMixin {

    private final ThreadLocal<int[]> surroundings = ThreadLocal.withInitial(() -> null);

    @Redirect(method = {"updateTick"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockLeaves;surroundings:[I", opcode = Opcodes.PUTFIELD))
    public void redirectCanProvidePowerToThreadLocal(BlockLeaves instance, int[] value) {
        this.surroundings.set(value);
    }

    @Redirect(method = {"updateTick"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockLeaves;surroundings:[I", opcode = Opcodes.GETFIELD))
    public int[] redirectCanProvidePowerToThreadLocal(BlockLeaves instance) {
        return this.surroundings.get();
    }
}
