package com.luneruniverse.minecraft.mod.nbteditor.mixin.toggled;

import java.util.List;

import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.luneruniverse.minecraft.mod.nbteditor.misc.MixinLink;
import com.luneruniverse.minecraft.mod.nbteditor.multiversion.MVMisc;
import com.luneruniverse.minecraft.mod.nbteditor.screens.ConfigScreen;
import com.luneruniverse.minecraft.mod.nbteditor.util.MainUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(Screen.class)
public class ScreenMixin {
	@Inject(method = "method_32633", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = At.Shift.AFTER), remap = false)
	private void renderTooltipFromComponents(MatrixStack matrices, List<TooltipComponent> tooltip, int x, int y, TooltipPositioner positioner, CallbackInfo info) {
		if (!ConfigScreen.isTooltipOverflowFix())
			return;
		
		int[] size = MixinLink.getTooltipSize(tooltip);
		Vector2ic pos = MVMisc.getPosition(positioner, MainUtil.client.currentScreen, x, y, size[0], size[1]);
		int screenWidth = MainUtil.client.currentScreen.width;
		int screenHeight = MainUtil.client.currentScreen.height;
		
		MixinLink.renderTooltipFromComponents(matrices, pos.x(), pos.y(), size[0], size[1], screenWidth, screenHeight);
	}
}
