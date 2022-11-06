package kubatech.loaders.block;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class KubaItemBlock extends ItemBlock {
    public KubaItemBlock(Block p_i45328_1_) {
        super(p_i45328_1_);
        hasSubtypes = true;
    }

    @Override
    public void registerIcons(IIconRegister p_94581_1_) {
        super.registerIcons(p_94581_1_);
    }

    @Override
    public String getUnlocalizedName(ItemStack p_77667_1_) {
        return KubaBlock.blocks.get(p_77667_1_.getItemDamage()).getUnlocalizedName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack p_77653_1_) {
        return KubaBlock.blocks.get(p_77653_1_.getItemDamage()).getDisplayName(p_77653_1_);
    }
}
