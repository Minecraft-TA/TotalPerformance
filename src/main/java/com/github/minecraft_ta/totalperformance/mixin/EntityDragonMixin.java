package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityDragon.class)
public abstract class EntityDragonMixin extends Entity {

    @Shadow
    @Final
    private PhaseManager phaseManager;

    public EntityDragonMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isImmuneToExplosions() {
        return phaseManager.getCurrentPhase().getType().equals(PhaseList.DYING);
    }
}
