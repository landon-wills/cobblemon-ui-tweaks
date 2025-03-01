package ca.landonjw.mixin.pc;

import ca.landonjw.GUIHandler;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.mixin.accessor.KeyBindingAccessor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {
    @Shadow(remap = false) private StorageWidget storageWidget;
    @Final @Shadow(remap = false) private ClientPC pc;


    @Unique private final Logger LOGGER = LoggerFactory.getLogger("cobblemon-ui-tweaks");
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

    @Inject(method = "init", at = @At(value = "TAIL"))
    private void cobblemon_ui_tweaks$init(CallbackInfo ci) {
        this.storageWidget.setBox(GUIHandler.INSTANCE.getLastPCBox());
    }

    @ModifyVariable(
            method = "<init>(Lcom/cobblemon/mod/common/client/storage/ClientPC;Lcom/cobblemon/mod/common/client/storage/ClientParty;Lcom/cobblemon/mod/common/client/gui/pc/PCGUIConfiguration;I)V",
            at = @At("HEAD"), // Modify the variable as soon as the constructor starts
            index = 4,        // The 5th parameter (0-based: this, 1st, 2nd, 3rd, 4th)
            name = "openOnBox",
            argsOnly = true,
            remap = false
    )
    private static int fixOpenOnBox(int value) {
        return value == 0 ? GUIHandler.INSTANCE.getLastPCBox() : value;
    }
}
