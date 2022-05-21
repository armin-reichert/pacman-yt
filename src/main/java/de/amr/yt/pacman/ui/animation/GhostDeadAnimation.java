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

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.ui.Sprites;

/**
 * @author Armin Reichert
 *
 */
public class GhostDeadAnimation extends SpriteAnimation {

	private EnumMap<Direction, BufferedImage> ghostEyes = new EnumMap<>(Direction.class);
	private final Ghost ghost;

	public GhostDeadAnimation(Ghost ghost) {
		this.ghost = ghost;
		name = "ghost-dead";
		frameDuration = 1;
		loop = false;

		Sprites spr = Sprites.get();
		ghostEyes.put(RIGHT, spr.s(8, 5));
		ghostEyes.put(LEFT, spr.s(9, 5));
		ghostEyes.put(UP, spr.s(10, 5));
		ghostEyes.put(DOWN, spr.s(11, 5));
	}

	@Override
	public int numFrames() {
		return 1;
	}

	@Override
	public BufferedImage sprite() {
		return ghostEyes.get(ghost.moveDir);
	}
}