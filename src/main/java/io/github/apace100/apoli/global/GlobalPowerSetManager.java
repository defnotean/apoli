package io.github.apace100.apoli.global;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PrioritizedEntry;
import io.github.apace100.calio.data.IdentifiableMultiJsonDataLoader;
import io.github.apace100.calio.data.MultiJsonDataContainer;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.TagLike;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.registries.Registries;

public class GlobalPowerSetManager extends IdentifiableMultiJsonDataLoader implements IdentifiableResourceReloadListener {

    public static final Set<Identifier> DEPENDENCIES = Util.make(new HashSet<>(), set -> set.add(Apoli.identifier("powers")));
    public static final Identifier ID = Apoli.identifier("global_powers");

    private static final Object2ObjectOpenHashMap<Identifier, GlobalPowerSet> SETS_BY_ID = new Object2ObjectOpenHashMap<>();
    private static final ObjectOpenHashSet<Identifier> DISABLED_SETS = new ObjectOpenHashSet<>();

    private static final Map<Identifier, Integer> LOADING_PRIORITIES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

    private final HolderLookup.Provider wrapperLookup;

    GlobalPowerSetManager(HolderLookup.Provider wrapperLookup) {
        super(GSON, "global_powers", PackType.SERVER_DATA);
        this.wrapperLookup = wrapperLookup;
    }

    public static void init() {

        ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(PowerManager.ID, ID);
        ServerEntityEvents.ENTITY_LOAD.register(ID, (entity, world) -> GlobalPowerSetUtil.applyGlobalPowers(entity));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(PowerManager.ID, ID);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(ID, (player, joined) -> {
            if (!joined) {
                GlobalPowerSetUtil.applyGlobalPowers(player);
            }
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ID, GlobalPowerSetManager::new);

    }

    @Override
    protected void apply(MultiJsonDataContainer prepared, ResourceManager manager, ProfilerFiller profiler) {

        Apoli.LOGGER.info("Reading global power sets from data packs...");
        startBuilding();

        Map<Identifier, List<PrioritizedEntry<GlobalPowerSet>>> loadedGlobalPowerSets = new Object2ObjectLinkedOpenHashMap<>();
        prepared.forEach((packName, id, jsonElement) -> {

            try {

                SerializableData.CURRENT_NAMESPACE.set(id.getNamespace());
                SerializableData.CURRENT_PATH.set(id.getPath());

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Not a JSON object: " + jsonElement);
                }

                GlobalPowerSet globalPowerSet = GlobalPowerSet.DATA_TYPE.read(jsonObject);
                int currLoadingPriority = GsonHelper.getAsInt(jsonObject, "loading_priority", 0);

                PrioritizedEntry<GlobalPowerSet> entry = new PrioritizedEntry<>(globalPowerSet, currLoadingPriority);
                int prevLoadingPriority = LOADING_PRIORITIES.getOrDefault(id, Integer.MIN_VALUE);

                if (globalPowerSet.shouldReplace() && currLoadingPriority <= prevLoadingPriority) {
                    Apoli.LOGGER.warn("Ignoring global power set \"{}\" with 'replace' set to true from data pack [{}]. Its loading priority ({}) must be higher than {} to replace the global power set!", id, packName, currLoadingPriority, prevLoadingPriority);
                }

                else {

                    if (globalPowerSet.shouldReplace()) {
                        Apoli.LOGGER.info("Global power set \"{}\" has been replaced by data pack \"{}\"!", id, packName);
                    }

                    List<String> invalidPowers = globalPowerSet.validate()
                        .stream()
                        .map(Power::getId)
                        .map(Identifier::toString)
                        .toList();

                    if (!invalidPowers.isEmpty()) {
                        Apoli.LOGGER.error("Global power set \"{}\" contained {} invalid power(s): {}", id, invalidPowers.size(), String.join(", ", invalidPowers));
                    }

                    loadedGlobalPowerSets.computeIfAbsent(id, k -> new LinkedList<>()).add(entry);
                    DISABLED_SETS.remove(id);

                    LOADING_PRIORITIES.put(id, currLoadingPriority);

                }

            }

            catch (Exception e) {
                Apoli.LOGGER.error("There was a problem reading global power set \"{}\" (skipping): {}", id, e.getMessage());
            }

        });

        SerializableData.CURRENT_NAMESPACE.set(null);
        SerializableData.CURRENT_PATH.set(null);

        Apoli.LOGGER.info("Finished reading global power sets from data packs. Merging similar global power sets...");

        loadedGlobalPowerSets.forEach((id, entries) -> {

            AtomicReference<GlobalPowerSet> currentSet = new AtomicReference<>();
            entries.sort(Comparator.comparing(PrioritizedEntry::priority));

            for (PrioritizedEntry<GlobalPowerSet> entry : entries) {

                if (currentSet.get() == null) {
                    currentSet.set(entry.value());
                }

                else {
                    currentSet.accumulateAndGet(entry.value(), this::merge);
                }

            }

            SETS_BY_ID.put(id, currentSet.get());

        });

        endBuilding();
        Apoli.LOGGER.info("Finished merging similar global power sets. Registry contains {} global power sets.", size());

    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return DEPENDENCIES;
    }

    private GlobalPowerSet merge(GlobalPowerSet oldSet, GlobalPowerSet newSet) {

        TagLike<EntityType<?>> mergedEntityTypes = new TagLike<>(BuiltInRegistries.ENTITY_TYPE);
        oldSet.getEntityTypes().ifPresent(mergedEntityTypes::addAll);
        newSet.getEntityTypes().ifPresent(mergedEntityTypes::addAll);

        Set<PowerReference> powerReferences = new ObjectLinkedOpenHashSet<>(oldSet.getPowerReferences());
        int order = oldSet.getOrder();

        if (newSet.shouldReplace()) {

            mergedEntityTypes.clear();
            newSet.getEntityTypes().ifPresent(mergedEntityTypes::addAll);
            powerReferences.clear();

            order = newSet.getOrder();

        }

        powerReferences.addAll(newSet.getPowerReferences());

        Optional<TagLike<EntityType<?>>> entityTypes = oldSet.getEntityTypes().isPresent() || newSet.getEntityTypes().isPresent()
            ? Optional.of(mergedEntityTypes)
            : Optional.empty();

        return new GlobalPowerSet(
            entityTypes,
            powerReferences,
            newSet.shouldReplace(),
            order
        );

    }

    public static DataResult<GlobalPowerSet> getResult(Identifier id) {
        return contains(id)
            ? DataResult.success(SETS_BY_ID.get(id))
            : DataResult.error(() -> "Couldn't get global power set from ID \"" + id + "\", as it wasn't registered!");
    }

    public static Optional<GlobalPowerSet> getOptional(Identifier id) {
        return getResult(id).result();
    }

    @Nullable
    public static GlobalPowerSet getNullable(Identifier id) {
        return SETS_BY_ID.get(id);
    }

    public static GlobalPowerSet get(Identifier id) {
        return getResult(id).getOrThrow();
    }

    public static Set<Map.Entry<Identifier, GlobalPowerSet>> entrySet() {
        return new ObjectOpenHashSet<>(SETS_BY_ID.entrySet());
    }

    public static Set<Identifier> keySet() {
        return new ObjectOpenHashSet<>(SETS_BY_ID.keySet());
    }

    public static Collection<GlobalPowerSet> values() {
        return new ObjectOpenHashSet<>(SETS_BY_ID.values());
    }

    public static boolean isDisabled(Identifier id) {
        return DISABLED_SETS.contains(id);
    }

    public static boolean contains(Identifier id) {
        return SETS_BY_ID.containsKey(id);
    }

    public static int size() {
        return SETS_BY_ID.size();
    }

    private static GlobalPowerSet remove(Identifier id) {
        return SETS_BY_ID.remove(id);
    }

    public static void disable(Identifier id) {
        remove(id);
        DISABLED_SETS.add(id);
    }

    private static void startBuilding() {

        LOADING_PRIORITIES.clear();

        SETS_BY_ID.clear();
        DISABLED_SETS.clear();

    }

    private static void endBuilding() {

        LOADING_PRIORITIES.clear();

        SETS_BY_ID.trim();
        DISABLED_SETS.trim();

    }

}
