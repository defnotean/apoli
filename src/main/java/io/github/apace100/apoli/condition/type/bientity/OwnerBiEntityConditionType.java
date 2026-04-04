package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.util.requirement.BiEntityRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Ownable;
import net.minecraft.world.entity.Tameable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OwnerBiEntityConditionType extends BiEntityConditionType {

	@Override
	public boolean test(BiEntityConditionContext context) {

		Entity actor = context.actor();
		Entity target = context.target();

		return (target instanceof Tameable tameableTarget && Objects.equals(actor, tameableTarget.getOwner()))
			|| (target instanceof Ownable ownableTarget && Objects.equals(actor, ownableTarget.getOwner()));

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
