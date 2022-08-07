package kubaworks.mixin.mixins.minecraft;

import java.util.Random;
import kubaworks.loaders.MobRecipeLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    private static final Random rnd = new Random();

    @Inject(method = "addRandomEnchantment", at = @At("HEAD"), require = 1)
    private static void addRandomEnchantmentDetector(
            Random random,
            ItemStack itemStack,
            int enchantabilityLevel,
            CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        if (random instanceof MobRecipeLoader.fakeRand) {
            ((MobRecipeLoader.fakeRand) random).randomenchantmentdetected.add(itemStack);
            ((MobRecipeLoader.fakeRand) random).enchantabilityLevel.add(enchantabilityLevel);
        }
    }

    @ModifyVariable(method = "addRandomEnchantment", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 1)
    private static Random addRandomEnchantmentModifier(Random random) {
        if (random instanceof MobRecipeLoader.fakeRand) return rnd;
        return random;
    }
}
