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
public abstract class Animation<T> {

	public static final Animation<Object> DEFAULT = new Animation<Object>() {
		{
			name = "Missing-Animation";
			loop = false;
			enabled = true;
		}

		@Override
		public int numFrames() {
			return 0;
		}

		@Override
		public Object sprite() {
			return null;
		}
	};

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

	/**
	 * Returns an animation frame for the current game clock time.
	 * 
	 * @param numFrames  number of frames of the complete animation
	 * @param frameTicks duration of a single frame in ticks
	 * @return <code>frame(3, 4)</code> returns for example the current entry from the sequence
	 *         <code>0 0 0 0 1 1 1 1 2 2 2 2...</code>
	 */
	public static int frame(int numFrames, int frameTicks) {
		return frame(GameClock.get().getTicks(), numFrames, frameTicks);
	}

	protected String name;
	protected boolean loop;
	protected boolean enabled;
	protected int frameIndex;
	protected int tickIndex;
	protected int frameDuration;

	public Animation() {
		loop = false;
		enabled = true;
		frameDuration = 1;
	}

	public abstract int numFrames();

	public abstract T sprite();

	@Override
	public String toString() {
		return "%s%s".formatted(name, enabled ? "" : " disabled");
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void reset() {
		frameIndex = 0;
		tickIndex = 0;
	}

	public void tick() {
		if (!enabled) {
			return;
		}
		if (frameDuration == 1) {
			advanceFrameIndex();
		} else {
			advanceTickIndex();
		}
	}

	private void advanceFrameIndex() {
		if (frameIndex < numFrames() - 1) {
			++frameIndex;
		} else {
			frameIndex = loop ? 0 : numFrames() - 1;
		}
	}

	private void advanceTickIndex() {
		if (tickIndex < frameDuration - 1) {
			++tickIndex;
		} else {
			advanceFrameIndex();
			tickIndex = 0;
		}
	}
}