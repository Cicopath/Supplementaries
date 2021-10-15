package net.mehvahdjukaar.supplementaries.block.blocks;

import net.mehvahdjukaar.supplementaries.block.tiles.BookPileBlockTile;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BookPileHorizontalBlock extends BookPileBlock {

    private static final VoxelShape SHAPE_1_Z = Block.box(6D, 0D, 4D, 10D, 10D, 12D);
    private static final VoxelShape SHAPE_1_X = Block.box(4D, 0D, 6D, 12D, 10D, 10D);

    private static final VoxelShape SHAPE_2_Z = Block.box(3D, 0D, 4D, 13D, 10D, 12D);
    private static final VoxelShape SHAPE_2_X = Block.box(4D, 0D, 3D, 12D, 10D, 13D);


    private static final VoxelShape SHAPE_3_Z = Block.box(1D, 0D, 4D, 15D, 10D, 12D);
    private static final VoxelShape SHAPE_3_X = Block.box(4D, 0D, 2D, 12D, 10D, 14D);


    private static final VoxelShape SHAPE_4_Z = Block.box(0D, 0D, 4D, 16D, 10D, 12D);
    private static final VoxelShape SHAPE_4_X = Block.box(4D, 0D, 0D, 12D, 10D, 16D);

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BookPileHorizontalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false).setValue(BOOKS, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
        if (blockstate.is(this)) {
            return blockstate.setValue(BOOKS, blockstate.getValue(BOOKS) + 1);
        }
        boolean flag = context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER;
        return this.defaultBlockState().setValue(WATERLOGGED, flag).setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new BookPileBlockTile(true);
    }

    public boolean isAcceptedItem(Item i){
        return i == Items.BOOK;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        boolean x = state.getValue(FACING).getAxis()== Direction.Axis.X;

        switch (state.getValue(BOOKS)){
            default:
            case 1:
                return x ? SHAPE_1_X : SHAPE_1_Z;
            case 2:
                return x ? SHAPE_2_X : SHAPE_2_Z;
            case 3:
                return x ? SHAPE_3_X : SHAPE_3_Z;
            case 4:
                return x ? SHAPE_4_X : SHAPE_4_Z;
        }
    }
}
