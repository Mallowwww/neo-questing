package com.mallowwww.neoquesting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
@EventBusSubscriber(modid = NeoQuesting.MODID)
public class ModRegistries {
    public record Quest(ResourceLocation id, List<ResourceLocation> dependencies, List<ResourceLocation> requirements, List<ResourceLocation> reward) {
        public static final Codec<Quest> CODEC = RecordCodecBuilder.create((RecordCodecBuilder.Instance<Quest> i) -> i.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(Quest::id),
                ResourceLocation.CODEC.listOf().fieldOf("dependencies").forGetter(Quest::dependencies),
                ResourceLocation.CODEC.listOf().fieldOf("requirements").forGetter(Quest::requirements),
                ResourceLocation.CODEC.listOf().fieldOf("reward").forGetter(Quest::reward)
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
    public static void newRegistries(NewRegistryEvent e) {

    }
}
