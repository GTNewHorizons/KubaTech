package kubatech.api.helpers;

import static gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase.isValidMetaTileEntity;

import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class GTHelper {

    private static final double ln4 = Math.log(4d);

    public static int calculateOverclockedNessMulti(
            GT_MetaTileEntity_MultiBlockBase mte, long aEUt, int aDuration, boolean perfect) {
        long maxInputVoltage = getMaxInputEU(mte);
        int tiers = (int) (Math.log((double) maxInputVoltage / (double) aEUt) / ln4);
        if (tiers <= 0) {
            mte.mEUt = (int) aEUt;
            mte.mMaxProgresstime = aDuration;
            return 0;
        }
        mte.mEUt = (int) (aEUt << (tiers << 1));
        int dMulti = 1;
        int aDurationModifier = perfect ? 2 : 1;
        for (int i = 0; i < tiers; i++)
            if (aDuration > 1) aDuration >>= aDurationModifier;
            else dMulti <<= aDurationModifier;
        if (dMulti > 1) {
            for (ItemStack mOutputItem : mte.mOutputItems) mOutputItem.stackSize *= dMulti;
            for (FluidStack mOutputFluid : mte.mOutputFluids) mOutputFluid.amount *= dMulti;
        }
        if (aDuration <= 0) aDuration = 1;
        mte.mMaxProgresstime = aDuration;
        return tiers;
    }

    public static long getMaxInputEU(GT_MetaTileEntity_MultiBlockBase mte) {
        long rEU = 0;
        for (GT_MetaTileEntity_Hatch_Energy tHatch : mte.mEnergyHatches)
            if (isValidMetaTileEntity(tHatch)) rEU += tHatch.maxEUInput() * tHatch.maxAmperesIn();
        return rEU;
    }
}
