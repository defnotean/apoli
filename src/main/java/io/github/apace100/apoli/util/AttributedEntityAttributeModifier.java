package io.github.apace100.apoli.util;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.Holder;

public record AttributedEntityAttributeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {

}
