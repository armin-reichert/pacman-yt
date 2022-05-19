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

import static de.amr.yt.pacman.lib.SpriteAnimation.frame;
import static de.amr.yt.pacman.model.World.t;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class Renderer {

	public static Color[] GHOST_COLORS = { Color.RED, new Color(252, 181, 255), Color.CYAN, new Color(253, 192, 90) };
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

	public static void drawPellet(Graphics2D g, int x, int y) {
		g.setColor(Color.PINK);
		g.fillOval(x + 3, y + 3, 2, 2);
	}

	public static void drawPowerPellet(Graphics2D g, int x, int y) {
		g.setColor(Color.PINK);
		g.fillOval(x, y, 8, 8);
	}

	public static void drawPacMan(Graphics2D g, PacMan pacMan) {
		if (pacMan.animation == pacMan.animDying) {
			drawGuy(g, pacMan, Sprites.get().pacDead.get(pacMan.animation.frame()));
		} else {
			drawGuy(g, pacMan, Sprites.get().pac.get(pacMan.moveDir).get(pacMan.animation.frame()));
		}
	}

	public static Color ghostColor(int id) {
		return GHOST_COLORS[id];
	}

	public static void drawGhost(Graphics2D g, Ghost ghost, boolean blinking) {
		if (ghost.state == GhostState.EATEN || ghost.state == GhostState.ENTERING_HOUSE) {
			if (ghost.valueTimer > 0) {
				drawGuy(g, ghost, Sprites.get().ghostValues.get(ghost.value));
			} else {
				drawGuy(g, ghost, Sprites.get().ghostEyes.get(ghost.moveDir));
			}
		}

		else if (ghost.animation == ghost.animFrightened) {
			int blinkingOffset = !blinking || frame(2, 10) == 0 ? 0 : 2;
			drawGuy(g, ghost, Sprites.get().ghostBlue.get(ghost.animation.frame() + blinkingOffset));
		}

		else {
			drawGuy(g, ghost, Sprites.get().ghosts.get(ghost.id).get(ghost.moveDir).get(ghost.animation.frame()));
		}
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
			text += " " + pacMan.animation;
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
			text += " " + ghost.animation;
			sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (int) ghost.x - sw / 2, (int) ghost.y + 12);
		}
	}

	public static void drawGhostTargetTiles(Graphics2D g, Ghost[] ghosts) {
		for (Ghost ghost : ghosts) {
			if (ghost.visible && ghost.targetTile != null) {
				g.setColor(ghostColor(ghost.id));
				g.fillRect(t(ghost.targetTile.x) + 2, t(ghost.targetTile.y) + 2, 4, 4);
				g.setStroke(new BasicStroke(0.5f));
				g.drawLine((int) ghost.x, (int) ghost.y, t(ghost.targetTile.x) + World.HT, t(ghost.targetTile.y) + World.HT);
			}
		}
	}
}