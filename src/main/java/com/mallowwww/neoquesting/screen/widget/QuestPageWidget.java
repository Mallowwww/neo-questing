package com.mallowwww.neoquesting.screen.widget;

import com.mallowwww.neoquesting.ModAttachments;
import com.mallowwww.neoquesting.ModRegistries;
import com.mallowwww.neoquesting.network.QuestNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestPageWidget extends AbstractContainerWidget {
    private final List<QuestWidget> CHILDREN = new ArrayList<>();
    private final ModRegistries.ModQuests QUESTS;
    public QuestPageWidget(int x, int y, int width, int height, Component message, ModRegistries.ModQuests _quests, Player player, ModAttachments.QuestAttachment playerData) {
        super(x, y, width, height, message);
        QUESTS = _quests;
        for (var quest : _quests.quests()) {
            ModAttachments.QuestState state = playerData.map().getOrDefault(quest.id(), ModAttachments.QuestState.INCOMPLETE);
            CHILDREN.add(new QuestWidget(x, y, 32, 32, quest.x() * 32, quest.y() * 32, Component.literal("a"), quest, state, player));
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(ResourceLocation.parse("neoquesting:textures/gui/quest_book_background.png"), 0, 0, 0, -getX(), -getY(), width, height, 32, 32);

        for (var x: CHILDREN) {
            x.setPosition(getX() + x.offsetX, getY() + x.offsetY);
            x.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseDragged(double p_313749_, double p_313887_, int p_313839_, double p_313844_, double p_313686_) {
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return CHILDREN;
    }
}
