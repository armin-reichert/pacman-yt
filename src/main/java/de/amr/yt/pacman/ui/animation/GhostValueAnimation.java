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

import static de.amr.yt.pacman.ui.Sprites.s;

import java.awt.image.BufferedImage;
import java.util.Map;

import de.amr.yt.pacman.lib.Animation;
import de.amr.yt.pacman.model.Ghost;

/**
 * @author Armin Reichert
 *
 */
public class GhostValueAnimation extends Animation<BufferedImage> {

	static final Map<Integer, BufferedImage> sprites = Map.of( //
			200, s(0, 8), 400, s(1, 8), 800, s(2, 8), 1600, s(3, 8));

	private final Ghost ghost;

	public GhostValueAnimation(Ghost ghost) {
		this.ghost = ghost;
		name = "ghost-value";
	}

	@Override
	public int numFrames() {
		return 1;
	}

	@Override
	public BufferedImage sprite() {
		return sprites.get(ghost.value);
	}
}