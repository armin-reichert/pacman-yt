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
public interface Animation {

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

	public boolean isEnabled();

	public void setEnabled(boolean enabled);

	public void reset();

	public int frame();

	public void advance();
}