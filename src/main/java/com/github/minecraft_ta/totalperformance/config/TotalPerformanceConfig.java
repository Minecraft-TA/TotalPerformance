package com.github.minecraft_ta.totalperformance.config;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class TotalPerformanceConfig {

    private static final String CATEGORY_UNSAFE = "unsafe";
    private static final String CATEGORY_EVENTBLOCK = "eventBlock";
    private static final String CATEGORY_MISC = "MISC";
    private final Configuration config;
    //event class -> triple(whitelist?, generic parameter, list of listeners)
    public Map<String, Triple<Boolean, String, List<String>>> eventBlockMap;
    public int maxPacketSize;
    public boolean doDragonParticles;
    public boolean loadSpawnChunks;
    public int autoSaveInterval;

    public TotalPerformanceConfig(Configuration config) {
        this.config = config;

        this.loadConfig();
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(TotalPerformance.MOD_ID)) {
            loadConfig();
        }
    }

    private void loadConfig() {
        this.eventBlockMap = new HashMap<>();

        ConfigCategory eventBlockCategory = this.config.getCategory(CATEGORY_UNSAFE + Configuration.CATEGORY_SPLITTER + CATEGORY_EVENTBLOCK);
        if (eventBlockCategory != null) {
            for (ConfigCategory child : eventBlockCategory.getChildren()) {
                if (child.getName().equals("whitelist"))
                    parseEventBlockCategory(child, true);
                else if (child.getName().equals("blacklist"))
                    parseEventBlockCategory(child, false);
            }
        }
        maxPacketSize = this.config.getInt("maxPacketSize", CATEGORY_MISC, 32767, 0, Integer.MAX_VALUE, "Sets a custom max size for the packet in CPacketCustomPayload");
        doDragonParticles = this.config.getBoolean("doDragonParticles", CATEGORY_MISC, true, "Set to false to disable the dragon particles");
        loadSpawnChunks = this.config.getBoolean("loadSpawnChunks", CATEGORY_MISC, true, "Set to false to disable the spawn chunks from loading");
        autoSaveInterval = this.config.getInt("autoSaveInterval", CATEGORY_MISC, 900, 0, Integer.MAX_VALUE, "Sets the interval in ticks for the server to save the world");

        if (this.config.hasChanged())
            this.config.save();
    }

    private void parseEventBlockCategory(ConfigCategory category, boolean whitelist) {
        if (category == null || category.entrySet().size() < 1)
            return;

        for (Map.Entry<String, Property> entry : category.entrySet()) {
            String key = entry.getKey();
            boolean withGeneric = key.contains("<");

            //invalid syntax
            if (!entry.getValue().isList() || (withGeneric && !key.contains(">")))
                continue;

            String eventClass = withGeneric ? key.substring(0, key.indexOf('<')) : key;
            String genericClass = withGeneric ? key.substring(key.indexOf('<') + 1, key.indexOf('>')) : null;

            List<String> blockedListeners = Collections.unmodifiableList(Arrays.asList(entry.getValue().getStringList()));

            this.eventBlockMap.put(eventClass, Triple.of(whitelist, genericClass, blockedListeners));
        }
    }

    public List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();

        list.add(new ConfigElement(config.getCategory(CATEGORY_MISC)));

        return list;
    }

    public Configuration getConfig() {
        return config;
    }

}
