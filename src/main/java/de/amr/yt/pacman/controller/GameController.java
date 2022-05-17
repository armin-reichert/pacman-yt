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
package de.amr.yt.pacman.controller;

import static de.amr.yt.pacman.lib.GameClock.sec;
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;

import java.util.Objects;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameWindow;

/**
 * @author Armin Reichert
 */
public class GameController {

	private final GameModel game;
	private GameWindow window;
	private Direction joystick;

	public GameController() {
		game = new GameModel();
		newGame();
	}

	public void newGame() {
		game.score = 0;
		game.lives = 3;
		game.setLevel(1);
		game.enterState(GameState.INTRO);
		Sounds.stopAll();
		// TODO: fixme (must call init explicitly in case of restart of intro scene)
		if (window != null) {
			window.currentScene().init();
		}
	}

	public void createAndShowUI(double scale) {
		window = new GameWindow(this, game, scale);
		window.show();
	}

	public void moveJoystick(Direction direction) {
		joystick = Objects.requireNonNull(direction);
	}

	public void startLevel() {
		if (game.state == GameState.INTRO) {
			game.enterState(GameState.LEVEL_STARTING);
		}
	}

	public void step(boolean singleStepMode) {
		if (!game.paused || singleStepMode) {
			++game.stateTimer;
			switch (game.state) {
			case INTRO -> update_INTRO();
			case LEVEL_STARTING -> update_LEVEL_STARTING();
			case READY -> update_READY();
			case PLAYING -> update_PLAYING();
			case PACMAN_DEAD -> update_PACMAN_DEAD();
			case GHOST_DYING -> update_GHOST_DYING();
			case LEVEL_COMPLETE -> update_LEVEL_COMPLETE();
			case GAME_OVER -> update_GAME_OVER();
			}
			joystick = null;
			window.update();
		}
		window.render();
	}

	private void update_INTRO() {
		if (game.stateTimer == sec(25)) {
			newGame(); // restart intro
		}
	}

	private void update_LEVEL_STARTING() {
		if (game.stateTimer == 0) {
			game.powerPelletsBlinking = false;
			game.world.resetFood();
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
			game.pacMan.visible = false;
		}

		else if (game.stateTimer == sec(1)) {
			if (game.level.number == 1) {
				Sounds.play("game_start");
			}
			game.enterState(GameState.READY);
		}
	}

	private void update_READY() {
		if (game.stateTimer == 0) {
			game.reset();
		}

		else if (game.stateTimer == sec(5)) {
			game.powerPelletsBlinking = true;
			game.pacMan.animWalking.setEnabled(true);
			for (Ghost ghost : game.ghosts) {
				ghost.animation.setEnabled(true);
			}
			game.enterState(GameState.PLAYING);
			return;
		}
	}

	private void update_PLAYING() {
		game.updateAttacWave();
		if (joystick != null) {
			game.pacMan.wishDir = joystick;
		}
		game.pacMan.update();
		game.checkPelletEaten();
		game.checkPowerPelletEaten();
		game.checkBonusEaten();
		if (game.world.checkAllPelletsEaten()) {
			game.enterState(GameState.LEVEL_COMPLETE);
			return;
		}
		if (game.checkPacManKilledByGhost(game.pacMan.tile())) {
			game.enterState(GameState.PACMAN_DEAD);
			return;
		}
		if (game.checkGhostKilledByPacMan()) {
			game.enterState(GameState.GHOST_DYING);
			return;
		}
		unlockGhosts();
		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}
		game.updateBonus();
	}

	private void unlockGhosts() {
		// TODO this is just some arbitrary logic, the real game uses dot counters and stuff
		if (game.ghosts[BLINKY].state == GhostState.LOCKED && game.stateTimer == sec(0)) {
			game.ghosts[BLINKY].state = GhostState.SCATTERING;
		}
		if (game.ghosts[PINKY].state == GhostState.LOCKED && game.stateTimer == sec(1)) {
			game.ghosts[PINKY].state = GhostState.LEAVING_HOUSE;
		}
		if (game.ghosts[INKY].state == GhostState.LOCKED && game.stateTimer == sec(3)) {
			game.ghosts[INKY].state = GhostState.LEAVING_HOUSE;
		}
		if (game.ghosts[CLYDE].state == GhostState.LOCKED && game.stateTimer == sec(7)) {
			game.ghosts[CLYDE].state = GhostState.LEAVING_HOUSE;
		}
	}

	private void update_PACMAN_DEAD() {
		game.pacMan.update();

		if (game.stateTimer == 0) {
			game.pacMan.animation.setEnabled(false);
			game.pacMan.animation.reset();
			game.bonus = -1;
		}

		else if (game.stateTimer == sec(1)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}

		else if (game.stateTimer == sec(2)) {
			game.pacMan.animation.setEnabled(true);
			Sounds.play("pacman_death");
		}

		else if (game.stateTimer == sec(4)) {
			--game.lives;
			if (game.lives > 0) {
				game.enterState(GameState.READY);
			} else {
				for (Ghost ghost : game.ghosts) {
					ghost.visible = true;
				}
				game.enterState(GameState.GAME_OVER);
			}
		}
	}

	private void update_GHOST_DYING() {
		if (game.stateTimer == 0) {
			game.pacMan.visible = false;
		}

		else if (game.stateTimer == sec(1)) {
			game.pacMan.visible = true;
			game.enterState(GameState.PLAYING);
			return;
		}

		for (Ghost ghost : game.ghosts) {
			if (ghost.state == GhostState.EATEN) {
				ghost.update();
			}
		}

	}

	private void update_LEVEL_COMPLETE() {
		if (game.stateTimer == 0) {
			game.pacMan.animation = game.pacMan.animStanding;
			for (Ghost ghost : game.ghosts) {
				ghost.animNormal.setEnabled(false);
			}
		}

		else if (game.stateTimer == sec(1)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
			game.mazeFlashing = true;
		}

		else if (game.stateTimer == sec(3)) {
			game.mazeFlashing = false;
			game.setLevel(game.level.number + 1);
			game.pacMan.visible = false;
			game.pacMan.animation = game.pacMan.animWalking;
			game.enterState(GameState.LEVEL_STARTING);
		}
	}

	private void update_GAME_OVER() {
		if (game.stateTimer == 0) {
			game.powerPelletsBlinking = false;
			for (Ghost ghost : game.ghosts) {
				ghost.animNormal.setEnabled(false);
			}
			game.pacMan.animWalking.setEnabled(false);
		}

		else if (game.stateTimer == sec(5)) {
			newGame();
		}
	}
}