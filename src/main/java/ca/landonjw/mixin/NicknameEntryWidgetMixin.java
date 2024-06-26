package ca.landonjw.mixin;

import ca.landonjw.GUIHandler;
import com.cobblemon.mod.common.client.gui.summary.widgets.NicknameEntryWidget;
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetNicknamePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.UUID;

@Mixin(value = NicknameEntryWidget.class, remap = false)
public class NicknameEntryWidgetMixin {

    @Redirect(
            method = "updateNickname",
            at = @At(
                    value = "NEW",
                    target = "(Ljava/util/UUID;ZLjava/lang/String;)Lcom/cobblemon/mod/common/net/messages/server/pokemon/update/SetNicknamePacket;"
            )
    )
    private SetNicknamePacket cobblemon_ui_tweaks$updateNickname(UUID pokemonUUID, boolean isParty, String nickname) {
        var isPC = Objects.equals(GUIHandler.INSTANCE.getHoveredPokemonType(), "pc");
        return new SetNicknamePacket(pokemonUUID, !isPC, nickname);
    }

}
