package net.mehvahdjukaar.supplementaries.mixins.forge;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteLiquidBlock;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.forge.ModFluidsImpl;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {

    @WrapOperation(method = "getHeight(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/Fluid;isSame(Lnet/minecraft/world/level/material/Fluid;)Z"))
    public boolean supplementaries$modifyLumiseneHeight(Fluid instance, Fluid above, Operation<Boolean> original) {
       return original.call(instance, above) || above.isSame(ModFluids.LUMISENE_FLUID.get());
    }

    @Inject(method = "calculateAverageHeight",
            at = @At("HEAD"), cancellable = true)
    public void supplementaries$modifyLumiseneHeight(BlockAndTintGetter level, Fluid fluid, float g, float h, float i, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        ModFluidsImpl.messWithAvH(level, fluid, g, h, i, pos, cir);
    }

    @WrapOperation(method = "getLightColor",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I"))
    public int supplementaries$modifyLumiseneLight(BlockAndTintGetter level, BlockPos pos, Operation<Integer> original) {
        return ModFluidsImpl.messWithFluidLight(level, pos, original);
    }
}
