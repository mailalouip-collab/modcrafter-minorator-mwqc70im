package com.example.minorator;

import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

@Mod(Minorator.MOD_ID)
public final class Minorator {
    public static final String MOD_ID = "minorator";

    public Minorator(IEventBus modEventBus) {
        ModContent.BLOCKS.register(modEventBus);
        ModContent.ITEMS.register(modEventBus);
        ModContent.BLOCK_ENTITIES.register(modEventBus);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::addCreativeItems);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModContent.MINORATOR_BLOCK_ENTITY.get(),
                (blockEntity, side) -> new SidedInvWrapper(
                        blockEntity, side == null ? Direction.UP : side));
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModContent.MINORATOR_ITEM.get());
        }
    }
}
