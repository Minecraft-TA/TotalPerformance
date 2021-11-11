package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import net.minecraft.world.gen.ChunkGeneratorFlat;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkGeneratorFlat.class)
public abstract class ChunkGeneratorFlatMixin {

    @Redirect(method = {"populate"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockFalling;fallInstantly:Z", opcode = Opcodes.PUTSTATIC))
    private static void redirectFallInstantlyToThreadLocal(boolean value) {
        GlobalThreadLocals.blockFallingFallInstantly.set(value);
    }
}
