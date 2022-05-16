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
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;
import static de.amr.yt.pacman.model.World.t;

import java.awt.Color;
import java.awt.Font;
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

	public static final Font ARCADE_FONT;

	static {
		Font font;
		try {
			font = Font.createFont(Font.TRUETYPE_FONT, Renderer.class.getResourceAsStream("/emulogic.ttf")).deriveFont(8f);
		} catch (Exception e) {
			e.printStackTrace();
			font = new Font(Font.SANS_SERIF, Font.BOLD, 8);
		}
		ARCADE_FONT = font;
	}

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
			drawGuy(g, pacMan, Sprites.get().pacDeadAnimation.get(frame));
		} else if (false /* TODO */) {
			drawGuy(g, pacMan, Sprites.get().pac.get(pacMan.moveDir).get(2)); // full face
		}
	}

	public static void drawPacManAlive(Graphics2D g, PacMan pacMan) {
		if (pacMan.stuck) {
			drawGuy(g, pacMan, Sprites.get().pac.get(pacMan.moveDir).get(1));
		} else {
			drawGuy(g, pacMan, Sprites.get().pac.get(pacMan.moveDir).get(pacMan.mouthAnimation.frame()));
			pacMan.mouthAnimation.advance();
		}
	}

	public static Color ghostColor(int id) {
		return switch (id) {
		case BLINKY -> Color.RED;
		case PINKY -> new Color(252, 181, 255);
		case INKY -> Color.CYAN;
		case CLYDE -> new Color(253, 192, 90);
		default -> null;
		};
	}

	public static void drawGhost(Graphics2D g, Ghost ghost, boolean pacManHasPower, boolean pacManLosingPower) {
		// frightened look (also when locked and Pac-Man has power)
		if (ghost.state == GhostState.FRIGHTENED || ghost.state == GhostState.LOCKED && pacManHasPower) {
			drawGhostFrightened(g, ghost, pacManLosingPower);
		}

		// eaten (eyes) or eaten (value) look
		else if (ghost.state == GhostState.EATEN || ghost.state == GhostState.ENTERING_HOUSE) {
			var sprite = ghost.eatenTimer > 0 //
					? Sprites.get().ghostValues.get(ghost.eatenValue) //
					: Sprites.get().ghostEaten.get(ghost.moveDir);
			drawGuy(g, ghost, sprite);
		}

		else { // normal look
			drawGhostNormal(g, ghost);
		}
	}

	public static void drawGhostNormal(Graphics2D g, Ghost ghost) {
		drawGuy(g, ghost, Sprites.get().ghosts.get(ghost.id).get(ghost.moveDir).get(ghost.feetAnimation.frame()));
		ghost.feetAnimation.advance();
	}

	public static void drawGhostFrightened(Graphics2D g, Ghost ghost, boolean blinking) {
		int frame = ghost.feetAnimation.frame();
		if (blinking) {
			int blinkOffset = frame(20, 2) == 0 ? 0 : 2;
			frame += blinkOffset;
		}
		drawGuy(g, ghost, Sprites.get().ghostFrightened.get(frame));
		ghost.feetAnimation.advance();
	}

	public static void drawGhostValue(Graphics2D g, Ghost ghost, int value) {
		drawGuy(g, ghost, Sprites.get().ghostValues.get(value));
	}

	private static void drawGuy(Graphics2D g, Creature guy, BufferedImage sprite) {
		if (guy.visible && sprite != null) {
			int x = (int) guy.x - sprite.getWidth() / 2;
			int y = (int) guy.y - sprite.getHeight() / 2;
			g.drawImage(sprite, x, y, sprite.getWidth(), sprite.getHeight(), null);
		}
	}

	public static void drawPacManState(Graphics2D g, PacMan pacMan) {
		if (pacMan.visible) {
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 6));
			g.setColor(Color.WHITE);
			String text = pacMan.isLosingPower() ? "LOSING POWER" : pacMan.state.name();
			int sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (int) pacMan.x - sw / 2, (int) pacMan.y - 8);
			text = "(%d,%d)".formatted(pacMan.tile().x, pacMan.tile().y);
			sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (int) pacMan.x - sw / 2, (int) pacMan.y + 12);
		}
	}

	public static void drawGhostState(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 6));
			g.setColor(Color.WHITE);
			String text = ghost.state.name();
			int sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (int) ghost.x - sw / 2, (int) ghost.y - 8);
			text = "(%d,%d)".formatted(ghost.tile().x, ghost.tile().y);
			sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (int) ghost.x - sw / 2, (int) ghost.y + 12);
		}
	}

	public static void drawGhostTarget(Graphics2D g, Ghost ghost) {
		if (ghost.visible && ghost.targetTile != null) {
			g.setColor(ghostColor(ghost.id));
			g.drawRect(t(ghost.targetTile.x) + 2, t(ghost.targetTile.y) + 2, 4, 4);
		}
	}
}