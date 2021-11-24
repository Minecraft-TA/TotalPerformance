package com.github.minecraft_ta.totalperformance.config;

import com.github.minecraft_ta.totalperformance.TotalPerformance;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TotalPerformanceConfig {

    private static final String CATEGORY_UNSAFE = "unsafe";
    private static final String CATEGORY_EVENTBLOCK = "eventBlock";
    private static final String CATEGORY_MISC = "misc";
    private static final String CATEGORY_DIM_THREADING = "dimThreading";
    private static final String CATEGORY_SYNCHRONIZED_LISTENERS = "synchronizedListeners";

    private final Configuration config;

    //Class of event -> Triple(whitelist?, generic parameter, list of listeners)
    public Map<String, Triple<Boolean, String, Set<String>>> eventBlockMap;
    /**
     * Class of event -> Pair(whitelist?, list of listeners)
     * <br><br>
     * Whitelist: All listeners except for the whitelisted ones are allowed to run in parallel
     * <br>
     * Blacklist: No listener is allowed to run in parallel except for the blacklisted ones
     */
    public Map<String, Pair<Boolean, Set<String>>> synchronizedListenersMap;

    public int maxPacketSize;

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
        this.synchronizedListenersMap = new ConcurrentHashMap<>();

        ConfigCategory eventBlockCategory = this.config.getCategory(CATEGORY_UNSAFE + Configuration.CATEGORY_SPLITTER + CATEGORY_EVENTBLOCK);
        if (eventBlockCategory != null) {
            for (ConfigCategory child : eventBlockCategory.getChildren()) {
                if (child.getName().equals("whitelist"))
                    parseEventBlockCategory(child, true);
                else if (child.getName().equals("blacklist"))
                    parseEventBlockCategory(child, false);
            }
        }

        ConfigCategory dimThreadingCategory = this.config.getCategory(CATEGORY_DIM_THREADING + Configuration.CATEGORY_SPLITTER + CATEGORY_SYNCHRONIZED_LISTENERS);
        if (dimThreadingCategory != null) {
            for (ConfigCategory child : dimThreadingCategory.getChildren()) {
                if (child.getName().equals("whitelist"))
                    parseSynchronizedListenersCategory(child, true);
                else if (child.getName().equals("blacklist"))
                    parseSynchronizedListenersCategory(child, false);
            }
        }

        this.maxPacketSize = this.config.getInt("maxPacketSize", CATEGORY_MISC, 32767, 0, Integer.MAX_VALUE, "Sets a custom max size for the packet in CPacketCustomPayload");

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

            Set<String> blockedListeners = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(entry.getValue().getStringList())));

            this.eventBlockMap.put(eventClass, Triple.of(whitelist, genericClass, blockedListeners));
        }
    }

    private void parseSynchronizedListenersCategory(ConfigCategory category, boolean whitelist) {
        if (category == null || category.entrySet().size() < 1)
            return;

        for (Map.Entry<String, Property> entry : category.entrySet()) {
            //invalid syntax
            if (!entry.getValue().isList())
                continue;

            Set<String> listeners = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(entry.getValue().getStringList())));
            this.synchronizedListenersMap.put(entry.getKey(), Pair.of(whitelist, listeners));
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
