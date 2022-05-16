/**
 * 
 */
package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 *
 */
public class SpriteAnimation {

	public static int frame(long clock, int totalAnimationTicks, int numFrames) {
		return (int) (clock % totalAnimationTicks) * numFrames / totalAnimationTicks;
	}

	public final String name;
	public final boolean cycle;
	public boolean enabled;

	private byte[] frames;
	private int majorIndex;
	private int minorIndex;
	private int frameLength;

	public SpriteAnimation(String name, byte[] frames, int frameLength, boolean cycle) {
		this.name = name;
		this.cycle = cycle;
		this.frames = frames;
		this.frameLength = frameLength;
		this.majorIndex = 0;
		this.minorIndex = 0;
	}

	public SpriteAnimation(String name, byte[] frames, boolean cycle) {
		this(name, frames, 1, cycle);
	}

	public SpriteAnimation(String name, int singleFrame, boolean cycle) {
		this(name, new byte[] { (byte) singleFrame }, cycle);
	}

	@Override
	public String toString() {
		return "%s %s".formatted(name, enabled ? "" : "disabled");
	}

	public void reset() {
		majorIndex = 0;
		minorIndex = 0;
	}

	public int frame() {
		return frames[majorIndex];
	}

	public void advance() {
		if (!enabled) {
			return;
		}
		if (frameLength == 1) {
			advanceMajorIndex();
		} else {
			advanceMinorIndex();
		}
	}

	private void advanceMinorIndex() {
		if (minorIndex < frameLength - 1) {
			++minorIndex;
		} else {
			advanceMajorIndex();
			minorIndex = 0;
		}
	}

	private void advanceMajorIndex() {
		if (majorIndex < frames.length - 1) {
			++majorIndex;
		} else {
			majorIndex = cycle ? 0 : frames.length - 1;
		}
	}
}