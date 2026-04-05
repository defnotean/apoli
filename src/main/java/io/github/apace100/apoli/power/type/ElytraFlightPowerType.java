package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ElytraFlightPowerType extends PowerType {

    public static final TypedDataObjectFactory<ElytraFlightPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("texture_location", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("render_elytra", SerializableDataTypes.BOOLEAN),
        (data, condition) -> new ElytraFlightPowerType(
            data.get("texture_location"),
            data.get("render_elytra"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("texture_location", powerType.textureLocation)
            .set("render_elytra", powerType.renderElytra)
    );

    private final Optional<Identifier> textureLocation;
    private final boolean renderElytra;

    public ElytraFlightPowerType(Optional<Identifier> textureLocation, boolean renderElytra, Optional<EntityCondition> condition) {
        super(condition);
        this.textureLocation = textureLocation;
        this.renderElytra = renderElytra;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ELYTRA_FLIGHT;
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    public Optional<Identifier> getTextureLocation() {
        return textureLocation;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }

    /**
     * Handles elytra physics for this power type via Fabric's EntityElytraEvents.CUSTOM.
     * <p>
     * When {@code tickElytra} is {@code true}, this replicates vanilla elytra tick behavior:
     * velocity along the look direction, gravity deceleration, and durability damage on real elytra.
     * </p>
     */
    public static boolean integrateCustomCallback(LivingEntity entity, boolean tickElytra) {

        if (!PowerHolderComponent.hasPowerType(entity, ElytraFlightPowerType.class)) {
            return false;
        }

        if (tickElytra) {
            Vec3 velocity = entity.getDeltaMovement();
            Vec3 look = entity.getLookAngle();

            float pitchRad = entity.getXRot() * Mth.DEG_TO_RAD;

            double freeflightLength = velocity.length();

            // Pitch-based acceleration: accelerate in the look direction
            double lerpFactor = Math.cos(pitchRad);
            lerpFactor = lerpFactor * lerpFactor * Math.min(1.0D, freeflightLength / 0.5D);

            velocity = velocity.add(
                look.x * (lerpFactor * 0.08D - velocity.x * 0.02D),
                look.y * (lerpFactor * 0.08D - velocity.y * 0.02D) + 0.08D * (Math.cos(pitchRad * 2.0D) + 1.0D) * (-0.02D),
                look.z * (lerpFactor * 0.08D - velocity.z * 0.02D)
            );

            // Apply aerodynamic drag
            float drag = entity.isSprinting() ? 0.0F : 1.0F;
            velocity = new Vec3(
                velocity.x * 0.99D + (look.x * 0.0D - velocity.x) * 0.014D * drag,
                velocity.y * 0.98D,
                velocity.z * 0.99D + (look.z * 0.0D - velocity.z) * 0.014D * drag
            );

            entity.setDeltaMovement(velocity);

            // Damage real elytra durability (vanilla: 1 per 10 ticks) only for players wearing elytra
            if (entity instanceof Player player && !entity.level().isClientSide) {
                var chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chestStack.is(Items.ELYTRA) && player.tickCount % 10 == 0) {
                    chestStack.hurtEquipment(EquipmentSlot.CHEST, 1, player,
                        e -> entity.broadcastBreakEvent(EquipmentSlot.CHEST));
                }
            }
        }

        return true;
    }

}
