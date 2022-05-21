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

import static de.amr.yt.pacman.lib.Logging.log;

import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.ui.GameUI;

/**
 * @author Armin Reichert
 */
public class GameController {

	public final GameModel game = new GameModel();
	public GameUI ui;
	private GameState state;

	public GameController() {
		GameState.setMaster(this);
		enterState(GameState.INTRO);
	}

	public GameState state() {
		return state;
	}

	public void enterState(GameState state) {
		if (this.state != state) {
			GameState prevState = this.state;
			this.state = state;
			state.timer = 0;
			log("onEnter(%s)", state);
			state.onEnter(game, ui);
			log("Game changed from %s to %s", prevState, state);
		}
	}

	public void update() {
		if (!game.paused) {
			state.onUpdate(game, ui);
			++state.timer;
			ui.update();
		}
		ui.render();
	}

	public void step() {
		state.onUpdate(game, ui);
		++state.timer;
		ui.update();
		ui.render();
	}
}