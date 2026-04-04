package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.networking.packet.s2c.SyncEntityTypeTagCacheS2CPacket;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.mixin.TagEntryAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagKey;
import net.minecraft.server.packs.resources.DependencyTracker;
import net.minecraft.server.packs.resources.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;

//  TODO: Rename this to 'modify_entity_type_tag' -eggohito
public class ModifyTypeTagPowerType extends PowerType {

    private static final Map<Identifier, Collection<Identifier>> ENTITY_TYPE_SUB_TAGS = new ConcurrentHashMap<>();
    private static final String ENTITY_TYPE_TAG_PATH = Registries.getTagPath(Registries.ENTITY_TYPE);

    public static final TypedDataObjectFactory<ModifyTypeTagPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("tag", SerializableDataTypes.ENTITY_TAG),
        (data, condition) -> new ModifyTypeTagPowerType(
            data.get("tag"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("tag", powerType.tag)
    );

    protected final TagKey<EntityType<?>> tag;

    public ModifyTypeTagPowerType(TagKey<EntityType<?>> tag, Optional<EntityCondition> condition) {
        super(condition);
        this.tag = tag;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_TYPE_TAG;
    }

    public boolean doesApply(TagKey<EntityType<?>> typeTag) {
        return Objects.equals(typeTag, tag) || ENTITY_TYPE_SUB_TAGS.getOrDefault(typeTag.id(), new ObjectArrayList<>())
            .stream()
            .map(id -> TagKey.of(Registries.ENTITY_TYPE, id))
            .anyMatch(this::doesApply);
    }

    public static boolean doesApply(Entity entity, TagKey<EntityType<?>> typeTag) {
        return PowerHolderComponent.hasPowerType(entity, ModifyTypeTagPowerType.class, type -> type.doesApply(typeTag));
    }

    public static boolean doesApply(Entity entity, HolderSet<EntityType<?>> entryList) {
        return entryList.getTagKey()
            .map(tagKey -> doesApply(entity, tagKey))
            .orElse(false);
    }

    @ApiStatus.Internal
    public static <T> void setTagCache(String directory, TagEntry.ValueGetter<T> valueGetter, DependencyTracker<Identifier, TagLoader.TagDependencies> dependencyTracker) {

        if (ENTITY_TYPE_TAG_PATH.equals(directory)) {
            dependencyTracker.traverse((id, dependencies) -> dependencies.entries()
                .stream()
                .map(TagLoader.TrackedEntry::entry)
                .filter(entry -> entry.resolve(valueGetter, value -> {}))
                .map(TagEntryAccessor.class::cast)
                .filter(TagEntryAccessor::isTag)
                .forEach(entry -> ENTITY_TYPE_SUB_TAGS
                    .computeIfAbsent(id, k -> new ObjectArraySet<>())
                    .add(entry.getId())));
        }

    }

    @ApiStatus.Internal
    public static void resetTagCache(MinecraftServer server, LifecycledResourceManager resourceManager) {
        ENTITY_TYPE_SUB_TAGS.clear();
    }

    @Environment(EnvType.CLIENT)
    @ApiStatus.Internal
    public static void receiveTagCache(SyncEntityTypeTagCacheS2CPacket payload, ClientPlayNetworking.Context context) {
        ENTITY_TYPE_SUB_TAGS.clear();
        ENTITY_TYPE_SUB_TAGS.putAll(payload.subTags());
    }

    @ApiStatus.Internal
    public static void sendTagCache(ServerPlayer player, boolean joined) {
        ServerPlayNetworking.send(player, new SyncEntityTypeTagCacheS2CPacket(ENTITY_TYPE_SUB_TAGS));
    }

}
