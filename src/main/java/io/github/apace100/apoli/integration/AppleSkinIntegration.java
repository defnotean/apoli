package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.EdibleItemPowerType;
import io.github.apace100.apoli.power.type.ModifyFoodPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;

import java.util.List;

public class AppleSkinIntegration implements AppleSkinApi {

    @Override
    public void registerEvents() {

        FoodValuesEvent.EVENT.register(event -> {

            Player player = event.player;
            ItemStack stack = event.itemStack;

            EdibleItemPowerType.get(stack, player)
                .map(EdibleItemPowerType::getFoodComponent)
                .ifPresent(foodComponent -> event.modifiedFoodComponent = foodComponent);

            List<ModifyFoodPowerType> modifyFoodPowers = PowerHolderComponent.getPowerTypes(player, ModifyFoodPowerType.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .toList();
            if (modifyFoodPowers.isEmpty()) {
                return;
            }

            FoodProperties originalFoodComponent = !event.modifiedFoodComponent.equals(event.defaultFoodComponent)
                ? event.modifiedFoodComponent
                : event.defaultFoodComponent;

            List<Modifier> nutritionModifiers = modifyFoodPowers
                .stream()
                .flatMap(p -> p.getFoodModifiers().stream())
                .toList();
            List<Modifier> saturationModifiers = modifyFoodPowers
                .stream()
                .flatMap(p -> p.getSaturationModifiers().stream())
                .toList();

            int newNutrition = (int) ModifierUtil.applyModifiers(player, nutritionModifiers, originalFoodComponent.nutrition());
            float newSaturation = (float) ModifierUtil.applyModifiers(player, saturationModifiers, originalFoodComponent.saturation());

            event.modifiedFoodComponent = new FoodProperties(
                newNutrition,
                newSaturation,
                originalFoodComponent.canAlwaysEat(),
                originalFoodComponent.eatSeconds(),
                originalFoodComponent.usingConvertsTo(),
                originalFoodComponent.effects()
            );

        });

    }

}
