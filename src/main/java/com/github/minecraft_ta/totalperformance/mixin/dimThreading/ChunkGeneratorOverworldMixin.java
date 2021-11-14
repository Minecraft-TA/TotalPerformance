package com.github.minecraft_ta.totalperformance.mixin.dimThreading;

import com.github.minecraft_ta.totalperformance.dimThreading.GlobalThreadLocals;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkGeneratorOverworld.class)
public abstract class ChunkGeneratorOverworldMixin {

    @Redirect(method = {"populate"}, at = @At(value = "FIELD", target = "Lnet/minecraft/block/BlockFalling;fallInstantly:Z", opcode = Opcodes.PUTSTATIC))
    private void redirectFallInstantlyToThreadLocal(boolean value) {
        GlobalThreadLocals.blockFallingFallInstantly.set(value);
    }
}
