package cookie.packfix.extra;

public class RenderContext {
	public final int x;
	public final int y;
	public final int width;
	public final int mouseX;
	public final int mouseY;
	public final int index;
	public final boolean hovered;

	public RenderContext(int x, int y, int width, int mouseX, int mouseY, int index, boolean hovered) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.index = index;
		this.hovered = hovered;
	}
}
