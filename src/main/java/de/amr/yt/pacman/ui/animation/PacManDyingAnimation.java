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

package de.amr.yt.pacman.ui.animation;

import java.awt.image.BufferedImage;
import java.util.List;

import de.amr.yt.pacman.ui.Sprites;

/**
 * @author Armin Reichert
 */
public class PacManDyingAnimation extends SpriteAnimation {

	private final List<Integer> frames = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
	private final List<BufferedImage> sprites = Sprites.get().stripe(3, 0, 11);

	public PacManDyingAnimation() {
		name = "pacman-dying";
		frameLength = 6;
		loop = false;
	}

	@Override
	public int numFrames() {
		return frames.size();
	}

	@Override
	public BufferedImage sprite() {
		return sprites.get(frames.get(majorIndex));
	}
}