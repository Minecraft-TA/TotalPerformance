package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.PhaseList;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.rong.mixinextras.injector.WrapWithCondition;

@Mixin(EntityDragon.class)
public abstract class EntityDragonMixin extends Entity {

    @Shadow
    @Final
    private PhaseManager phaseManager;

    public EntityDragonMixin(World worldIn) {
        super(worldIn);
    }

    @WrapWithCondition(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private boolean shouldRenderParticles(EntityDragon dragon, World instance, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int[] parameters) {
        return TotalPerformance.CONFIG.doDragonParticles;
    }

    @WrapWithCondition(method = "onDeathUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private boolean shouldRenderDeathParticles(EntityDragon dragon, World instance, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int[] parameters) {
        return TotalPerformance.CONFIG.doDragonParticles;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return phaseManager.getCurrentPhase().getType().equals(PhaseList.DYING);
    }

    @Override
    public boolean isInLava() {
        return false;
    }

    @Override
    public boolean handleWaterMovement() {
        return false;
    }
}
