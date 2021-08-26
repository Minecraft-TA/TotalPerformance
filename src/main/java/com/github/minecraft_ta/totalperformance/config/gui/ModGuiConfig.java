package com.github.minecraft_ta.totalperformance.config.gui;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ModGuiConfig extends GuiConfig {

    public ModGuiConfig(GuiScreen parentScreen) {
        super(
                parentScreen,
                TotalPerformance.CONFIG.getConfigElements(),
                TotalPerformance.MOD_ID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(TotalPerformance.CONFIG.getConfig().toString())
        );
    }


}
