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
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.ui.Sprites;

/**
 * @author Armin Reichert
 *
 */
public class GhostWalkingAnimation extends SpriteAnimation {

	private final Ghost ghost;
	private final List<EnumMap<Direction, List<BufferedImage>>> ghostsWalkingSprites;
	private final List<Integer> frames = List.of(0, 1);

	public GhostWalkingAnimation(Ghost ghost) {
		this.ghost = ghost;
		name = "ghost-walking";
		frameLength = 8;
		loop = true;

		Sprites spr = Sprites.get();
		EnumMap<Direction, List<BufferedImage>> redGhost = new EnumMap<>(Direction.class);
		redGhost.put(RIGHT, spr.stripe(0, 4, 2));
		redGhost.put(LEFT, spr.stripe(2, 4, 2));
		redGhost.put(UP, spr.stripe(4, 4, 2));
		redGhost.put(DOWN, spr.stripe(6, 4, 2));

		EnumMap<Direction, List<BufferedImage>> pinkGhost = new EnumMap<>(Direction.class);
		pinkGhost.put(RIGHT, spr.stripe(0, 5, 2));
		pinkGhost.put(LEFT, spr.stripe(2, 5, 2));
		pinkGhost.put(UP, spr.stripe(4, 5, 2));
		pinkGhost.put(DOWN, spr.stripe(6, 5, 2));

		EnumMap<Direction, List<BufferedImage>> cyanGhost = new EnumMap<>(Direction.class);
		cyanGhost.put(RIGHT, spr.stripe(0, 6, 2));
		cyanGhost.put(LEFT, spr.stripe(2, 6, 2));
		cyanGhost.put(UP, spr.stripe(4, 6, 2));
		cyanGhost.put(DOWN, spr.stripe(6, 6, 2));

		EnumMap<Direction, List<BufferedImage>> orangeGhost = new EnumMap<>(Direction.class);
		orangeGhost.put(RIGHT, spr.stripe(0, 7, 2));
		orangeGhost.put(LEFT, spr.stripe(2, 7, 2));
		orangeGhost.put(UP, spr.stripe(4, 7, 2));
		orangeGhost.put(DOWN, spr.stripe(6, 7, 2));

		ghostsWalkingSprites = List.of(redGhost, pinkGhost, cyanGhost, orangeGhost);
	}

	@Override
	public List<Integer> getFrames() {
		return frames;
	}

	@Override
	public List<BufferedImage> getSprites() {
		return ghostsWalkingSprites.get(ghost.id).get(ghost.moveDir);
	}
}