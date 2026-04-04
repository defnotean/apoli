package io.github.apace100.apoli.integration;

import com.google.gson.JsonElement;
import io.github.apace100.apoli.power.Power;
import net.minecraft.resources.ResourceLocation;

/**
 * Use this callback by registering an additional data field with `PowerTypes.registerAdditionalData(...)`.
 */
public interface AdditionalPowerDataCallback {

    void readAdditionalPowerData(ResourceLocation powerId, ResourceLocation factoryId, boolean isSubPower, JsonElement data, Power power);
}
