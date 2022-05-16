package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	private final byte[] frames;
	public String name;
	public int index;
	public boolean enabled;
	public boolean cycle;

	public SpriteAnimation(String name, byte[] frames, boolean cycle) {
		this.name = name;
		this.frames = frames;
		this.enabled = false;
		this.cycle = cycle;
	}

	public int frame() {
		return frames[index];
	}

	public void advance() {
		if (enabled) {
			if (index + 1 == frames.length) {
				index = cycle ? 0 : frames.length;
			} else {
				index++;
			}
		}
	}
}