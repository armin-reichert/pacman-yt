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
package de.amr.yt.pacman.model;

import static de.amr.yt.pacman.lib.GameClock.sec;

import de.amr.yt.pacman.controller.GameController;
import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.ui.GameUI;

/**
 * @author Armin Reichert
 */
public enum GameState {

	INTRO {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
		}
	},

	LEVEL_STARTING {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
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
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
			if (game.state.timer == sec(1)) {
				game.setState(GameState.READY);
			}
		}
	},

	READY {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
			game.getReadyToRumble();
			if (game.level.number == 1 && !game.levelStarted) {
				Sounds.play("level_start");
			}
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
			boolean playSound = game.level.number == 1 && !game.levelStarted;
			if (game.state.timer == sec(playSound ? 5 : 1)) {
				game.levelStarted = true;
				game.setState(GameState.PLAYING);
			}
		}
	},

	PLAYING {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
			game.powerPelletsBlinking = true;
			for (var ghost : game.ghosts) {
				ghost.animation.setEnabled(true);
			}
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
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
	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
			game.pacMan.animation = game.pacMan.animStanding;
			for (var ghost : game.ghosts) {
				ghost.animWalking.setEnabled(false);
			}
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
			if (game.state.timer == sec(1)) {
				for (var ghost : game.ghosts) {
					ghost.visible = false;
				}
				game.mazeFlashing = true;
			} else if (game.state.timer == sec(3)) {
				game.mazeFlashing = false;
				game.setLevel(game.level.number + 1);
				game.pacMan.visible = false;
				game.pacMan.animation = game.pacMan.animWalking;
				game.setState(GameState.LEVEL_STARTING);
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
			game.powerPelletsBlinking = false;
			for (var ghost : game.ghosts) {
				ghost.animation.setEnabled(false);
			}
			game.pacMan.animation.setEnabled(false);
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
			if (game.state.timer == sec(5)) {
				controller.newGame();
			}
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
			game.attackTimer = 0;
			game.pacMan.animation = game.pacMan.animDying;
			game.pacMan.animation.setEnabled(false);
			game.pacMan.animation.reset();
			game.bonus = null;
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
			if (game.state.timer == sec(1)) {
				for (var ghost : game.ghosts) {
					ghost.visible = false;
				}
			} else if (game.state.timer == sec(2)) {
				game.pacMan.animation.setEnabled(true);
				Sounds.play("pacman_death");
			} else if (game.state.timer == sec(4)) {
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
	},

	GHOST_DYING {
		@Override
		public void onEnter(GameController controller, GameModel game, GameUI ui) {
			game.pacMan.visible = false;
		}

		@Override
		public void onUpdate(GameController controller, GameModel game, GameUI ui) {
			if (game.state.timer == sec(1)) {
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
	};

	public long timer;

	public abstract void onEnter(GameController controller, GameModel game, GameUI ui);

	public abstract void onUpdate(GameController controller, GameModel game, GameUI ui);
}