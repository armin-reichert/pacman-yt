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

import static de.amr.yt.pacman.lib.GameClock.sec;
import static de.amr.yt.pacman.lib.Logging.log;
import static de.amr.yt.pacman.lib.SpriteAnimation.frame;
import static de.amr.yt.pacman.model.World.t;
import static de.amr.yt.pacman.ui.Renderer.drawGhost;
import static de.amr.yt.pacman.ui.Renderer.drawGhostValue;
import static de.amr.yt.pacman.ui.Renderer.drawPacMan;
import static de.amr.yt.pacman.ui.Renderer.drawPellet;
import static de.amr.yt.pacman.ui.Renderer.drawPowerPellet;
import static de.amr.yt.pacman.ui.Renderer.drawScore;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import de.amr.yt.pacman.controller.GameController;
import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.GameClock;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class IntroScene implements GameScene {

	public static final int READY_TO_PLAY_TIME = sec(20);

	private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
	private static final String[] GHOST_NICKNAMES = { "BLINKY", "PINKY", "INKY", "CLYDE" };
	private static final int[] GHOST_VALUES = { 200, 400, 800, 1600 };

	private static final int COL_LEFT = t(3), COL_MIDDLE = t(6), COL_RIGHT = t(17);

	private static int row(int ghostID) {
		return t(6 + 3 * ghostID) + World.HT;
	}

	@SuppressWarnings("unused")
	private final GameController gameController;
	private final GameModel game;

	private long passed;
	private boolean pacManChasingGhosts;
	private boolean powerPelletsBlinking;
	private int ghostEaten;
	private int ghostEatenCountdown;

	public IntroScene(GameController gameController) {
		this.gameController = gameController;
		this.game = gameController.game;
	}

	private boolean between(long begin, long end) {
		return begin <= passed && passed <= end;
	}

	@Override
	public void onKeyPressed(int key) {
		switch (key) {
		case KeyEvent.VK_SPACE -> {
			if (passed >= READY_TO_PLAY_TIME) {
				gameController.enterState(GameState.LEVEL_STARTING);
			}
		}
		case KeyEvent.VK_ENTER -> {
			if (!game.paused) {
				gameController.enterState(GameState.LEVEL_STARTING);
			}
		}
		}
	}

	@Override
	public boolean expired() {
		return passed >= sec(25);
	}

	@Override
	public void init() {
		log("IntroScene initialized at %s", GameClock.get());
		passed = 0;
		powerPelletsBlinking = false;
		pacManChasingGhosts = false;
		ghostEaten = -1;
		ghostEatenCountdown = 0;

		game.pacMan.reset();
		game.pacMan.x = t(World.COLS);
		game.pacMan.y = t(20) + World.HT;
		game.pacMan.speed = game.level.playerSpeed;
		game.pacMan.moveDir = Direction.LEFT;
		game.pacMan.setWalkingAnimation();
		game.pacMan.animation().setEnabled(true);

		for (var ghost : game.ghosts) {
			ghost.reset();
			ghost.x = game.pacMan.x + t(3) + ghost.id * 16;
			ghost.y = game.pacMan.y;
			ghost.speed = game.pacMan.speed * 1.05f;
			ghost.moveDir = Direction.LEFT;
			ghost.setWalkingAnimation();
			ghost.animation().setEnabled(true);
		}
	}

	@Override
	public void update() {
		if (between(sec(12), READY_TO_PLAY_TIME)) {
			updateGuys();
		} else if (expired()) {
			init();
		}
		++passed;
	}

	@Override
	public void draw(Graphics2D g) {
		drawScore(g, 0, 0, false);
		if (passed >= sec(1.0)) {
			drawHeading(g);
		}
		if (passed >= sec(2.0)) {
			drawGhostImage(g, Ghost.BLINKY);
		}
		if (passed >= sec(3.0)) {
			drawGhostCharacter(g, Ghost.BLINKY);
		}
		if (passed >= sec(3.5)) {
			drawGhostNickname(g, Ghost.BLINKY);
		}
		if (passed >= sec(4.0)) {
			drawGhostImage(g, Ghost.PINKY);
		}
		if (passed >= sec(5.0)) {
			drawGhostCharacter(g, Ghost.PINKY);
		}
		if (passed >= sec(5.5)) {
			drawGhostNickname(g, Ghost.PINKY);
		}
		if (passed >= sec(6.0)) {
			drawGhostImage(g, Ghost.INKY);
		}
		if (passed >= sec(7.0)) {
			drawGhostCharacter(g, Ghost.INKY);
		}
		if (passed >= sec(7.5)) {
			drawGhostNickname(g, Ghost.INKY);
		}
		if (passed >= sec(8.0)) {
			drawGhostImage(g, Ghost.CLYDE);
		}
		if (passed >= sec(9.0)) {
			drawGhostCharacter(g, Ghost.CLYDE);
		}
		if (passed >= sec(9.5)) {
			drawGhostNickname(g, Ghost.CLYDE);
		}
		if (passed >= sec(10.0)) {
			drawPointsAwarded(g);
		}
		if (passed >= sec(11.0)) {
			drawPacManTargetPowerPellet(g);
		}
		if (passed == sec(11.5)) {
			powerPelletsBlinking = true;
		}
		if (between(sec(12), READY_TO_PLAY_TIME)) {
			if (pacManChasingGhosts) {
				drawPacManChasingGhosts(g);
			} else {
				for (var ghost : game.ghosts) {
					drawGhost(g, ghost, false);
				}
				drawPacMan(g, game.pacMan);
			}
		}
		if (passed >= READY_TO_PLAY_TIME) {
			drawReadyToPlay(g);
		}
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

	private void moveGuys() {
		game.pacMan.move(game.pacMan.moveDir);
		game.pacMan.animation().advance();
		for (var ghost : game.ghosts) {
			ghost.move(ghost.moveDir);
			ghost.animation().advance();
		}
	}

	/*
	 * Phase 1: Guys come in from right side, when Pac-Man finds the power pellet, they reverse direction and chase
	 * begins.
	 */
	private void updateGhostsChasingPacMan() {
		moveGuys();
		if (game.pacMan.x <= COL_LEFT) { // finds power pellet
			game.pacMan.moveDir = Direction.RIGHT;
			for (var ghost : game.ghosts) {
				ghost.moveDir = Direction.RIGHT;
				ghost.speed = game.level.ghostSpeedFrightened;
				ghost.setFrightenedAnimation();
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
		} else {
			moveGuys();
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
		g.drawImage(Sprites.get().ghosts.get(id).get(Direction.RIGHT).get(0), COL_LEFT, row(id), null);
	}

	private void drawGhostCharacter(Graphics2D g, int id) {
		g.setColor(Renderer.ghostColor(id));
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("-" + GHOST_CHARACTERS[id], COL_MIDDLE, row(id) + 12);
	}

	private void drawGhostNickname(Graphics2D g, int id) {
		g.setColor(Renderer.ghostColor(id));
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("\"" + GHOST_NICKNAMES[id] + "\"", COL_RIGHT, row(id) + 12);
	}

	private void drawPointsAwarded(Graphics2D g) {
		drawPellet(g, t(10), t(24));
		if (!powerPelletsBlinking || frame(passed, 2, 15) == 0) {
			drawPowerPellet(g, t(10), t(26));
		}
		g.setColor(Color.WHITE);
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("10", t(12), t(25));
		g.drawString("50", t(12), t(27));
		g.setFont(Renderer.ARCADE_FONT.deriveFont(6.0f));
		g.drawString("PTS", t(15), t(25));
		g.drawString("PTS", t(15), t(27));
	}

	private void drawPacManTargetPowerPellet(Graphics2D g) {
		if (pacManChasingGhosts) {
			return;
		}
		if (!powerPelletsBlinking || frame(passed, 2, 15) == 0) {
			drawPowerPellet(g, COL_LEFT, t(20));
		}
	}

	private void drawReadyToPlay(Graphics2D g) {
		if (frame(passed, 2, 30) == 0) {
			g.setColor(Color.WHITE);
			g.setFont(Renderer.ARCADE_FONT);
			g.drawString("PRESS SPACE TO PLAY", t(4), t(32));
		}
	}
}