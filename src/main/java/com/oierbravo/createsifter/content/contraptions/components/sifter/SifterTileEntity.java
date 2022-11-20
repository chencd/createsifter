package com.oierbravo.createsifter.content.contraptions.components.sifter;

import com.oierbravo.createsifter.ModRecipeTypes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.sound.SoundScapes;
import com.simibubi.create.foundation.utility.VecHelper;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemTransferable;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SifterTileEntity extends KineticTileEntity implements ItemTransferable {
    public ItemStackHandler inputInv;
    public ItemStackHandler outputInv;
    public SifterInventoryHandler handler;
    public int timer;
    private SiftingRecipe lastRecipe;

    public ItemStackHandler meshInv;

    protected CombinedStorage<ItemVariant, ItemStackHandler> inputAndMeshCombined ;

    public SifterTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        inputInv = new ItemStackHandler(1);
        outputInv = new ItemStackHandler(9);
        handler = new SifterInventoryHandler();
        meshInv = new ItemStackHandler(1){
            @Override
            public boolean isItemValid(int slot, @NotNull ItemVariant stack) {
                if(SiftingRecipe.isMeshItemStack(stack.toStack())){
                    return true;
                }
                return false;
            }
        };
        inputAndMeshCombined = new CombinedStorage<>(List.of(inputInv,meshInv));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void tickAudio() {
        super.tickAudio();

        if (getSpeed() == 0)
            return;
        if (inputInv.getStackInSlot(0)
                .isEmpty())
            return;

        float pitch = Mth.clamp((Math.abs(getSpeed()) / 256f) + .45f, .85f, 1f);
        SoundScapes.play(SoundScapes.AmbienceGroup.MILLING, worldPosition, pitch);
    }

    @Override
    public void tick() {
        super.tick();

        if (getSpeed() == 0)
            return;
        for (int i = 0; i < outputInv.getSlots(); i++)
            if (outputInv.getStackInSlot(i)
                    .getCount() == outputInv.getSlotLimit(i))
                return;

        if (timer > 0) {
            timer -= getProcessingSpeed();

            if (level.isClientSide) {
                spawnParticles();
                return;
            }
            if (timer <= 0)
                process();
            return;
        }

        if (inputInv.getStackInSlot(0)
                .isEmpty())
            return;

        FabricRecipeWrapper inventoryIn = new FabricRecipeWrapper(inputAndMeshCombined);
        if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level)) {
            Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(inventoryIn, level);
            if (!recipe.isPresent()) {
                timer = 100;
                sendData();
            } else {
                lastRecipe = recipe.get();
                timer = lastRecipe.getProcessingDuration();
                sendData();
            }
            return;
        }

        timer = lastRecipe.getProcessingDuration();
        sendData();
    }

    private void process() {

        FabricRecipeWrapper inventoryIn = new FabricRecipeWrapper(inputAndMeshCombined);

        if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level)) {
            Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(inventoryIn, level);
            if (!recipe.isPresent())
                return;
            lastRecipe = recipe.get();
        }

        ItemStack stackInSlot = inputInv.getStackInSlot(0);
        stackInSlot.shrink(1);
        inputInv.setStackInSlot(0, stackInSlot);
        lastRecipe.rollResults()
                .forEach(stack -> {
                    try (Transaction t = TransferUtil.getTransaction()) {
                        long inserted = outputInv.insert(ItemVariant.of(stack), stack.getCount(), t);
                        t.commit();
                        long remainder = stack.getCount() - inserted;
                        if (remainder == 0)
                            return;
                        stack = stack.copy();
                        stack.setCount((int) remainder);
                    }
                });
        sendData();
        setChanged();
    }

    public void spawnParticles() {
        ItemStack stackInSlot = inputInv.getStackInSlot(0);
        if (stackInSlot.isEmpty())
            return;

        ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, stackInSlot);
        float angle = level.random.nextFloat() * 360;
        Vec3 offset = new Vec3(0, 0, 0.5f);
        offset = VecHelper.rotate(offset, angle, Direction.Axis.Y);
        Vec3 target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Direction.Axis.Y);

        Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
        target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
        level.addParticle(data, center.x, center.y, center.z, target.x, target.y, target.z);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putInt("Timer", timer);
        compound.put("InputInventory", inputInv.serializeNBT());
        compound.put("OutputInventory", outputInv.serializeNBT());
        compound.put("MeshInventory", meshInv.serializeNBT());

        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        timer = compound.getInt("Timer");
        inputInv.deserializeNBT(compound.getCompound("InputInventory"));
        outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
        meshInv.deserializeNBT(compound.getCompound("MeshInventory"));
        super.read(compound, clientPacket);
    }

    public int getProcessingSpeed() {
        return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
    }

    @Override
    public Storage<ItemVariant> getItemStorage(@Nullable Direction face) {
        return handler;
    }

    private boolean canProcess(ItemStack stack) {

        ItemStackHandler tester = new ItemStackHandler(2);
        tester.setStackInSlot(0, stack);
        tester.setStackInSlot(1, this.meshInv.getStackInSlot(0));
        RecipeWrapper inventoryIn = new RecipeWrapper(tester);

        if (lastRecipe != null && lastRecipe.matches(inventoryIn, level))
            return true;
        return ModRecipeTypes.SIFTING.find(inventoryIn, level)
                .isPresent();
    }

    public void insertMesh(ItemStack meshStack, Player player) {
        if(meshInv.getStackInSlot(0).isEmpty()){
            ItemStack meshToInsert = meshStack.copy();
            meshToInsert.setCount(1);
            meshStack.shrink(1);
            meshInv.setStackInSlot(0, meshToInsert);
            setChanged();
        }
    }
    public boolean hasMesh(){
        return !meshInv.getStackInSlot(0).isEmpty();
    }

    public void removeMesh(Player player) {
        player.getInventory().placeItemBackInInventory(meshInv.getStackInSlot(0));
        meshInv.setStackInSlot(0, ItemStack.EMPTY);
    }

    private class SifterInventoryHandler extends CombinedStorage<ItemVariant, ItemStackHandler> {
        public SifterInventoryHandler() {
            super(List.of(inputInv, outputInv));
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!canProcess(resource.toStack((int) maxAmount)))
                return 0;
            return inputInv.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return outputInv.extract(resource, maxAmount, transaction);
        }
    }
}
