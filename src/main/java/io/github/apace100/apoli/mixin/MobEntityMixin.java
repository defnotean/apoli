package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.type.ActionOnItemPickupPowerType;
import io.github.apace100.apoli.power.type.PreventItemPickupPowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeting {

    private MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @WrapWithCondition(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;loot(Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private boolean apoli$preventItemPickup(Mob mobEntity, ItemEntity itemEntity) {
        return !PreventItemPickupPowerType.doesPrevent(itemEntity, this);
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;loot(Lnet/minecraft/world/entity/item/ItemEntity;)V"))
    private void apoli$actionOnItemPickup(Mob mobEntity, ItemEntity itemEntity, Operation<Void> original) {

        SlotAccess stackReference = InventoryUtil.createStackReference(itemEntity.getItem());
        Entity thrower = EntityReference.getEntity(((ItemEntityAccessor) itemEntity).getThrower(), this.level());

        Prioritized.CallInstance<ActionOnItemPickupPowerType> callInstance = ActionOnItemPickupPowerType.executeItemAction(thrower, stackReference, this);
        itemEntity.setItem(stackReference.get());

        original.call(mobEntity, itemEntity);
        ActionOnItemPickupPowerType.executeBiEntityAction(callInstance, thrower);

    }

}
