package com.github.minecraft_ta.totalperformance.config;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

public class TotalPerformanceConfig {

    public List<String> whitelistedAttachCapabilityHandlers;

    private final Configuration config;

    public TotalPerformanceConfig(Configuration config) {
        this.config = config;

        this.loadConfig();
    }

    @SubscribeEvent
    public void onChange(ConfigChangedEvent e) {
        if (e.getModID().equals(TotalPerformance.MOD_ID)) {
            this.loadConfig();
        }
    }

    private void loadConfig() {
        this.whitelistedAttachCapabilityHandlers = Arrays.asList(config.getStringList("whitelistedAttachCapabilityHandlers",
                "unsafe", new String[]{
                        "baubles.common.event.EventHandlerItem",
                        "hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler"
                }, "Whitelist for the AttachCapabilityEvent (USE WITH CAUTION!)"));

        if (this.config.hasChanged())
            this.config.save();
    }
}
