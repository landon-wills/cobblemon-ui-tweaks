package ca.landonjw.mixin.pc;

import ca.landonjw.GUIHandler;
import ca.landonjw.HAHighlighterRenderer;
import ca.landonjw.JumpPCBoxWidget;
import ca.landonjw.OverridedUIRenderer;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.mixin.accessor.KeyBindingAccessor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.cobblemon.mod.common.client.gui.pc.PCGUI.*;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {

    @Shadow(remap = false) private StorageWidget storageWidget;
    @Final @Shadow(remap = false) private ClientPC pc;

    @Unique private JumpPCBoxWidget jumpPCBoxWidget;
    @Shadow(remap = false) private Pokemon previewPokemon = null;

    protected PCGUIMixin(Component component) {
        super(component);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void cobblemon_ui_tweaks$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        var summaryKey = (KeyBindingAccessor) CobblemonKeyBinds.INSTANCE.getSUMMARY();
        if (keyCode == summaryKey.boundKey().getValue()) {
            GUIHandler.INSTANCE.onSummaryPressFromPC((PCGUI)(Object)this);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount, double verticalAmount) {
        var pastureWidget = storageWidget.getPastureWidget();
        if (pastureWidget != null && pastureWidget.getPastureScrollList().isMouseOver(mouseX, mouseY)) {
            pastureWidget.getPastureScrollList().mouseScrolled(mouseX, mouseY, amount, verticalAmount);
        }
        else {
            var newBox = (storageWidget.getBox() - (int)verticalAmount) % this.pc.getBoxes().size();
            storageWidget.setBox(newBox);
        }

        // If JumpPCBoxWidget is set, unfocus the EditBox to prevent de-synced box numbers
        if (jumpPCBoxWidget != null) {
            jumpPCBoxWidget.setFocused(false);
        }

        return super.mouseScrolled(mouseX, mouseY, amount, verticalAmount);
    }

    @Inject(method = "closeNormally", at = @At("TAIL"), remap = false)
    private void cobblemon_ui_tweaks$closeNormally(CallbackInfo ci) {
        GUIHandler.INSTANCE.onPCClose();
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/gui/pc/PCGUI;playSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void cobblemon_ui_tweaks$keyPressedToClosePC(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        GUIHandler.INSTANCE.onPCClose();
    }

    // Feature added by RIze2kNight, 2024
    //Renders the PC Box Jump widget
    @Inject(method = "init", at = @At(value = "TAIL"))
    private void cobblemon_ui_tweaks$init(CallbackInfo ci) {

        var PCBox = storageWidget.getBox() + 1;

        jumpPCBoxWidget = new JumpPCBoxWidget(
                this.storageWidget,
                this.pc,
                ((width - BASE_WIDTH) / 2) + 140,
                ((height - BASE_HEIGHT) / 2) + 15,
                60,
                PC_SPACER_HEIGHT,
                Component.translatable("cobblemon.ui.pc.box.title", Component.literal(String.valueOf(PCBox)).withStyle(ChatFormatting.BOLD))
        );

        this.addRenderableWidget(jumpPCBoxWidget);
    }

    //Stop render original PC Box Title and replace with JumpPCBox + rest of code init + HA highlight renderer
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V",
                    ordinal = 12// Target the 13th Box Label occurrence (0-based index)
            ),
            cancellable = true               // Allow cancellation if necessary
    )
    private void overridePCRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        var pokemon = previewPokemon;
        var x = (width - BASE_WIDTH) / 2;
        var y = (height - BASE_HEIGHT) / 2;

        super.render(context, mouseX, mouseY, delta);

        /// Item Tooltip
        if (pokemon != null) {
            if (!pokemon.getHeldItem$common().isEmpty()) {
                int itemX = x + 3;
                int itemY = y + 98;
                boolean itemHovered =
                        (mouseX >= itemX && mouseX <= (itemX + 16)) && (mouseY >= itemY && mouseY <= (itemY + 16));
                if (itemHovered) {
                    context.renderTooltip(
                            Minecraft.getInstance().font,
                            pokemon.heldItemNoCopy$common(),
                            mouseX,
                            mouseY
                    );
                }
            }

            OverridedUIRenderer.INSTANCE.renderPC(context,x,y);
            HAHighlighterRenderer.INSTANCE.renderPC(context,x,y,pokemon);
        }

        ci.cancel();
    }
}
