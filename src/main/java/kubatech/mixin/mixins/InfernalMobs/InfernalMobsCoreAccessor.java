package kubatech.mixin.mixins.InfernalMobs;

import java.util.ArrayList;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;

@Mixin(value = InfernalMobsCore.class, remap = false)
public interface InfernalMobsCoreAccessor {

    @Invoker
    boolean callIsClassAllowed(EntityLivingBase entity);

    @Invoker
    boolean callCheckEntityClassForced(EntityLivingBase entity);

    @Accessor
    ArrayList<ModifierLoader<?>> getModifierLoaders();

    @Accessor
    int getEliteRarity();

    @Accessor
    int getUltraRarity();

    @Accessor
    int getInfernoRarity();

    @Accessor
    int getMinEliteModifiers();

    @Accessor
    int getMinUltraModifiers();

    @Accessor
    int getMinInfernoModifiers();

    @Accessor
    ArrayList<Integer> getDimensionBlackList();

    @Accessor
    ArrayList<ItemStack> getDropIdListElite();

    @Accessor
    ArrayList<ItemStack> getDropIdListUltra();

    @Accessor
    ArrayList<ItemStack> getDropIdListInfernal();

}
