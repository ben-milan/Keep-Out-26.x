package com.bmk.keepout.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class ConfigManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("keepout.properties");

    private static KeepoutConfig instance;

    public static KeepoutConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            writeDefaultTemplate();
        }

        KeepoutConfig cfg = new KeepoutConfig();

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Properties props = new Properties();
            props.load(reader);

            cfg.netherEnabled = getBool(props, "netherEnabled", cfg.netherEnabled);
            cfg.endEnabled = getBool(props, "endEnabled", cfg.endEnabled);

            cfg.leadInDelayTicks = getInt(props, "leadInDelayTicks", cfg.leadInDelayTicks);
            cfg.showTitle = getBool(props, "showTitle", cfg.showTitle);
            cfg.titleFadeIn = getInt(props, "titleFadeIn", cfg.titleFadeIn);
            cfg.titleStay = getInt(props, "titleStay", cfg.titleStay);
            cfg.titleFadeOut = getInt(props, "titleFadeOut", cfg.titleFadeOut);
            cfg.playSounds = getBool(props, "playSounds", cfg.playSounds);


            cfg.netherEnabledTitle = props.getProperty("netherEnabledTitle", cfg.netherEnabledTitle);
            cfg.endEnabledTitle = props.getProperty("endEnabledTitle", cfg.endEnabledTitle);

            cfg.decorateTitle = getBool(props, "decorateTitle", cfg.decorateTitle);
            cfg.boldTitle = getBool(props, "boldTitle", cfg.boldTitle);
            cfg.italicTitle = getBool(props, "italicTitle", cfg.italicTitle);
            cfg.underlineTitle = getBool(props, "underlineTitle", cfg.underlineTitle);

            cfg.netherPrimaryColor = props.getProperty("netherPrimaryColor", cfg.netherPrimaryColor);
            cfg.netherSecondaryColor = props.getProperty("netherSecondaryColor", cfg.netherSecondaryColor);
            cfg.endPrimaryColor = props.getProperty("endPrimaryColor", cfg.endPrimaryColor);
            cfg.endSecondaryColor = props.getProperty("endSecondaryColor", cfg.endSecondaryColor);

        } catch (IOException e) {
            e.printStackTrace();
        }

        instance = cfg;
    }

    public static void updateBoolen(String key, boolean value) {
        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH);
            boolean found = false;

            for (int i = 0; i < lines.size(); i++) {
                String trimmed = lines.get(i).trim();
                if (trimmed.startsWith(key + "=")) {
                    lines.set(i, key + "=" + value);
                    found = true;
                    break;
                }
            }

            if (found) {
                Files.write(CONFIG_PATH, lines);
            } else {
                System.err.println("[Keepout] Config key not found, skipping update: " + key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean getBool(Properties props, String key, boolean fallback) {
        String val = props.getProperty(key);
        return val != null ? Boolean.parseBoolean(val) : fallback;
    }

    private static int getInt(Properties props, String key, int fallback) {
        try {
            String val = props.getProperty(key);
            return val != null ? Integer.parseInt(val.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static float getFloat(Properties props, String key, float fallback) {
        try {
            String val = props.getProperty(key);
            return val != null ? Float.parseFloat(val.trim()) : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static void writeDefaultTemplate() {
        String template = """
                # Keepout mod configuration
                
                # Dimensions
                netherEnabled=true
                endEnabled=true
                
                # Sounds
                playSounds=true
                leadInDelayTicks=20
                
                # Titles
                netherEnabledTitle=Hell Unleashed
                endEnabledTitle=End Unlocked
                
                # Title animation
                showTitle=true
                titleFadeIn=10
                titleStay=70
                titleFadeOut=20
                
                # Title styling toggles
                decorateTitle=true
                boldTitle=true
                italicTitle=true
                underlineTitle=true
                
                # Title Color codes - see table: 0-9,a-f are colors
                # (https://minecraft.fandom.com/wiki/Formatting_codes#Use_in_server.properties_and_pack.mcmeta)
                netherPrimaryColor=4
                netherSecondaryColor=c
                endPrimaryColor=d
                endSecondaryColor=5
                """;

        try {
            Files.writeString(CONFIG_PATH, template);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}