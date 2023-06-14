package kubatech.mixin.mixins.minecraft;

import static kubatech.mixin.MixinsVariablesHelper.currentlyTranslating;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

@Mixin(value = LanguageRegistry.class)
public class LanguageRegistryMixin {

    @Inject(method = "loadLanguagesFor", at = @At(value = "HEAD"), remap = false)
    private void loadLanguagesForHEAD(ModContainer container, Side side, CallbackInfo callbackInfo) {
        currentlyTranslating = container.getModId();
    }

    @Inject(method = "loadLanguagesFor", at = @At(value = "RETURN"), remap = false)
    private void loadLanguagesForRETURN(ModContainer container, Side side, CallbackInfo callbackInfo) {
        currentlyTranslating = null;
    }
}
