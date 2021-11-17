package com.github.minecraft_ta.totalperformance.mixin;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    private final BlockPos.MutableBlockPos cachedPos = new BlockPos.MutableBlockPos();
    @Shadow
    @Final
    private float size;
    @Shadow
    @Final
    private double x;
    @Shadow
    @Final
    private double y;
    @Shadow
    @Final
    private double z;
    @Shadow
    @Final
    private World world;
    @Shadow
    @Final
    private Entity exploder;
    @Shadow
    @Final
    private List<BlockPos> affectedBlockPositions;
    @Shadow
    @Final
    private Map<EntityPlayer, Vec3d> playerKnockbackMap;
    private int prevChunkX = Integer.MIN_VALUE;

    private int prevChunkZ = Integer.MIN_VALUE;

    private Chunk prevChunk;

    @Inject(method = "doExplosionA", at = @At("HEAD"), cancellable = true)
    public void doExplosionA(CallbackInfo ci) {
        ci.cancel();
        final LongOpenHashSet touched = new LongOpenHashSet(216);
        final Random random = this.world.rand;
        for (int rayX = 0; rayX < 16; ++rayX) {
            final boolean xPlane = rayX == 0 || rayX == 15;
            for (int rayY = 0; rayY < 16; ++rayY) {
                final boolean yPlane = rayY == 0 || rayY == 15;
                for (int rayZ = 0; rayZ < 16; ++rayZ) {
                    final boolean zPlane = rayZ == 0 || rayZ == 15;
                    if (xPlane || yPlane || zPlane) {
                        final double vecX = rayX / 15.0f * 2.0f - 1.0f;
                        final double vecY = rayY / 15.0f * 2.0f - 1.0f;
                        final double vecZ = rayZ / 15.0f * 2.0f - 1.0f;
                        this.performRayCast(random, vecX, vecY, vecZ, touched);
                    }
                }
            }
        }
        final List<BlockPos> affectedBlocks = this.affectedBlockPositions;
        final LongIterator it = touched.iterator();
        while (it.hasNext()) {
            affectedBlocks.add(BlockPos.fromLong(it.nextLong()));
        }
        this.damageEntities();
    }

    private void performRayCast(final Random random, final double vecX, final double vecY, final double vecZ, final LongOpenHashSet touched) {
        final double dist = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
        final double normX = vecX / dist;
        final double normY = vecY / dist;
        final double normZ = vecZ / dist;
        float strength = this.size * (0.7f + random.nextFloat() * 0.6f);
        double stepX = this.x;
        double stepY = this.y;
        double stepZ = this.z;
        int prevX = Integer.MIN_VALUE;
        int prevY = Integer.MIN_VALUE;
        int prevZ = Integer.MIN_VALUE;
        float prevResistance = 0.0f;
        while (strength > 0.0f) {
            final int blockX = MathHelper.floor(stepX);
            final int blockY = MathHelper.floor(stepY);
            final int blockZ = MathHelper.floor(stepZ);
            float resistance;
            if (prevX != blockX || prevY != blockY || prevZ != blockZ) {
                resistance = this.traverseBlock(strength, blockX, blockY, blockZ, touched);
                prevX = blockX;
                prevY = blockY;
                prevZ = blockZ;
                prevResistance = resistance;
            } else {
                resistance = prevResistance;
            }
            strength -= resistance + 0.225f;
            stepX += normX * 0.3;
            stepY += normY * 0.3;
            stepZ += normZ * 0.3;
        }
    }

    private float traverseBlock(final float strength, final int blockX, final int blockY, final int blockZ, final LongOpenHashSet touched) {
        if (blockY < 0 || blockY >= 256) {
            return 0.0f;
        }
        final BlockPos pos = this.cachedPos.setPos(blockX, blockY, blockZ);
        final int chunkX = blockX >> 4;
        final int chunkZ = blockZ >> 4;
        if (this.prevChunkX != chunkX || this.prevChunkZ != chunkZ) {
            this.prevChunk = this.world.getChunk(chunkX, chunkZ);
            this.prevChunkX = chunkX;
            this.prevChunkZ = chunkZ;
        }
        final Chunk chunk = this.prevChunk;
        IBlockState blockState = Blocks.AIR.getDefaultState();
        float totalResistance = 0.0f;
        if (chunk != null) {
            final ExtendedBlockStorage section = chunk.getBlockStorageArray()[blockY >> 4];
            if (section != null && !section.isEmpty()) {
                blockState = section.get(blockX & 0xF, blockY & 0xF, blockZ & 0xF);
                if (blockState.getMaterial() != Material.AIR) {
                    //final IFluidState fluidState = blockState.fludi
                    //float resistance = Math.max(blockState.getBlock().getExplosionResistance(), fluidState.get);
                    float resistance = this.exploder != null
                            ? this.exploder.getExplosionResistance((Explosion) (Object) this, this.world, pos, blockState)
                            : blockState.getBlock().getExplosionResistance(world, pos, null, (Explosion) (Object) this);
                    totalResistance = (resistance + 0.3f) * 0.3f;
                }
            }
        }
        if (strength - totalResistance > 0.0f && (this.exploder == null || this.exploder.canExplosionDestroyBlock((Explosion) (Object) this, this.world, pos, blockState, strength))) {
            touched.add(pos.toLong());
        }
        return totalResistance;
    }

    private void damageEntities() {
        final float range = this.size * 2.0f;
        final int minX = MathHelper.floor(this.x - range - 1.0);
        final int maxX = MathHelper.floor(this.x + range + 1.0);
        final int minY = MathHelper.floor(this.y - range - 1.0);
        final int maxY = MathHelper.floor(this.y + range + 1.0);
        final int minZ = MathHelper.floor(this.z - range - 1.0);
        final int maxZ = MathHelper.floor(this.z + range + 1.0);
        final List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
        ForgeEventFactory.onExplosionDetonate(this.world, (Explosion) (Object) this, entities, range);
        final Vec3d selfPos = new Vec3d(this.x, this.y, this.z);
        for (final Entity entity : entities) {
            if (entity.isImmuneToExplosions()) {
                continue;
            }
            final double damageScale = MathHelper.sqrt(entity.getDistanceSq(selfPos.x, selfPos.y, selfPos.z)) / range;
            if (damageScale > 1.0) {
                continue;
            }
            double distXSq = entity.posX - this.x;
            double distYSq = entity.posY - this.y;
            double distZSq = entity.posZ - this.z;
            final double dist = MathHelper.sqrt(distXSq * distXSq + distYSq * distYSq + distZSq * distZSq);
            if (dist == 0.0) {
                continue;
            }
            distXSq /= dist;
            distYSq /= dist;
            distZSq /= dist;
            final double exposure = this.world.getBlockDensity(selfPos, entity.getEntityBoundingBox());
            final double damage = (1.0 - damageScale) * exposure;
            entity.attackEntityFrom(DamageSource.causeExplosionDamage((Explosion) (Object) this), (float) (int) ((damage * damage + damage) / 2.0 * 7.0 * range + 1.0));
            double knockback = damage;
            if (entity instanceof EntityLiving) {
                knockback = EnchantmentProtection.getBlastDamageReduction((EntityLiving) entity, damage);
            }
            entity.motionX += distXSq * knockback;
            entity.motionY += distYSq * knockback;
            entity.motionZ += distZSq * knockback;

            if (!(entity instanceof EntityPlayer)) {
                continue;
            }
            final EntityPlayer player = (EntityPlayer) entity;
            if (player.isSpectator() || (player.isCreative() && player.capabilities.isFlying)) {
                continue;
            }
            this.playerKnockbackMap.put(player, new Vec3d(distXSq * damage, distYSq * damage, distZSq * damage));
        }
    }
}
