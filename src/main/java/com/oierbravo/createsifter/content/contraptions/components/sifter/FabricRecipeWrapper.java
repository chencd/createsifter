package com.oierbravo.createsifter.content.contraptions.components.sifter;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class FabricRecipeWrapper extends RecipeWrapper {
    protected final CombinedStorage<ItemVariant, ItemStackHandler> handler;

    protected final int[] baseIndex;
    private final int slots;

    public FabricRecipeWrapper(CombinedStorage<ItemVariant, ItemStackHandler> handler) {
        super(handler.parts.get(0)); // Hack to get around create fabric
        this.handler = handler;
        this.baseIndex = new int[handler.parts.size()];
        int index = 0;
        for (int i = 0; i < handler.parts.size(); i++) {
            index += handler.parts.get(i).getSlots();
            baseIndex[i] = index;
        }
        this.slots = index;
    }

    @Override
    public int getContainerSize() {
        return slots;
    }

    @Nullable
    protected ItemStackHandler getHandlerFromIndex(int index) {
        if (index < 0 || index >= handler.parts.size()) {
            return null;
        }
        return handler.parts.get(index);
    }

    protected int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= baseIndex.length) {
            return slot;
        }
        return slot - baseIndex[index - 1];
    }

    protected int getIndexForSlot(int slot) {
        if (slot < 0)
            return -1;

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStackHandler inv : handler.parts) {
            for (ItemStack stack : inv.stacks) {
                if (!stack.isEmpty())
                    return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        int index = getIndexForSlot(slot);
        ItemStackHandler handler = getHandlerFromIndex(index);
        if (handler == null)
            return ItemStack.EMPTY;
        slot = getSlotFromIndex(slot, index);
        return handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        int index = getIndexForSlot(slot);
        ItemStackHandler handler = getHandlerFromIndex(index);
        if (handler == null)
            return ItemStack.EMPTY;
        slot = getSlotFromIndex(slot, index);
        ItemStack stack = handler.getStackInSlot(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack s = getItem(index);
        if(s.isEmpty()) return ItemStack.EMPTY;
        setItem(index, ItemStack.EMPTY);
        return s;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int index = getIndexForSlot(slot);
        ItemStackHandler handler = getHandlerFromIndex(index);
        if (handler == null)
            return;
        slot = getSlotFromIndex(slot, index);
        handler.setStackInSlot(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        int index = getIndexForSlot(slot);
        ItemStackHandler handler = getHandlerFromIndex(index);
        if (handler == null)
            return false;
        int localSlot = getSlotFromIndex(slot, index);
        return handler.isItemValid(localSlot, ItemVariant.of(stack));
    }

    @Override
    public void clearContent() {
        handler.parts.forEach(itemStackHandler -> Arrays.fill(itemStackHandler.stacks, ItemStack.EMPTY));
    }

    @Override
    public int getMaxStackSize() { return 0; }
    @Override
    public void setChanged() {}
    @Override
    public boolean stillValid(Player player) { return false; }
    @Override
    public void startOpen(Player player) {}
    @Override
    public void stopOpen(Player player) {}

    @Override
    public String toString() {
        return "RecipeWrapper{" + handler + "}";
    }
}