package io.github.asgerjon.f5map;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * f5Map settings, stored in {@code config/f5map.cfg} as {@code key = value}
 * lines. Read once at startup and written whenever the settings screen
 * closes, so the file can be edited by hand while the game is not running.
 */
public final class F5MapConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("f5map");
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("f5map.cfg");

    public static boolean enabled = true;
    public static boolean thirdPersonBack = true;
    public static boolean thirdPersonFront = true;
    public static boolean keepMapsRaisedInBoats = true;
    public static boolean bigMapInThirdPerson = false;

    private F5MapConfig() {
    }

    /**
     * Reads the file, then writes it back so a missing file or missing keys
     * get filled in with their defaults.
     */
    public static void load() {
        if (Files.exists(PATH)) {
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(PATH)) {
                properties.load(reader);
            } catch (IOException e) {
                LOGGER.error("Could not read {}, using defaults", PATH, e);
            }
            enabled = readBoolean(properties, "enabled", enabled);
            thirdPersonBack = readBoolean(properties, "thirdPersonBack", thirdPersonBack);
            thirdPersonFront = readBoolean(properties, "thirdPersonFront", thirdPersonFront);
            keepMapsRaisedInBoats = readBoolean(properties, "keepMapsRaisedInBoats", keepMapsRaisedInBoats);
            bigMapInThirdPerson = readBoolean(properties, "bigMapInThirdPerson", bigMapInThirdPerson);
        }
        save();
    }

    public static void save() {
        String contents = """
                # f5Map settings. Edit while the game is closed, or in game through Mod Menu.
                # Every value is true or false.
                
                # Turn the whole mod on or off.
                enabled = %s
                
                # Show held maps in back third person (camera behind you).
                thirdPersonBack = %s
                
                # Show held maps in front third person (camera facing you).
                thirdPersonFront = %s
                
                # Keep held maps raised while paddling a boat, in first and third person.
                keepMapsRaisedInBoats = %s
                
                # Also show the big two-handed map (main-hand map, empty offhand) in third person.
                bigMapInThirdPerson = %s
                """.formatted(enabled, thirdPersonBack, thirdPersonFront, keepMapsRaisedInBoats, bigMapInThirdPerson);
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, contents);
        } catch (IOException e) {
            LOGGER.error("Could not write {}", PATH, e);
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        }
        LOGGER.warn("Invalid value '{}' for {} in {}, using {}", value, key, PATH, defaultValue);
        return defaultValue;
    }
}
