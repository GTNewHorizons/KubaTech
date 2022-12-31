package kubatech.loaders.item;

import static kubatech.kubatech.KT;

import gregtech.api.enums.GT_Values;
import gregtech.api.util.GT_Utility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kubatech.Tags;
import kubatech.loaders.ItemLoader;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FluidCell extends Item {
    public FluidCell() {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(KT);
        setUnlocalizedName("kubaitem.fluid_cell");
    }

    public static String getFluidName(ItemStack stack) {
        return variants.get(stack.getItemDamage()).getUnlocalizedName();
    }

    public static ItemStack getFluidCell(Fluid fluid) {
        return new ItemStack(ItemLoader.fluidcell, 1, lookup.get(fluid.getUnlocalizedName()));
    }

    public static final List<Fluid> variants = new ArrayList<>();
    public static final HashMap<Integer, IIcon> icons = new HashMap<>();
    private static final HashMap<String, Integer> lookup = new HashMap<>();

    public static void registerFluidCell(Fluid fluid) {
        if (variants.contains(fluid)) return;
        int index = variants.size();
        variants.add(fluid);
        icons.put(index, null);
        lookup.put(fluid.getUnlocalizedName(), index);
        ItemStack fullContainer = getFluidCell(fluid);
        ItemStack emptyContainer = gregtech.api.enums.ItemList.Cell_Empty.get(1);
        FluidStack fluidStack = new FluidStack(fluid, 1000);
        if (!FluidContainerRegistry.registerFluidContainer(fluidStack, fullContainer, emptyContainer))
            GT_Values.RA.addFluidCannerRecipe(
                    fullContainer, GT_Utility.getContainerItem(fullContainer, false), null, fluidStack);
    }

    @Override
    public String getUnlocalizedName() {
        return "kubaitem.fluid_cell";
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "kubaitem.fluid_cell";
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted(
                this.getUnlocalizedName() + ".name", StatCollector.translateToLocal(getFluidName(stack)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack stack, EntityPlayer entity, List tooltipList, boolean showDebugInfo) {
        tooltipList.add("1000L");
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
        for (int i = 0; i < variants.size(); i++) p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return gregtech.api.enums.ItemList.Cell_Empty.get(1);
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons.entrySet().forEach(e -> {
            String texturePath =
                    Tags.MODID + ":fluid_cell/" + variants.get(e.getKey()).getUnlocalizedName();
            e.setValue(iconRegister.registerIcon(texturePath));
        });
    }

    @Override
    public IIcon getIconFromDamage(int damage) {
        return icons.get(damage);
    }
}
