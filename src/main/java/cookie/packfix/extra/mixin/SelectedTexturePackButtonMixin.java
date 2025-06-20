package cookie.packfix.extra.mixin;

import cookie.packfix.extra.RenderContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.options.components.SelectedTexturePackListComponent;
import net.minecraft.client.render.Font;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin to handle the selected texture pack font.
 */
@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.client.gui.options.components.SelectedTexturePackListComponent$TexturePackButton", remap = false)
public abstract class SelectedTexturePackButtonMixin {

	@Unique
	private final ThreadLocal<RenderContext> renderContext = new ThreadLocal<>();

	/**
	 * Inject a method into render. Here we set the renderContext.
	 * @param component The component instance
	 * @param x posX
	 * @param y posY
	 * @param width width
	 * @param mouseX MouseX
	 * @param mouseY mouseY
	 * @param ci Callback Info
	 */
	@Inject(method = "render", at = @At("HEAD"))
	private void captureRenderContext(SelectedTexturePackListComponent component, int x, int y, int width, int mouseX, int mouseY, CallbackInfo ci) {
		// For selected texture pack buttons, we don't have index/hovered params, so we use defaults
		// We could potentially calculate hovered state here if needed
		renderContext.set(new RenderContext(x, y, width, mouseX, mouseY, 0, false));
	}

	/**
	 * Inject a method into render. Here we clear the render context when closed.
	 * @param ci Callback Info
	 */
	@Inject(method = "render", at = @At("RETURN"))
	private void clearRenderContext(CallbackInfo ci) {
		renderContext.remove();
	}

	/**
	 * Redirect a method inside render. We redirect every instance of "drawString".
	 * @param font The current game font
	 * @param text The text to display
	 * @param x Text Pos X
	 * @param y Text Pos Y
	 * @param color Text Color
	 */
	@Redirect(method = "render", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/render/Font;drawString(Ljava/lang/String;III)V"))
	private void drawScaledStringAdvanced(Font font, String text, int x, int y, int color) {
		RenderContext context = renderContext.get();
		if (context == null) {
			font.drawString(text, x, y, color);
			return;
		}

		int maxWidth = calculateMaxWidthAdvanced(context, x, y, text);
		drawStringWithAutoScale(font, text, x, y, color, maxWidth);
	}

	/**
	 * A method to calculate the max width.
	 * @param context Render Context
	 * @param textX Text Pos X
	 * @param textY Text Pos Y
	 * @param text Displayed Text
	 * @return Returns a new width.
	 */
	@Unique
	private int calculateMaxWidthAdvanced(RenderContext context, int textX, int textY, String text) {
		// First, we do some width calculate from the render context.
		// We account for the icon and some padding. (32 for the icon, 2 for padding)
		// We also remove some available width from the right. (25 seemed good - Cookie (FatherCheese))
		int availableWidth = context.width - 34;
		availableWidth -= 9;

		// For selected texture packs we might need to account for the
		// remove button. Since we don't have hover info easily, we'll
		// just add the button padding by default. (20 for the button, 2 for padding)
		availableWidth -= 22;

		// Now we set a relative Y position based on the context.
		// The name (<= 5) has more width, while the descriptions have less.
		// Finally, we return the available width via a Math.max call.
		int relativeY = textY - context.y;
		if (relativeY <= 5) return Math.max(availableWidth, 120);
		return Math.max(availableWidth - 10, 110);
	}

	/**
	 * A method to draw auto-scalable strings.
	 * @param font The current game font
	 * @param text The text to display
	 * @param x Text Pos X
	 * @param y Text Pos Y
	 * @param color Text color
	 * @param maxWidth The maximum width of a string
	 */
	@Unique
	private void drawStringWithAutoScale(Font font, String text, int x, int y, int color, int maxWidth) {
		// First, we get the current string width.
		// If it's below the maximum width, we return as normal.
		int textWidth = font.getStringWidth(text);
		if (textWidth <= maxWidth) {
			font.drawString(text, x, y, color);
			return;
		}

		// Otherwise, we set a new scale float, dividing the
		// max width and the text width. After that we
		// push a new OpenGL matrix, scale/render the text,
		// and pop OpenGL to close it.
		float scale = (float) maxWidth / textWidth;
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, 0);
		GL11.glScalef(scale, scale, 1.0f);
		GL11.glTranslatef(-x, -y, 0);

		font.drawString(text, x, y, color);

		GL11.glPopMatrix();
	}
}
