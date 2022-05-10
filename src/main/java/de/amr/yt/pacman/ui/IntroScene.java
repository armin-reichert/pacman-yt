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

import static de.amr.yt.pacman.controller.GameController.sec;
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;
import static de.amr.yt.pacman.model.World.t;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class IntroScene {

	private final int animStart = sec(1);
	private final GameModel game;
	private final Spritesheet ss;
	private float pacManX, blinkyX;
	private float pacManSpeed, ghostSpeed;
	private boolean chasingGhosts;

	public IntroScene(Spritesheet ss, GameModel game) {
		this.ss = ss;
		this.game = game;
		init();
	}

	private void init() {
		pacManX = t(World.COLS);
		pacManSpeed = game.playerSpeed;
		blinkyX = pacManX + t(3);
		ghostSpeed = game.playerSpeed * 1.05f;
		chasingGhosts = false;
	}

	public void draw(Graphics2D g) {
		final long t = game.stateTimer;
		if (t == 0) {
			init();
		}
		if (t >= animStart) {
			drawTitle(g);
		}
		for (int id = 0; id <= 3; ++id) {
			drawGhostInfo(g, id, animStart + sec(1 + 1.5 * id));
		}
		boolean blink = game.stateTimer >= animStart + sec(9);
		if (t >= animStart + sec(8)) {
			drawPointsAwarded(g, blink);
		}
		if (t >= animStart + sec(9)) {
			if (!chasingGhosts) {
				drawPowerPellet(g, blink);
			}
		}
		if (t >= animStart + sec(10) && t < animStart + sec(17)) {
			drawGuys(g);
		}
		if (t >= animStart + sec(17)) {
			drawPressSpaceToPlay(g);
		}
	}

	private void drawPowerPellet(Graphics2D g, boolean blink) {
		if (!blink || game.frame(30, 2) == 0) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(20) + World.HTS, t(1), t(1));
		}
	}

	private void drawPressSpaceToPlay(Graphics2D g) {
		if (game.frame(60, 2) == 0) {
			g.setColor(Color.WHITE);
			g.setFont(ss.arcadeFont);
			g.drawString("PRESS SPACE TO PLAY", t(4), t(32));
		}
	}

	private void drawPointsAwarded(Graphics2D g, boolean blink) {
		int x = t(10);
		int y = t(24);

		g.setColor(Color.PINK);
		g.fillRect(x + 3, y + 3, 2, 2);
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("10", x + 16, y + 8);
		g.setFont(ss.arcadeFont.deriveFont(6.0f));
		g.drawString("PTS", x + 40, y + 8);

		y += t(2);
		g.setColor(Color.PINK);
		if (!blink || game.frame(30, 2) == 0) {
			g.fillOval(t(10), y, 8, 8);
		}
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("50", x + 16, y + 8);
		g.setFont(ss.arcadeFont.deriveFont(6.0f));
		g.drawString("PTS", x + 40, y + 8);
	}

	private void drawGhostInfo(Graphics2D g, int id, int t) {
		int y = t(6 + 3 * id) + World.HTS;
		if (game.stateTimer >= t) {
			g.drawImage(ss.ghosts.get(id).get(Direction.RIGHT).get(0), t(3), y, null);
		}
		if (game.stateTimer >= t + sec(0.5)) {
			g.setColor(ss.ghostColor(id));
			g.setFont(ss.arcadeFont);
			g.drawString("-" + ghostCharacter(id), t(6), y + 12);
		}
		if (game.stateTimer >= t + sec(1.0)) {
			g.setColor(ss.ghostColor(id));
			g.setFont(ss.arcadeFont);
			g.drawString("\"" + ghostNickname(id) + "\"", t(17), y + 12);
		}
	}

	private void drawTitle(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("CHARACTER / NICKNAME", t(6), t(6));
	}

	private void drawGuys(Graphics2D g) {
		int ghostFrame = game.frame(10, 2);
		int y = t(20);
		Direction moveDir = chasingGhosts ? Direction.RIGHT : Direction.LEFT;
		pacManX += moveDir.vector.x * pacManSpeed;
		blinkyX += moveDir.vector.x * ghostSpeed;
		if (pacManX <= t(2)) {
			pacManSpeed = game.playerSpeedPowered;
			ghostSpeed = game.ghostSpeedFrightened;
			chasingGhosts = true;
		}
		if (chasingGhosts) {
			int hitGhost = -1;
			for (int id = 0; id <= 3; ++id) {
				float ghostX = blinkyX + id * 16;
				if (Math.abs(pacManX - ghostX) <= 8) {
					hitGhost = id;
					break;
				}
			}
			if (pacManX > blinkyX + 3 * 16) {
				hitGhost = 4;
			}
			for (int id = 0; id <= 3; ++id) {
				float ghostX = blinkyX + id * 16;
				if (id > hitGhost) {
					var ghostSprite = ss.ghostFrightened.get(ghostFrame);
					g.drawImage(ghostSprite, (int) ghostX, y, null);
				} else if (id == hitGhost) {
					var ghostSprite = ss.ghostValues.get(id == 0 ? 200 : id == 1 ? 400 : id == 2 ? 800 : 1600);
					g.drawImage(ghostSprite, (int) ghostX, y, null);
					g.drawImage(ss.pac.get(moveDir).get(game.frame(15, 3)), (int) pacManX, y, null);
				}
			}
			if (hitGhost == -1 || hitGhost == 4) {
				g.drawImage(ss.pac.get(moveDir).get(game.frame(15, 3)), (int) pacManX, y, null);
			}
		} else {
			g.drawImage(ss.pac.get(moveDir).get(game.frame(15, 3)), (int) pacManX, y, null);
			for (int id = 0; id <= 3; ++id) {
				float ghostX = blinkyX + id * 16;
				var ghostSprite = ss.ghosts.get(id).get(moveDir).get(ghostFrame);
				g.drawImage(ghostSprite, (int) ghostX, y, null);
			}
		}
	}

	private String ghostCharacter(int id) {
		return switch (id) {
		case BLINKY -> "SHADOW";
		case PINKY -> "SPEEDY";
		case INKY -> "BASHFUL";
		case CLYDE -> "POKEY";
		default -> null;
		};
	}

	private String ghostNickname(int id) {
		return switch (id) {
		case BLINKY -> "BLINKY";
		case PINKY -> "PINKY";
		case INKY -> "INKY";
		case CLYDE -> "CLYDE";
		default -> null;
		};
	}
}