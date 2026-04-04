package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class ValueModifyingPowerType extends PowerType {

    private final List<Modifier> modifiers;

    public ValueModifyingPowerType(List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(condition);
        this.modifiers = new ObjectArrayList<>(modifiers);
    }

    public ValueModifyingPowerType(Optional<EntityCondition> condition) {
        super(condition);
        this.modifiers = new ObjectArrayList<>();
    }

    public ValueModifyingPowerType() {
        this(Optional.empty());
    }

    protected void addModifier(Modifier modifier) {
        this.modifiers.add(modifier);
    }

    public List<Modifier> getModifiers() {
        return new ObjectArrayList<>(modifiers);
    }

    public static SerializableData addRequiredModifiersField(SerializableData serializableData) {
        return serializableData
            .add("modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrNull(data.get("modifier")))
            .validate(MiscUtil.validateAnyFieldsPresent("modifier", "modifiers"));
    }

    public static SerializableData addModifiersField(SerializableData serializableData) {
        return serializableData
            .add("modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("modifier")));
    }

    public static List<Modifier> getModifiersField(SerializableData.Instance data) {
        return data.get("modifiers");
    }

    public static SerializableData.Instance setModifiersField(SerializableData.Instance data, List<Modifier> modifiers) {
        return data.set("modifiers", modifiers);
    }

    public static <T extends ValueModifyingPowerType> TypedDataObjectFactory<T> createModifyingDataFactory(SerializableData serializableData, BiFunction<SerializableData.Instance, List<Modifier>, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return TypedDataObjectFactory.simple(
            addModifiersField(serializableData),
            data -> fromData.apply(
                data,
                getModifiersField(data)
            ),
			(t, _serializableData) ->
                setModifiersField(toData.apply(t, _serializableData), t.getModifiers())
        );
    }

    public static <T extends ValueModifyingPowerType> TypedDataObjectFactory<T> createModifyingRequiredDataFactory(SerializableData serializableData, BiFunction<SerializableData.Instance, List<Modifier>, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return TypedDataObjectFactory.simple(
            addRequiredModifiersField(serializableData),
            data -> fromData.apply(
                data,
                getModifiersField(data)
            ),
            (t, _serializableData) ->
                setModifiersField(toData.apply(t, _serializableData), t.getModifiers())
        );
    }

    public static <T extends ValueModifyingPowerType> TypedDataObjectFactory<T> createConditionedModifyingRequiredDataFactory(SerializableData serializableData, TriFunction<SerializableData.Instance, List<Modifier>, Optional<EntityCondition>, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return PowerType.createConditionedDataFactory(
            addRequiredModifiersField(serializableData),
            (data, condition) -> fromData.apply(
                data,
                getModifiersField(data),
                condition
            ),
            (t, _serializableData) ->
                setModifiersField(toData.apply(t, _serializableData), t.getModifiers())
        );
    }

    public static <T extends ValueModifyingPowerType> TypedDataObjectFactory<T> createConditionedModifyingDataFactory(SerializableData serializableData, TriFunction<SerializableData.Instance, List<Modifier>, Optional<EntityCondition>, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return PowerType.createConditionedDataFactory(
            addModifiersField(serializableData),
            (data, condition) -> fromData.apply(
                data,
                getModifiersField(data),
                condition
            ),
            (t, _serializableData) ->
                setModifiersField(toData.apply(t, _serializableData), t.getModifiers())
        );
    }

    public static <T extends ValueModifyingPowerType> PowerConfiguration<T> createModifyingConfiguration(Identifier id, BiFunction<List<Modifier>, Optional<EntityCondition>, T> constructor) {
        return PowerConfiguration.of(id, createConditionedModifyingRequiredDataFactory(new SerializableData(), (data, modifiers, condition) -> constructor.apply(modifiers, condition), (t, serializableData) -> serializableData.instance()));
    }

}
