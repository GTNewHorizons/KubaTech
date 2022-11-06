package kubatech.loaders.block;

import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockProxy {

    public void itemInit(int ID) {}

    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player) {
        if (this instanceof IProxyTileEntityProvider) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof ITileWithModularUI) {
                if (world.isRemote) return true;
                if (te instanceof KubaBlock.IModularUIContainerCreator)
                    KubaBlock.TileEntityUIFactory.apply((KubaBlock.IModularUIContainerCreator) te)
                            .open(player, world, x, y, z);
                else
                    KubaBlock.TileEntityUIFactory.apply(ModularUIContainer::new).open(player, world, x, y, z);
                return true;
            }
        }
        return false;
    }

    public void onBlockPlaced(World world, int x, int y, int z, EntityLivingBase player) {}
}
