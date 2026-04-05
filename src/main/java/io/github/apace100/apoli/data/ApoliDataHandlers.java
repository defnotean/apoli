package io.github.apace100.apoli.data;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.HashSet;
import java.util.Set;

public class ApoliDataHandlers {

    @SuppressWarnings("unchecked")
    public static final EntityDataSerializer<Set<String>> STRING_SET = EntityDataSerializer.forValueType(
        (net.minecraft.network.codec.StreamCodec) ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.stringUtf8(32767))
    );

    public static void register() {
        EntityDataSerializers.registerSerializer(STRING_SET);
    }

}
