package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.block.BlockLeaves;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(BlockLeaves.class)
public abstract class BlockLeavesMixin {

    private final ThreadLocal<int[]> surroundings = ThreadLocal.withInitial(() -> null);

    /**
     * Mixin GETFIELD for array fields automatically targets array=get access, so we do this instead.
     */
    @Redirect(method = {"updateTick"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockLeaves;surroundings:[I", opcode = Opcodes.GETFIELD, args = {"array=get", "fuzz=25"}))
    public int redirectCanProvidePowerToThreadLocal(int[] array, int index) {
        array = this.surroundings.get();
        if (array == null) {
            array = new int[32768];
            this.surroundings.set(array);
        }
        return array[index];
    }

    @Redirect(method = {"updateTick"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockLeaves;surroundings:[I", opcode = Opcodes.GETFIELD, args = {"array=set", "fuzz=25"}))
    public void redirectCanProvidePowerToThreadLocal(int[] array, int index, int value) {
        array = this.surroundings.get();
        if (array == null) {
            array = new int[32768];
            this.surroundings.set(array);
        }
        array[index] = value;
    }

    /**
     * @reason Save some memory because that array won't be used
     */
    @ModifyConstant(method = "updateTick", constant = @Constant(intValue = 32768))
    public int modifyArraySize(int oldValue) {
        return 0;
    }
}
