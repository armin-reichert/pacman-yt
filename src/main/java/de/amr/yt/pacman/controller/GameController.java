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

import javax.sound.sampled.Clip;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.FPSCounter;
import de.amr.yt.pacman.lib.Logging;
import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameWindow;

/**
 * @author Armin Reichert
 */
public class GameController {

	public static int sec(double n) {
		return (int) (n * 60);
	}

	public final GameModel game;
	public GameWindow window;

	private final FPSCounter fpsCounter = new FPSCounter();
	private Thread simulation;
	private boolean running;
	private Direction move;

	private Clip clipGameStart = Sounds.clip("game_start.wav");
	private Clip clipPacManDeath = Sounds.clip("pacman_death.wav");

	public GameController() {
		game = new GameModel();
	}

	public void createAndShowUI() {
		window = new GameWindow(game, fpsCounter, 2.0);
		window.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP -> move = Direction.UP;
				case KeyEvent.VK_DOWN -> move = Direction.DOWN;
				case KeyEvent.VK_LEFT -> move = Direction.LEFT;
				case KeyEvent.VK_RIGHT -> move = Direction.RIGHT;
				case KeyEvent.VK_I -> window.showInfo = !window.showInfo;
				case KeyEvent.VK_P -> game.paused = !game.paused;
				case KeyEvent.VK_Q -> {
					if (game.state != GameState.INTRO) {
						enterState(GameState.INTRO);
					}
				}
				case KeyEvent.VK_S -> game.pacSafe = !game.pacSafe;
				case KeyEvent.VK_SPACE -> {
					if (game.state == GameState.INTRO) {
						enterState(GameState.LEVEL_STARTING);
					}
				}
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
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
	}

	public void startSimulation() {
		newGame();
		running = true;
		simulation = new Thread(this::loop);
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
		final long targetTime = 1_000_000_000 / 60;
		fpsCounter.start();
		while (running) {
			long frameStart = System.nanoTime();
			if (!game.paused) {
				update();
			}
			window.repaint();
			long frameDuration = System.nanoTime() - frameStart;
			if (frameDuration < targetTime) {
				try {
					Thread.sleep((targetTime - frameDuration) / 1_000_000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			fpsCounter.update();
		}
	}

	private void enterState(GameState state) {
		game.state = state;
		game.stateTimer = -1;
	}

	private void newGame() {
		game.setLevelNumber(1);
		game.levelSymbols.clear();
		game.levelSymbols.add(game.bonusSymbol);
		game.lives = 3;
		enterState(GameState.INTRO);
	}

	private void update() {
		++game.ticks;
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
		move = null;
	}

	private void update_INTRO() {
		if (game.stateTimer == sec(1000)) {
			enterState(GameState.LEVEL_STARTING);
		}
	}

	private void update_LEVEL_STARTING() {
		game.powerPelletsBlinking = false;
		game.world.resetFood();
		for (Ghost ghost : game.ghosts) {
			ghost.visible = false;
		}
		game.pac.visible = false;
		if (game.stateTimer == sec(1)) {
			if (game.levelNumber == 1) {
				Sounds.play(clipGameStart);
			}
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
			return;
		}

		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}
	}

	private void update_PLAYING() {

		// check if level is complete
		if (game.world.allFoodEaten()) {
			enterState(GameState.LEVEL_COMPLETE);
			return;
		}

		// check if new attack phase starts
		if (game.chaseStartTicks.contains(game.attackTimer)) {
			startChasingPhase();
		} else if (game.scatterStartTicks.contains(game.attackTimer)) {
			startScatteringPhase();
		}
		++game.attackTimer;

		// Pac-Man business
		if (move != null) {
			game.pac.wishDir = move;
		}
		game.pac.update();

		game.checkPacManFoundPellet();

		if (game.checkPacManFoundPowerPellet()) {
			game.pac.enterPowerState();
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING) {
					ghost.state = GhostState.FRIGHTENED;
				}
			}
			game.ghostsKilledByCurrentPowerPellet = 0;
		}

		game.checkPacManFoundBonus();

		if (game.checkPacManKilledByGhost()) {
			enterState(GameState.PACMAN_DEAD);
			return;
		}

		if (game.checkGhostsKilledByPac()) {
			enterState(GameState.GHOST_DYING);
			return;
		}

		// Ghost business
		releaseGhosts();
		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}

		// Bonus stuff
		game.updateBonus();
	}

	private void releaseGhosts() {
		// TODO this is just arbitrary logic, the real game uses dot counters and stuff
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

	private void startScatteringPhase() {
		for (Ghost ghost : game.ghosts) {
			if (ghost.state == GhostState.CHASING) {
				ghost.state = GhostState.SCATTERING;
			}
			game.chasingPhase = false;
		}
		log("Scattering phase started");
	}

	private void startChasingPhase() {
		for (Ghost ghost : game.ghosts) {
			if (ghost.state == GhostState.SCATTERING) {
				ghost.state = GhostState.CHASING;
			}
			game.chasingPhase = true;
		}
		log("Chasing phase started");
	}

	private void update_PACMAN_DEAD() {
		game.pac.update();

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
			game.pac.dyingAnimationCountdown = game.pac.dyingAnimationDuration;
			game.pac.animated = true;
			Sounds.play(clipPacManDeath);
		}

		else if (game.stateTimer == sec(5)) {
			--game.lives;
			if (game.lives > 0) {
				enterState(GameState.READY);
			} else {
				for (Ghost ghost : game.ghosts) {
					ghost.visible = true;
				}
				enterState(GameState.GAME_OVER);
			}
		}
	}

	private void update_GHOST_DYING() {
		if (game.stateTimer == 0) {
			game.pac.visible = false;
		}

		else if (game.stateTimer == sec(1)) {
			game.pac.visible = true;
			enterState(GameState.PLAYING);
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
			enterState(GameState.LEVEL_STARTING);
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
			newGame();
		}
	}
}