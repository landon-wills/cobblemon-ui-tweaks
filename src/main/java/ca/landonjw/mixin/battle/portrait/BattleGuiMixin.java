package ca.landonjw.mixin.battle.portrait;

import ca.landonjw.BattlePortraitHoverRenderer;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BattleGUI.class)
public class BattleGuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BattlePortraitHoverRenderer.INSTANCE.render(context, mouseX, mouseY);
    }

}
