package ca.landonjw.mixin;

import ca.landonjw.CobblemonUITweaks;
import ca.landonjw.GUIHandler;
import ca.landonjw.ResizeableTextQueue;
import com.cobblemon.mod.common.api.gui.GuiUtilsKt;
import com.cobblemon.mod.common.client.CobblemonResources;
import com.cobblemon.mod.common.client.battle.ClientBattleMessageQueue;
import com.cobblemon.mod.common.client.gui.battle.widgets.BattleMessagePane;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BattleMessagePane.class)
public abstract class BattleMessagePaneMixin extends AbstractSelectionList {

    @Shadow(remap = false) @Final public static int TEXT_BOX_WIDTH;
    @Shadow(remap = false) private static boolean expanded;
    @Shadow(remap = false) @Final public static int TEXT_BOX_HEIGHT;

    @Shadow(remap = false) protected abstract void correctSize();

    @Shadow(remap = false) private float opacity;

    @Shadow public abstract double getScrollAmount();

    @Shadow public abstract int addEntry(Entry par1);

    @Shadow(remap = false) protected abstract int addEntry(@NotNull BattleMessagePane.BattleMessageLine entry);

    @Shadow(remap = false) protected abstract void updateScrollingState(double mouseX, double mouseY);

    @Shadow private boolean scrolling;
    @Shadow(remap = false) @Final public static int EXPAND_TOGGLE_SIZE;
    @Unique private final List<Component> battleMessages = new ArrayList<>();

    private int x = minecraft.getWindow().getGuiScaledWidth() - (getFrameWidth() + 12);
    private int y = minecraft.getWindow().getGuiScaledHeight() - (30 + (expanded ? 101 : 55));

    @Unique private final ResourceLocation battlePaneL = new ResourceLocation(CobblemonUITweaks.MODID, "textures/battle/battle_log_left.png");
    @Unique private final ResourceLocation battlePaneM = new ResourceLocation(CobblemonUITweaks.MODID, "textures/battle/battle_log_middle.png");
    @Unique private final ResourceLocation battlePaneR = new ResourceLocation(CobblemonUITweaks.MODID, "textures/battle/battle_log_right.png");
    @Unique private final ResourceLocation expandedBattlePaneL = new ResourceLocation(CobblemonUITweaks.MODID, "textures/battle/expanded_battle_log_left.png");
    @Unique private final ResourceLocation expandedBattlePaneM = new ResourceLocation(CobblemonUITweaks.MODID, "textures/battle/expanded_battle_log_middle.png");
    @Unique private final ResourceLocation expandedBattlePaneR = new ResourceLocation(CobblemonUITweaks.MODID, "textures/battle/expanded_battle_log_right.png");

    public BattleMessagePaneMixin(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/battle/ClientBattleMessageQueue;subscribe(Lkotlin/jvm/functions/Function1;)V"), remap = false)
    private void cobblemon_ui_tweaks$init(ClientBattleMessageQueue instance, Function1<? super FormattedCharSequence, Unit> $i$f$forEach) {
        var queueWithBattleMessages = (ResizeableTextQueue)(Object)instance;
        queueWithBattleMessages.cobblemon_ui_tweaks$subscribe(text -> {
            battleMessages.add(text);
            var isFullyScrolled = getMaxScroll() - getScrollAmount() < 10;
            if (isFullyScrolled) {
                setScrollAmount(getMaxScroll());
            }
            cobblemon_ui_tweaks$correctBattleText();
        });
    }

    @Inject(method = "correctSize", at = @At("HEAD"), remap = false, cancellable = true)
    private void cobblemon_ui_tweaks$correctSize(CallbackInfo ci) {
        var textboxHeight = expanded ? TEXT_BOX_HEIGHT * 2 : TEXT_BOX_HEIGHT;
        updateSize(getWidthOverride(), textboxHeight, y + 6, y + 6 + textboxHeight);
        setLeftPos(x);
        ci.cancel();
    }

    @Unique
    private void cobblemon_ui_tweaks$correctBattleText() {
        this.clearEntries();
        var textRenderer = Minecraft.getInstance().font;
        for (var message : battleMessages) {
            var line = message.copy().setStyle(message.getStyle().withBold(true).withFont(CobblemonResources.INSTANCE.getDEFAULT_LARGE()));
            var wrappedLines = textRenderer.getSplitter().splitLines(line, getWidthOverride() - 11, line.getStyle());
            var lines = Language.getInstance().getVisualOrder(wrappedLines);
            for (var finalLine : lines) {
                this.addEntry(new BattleMessagePane.BattleMessageLine((BattleMessagePane)(Object)this, finalLine));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var toggleOffsetY = expanded ? 92 : 46;
        if (mouseX > (x0 + getFrameWidth() - 9) && mouseX < (x0 + getFrameWidth() - 9 + EXPAND_TOGGLE_SIZE) && mouseY > (y + toggleOffsetY) && mouseY < (y + toggleOffsetY + EXPAND_TOGGLE_SIZE)) {
            expanded = !expanded;
        }

        updateScrollingState(mouseX, mouseY);
        if (scrolling) {
            setFocused(getEntryAtPosition(mouseX, mouseY));
            setDragging(true);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Unique
    private int getWidthOverride() {
        return GUIHandler.INSTANCE.getBattleLogWidthOverride();
    }

    @Unique
    private int getFrameWidth() {
        return getWidthOverride() + 16;
    }

    @Unique
    private void setWidthOverride(int width) {
        GUIHandler.INSTANCE.setBattleLogWidthOverride(width);
    }

    @Inject(method = "mouseDragged", at = @At("TAIL"))
    private void cobblemon_ui_tweaks$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (tryMove(mouseX, mouseY, deltaX, deltaY)) return;
        tryAdjustWidth(mouseX, mouseY, button, deltaX);
    }

    private boolean tryMove(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (mouseY - deltaY < y0 - 20 || mouseY - deltaY > y0 + 20) return false;
        if (mouseX - deltaX < this.x0 - 10 || mouseX - deltaX > this.x1 + 10) return false;
        x = (int)mouseX;
        y = (int)mouseY;
        return true;
    }

    @Unique
    private boolean tryAdjustWidth(double mouseX, double mouseY, int button, double deltaX) {
        if (button == 1) return false;
        if (mouseY < this.y0 || mouseY > this.y1) return false;
        if (mouseX - deltaX < this.x0 - 10 || mouseX - deltaX > this.x0 + 10) return false;
        var endX = minecraft.getWindow().getGuiScaledWidth() - 28;
        setWidthOverride((int)Math.max(endX - mouseX, TEXT_BOX_WIDTH));
        cobblemon_ui_tweaks$correctBattleText();
        return true;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cobblemon_ui_tweaks$render(GuiGraphics context, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        correctSize();

        GuiUtilsKt.blitk(
                context.pose(),
                expanded ? expandedBattlePaneR : battlePaneR,
                x0 + getFrameWidth() - 12,
                y,
                expanded ? 101 : 55,
                12,
                0,
                0,
                12,
                expanded ? 101 : 55,
                0,
                1,
                1,
                1,
                opacity,
                true,
                1f
        );

        GuiUtilsKt.blitk(
                context.pose(),
                expanded ? expandedBattlePaneM : battlePaneM,
                x0 + 6,
                y,
                expanded ? 101 : 55,
                getFrameWidth() - 18,
                0,
                0,
                getFrameWidth() - 18,
                expanded ? 101 : 55,
                0,
                1,
                1,
                1,
                opacity,
                true,
                1f
        );

        GuiUtilsKt.blitk(
                context.pose(),
                expanded ? expandedBattlePaneL : battlePaneL,
                x0,
                y,
                expanded ? 101 : 55,
                6,
                0,
                0,
                6,
                expanded ? 101 : 55,
                0,
                1,
                1,
                1,
                opacity,
                true,
                1f
        );

        var textboxHeight = expanded ? 46 * 2 : 46;
        context.enableScissor(
                x0 + 5,
                y + 6,
                x0 + 5 + getWidthOverride(),
                y + 6 + textboxHeight
        );
        super.render(context, mouseX, mouseY, partialTicks);
        context.disableScissor();
        ci.cancel();
    }

    @Override
    public int getRowWidth() {
        return getWidthOverride();
    }

    @Override
    public int getRowLeft() {
        return x0 + 40;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x0 + getWidthOverride();
    }

}