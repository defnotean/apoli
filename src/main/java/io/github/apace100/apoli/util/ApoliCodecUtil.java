package io.github.apace100.apoli.util;

import com.mojang.datafixers.util.Either;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.Function;

public class ApoliCodecUtil {

    public static <P, S, B extends ByteBuf> StreamCodec<B, P> withAlternativeStreamCodec(StreamCodec<B, P> primary, StreamCodec<B, S> secondary, Function<S, P> converter) {
        return ByteBufCodecs.either(
            primary,
            secondary
        ).map(
            fsEither -> fsEither.map(p -> p, converter),
            Either::left
        );
    }

}
