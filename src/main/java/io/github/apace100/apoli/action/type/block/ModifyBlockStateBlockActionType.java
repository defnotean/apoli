package io.github.apace100.apoli.action.type.block;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.context.BlockActionContext;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

public class ModifyBlockStateBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<ModifyBlockStateBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("property", SerializableDataTypes.STRING)
            .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD)
            .add("change", SerializableDataTypes.INT.optional(), Optional.empty())
            .add("value", SerializableDataTypes.BOOLEAN.optional(), Optional.empty())
            .add("enum", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("cycle", SerializableDataTypes.BOOLEAN, false),
        data -> new ModifyBlockStateBlockActionType(
            data.get("property"),
            data.get("operation"),
            data.get("change"),
            data.get("value"),
            data.get("enum"),
            data.get("cycle")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("property", actionType.property)
            .set("operation", actionType.operation)
            .set("change", actionType.change)
            .set("value", actionType.boolValue)
            .set("enum", actionType.enumValue)
            .set("cycle", actionType.cycle)
    );

    private final String property;

    private final ResourceOperation operation;
    private final Optional<Integer> change;

    private final Optional<Boolean> boolValue;
    private final Optional<String> enumValue;

    private final boolean cycle;

    public ModifyBlockStateBlockActionType(String property, ResourceOperation operation, Optional<Integer> change, Optional<Boolean> boolValue, Optional<String> enumValue, boolean cycle) {
        this.property = property;
        this.operation = operation;
        this.change = change;
        this.boolValue = boolValue;
        this.enumValue = enumValue;
        this.cycle = cycle;
    }

    @Override
    public void accept(BlockActionContext context) {

        Level world = context.world();
        BlockPos pos = context.pos();

        BlockState blockState = world.getBlockState(pos);
        Property<?> blockProperty = blockState.getProperties()
            .stream()
            .filter(prop -> prop.getName().equals(property))
            .findFirst()
            .orElse(null);

        if (blockProperty == null) {
            return;
        }

        if (cycle) {
            world.setBlock(pos, blockState.cycle(blockProperty));
            return;
        }

        switch (blockProperty) {
            case EnumProperty<?> enumProp when enumValue.isPresent() && !enumValue.get().isEmpty() ->
                setEnumProperty(enumProp, enumValue.get(), world, pos, blockState);
            case BooleanProperty boolProp when boolValue.isPresent() ->
                world.setBlock(pos, blockState.with(boolProp, boolValue.get()));
            case IntegerProperty intProp when change.isPresent() -> {

                int newValue = switch (operation) {
                    case ADD ->
                        Optional.ofNullable(blockState.get(intProp)).orElse(0) + change.get();
                    case SET ->
                        change.get();
                };

                if (intProp.getValues().contains(newValue)) {
                    world.setBlock(pos, blockState.with(intProp, newValue));
                }

            }
            default -> {

            }
        }

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return BlockActionTypes.MODIFY_BLOCK_STATE;
    }

    private <T extends Enum<T> & StringRepresentable> void setEnumProperty(EnumProperty<T> property, String name, Level world, BlockPos pos, BlockState originalState) {
        property.parse(name).ifPresentOrElse(
            propValue ->
                world.setBlock(pos, originalState.with(property, propValue)),
            () -> {

                RegistryOps<JsonElement> jsonOps = world.registryAccess().getOps(JsonOps.INSTANCE);
                Optional<JsonElement> blockActionJson = BlockAction.DATA_TYPE.write(jsonOps, this.getAction()).result();

                Apoli.LOGGER.warn("Couldn't set enum property \"{}\" of block at {} to \"{}\" (with block action {})! Expected value to be any of {}", property.getName(), pos.toShortString(), name, blockActionJson.map(JsonElement::toString).orElse("<unknown>"), property.getValues().stream().map(StringRepresentable::asString).collect(Collectors.joining(", ")));

            }
        );
    }

}
