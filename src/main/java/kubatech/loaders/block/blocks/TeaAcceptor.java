package kubatech.loaders.block.blocks;

import kubatech.loaders.block.BlockProxy;
import kubatech.loaders.block.IProxyTileEntityProvider;
import kubatech.tileentity.TeaAcceptorTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TeaAcceptor extends BlockProxy implements IProxyTileEntityProvider {

    @Override
    public TileEntity createTileEntity(World world) {
        return new TeaAcceptorTile();
    }
}
