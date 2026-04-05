package io.github.apace100.apoli.data;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.HashSet;
import java.util.Set;

public class ApoliDataHandlers {

    public static final EntityDataSerializer<Set<String>> STRING_SET = EntityDataSerializer.create(ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.string(32767)));

    public static void register() {
        EntityDataSerializers.register(STRING_SET);
    }

}
