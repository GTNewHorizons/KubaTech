package kubatech.loaders;

import kubatech.api.enums.FluidList;
import kubatech.loaders.fluid.KubaFluid;
import kubatech.loaders.item.FluidCell;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidLoader {

    public static void registerFluids() {
        Fluid flowerExtract = new KubaFluid("flowerextract");
        FluidRegistry.registerFluid(flowerExtract);
        FluidList.FlowerExtract.set(flowerExtract);
        FluidCell.registerFluidCell(flowerExtract);
    }
}
