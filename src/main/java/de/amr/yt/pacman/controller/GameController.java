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
		if (game.stateTimer == sec(3)) {
			enterState(GameState.READY);
		}
	}

	private void update_READY() {
		if (game.stateTimer == 0) {
			game.reset();
			game.pac.visible = true;
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
				ghost.animated = true;
			}
		}

		else if (game.stateTimer == sec(3)) {
			game.powerPelletsBlinking = true;
			game.pac.animated = true;
			enterState(GameState.PLAYING);
		}
	}

	private void update_PLAYING() {

		if (game.world.allFoodEaten()) {
			enterState(GameState.LEVEL_COMPLETE);
			return;
		}

		++game.attackTimer;
		if (game.chaseStartTicks.contains(game.attackTimer)) {
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.SCATTERING) {
					ghost.state = GhostState.CHASING;
				}
				game.chasing = true;
			}
		} else if (game.scatterStartTicks.contains(game.attackTimer)) {
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.CHASING) {
					ghost.state = GhostState.SCATTERING;
				}
				game.chasing = false;
			}
		}

		if (move != null) {
			game.pac.wishDir = move;
		}
		game.pac.update(game);
		if (game.pac.dead) {
			enterState(GameState.PACMAN_DEAD);
			return;
		}

		for (Ghost ghost : game.ghosts) {
			ghost.update(game);
		}

		if (game.bonusTimer > 0) {
			--game.bonusTimer;
			if (game.bonusTimer == 0) {
				game.bonus = -1;
				game.bonusValue = 0;
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
			if (--game.pac.lives > 0) {
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