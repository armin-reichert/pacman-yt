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
import static de.amr.yt.pacman.controller.GameController.sec;
import static de.amr.yt.pacman.lib.Logging.log;
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;
import static de.amr.yt.pacman.model.World.t;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.yt.pacman.controller.GameController;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class IntroScene implements GameScene {

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
	private final CreatureRenderer renderer = new CreatureRenderer();

	private boolean pacManChasingGhosts;
	private boolean powerPelletsBlinking;
	private boolean powerPelletVisible;
	private int ghostHit;
	private int ghostHitCountdown;

	public IntroScene(GameModel game) {
		this.game = game;
	}

	private boolean at(long tick) {
		return game.stateTimer == tick;
	}

	private boolean passed(long tick) {
		return game.stateTimer >= tick;
	}

	private boolean between(long fromTick, long toTick) {
		return fromTick <= game.stateTimer && game.stateTimer <= toTick;
	}

	@Override
	public void init() {
		log("IntroScene init, game time=%d", GameController.ticks);
		game.pacMan.reset();
		game.pacMan.x = t(World.COLS);
		game.pacMan.y = t(20);
		game.pacMan.speed = game.playerSpeed;
		game.pacMan.moveDir = Direction.LEFT;
		game.pacMan.animated = true;
		for (var ghost : game.ghosts) {
			ghost.reset();
			ghost.x = game.pacMan.x + t(3) + ghost.id * 16;
			ghost.y = game.pacMan.y;
			ghost.speed = game.pacMan.speed * 1.05f;
			ghost.moveDir = game.pacMan.moveDir;
			ghost.animated = true;
		}
		powerPelletVisible = true;
		powerPelletsBlinking = false;
		pacManChasingGhosts = false;
		ghostHit = -1;
		ghostHitCountdown = 0;
	}

	@Override
	public void update() {
		if (between(sec(12), sec(22))) {
			updateGuys();
		}
	}

	@Override
	public void draw(Graphics2D g) {
		if (passed(sec(1.0))) {
			drawHeading(g);
		}
		if (passed(sec(2.0))) {
			drawGhostImage(g, 0);
		}
		if (passed(sec(3.0))) {
			drawGhostCharacter(g, 0);
		}
		if (passed(sec(3.5))) {
			drawGhostNickname(g, 0);
		}
		if (passed(sec(4.0))) {
			drawGhostImage(g, 1);
		}
		if (passed(sec(5.0))) {
			drawGhostCharacter(g, 1);
		}
		if (passed(sec(5.5))) {
			drawGhostNickname(g, 1);
		}
		if (passed(sec(6.0))) {
			drawGhostImage(g, 2);
		}
		if (passed(sec(7.0))) {
			drawGhostCharacter(g, 2);
		}
		if (passed(sec(7.5))) {
			drawGhostNickname(g, 2);
		}
		if (passed(sec(8.0))) {
			drawGhostImage(g, 3);
		}
		if (passed(sec(9.0))) {
			drawGhostCharacter(g, 3);
		}
		if (passed(sec(9.5))) {
			drawGhostNickname(g, 3);
		}
		if (passed(sec(10.0))) {
			drawPointsAwarded(g);
		}
		if (passed(sec(11.0))) {
			drawPowerPellet(g);
		}
		if (at(sec(12))) {
			powerPelletsBlinking = true;
		}
		if (between(sec(12), sec(22))) {
			if (pacManChasingGhosts) {
				drawPacManChasingGhosts(g);
			} else {
				for (var ghost : game.ghosts) {
					renderer.drawGhostNormal(g, ghost);
				}
				renderer.drawPacMan(g, game.pacMan);
			}
		}
		if (passed(sec(22))) {
			drawPressSpaceToPlay(g);
		}
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		for (var ghost : game.ghosts) {
			if (ghost.id > ghostHit) {
				renderer.drawGhostFrightened(g, ghost);
			} else if (ghost.id == ghostHit) {
				int value = ghost.id == 0 ? 200 : ghost.id == 1 ? 400 : ghost.id == 2 ? 800 : 1600;
				renderer.drawGhostValue(g, ghost, value);
			}
		}
		renderer.drawPacMan(g, game.pacMan);
	}

	private void updateGuys() {
		if (pacManChasingGhosts) {
			updatePacManChasingGhosts();
		} else {
			updateGhostsChasingPacMan();
		}
	}

	/*
	 * Phase 1: Guys come in from right side, when Pac-Man finds the power pellet, they reverse direction and chase
	 * begins.
	 */
	private void updateGhostsChasingPacMan() {
		game.pacMan.move(game.pacMan.moveDir);
		for (var ghost : game.ghosts) {
			ghost.move(ghost.moveDir);
		}
		if (game.pacMan.x <= t(2)) { // finds power pellet
			powerPelletVisible = false;
			game.pacMan.moveDir = Direction.RIGHT;
			for (var ghost : game.ghosts) {
				ghost.moveDir = Direction.RIGHT;
				ghost.speed = game.ghostSpeedFrightened;
			}
			pacManChasingGhosts = true;
		}
	}

	/*
	 * Phase 2: Pac-Man chases the ghosts, if a ghost is hit, its value is displayed for a second, Pac-Man is hidden and
	 * the other ghosts stop.
	 */
	private void updatePacManChasingGhosts() {
		if (ghostHitCountdown > 0) {
			--ghostHitCountdown;
			if (ghostHitCountdown == 0) {
				game.pacMan.visible = true;
			} else if (ghostHitCountdown == 15) {
				game.ghosts[ghostHit].visible = false;
			}
		} else {
			game.pacMan.move(game.pacMan.moveDir);
			for (var ghost : game.ghosts) {
				ghost.move(ghost.moveDir);
			}
			if (game.pacMan.x > game.ghosts[3].x) {
				ghostHit = 4;
			} else {
				for (var ghost : game.ghosts) {
					if (Math.abs(ghost.x - game.pacMan.x) <= 1 && ghostHit != ghost.id) {
						ghostHit = ghost.id;
						ghostHitCountdown = sec(0.5);
						game.pacMan.visible = false;
						break;
					}
				}
			}
		}
	}

	private void drawHeading(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(Spritesheet.get().arcadeFont);
		g.drawString("CHARACTER / NICKNAME", t(6), t(6));
	}

	private void drawGhostImage(Graphics2D g, int id) {
		g.drawImage(Spritesheet.get().ghosts.get(id).get(Direction.RIGHT).get(0), t(3), t(6 + 3 * id) + World.HTS, null);
	}

	private void drawGhostCharacter(Graphics2D g, int id) {
		g.setColor(Spritesheet.get().ghostColor(id));
		g.setFont(Spritesheet.get().arcadeFont);
		g.drawString("-" + character(id), t(6), t(6 + 3 * id) + World.HTS + 12);
	}

	private void drawGhostNickname(Graphics2D g, int id) {
		g.setColor(Spritesheet.get().ghostColor(id));
		g.setFont(Spritesheet.get().arcadeFont);
		g.drawString("\"" + nickname(id) + "\"", t(17), t(6 + 3 * id) + World.HTS + 12);
	}

	private void drawPointsAwarded(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(10) + 3, t(24) + 3, 2, 2);
		if (!powerPelletsBlinking || frame(30, 2) == 0) {
			g.fillOval(t(10), t(26), t(1), t(1));
		}
		g.setColor(Color.WHITE);
		g.setFont(Spritesheet.get().arcadeFont);
		g.drawString("10", t(12), t(25));
		g.drawString("50", t(12), t(27));
		g.setFont(Spritesheet.get().arcadeFont.deriveFont(6.0f));
		g.drawString("PTS", t(15), t(25));
		g.drawString("PTS", t(15), t(27));
	}

	private void drawPowerPellet(Graphics2D g) {
		if (!powerPelletVisible) {
			return;
		}
		if (!powerPelletsBlinking || frame(30, 2) == 0) {
			g.setColor(Color.PINK);
			g.fillOval(t(2), t(19) + World.HTS, t(1), t(1));
		}
	}

	private void drawPressSpaceToPlay(Graphics2D g) {
		if (frame(60, 2) == 0) {
			g.setColor(Color.WHITE);
			g.setFont(Spritesheet.get().arcadeFont);
			g.drawString("PRESS SPACE TO PLAY", t(4), t(32));
		}
	}
}