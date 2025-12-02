package com.mallowwww.neoquesting;

import com.mallowwww.neoquesting.item.QuestBook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, NeoQuesting.MODID);
    public static final DeferredHolder<Item, QuestBook> QUEST_BOOK = ITEMS.register("quest_book", () -> new QuestBook(NeoQuesting.path("example")));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
