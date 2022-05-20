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

package de.amr.yt.pacman.ui;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import de.amr.yt.pacman.lib.Animation;

/**
 * @author Armin Reichert
 *
 */
public abstract class SpriteAnimation implements Animation {

	public final String name;
	public final boolean loop;
	private boolean enabled;
	private byte[] frames;
	private int majorIndex;
	private int minorIndex;
	private int frameLength;

	/**
	 * Creates a sprite animation (frame pattern) where each frame takes one tick.
	 * <p>
	 * For example, <code>new SpriteAnimation("my-animation", new byte[] {0,1,2}, true)</code> creates the repeated
	 * pattern <code>0 1 2</code>.
	 * 
	 * @param name        animation name
	 * @param frames      frame indices
	 * @param frameLength length of a single frame in ticks
	 * @param loop        if the animation should repeat from start endlessly
	 */
	public SpriteAnimation(String name, byte[] frames, boolean loop) {
		this(name, frames, 1, loop);
	}

	/**
	 * Creates a sprite animation (frame pattern).
	 * <p>
	 * For example, <code>new SpriteAnimation("my-animation", new byte[] {0,1,2}, 3, true)</code> creates the repeated
	 * pattern <code>0 0 0 1 1 1 2 2 2</code>.
	 * 
	 * @param name        animation name
	 * @param frames      frame indices
	 * @param frameLength length of a single frame in ticks
	 * @param loop        if the animation should repeat from start endlessly
	 */
	public SpriteAnimation(String name, byte[] frames, int frameLength, boolean loop) {
		this.name = name;
		this.loop = loop;
		this.enabled = true;
		this.frames = Arrays.copyOf(frames, frames.length);
		this.frameLength = frameLength;
		this.majorIndex = 0;
		this.minorIndex = 0;
	}

	@Override
	public String toString() {
		return "%s %s".formatted(name, enabled ? "" : "disabled");
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void reset() {
		majorIndex = 0;
		minorIndex = 0;
	}

	@Override
	public int frame() {
		return frames[majorIndex];
	}

	@Override
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

	private void advanceMajorIndex() {
		if (majorIndex < frames.length - 1) {
			++majorIndex;
		} else {
			majorIndex = loop ? 0 : frames.length - 1;
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

	public abstract List<BufferedImage> getSprites();

	public BufferedImage sprite() {
		return getSprites().get(frame());
	}
}