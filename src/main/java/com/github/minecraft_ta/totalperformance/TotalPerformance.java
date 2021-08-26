package com.github.minecraft_ta.totalperformance;

import com.github.minecraft_ta.totalperformance.config.TotalPerformanceConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(
        modid = TotalPerformance.MOD_ID,
        version = "1.0.0",
        guiFactory = "com.github.minecraft_ta.totalperformance.config.gui.ModGuiFactory"
)
public class TotalPerformance {
    public static final String MOD_ID = "total_performance";

    @Mod.Instance
    public static TotalPerformance INSTANCE;

    public static final TotalPerformanceConfig CONFIG;
    static {
        //pre load this because we need it in MixinEventBus
        CONFIG = new TotalPerformanceConfig(new Configuration(new File(new File("."), "config" + File.separator + MOD_ID + ".cfg")));
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(CONFIG);
    }
}
