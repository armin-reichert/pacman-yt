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
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class IntroScene {

	private static String character(int id) {
		return switch (id) {
		case BLINKY -> "SHADOW";
		case PINKY -> "SPEEDY";
		case INKY -> "BASHFUL";
		case CLYDE -> "POKEY";
		default -> null;
		};
	}

	private static String nickname(int id) {
		return switch (id) {
		case BLINKY -> "BLINKY";
		case PINKY -> "PINKY";
		case INKY -> "INKY";
		case CLYDE -> "CLYDE";
		default -> null;
		};
	}

	private final GameModel game;
	private final Spritesheet ss;

	private final PacMan pacMan;
	private final Ghost[] ghosts;
	private boolean pacManChasingGhosts;
	private boolean powerPelletsBlinking;
	private boolean powerPelletVisible;
	private int hit;

	public IntroScene(Spritesheet ss, GameModel game) {
		this.ss = ss;
		this.game = game;
		pacMan = new PacMan(game);
		ghosts = new Ghost[] { new Ghost(game, 0), new Ghost(game, 1), new Ghost(game, 2), new Ghost(game, 3) };
	}

	private void at(long point, Runnable action) {
		if (game.stateTimer == point) {
			action.run();
		}
	}

	private void from(long start, Runnable action) {
		if (game.stateTimer >= start) {
			action.run();
		}
	}

	private void between(long start, long end, Runnable action) {
		if (start <= game.stateTimer && game.stateTimer <= end) {
			action.run();
		}
	}

	public void init() {
		powerPelletVisible = true;
		powerPelletsBlinking = false;
		pacMan.x = t(World.COLS);
		pacMan.y = t(20);
		pacMan.speed = game.playerSpeed;
		pacMan.moveDir = Direction.LEFT;
		for (Ghost ghost : ghosts) {
			ghost.x = pacMan.x + t(3) + ghost.id * 16;
			ghost.y = pacMan.y;
			ghost.speed = game.playerSpeed * 1.05f;
			ghost.moveDir = Direction.LEFT;
		}
		pacManChasingGhosts = false;
		hit = -1;
	}

	public void update() {
		if (game.stateTimer == 0) {
			init(); // TODO init() should be called by scene manager (GameWindow) when scene is exchanged
		}
		between(sec(12), sec(19), () -> updateGuys());
	}

	public void draw(Graphics2D g) {
		from(sec(1.0), () -> drawHeading(g));
		from(sec(2.0), () -> drawGhostImage(g, 0));
		from(sec(2.5), () -> drawGhostCharacter(g, 0));
		from(sec(3.0), () -> drawGhostNickname(g, 0));
		from(sec(3.5), () -> drawGhostImage(g, 1));
		from(sec(4.0), () -> drawGhostCharacter(g, 1));
		from(sec(4.5), () -> drawGhostNickname(g, 1));
		from(sec(5.0), () -> drawGhostImage(g, 2));
		from(sec(5.5), () -> drawGhostCharacter(g, 2));
		from(sec(6.0), () -> drawGhostNickname(g, 2));
		from(sec(6.5), () -> drawGhostImage(g, 3));
		from(sec(7.0), () -> drawGhostCharacter(g, 3));
		from(sec(7.5), () -> drawGhostNickname(g, 3));
		from(sec(9.0), () -> drawPointsAwarded(g));
		from(sec(10.0), () -> drawPowerPellet(g));
		at(sec(11), () -> powerPelletsBlinking = true);
		between(sec(12), sec(19), () -> drawGuys(g));
		from(sec(19), () -> drawPressSpaceToPlay(g));
	}

	private void updateGuys() {
		if (pacMan.x <= t(2)) { // finds power pellet
			powerPelletVisible = false;
			pacMan.moveDir = Direction.RIGHT;
			for (var ghost : ghosts) {
				ghost.moveDir = Direction.RIGHT;
				ghost.speed = game.ghostSpeedFrightened;
			}
			pacManChasingGhosts = true;
		}
		pacMan.move(pacMan.moveDir);
		for (var ghost : ghosts) {
			ghost.move(ghost.moveDir);
		}
		if (pacManChasingGhosts) {
			if (pacMan.x > ghosts[3].x) {
				hit = 4;
			} else {
				for (var ghost : ghosts) {
					if (Math.abs(pacMan.x - ghost.x) <= 8) {
						hit = ghost.id;
						break;
					}
				}
			}
		}
	}

	private void drawHeading(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("CHARACTER / NICKNAME", t(6), t(6));
	}

	private void drawGhostImage(Graphics2D g, int id) {
		g.drawImage(ss.ghosts.get(id).get(Direction.RIGHT).get(0), t(3), t(6 + 3 * id) + World.HTS, null);
	}

	private void drawGhostCharacter(Graphics2D g, int id) {
		g.setColor(ss.ghostColor(id));
		g.setFont(ss.arcadeFont);
		g.drawString("-" + character(id), t(6), t(6 + 3 * id) + World.HTS + 12);
	}

	private void drawGhostNickname(Graphics2D g, int id) {
		g.setColor(ss.ghostColor(id));
		g.setFont(ss.arcadeFont);
		g.drawString("\"" + nickname(id) + "\"", t(17), t(6 + 3 * id) + World.HTS + 12);
	}

	private void drawPointsAwarded(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("10", t(12), t(25));
		g.drawString("50", t(12), t(27));
		g.setFont(ss.arcadeFont.deriveFont(6.0f));
		g.drawString("PTS", t(15), t(25));
		g.drawString("PTS", t(15), t(27));
		g.setColor(Color.PINK);
		g.fillRect(t(10) + 3, t(24) + 3, 2, 2);
		if (!powerPelletsBlinking || game.frame(30, 2) == 0) {
			g.fillOval(t(10), t(26), t(1), t(1));
		}
	}

	private void drawPowerPellet(Graphics2D g) {
		if (!powerPelletVisible) {
			return;
		}
		if (!powerPelletsBlinking || game.frame(30, 2) == 0) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(20) + World.HTS, t(1), t(1));
		}
	}

	private void drawGuys(Graphics2D g) {
		if (pacManChasingGhosts) {
			drawPacManChasingGhosts(g, hit);
		} else {
			drawGhostChasingPacMan(g);
		}
	}

	private void drawGhostChasingPacMan(Graphics2D g) {
		drawPacMan(g);
		for (var ghost : ghosts) {
			drawGhostNormal(g, ghost);
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g, int hit) {
		for (var ghost : ghosts) {
			if (ghost.id > hit) {
				drawGhostFrightened(g, ghost);
			} else if (ghost.id == hit) {
				drawGhostValue(g, ghost);
				drawPacMan(g);
			}
		}
		if (hit == -1 || hit == 4) {
			drawPacMan(g);
		}
	}

	private void drawPacMan(Graphics2D g) {
		g.drawImage(ss.pac.get(pacMan.moveDir).get(game.frame(15, 3)), (int) pacMan.x, (int) pacMan.y, null);
	}

	private void drawGhost(Graphics2D g, Ghost ghost, BufferedImage sprite) {
		g.drawImage(sprite, (int) ghost.x, (int) ghost.y, null);
	}

	private void drawGhostNormal(Graphics2D g, Ghost ghost) {
		drawGhost(g, ghost, ss.ghosts.get(ghost.id).get(ghost.moveDir).get(game.frame(10, 2)));
	}

	private void drawGhostFrightened(Graphics2D g, Ghost ghost) {
		drawGhost(g, ghost, ss.ghostFrightened.get(game.frame(10, 2)));
	}

	private void drawGhostValue(Graphics2D g, Ghost ghost) {
		drawGhost(g, ghost, ss.ghostValues.get(ghost.id == 0 ? 200 : ghost.id == 1 ? 400 : ghost.id == 2 ? 800 : 1600));
	}

	private void drawPressSpaceToPlay(Graphics2D g) {
		if (game.frame(60, 2) == 0) {
			g.setColor(Color.WHITE);
			g.setFont(ss.arcadeFont);
			g.drawString("PRESS SPACE TO PLAY", t(4), t(32));
		}
	}
}