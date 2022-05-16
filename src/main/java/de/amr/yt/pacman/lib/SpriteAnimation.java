package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	public static byte[] bytes(int... numbers) {
		byte[] bytes = new byte[numbers.length];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte) numbers[i];
		}
		return bytes;
	}

	public static byte[] nfold(int n, byte[] frames) {
		byte[] result = new byte[frames.length * n];
		for (int i = 0; i < frames.length; ++i) {
			for (int j = 0; j < n; ++j) {
				result[n * i + j] = frames[i];
			}
		}
		return result;
	}

	private final byte[] frames;
	private int index;
	public String name;
	public boolean enabled;
	public boolean cycle;

	public SpriteAnimation(String name, byte[] frames, boolean cycle) {
		this.name = name;
		this.frames = frames;
		this.enabled = true;
		this.cycle = cycle;
	}

	public SpriteAnimation(String name, int singleFrame, boolean cycle) {
		this.name = name;
		this.frames = new byte[] { (byte) singleFrame };
		this.enabled = true;
		this.cycle = cycle;
	}

	@Override
	public String toString() {
		return "%s %s".formatted(name, (enabled ? " enabled" : "disabled"));
	}

	public void reset() {
		index = 0;
	}

	public int frame() {
		return frames[index];
	}

	public void advance() {
		if (enabled) {
			if (index + 1 == frames.length) {
				index = cycle ? 0 : frames.length - 1;
			} else {
				index++;
			}
		}
	}
}