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

import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameUI;

/**
 * @author Armin Reichert
 */
public class GameController {

	public final GameModel game = new GameModel();
	public GameUI ui;
	public GameState state;

	public GameController() {
		for (var gameState : GameState.values()) {
			gameState.gameController = this;
		}
		setState(GameState.INTRO);
	}

	public void setState(GameState state) {
		this.state = state;
		state.timer = -1;
		log("Game state set to %s", state);
	}

	public void step(boolean doUpdate) {
		if (doUpdate) {
			++state.timer;
			if (state.timer == 0) {
				log("onEnter(%s)", state);
				state.onEnter(game, ui);
			} else {
				state.onUpdate(game, ui);
			}
			ui.update();
		}
		ui.render();
	}

	// TODO this is just some arbitrary sample logic, the real game uses dot counters and stuff
	public void unlockGhosts(Ghost[] ghosts) {
		for (var ghost : ghosts) {
			int unlockSeconds = switch (ghost.id) {
			case Ghost.BLINKY -> 0;
			case Ghost.PINKY -> 1;
			case Ghost.INKY -> 5;
			case Ghost.CLYDE -> 15;
			default -> 0;
			};
			if (ghost.state == GhostState.LOCKED && state.timer >= sec(unlockSeconds)) {
				ghost.state = ghost.id == Ghost.BLINKY ? GhostState.SCATTERING : GhostState.LEAVING_HOUSE;
			}
		}
	}

}