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

import de.amr.yt.pacman.lib.Logging;
import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameUI;

/**
 * @author Armin Reichert
 */
public enum GameState {

	INTRO {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
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

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(1)) {
				gameController.enterState(GameState.READY);
			}
		}
	},

	READY {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.getReadyToRumble();
			if (game.level.number == 1 && !game.levelStarted) {
				Sounds.play("level_start");
			}
			Logging.log("onEnter(%s): Pac-Man animation is %s", this, game.pacMan.animation());
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			boolean playSound = game.level.number == 1 && !game.levelStarted;
			if (timer == sec(playSound ? 5 : 1)) {
				game.levelStarted = true;
				gameController.enterState(GameState.PLAYING);
			}
		}
	},

	PLAYING {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.powerPelletsBlinking = true;
			for (var ghost : game.ghosts) {
				ghost.animation().setEnabled(true);
			}
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			game.updateAttackWave();
			ui.joystick.state().ifPresent(joystickState -> {
				game.pacMan.wishDir = joystickState;
			});
			game.pacMan.update();
			game.checkPelletEaten();
			game.checkPowerPelletEaten();
			game.checkBonusEaten();
			if (game.checkAllPelletsEaten()) {
				gameController.enterState(GameState.LEVEL_COMPLETE);
				return;
			}
			if (game.checkPacManKilledByGhost(game.pacMan.tile())) {
				gameController.enterState(GameState.PACMAN_DYING);
				return;
			}
			if (game.checkGhostKilledByPacMan()) {
				gameController.enterState(GameState.GHOST_DYING);
				return;
			}
			gameController.unlockGhosts(game.ghosts);
			for (var ghost : game.ghosts) {
				ghost.update();
			}
			game.updateBonus();
		}
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.pacMan.showStanding();
			for (var ghost : game.ghosts) {
				ghost.animation().setEnabled(false);
			}
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(1)) {
				for (var ghost : game.ghosts) {
					ghost.visible = false;
				}
				game.mazeFlashing = true;
			} else if (timer == sec(3)) {
				game.mazeFlashing = false;
				game.setLevel(game.level.number + 1);
				game.pacMan.visible = false;
				game.pacMan.showWalking();
				gameController.enterState(GameState.LEVEL_STARTING);
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.powerPelletsBlinking = false;
			for (var ghost : game.ghosts) {
				ghost.animation().setEnabled(false);
			}
			game.pacMan.animation().setEnabled(false);
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(5)) {
				game.reset();
			}
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.attackTimer = 0;
			game.pacMan.showDying();
			game.pacMan.animation().setEnabled(false);
			game.pacMan.animation().reset();
			game.bonus = null;
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(1)) {
				for (var ghost : game.ghosts) {
					ghost.visible = false;
				}
			} else if (timer == sec(2)) {
				game.pacMan.animation().setEnabled(true);
				Sounds.play("pacman_death");
			} else if (timer == sec(4)) {
				--game.lives;
				if (game.lives > 0) {
					gameController.enterState(GameState.READY);
				} else {
					for (var ghost : game.ghosts) {
						ghost.visible = true;
					}
					gameController.enterState(GameState.GAME_OVER);
					return;
				}
			} else {
				game.pacMan.update();
			}
		}
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.pacMan.visible = false;
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(1)) {
				game.pacMan.visible = true;
				gameController.enterState(GameState.PLAYING);
				return;
			}
			for (var ghost : game.ghosts) {
				if (ghost.state == GhostState.EATEN) {
					ghost.update();
				}
			}
		}
	};

	public static void setGameController(GameController gameController) {
		for (var state : values()) {
			state.gameController = gameController;
		}
	}

	protected GameController gameController;
	protected long timer;

	public long timer() {
		return timer;
	}

	public abstract void onEnter(GameModel game, GameUI ui);

	public abstract void onUpdate(GameModel game, GameUI ui);
}