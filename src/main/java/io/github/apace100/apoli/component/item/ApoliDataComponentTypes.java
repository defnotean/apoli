package io.github.apace100.apoli.component.item;

import io.github.apace100.apoli.Apoli;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;

public class ApoliDataComponentTypes {

    public static final DataComponentType<ItemPowersComponent> POWERS = DataComponentType.<ItemPowersComponent>builder()
        .codec(ItemPowersComponent.CODEC)
        .packetCodec(ItemPowersComponent.PACKET_CODEC)
        .build();

    public static void register() {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Apoli.identifier("powers"), POWERS);
    }

}
