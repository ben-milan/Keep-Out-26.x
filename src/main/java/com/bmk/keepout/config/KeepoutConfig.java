package com.bmk.keepout.config;

public class KeepoutConfig {
    // Default on server start
    public boolean netherEnabled = true;
    public boolean endEnabled = true;

    // Customization
    public String netherEnabledTitle = "Hell Unleashed";
    public String endEnabledTitle = "End Unlocked";
    public String netherPrimaryColor = "4";
    public String netherSecondaryColor = "c";
    public String endPrimaryColor = "d";
    public String endSecondaryColor = "5";
    public boolean decorateTitle = true;
    public boolean boldTitle = true;
    public boolean italicTitle = true;
    public boolean underlineTitle = true;
    public boolean playSounds = true;
    public boolean showTitle = true;

    // Timing in server ticks
    public int leadInDelayTicks = 20;
    public int titleFadeIn = 10;
    public int titleStay = 70;
    public int titleFadeOut = 20;




}
