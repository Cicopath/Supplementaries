package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public abstract class ShulkerBoxSlotMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    public void isItemValid(ItemStack itemStackIn, CallbackInfoReturnable<Boolean> info ) {
        if(itemStackIn.getItem().is(ModTags.SHULKER_BLACKLIST_TAG))
            info.setReturnValue(false);
    }
}
