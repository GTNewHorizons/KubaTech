package kubatech.loaders.block;

import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import kubatech.Tags;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import static kubatech.loaders.block.KubaBlock.defaultTileEntityUI;

public class BlockProxy {

    private final String unlocalizedName;
    private final String texturepath;
    private IIcon icon;

    public BlockProxy(String unlocalizedName, String texture) {
        this.unlocalizedName = "kubablock." + unlocalizedName;
        texturepath = Tags.MODID + ":" + texture;
    }

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
                    defaultTileEntityUI.open(player, world, x, y, z);
                return true;
            }
        }
        return false;
    }

    public void onBlockPlaced(World world, int x, int y, int z, EntityLivingBase player) {}

    public void registerIcon(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(texturepath);
    }

    public IIcon getIcon(int side) {
        return icon;
    }

    public String getUnlocalizedName() {
        return this.unlocalizedName;
    }

    public String getDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal(this.unlocalizedName + ".name").trim();
    }
}
