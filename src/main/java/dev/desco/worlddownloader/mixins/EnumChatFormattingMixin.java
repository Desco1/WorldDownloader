package dev.desco.worlddownloader.mixins;

import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnumChatFormatting.class)
public class EnumChatFormattingMixin {

    @Inject(method = "func_175744_a", at = @At(value = "RETURN", ordinal = 2), cancellable = true)
    private static void doNotReturnNull(int in, CallbackInfoReturnable<EnumChatFormatting> cir) {
        // Another brilliant gambit by Mojang. Well played, sir.
        cir.setReturnValue(EnumChatFormatting.RESET);
    }
}
