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

import static de.amr.yt.pacman.controller.GameController.frame;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.model.PacManState;

/**
 * @author Armin Reichert
 */
public class Renderer {

	public static void drawPacMan(Graphics2D g, PacMan pacMan) {
		if (pacMan.state == PacManState.DEAD) {
			drawPacManDead(g, pacMan);
		} else {
			drawPacManAlive(g, pacMan);
		}
	}

	public static void drawPacManDead(Graphics2D g, PacMan pacMan) {
		if (pacMan.dyingAnimationCountdown > 0) {
			int frame = 10 - (10 * pacMan.dyingAnimationCountdown / pacMan.dyingAnimationDuration);
			drawGuy(g, pacMan, Spritesheet.get().pacDeadAnimation.get(frame));
		} else if (!pacMan.animated) {
			drawGuy(g, pacMan, Spritesheet.get().pac.get(pacMan.moveDir).get(2)); // full face
		}
	}

	public static void drawPacManAlive(Graphics2D g, PacMan pacMan) {
		if (pacMan.animated) {
			pacMan.animFrame = pacMan.stuck //
					? 1 // half open mouth
					: frame(Spritesheet.PACMAN_MOUTH_ANIMATION);
		}
		drawGuy(g, pacMan, Spritesheet.get().pac.get(pacMan.moveDir).get(pacMan.animFrame));
	}

	public static void drawGhost(Graphics2D g, Ghost ghost, boolean pacManHasPower, boolean pacManLosingPower) {
		// frightened look (also when locked and Pac-Man has power)
		if (ghost.state == GhostState.FRIGHTENED || ghost.state == GhostState.LOCKED && pacManHasPower) {
			if (ghost.animated) {
				ghost.animFrame = frame(Spritesheet.GHOST_ANIMATION);
				if (pacManLosingPower) {
					int blinkOffset = frame(20, 2) == 0 ? 0 : 2;
					ghost.animFrame += blinkOffset;
				}
			}
			drawGuy(g, ghost, Spritesheet.get().ghostFrightened.get(ghost.animFrame));
		}

		// eaten (eyes) or eaten (value) look
		else if (ghost.state == GhostState.EATEN || ghost.state == GhostState.ENTERING_HOUSE) {
			var sprite = ghost.eatenTimer > 0 //
					? Spritesheet.get().ghostValues.get(ghost.eatenValue) //
					: Spritesheet.get().ghostEaten.get(ghost.moveDir);
			drawGuy(g, ghost, sprite);
		}

		else { // normal look
			if (ghost.animated) {
				ghost.animFrame = frame(Spritesheet.GHOST_ANIMATION);
			}
			drawGuy(g, ghost, Spritesheet.get().ghosts.get(ghost.id).get(ghost.moveDir).get(ghost.animFrame));
		}
	}

	public static void drawGhostNormal(Graphics2D g, Ghost ghost) {
		drawGuy(g, ghost,
				Spritesheet.get().ghosts.get(ghost.id).get(ghost.moveDir).get(frame(Spritesheet.GHOST_ANIMATION)));
	}

	public static void drawGhostFrightened(Graphics2D g, Ghost ghost) {
		drawGuy(g, ghost, Spritesheet.get().ghostFrightened.get(frame(Spritesheet.GHOST_ANIMATION)));
	}

	public static void drawGhostValue(Graphics2D g, Ghost ghost, int value) {
		drawGuy(g, ghost, Spritesheet.get().ghostValues.get(value));
	}

	private static void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible && sprite != null) {
			int x = (int) guy.x - sprite.getWidth() / 2;
			int y = (int) guy.y - sprite.getHeight() / 2;
			g.drawImage(sprite, x, y, sprite.getWidth(), sprite.getHeight(), null);
		}
	}
}