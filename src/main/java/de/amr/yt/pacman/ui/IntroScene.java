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

import static de.amr.yt.pacman.lib.Clock.sec;
import static de.amr.yt.pacman.lib.Logging.log;
import static de.amr.yt.pacman.lib.SpriteAnimation.frame;
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;
import static de.amr.yt.pacman.model.World.t;
import static de.amr.yt.pacman.ui.Renderer.drawGhost;
import static de.amr.yt.pacman.ui.Renderer.drawGhostValue;
import static de.amr.yt.pacman.ui.Renderer.drawPacMan;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Clock;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class IntroScene implements GameScene {

	private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
	private static final String[] GHOST_NICKNAMES = { "BLINKY", "PINKY", "INKY", "CLYDE" };
	private static final int[] GHOST_VALUES = { 200, 400, 800, 1600 };

	private final GameModel game;

	private long passed;
	private boolean pacManChasingGhosts;
	private boolean powerPelletsBlinking;
	private int ghostEaten;
	private int ghostEatenCountdown;

	public IntroScene(GameModel game) {
		this.game = game;
	}

	@Override
	public void init() {
		log("IntroScene init, start time=%d", Clock.ticks);
		passed = 0;
		game.pacMan.reset();
		game.pacMan.x = t(World.COLS);
		game.pacMan.y = t(20) + World.HTS;
		game.pacMan.speed = game.playerSpeed;
		game.pacMan.moveDir = Direction.LEFT;
		game.pacMan.animation = game.pacMan.animWalking;
		game.pacMan.animation.enabled = true;
		for (var ghost : game.ghosts) {
			ghost.reset();
			ghost.x = game.pacMan.x + t(3) + ghost.id * 16;
			ghost.y = game.pacMan.y;
			ghost.speed = game.pacMan.speed * 1.05f;
			ghost.moveDir = game.pacMan.moveDir;
			ghost.animation = ghost.animNormal;
			ghost.animation.enabled = true;
		}
		powerPelletsBlinking = false;
		pacManChasingGhosts = false;
		ghostEaten = -1;
		ghostEatenCountdown = 0;
	}

	@Override
	public void update() {
		if (between(sec(12), sec(22))) {
			updateGuys();
		}
		++passed;
	}

	@Override
	public void draw(Graphics2D g) {
		if (passed >= sec(1.0)) {
			drawHeading(g);
		}
		if (passed >= sec(2.0)) {
			drawGhostImage(g, BLINKY);
		}
		if (passed >= sec(3.0)) {
			drawGhostCharacter(g, BLINKY);
		}
		if (passed >= sec(3.5)) {
			drawGhostNickname(g, BLINKY);
		}
		if (passed >= sec(4.0)) {
			drawGhostImage(g, PINKY);
		}
		if (passed >= sec(5.0)) {
			drawGhostCharacter(g, PINKY);
		}
		if (passed >= sec(5.5)) {
			drawGhostNickname(g, PINKY);
		}
		if (passed >= sec(6.0)) {
			drawGhostImage(g, INKY);
		}
		if (passed >= sec(7.0)) {
			drawGhostCharacter(g, INKY);
		}
		if (passed >= sec(7.5)) {
			drawGhostNickname(g, INKY);
		}
		if (passed >= sec(8.0)) {
			drawGhostImage(g, CLYDE);
		}
		if (passed >= sec(9.0)) {
			drawGhostCharacter(g, CLYDE);
		}
		if (passed >= sec(9.5)) {
			drawGhostNickname(g, CLYDE);
		}
		if (passed >= sec(10.0)) {
			drawPointsAwarded(g);
		}
		if (passed >= sec(11.0)) {
			drawPowerPellet(g);
		}
		if (passed == sec(11.5)) {
			powerPelletsBlinking = true;
		}
		if (between(sec(12), sec(22))) {
			if (pacManChasingGhosts) {
				drawPacManChasingGhosts(g);
			} else {
				for (var ghost : game.ghosts) {
					drawGhost(g, ghost, false);
				}
				drawPacMan(g, game.pacMan);
			}
		}
		if (passed >= sec(22.5)) {
			drawPressSpaceToPlay(g);
		}
	}

	private boolean between(long begin, long end) {
		return begin <= passed && passed <= end;
	}

	private void drawPacManChasingGhosts(Graphics2D g) {
		for (var ghost : game.ghosts) {
			if (ghost.id > ghostEaten) {
				drawGhost(g, ghost, false);
			} else if (ghost.id == ghostEaten) {
				drawGhostValue(g, ghost, GHOST_VALUES[ghost.id]);
			}
		}
		drawPacMan(g, game.pacMan);
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
		game.pacMan.animation.advance();
		for (var ghost : game.ghosts) {
			ghost.move(ghost.moveDir);
			ghost.animation.advance();
		}
		if (game.pacMan.x <= t(3)) { // finds power pellet
			game.pacMan.moveDir = Direction.RIGHT;
			for (var ghost : game.ghosts) {
				ghost.moveDir = Direction.RIGHT;
				ghost.speed = game.ghostSpeedFrightened;
				ghost.animation = ghost.animFrightened;
			}
			pacManChasingGhosts = true;
		}
	}

	/*
	 * Phase 2: Pac-Man chases the ghosts, if a ghost is hit, its value is displayed for a second, Pac-Man is hidden and
	 * the other ghosts stop.
	 */
	private void updatePacManChasingGhosts() {
		if (ghostEatenCountdown > 0) {
			--ghostEatenCountdown;
			if (ghostEatenCountdown == 0) {
				if (ghostEaten < 3) {
					game.pacMan.visible = true;
				}
			} else if (ghostEatenCountdown == 15) {
				game.ghosts[ghostEaten].visible = false;
			}
			for (var ghost : game.ghosts) {
				ghost.animNormal.enabled = false;
			}
		} else {
			game.pacMan.move(game.pacMan.moveDir);
			game.pacMan.animation.advance();
			for (var ghost : game.ghosts) {
				ghost.animNormal.enabled = true;
				ghost.move(ghost.moveDir);
				ghost.animation.advance();
			}
			if (game.pacMan.x > game.ghosts[3].x) {
				ghostEaten = 4;
			} else {
				for (var ghost : game.ghosts) {
					if (Math.abs(ghost.x - game.pacMan.x) <= 1 && ghostEaten != ghost.id) {
						ghostEaten = ghost.id;
						ghostEatenCountdown = sec(0.5);
						game.pacMan.visible = false;
						break;
					}
				}
			}
		}
	}

	private void drawHeading(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("CHARACTER / NICKNAME", t(6), t(6));
	}

	private void drawGhostImage(Graphics2D g, int id) {
		g.drawImage(Sprites.get().ghosts.get(id).get(Direction.RIGHT).get(0), t(3), t(6 + 3 * id) + World.HTS, null);
	}

	private void drawGhostCharacter(Graphics2D g, int id) {
		g.setColor(Renderer.ghostColor(id));
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("-" + GHOST_CHARACTERS[id], t(6), t(6 + 3 * id) + World.HTS + 12);
	}

	private void drawGhostNickname(Graphics2D g, int id) {
		g.setColor(Renderer.ghostColor(id));
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("\"" + GHOST_NICKNAMES[id] + "\"", t(17), t(6 + 3 * id) + World.HTS + 12);
	}

	private void drawPointsAwarded(Graphics2D g) {
		g.setColor(Color.PINK);
		g.fillRect(t(10) + 3, t(24) + 3, 2, 2);
		if (!powerPelletsBlinking || frame(passed, 30, 2) == 0) {
			g.fillOval(t(10), t(26), t(1), t(1));
		}
		g.setColor(Color.WHITE);
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("10", t(12), t(25));
		g.drawString("50", t(12), t(27));
		g.setFont(Renderer.ARCADE_FONT.deriveFont(6.0f));
		g.drawString("PTS", t(15), t(25));
		g.drawString("PTS", t(15), t(27));
	}

	private void drawPowerPellet(Graphics2D g) {
		if (pacManChasingGhosts) {
			return;
		}
		if (!powerPelletsBlinking || frame(passed, 30, 2) == 0) {
			g.setColor(Color.PINK);
			g.fillOval(t(3), t(20), t(1), t(1));
		}
	}

	private void drawPressSpaceToPlay(Graphics2D g) {
		if (frame(passed, 60, 2) == 0) {
			g.setColor(Color.WHITE);
			g.setFont(Renderer.ARCADE_FONT);
			g.drawString("PRESS SPACE TO PLAY", t(4), t(32));
		}
	}
}