package com.example.minorator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MinoratorBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int FUEL_TICKS = 1600;
    public static final int CYCLE_TICKS = 400;
    private static final int[] FUEL_SLOTS = {0};
    private static final int[] OUTPUT_SLOTS = {1, 2, 3, 4};
    private static final Item[] OUTPUT_ITEMS = {
            Items.IRON_INGOT, Items.COAL, Items.DIAMOND, Items.GOLD_INGOT
    };
    private static final int[] OUTPUT_WEIGHTS = {60, 20, 2, 18};

    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int burnTicks;
    private int workTicks;

    public MinoratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.MINORATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MinoratorBlockEntity machine) {
        boolean changed = false;
        boolean hasSpace = machine.hasOutputSpace();

        if (hasSpace) {
            if (machine.burnTicks == 0 && isFuel(machine.items.get(0))) {
                machine.items.get(0).shrink(1);
                if (machine.items.get(0).isEmpty()) {
                    machine.items.set(0, ItemStack.EMPTY);
                }
                machine.burnTicks = FUEL_TICKS;
                changed = true;
            }

            if (machine.burnTicks > 0) {
                machine.burnTicks--;
                machine.workTicks++;
                if (machine.workTicks >= CYCLE_TICKS) {
                    machine.produceItem(level);
                    machine.workTicks = 0;
                }
                changed = true;
            }
        }

        boolean lit = hasSpace && machine.burnTicks > 0;
        if (state.getValue(MinoratorBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MinoratorBlock.LIT, lit), Block.UPDATE_ALL);
            changed = true;
        }
        if (changed) {
            machine.setChanged();
        }
    }

    private boolean canStoreOutput(int outputIndex) {
        ItemStack stack = items.get(outputIndex + 1);
        return stack.isEmpty() || (stack.is(OUTPUT_ITEMS[outputIndex])
                && stack.getCount() < Math.min(getMaxStackSize(), stack.getMaxStackSize()));
    }

    private boolean hasOutputSpace() {
        for (int i = 0; i < OUTPUT_ITEMS.length; i++) {
            if (canStoreOutput(i)) {
                return true;
            }
        }
        return false;
    }

    private void produceItem(Level level) {
        // Roll the normal distribution first. A full destination forfeits this
        // cycle's output instead of increasing the odds of rarer resources.
        int roll = level.random.nextInt(100);
        for (int i = 0; i < OUTPUT_ITEMS.length; i++) {
            roll -= OUTPUT_WEIGHTS[i];
            if (roll < 0) {
                if (canStoreOutput(i)) {
                    ItemStack stack = items.get(i + 1);
                    if (stack.isEmpty()) {
                        items.set(i + 1, new ItemStack(OUTPUT_ITEMS[i]));
                    } else {
                        stack.grow(1);
                    }
                }
                return;
            }
        }
    }

    public int insertFuel(ItemStack held, boolean creative) {
        if (!isFuel(held)) {
            return 0;
        }
        ItemStack existing = items.get(0);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, held)) {
            return 0;
        }
        int room = Math.min(getMaxStackSize(), held.getMaxStackSize()) - existing.getCount();
        int count = Math.min(room, held.getCount());
        if (count <= 0) {
            return 0;
        }
        if (existing.isEmpty()) {
            items.set(0, held.copyWithCount(count));
        } else {
            existing.grow(count);
        }
        if (!creative) {
            held.shrink(count);
        }
        setChanged();
        return count;
    }

    public boolean giveSlotToPlayer(int slot, Player player) {
        if (items.get(slot).isEmpty()) {
            return false;
        }
        ItemStack extracted = removeItem(slot, getMaxStackSize());
        player.getInventory().add(extracted);
        if (!extracted.isEmpty()) {
            player.drop(extracted, false);
        }
        return true;
    }

    public boolean giveFirstOutputToPlayer(Player player) {
        for (int slot : OUTPUT_SLOTS) {
            if (giveSlotToPlayer(slot, player)) {
                return true;
            }
        }
        return false;
    }

    public int getBurnTicks() {
        return burnTicks;
    }

    public int getWorkTicks() {
        return workTicks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("BurnTicks", burnTicks);
        tag.putInt("WorkTicks", workTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.clear();
        ContainerHelper.loadAllItems(tag, items, registries);
        burnTicks = Math.clamp(tag.getInt("BurnTicks"), 0, FUEL_TICKS);
        workTicks = Math.clamp(tag.getInt("WorkTicks"), 0, CYCLE_TICKS - 1);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty()) {
            stack.setCount(Math.min(stack.getCount(), Math.min(getMaxStackSize(), stack.getMaxStackSize())));
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && isFuel(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT_SLOTS.clone() : FUEL_SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side != Direction.DOWN && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot >= 1 && slot <= 4;
    }
}
