package com.github.minecraft_ta.totalperformance.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityTrackerEntry.class)
public abstract class EntityTrackerEntryMixin {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private Entity trackedEntity;
    @Shadow
    private int lastHeadMotion;

    /**
     * @author Pelotrio
     * @reason Causes a SIGSEGV with graalvm
     */
    @Overwrite
    private Packet<?> createSpawnPacket() {
        return createSpawnPacket0();
    }

    private Packet<?> createSpawnPacket0() {
        if (this.trackedEntity.isDead) {
            LOGGER.warn("Fetching addPacket for removed entity");
        }

        Packet<?> pkt = FMLNetworkHandler.getEntitySpawningPacket(this.trackedEntity);
        if (pkt != null) return pkt;

        if (this.trackedEntity instanceof EntityPlayerMP) {
            return new SPacketSpawnPlayer((EntityPlayer) this.trackedEntity);
        } else if (this.trackedEntity instanceof IAnimals) {
            this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
            return new SPacketSpawnMob((EntityLivingBase) this.trackedEntity);
        } else if (this.trackedEntity instanceof EntityPainting) {
            return new SPacketSpawnPainting((EntityPainting) this.trackedEntity);
        } else if (this.trackedEntity instanceof EntityItem) {
            return new SPacketSpawnObject(this.trackedEntity, 2, 1);
        } else if (this.trackedEntity instanceof EntityMinecart) {
            EntityMinecart entityminecart = (EntityMinecart) this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 10, entityminecart.getType().getId());
        } else if (this.trackedEntity instanceof EntityBoat) {
            return new SPacketSpawnObject(this.trackedEntity, 1);
        } else if (this.trackedEntity instanceof EntityXPOrb) {
            return new SPacketSpawnExperienceOrb((EntityXPOrb) this.trackedEntity);
        } else if (this.trackedEntity instanceof EntityFishHook) {
            Entity entity2 = ((EntityFishHook) this.trackedEntity).getAngler();
            return new SPacketSpawnObject(this.trackedEntity, 90, entity2 == null ? this.trackedEntity.getEntityId() : entity2.getEntityId());
        } else {
            Packet<?> spawnPacket1 = createSpawnPacket1();
            if (spawnPacket1 != null) return spawnPacket1;
            throw new IllegalArgumentException("Don't know how to add " + this.trackedEntity.getClass() + "!");
        }
    }

    private Packet<?> createSpawnPacket1() {
        if (this.trackedEntity instanceof EntitySpectralArrow) {
            Entity entity1 = ((EntitySpectralArrow) this.trackedEntity).shootingEntity;
            return new SPacketSpawnObject(this.trackedEntity, 91, 1 + (entity1 == null ? this.trackedEntity.getEntityId() : entity1.getEntityId()));
        } else if (this.trackedEntity instanceof EntityTippedArrow) {
            Entity entity = ((EntityArrow) this.trackedEntity).shootingEntity;
            return new SPacketSpawnObject(this.trackedEntity, 60, 1 + (entity == null ? this.trackedEntity.getEntityId() : entity.getEntityId()));
        } else if (this.trackedEntity instanceof EntitySnowball) {
            return new SPacketSpawnObject(this.trackedEntity, 61);
        } else if (this.trackedEntity instanceof EntityLlamaSpit) {
            return new SPacketSpawnObject(this.trackedEntity, 68);
        } else if (this.trackedEntity instanceof EntityPotion) {
            return new SPacketSpawnObject(this.trackedEntity, 73);
        } else if (this.trackedEntity instanceof EntityExpBottle) {
            return new SPacketSpawnObject(this.trackedEntity, 75);
        } else if (this.trackedEntity instanceof EntityEnderPearl) {
            return new SPacketSpawnObject(this.trackedEntity, 65);
        } else if (this.trackedEntity instanceof EntityEnderEye) {
            return new SPacketSpawnObject(this.trackedEntity, 72);
        } else if (this.trackedEntity instanceof EntityFireworkRocket) {
            return new SPacketSpawnObject(this.trackedEntity, 76);
        } else {
            return createSpawnPacket2();
        }
    }

    private Packet<?> createSpawnPacket2() {
        if (this.trackedEntity instanceof EntityFireball) {
            EntityFireball entityfireball = (EntityFireball) this.trackedEntity;
            SPacketSpawnObject spacketspawnobject = null;
            int i = 63;

            if (this.trackedEntity instanceof EntitySmallFireball) {
                i = 64;
            } else if (this.trackedEntity instanceof EntityDragonFireball) {
                i = 93;
            } else if (this.trackedEntity instanceof EntityWitherSkull) {
                i = 66;
            }

            if (entityfireball.shootingEntity != null) {
                spacketspawnobject = new SPacketSpawnObject(this.trackedEntity, i, ((EntityFireball) this.trackedEntity).shootingEntity.getEntityId());
            } else {
                spacketspawnobject = new SPacketSpawnObject(this.trackedEntity, i, 0);
            }

            spacketspawnobject.setSpeedX((int) (entityfireball.accelerationX * 8000.0D));
            spacketspawnobject.setSpeedY((int) (entityfireball.accelerationY * 8000.0D));
            spacketspawnobject.setSpeedZ((int) (entityfireball.accelerationZ * 8000.0D));
            return spacketspawnobject;
        } else if (this.trackedEntity instanceof EntityShulkerBullet) {
            SPacketSpawnObject spacketspawnobject1 = new SPacketSpawnObject(this.trackedEntity, 67, 0);
            spacketspawnobject1.setSpeedX((int) (this.trackedEntity.motionX * 8000.0D));
            spacketspawnobject1.setSpeedY((int) (this.trackedEntity.motionY * 8000.0D));
            spacketspawnobject1.setSpeedZ((int) (this.trackedEntity.motionZ * 8000.0D));
            return spacketspawnobject1;
        } else if (this.trackedEntity instanceof EntityEgg) {
            return new SPacketSpawnObject(this.trackedEntity, 62);
        } else if (this.trackedEntity instanceof EntityEvokerFangs) {
            return new SPacketSpawnObject(this.trackedEntity, 79);
        } else if (this.trackedEntity instanceof EntityTNTPrimed) {
            return new SPacketSpawnObject(this.trackedEntity, 50);
        } else if (this.trackedEntity instanceof EntityEnderCrystal) {
            return new SPacketSpawnObject(this.trackedEntity, 51);
        } else if (this.trackedEntity instanceof EntityFallingBlock) {
            EntityFallingBlock entityfallingblock = (EntityFallingBlock) this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock()));
        } else if (this.trackedEntity instanceof EntityArmorStand) {
            return new SPacketSpawnObject(this.trackedEntity, 78);
        } else if (this.trackedEntity instanceof EntityItemFrame) {
            EntityItemFrame entityitemframe = (EntityItemFrame) this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex(), entityitemframe.getHangingPosition());
        } else if (this.trackedEntity instanceof EntityLeashKnot) {
            EntityLeashKnot entityleashknot = (EntityLeashKnot) this.trackedEntity;
            return new SPacketSpawnObject(this.trackedEntity, 77, 0, entityleashknot.getHangingPosition());
        } else if (this.trackedEntity instanceof EntityAreaEffectCloud) {
            return new SPacketSpawnObject(this.trackedEntity, 3);
        }
        return null;
    }

}
