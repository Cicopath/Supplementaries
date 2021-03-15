package net.mehvahdjukaar.supplementaries.block.blocks;


import net.mehvahdjukaar.supplementaries.block.tiles.SignPostBlockTile;
import net.mehvahdjukaar.supplementaries.client.gui.SignPostGui;
import net.mehvahdjukaar.supplementaries.datagen.types.VanillaWoodTypes;
import net.mehvahdjukaar.supplementaries.items.SignPostItem;
import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.extensions.IForgeBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class SignPostBlock extends FenceMimicBlock{

    public SignPostBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
                                             BlockRayTraceResult hit) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof SignPostBlockTile) {
            SignPostBlockTile te = (SignPostBlockTile) tileentity;
            ItemStack itemstack = player.getHeldItem(handIn);
            Item item = itemstack.getItem();

            //put post on map
            if(item instanceof FilledMapItem){
                MapData data = FilledMapItem.getMapData(itemstack,worldIn);
                if(data!=null) {
                    data.tryAddBanner(worldIn, pos);
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }


            boolean server = !worldIn.isRemote();
            boolean emptyhand = itemstack.isEmpty();
            boolean isDye = item instanceof DyeItem && player.abilities.allowEdit;
            boolean isSneaking = player.isSneaking() && emptyhand;
            boolean isSignPost = item instanceof SignPostItem;
            boolean isCompass = item instanceof CompassItem;
            //color
            if (isDye){
                if(te.textHolder.setTextColor(((DyeItem) itemstack.getItem()).getDyeColor())){
                    if (!player.isCreative()) {
                        itemstack.shrink(1);
                    }
                    if(server)te.markDirty();
                }
            }
            //sneak right click rotates the sign on z axis
            else if (isSneaking){
                double y = hit.getHitVec().y;
                boolean up = y%((int)y) > 0.5d;
                if(up){
                    te.leftUp = !te.leftUp;
                }
                else{
                    te.leftDown = !te.leftDown;
                }
                if(server)te.markDirty();
            }
            //change direction with compass
            else if (isCompass){
                //itemModelProperties code
                BlockPos pointingPos = CompassItem.func_234670_d_(itemstack) ?
                        this.getLodestonePos(worldIn, itemstack.getOrCreateTag()) : this.getWorldSpawnPos(worldIn);

                if(pointingPos!=null) {
                    double y = hit.getHitVec().y;
                    boolean up = y % ((int) y) > 0.5d;
                    if (up && te.up) {
                        te.pointToward(pointingPos,true);
                    } else if (!up && te.down) {
                        te.pointToward(pointingPos,false);
                    }
                    if(server)te.markDirty();
                    return ActionResultType.func_233537_a_(worldIn.isRemote);
                }
            }
            else if (isSignPost){
                //let sign item handle this one
                return ActionResultType.PASS;
            }
            // open gui (edit sign with empty hand)
            else if (!server) {
                SignPostGui.open(te);
            }
            return ActionResultType.func_233537_a_(worldIn.isRemote);
        }
        return ActionResultType.PASS;
    }


    @Nullable
    private BlockPos getLodestonePos(World world, CompoundNBT cmp) {
        boolean flag = cmp.contains("LodestonePos");
        boolean flag1 = cmp.contains("LodestoneDimension");
        if (flag && flag1) {
            Optional<RegistryKey<World>> optional = CompassItem.func_234667_a_(cmp);
            if ( optional.isPresent() && world.getDimensionKey() == optional.get() ) {
                return NBTUtil.readBlockPos(cmp.getCompound("LodestonePos"));
            }
        }
        return null;
    }

    @Nullable
    private BlockPos getWorldSpawnPos(World world) {
        return world.getDimensionType().isNatural() ? new BlockPos(world.getWorldInfo().getSpawnX(),
                world.getWorldInfo().getSpawnY(),world.getWorldInfo().getSpawnZ()) : null;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile)te);
            double y = target.getHitVec().y;
            boolean up = y%((int)y) > 0.5d;
            if(up && tile.up){
                return new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
            }
            else if(!up && tile.down){
                return new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
            }
            else return new ItemStack(tile.fenceBlock.getBlock());
        }
        return new ItemStack(Registry.SIGN_POST_ITEMS.get(VanillaWoodTypes.OAK).get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof SignPostBlockTile){
            SignPostBlockTile tile = ((SignPostBlockTile) tileentity);
            List<ItemStack> list = new ArrayList<>();
            list.add(new ItemStack(tile.fenceBlock.getBlock()));

            if (tile.up) {
                ItemStack s = new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeUp).get());
                list.add(s);
            }
            if (tile.down) {
                ItemStack s = new ItemStack(Registry.SIGN_POST_ITEMS.get(tile.woodTypeDown).get());
                list.add(s);
            }
            return list;
        }
        return super.getDrops(state,builder);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
        float angle = rot.equals(Rotation.CLOCKWISE_90)? 90 : -90;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof SignPostBlockTile) {
            SignPostBlockTile tile = (SignPostBlockTile) te;
            boolean success = false;
            if(tile.up){
                tile.yawUp= MathHelper.wrapDegrees(tile.yawUp+angle);
                success=true;
            }
            if(tile.down){
                tile.yawDown= MathHelper.wrapDegrees(tile.yawDown+angle);
                success=true;
            }

            if(success){
                //world.notifyBlockUpdate(pos, tile.getBlockState(), tile.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
                tile.markDirty();
            }
        }
        return state;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SignPostBlockTile();
    }

}