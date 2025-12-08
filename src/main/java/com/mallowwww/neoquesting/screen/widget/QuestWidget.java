package com.mallowwww.neoquesting.screen.widget;

import com.mallowwww.neoquesting.ModAttachments;
import com.mallowwww.neoquesting.ModRegistries;
import com.mallowwww.neoquesting.network.QuestNetworking;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.io.output.QueueOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestWidget extends AbstractWidget {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.parse("minecraft:textures/block/redstone_block.png"),
            ResourceLocation.parse("minecraft:textures/block/stone.png"),
            ResourceLocation.parse("minecraft:textures/block/end_stone.png")
    };
    private final ModRegistries.Quest QUEST;
    private ModAttachments.QuestState STATE;
    private final Player PLAYER;
    public final int offsetX, offsetY;
    private float ticks = 0;
    public QuestWidget(int x, int y, int width, int height, int offsetX, int offsetY, Component message, ModRegistries.Quest _quest, ModAttachments.QuestState _state, Player _player) {
        super(x, y, width, height, message);
        this.QUEST = _quest;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.STATE = _state;
        PLAYER = _player;
    }
    private boolean inBounds(int mouseX, int mouseY) {
        var maxX = getX() + width;
        var maxY = getY() + height;
        return mouseX < maxX && mouseX > getX() && mouseY < maxY && mouseY > getY();
    }
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var x = getX();
        var y = getY();
        var maxX = getX() + width;
        var maxY = getY() + height;
        var outline = 3;
        ticks += partialTick;
        if (inBounds(mouseX, mouseY))
            guiGraphics.fill(getX()-outline, getY()-outline, maxX+outline, maxY+outline, 0xFFFFFFFF);
        guiGraphics.blit(TEXTURES[STATE.ordinal()], getX(), getY(), 0, 0, 0, width, height, 32, 32);
//        guiGraphics.renderFakeItem(QUEST.requirements().getFirst(), getX(), getY());
        var size = QUEST.requirements().size();
        var stack = QUEST.requirements().get(((int)(ticks/20))%size);
        var minecraft = Minecraft.getInstance();
        var scale = 2;
        if (!stack.isEmpty()) {
            BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, null, null, 0);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float)(x + 8 * scale), (float)(y + 8 * scale), (float)(150 + (bakedmodel.isGui3d() ? 0 : 0)));

            try {
                guiGraphics.pose().scale(16f * scale, -16f * scale, 16f * scale);
                boolean flag = !bakedmodel.usesBlockLight();
                if (flag) {
                    Lighting.setupForFlatItems();
                }

                minecraft
                        .getItemRenderer()
                        .render(stack, ItemDisplayContext.GUI, false, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);

                guiGraphics.flush();
                if (flag) {
                    Lighting.setupFor3DItems();
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering item");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being rendered");
                crashreportcategory.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                crashreportcategory.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                crashreportcategory.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                throw new ReportedException(crashreport);
            }

            guiGraphics.pose().popPose();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(2, 2, 2);
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.drawString(Minecraft.getInstance().font, String.valueOf(stack.getCount()), (int)((float)x/2+20/2), (int)((float)y/2+20/2), 0xFFFFFFFF);
            guiGraphics.pose().translate(0, 0, 100);
            guiGraphics.pose().popPose();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 300);
            var questsRegistry = minecraft.player.registryAccess().registry(ModRegistries.MOD_QUESTS_RESOURCE_KEY).get();
            Map<ResourceLocation, ModAttachments.QuestState> data = PLAYER.getData(ModAttachments.QUEST_ATTACHMENT_TYPE).map();
            guiGraphics.pose().translate(0, 0, -200);
            QUEST.dependencies().forEach(dep -> {
                var quests = questsRegistry.stream().filter(modQuests -> modQuests.quests().stream().anyMatch(q -> q.id().equals(dep))).findFirst().get();
                var depQuest = quests.quests().stream().filter(q -> q.id().equals(dep)).findFirst().get();
                var depX = depQuest.x() + x - offsetX;
                var depY = depQuest.y() + y - offsetY;
                var first = Math.min(x, depX)+14;
                var last = Math.max(x, depX)+14;
                var firstY = Math.min(y, depY)+14;
                var lastY = Math.max(y, depY)+14;
                var dist = last - first;
                var distY = lastY - firstY;
                var color = (!data.containsKey(dep) || data.get(dep) == ModAttachments.QuestState.INCOMPLETE) ? 0xFFFF0000 : 0xFF00FF00;
                var offset = 4;
                if (dist < distY) {
                    guiGraphics.fill(first, firstY, first + offset, firstY + offset + distY / 2, color);
                    guiGraphics.fill(first, firstY + distY / 2, last + offset, lastY + offset - distY / 2, color);
                    guiGraphics.fill(last, lastY, last + offset, lastY + offset - distY / 2, color);
                } else {
                    guiGraphics.fill(first, firstY, first + dist / 2 + offset, firstY + offset, color);
                    guiGraphics.fill(first + dist / 2, firstY, last - dist / 2 + offset, lastY + offset, color);
                    guiGraphics.fill(last, lastY, last - dist / 2 + offset, lastY + offset, color);
                }
            });
            guiGraphics.pose().translate(0, 0, 200);
            if (inBounds(mouseX, mouseY)) {
//                guiGraphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
                var id = QUEST.id();
                List<Component> stackComponents = stack.getTooltipLines(Item.TooltipContext.of(minecraft.level), null, TooltipFlag.NORMAL);
                Component questTitle = Component.translatable("quest."+id.getNamespace()+"."+id.getPath())
                        .withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.AQUA);
                List<Component> finalComponents = new ArrayList<>();
                finalComponents.add(questTitle);
                QUEST.description().ifPresent(s -> finalComponents.add(Component.literal(s).withStyle(ChatFormatting.ITALIC)));
                if (!QUEST.dependencies().isEmpty() || !QUEST.requirements().isEmpty()) {
                    finalComponents.add(Component.empty());
                    finalComponents.add(Component.translatable("quest_info.neoquesting.dependencies"));
                }
                for (var d : QUEST.dependencies()) {
                    Component depTitle = Component.translatable("quest."+d.getNamespace()+"."+d.getPath())
                            .withStyle(
                                    (!data.containsKey(d) || data.get(d) == ModAttachments.QuestState.INCOMPLETE) ? ChatFormatting.RED : ChatFormatting.GREEN
                            );
                    finalComponents.add(
                            Component.literal("Quest: ").append(depTitle)
                    );
                }
                for (var d : QUEST.requirements()) {
                    Component depTitle = d.getDisplayName().copy();
                    finalComponents.add(
                            Component.literal("Item: "+d.getCount()+"x ").append(depTitle)
                    );
                }
                guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, finalComponents, mouseX, mouseY);
            }
            guiGraphics.pose().popPose();

        }

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (!inBounds((int)mouseX, (int)mouseY))
            return false;
        if (STATE == ModAttachments.QuestState.INCOMPLETE)
            return false;
        STATE = ModAttachments.QuestState.COLLECTED;
        var data = new HashMap<>(PLAYER.getData(ModAttachments.QUEST_ATTACHMENT_TYPE).map());
        data.put(QUEST.id(), STATE);
        PLAYER.setData(ModAttachments.QUEST_ATTACHMENT_TYPE, new ModAttachments.QuestAttachment(data));
        PacketDistributor.sendToServer(new QuestNetworking.QuestClickedPayload(QUEST.id()));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
