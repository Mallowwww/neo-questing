package com.mallowwww.neoquesting;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModAttachments {
    public enum QuestState {
        INCOMPLETE,
        COMPLETE,
        COLLECTED;
        public static final Codec<QuestState> CODEC = Codec.stringResolver(Enum::name, QuestState::valueOf);
    }
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NeoQuesting.MODID);
    public static final Supplier<AttachmentType<QuestAttachment>> QUEST_ATTACHMENT_TYPE = ATTACHMENTS.register("quest_attachment", () -> AttachmentType.<QuestAttachment>builder(
            () -> new QuestAttachment(new HashMap<ResourceLocation, QuestState>())
    ).serialize(QuestAttachment.CODEC).sync(QuestAttachment.STREAM_CODEC).build());
    public record QuestAttachment(Map<ResourceLocation, QuestState> map) {
        public void encode(ByteBuf buf) {
            map.forEach((key, value) -> {
                buf.writeByte(key.toString().length());
                buf.writeCharSequence(key.toString(), StandardCharsets.UTF_8);
                buf.writeByte(value.ordinal());
            });
        }
        public static QuestAttachment decode(ByteBuf buf) {
            Map<ResourceLocation, QuestState> map = new HashMap<>();
            for (int i = 0; i < buf.readableBytes();) {
                byte size = buf.readByte();
                String idString = buf.readCharSequence(size, StandardCharsets.UTF_8).toString();
                QuestState state = QuestState.values()[buf.readByte()];
                i += 1 + size + 1;
                map.put(ResourceLocation.parse(idString), state);
            }
            return new QuestAttachment(map);
        }

        private static final Codec<QuestAttachment> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.simpleMap(ResourceLocation.CODEC, QuestState.CODEC, Keyable.forStrings(() -> Arrays.stream(QuestState.values()).map(Enum::name))).fieldOf("map").forGetter(QuestAttachment::map)
        ).apply(i, QuestAttachment::new));
        private static final StreamCodec<ByteBuf, QuestAttachment> STREAM_CODEC = StreamCodec.ofMember(
            QuestAttachment::encode, QuestAttachment::decode
        );
    }
    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
