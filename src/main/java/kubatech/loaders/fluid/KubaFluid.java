package kubatech.loaders.fluid;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import kubatech.Tags;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;

public class KubaFluid extends Fluid {

    public KubaFluid(String fluidName) {
        super(fluidName);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getUnlocalizedName() {
        return "kubafluid." + this.unlocalizedName;
    }

    public void registerIcon(IIconRegister iconRegister) {
        setIcons(iconRegister.registerIcon(Tags.MODID + ":fluids/" + getUnlocalizedName()));
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void textureHook(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) registerIcon(event.map);
    }
}
