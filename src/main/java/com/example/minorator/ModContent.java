package com.example.minorator;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Minorator.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Minorator.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Minorator.MOD_ID);

    public static final DeferredBlock<MinoratorBlock> MINORATOR = BLOCKS.registerBlock(
            "minorator",
            MinoratorBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.METAL)
                    .lightLevel(state -> state.getValue(MinoratorBlock.LIT) ? 7 : 0));

    public static final DeferredItem<BlockItem> MINORATOR_ITEM = ITEMS.register(
            "minorator", () -> new BlockItem(MINORATOR.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MinoratorBlockEntity>> MINORATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("minorator", () -> BlockEntityType.Builder.of(
                    MinoratorBlockEntity::new, MINORATOR.get()).build(null));

    private ModContent() {
    }
}
