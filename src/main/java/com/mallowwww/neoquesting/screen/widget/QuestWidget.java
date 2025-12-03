package com.mallowwww.neoquesting.screen.widget;

import com.mallowwww.neoquesting.ModRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class QuestWidget extends AbstractWidget {
    private final ModRegistries.Quest QUEST;
    public final int offsetX, offsetY;
    public QuestWidget(int x, int y, int width, int height, int offsetX, int offsetY, Component message, ModRegistries.Quest _quest) {
        super(x, y, width, height, message);
        QUEST = _quest;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var maxX = getX() + width;
        var maxY = getY() + height;
        var outline = 3;
        if (mouseX < maxX && mouseX > getX() && mouseY < maxY && mouseY > getY())
            guiGraphics.fill(getX()-outline, getY()-outline, maxX+outline, maxY+outline, 0xFFFFFFFF);
        guiGraphics.blit(ResourceLocation.parse("minecraft:textures/block/stone.png"), getX(), getY(), 0, 0, 0, width, height, 32, 32);


    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
