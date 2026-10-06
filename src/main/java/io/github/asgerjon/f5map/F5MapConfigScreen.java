package io.github.asgerjon.f5map;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

/**
 * Settings screen opened from Mod Menu. Changes apply immediately and are
 * written to {@code config/f5map.cfg} when the screen closes.
 */
public class F5MapConfigScreen extends OptionsSubScreen {
    public F5MapConfigScreen(Screen lastScreen) {
        super(lastScreen, Minecraft.getInstance().options, Component.literal("f5Map Settings"));
    }

    @Override
    protected void addOptions() {
        // Set by OptionsSubScreen.addContents() right before it calls this.
        OptionsList list = Objects.requireNonNull(this.list);
        list.addBig(toggle(
                "Enabled", "Turn the whole mod on or off.",
                F5MapConfig.enabled, value -> F5MapConfig.enabled = value
        ));
        list.addSmall(List.of(
                toggle(
                        "Back F5", "Show held maps in back third person (camera behind you).",
                        F5MapConfig.thirdPersonBack, value -> F5MapConfig.thirdPersonBack = value
                ),
                toggle(
                        "Front F5", "Show held maps in front third person (camera facing you).",
                        F5MapConfig.thirdPersonFront, value -> F5MapConfig.thirdPersonFront = value
                ),
                toggle(
                        "Map Up in Boats", "Keep held maps raised while paddling a boat, in first and third person.",
                        F5MapConfig.keepMapsRaisedInBoats, value -> F5MapConfig.keepMapsRaisedInBoats = value
                ),
                toggle(
                        "Big Map in F5", "Also show the big two-handed map (main-hand map, empty offhand) in third person.",
                        F5MapConfig.bigMapInThirdPerson, value -> F5MapConfig.bigMapInThirdPerson = value
                )
        ));
    }

    @Override
    public void removed() {
        F5MapConfig.save();
    }

    private static CycleButton<Boolean> toggle(String name, String description, boolean value, BooleanConsumer setter) {
        Tooltip tooltip = Tooltip.create(Component.literal(description));
        return CycleButton.onOffBuilder(value)
                .withTooltip(current -> tooltip)
                .create(Component.literal(name), (button, newValue) -> setter.accept(newValue.booleanValue()));
    }
}
