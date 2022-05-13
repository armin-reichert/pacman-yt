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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.model.PacManState;

/**
 * @author Armin Reichert
 */
public class CreatureRenderer {

	private final Spritesheet ss;
	private final GameModel game;

	public CreatureRenderer(Spritesheet ss, GameModel game) {
		this.ss = ss;
		this.game = game;
	}

	public void drawPacMan(Graphics2D g, PacMan pacMan) {
		if (pacMan.state == PacManState.DEAD) {
			drawPacManDead(g, pacMan);
		} else {
			drawPacManAlive(g, pacMan);
		}
	}

	private void drawPacManDead(Graphics2D g, PacMan pacMan) {
		if (pacMan.dyingAnimationCountdown > 0) {
			int frame = 10 - (10 * pacMan.dyingAnimationCountdown / pacMan.dyingAnimationDuration);
			drawGuy(g, pacMan, ss.pacDeadAnimation.get(frame));
		} else if (!pacMan.animated) {
			drawGuy(g, pacMan, ss.pac.get(pacMan.moveDir).get(2)); // full face
		}
	}

	private void drawPacManAlive(Graphics2D g, PacMan pacMan) {
		if (pacMan.animated) {
			pacMan.animFrame = pacMan.stuck ? 0 : game.frame(Spritesheet.PACMAN_MOUTH_ANIMATION);
		}
		drawGuy(g, pacMan, ss.pac.get(pacMan.moveDir).get(pacMan.animFrame));
	}

	public void drawGhost(Graphics2D g, Ghost ghost) {
		BufferedImage sprite = null;
		if (ghost.state == GhostState.EATEN || ghost.state == GhostState.ENTERING_HOUSE) {
			sprite = ghost.eatenTimer > 0 ? ss.ghostValues.get(ghost.eatenValue) : ss.ghostEaten.get(ghost.moveDir);
		} else if (ghost.state == GhostState.FRIGHTENED
				|| ghost.state == GhostState.LOCKED && game.pacMan.powerCountdown > 0) {
			if (ghost.animated) {
				ghost.animFrame = game.frame(Spritesheet.GHOST_ANIMATION);
				if (game.pacMan.isLosingPower()) {
					int blink = game.frame(20, 2) == 0 ? 0 : 2;
					ghost.animFrame += blink;
				}
			}
			sprite = ss.ghostFrightened.get(ghost.animFrame);
		} else {
			if (ghost.animated) {
				ghost.animFrame = game.frame(Spritesheet.GHOST_ANIMATION);
			}
			sprite = ss.ghosts.get(ghost.id).get(ghost.moveDir).get(ghost.animFrame);
		}
		drawGuy(g, ghost, sprite);
	}

	public void drawGhostNormal(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			drawGuy(g, ghost, ss.ghosts.get(ghost.id).get(ghost.moveDir).get(game.frame(Spritesheet.GHOST_ANIMATION)));
		}
	}

	public void drawGhostFrightened(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			drawGuy(g, ghost, ss.ghostFrightened.get(game.frame(Spritesheet.GHOST_ANIMATION)));
		}
	}

	public void drawGhostValue(Graphics2D g, Ghost ghost, int value) {
		if (ghost.visible) {
			drawGuy(g, ghost, ss.ghostValues.get(value));
		}
	}

	public void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible && sprite != null) {
			int x = (int) guy.x - sprite.getWidth() / 2;
			int y = (int) guy.y - sprite.getHeight() / 2;
			g.drawImage(sprite, x, y, sprite.getWidth(), sprite.getHeight(), null);
		}
	}
}