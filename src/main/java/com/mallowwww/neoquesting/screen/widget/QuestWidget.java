package com.mallowwww.neoquesting.screen.widget;

import com.mallowwww.neoquesting.ModAttachments;
import com.mallowwww.neoquesting.ModRegistries;
import com.mallowwww.neoquesting.network.QuestNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

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
        var maxX = getX() + width;
        var maxY = getY() + height;
        var outline = 3;
        if (inBounds(mouseX, mouseY))
            guiGraphics.fill(getX()-outline, getY()-outline, maxX+outline, maxY+outline, 0xFFFFFFFF);
        guiGraphics.blit(TEXTURES[STATE.ordinal()], getX(), getY(), 0, 0, 0, width, height, 32, 32);


    }

    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        if (!inBounds((int)mouseX, (int)mouseY))
            return false;
        STATE = ModAttachments.QuestState.COMPLETE;
        var data = PLAYER.getData(ModAttachments.QUEST_ATTACHMENT_TYPE).map();
        data.put(QUEST.id(), STATE);
        PLAYER.setData(ModAttachments.QUEST_ATTACHMENT_TYPE, new ModAttachments.QuestAttachment(data));
        PacketDistributor.sendToServer(new QuestNetworking.QuestClickedPayload(QUEST.id()));
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
