package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.selene.blocks.WaterBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class GlobeBlock extends WaterBlock {
    protected static final VoxelShape SHAPE = Shapes.box(0.125D, 0D, 0.125D, 0.875D, 1D, 0.875D);

    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GlobeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false).setValue(TRIGGERED,false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(!ClientConfigs.cached.TOOLTIP_HINTS || !Minecraft.getInstance().options.advancedItemTooltips)return;
        tooltip.add(new TranslatableComponent("message.supplementaries.globe").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));

    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        this.updatePower(state, worldIn, pos);
        if (stack.hasCustomHoverName()) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof GlobeBlockTile) {
                ((GlobeBlockTile) tileentity).setCustomName(stack.getHoverName());
            }
        }
    }

    public void updatePower(BlockState state, Level world, BlockPos pos) {
        boolean powered = world.getBestNeighborSignal(pos) > 0;
        if(powered != state.getValue(TRIGGERED)){
            world.setBlock(pos, state.setValue(TRIGGERED, powered), 4);
            //server
            //calls event on server and client through packet
            if(powered)
                world.blockEvent(pos, state.getBlock(), 1, 0);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                             BlockHitResult hit) {
        if(player.getItemInHand(handIn).getItem() instanceof ShearsItem){
            BlockEntity te = worldIn.getBlockEntity(pos);
            if(te instanceof GlobeBlockTile ){
                ((GlobeBlockTile) te).sheared = !((GlobeBlockTile) te).sheared;
                if(worldIn.isClientSide){
                    Minecraft.getInstance().particleEngine.destroy(pos, state);
                }
                return InteractionResult.sidedSuccess(worldIn.isClientSide);
            }
        }

        if (!worldIn.isClientSide) {
            worldIn.blockEvent(pos, state.getBlock(), 1, 0);
        }
        else {
            player.displayClientMessage(new TextComponent("X: "+pos.getX()+", Z: "+pos.getZ()), true);
        }
        return InteractionResult.sidedSuccess(worldIn.isClientSide);
    }

    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        if(CommonUtil.FESTIVITY.isEarthDay() && worldIn.isClientSide){
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            for (int l = 0; l < 1; ++l) {
                double d0 = (x + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                double d1 = (y + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                double d2 = (z + 0.5 + (rand.nextFloat() - 0.5) * (0.625D));
                worldIn.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0, 0, 0);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, moving);
        this.updatePower(state,world,pos);
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }
        return facing == Direction.DOWN && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return !worldIn.isEmptyBlock(pos.below());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, FACING, TRIGGERED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, flag);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new GlobeBlockTile();
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity tileentity = world.getBlockEntity(pos);
        return tileentity != null && tileentity.triggerEvent(eventID, eventParam);
    }

    @Override
    public boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
            if(te instanceof GlobeBlockTile){
                if(((GlobeBlockTile)te).yaw!=0) return 15;
                else return ((GlobeBlockTile)te).face/-90 +1;
            }
        return 0;
    }

}