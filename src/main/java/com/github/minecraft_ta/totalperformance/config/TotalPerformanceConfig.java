package com.github.minecraft_ta.totalperformance.config;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class TotalPerformanceConfig {

    //event class -> triple(whitelist?, generic parameter, list of listeners)
    public Map<String, Triple<Boolean, String, List<String>>> eventBlockMap;

    private static final String CATEGORY_UNSAFE = "unsafe";
    private static final String CATEGORY_EVENTBLOCK = "eventBlock";

    private final Configuration config;

    public TotalPerformanceConfig(Configuration config) {
        this.config = config;

        this.loadConfig();
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
}
