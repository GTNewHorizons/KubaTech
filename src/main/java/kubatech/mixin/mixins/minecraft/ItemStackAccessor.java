package kubatech.mixin.mixins.minecraft;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemStack.class)
public interface ItemStackAccessor {

    @Accessor(value = "field_151002_e", remap = false)
    Item getItem();

    @Accessor
    int getItemDamage();

}
