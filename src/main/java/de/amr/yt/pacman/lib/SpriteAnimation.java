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
				index = cycle ? 0 : frames.length - 1;
			} else {
				index++;
			}
		}
	}
}