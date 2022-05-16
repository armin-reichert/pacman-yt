package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	private final byte[] frames;
	public int index;
	public boolean enabled;

	public SpriteAnimation(byte[] frames) {
		this.frames = frames;
	}

	public int frame() {
		return frames[index];
	}

	public void advance() {
		if (enabled) {
			index = (index + 1) % frames.length;
		}
	}
}