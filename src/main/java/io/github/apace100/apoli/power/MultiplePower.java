package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultiplePower extends Power {

    protected static final Function<Power, PacketCodec<RegistryByteBuf, MultiplePower>> PACKET_CODEC = power -> new PacketCodec<>() {

		@Override
		public MultiplePower decode(RegistryByteBuf buf) {
			Set<ResourceLocation> subPowerIds = buf.readCollection(ObjectLinkedOpenHashSet::new, FriendlyByteBuf::readIdentifier);
			return new MultiplePower(power, subPowerIds);

		}

		@Override
		public void encode(RegistryByteBuf buf, MultiplePower value) {
			buf.writeCollection(value.getSubPowerIds(), FriendlyByteBuf::writeIdentifier);
		}

	};

    private ImmutableSet<ResourceLocation> subPowerIds;

    MultiplePower(Power basePower, Set<ResourceLocation> subPowerIds) {
        super(basePower);
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    MultiplePower(Power basePower) {
        super(basePower);
		this.subPowerIds = ImmutableSet.of();
    }

    public ImmutableSet<ResourceLocation> getSubPowerIds() {
        return subPowerIds;
    }

    void setSubPowerIds(Set<ResourceLocation> subPowerIds) {
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    public Set<SubPower> getSubPowers() {
        return this.getSubPowerIds()
            .stream()
            .filter(PowerManager::contains)
            .map(PowerManager::get)
            .filter(SubPower.class::isInstance)
            .map(SubPower.class::cast)
            .collect(Collectors.toCollection(HashSet::new));
    }

}
