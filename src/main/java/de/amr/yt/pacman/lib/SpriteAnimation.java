package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	public static byte[] nfold(int n, byte[] frames) {
		byte[] result = new byte[frames.length * n];
		for (int i = 0; i < frames.length; ++i) {
			for (int j = 0; j < n; ++j) {
				result[n * i + j] = frames[i];
			}
		}
		return result;
	}

	public final String name;
	public boolean enabled;
	private final byte[] frames;
	private final boolean cycle;
	private int index;

	public SpriteAnimation(String name, byte[] frames, boolean cycle) {
		this.name = name;
		this.frames = frames;
		this.enabled = true;
		this.cycle = cycle;
	}

	public SpriteAnimation(String name, int singleFrame, boolean cycle) {
		this(name, new byte[] { (byte) singleFrame }, cycle);
	}

	@Override
	public String toString() {
		return "%s %s".formatted(name, enabled ? "" : "disabled");
	}

	public void reset() {
		index = 0;
	}

	public int frame() {
		return frames[index];
	}

	public void advance() {
		if (enabled) {
			if (index < frames.length - 1) {
				++index;
			} else {
				index = cycle ? 0 : frames.length - 1;
			}
		}
	}
}