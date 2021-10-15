package net.mehvahdjukaar.supplementaries.block.tiles;

import net.mehvahdjukaar.selene.blocks.IOwnerProtected;
import net.mehvahdjukaar.selene.blocks.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.block.blocks.NoticeBoardBlock;
import net.mehvahdjukaar.supplementaries.block.util.IMapDisplay;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.inventories.NoticeBoardContainer;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.WrittenBookItem;

public class NoticeBoardBlockTile extends ItemDisplayTile implements Nameable, IMapDisplay {
    private UUID owner = null;
    //client stuff
    private String text = null;
    private int fontScale = 1;
    private List<FormattedCharSequence> cachedPageLines = Collections.emptyList();
    //used to tell renderer when it has to slit new line(have to do it there cause i need fontrenderer function)
    private boolean needsVisualRefresh = true;
    private Material cachedPattern = null;

    private boolean powered = false;
    private int pageNumber = 0;

    private DyeColor textColor = DyeColor.BLACK;
    // private int packedFrontLight =0;
    private boolean textVisible = true; //for culling

    public NoticeBoardBlockTile() {
        super(ModRegistry.NOTICE_BOARD_TILE.get());
    }

    @Override
    public Component getDefaultName() {
        return new TranslatableComponent("block.supplementaries.notice_board");
    }

    //update blockState and plays sound. server side
    @Override
    public void updateTileOnInventoryChanged() {

        boolean shouldHaveBook = !this.getDisplayedItem().isEmpty();

        BlockState state = this.getBlockState();
        if (state.getValue(BlockStateProperties.HAS_BOOK) != shouldHaveBook) {
            this.level.setBlock(this.worldPosition, state.setValue(BlockStateProperties.HAS_BOOK, shouldHaveBook), 2);
            if (shouldHaveBook) {
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.85F);
            } else {
                this.pageNumber = 0;
                this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                        this.level.random.nextFloat() * 0.10F + 0.50F);
            }
        }
    }

    @Override
    public ItemStack getMapStack() {
        return this.getDisplayedItem();
    }

    @Override
    public void updateClientVisualsOnLoad() {

        ItemStack itemstack = getDisplayedItem();
        Item item = itemstack.getItem();
        this.cachedPattern = null;
        if (item instanceof BannerPatternItem) {
            this.cachedPattern = Materials.FLAG_MATERIALS.get(((BannerPatternItem) item).getBannerPattern());
        }

        this.needsVisualRefresh = true;
        this.cachedPageLines = Collections.emptyList();
        this.text = null;

        if (item instanceof WrittenBookItem) {
            CompoundTag com = itemstack.getTag();
            if (WrittenBookItem.makeSureTagIsValid(com)) {

                ListTag listnbt = com.getList("pages", 8).copy();
                if (this.pageNumber >= listnbt.size()) {
                    this.pageNumber = this.pageNumber % listnbt.size();
                }
                this.text = listnbt.getString(this.pageNumber);
            }
        } else if (item instanceof WritableBookItem) {
            CompoundTag com = itemstack.getTag();
            if (WritableBookItem.makeSureTagIsValid(com)) {

                ListTag listnbt = com.getList("pages", 8).copy();
                if (this.pageNumber >= listnbt.size()) {
                    this.pageNumber = this.pageNumber % listnbt.size();
                }
                this.text = listnbt.getString(this.pageNumber);
            }
        }

    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        this.textColor = DyeColor.byName(compound.getString("Color"), DyeColor.BLACK);
        this.textVisible = compound.getBoolean("TextVisible");
        this.pageNumber = compound.getInt("PageNumber");
        super.load(state, compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putString("Color", this.textColor.getName());
        compound.putBoolean("TextVisible", this.textVisible);
        compound.putInt("PageNumber", this.pageNumber);
        return compound;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory player) {
        return new NoticeBoardContainer(id, player, this);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return this.isEmpty() && (ServerConfigs.cached.NOTICE_BOARDS_UNRESTRICTED || isPageItem(stack.getItem()));
    }

    public static boolean isPageItem(Item item) {
        return (ItemTags.LECTERN_BOOKS != null && item.is(ItemTags.LECTERN_BOOKS))
                || item instanceof MapItem || item instanceof BannerPatternItem;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    //TODO: remove some of these
    public DyeColor getTextColor() {
        return this.textColor;
    }

    public boolean setTextColor(DyeColor newColor) {
        if (newColor != this.getTextColor()) {
            this.textColor = newColor;
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldSkipTileRenderer() {
        return !textVisible || !getBlockState().getValue(NoticeBoardBlock.HAS_BOOK);
    }

    public void setTextVisible(boolean textVisible) {
        this.textVisible = textVisible;
    }

    public Material getCachedPattern() {
        return cachedPattern;
    }

    public String getText() {
        return text;
    }

    public int getFontScale() {
        return this.fontScale;
    }

    public void setFontScale(int s) {
        this.fontScale = s;
    }

    public void setCachedPageLines(List<FormattedCharSequence> l) {
        this.cachedPageLines = l;
    }

    public List<FormattedCharSequence> getCachedPageLines() {
        return this.cachedPageLines;
    }

    public boolean getFlag() {
        if (this.needsVisualRefresh) {
            this.needsVisualRefresh = false;
            return true;
        }
        return false;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(NoticeBoardBlock.FACING);
    }

    //server side only
    public void updatePower(boolean powered) {
        if (powered != this.powered && powered) {
            this.pageNumber++;
            this.level.playSound(null, worldPosition, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F,
                    this.level.random.nextFloat() * 0.10F + 1.45F);
            this.setChanged();
        }
        this.powered = powered;
    }


    public InteractionResult interact(Player player, InteractionHand handIn, BlockPos pos, BlockState state) {
        ItemStack itemStack = player.getItemInHand(handIn);
        Item item = itemStack.getItem();
        boolean server = !this.level.isClientSide;
        if (item instanceof DyeItem && player.abilities.mayBuild) {
            if (this.setTextColor(((DyeItem) item).getDyeColor())) {
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }
                if (server) {
                    this.setChanged();
                }
            }
        } else if (player.isShiftKeyDown() && !this.isEmpty()) {
            if (server) {
                ItemStack it = this.removeItemNoUpdate(0);
                BlockPos newPos = pos.offset(state.getValue(NoticeBoardBlock.FACING).getNormal());
                ItemEntity drop = new ItemEntity(this.level, newPos.getX() + 0.5, newPos.getY() + 0.5, newPos.getZ() + 0.5, it);
                drop.setDefaultPickUpDelay();
                this.level.addFreshEntity(drop);
                this.setChanged();
            }
        }
        //try place
        else if (!super.interact(player, handIn).consumesAction()) {
            if (server) {
                player.openMenu(this);
            }
        }
        return InteractionResult.sidedSuccess(!server);
    }
}