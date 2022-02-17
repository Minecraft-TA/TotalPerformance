package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Mutable
    @Shadow
    @Final
    private Map<BlockPos, TileEntity> tileEntities;


    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"))
    public void constructorEnd(World worldIn, int x, int z, CallbackInfo ci) {
        this.tileEntities = new ConcurrentHashMap<>();
    }
}