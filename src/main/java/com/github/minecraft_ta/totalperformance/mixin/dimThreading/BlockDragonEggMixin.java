package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.block.BlockDragonEgg;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockDragonEgg.class)
public abstract class BlockDragonEggMixin {

    @Redirect(method = {"checkFall"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockFalling;fallInstantly:Z", opcode = Opcodes.GETSTATIC))
    private static boolean redirectFallInstantlyToThreadLocal() {
        return GlobalThreadLocals.blockFallingFallInstantly.get();
    }
}
