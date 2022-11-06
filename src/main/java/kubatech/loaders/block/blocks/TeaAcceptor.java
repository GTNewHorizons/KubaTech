package kubatech.loaders.block.blocks;

import kubatech.loaders.block.BlockProxy;
import kubatech.loaders.block.IProxyTileEntityProvider;
import kubatech.tileentity.TeaAcceptorTile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TeaAcceptor extends BlockProxy implements IProxyTileEntityProvider {

    @Override
    public TileEntity createTileEntity(World world) {
        return new TeaAcceptorTile();
    }

    @Override
    public void onBlockPlaced(World world, int x, int y, int z, EntityLivingBase player) {
        if (world.isRemote) return;
        if (!(player instanceof EntityPlayerMP)) return;
        ((TeaAcceptorTile) world.getTileEntity(x, y, z)).setTeaOwner(player.getCommandSenderName());
    }
}
