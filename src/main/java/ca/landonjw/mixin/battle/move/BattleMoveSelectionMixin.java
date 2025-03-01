package ca.landonjw.mixin.battle.move;

import ca.landonjw.MoveHoverRenderer;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(BattleMoveSelection.class)
public class BattleMoveSelectionMixin {

    @Shadow(remap = false) private List<BattleMoveSelection.MoveTile> moveTiles;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.moveTiles.forEach(tile -> {
            if (tile.isHovered(mouseX, mouseY)) {
                context.pose().pushPose();
                context.pose().translate(0.0F, 0.0F, 400.0F);

                var guiScale = Minecraft.getInstance().options.guiScale().get();
                var tooltipScale = getTooltipScale(guiScale);

                context.pose().scale(tooltipScale, tooltipScale, 1.0f);
                MoveHoverRenderer.INSTANCE.render(context, tile.getX() / tooltipScale, tile.getY() / tooltipScale, tile.getMoveTemplate());
                context.pose().popPose();
            }
        });
    }

    @Unique
    private float getTooltipScale(float guiScale) {
        if (guiScale == 5f) return 0.9f;
        if (guiScale == 4f) return 1f;
        if (guiScale == 3f) return 1.3f;
        if (guiScale == 2f) return 1.6f;
        if (guiScale == 1f) return 2.0f;
        return 1f;
    }
}
