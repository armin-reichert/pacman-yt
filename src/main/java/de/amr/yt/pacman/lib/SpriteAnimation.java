/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 */
public class SpriteAnimation {

	/**
	 * Returns an animation frame for the current game clock time.
	 * 
	 * @param numFrames  number of frames of the complete animation
	 * @param frameTicks duration of a single frame in ticks
	 * @return <code>frame(3, 4)</code> returns for example the current entry from the sequence
	 *         <code>0 0 0 0 1 1 1 1 2 2 2 2...</code>
	 */
	public static int frame(int numFrames, int frameTicks) {
		return frame(GameClock.get().ticks, numFrames, frameTicks);
	}

	/**
	 * Returns an animation frame for the given time (tick).
	 * 
	 * @param numFrames  number of frames of the complete animation
	 * @param frameTicks duration of a single frame in ticks
	 * @return <code>frame(3, 4)</code> returns for example the current entry from the sequence
	 *         <code>0 0 0 0 1 1 1 1 2 2 2 2...</code>
	 */
	public static int frame(long time, int numFrames, int frameTicks) {
		int animationLength = numFrames * frameTicks;
		return (int) (time % animationLength) / frameTicks;
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
		this.enabled = true;
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