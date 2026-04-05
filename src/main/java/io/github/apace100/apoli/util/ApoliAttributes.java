package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.resources.Identifier;

/**
 * Custom attributes that replace the dependency on the AdditionalEntityAttributes mod.
 * These cover lava vision and lava/swim speed that AEA previously provided.
 */
public class ApoliAttributes {

    /**
     * Controls the degree to which a player can see in lava (0.0 = no vision, 1.0 = full vision).
     * Used by lava_vision power type.
     */
    public static Holder<Attribute> LAVA_VISIBILITY;

    /**
     * Multiplicative modifier for the entity's movement speed while in lava.
     * Used by modify_lava_speed power type.
     */
    public static Holder<Attribute> LAVA_SPEED;

    /**
     * Multiplicative modifier for the entity's movement speed while in water.
     * Used by modify_swim_speed power type. Falls back to vanilla WATER_MOVEMENT_EFFICIENCY
     * semantics but as a raw multiplier.
     */
    public static Holder<Attribute> WATER_SPEED;

    public static void register() {
        LAVA_VISIBILITY = registerAttribute(
            Apoli.identifier("lava_visibility"),
            "apoli.attribute.lava_visibility",
            0.0D, 0.0D, 1.0D
        );
        LAVA_SPEED = registerAttribute(
            Apoli.identifier("lava_speed"),
            "apoli.attribute.lava_speed",
            0.0D, 0.0D, 1024.0D
        );
        WATER_SPEED = registerAttribute(
            Apoli.identifier("water_speed"),
            "apoli.attribute.water_speed",
            0.0D, 0.0D, 1024.0D
        );
    }

    private static Holder<Attribute> registerAttribute(Identifier id, String translationKey, double defaultValue, double min, double max) {
        RangedAttribute attribute = new RangedAttribute(translationKey, defaultValue, min, max);
        attribute.setSentiment(Attribute.Sentiment.POSITIVE);
        return Registry.registerForHolder(
            BuiltInRegistries.ATTRIBUTE,
            Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath()),
            attribute
        );
    }

}
