package ca.landonjw.mixin.pc;

import ca.landonjw.GUIHandler;
import com.cobblemon.mod.common.client.gui.pasture.PastureWidget;
import com.cobblemon.mod.common.client.gui.pc.BoxStorageSlot;
import com.cobblemon.mod.common.client.gui.pc.PartyStorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageSlot;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(StorageWidget.class)
public abstract class StorageWidgetMixin {

    @Final @Shadow(remap = false) private ArrayList<BoxStorageSlot> boxSlots;
    @Final @Shadow(remap = false) private ArrayList<PartyStorageSlot> partySlots;
    @Shadow(remap = false) private PastureWidget pastureWidget;
    @Shadow(remap = false) public abstract void setBox(int value);
    @Final @Shadow(remap = false) private ClientPC pc;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    public void cobblemon_ui_tweaks$renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GUIHandler.INSTANCE.setHoveredPokemon(null);
        this.boxSlots.forEach(slot -> tryGetHoveredPokemon(slot, mouseX, mouseY, "pc"));
        this.partySlots.forEach(slot -> tryGetHoveredPokemon(slot, mouseX, mouseY, "party"));
        if (this.pastureWidget != null) {
            this.pastureWidget.getPastureScrollList().children().forEach(slot -> {
                if (slot.isMouseOver(mouseX, mouseY)) {
                    for (ClientBox box : pc.getBoxes()) {
                        for (Pokemon pokemon : box.getSlots()) {
                            if (pokemon != null && pokemon.getUuid().equals(slot.getPokemon().getPokemonId())) {
                                GUIHandler.INSTANCE.setHoveredPokemon(pokemon);
                                GUIHandler.INSTANCE.setHoveredPokemonType("pasture");
                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    @Unique
    private void tryGetHoveredPokemon(StorageSlot slot, int mouseX, int mouseY, String slotType) {
        if (slot.isHovered(mouseX, mouseY) && slot.getPokemon() != null) {
            GUIHandler.INSTANCE.setHoveredPokemon(slot.getPokemon());
            GUIHandler.INSTANCE.setHoveredPokemonType(slotType);
        }
    }

    @Inject(method = "setBox", at = @At("TAIL"), remap = false)
    public void cobblemon_ui_tweaks$setBox(int value, CallbackInfo ci) {
        GUIHandler.INSTANCE.setLastPCBox(value);
    }

}
