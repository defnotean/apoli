package io.github.apace100.apoli;

import io.github.apace100.apoli.integration.PowerIntegrationClient;
import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowerTypesC2SPacket;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliClassDataClient;
import io.github.apace100.apoli.screen.GameHudRender;
import io.github.apace100.apoli.screen.PowerHudRenderer;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.keybinding.KeyBindingUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ApoliClient implements ClientModInitializer {

	public static KeyMapping showPowersOnUsabilityHint;

	public static final Map<String, Boolean> lastKeyBindingStates = new HashMap<>();
	public static boolean shouldReloadWorldRenderer = false;

	@Override
	public void onInitializeClient() {

		showPowersOnUsabilityHint = KeyMappingHelper.registerKeyBinding(new KeyMapping("key.apoli.usability_hint.show_powers", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category." + Apoli.MODID));
		ModPacketsS2C.register();

		ApoliClassDataClient.registerAll();
		PowerIntegrationClient.register();

		GameHudRender.HUD_RENDERS.add(new PowerHudRenderer());

		AutoConfig.register(ApoliConfigClient.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigClient.class).getConfig();

	}

	public static <P extends PowerType & Active> void performActivePowerTypes(List<P> activePowerTypes) {

		List<Identifier> powerTypeIds = activePowerTypes
			.stream()
			.peek(pt -> {if (pt.isActive()) {pt.onUse();}})
			.map(PowerType::getPower)
			.map(Power::getId)
			.toList();

		if (!powerTypeIds.isEmpty()) {
			ClientPlayNetworking.send(new UseActivePowerTypesC2SPacket(powerTypeIds));
		}

	}

	/**
	 * 	Add aliases to {@link KeyBindingUtil#ALIASES} instead.
	 */
	@Deprecated(forRemoval = true)
	public static void registerPowerKeybinding(String keyId, KeyMapping keyBinding) {
		KeyBindingUtil.ALIASES.addAlias(keyId, keyBinding.getTranslationKey());
	}

	/**
	 * 	Use {@link KeyBindingUtil#getKeyBinding(String)} instead.
	 */
	@Deprecated(forRemoval = true)
	@Nullable
	public static KeyMapping getKeyBinding(String keyBindingId) {
		return KeyBindingUtil.getKeyBinding(keyBindingId).orElse(null);
	}

}
