package io.github.asgerjon.f5map.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.asgerjon.f5map.F5MapConfig;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps held maps raised while paddling a boat.
 */
@Mixin(FirstPersonHandsAndItems.class)
public abstract class FirstPersonHandsAndItemsMixin {
    /**
     * Vanilla lowers both hands while {@code player.isHandsBusy()}, which is
     * true while steering a boat. Treat the hands as free when a filled map is
     * held, so it stays visible. Only this call is changed; attacking and using
     * items are still blocked while paddling. Controlled by
     * {@link F5MapConfig#keepMapsRaisedInBoats}.
     */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isHandsBusy()Z")
    )
    private boolean keepMapsRaised(boolean handsBusy, @Local(argsOnly = true) LocalPlayer player) {
        if (!F5MapConfig.enabled || !F5MapConfig.keepMapsRaisedInBoats) {
            return handsBusy;
        }
        return handsBusy
                && !player.getMainHandItem().has(DataComponents.MAP_ID)
                && !player.getOffhandItem().has(DataComponents.MAP_ID);
    }
}
