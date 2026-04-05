package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public record SyncEntityTypeTagCacheS2CPacket(Map<Identifier, Collection<Identifier>> subTags) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<SyncEntityTypeTagCacheS2CPacket> PACKET_ID = new CustomPacketPayload.Type<>(Apoli.identifier("s2c/sync_entity_type_tag_cache"));
	public static final StreamCodec<FriendlyByteBuf, SyncEntityTypeTagCacheS2CPacket> PACKET_CODEC = StreamCodec.of(SyncEntityTypeTagCacheS2CPacket::write, SyncEntityTypeTagCacheS2CPacket::read);

	private static SyncEntityTypeTagCacheS2CPacket read(FriendlyByteBuf buf) {
		return new SyncEntityTypeTagCacheS2CPacket(buf.readMap(FriendlyByteBuf::readIdentifier, valBuf -> valBuf.readCollection(ArrayList::new, FriendlyByteBuf::readIdentifier)));
	}

	private void write(FriendlyByteBuf buf) {
		buf.writeMap(subTags, FriendlyByteBuf::writeIdentifier, (valBuf, value) -> valBuf.writeCollection(value, FriendlyByteBuf::writeIdentifier));
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}

}
