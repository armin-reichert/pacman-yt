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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.FPSCounter;
import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class GameWindow extends JFrame {

	public boolean showInfo = true;

	private final GameModel game;
	private final FPSCounter fpsCounter;
	private JComponent canvas;
	private Font arcadeFont;
	private Spritesheet ss = new Spritesheet();

	public GameWindow(GameModel game, FPSCounter fpsCounter, double scale) {
		super("Pac-Man");
		this.game = game;
		this.fpsCounter = fpsCounter;
		loadFonts();
		createCanvas(scale);
		add(canvas);
		setResizable(false);
	}

	private void createCanvas(double scale) {
		canvas = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2D = (Graphics2D) g.create();
				g2D.setColor(Color.BLACK);
				g2D.fillRect(0, 0, getWidth(), getHeight());
				g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2D.scale(scale, scale);
				drawGameScene(g2D);
				g2D.dispose();
			}
		};
		Dimension size = new Dimension((int) (World.COLS * World.TS * scale), (int) (World.ROWS * World.TS * scale));
		canvas.setPreferredSize(size);
		canvas.setMinimumSize(size);
		canvas.setSize(size);
	}

	private void loadFonts() {
		try {
			arcadeFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/emulogic.ttf"));
			arcadeFont = arcadeFont.deriveFont(8.0f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int frame(int duration, int frames) {
		return (int) (game.ticks % duration) * frames / duration;
	}

	private void drawGameScene(Graphics2D g) {
		if (!game.mazeFlashing || frame(30, 2) == 0) {
			g.drawImage(ss.mazeImage, 0, 3 * World.TS, null);
		}

		g.setColor(Color.PINK);
		for (int row = 0; row < World.ROWS; ++row) {
			for (int col = 0; col < World.COLS; ++col) {
				if (game.world.isPellet(row, col)) {
					g.fillRect(col * World.TS + 3, row * World.TS + 3, 2, 2);
				} else if (game.world.isPowerPellet(row, col)) {
					if (!game.powerPelletsBlinking || frame(30, 2) == 0) {
						g.fillOval(col * World.TS, row * World.TS, World.TS, World.TS);
					}
				}
			}
		}

		if (game.bonus != -1) {
			int bonusValue = game.bonusValue(game.bonus);
			BufferedImage sprite = game.bonusEaten ? ss.bonusValues.get(bonusValue) : ss.bonusSymbols.get(game.bonus);
			g.drawImage(sprite, 14 * World.TS - sprite.getWidth() / 2, 20 * World.TS - World.HTS, null);
		}

		drawPacMan(g);
		for (Ghost ghost : game.ghosts) {
			drawGhost(g, ghost);
		}

		if (showInfo) {
			drawInfo(g);
		}

		g.setColor(Color.WHITE);
		g.setFont(arcadeFont);
		g.drawString("SCORE " + game.score, 8, 8);
		g.drawString("LEVEL " + game.levelNumber, 140, 8);

		if (game.state == GameState.READY) {
			g.setColor(Color.YELLOW);
			g.setFont(arcadeFont.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("READY!", 11 * World.TS, 21 * World.TS);
		} else if (game.state == GameState.GAME_OVER) {
			g.setColor(Color.RED);
			g.setFont(arcadeFont.deriveFont(Font.ITALIC | Font.BOLD));
			g.drawString("GAME  OVER", 9 * World.TS, 21 * World.TS);
		}

		for (int i = 0; i < game.lives; ++i) {
			g.drawImage(ss.liveCount, (1 + 2 * i) * World.TS, 34 * World.TS, null);
		}

		for (int i = 0; i < game.levelSymbols.size(); ++i) {
			int x = (World.COLS - 3) * World.TS - 2 * i * World.TS;
			int y = (World.ROWS - 2) * World.TS;
			int symbol = game.levelSymbols.get(i);
			g.drawImage(ss.bonusSymbols.get(symbol), x, y, null);
		}
	}

	private void drawInfo(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.BOLD, 8));
		String state = game.state + "";
		if (game.state == GameState.PLAYING) {
			state = game.chasingPhase ? "CHASING" : "SCATTERING";
		}
		g.drawString(fpsCounter.getFrameRate() + " FPS", 8, 16);
		if (game.paused) {
			g.drawString("(PAUSED)", 48, 16);
		}
		g.drawString(state + " " + game.stateTimer, 8, 24);
		if (game.pacSafe) {
			g.drawString("Pac-Man is safe", 144, 24);
		}
		for (Ghost ghost : game.ghosts) {
			drawGhostTarget(g, ghost);
			drawGhostState(g, ghost);
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
				game.pac.animFrame = game.pac.stuck ? 0 : frame(15, 3);
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
				ghost.animFrame = frame(10, 2);
				if (game.pac.losingPower) {
					int blink = frame(20, 2) == 0 ? 0 : 2;
					ghost.animFrame += blink;
				}
			}
			sprite = ss.ghostFrightened.get(ghost.animFrame);
		} else {
			if (ghost.animated) {
				ghost.animFrame = frame(10, 2);
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
			g.setColor(switch (ghost.id) {
			case GameModel.BLINKY -> Color.RED;
			case GameModel.PINKY -> Color.PINK;
			case GameModel.INKY -> Color.CYAN;
			case GameModel.CLYDE -> Color.ORANGE;
			default -> null;
			});
			g.drawRect(ghost.targetTile.x * World.TS + 2, ghost.targetTile.y * World.TS + 2, 4, 4);
		}
	}
}