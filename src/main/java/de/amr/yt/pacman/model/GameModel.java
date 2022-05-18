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
import static de.amr.yt.pacman.lib.Logging.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.GameClock;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class GameModel {

	public static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;
	public static final float BASE_SPEED = 1.25f;

	public boolean paused;
	public boolean pacSafe;

	public final World world;
	public final PacMan pacMan;
	public final Ghost[] ghosts;

	public Bonus bonus;

	public GameState state;
	public long stateTimer;
	public int attackTimer;

	public boolean levelStarted;
	public boolean chasingPhase;
	public boolean mazeFlashing;
	public boolean powerPelletsBlinking;
	public int ghostsKilledByEnergizer;
	public int pacManLosingPowerTicks = 120; // TODO just a guess
	public int score;
	public int lives;
	public final List<Integer> levelCounter = new ArrayList<>();

	public GameLevel level;

	public GameModel() {
		world = new World();
		pacMan = new PacMan(this);
		ghosts = new Ghost[] { //
				new Ghost(this, BLINKY), new Ghost(this, PINKY), new Ghost(this, INKY), new Ghost(this, CLYDE) };
		bonus = null;
	}

	public void setLevel(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Level number must be at least 1");
		}
		level = switch (n) {
		//@formatter:off
		case  1 -> new GameLevel(n, Bonus.CHERRIES,    100, 0.80f, 0.75f, 0.40f,  20, 0.80f, 10, 0.85f, 0.90f, 0.50f, 6, 5);
		case  2 -> new GameLevel(n, Bonus.STRAWBERRY,  300, 0.90f, 0.85f, 0.45f,  30, 0.90f, 15, 0.95f, 0.95f, 0.55f, 5, 5);
		case  3 -> new GameLevel(n, Bonus.PEACH,       500, 0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 4, 5);
		case  4 -> new GameLevel(n, Bonus.PEACH,       500, 0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 3, 5);
		case  5 -> new GameLevel(n, Bonus.APPLE,       700, 1.00f, 0.95f, 0.50f,  40, 1.00f, 20, 1.05f, 1.00f, 0.60f, 2, 5);
		case  6 -> new GameLevel(n, Bonus.APPLE,       700, 1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 5, 5);
		case  7, 
		      8 -> new GameLevel(n, Bonus.GRAPES,     1000, 1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 2, 5);
		case  9 -> new GameLevel(n, Bonus.GALAXIAN,   2000, 1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 1, 3);
		case 10 -> new GameLevel(n, Bonus.GALAXIAN,   2000, 1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 5, 5);
		case 11 -> new GameLevel(n, Bonus.BELL,       3000, 1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 2, 5);
		case 12 -> new GameLevel(n, Bonus.BELL,       3000, 1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 13 -> new GameLevel(n, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 14 -> new GameLevel(n, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 3, 5);
		case 15, 
		     16 -> new GameLevel(n, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 17 -> new GameLevel(n, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 0.00f, 0.00f, 0, 0);
		case 18 -> new GameLevel(n, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 19, 
		     20 -> new GameLevel(n, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		default -> new GameLevel(n, Bonus.KEY,        5000, 0.90f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		//@formatter:on
		};

		if (n == 1) {
			score = 0;
			lives = 3;
			levelCounter.clear();
		}
		levelCounter.add(level.bonusSymbol);
		if (levelCounter.size() == 8) {
			levelCounter.remove(0);
		}

		log("Level %d created", level.number);
	}

	public void reset() {
		attackTimer = 0;
		bonus = null;
		chasingPhase = true;
		powerPelletsBlinking = false;
		ghostsKilledByEnergizer = 0;

		pacMan.placeAtTile(world.pacManHomeTile, World.HTS, 0);
		pacMan.wishDir = Direction.LEFT;
		pacMan.moveDir = Direction.LEFT;
		pacMan.speed = level.playerSpeed;
		pacMan.state = PacManState.NO_POWER;
		pacMan.visible = true;
		pacMan.animation = pacMan.animStanding;
		pacMan.animation.reset();

		for (var ghost : ghosts) {
			ghost.speed = level.ghostSpeed;
			ghost.targetTile = null;
			ghost.state = GhostState.LOCKED;
			ghost.visible = true;
			ghost.animation = ghost.animNormal;
			ghost.animation.reset();
			ghost.animation.setEnabled(false);
			switch (ghost.id) {
			case BLINKY -> {
				ghost.placeAtTile(world.blinkyHomeTile, World.HTS, 0);
				ghost.wishDir = ghost.moveDir = Direction.LEFT;
			}
			case PINKY -> {
				ghost.placeAtTile(world.pinkyHomeTile, World.HTS, 0);
				ghost.wishDir = ghost.moveDir = Direction.DOWN;
			}
			case INKY -> {
				ghost.placeAtTile(world.inkyHomeTile, World.HTS, 0);
				ghost.wishDir = ghost.moveDir = Direction.UP;
			}
			case CLYDE -> {
				ghost.placeAtTile(world.clydeHomeTile, World.HTS, 0);
				ghost.wishDir = ghost.moveDir = Direction.UP;
			}
			}
		}
	}

	public void setState(GameState state) {
		this.state = state;
		stateTimer = -1;
		log("Game state set to %s", state);
	}

	private void scorePoints(int points) {
		int oldScore = score;
		score += points;
		if (oldScore < 10_000 && score >= 10_000) {
			lives++;
		}
	}

	public void updateAttackWave() {
		if (!chasingPhase) {
			// is next chasing phase due?
			int phaseStart = level.chaseStartTicks.indexOf(attackTimer);
			if (phaseStart != -1) {
				startChasingPhase(phaseStart);
			}
		} else {
			// is next scattering phase due?
			int phaseStart = level.scatterStartTicks.indexOf(attackTimer);
			if (phaseStart != -1) {
				startScatteringPhase(phaseStart);
			}
		}
		if (!pacMan.hasPower()) {
			// timer is stopped while Pac-Man has power
			++attackTimer;
		}
	}

	private void startScatteringPhase(int phase) {
		for (var ghost : ghosts) {
			if (ghost.state == GhostState.CHASING) {
				ghost.state = GhostState.SCATTERING;
				ghost.reverse();
			}
			chasingPhase = false;
		}
		log("Scatter phase %d started at %s", phase + 1, GameClock.get());
	}

	private void startChasingPhase(int phase) {
		for (var ghost : ghosts) {
			if (ghost.state == GhostState.SCATTERING) {
				ghost.state = GhostState.CHASING;
				ghost.reverse();
			}
			chasingPhase = true;
		}
		log("Chasing phase %d started at %s", phase + 1, GameClock.get());
	}

	public void unlockGhosts() {
		// TODO this is just some arbitrary logic, the real game uses dot counters and stuff
		if (ghosts[BLINKY].state == GhostState.LOCKED && stateTimer == sec(0)) {
			ghosts[BLINKY].state = GhostState.SCATTERING;
		}
		if (ghosts[PINKY].state == GhostState.LOCKED && stateTimer == sec(1)) {
			ghosts[PINKY].state = GhostState.LEAVING_HOUSE;
		}
		if (ghosts[INKY].state == GhostState.LOCKED && stateTimer == sec(3)) {
			ghosts[INKY].state = GhostState.LEAVING_HOUSE;
		}
		if (ghosts[CLYDE].state == GhostState.LOCKED && stateTimer == sec(7)) {
			ghosts[CLYDE].state = GhostState.LEAVING_HOUSE;
		}
	}

	public void onPacPowerEnding() {
		for (var ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED) {
				ghost.state = chasingPhase ? GhostState.CHASING : GhostState.SCATTERING;
				// TODO not sure about this workaround to avoid ghost gets stuck:
				ghost.enteredNewTile = true;
			}
		}
	}

	public boolean checkPelletEaten() {
		if (world.pelletEaten(pacMan.tile())) {
			pacMan.restCountdown = 1;
			scorePoints(10);
			checkBonusAwarded();
			return true;
		}
		return false;
	}

	public boolean checkPowerPelletEaten() {
		if (world.powerPelletEaten(pacMan.tile())) {
			pacMan.state = PacManState.POWER;
			pacMan.powerCountdown = sec(level.ghostFrightenedSeconds);
			pacMan.restCountdown = 3;
			scorePoints(50);
			checkBonusAwarded();
			ghostsKilledByEnergizer = 0;
			for (var ghost : ghosts) {
				if (ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING) {
					ghost.state = GhostState.FRIGHTENED;
					ghost.animFrightened.setEnabled(true);
					ghost.reverse();
				}
			}
			log("Pac-Man gets power for %d ticks", pacMan.powerCountdown);
			return true;
		}
		return false;
	}

	public boolean checkAllPelletsEaten() {
		for (int row = 0; row < World.ROWS; ++row) {
			for (int col = 0; col < World.COLS; ++col) {
				if (world.isPellet(row, col)) {
					return false;
				} else if (world.isPowerPellet(row, col)) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean checkBonusAwarded() {
		if (world.eatenFoodCount == 70 || world.eatenFoodCount == 170) {
			bonus = new Bonus(level.bonusSymbol, level.bonusValue);
			bonus.timer = sec(9 + new Random().nextDouble());
			return true;
		}
		return false;
	}

	public boolean checkBonusEaten() {
		if (bonus != null && !bonus.eaten && pacMan.tile().equals(world.bonusTile)) {
			bonus.timer = sec(2);
			bonus.eaten = true;
			scorePoints(bonus.value);
			return true;
		}
		return false;
	}

	/**
	 * @return {@code true} if Pac-Man has been killed
	 */
	public boolean checkPacManKilledByGhost(Vector2 tile) {
		if (pacSafe || pacMan.hasPower()) {
			return false;
		}
		for (var ghost : ghosts) {
			if (ghost.tile().equals(tile)) {
				if (ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING) {
					pacMan.state = PacManState.DEAD;
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return {@code true} if at least one ghost got killed
	 */
	public boolean checkGhostKilledByPacMan() {
		if (!pacMan.hasPower()) {
			return false;
		}
		boolean killed = false;
		for (var ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED && ghost.tile().equals(pacMan.tile())) {
				killed = true;
				ghostsKilledByEnergizer++;
				if (++level.ghostsKilled == 16) {
					score += 12000;
				}
				ghost.state = GhostState.EATEN;
				ghost.valueTimer = sec(1);
				ghost.value = (int) Math.pow(2, ghostsKilledByEnergizer) * 100;
				score += ghost.value;
			}
		}
		return killed;
	}

	public void updateBonus() {
		if (bonus != null) {
			if (bonus.timer > 0) {
				--bonus.timer;
				if (bonus.timer == 0) {
					bonus = null;
				}
			}
		}
	}
}