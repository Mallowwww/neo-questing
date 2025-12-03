package com.mallowwww.neoquesting.screen;

import com.daqem.uilib.client.gui.AbstractScreen;
import com.daqem.uilib.client.gui.background.Backgrounds;
import com.mallowwww.neoquesting.ModAttachments;
import com.mallowwww.neoquesting.ModRegistries;
import com.mallowwww.neoquesting.screen.widget.QuestPageWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class QuestScreen extends AbstractScreen {
    private QuestPageWidget QUESTS_WIDGET;
    private final Player PLAYER;
    private double X = 0;
    private double Y = 0;
    private final ModRegistries.ModQuests QUESTS;
    public QuestScreen(ModRegistries.ModQuests _quests, Player _player) {
        super(Component.literal("Quests"));
        QUESTS = _quests;
        this.PLAYER = _player;
    }

    @Override
    public void startScreen() {

        setBackground(Backgrounds.getDefaultBackground(width, height));
        setPauseScreen(false);
        setDragging(true);
        var data = getMinecraft().player.getData(ModAttachments.QUEST_ATTACHMENT_TYPE);
        QUESTS_WIDGET = new QuestPageWidget(0, 0, width, height, Component.literal("hi"), QUESTS, getMinecraft().player, data);
        addRenderableWidget(QUESTS_WIDGET);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 1)
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        X+=dragX;
        Y+=dragY;
        QUESTS_WIDGET.setPosition((int) X, (int) Y);
        return true;
    }

    @Override
    public void onTickScreen(GuiGraphics guiGraphics, int i, int i1, float v) {

    }
}
