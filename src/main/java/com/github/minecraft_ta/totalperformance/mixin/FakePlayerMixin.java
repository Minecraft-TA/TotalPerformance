package com.github.minecraft_ta.totalperformance.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FakePlayer.class)
public abstract class FakePlayerMixin extends EntityPlayerMP {

    public FakePlayerMixin(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn) {
        super(server, worldIn, profile, interactionManagerIn);
    }

    @Override
    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
        //NO OP
    }
}
