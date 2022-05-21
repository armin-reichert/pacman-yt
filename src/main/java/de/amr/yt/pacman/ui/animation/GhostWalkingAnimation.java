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
import static de.amr.yt.pacman.ui.Sprites.stripe;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;

import de.amr.yt.pacman.lib.Animation;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.Ghost;

/**
 * @author Armin Reichert
 *
 */
public class GhostWalkingAnimation extends Animation<BufferedImage> {

	static final List<EnumMap<Direction, List<BufferedImage>>> ghostsWalkingSprites;

	static {
		EnumMap<Direction, List<BufferedImage>> redGhost = new EnumMap<>(Direction.class);
		redGhost.put(RIGHT, stripe(0, 4, 2));
		redGhost.put(LEFT, stripe(2, 4, 2));
		redGhost.put(UP, stripe(4, 4, 2));
		redGhost.put(DOWN, stripe(6, 4, 2));

		EnumMap<Direction, List<BufferedImage>> pinkGhost = new EnumMap<>(Direction.class);
		pinkGhost.put(RIGHT, stripe(0, 5, 2));
		pinkGhost.put(LEFT, stripe(2, 5, 2));
		pinkGhost.put(UP, stripe(4, 5, 2));
		pinkGhost.put(DOWN, stripe(6, 5, 2));

		EnumMap<Direction, List<BufferedImage>> cyanGhost = new EnumMap<>(Direction.class);
		cyanGhost.put(RIGHT, stripe(0, 6, 2));
		cyanGhost.put(LEFT, stripe(2, 6, 2));
		cyanGhost.put(UP, stripe(4, 6, 2));
		cyanGhost.put(DOWN, stripe(6, 6, 2));

		EnumMap<Direction, List<BufferedImage>> orangeGhost = new EnumMap<>(Direction.class);
		orangeGhost.put(RIGHT, stripe(0, 7, 2));
		orangeGhost.put(LEFT, stripe(2, 7, 2));
		orangeGhost.put(UP, stripe(4, 7, 2));
		orangeGhost.put(DOWN, stripe(6, 7, 2));

		ghostsWalkingSprites = List.of(redGhost, pinkGhost, cyanGhost, orangeGhost);
	}

	private final Ghost ghost;

	public GhostWalkingAnimation(Ghost ghost) {
		this.ghost = ghost;
		name = "ghost-walking";
		frameDuration = 8;
		loop = true;
	}

	@Override
	public int numFrames() {
		return 2;
	}

	@Override
	public BufferedImage sprite() {
		return ghostsWalkingSprites.get(ghost.id).get(ghost.moveDir).get(frameIndex);
	}
}