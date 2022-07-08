package net.mehvahdjukaar.supplementaries.client.block_models.forge;

import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.block.BlockProperties;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.MimicBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FrameBlockBakedModel implements IDynamicBakedModel {
    private final BakedModel overlay;
    private final BlockModelShaper blockModelShaper;

    public FrameBlockBakedModel(BakedModel overlay) {
        this.overlay = overlay;
        this.blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull ModelData extraData, RenderType renderType) {

        //always on cutout layer

        List<BakedQuad> quads = new ArrayList<>();

        if (state != null) {
            try {
                BlockState mimic = extraData.get(BlockProperties.MIMIC);
                if (mimic == null || (mimic.isAir())) {
                    BakedModel model = blockModelShaper.getBlockModel(state.setValue(FrameBlock.HAS_BLOCK, false));
                    quads.addAll(model.getQuads(mimic, side, rand, ModelData.EMPTY, renderType));
                    return quads;
                }

                if (!(mimic.getBlock() instanceof MimicBlock)) {
                    BakedModel model = blockModelShaper.getBlockModel(mimic);

                    quads.addAll(model.getQuads(mimic, side, rand, ModelData.EMPTY, renderType));
                }
            } catch (Exception ignored) {
            }

            //IBakedModel overlay = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state.setValue(FrameBlock.TILE,2));
            try {
                quads.addAll(overlay.getQuads(state, side, rand, ModelData.EMPTY, renderType));
            } catch (Exception ignored) {
            }
        }

        return quads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Textures.TIMBER_CROSS_BRACE_TEXTURE);
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }


}
