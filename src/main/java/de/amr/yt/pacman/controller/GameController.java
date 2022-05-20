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

import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.GameState;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameUI;
import de.amr.yt.pacman.ui.IntroScene;

/**
 * @author Armin Reichert
 */
public class GameController {

	private final GameModel game = new GameModel();
	private GameUI ui;

	public GameController() {
		newGame();
	}

	public void newGame() {
		game.setLevel(1);
		game.setState(GameState.INTRO);
		Sounds.stopAll();

		// TODO: avoid need for calling init() explicitly
		if (ui != null) {
			ui.currentScene().init();
		}
	}

	public void createAndShowUI(double scale) {
		ui = new GameUI(this, game, scale);
		ui.show();
	}

	public void step(boolean singleStepMode) {
		if (!game.paused || singleStepMode) {
			++game.state.timer;
			switch (game.state) {
			case INTRO -> update_INTRO();
			case LEVEL_STARTING -> update_LEVEL_STARTING();
			case READY -> update_READY();
			case PLAYING -> update_PLAYING();
			case PACMAN_DYING -> update_PACMAN_DEAD();
			case GHOST_DYING -> update_GHOST_DYING();
			case LEVEL_COMPLETE -> update_LEVEL_COMPLETE();
			case GAME_OVER -> update_GAME_OVER();
			}
			ui.update();
		}
		ui.render();
	}

	private void update_INTRO() {
		if (game.state.timer == IntroScene.EXPIRATION_TIME) {
			newGame(); // restart intro
		}
	}

	private void update_LEVEL_STARTING() {
		if (game.state.timer == 0) {
			game.attackTimer = 0;
			game.levelStarted = false;
			game.powerPelletsBlinking = false;
			game.world.resetFood();
			for (var ghost : game.ghosts) {
				ghost.visible = false;
				ghost.elroyState = 0;
			}
			game.pacMan.visible = false;
		}

		else if (game.state.timer == sec(1)) {
			game.setState(GameState.READY);
		}
	}

	private void update_READY() {
		boolean playSound = game.level.number == 1 && !game.levelStarted;
		if (game.state.timer == 0) {
			game.getReadyToRumble();
			if (playSound) {
				Sounds.play("level_start");
			}
		} else if (game.state.timer == sec(playSound ? 5 : 1)) {
			game.levelStarted = true;
			game.setState(GameState.PLAYING);
		}
	}

	private void update_PLAYING() {
		if (game.state.timer == 0) {
			game.powerPelletsBlinking = true;
			for (var ghost : game.ghosts) {
				ghost.animation.setEnabled(true);
			}
		}

		game.updateAttackWave();
		ui.joystick.state().ifPresent(joystickState -> {
			game.pacMan.wishDir = joystickState;
		});
		game.pacMan.update();
		game.checkPelletEaten();
		game.checkPowerPelletEaten();
		game.checkBonusEaten();
		if (game.checkAllPelletsEaten()) {
			game.setState(GameState.LEVEL_COMPLETE);
			return;
		}
		if (game.checkPacManKilledByGhost(game.pacMan.tile())) {
			game.setState(GameState.PACMAN_DYING);
			return;
		}
		if (game.checkGhostKilledByPacMan()) {
			game.setState(GameState.GHOST_DYING);
			return;
		}
		game.unlockGhosts();
		for (var ghost : game.ghosts) {
			ghost.update();
		}
		game.updateBonus();
	}

	private void update_PACMAN_DEAD() {
		if (game.state.timer == 0) {
			game.attackTimer = 0;
			game.pacMan.animation = game.pacMan.animDying;
			game.pacMan.animation.setEnabled(false);
			game.pacMan.animation.reset();
			game.bonus = null;
		}

		else if (game.state.timer == sec(1)) {
			for (var ghost : game.ghosts) {
				ghost.visible = false;
			}
		}

		else if (game.state.timer == sec(2)) {
			game.pacMan.animation.setEnabled(true);
			Sounds.play("pacman_death");
		}

		else if (game.state.timer == sec(4)) {
			--game.lives;
			if (game.lives > 0) {
				game.setState(GameState.READY);
			} else {
				for (var ghost : game.ghosts) {
					ghost.visible = true;
				}
				game.setState(GameState.GAME_OVER);
				return;
			}
		}

		game.pacMan.update();
	}

	private void update_GHOST_DYING() {
		if (game.state.timer == 0) {
			game.pacMan.visible = false;
		}

		else if (game.state.timer == sec(1)) {
			game.pacMan.visible = true;
			game.setState(GameState.PLAYING);
			return;
		}

		for (var ghost : game.ghosts) {
			if (ghost.state == GhostState.EATEN) {
				ghost.update();
			}
		}
	}

	private void update_LEVEL_COMPLETE() {
		if (game.state.timer == 0) {
			game.pacMan.animation = game.pacMan.animStanding;
			for (var ghost : game.ghosts) {
				ghost.animWalking.setEnabled(false);
			}
		}

		else if (game.state.timer == sec(1)) {
			for (var ghost : game.ghosts) {
				ghost.visible = false;
			}
			game.mazeFlashing = true;
		}

		else if (game.state.timer == sec(3)) {
			game.mazeFlashing = false;
			game.setLevel(game.level.number + 1);
			game.pacMan.visible = false;
			game.pacMan.animation = game.pacMan.animWalking;
			game.setState(GameState.LEVEL_STARTING);
		}
	}

	private void update_GAME_OVER() {
		if (game.state.timer == 0) {
			game.powerPelletsBlinking = false;
			for (var ghost : game.ghosts) {
				ghost.animation.setEnabled(false);
			}
			game.pacMan.animation.setEnabled(false);
		}

		else if (game.state.timer == sec(5)) {
			newGame();
		}
	}
}