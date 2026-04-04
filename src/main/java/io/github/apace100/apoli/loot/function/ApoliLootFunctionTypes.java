package io.github.apace100.apoli.loot.function;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.Apoli;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class ApoliLootFunctionTypes {

	public static final LootItemFunctionType<AddPowerLootFunction> ADD_POWER = register("add_power", AddPowerLootFunction.MAP_CODEC);
	public static final LootItemFunctionType<RemovePowerLootFunction> REMOVE_POWER = register("remove_power", RemovePowerLootFunction.MAP_CODEC);

	public static void register() {

	}

	public static <F extends LootItemFunction> LootItemFunctionType<F> register(String path, MapCodec<F> mapCodec) {
		return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Apoli.identifier(path), new LootItemFunctionType<>(mapCodec));
	}

}
