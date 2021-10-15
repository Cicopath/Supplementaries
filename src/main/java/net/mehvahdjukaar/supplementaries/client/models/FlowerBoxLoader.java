package net.mehvahdjukaar.supplementaries.client.models;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class FlowerBoxLoader implements IModelLoader<FlowerBoxGeometry> {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }

    @Override
    public FlowerBoxGeometry read(JsonDeserializationContext context, JsonObject json) {
        BlockModel model;
        model = ModelLoaderRegistry.ExpandedBlockModelDeserializer.INSTANCE
                .getAdapter(BlockModel.class).fromJsonTree(json.get("box"));
        return new FlowerBoxGeometry(model);
    }
}
