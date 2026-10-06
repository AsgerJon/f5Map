package io.github.asgerjon.f5map.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.asgerjon.f5map.F5MapConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState.HandRenderSelection;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.component.DataComponents;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets the vanilla first-person hand pass run in third person while a filled
 * map is held in the small one-handed pose, restricted to those hands.
 * Controlled by {@link F5MapConfig}.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    /**
     * Vanilla only runs the hand pass when {@code cameraType.isFirstPerson()}.
     * In third person, pretend we are in first person if the settings allow
     * this camera mode and a hand holds a filled map in the one-handed pose,
     * and remember which hands those are for the submit call below. The other
     * vanilla conditions (panorama, sleeping, F1, spectator) still apply.
     */
    @WrapOperation(
            method = "renderItemInHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z")
    )
    private boolean allowMapsInThirdPerson(
            CameraType cameraType,
            Operation<Boolean> original,
            @Local(argsOnly = true) PlayerRenderState playerState,
            @Share("mapHands") LocalRef<HandRenderSelection> mapHands
    ) {
        if (original.call(cameraType)) {
            return true;
        }
        boolean allowedInThisMode = cameraType.isMirrored() ? F5MapConfig.thirdPersonFront : F5MapConfig.thirdPersonBack;
        if (!F5MapConfig.enabled || !allowedInThisMode) {
            return false;
        }
        HandRenderSelection selection = mapHandsOnly(playerState.firstPersonHandsAndItems);
        mapHands.set(selection);
        return selection != null;
    }

    /**
     * In third person, swap the hand selection for one that only includes map
     * hands, so no bare arm or other held item gets drawn.
     */
    @WrapOperation(
            method = "renderItemInHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;submitHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;)V"
            )
    )
    private void restrictToMapHands(
            FirstPersonHandsAndItemsRenderer renderer,
            float partialTicks,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState state,
            Operation<Void> original,
            @Share("mapHands") LocalRef<HandRenderSelection> mapHands
    ) {
        HandRenderSelection selection = mapHands.get();
        if (selection == null) {
            original.call(renderer, partialTicks, poseStack, submitNodeCollector, playerState, state);
            return;
        }
        HandRenderSelection vanillaSelection = state.handRenderSelection;
        state.handRenderSelection = selection;
        try {
            original.call(renderer, partialTicks, poseStack, submitNodeCollector, playerState, state);
        } finally {
            state.handRenderSelection = vanillaSelection;
        }
    }

    /**
     * The hands vanilla would render that hold a filled map in the one-handed
     * pose, using the same checks the renderer uses to pick its map pose, or
     * null if there are none. A main-hand map with an empty offhand gets the
     * centered two-handed pose, which is only included if the settings allow.
     */
    private static @Nullable HandRenderSelection mapHandsOnly(FirstPersonHandsAndItemsRenderState state) {
        HandRenderSelection vanillaSelection = state.handRenderSelection;
        if (vanillaSelection == null) {
            return null;
        }
        boolean mainHand = vanillaSelection.renderMainHand
                && state.mainHandItem.has(DataComponents.MAP_ID)
                && (F5MapConfig.bigMapInThirdPerson || !state.offHandItem.isEmpty());
        boolean offHand = vanillaSelection.renderOffHand && state.offHandItem.has(DataComponents.MAP_ID);
        if (mainHand && offHand) {
            return HandRenderSelection.RENDER_BOTH_HANDS;
        } else if (mainHand) {
            return HandRenderSelection.RENDER_MAIN_HAND_ONLY;
        } else if (offHand) {
            return HandRenderSelection.RENDER_OFF_HAND_ONLY;
        }
        return null;
    }
}
