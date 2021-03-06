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
import static de.amr.yt.pacman.lib.Logging.log;

import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.PacMan;
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
			log("onEnter(%s): Pac-Man animation is %s", this, game.pacMan.animations.selected());
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
				ghost.animations.selected().setEnabled(true);
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
			unlockGhosts(game.ghosts);
			for (var ghost : game.ghosts) {
				ghost.update();
			}
			game.updateBonus();
		}

		// TODO this is just some arbitrary sample logic, the real game uses dot counters and stuff
		private void unlockGhosts(Ghost[] ghosts) {
			for (var ghost : ghosts) {
				int unlockSeconds = switch (ghost.id) {
				case Ghost.BLINKY -> 0;
				case Ghost.PINKY -> 1;
				case Ghost.INKY -> 5;
				case Ghost.CLYDE -> 15;
				default -> 0;
				};
				if (ghost.state == GhostState.LOCKED && timer >= sec(unlockSeconds)) {
					ghost.state = ghost.id == Ghost.BLINKY ? GhostState.SCATTERING : GhostState.LEAVING_HOUSE;
				}
			}
		}

	},

	LEVEL_COMPLETE {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.pacMan.selectAnimation(PacMan.AnimationKey.STANDING);
			for (var ghost : game.ghosts) {
				ghost.animations.selected().setEnabled(false);
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
				game.pacMan.selectAnimation(PacMan.AnimationKey.WALKING);
				gameController.enterState(GameState.LEVEL_STARTING);
			}
		}
	},

	GAME_OVER {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.powerPelletsBlinking = false;
			for (var ghost : game.ghosts) {
				ghost.animations.selected().setEnabled(false);
			}
			game.pacMan.animations.selected().setEnabled(false);
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(5)) {
				game.reset();
				gameController.enterState(INTRO);
			}
		}
	},

	PACMAN_DYING {
		@Override
		public void onEnter(GameModel game, GameUI ui) {
			game.attackTimer = 0;
			game.pacMan.selectAnimation(PacMan.AnimationKey.DYING);
			game.pacMan.animations.selected().setEnabled(false);
			game.bonus = null;
		}

		@Override
		public void onUpdate(GameModel game, GameUI ui) {
			if (timer == sec(1)) {
				for (var ghost : game.ghosts) {
					ghost.visible = false;
				}
			} else if (timer == sec(2)) {
				game.pacMan.animations.selected().setEnabled(true);
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

	public static void setMaster(GameController master) {
		for (var state : values()) {
			state.gameController = master;
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