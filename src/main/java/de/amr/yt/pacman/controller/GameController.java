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

import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.GameState;
import de.amr.yt.pacman.ui.GameUI;

/**
 * @author Armin Reichert
 */
public class GameController {

	public final GameModel game = new GameModel();
	public GameUI ui;

	public synchronized void newGame() {
		Sounds.stopAll();
		game.setLevel(1);
		game.setState(GameState.INTRO);
	}

	public void step(boolean doUpdate) {
		if (doUpdate) {
			++game.state.timer;
			if (game.state.timer == 0) {
				log("onEnter(%s)", game.state);
				game.state.onEnter(this, game, ui);
			} else {
				game.state.onUpdate(this, game, ui);
			}
			ui.update();
		}
		ui.render();
	}
}