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
import static de.amr.yt.pacman.ui.Renderer.drawGhost;
import static de.amr.yt.pacman.ui.Renderer.drawGhostState;
import static de.amr.yt.pacman.ui.Renderer.drawGhostTarget;
import static de.amr.yt.pacman.ui.Renderer.drawPacMan;
import static de.amr.yt.pacman.ui.Renderer.drawPacManState;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class PlayScene implements GameScene {

	private final GameModel game;

	public PlayScene(GameModel game) {
		this.game = game;
	}

	@Override
	public void init() {
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		if (!game.mazeFlashing || frame(2, 15) == 0) {
			g.drawImage(Sprites.get().mazeImage, 0, t(3), null);
		}
		g.setColor(Color.PINK);
		for (int row = 0; row < World.ROWS; ++row) {
			for (int col = 0; col < World.COLS; ++col) {
				if (game.world.isPellet(row, col)) {
					g.fillOval(t(col) + 3, t(row) + 3, 2, 2);
				} else if (game.world.isPowerPellet(row, col)) {
					if (!game.powerPelletsBlinking || frame(2, 15) == 0) {
						g.fillOval(t(col), t(row), t(1), t(1));
					}
				}
			}
		}
		if (game.bonus != -1) {
			int bonusValue = GameModel.BONUS_VALUES[game.bonus];
			BufferedImage sprite = game.bonusEaten ? Sprites.get().bonusValues.get(bonusValue)
					: Sprites.get().bonusSymbols.get(game.bonus);
			g.drawImage(sprite, t(14) - sprite.getWidth() / 2, t(20) - World.HTS, null);
		}
		drawPacMan(g, game.pacMan);
		for (Ghost ghost : game.ghosts) {
			drawGhost(g, ghost, game.pacMan.isLosingPower());
		}
		if (game.state == GameState.READY) {
			g.setColor(Color.YELLOW);
			g.setFont(Renderer.ARCADE_FONT.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("READY!", t(11), t(21));
		} else if (game.state == GameState.GAME_OVER) {
			g.setColor(Color.RED);
			g.setFont(Renderer.ARCADE_FONT.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("GAME  OVER", t(9), t(21));
		}
		int livesDisplayed = game.score == 0 && game.state == GameState.LEVEL_STARTING ? game.lives : game.lives - 1;
		for (int i = 0; i < livesDisplayed; ++i) {
			g.drawImage(Sprites.get().liveCount, t(2 + 2 * i), t(World.ROWS - 2), null);
		}
		for (int i = 0; i < game.levelCounter.size(); ++i) {
			int x = t(World.COLS - 4 - 2 * i);
			int y = t(World.ROWS - 2);
			int symbol = game.levelCounter.get(i);
			g.drawImage(Sprites.get().bonusSymbols.get(symbol), x, y, null);
		}
	}

	public void drawInfo(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
		if (game.state != GameState.INTRO) {
			drawPacManState(g, game.pacMan);
			for (Ghost ghost : game.ghosts) {
				drawGhostTarget(g, ghost);
				drawGhostState(g, ghost);
			}
		}
	}
}