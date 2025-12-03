package com.mallowwww.neoquesting.item;

import com.mallowwww.neoquesting.ModRegistries;
import com.mallowwww.neoquesting.NeoQuesting;
import com.mallowwww.neoquesting.screen.QuestScreen;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class QuestBook extends Item {
    public QuestBook(ResourceLocation quests) {
        super(new Properties()
                .stacksTo(1)
                .component(QuestDataComponent.SUPPLIER, new QuestDataComponent(quests))
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        InteractionResultHolder<ItemStack> interaction = InteractionResultHolder.pass(player.getItemInHand(usedHand));
        var stack = player.getItemInHand(usedHand);
        var component = stack.get(QuestDataComponent.SUPPLIER);
        var instance = Minecraft.getInstance();
        if (component == null)
            return super.use(level, player, usedHand);
        var questID = component.id;
        var questsRegistry = level.registryAccess().registry(ModRegistries.MOD_QUESTS_RESOURCE_KEY).orElse(null);
        if (questsRegistry == null)
            return super.use(level, player, usedHand);
        var modQuests = questsRegistry.get(questID);
        if (modQuests == null)
            return super.use(level, player, usedHand);
        if (level.isClientSide())
            instance.setScreen(new QuestScreen(modQuests));
//        player.sendSystemMessage(Component.literal("AAAA " + modQuests.quests().getFirst().id().toString()));

        return super.use(level, player, usedHand);
    }

    public record QuestDataComponent(ResourceLocation id) {
        public static final Codec<QuestDataComponent> CODEC = RecordCodecBuilder.create(i ->
                i.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(QuestDataComponent::id)
                ).apply(i, QuestDataComponent::new)
        );
        public static final StreamCodec<ByteBuf, QuestDataComponent> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, QuestDataComponent::id,
                QuestDataComponent::new
        );
        public static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, NeoQuesting.MODID);
        public static final Supplier<DataComponentType<QuestDataComponent>> SUPPLIER = REGISTRAR.registerComponentType(
                "questbook",
                builder -> builder
                        .persistent(CODEC)
                        .networkSynchronized(STREAM_CODEC)
        );
        public static void register(IEventBus eventBus) {
            REGISTRAR.register(eventBus);
        }
    }
}
