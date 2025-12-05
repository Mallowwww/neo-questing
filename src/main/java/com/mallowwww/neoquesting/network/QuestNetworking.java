package com.mallowwww.neoquesting.network;

import com.mallowwww.neoquesting.ModAttachments;
import com.mallowwww.neoquesting.ModRegistries;
import com.mallowwww.neoquesting.NeoQuesting;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

@EventBusSubscriber(modid = NeoQuesting.MODID)
public class QuestNetworking {
    public record QuestClickedPayload(ResourceLocation questID) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<QuestClickedPayload> TYPE = new Type<>(NeoQuesting.path("quest_clicked"));

        public static final StreamCodec<ByteBuf, QuestClickedPayload> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                QuestClickedPayload::questID,
                QuestClickedPayload::new
        );

        public static void handleServer(final QuestClickedPayload data, IPayloadContext ctx) {
            var playerData = new HashMap<>(ctx.player().getData(ModAttachments.QUEST_ATTACHMENT_TYPE).map());
            var state = playerData.getOrDefault(data.questID, ModAttachments.QuestState.INCOMPLETE);
            if (state == ModAttachments.QuestState.COMPLETE) {
                playerData.put(data.questID, ModAttachments.QuestState.COLLECTED);
                ctx.player().setData(ModAttachments.QUEST_ATTACHMENT_TYPE, new ModAttachments.QuestAttachment(playerData));

                ctx.player().registryAccess().registry(ModRegistries.MOD_QUESTS_RESOURCE_KEY).get().forEach(modQuests -> {
                    modQuests.quests().forEach(quest -> {
                        if (quest.id().equals(data.questID)) {
                            quest.reward().forEach(reward -> {
                                ctx.player().getInventory().add(reward);
                            });
                        }
                    });
                });
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent e) {
        final var registrar = e.registrar("quest_clicked");
        registrar.playToServer(QuestClickedPayload.TYPE, QuestClickedPayload.STREAM_CODEC,
                QuestClickedPayload::handleServer
        );
    }
}
