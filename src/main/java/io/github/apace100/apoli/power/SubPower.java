package io.github.apace100.apoli.power;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class SubPower extends Power {

    protected static final Function<Power, PacketCodec<RegistryByteBuf, SubPower>> PACKET_CODEC = power -> new PacketCodec<>() {

        @Override
        public SubPower decode(RegistryByteBuf buf) {

            ResourceLocation superPowerId = buf.readIdentifier();
            String subName = buf.readString();

            return new SubPower(superPowerId, subName, power);

        }

        @Override
        public void encode(RegistryByteBuf buf, SubPower value) {
            buf.writeIdentifier(value.getSuperPowerId());
            buf.writeString(value.getSubName());
        }

    };

    private final ResourceLocation superPowerId;
    private final String subName;

    SubPower(ResourceLocation superPowerId, String subName, Power basePower) {
        super(basePower);
        this.superPowerId = superPowerId;
        this.subName = subName;
    }

    public ResourceLocation getSuperPowerId() {
        return superPowerId;
    }

    public String getSubName() {
        return subName;
    }

}
