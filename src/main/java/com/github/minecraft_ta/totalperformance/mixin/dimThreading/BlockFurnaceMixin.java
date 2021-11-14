package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.block.BlockFurnace;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockFurnace.class)
public abstract class BlockFurnaceMixin {

    private static final ThreadLocal<Boolean> keepInventory = ThreadLocal.withInitial(() -> false);

    @Redirect(method = {"setState"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockFurnace;keepInventory:Z", opcode = Opcodes.PUTSTATIC))
    private static void redirectKeepInventoryToThreadLocal(boolean value) {
        keepInventory.set(value);
    }

    @Redirect(method = {"breakBlock"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockFurnace;keepInventory:Z", opcode = Opcodes.GETSTATIC))
    private boolean redirectKeepInventoryToThreadLocal() {
        return keepInventory.get();
    }
}
