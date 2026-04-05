package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.type.ActionOnItemPickupPowerType;
import io.github.apace100.apoli.power.type.PreventItemPickupPowerType;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.MiscUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow
    private EntityReference<Entity> thrower;

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public abstract void setItem(ItemStack stack);

    private ItemEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean apoli$onItemPickup(Inventory playerInventory, ItemStack stack, Operation<Boolean> original, Player player) {

        if (PreventItemPickupPowerType.doesPrevent(thisAsItemEntity(), player)) {
            return false;
        }

        else if (MiscUtil.hasSpaceInInventory(playerInventory, stack)) {

            SlotAccess stackReference = InventoryUtil.createStackReference(stack);
            Entity throwerEntity = EntityReference.getEntity(this.thrower, this.level());

            Prioritized.CallInstance<ActionOnItemPickupPowerType> callInstance = ActionOnItemPickupPowerType.executeItemAction(throwerEntity, stackReference, player);
            this.setItem(stackReference.get());

            boolean result = original.call(playerInventory, this.getItem());
            if (result) {
                ActionOnItemPickupPowerType.executeBiEntityAction(callInstance, throwerEntity);
            }

            return result;

        }

        else {
            return original.call(playerInventory, stack);
        }

    }

    @Unique
    private ItemEntity thisAsItemEntity() {
        return (ItemEntity) (Object) this;
    }

}
