package io.github.apace100.apoli.component;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.GainedPowerCriterion;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PowerHolderComponentImpl implements PowerHolderComponent {

    private final ConcurrentHashMap<Power, PowerType> powers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Power, Set<Identifier>> powerSources = new ConcurrentHashMap<>();

    private final LivingEntity owner;

    public PowerHolderComponentImpl(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasPower(Power power) {
        return powers.containsKey(power);
    }

    @Override
    public boolean hasPower(Power power, Identifier source) {
        return powerSources.containsKey(power) && powerSources.get(power).contains(source);
    }

    @Override
    public PowerType getPowerType(Power power) {
        return powers.get(power);
    }

    @Override
    public List<PowerType> getPowerTypes() {
        return new LinkedList<>(powers.values());
    }

    @Override
    public Set<Power> getPowers(boolean includeSubPowers) {
        return powers.keySet()
            .stream()
            .filter(p -> includeSubPowers || !p.isSubPower())
            .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass) {
        return getPowerTypes(typeClass, false);
    }

    @Override
    public <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass, boolean includeInactive) {
        return powers.values()
            .stream()
            .filter(typeClass::isInstance)
            .map(typeClass::cast)
            .filter(type -> includeInactive || type.isActive())
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public List<Identifier> getSources(Power power) {

        if (powerSources.containsKey(power)) {
            return List.copyOf(powerSources.get(power));
        }

        else {
            return List.of();
        }

    }

    @Override
    public boolean removePower(Power power, Identifier source) {

        ConcurrentHashMap.KeySetView<Power, Boolean> powersToRemove = ConcurrentHashMap.newKeySet();
        boolean result = this.removePower(power, source, powersToRemove::add);

        powers.keySet().removeAll(powersToRemove);
        powerSources.keySet().removeAll(powersToRemove);

        return result;

    }

    protected boolean removePower(Power power, Identifier source, Consumer<Power> adder) {

        Set<Identifier> sources = powerSources.getOrDefault(power, new ObjectOpenHashSet<>());
        if (!sources.remove(source)) {
            return false;
        }

        if (sources.isEmpty() && powers.containsKey(power)) {

            PowerType powerType = powers.get(power);
            adder.accept(power);

            powerType.onRemoved();
            powerType.onLost();

        }

        if (power instanceof MultiplePower multiplePower) {
            multiplePower.getSubPowers().forEach(subPower -> this.removePower(subPower, source, adder));
        }

        return true;

    }

    @Override
    public int removeAllPowersFromSource(Identifier source) {
        //noinspection MappingBeforeCount
        return (int) this.getPowersFromSource(source)
            .stream()
            .filter(Predicate.not(Power::isSubPower))
            .peek(pt -> this.removePower(pt, source))
            .count();
    }

    @Override
    public List<Power> getPowersFromSource(Identifier source) {
        return powerSources.entrySet()
            .stream()
            .filter(e -> e.getValue().contains(source))
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public boolean addPower(Power power, Identifier source) {

        ConcurrentHashMap<Power, PowerType> powersToAdd = new ConcurrentHashMap<>();
        boolean result = this.addPower(power, source, powersToAdd::put);

        powersToAdd.forEach((powerToAdd, powerTypeToAdd) -> {

            powerTypeToAdd.onAdded();
            powerTypeToAdd.onGained();

            if (owner instanceof ServerPlayer serverPlayer) {
                GainedPowerCriterion.INSTANCE.trigger(serverPlayer, powerToAdd);
            }

        });

        return result;

    }

    protected boolean addPower(Power power, Identifier source, BiConsumer<Power, PowerType> adder) {

        Set<Identifier> sources = powerSources.computeIfAbsent(power, pt -> new ObjectOpenHashSet<>());
        if (!sources.add(source)) {
            return false;
        }

        PowerType powerType = shallowCopy(power.getType());

        powerType.setPower(power);
        powerType.setHolder(owner);

        powerType.onInit();
        adder.accept(power, powerType);

        powers.put(power, powerType);
        powerSources.put(power, sources);

        if (power instanceof MultiplePower multiplePower) {
            multiplePower.getSubPowers().forEach(subPower -> this.addPower(subPower, source, adder));
        }

        return true;

    }

    @Override
    public void serverTick() {
        powers.values()
            .stream()
            .filter(PowerType::shouldTick)
            .filter(powerType -> powerType.shouldTickWhenInactive() || powerType.isActive())
            .peek(PowerType::commonTick)
            .forEach(PowerType::serverTick);
    }

    @Override
    public void clientTick() {
        powers.values()
            .stream()
            .filter(PowerType::shouldTick)
            .filter(powerType -> powerType.shouldTickWhenInactive() || powerType.isActive())
            .peek(PowerType::commonTick)
            .forEach(PowerType::clientTick);
    }

    @Override
    public void tick() {

    }

    @Override
    public void readData(@NotNull ValueInput input) {

        powers.clear();
        powerSources.clear();

        HolderLookup.Provider lookup = input.lookup();
        var ops = RegistryOps.create(NbtOps.INSTANCE, lookup);

        var powersInput = input.listOrEmpty("powers", Power.DataEntry.CODEC.codec());
        int i = 0;
        for (Power.DataEntry powerDataEntry : powersInput) {

            try {

                PowerReference powerReference = powerDataEntry.powerReference();

                try {

                    Power power = powerReference.getPower();
                    PowerType powerType = shallowCopy(power.getType());

                    powerType.setPower(power);
                    powerType.setHolder(owner);

                    powerType.onInit();

                    try {
                        powerType.fromTag(powerDataEntry.nbtData());
                    }

                    catch (ClassCastException cce) {
                        Apoli.LOGGER.warn("Data type of power \"{}\" has changed, skipping data for that power on entity {} (UUID: {})", powerReference.id(), owner.getName().getString(), owner.getStringUUID());
                    }

                    powers.put(power, powerType);
                    powerSources.put(power, powerDataEntry.sources());

                }

                catch (Throwable t) {
                    Apoli.LOGGER.warn("Unregistered power \"{}\" found on entity {} (UUID: {}), skipping...", powerReference.id(), owner.getName().getString(), owner.getStringUUID());
                }

            }

            catch (Throwable t) {
                Apoli.LOGGER.warn("Error trying to decode power at index {} from NBT of entity {} (UUID: {}) (skipping): {}", i, owner.getName().getString(), owner.getStringUUID(), t.getMessage());
            }

            i++;
        }

    }

    @Override
    public void writeData(@NotNull ValueOutput output) {

        HolderLookup.Provider lookup = owner.level().registryAccess();
        var ops = RegistryOps.create(NbtOps.INSTANCE, lookup);

        List<Power.DataEntry> powersEntries = new ArrayList<>();
        powers.forEach((power, powerType) -> {

            PowerConfiguration<?> typeConfig = power.getType().getConfig();
            PowerReference powerReference = PowerReference.of(power.getId());

            powersEntries.add(new Power.DataEntry(typeConfig, powerReference, powerType.toTag(), powerSources.get(power)));

        });

        // Store the list using codec-based approach
        output.store("powers", Power.DataEntry.CODEC.codec().listOf(), powersEntries);

    }

    @Override
    public void writeSyncPacket(RegistryFriendlyByteBuf buf, ServerPlayer recipient) {
        buf.writeVarInt(0);
        PowerHolderComponent.super.writeSyncPacket(buf, recipient);
    }

    @Override
    public void applySyncPacket(RegistryFriendlyByteBuf buf) {

        int syncType = buf.readVarInt();
        switch (syncType) {
            case 0 ->
                PowerHolderComponent.super.applySyncPacket(buf);
            case 1 ->
                PacketHandlers.GRANT_POWERS.apply(buf, this);
            case 2 ->
                PacketHandlers.REVOKE_POWERS.apply(buf, this);
            case 3 ->
                PacketHandlers.REVOKE_ALL_POWERS.apply(buf, this);
            default ->
                Apoli.LOGGER.warn("Received unknown sync type with ID {} (expected value range: [0 to 3]) when applying sync packet to entity {}! Skipping...", syncType, owner.getName().getString());
        }

    }

    @Override
    public void sync() {
        KEY.sync(this.owner);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("PowerHolderComponent[\n");
        for (Map.Entry<Power, PowerType> powerEntry : powers.entrySet()) {
            str.append("\t").append(powerEntry.getKey().getId()).append(": ").append(powerEntry.getValue().toTag().toString()).append("\n");
        }
        str.append("]");
        return str.toString();
    }

    private static PowerType shallowCopy(PowerType powerType) {

		//noinspection unchecked
		PowerConfiguration<PowerType> config = (PowerConfiguration<PowerType>) powerType.getConfig();
        TypedDataObjectFactory<PowerType> dataFactory = config.dataFactory();

        SerializableData.Instance data = dataFactory.toData(powerType);
        return dataFactory.fromData(data);

    }

}
