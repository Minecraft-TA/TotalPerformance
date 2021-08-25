package com.github.minecraft_ta.totalperformance.mixin;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CPacketCustomPayload.class)
public abstract class CPacketCustomPayloadMixin {

    @ModifyConstant(method = "readPacketData", constant = @Constant(intValue = 32767))
    private int modifyMaxPacketSize(int maxPacketSize) {
        return TotalPerformance.CONFIG.maxPacketSize;
    }

    @ModifyConstant(method = "readPacketData", constant = @Constant(stringValue = "Payload may not be larger than 32767 bytes"))
    private String modifyMaxPacketSizeMessage(String maxPacketSizeMessage) {
        return maxPacketSizeMessage.replace("32767", Integer.toString(TotalPerformance.CONFIG.maxPacketSize));
    }

    @ModifyConstant(method = "<init>(Ljava/lang/String;Lnet/minecraft/network/PacketBuffer;)V", constant = @Constant(intValue = 32767))
    private int modifyMaxPacketSizeConstructor(int maxPacketSize) {
        return TotalPerformance.CONFIG.maxPacketSize;
    }

    @ModifyConstant(method = "<init>(Ljava/lang/String;Lnet/minecraft/network/PacketBuffer;)V", constant = @Constant(stringValue = "Payload may not be larger than 32767 bytes"))
    private String modifyMaxPacketSizeConstructorMessage(String maxPacketSizeMessage) {
        return maxPacketSizeMessage.replace("32767", Integer.toString(TotalPerformance.CONFIG.maxPacketSize));
    }

}
