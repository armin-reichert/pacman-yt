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
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Logging;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameWindow;

/**
 * @author Armin Reichert
 */
public class GameController {

	public static void main(String[] args) {
		GameController controller = new GameController();
		controller.startSimulation();
	}

	public static int sec(double n) {
		return (int) (n * 60);
	}

	public final GameModel game;
	public final GameWindow window;

	private final Thread simulation = new Thread(this::loop);
	private volatile boolean running;
	private volatile Direction move;

	public GameController() {
		game = new GameModel();
		window = new GameWindow(game, 2.0);
		window.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP -> move = Direction.UP;
				case KeyEvent.VK_DOWN -> move = Direction.DOWN;
				case KeyEvent.VK_LEFT -> move = Direction.LEFT;
				case KeyEvent.VK_RIGHT -> move = Direction.RIGHT;
				case KeyEvent.VK_I -> window.showInfo = !window.showInfo;
				}
			}
		});
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopSimulation();
				System.exit(0);
			}
		});
	}

	private void startSimulation() {
		running = true;
		simulation.run();
	}

	private void stopSimulation() {
		running = false;
		try {
			simulation.join();
			Logging.log("Simulation thread ended");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void loop() {
		init();
		while (running) {
			update();
			window.repaint();
			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	private void enterState(GameState state) {
		game.state = state;
		game.stateTimer = -1;
	}

	private void init() {
		game.setLevelNumber(1);
		game.levelSymbols.clear();
		game.levelSymbols.add(game.bonusSymbol);
		game.pac.lives = 3;
		enterState(GameState.INIT_LEVEL);
	}

	private void update() {
		++game.ticks;
		++game.stateTimer;
		switch (game.state) {
		case INIT_LEVEL -> update_INIT_LEVEL();
		case READY -> update_READY();
		case PLAYING -> update_PLAYING();
		case PACMAN_DEAD -> update_PACMAN_DEAD();
		case LEVEL_COMPLETE -> update_LEVEL_COMPLETE();
		case GAME_OVER -> update_GAME_OVER();
		}
		move = null;
	}

	private void update_INIT_LEVEL() {
		game.powerPelletsBlinking = false;
		game.attackTimer = 0;
		game.world.resetFood();
		game.bonus = -1;
		for (Ghost ghost : game.ghosts) {
			ghost.visible = false;
		}
		game.pac.visible = false;
		if (game.stateTimer == sec(2)) {
			enterState(GameState.READY);
		}
	}

	private void update_READY() {
		if (game.stateTimer == 0) {
			game.reset();
			game.pac.visible = true;
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
				ghost.animated = ghost.id != BLINKY;
			}
		}

		else if (game.stateTimer == sec(5)) {
			game.powerPelletsBlinking = true;
			game.pac.animated = true;
			game.ghosts[BLINKY].animated = true;
			enterState(GameState.PLAYING);
		}

		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}
	}

	private void update_PLAYING() {
		// TODO this is just mockery

		if (game.ghosts[BLINKY].state == GhostState.LOCKED) {
			if (game.stateTimer == sec(0)) {
				game.ghosts[BLINKY].state = GhostState.SCATTERING;
			}
		}
		if (game.ghosts[PINKY].state == GhostState.LOCKED) {
			if (game.stateTimer == sec(1)) {
				game.ghosts[PINKY].state = GhostState.LEAVING_HOUSE;
			}
		}
		if (game.ghosts[INKY].state == GhostState.LOCKED) {
			if (game.stateTimer == sec(3)) {
				game.ghosts[INKY].state = GhostState.LEAVING_HOUSE;
			}
		}
		if (game.ghosts[CLYDE].state == GhostState.LOCKED) {
			if (game.stateTimer == sec(5)) {
				game.ghosts[CLYDE].state = GhostState.LEAVING_HOUSE;
			}
		}

		if (game.world.allFoodEaten()) {
			enterState(GameState.LEVEL_COMPLETE);
			return;
		}

		if (game.chaseStartTicks.contains(game.attackTimer)) {
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.SCATTERING) {
					ghost.state = GhostState.CHASING;
				}
				game.chasing = true;
			}
			log("Chasing phase started");
		} else if (game.scatterStartTicks.contains(game.attackTimer)) {
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.CHASING) {
					ghost.state = GhostState.SCATTERING;
				}
				game.chasing = false;
			}
			log("Scattering phase started");
		}
		++game.attackTimer;

		if (move != null) {
			game.pac.wishDir = move;
		}
		game.pac.update(game);
		if (game.pac.dead) {
			enterState(GameState.PACMAN_DEAD);
			return;
		}

		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}

		if (game.bonusTimer > 0) {
			--game.bonusTimer;
			if (game.bonusTimer == 0) {
				game.bonus = -1;
			}
		}

	}

	private void update_PACMAN_DEAD() {
		game.pac.update(game);

		if (game.stateTimer == 0) {
			game.pac.animated = false;
			game.bonus = -1;
		}

		else if (game.stateTimer == sec(1)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}

		else if (game.stateTimer == sec(1.5)) {
			game.pac.dyingAnimationTimer = game.pac.dyingAnimationDuration;
			game.pac.animated = true;
		}

		else if (game.stateTimer == sec(5)) {
			--game.pac.lives;
			if (game.pac.lives > 0) {
				enterState(GameState.READY);
			} else {
				for (Ghost ghost : game.ghosts) {
					ghost.visible = true;
				}
				enterState(GameState.GAME_OVER);
			}
		}
	}

	private void update_LEVEL_COMPLETE() {
		if (game.stateTimer == 0) {
			game.pac.animated = false;
			game.pac.animFrame = 2; // full face
			for (Ghost ghost : game.ghosts) {
				ghost.animated = false;
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
			game.pac.visible = false;
			game.setLevelNumber(game.levelNumber + 1);
			game.levelSymbols.add(game.bonusSymbol);
			if (game.levelSymbols.size() == 8) {
				game.levelSymbols.remove(0);
			}
			enterState(GameState.INIT_LEVEL);
		}
	}

	private void update_GAME_OVER() {
		if (game.stateTimer == 0) {
			game.powerPelletsBlinking = false;
			for (Ghost ghost : game.ghosts) {
				ghost.animated = false;
			}
			game.pac.animated = false;
		}

		else if (game.stateTimer == sec(5)) {
			init();
		}
	}
}