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

import java.util.Objects;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.FPSCounter;
import de.amr.yt.pacman.lib.Logging;
import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.lib.Vector2;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.ui.GameWindow;

/**
 * @author Armin Reichert
 */
public class GameController {

	public static final int FPS = 60;

	public static long ticks;

	public static int sec(double n) {
		return (int) (n * FPS);
	}

	public static int frame(int duration, int frames) {
		return (int) (ticks % duration) * frames / duration;
	}

	public static int frame(int[] animation) {
		return animation[(int) ticks % animation.length];
	}

	public final GameModel game = new GameModel();

	private final FPSCounter fpsCounter = new FPSCounter();
	private final Thread gameLoop = new Thread(this::gameLoop);
	private boolean gameLoopRunning;
	private Direction steering;
	private GameWindow window;

	public void startGame(double scale) {
		ticks = 0;
		window = new GameWindow(this, fpsCounter, scale);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		window.requestFocus();
		initGame();
		gameLoopRunning = true;
		gameLoop.run();
	}

	public void stopGameLoop() {
		gameLoopRunning = false;
		try {
			gameLoop.join();
			Logging.log("Game loop stopped");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void gameLoop() {
		log("Game loop starting...");
		fpsCounter.start();
		while (gameLoopRunning) {
			long frameStart = System.nanoTime();
			if (!game.paused) {
				update();
				window.update();
			}
			long frameDuration = System.nanoTime() - frameStart;
			long targetFrameDuration = 1_000_000_000L / FPS;
			if (frameDuration < targetFrameDuration) {
				long sleepMillis = (targetFrameDuration - frameDuration) / 1_000_000;
				try {
					Thread.sleep(sleepMillis);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			fpsCounter.update();
			window.repaint();
			++ticks;
		}
	}

	public void enterGameState(GameState state) {
		game.state = state;
		game.stateTimer = -1;
		log("Game state changed to %s", game.state);
	}

	public void steerPacMan(Direction direction) {
		steering = Objects.requireNonNull(direction);
	}

	public void initGame() {
		game.initLevel(1);
		game.levelSymbols.clear();
		game.levelSymbols.add(game.bonusSymbol);
		game.lives = 3;
		game.score = 0;
		Sounds.stopAll();
		enterGameState(GameState.INTRO);
		window.currentScene().init(); // TODO
	}

	public void startLevel() {
		if (game.state == GameState.INTRO) {
			enterGameState(GameState.LEVEL_STARTING);
		}
	}

	private void update() {
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
		steering = null;
	}

	private void update_INTRO() {
		if (game.stateTimer == sec(28)) {
			initGame(); // restart intro
		}
	}

	private void update_LEVEL_STARTING() {
		game.powerPelletsBlinking = false;
		game.world.resetFood();
		for (Ghost ghost : game.ghosts) {
			ghost.visible = false;
		}
		game.pacMan.visible = false;
		if (game.stateTimer == sec(1)) {
			if (game.levelNumber == 1) {
				Sounds.play("game_start");
			}
			enterGameState(GameState.READY);
		}
	}

	private void update_READY() {
		if (game.stateTimer == 0) {
			game.reset();
			game.pacMan.visible = true;
			for (Ghost ghost : game.ghosts) {
				ghost.visible = true;
				ghost.animated = ghost.id != BLINKY;
			}
		}

		else if (game.stateTimer == sec(5)) {
			game.powerPelletsBlinking = true;
			game.pacMan.animated = true;
			game.ghosts[BLINKY].animated = true;
			enterGameState(GameState.PLAYING);
			return;
		}

		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}
	}

	private void update_PLAYING() {
		// Attack logic
		if (game.chaseStartTicks.contains(game.attackTimer)) {
			startChasingPhase();
		} else if (game.scatterStartTicks.contains(game.attackTimer)) {
			startScatteringPhase();
		}
		if (game.pacMan.powerCountdown == 0) {
			++game.attackTimer;
		}

		// Pac-Man behavior
		if (steering != null) {
			game.pacMan.wishDir = steering;
		}
		game.pacMan.update();
		final Vector2 pacManTile = game.pacMan.tile();
		if (game.pacManFindsPellet(pacManTile)) {
			// found normal pellet
		}
		if (game.pacManFindsPowerPellet(pacManTile)) {
			game.ghostsKilledByCurrentPowerPellet = 0;
			for (Ghost ghost : game.ghosts) {
				if (ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING) {
					ghost.state = GhostState.FRIGHTENED;
					ghost.reverse();
				}
			}
			game.pacMan.powerCountdown = sec(game.ghostFrightenedSeconds);
			log("Pac-Man gets power for %d ticks", game.pacMan.powerCountdown);
		}
		if (game.world.allPelletsEaten()) {
			enterGameState(GameState.LEVEL_COMPLETE);
			return;
		}
		if (game.pacManFindsBonus(pacManTile)) {
			// found bonus
		}
		if (game.isPacManKilledByGhost(pacManTile)) {
			enterGameState(GameState.PACMAN_DEAD);
			return;
		}
		if (game.isGhostKilledByPacMan()) {
			enterGameState(GameState.GHOST_DYING);
			return;
		}

		// Ghost stuff
		unlockGhosts();
		for (Ghost ghost : game.ghosts) {
			ghost.update();
		}

		// Bonus stuff
		game.updateBonus();
	}

	private void unlockGhosts() {
		// TODO this is just some arbitrary logic, the real game uses dot counters and stuff
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
				ghost.reverse();
			}
			game.chasingPhase = false;
		}
		log("Scattering phase started");
	}

	private void startChasingPhase() {
		for (Ghost ghost : game.ghosts) {
			if (ghost.state == GhostState.SCATTERING) {
				ghost.state = GhostState.CHASING;
				ghost.reverse();
			}
			game.chasingPhase = true;
		}
		log("Chasing phase started");
	}

	private void update_PACMAN_DEAD() {
		game.pacMan.update();

		if (game.stateTimer == 0) {
			game.pacMan.animated = false;
			game.bonus = -1;
		}

		else if (game.stateTimer == sec(1)) {
			for (Ghost ghost : game.ghosts) {
				ghost.visible = false;
			}
		}

		else if (game.stateTimer == sec(1.5)) {
			game.pacMan.dyingAnimationCountdown = game.pacMan.dyingAnimationDuration;
			game.pacMan.animated = true;
			Sounds.play("pacman_death");
		}

		else if (game.stateTimer == sec(5)) {
			--game.lives;
			if (game.lives > 0) {
				enterGameState(GameState.READY);
			} else {
				for (Ghost ghost : game.ghosts) {
					ghost.visible = true;
				}
				enterGameState(GameState.GAME_OVER);
			}
		}
	}

	private void update_GHOST_DYING() {
		if (game.stateTimer == 0) {
			game.pacMan.visible = false;
		}

		else if (game.stateTimer == sec(1)) {
			game.pacMan.visible = true;
			enterGameState(GameState.PLAYING);
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
			game.pacMan.animated = false;
			game.pacMan.animFrame = 2; // full face
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
			game.pacMan.visible = false;
			game.initLevel(game.levelNumber + 1);
			game.levelSymbols.add(game.bonusSymbol);
			if (game.levelSymbols.size() == 8) {
				game.levelSymbols.remove(0);
			}
			enterGameState(GameState.LEVEL_STARTING);
		}
	}

	private void update_GAME_OVER() {
		if (game.stateTimer == 0) {
			game.powerPelletsBlinking = false;
			for (Ghost ghost : game.ghosts) {
				ghost.animated = false;
			}
			game.pacMan.animated = false;
		}

		else if (game.stateTimer == sec(5)) {
			initGame();
		}
	}
}