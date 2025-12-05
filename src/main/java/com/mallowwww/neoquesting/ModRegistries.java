package com.mallowwww.neoquesting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.joml.Vector2f;

import java.util.HashMap;
import java.util.List;
@EventBusSubscriber(modid = NeoQuesting.MODID)
public class ModRegistries {
    public record Quest(ResourceLocation id, List<ResourceLocation> dependencies, List<ItemStack> requirements, List<ItemStack> reward, int x, int y) {
        public Quest(ResourceLocation id, List<ResourceLocation> dependencies, List<ItemStack> requirements, List<ItemStack> reward, List<Integer> pos) {
            this(id, dependencies, requirements, reward, pos.getFirst(), pos.getLast());
        }
        public List<Integer> pos() {
            return List.of(x, y);
        }
        public static final Codec<Quest> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<Quest> i) -> i.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Quest::id),
                ResourceLocation.CODEC.listOf().fieldOf("dependencies").forGetter(Quest::dependencies),
                ItemStack.CODEC.listOf().fieldOf("requirements").forGetter(Quest::requirements),
                ItemStack.CODEC.listOf().fieldOf("reward").forGetter(Quest::reward),
                Codec.INT.sizeLimitedListOf(2).fieldOf("position").forGetter(Quest::pos)
        ).apply(i, Quest::new));
    }
    public record ModQuests(List<Quest> quests) {
        public static final Codec<ModQuests> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<ModQuests> i) -> i.group(
                Quest.CODEC.listOf().fieldOf("quests").forGetter(ModQuests::quests)
        ).apply(i, ModQuests::new));
    }
    public static final ResourceKey<Registry<ModQuests>> MOD_QUESTS_RESOURCE_KEY = ResourceKey.createRegistryKey(NeoQuesting.path("quest"));

    @SubscribeEvent
    public static void newRegistryEvent(DataPackRegistryEvent.NewRegistry e) {
        e.dataPackRegistry(
                MOD_QUESTS_RESOURCE_KEY,
                ModQuests.CODEC,
                ModQuests.CODEC,
                builder -> builder.maxId(256)
        );
    }
    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post e) {
        var player = e.getEntity();
        if (player.level().isClientSide)
            return;
        var data = player.getData(ModAttachments.QUEST_ATTACHMENT_TYPE);
        for (var x : player.registryAccess().registry(MOD_QUESTS_RESOURCE_KEY).get()) {
            for (var quest : x.quests) {
                var hasReqs = quest.requirements.stream().allMatch(req -> player.getInventory().countItem(req.getItem())>=req.getCount());
                var hasDeps = quest.dependencies.stream().allMatch(dep -> data.map().getOrDefault(dep, ModAttachments.QuestState.INCOMPLETE).ordinal() > 0);
                var hasQuestUnlocked = data.map().getOrDefault(quest.id, ModAttachments.QuestState.INCOMPLETE) != ModAttachments.QuestState.INCOMPLETE;
                if (hasDeps && hasReqs && !hasQuestUnlocked) {
                    var newMap = new HashMap<>(data.map());
                    newMap.put(quest.id, ModAttachments.QuestState.COMPLETE);
                    player.setData(ModAttachments.QUEST_ATTACHMENT_TYPE, new ModAttachments.QuestAttachment(newMap));
                    player.displayClientMessage(Component.literal(quest.id.toString()), false);
                    return;
                }
            }
        }
    }
}
