package io.github.apace100.apoli.power.type;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.function.BiConsumer;

public interface AttributeModifying {

	List<AttributedEntityAttributeModifier> attributedModifiers();

	boolean shouldUpdateHealth();

	default void processModifiers(LivingEntity entity, BiConsumer<AttributeModifier, AttributeInstance> processor) {

		if (entity.level().isClientSide()) {
			return;
		}

		float prevMaxHealth = entity.getMaxHealth();
		float prevMaxHealthPercent = entity.getHealth() / prevMaxHealth;

		attributedModifiers()
			.stream()
			.map(mod -> Pair.of(mod, entity.getAttribute(mod.attribute())))
			.filter(pair -> pair.getSecond() != null)
			.forEach(pair -> processor.accept(pair.getFirst().modifier(), pair.getSecond()));

		float currMaxHealth = entity.getMaxHealth();
		if (shouldUpdateHealth() && currMaxHealth != prevMaxHealth) {
			entity.setHealth(currMaxHealth * prevMaxHealthPercent);
		}

	}

	default void addTemporaryModifiers(LivingEntity entity) {
		processModifiers(entity, (modifier, attributeInstance) -> {

			if (!attributeInstance.hasModifier(modifier.id())) {
				attributeInstance.addTransientModifier(modifier);
			}

		});
	}

	default void addPersistentModifiers(LivingEntity entity) {
		processModifiers(entity, (modifier, attributeInstance) -> attributeInstance.addOrReplacePermanentModifier(modifier));
	}

	default void removeModifiers(LivingEntity entity) {
		processModifiers(entity, (modifier, attributeInstance) -> attributeInstance.removeModifier(modifier));
	}

}
