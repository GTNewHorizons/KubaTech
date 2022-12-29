package kubatech.api.enums;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public enum FluidList {
    FlowerExtract;
    private FluidStack stack;
    private boolean mHasNotBeenSet = true;

    public FluidList set(Fluid fluid) {
        mHasNotBeenSet = false;
        if (fluid == null) return this;
        stack = new FluidStack(fluid, 1000);
        return this;
    }

    public FluidList set(FluidStack fluid) {
        mHasNotBeenSet = false;
        if (fluid == null) return this;
        stack = fluid.copy();
        stack.amount = 1000;
        return this;
    }

    public Fluid getFluid() {
        if (mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + name() + "' has not been set to an Item at this time!");
        if (stack == null) return null;
        return stack.getFluid();
    }

    public boolean hasBeenSet() {
        return !mHasNotBeenSet;
    }

    public boolean isStackEqual(FluidStack stack) {
        return isStackEqual(stack, false);
    }

    public boolean isStackEqual(FluidStack stack, boolean ignoreNBT) {
        if (stack == null) return false;
        if (getFluid() != stack.getFluid()) return false;
        if (ignoreNBT) return true;
        return stack.tag.equals(this.stack.tag);
    }

    public FluidStack get(int amount) {
        if (mHasNotBeenSet)
            throw new IllegalAccessError("The Enum '" + name() + "' has not been set to an Item at this time!");
        FluidStack copy = stack.copy();
        copy.amount = amount;
        return copy;
    }
}
