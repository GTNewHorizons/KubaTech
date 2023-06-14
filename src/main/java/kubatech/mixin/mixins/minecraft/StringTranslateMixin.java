package kubatech.mixin.mixins.minecraft;

import static kubatech.mixin.MixinsVariablesHelper.currentlyTranslating;

import java.lang.reflect.Field;
import java.util.regex.Matcher;

import net.minecraft.util.StringTranslate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import kubatech.Tags;

@Mixin(value = StringTranslate.class)
public class StringTranslateMixin {

    private static Field matcherTextField = null;

    @Redirect(
        method = "parseLangFile",
        at = @At(value = "INVOKE", target = "Ljava/util/regex/Matcher;replaceAll(S)S", remap = false),
        remap = false)
    private static String replaceAll(Matcher matcher, String replace) {
        if (currentlyTranslating != null && currentlyTranslating.equals(Tags.MODID)) {
            try {
                if (matcherTextField == null) {
                    matcherTextField = Matcher.class.getDeclaredField("text");
                    matcherTextField.setAccessible(true);
                }
                return (String) matcherTextField.get(matcher);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return matcher.replaceAll(replace);
    }
}
