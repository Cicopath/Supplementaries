package net.mehvahdjukaar.supplementaries.mixins;


import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.GrindstoneContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneContainer.class)
public abstract class GrindstoneContainerMixin extends Container {

    @Final
    @Shadow
    private IInventory resultSlots;

    @Final
    @Shadow
    private IInventory repairSlots;

    protected GrindstoneContainerMixin(@Nullable ContainerType<?> p_i50105_1_, int p_i50105_2_) {
        super(p_i50105_1_, p_i50105_2_);
    }


    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void updateGoldenAppleResult(CallbackInfo ci) {
        ItemStack stack1 = this.repairSlots.getItem(0);
        ItemStack stack2 = this.repairSlots.getItem(1);

        boolean apple1 = stack1.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
        boolean apple2 = stack2.getItem() == Items.ENCHANTED_GOLDEN_APPLE;

        if((apple1 && stack2.isEmpty()) || (apple2 && stack1.isEmpty()) || (apple1 && apple2)) {
            int count = stack1.getCount() + stack2.getCount();
            if (count <= Items.GOLDEN_APPLE.getMaxStackSize()) {
                this.resultSlots.setItem(0, new ItemStack(Items.GOLDEN_APPLE, count));
                this.broadcastChanges();
                ci.cancel();
            }
        }

        boolean bomb1 = stack1.getItem() == ModRegistry.BOMB_BLUE_ITEM.get();
        boolean bomb2 = stack2.getItem() == ModRegistry.BOMB_BLUE_ITEM.get();

        if((bomb1 && stack2.isEmpty()) || (bomb2 && stack1.isEmpty()) || (bomb1 && bomb2)) {
            int count = stack1.getCount() + stack2.getCount();
            if (count <= ModRegistry.BOMB_BLUE_ITEM.get().getMaxStackSize()) {
                this.resultSlots.setItem(0, new ItemStack(ModRegistry.BOMB_ITEM.get(), count));
                this.broadcastChanges();
                ci.cancel();
            }
        }

    }
}
