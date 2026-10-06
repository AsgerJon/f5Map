package io.github.asgerjon.f5map;

import net.fabricmc.api.ClientModInitializer;

public class F5MapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        F5MapConfig.load();
    }
}
