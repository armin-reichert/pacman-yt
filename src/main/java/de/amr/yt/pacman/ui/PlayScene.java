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

import static de.amr.yt.pacman.model.World.t;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class PlayScene {

	private final GameModel game;
	private final Spritesheet ss;

	public PlayScene(Spritesheet ss, GameModel game) {
		this.game = game;
		this.ss = ss;
	}

	public void draw(Graphics2D g) {
		if (!game.mazeFlashing || game.frame(30, 2) == 0) {
			g.drawImage(ss.mazeImage, 0, t(3), null);
		}
		g.setColor(Color.PINK);
		for (int row = 0; row < World.ROWS; ++row) {
			for (int col = 0; col < World.COLS; ++col) {
				if (game.world.isPellet(row, col)) {
					g.fillRect(t(col) + 3, t(row) + 3, 2, 2);
				} else if (game.world.isPowerPellet(row, col)) {
					if (!game.powerPelletsBlinking || game.frame(30, 2) == 0) {
						g.fillOval(t(col), t(row), t(1), t(1));
					}
				}
			}
		}
		if (game.bonus != -1) {
			int bonusValue = game.bonusValue(game.bonus);
			BufferedImage sprite = game.bonusEaten ? ss.bonusValues.get(bonusValue) : ss.bonusSymbols.get(game.bonus);
			g.drawImage(sprite, t(14) - sprite.getWidth() / 2, t(20) - World.HTS, null);
		}
		drawPacMan(g);
		for (Ghost ghost : game.ghosts) {
			drawGhost(g, ghost);
		}
		if (game.state == GameState.READY) {
			g.setColor(Color.YELLOW);
			g.setFont(ss.arcadeFont.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("READY!", t(11), t(21));
		} else if (game.state == GameState.GAME_OVER) {
			g.setColor(Color.RED);
			g.setFont(ss.arcadeFont.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("GAME  OVER", t(9), t(21));
		}
		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(ss.liveCount, t(1 + 2 * i), t(World.ROWS - 2), null);
		}
		for (int i = 0; i < game.levelSymbols.size(); ++i) {
			int x = t(World.COLS - 3 - 2 * i);
			int y = t(World.ROWS - 2);
			int symbol = game.levelSymbols.get(i);
			g.drawImage(ss.bonusSymbols.get(symbol), x, y, null);
		}
	}

	public void drawInfo(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 8));
		if (game.state != GameState.INTRO) {
			for (Ghost ghost : game.ghosts) {
				drawGhostTarget(g, ghost);
				drawGhostState(g, ghost);
			}
		}
	}

	private void drawPacMan(Graphics2D g) {
		BufferedImage sprite = null;
		if (game.pac.dead) {
			if (game.pac.dyingAnimationCountdown > 0) {
				int frame = 10 - (10 * game.pac.dyingAnimationCountdown / game.pac.dyingAnimationDuration);
				sprite = ss.pacDeadAnimation.get(frame);
			} else if (!game.pac.animated) {
				sprite = ss.pac.get(game.pac.moveDir).get(2);
			}
		} else {
			if (game.pac.animated) {
				game.pac.animFrame = game.pac.stuck ? 0 : game.frame(15, 3);
			}
			sprite = ss.pac.get(game.pac.moveDir).get(game.pac.animFrame);
		}
		drawCreatureSprite(g, game.pac, sprite);
	}

	private void drawGhost(Graphics2D g, Ghost ghost) {
		BufferedImage sprite = null;
		if (ghost.state == GhostState.EATEN || ghost.state == GhostState.ENTERING_HOUSE) {
			sprite = ghost.eatenTimer > 0 ? ss.ghostValues.get(ghost.eatenValue) : ss.ghostEaten.get(ghost.moveDir);
		} else if (ghost.state == GhostState.FRIGHTENED) {
			if (ghost.animated) {
				ghost.animFrame = game.frame(10, 2);
				if (game.pac.losingPower) {
					int blink = game.frame(20, 2) == 0 ? 0 : 2;
					ghost.animFrame += blink;
				}
			}
			sprite = ss.ghostFrightened.get(ghost.animFrame);
		} else {
			if (ghost.animated) {
				ghost.animFrame = game.frame(10, 2);
			}
			sprite = ss.ghosts.get(ghost.id).get(ghost.moveDir).get(ghost.animFrame);
		}
		drawCreatureSprite(g, ghost, sprite);
	}

	private void drawCreatureSprite(Graphics2D g, Creature creature, BufferedImage sprite) {
		if (creature.visible && sprite != null) {
			int x = (int) creature.x - sprite.getWidth() / 2;
			int y = (int) creature.y - sprite.getHeight() / 2;
			g.drawImage(sprite, x, y, sprite.getWidth(), sprite.getHeight(), null);
		}
	}

	private void drawGhostState(Graphics2D g, Ghost ghost) {
		if (ghost.visible) {
			g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 6));
			g.setColor(Color.WHITE);
			int sw = g.getFontMetrics().stringWidth(ghost.state.name());
			g.drawString(ghost.state.name(), (int) ghost.x - sw / 2, (int) ghost.y - 8);
		}
	}

	private void drawGhostTarget(Graphics2D g, Ghost ghost) {
		if (ghost.visible && ghost.targetTile != null) {
			g.setColor(ss.ghostColor(ghost.id));
			g.drawRect(ghost.targetTile.x * World.TS + 2, ghost.targetTile.y * World.TS + 2, 4, 4);
		}
	}
}