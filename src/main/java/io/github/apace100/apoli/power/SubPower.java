package io.github.apace100.apoli.power;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public class SubPower extends Power {

    protected static final Function<Power, StreamCodec<RegistryFriendlyByteBuf, SubPower>> PACKET_CODEC = power -> new StreamCodec<>() {

        @Override
        public SubPower decode(RegistryFriendlyByteBuf buf) {

            Identifier superPowerId = buf.readIdentifier();
            String subName = buf.readUtf();

            return new SubPower(superPowerId, subName, power);

        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SubPower value) {
            buf.writeIdentifier(value.getSuperPowerId());
            buf.writeUtf(value.getSubName());
        }

    };

    private final Identifier superPowerId;
    private final String subName;

    SubPower(Identifier superPowerId, String subName, Power basePower) {
        super(basePower);
        this.superPowerId = superPowerId;
        this.subName = subName;
    }

    public Identifier getSuperPowerId() {
        return superPowerId;
    }

    public String getSubName() {
        return subName;
    }

}
