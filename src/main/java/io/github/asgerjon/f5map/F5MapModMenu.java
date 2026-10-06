package io.github.asgerjon.f5map;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu entrypoint. Only loaded when Mod Menu is installed.
 */
public class F5MapModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return F5MapConfigScreen::new;
    }
}
