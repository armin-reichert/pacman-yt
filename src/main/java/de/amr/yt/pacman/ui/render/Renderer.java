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
package de.amr.yt.pacman.ui.render;

import static de.amr.yt.pacman.model.World.t;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class Renderer {

	public static final Color[] GHOST_COLORS = { //
			Color.RED, new Color(252, 181, 255), Color.CYAN, new Color(253, 192, 90) };

	public static final Font ARCADE_FONT = loadTrueTypeFont("/emulogic.ttf", 8);

	public static Font loadTrueTypeFont(String path, float size) {
		try {
			return Font.createFont(Font.TRUETYPE_FONT, Renderer.class.getResourceAsStream(path)).deriveFont(size);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public static void drawMaze(Graphics2D g, int x, int y) {
		g.drawImage(Sprites.mazeImage, 0, t(3), null);
	}

	public static void drawBonusValue(Graphics2D g, int value, int centerX, int centerY) {
		BufferedImage sprite = Sprites.bonusValues.get(value);
		g.drawImage(sprite, centerX - sprite.getWidth() / 2, centerY - sprite.getHeight() / 2, null);
	}

	public static void drawBonusSymbol(Graphics2D g, int symbol, int centerX, int centerY) {
		BufferedImage sprite = Sprites.bonusSymbols.get(symbol);
		g.drawImage(sprite, centerX - sprite.getWidth() / 2, centerY - sprite.getHeight() / 2, null);
	}

	public static void drawLifeSymbol(Graphics2D g, int centerX, int centerY) {
		BufferedImage sprite = Sprites.liveCount;
		g.drawImage(sprite, centerX - sprite.getWidth() / 2, centerY - sprite.getHeight() / 2, null);
	}

	public static void drawScore(Graphics2D g, int score, int levelNumber, boolean contentVisible) {
		g.setColor(Color.WHITE);
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("LEVEL", t(18), t(1));
		if (contentVisible) {
			g.drawString("%07d".formatted(score), t(7), t(1));
			g.drawString("%03d".formatted(levelNumber), t(24), t(1));
		}
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
		drawGuy(g, pacMan, pacMan.animations.selected().sprite());
	}

	public static Color ghostColor(int id) {
		return GHOST_COLORS[id];
	}

	public static void drawGhost(Graphics2D g, Ghost ghost) {
		drawGuy(g, ghost, ghost.animations.selected().sprite());
	}

	public static void drawGuy(Graphics2D g, Creature guy, Object spriteObject) {
		BufferedImage sprite = (BufferedImage) spriteObject;
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

			int offset = 0;

			String textAbove = pacMan.isLosingPower() ? "LOSING POWER" : pacMan.state.name();
			textAbove += " %.2f px/tick".formatted(pacMan.currentSpeed());
			offset = -g.getFontMetrics().stringWidth(textAbove) / 2;
			g.drawString(textAbove, (int) pacMan.x + offset, (int) pacMan.y - 8);

			String textBelow = "(%d,%d)".formatted(pacMan.tile().x, pacMan.tile().y);
			textBelow += " " + pacMan.animations.selected();
			offset = -g.getFontMetrics().stringWidth(textBelow) / 2;
			g.drawString(textBelow, (int) pacMan.x + offset, (int) pacMan.y + 12);
		}
	}

	public static void drawGhostState(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 6));
			g.setColor(Color.WHITE);

			int offset = 0;

			String textAbove = ghost.state.name();
			textAbove += ghost.elroyState > 0 ? " Elroy %d".formatted(ghost.elroyState) : "";
			textAbove += " %.2f px/tick".formatted(ghost.currentSpeed());
			offset = -g.getFontMetrics().stringWidth(textAbove) / 2;
			g.drawString(textAbove, (int) ghost.x + offset, (int) ghost.y - 8);

			String textBelow = "(%d,%d)".formatted(ghost.tile().x, ghost.tile().y);
			textBelow += " " + ghost.animations.selected();
			offset = -g.getFontMetrics().stringWidth(textBelow) / 2;
			g.drawString(textBelow, (int) ghost.x + offset, (int) ghost.y + 12);
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