package kubatech.loaders.fluid;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kubatech.Tags;
import kubatech.api.utils.ModUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;

public class KubaFluid extends Fluid {

    public KubaFluid(String fluidName) {
        super(fluidName);
        if (ModUtils.isClientSided) MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getUnlocalizedName() {
        return "kubafluid." + this.unlocalizedName;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcon(IIconRegister iconRegister) {
        setIcons(iconRegister.registerIcon(Tags.MODID + ":fluids/" + getUnlocalizedName()));
    }

    @SuppressWarnings("unused")
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void textureHook(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) registerIcon(event.map);
    }
}
