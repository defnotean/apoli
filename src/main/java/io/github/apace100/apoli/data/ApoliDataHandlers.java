package io.github.apace100.apoli.data;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.TrackedDataHandlerRegistry;
import net.minecraft.network.codec.PacketCodecs;

import java.util.HashSet;
import java.util.Set;

public class ApoliDataHandlers {

    public static final EntityDataSerializer<Set<String>> STRING_SET = EntityDataSerializer.create(PacketCodecs.collection(HashSet::new, PacketCodecs.string(32767)));

    public static void register() {
        TrackedDataHandlerRegistry.register(STRING_SET);
    }

}
