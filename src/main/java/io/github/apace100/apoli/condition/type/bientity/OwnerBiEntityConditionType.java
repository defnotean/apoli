package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OwnerBiEntityConditionType extends BiEntityConditionType {

	@Override
	public boolean test(BiEntityConditionContext context) {

		Entity actor = context.actor();
		Entity target = context.target();

		return (target instanceof TamableAnimal tameableTarget && Objects.equals(actor, tameableTarget.getOwner()))
			|| (target instanceof OwnableEntity ownableTarget && Objects.equals(actor, ownableTarget.getOwner()));

	}

	@Override
	public BiEntityRequirement getRequirement() {
		return BiEntityRequirement.BOTH;
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return BiEntityConditionTypes.OWNER;
	}

}
