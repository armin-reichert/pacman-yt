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
package de.amr.yt.pacman.ui.scene;

import static de.amr.yt.pacman.lib.Animation.frame;
import static de.amr.yt.pacman.lib.Logging.log;
import static de.amr.yt.pacman.model.World.t;
import static de.amr.yt.pacman.ui.render.Renderer.drawBonusSymbol;
import static de.amr.yt.pacman.ui.render.Renderer.drawBonusValue;
import static de.amr.yt.pacman.ui.render.Renderer.drawGhost;
import static de.amr.yt.pacman.ui.render.Renderer.drawGhostState;
import static de.amr.yt.pacman.ui.render.Renderer.drawGhostTargetTiles;
import static de.amr.yt.pacman.ui.render.Renderer.drawMaze;
import static de.amr.yt.pacman.ui.render.Renderer.drawPacMan;
import static de.amr.yt.pacman.ui.render.Renderer.drawPacManState;
import static de.amr.yt.pacman.ui.render.Renderer.drawScore;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.yt.pacman.controller.GameController;
import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.GameClock;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.World;
import de.amr.yt.pacman.ui.GameUI;
import de.amr.yt.pacman.ui.render.Renderer;

/**
 * @author Armin Reichert
 */
public class PlayScene implements GameScene {

	public boolean showTargetTiles = false;

	private final GameController gameController;
	private final GameModel game;

	public PlayScene(GameController gameController) {
		this.gameController = gameController;
		game = gameController.game;
	}

	@Override
	public boolean expired() {
		return false;
	}

	@Override
	public void onKeyPressed(int key) {
		switch (key) {
		case KeyEvent.VK_T -> showTargetTiles = !showTargetTiles;
		case KeyEvent.VK_SPACE -> {
			if (game.paused) {
				gameController.step(true);
			}
		}
		}
	}

	@Override
	public void init() {
		log("Initializing PlayScene at game time: %s", GameClock.get());
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D g) {
		drawScore(g, game.score, game.level.number, true);
		if (!game.mazeFlashing || frame(2, 15) == 0) {
			drawMaze(g, 0, t(3));
		}
		g.setColor(Color.PINK);
		for (int row = 0; row < World.ROWS; ++row) {
			for (int col = 0; col < World.COLS; ++col) {
				if (game.world.hasUneatenPelletAt(row, col)) {
					g.fillOval(t(col) + 3, t(row) + 3, 2, 2);
				} else if (game.world.hasUneatenPowerPelletAt(row, col)) {
					if (!game.powerPelletsBlinking || frame(2, 15) == 0) {
						g.fillOval(t(col), t(row), t(1), t(1));
					}
				}
			}
		}
		if (game.bonus != null) {
			if (game.bonus.eaten) {
				drawBonusValue(g, game.bonus.value, t(game.bonusTile.x + 1), t(game.bonusTile.y) + World.HT);
			} else {
				drawBonusSymbol(g, game.bonus.symbol, t(game.bonusTile.x) + World.TS, t(game.bonusTile.y) + World.HT);
			}
		}
		drawPacMan(g, game.pacMan);
		for (Ghost ghost : game.ghosts) {
			drawGhost(g, ghost);
		}
		if (gameController.state() == GameState.READY) {
			g.setColor(Color.YELLOW);
			g.setFont(Renderer.ARCADE_FONT.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("READY!", t(11), t(21));
		} else if (gameController.state() == GameState.GAME_OVER) {
			g.setColor(Color.RED);
			g.setFont(Renderer.ARCADE_FONT.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("GAME  OVER", t(9), t(21));
		}
		int livesDisplayed = game.score == 0 && gameController.state() == GameState.LEVEL_STARTING ? game.lives
				: game.lives - 1;
		for (int i = 0; i < livesDisplayed; ++i) {
			int centerX = t(2 + 2 * i) + World.HT;
			int centerY = t(World.ROWS - 2) + World.TS;
			Renderer.drawLifeSymbol(g, centerX, centerY);
		}
		for (int i = 0; i < game.levelCounter.size(); ++i) {
			int centerX = t(World.COLS - 4 - 2 * i) + World.HT;
			int centerY = t(World.ROWS - 2) + World.TS;
			drawBonusSymbol(g, game.levelCounter.get(i), centerX, centerY);
		}
		if (showTargetTiles) {
			drawGhostTargetTiles(g, game.ghosts);
		}
		if (GameUI.showInfo) {
			drawGuysState(g);
		}
	}

	private void drawGuysState(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
		drawPacManState(g, game.pacMan);
		for (Ghost ghost : game.ghosts) {
			drawGhostState(g, ghost);
		}
	}
}