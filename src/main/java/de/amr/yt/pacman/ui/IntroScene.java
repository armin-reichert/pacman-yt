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

	private final GameModel game;
	private final Spritesheet ss;
	private final int animStart = sec(2);
	private float pacManX, blinkyX;
	private float pacManSpeed, ghostSpeed;
	private Direction pacManDir;
	private boolean chasing = false;

	public IntroScene(Spritesheet ss, GameModel game) {
		this.ss = ss;
		this.game = game;
		init();
	}

	private void init() {
		pacManX = t(30);
		pacManSpeed = 1f;
		blinkyX = pacManX + t(2);
		ghostSpeed = 1f;
		pacManDir = Direction.LEFT;
		chasing = false;
	}

	public void draw(Graphics2D g) {
		if (game.stateTimer == 0) {
			init();
		}

		if (game.stateTimer >= sec(1)) {
			g.setColor(Color.WHITE);
			g.setFont(ss.arcadeFont);
			g.drawString("CHARACTER / NICKNAME", t(6), t(6));
		}

		int y = t(6) + World.HTS;
		for (int id = 0; id <= 3; ++id) {
			int t = animStart + sec(2 * id);
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
			y += t(3);
		}
		boolean blink = game.stateTimer >= animStart + sec(9);
		if (game.stateTimer >= animStart + sec(8)) {
			int x = t(10);

			y = t(24);
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
		if (game.stateTimer >= animStart + sec(9) && !chasing) {
			if (!blink || game.frame(30, 2) == 0) {
				g.setColor(Color.PINK);
				g.fillOval(t(2), t(20) + World.HTS, t(1), t(1));
			}
		}
		if (game.stateTimer >= animStart + sec(10) && game.stateTimer < animStart + sec(19)) {
			drawGuys(g);
		}
		if (game.stateTimer >= animStart + sec(20)) {
			if (game.frame(60, 2) == 0) {
				g.setColor(Color.WHITE);
				g.setFont(ss.arcadeFont);
				g.drawString("PRESS SPACE TO PLAY", t(4), t(32));
			}
		}
	}

	private void drawGuys(Graphics2D g) {
		int ghostFrame = game.frame(10, 2);
		int y = t(20);
		pacManX += pacManDir.vector.x * pacManSpeed;
		blinkyX += pacManDir.vector.x * ghostSpeed;
		if (pacManX <= t(2)) {
			pacManDir = Direction.RIGHT;
			pacManSpeed = 1.0f;
			ghostSpeed = 0.5f;
			chasing = true;
		}
		if (chasing) {
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
					g.drawImage(ss.pac.get(pacManDir).get(game.frame(15, 3)), (int) pacManX, y, null);
				}
			}
			if (hitGhost == -1 || hitGhost == 4) {
				g.drawImage(ss.pac.get(pacManDir).get(game.frame(15, 3)), (int) pacManX, y, null);
			}
		} else {
			g.drawImage(ss.pac.get(pacManDir).get(game.frame(15, 3)), (int) pacManX, y, null);
			for (int id = 0; id <= 3; ++id) {
				float ghostX = blinkyX + id * 16;
				var ghostSprite = ss.ghosts.get(id).get(pacManDir).get(ghostFrame);
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