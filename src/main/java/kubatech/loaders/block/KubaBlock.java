package kubatech.loaders.block;

import java.util.HashMap;
import java.util.List;
import kubatech.loaders.BlockLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class KubaBlock extends Block {

    private static final HashMap<Integer, BlockProxy> blocks = new HashMap<>();
    private static int idCounter = 0;

    public KubaBlock(Material p_i45394_1_) {
        super(p_i45394_1_);
    }

    public ItemStack registerProxyBlock(BlockProxy block) {
        blocks.put(idCounter, block);
        block.itemInit(idCounter);
        return new ItemStack(BlockLoader.kubaItemBlock, 1, idCounter++);
    }

    private BlockProxy getBlock(int id) {
        return blocks.get(id);
    }

    @Override
    public boolean hasTileEntity(int meta) {
        return getBlock(meta) instanceof IProxyTileEntityProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        for (int i = 0; i < blocks.size(); i++) p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        if (!hasTileEntity(metadata)) return null;
        return ((IProxyTileEntityProvider) getBlock(metadata)).createTileEntity(world);
    }
}
