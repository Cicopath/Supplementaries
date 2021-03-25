package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.block.tiles.JarBlockTile;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;


import net.minecraft.block.AbstractBlock.Properties;

public class JarBlock extends Block implements IWaterLoggable {
    protected static final VoxelShape SHAPE = VoxelShapes.or(VoxelShapes.box(0.1875D, 0D, 0.1875D, 0.8125D, 0.875D, 0.8125D),
            VoxelShapes.box(0.3125, 0.875, 0.3125, 0.6875, 1, 0.6875));

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LIGHT_LEVEL = BlockProperties.LIGHT_LEVEL_0_15;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public JarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT_LEVEL, 0).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED,false));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    //check if it only gets called client side
    public int getJarLiquidColor(BlockPos pos, IWorldReader world){
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof JarBlockTile) {
            return ((JarBlockTile)te).fluidHolder.getParticleColor();
        }
        return 0xffffff;
    }

    @Override
    public float[] getBeaconColorMultiplier(BlockState state, IWorldReader world, BlockPos pos, BlockPos beaconPos) {
        int color = getJarLiquidColor(pos,world);
        if(color==-1)return null;
        float r = (float) ((color >> 16 & 255)) / 255.0F;
        float g = (float) ((color >> 8 & 255)) / 255.0F;
        float b = (float) ((color & 255)) / 255.0F;
        return new float[]{r, g, b};
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            // make te do the work
            JarBlockTile te = (JarBlockTile) tileentity;
            if (te.handleInteraction(player, handIn)) {
                if (!worldIn.isClientSide())
                    te.setChanged();
                return ActionResultType.sidedSuccess(worldIn.isClientSide);
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            TileEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof JarBlockTile) {
                ((LockableTileEntity) tileentity).setCustomName(stack.getHoverName());
            }
        }

        //remove in the future
        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            ((JarBlockTile) tileentity).convertOldJars(stack.getTagElement("BlockEntityTag"));
        }
    }

    public ItemStack getJarItem(JarBlockTile te){
        ItemStack returnStack;
        //TODO: generalize this
        boolean flag = this.getBlock() == Registry.JAR.get();
        if(!te.hasContent()){
            returnStack = new ItemStack(flag ? Registry.EMPTY_JAR_ITEM.get() : Registry.EMPTY_JAR_ITEM_TINTED.get());
        }
        else{
            returnStack = new ItemStack(flag ? Registry.JAR_ITEM.get() : Registry.JAR_ITEM_TINTED.get());
            CompoundNBT compoundnbt = te.save(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
                returnStack.addTagElement("BlockEntityTag", compoundnbt);
            }
        }
        if(te.hasCustomName()){
            returnStack.setHoverName(te.getCustomName());
        }
        return returnStack;
    }

    // shulker box code

    //forces creative drop. might remove this since pick block does work
    /*
    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile tile = (JarBlockTile) tileentity;
            if (!worldIn.isRemote && player.isCreative() && tile.hasContent()) {

                ItemStack itemstack = this.getJarItem(tile);

                ItemEntity itementity = new ItemEntity(worldIn, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, itemstack);
                itementity.setDefaultPickupDelay();
                worldIn.addEntity(itementity);
            } else {
                tile.fillWithLoot(player);
            }
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }*/

    @Override
    public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile tile = (JarBlockTile) tileentity;

            ItemStack itemstack = this.getJarItem(tile);

            return Collections.singletonList(itemstack);
        }
        return super.getDrops(state, builder);
    }

    //for pick block
    @Override
    public ItemStack getCloneItemStack(IBlockReader worldIn, BlockPos pos, BlockState state) {

        TileEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile tile = (JarBlockTile) tileentity;
            return this.getJarItem(tile);
        }
        return super.getCloneItemStack(worldIn, pos, state);
    }

    // end shoulker box code
    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(LIGHT_LEVEL,FACING,WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof INamedContainerProvider ? (INamedContainerProvider) tileEntity : null;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new JarBlockTile();
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIGHT_LEVEL);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos) {
        TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof JarBlockTile) {
            JarBlockTile te = ((JarBlockTile) tileentity);
            if (!te.isEmpty())
                return Container.getRedstoneSignalFromContainer(te);
            else if (!te.fluidHolder.isEmpty()) {
                return ((JarBlockTile) tileentity).fluidHolder.getComparator();
            }
            else if(!te.mobHolder.isEmpty())return 15;
        }
        return 0;
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED,context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER);
    }


}