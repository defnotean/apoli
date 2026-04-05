package io.github.apace100.apoli.component.item;

import io.github.apace100.apoli.Apoli;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class ApoliDataComponentTypes {

    @SuppressWarnings("unchecked")
    public static final DataComponentType<ItemPowersComponent> POWERS = DataComponentType.<ItemPowersComponent>builder()
        .persistent(ItemPowersComponent.CODEC)
        .networkSynchronized((net.minecraft.network.codec.StreamCodec) ItemPowersComponent.PACKET_CODEC)
        .build();

    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Apoli.identifier("powers"), POWERS);
    }

}
