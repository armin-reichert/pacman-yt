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

import static de.amr.yt.pacman.lib.Direction.DOWN;
import static de.amr.yt.pacman.lib.Direction.LEFT;
import static de.amr.yt.pacman.lib.Direction.RIGHT;
import static de.amr.yt.pacman.lib.Direction.UP;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.ui.Sprites;

public class PacManWalkingAnimation extends SpriteAnimation {

	private final List<Integer> frames = List.of(1, 0, 1, 2);
	private final EnumMap<Direction, List<BufferedImage>> pacWalkingSprites = new EnumMap<>(Direction.class);

	private final PacMan pacMan;

	public PacManWalkingAnimation(PacMan pacMan) {
		this.pacMan = pacMan;
		Sprites spr = Sprites.get();
		pacWalkingSprites.put(RIGHT, List.of(spr.s(0, 0), spr.s(1, 0), spr.s(2, 0)));
		pacWalkingSprites.put(LEFT, List.of(spr.s(0, 1), spr.s(1, 1), spr.s(2, 0)));
		pacWalkingSprites.put(UP, List.of(spr.s(0, 2), spr.s(1, 2), spr.s(2, 0)));
		pacWalkingSprites.put(DOWN, List.of(spr.s(0, 3), spr.s(1, 3), spr.s(2, 0)));
		name = "pacman-walking";
		frameLength = 2;
		loop = true;
	}

	@Override
	public List<Integer> getFrames() {
		return frames;
	}

	@Override
	public List<BufferedImage> getSprites() {
		return pacWalkingSprites.get(pacMan.moveDir);
	}
}