package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MultiPartEntityPart.class)
public abstract class MultiPartEntityPartMixin extends Entity {

    @Shadow @Final public IEntityMultiPart parent;

    public MultiPartEntityPartMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isImmuneToExplosions() {
        return parent instanceof EntityDragon && ((EntityDragon) parent).getPhaseManager().getCurrentPhase().getType().equals(PhaseList.DYING);
    }
}
