package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.supplementaries.block.blocks.SackBlock;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.inventories.SackContainer;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import vazkii.quark.api.ITransferManager;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class SackBlockTile extends LockableLootTileEntity implements INameable, ISidedInventory, ICapabilityProvider, ITransferManager {

    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private int numPlayersUsing;

    private ITextComponent customName;

    public SackBlockTile() {
        super(Registry.SACK_TILE.get());
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getCustomName() {
        return this.customName;
    }

    @Override
    public ITextComponent getDefaultName() {
        return new TranslationTextComponent("block.supplementaries.sack");
    }

    @Override
    public void startOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean flag = blockstate.getValue(SackBlock.OPEN);
            if (!flag) {
                this.level.playSound(null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.WOOL_BREAK, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.55F);
                this.level.playSound(null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.7F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SackBlock.OPEN, true), 3);
            }
            this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
        }
    }
    public static int calculatePlayersUsing(World world, LockableTileEntity tile, int x, int y, int z) {
        int i = 0;
        for(PlayerEntity playerentity : world.getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB((float)x - 5.0F, (float)y - 5.0F, (float)z - 5.0F, (float)(x + 1) + 5.0F, (float)(y + 1) + 5.0F, (float)(z + 1) + 5.0F))) {
            if (playerentity.containerMenu instanceof SackContainer) {
                IInventory iinventory = ((SackContainer)playerentity.containerMenu).inventory;
                if (iinventory == tile) {
                    ++i;
                }
            }
        }
        return i;
    }

    public void barrelTick() {
        int i = this.worldPosition.getX();
        int j = this.worldPosition.getY();
        int k = this.worldPosition.getZ();
        this.numPlayersUsing = calculatePlayersUsing(this.level, this, i, j, k);
        if (this.numPlayersUsing > 0) {
            this.level.getBlockTicks().scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
        } else {
            BlockState blockstate = this.getBlockState();
            /*
            if (!blockstate.isIn(Blocks.BARREL)) {
                this.remove();
                return;
            }*/

            boolean flag = blockstate.getValue(SackBlock.OPEN);
            if (flag) {
                //this.playSound(blockstate, SoundEvents.BLOCK_BARREL_CLOSE);
                this.level.playSound((PlayerEntity)null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.WOOL_BREAK, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.5F);
                this.level.playSound(null, this.worldPosition.getX()+0.5, this.worldPosition.getY()+0.5, this.worldPosition.getZ()+0.5,
                        SoundEvents.LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.6F);
                this.level.setBlock(this.getBlockPos(), blockstate.setValue(SackBlock.OPEN, false), 3);
            }
        }

    }

    @Override
    public void stopOpen(PlayerEntity player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        this.loadFromNbt(nbt);
    }

    //TODO: separate save to nbt from write so you don't write data you don't need. it update packet too
    @Override
    public CompoundNBT save(CompoundNBT compound) {
        super.save(compound);
        return this.saveToNbt(compound);
    }

    public void loadFromNbt(CompoundNBT compound) {
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compound) && compound.contains("Items", 9)) {
            ItemStackHelper.loadAllItems(compound, this.items);
        }
        if (compound.contains("CustomName", 8)) {
            this.customName = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
        }
    }

    public CompoundNBT saveToNbt(CompoundNBT compound) {
        if (!this.trySaveLootTable(compound)) {
            ItemStackHelper.saveAllItems(compound, this.items, false);
        }
        if (this.customName != null) {
            compound.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
        }
        return compound;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemsIn) {
        this.items = itemsIn;
    }

    @Override
    public Container createMenu(int id, PlayerInventory player) {
        return new SackContainer(id, player, this);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }

    public boolean isSlotUnlocked(int ind){
        int add = ServerConfigs.cached.SACK_SLOTS;
        return (ind<(this.getContainerSize()-4+(2*add)));
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return isSlotUnlocked(index) && CommonUtil.isAllowedInShulker(stack);
        //return super.isItemValidForSlot(index,stack);
    }

    //TODO: figure out what this handlers and ISided inventory do
    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return isSlotUnlocked(index);
    }

    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }

    @Override
    public boolean acceptsTransfer(PlayerEntity player) {
        return false;
    }
}
