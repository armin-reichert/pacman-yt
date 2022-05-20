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
import static de.amr.yt.pacman.lib.Vector2.v;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.GameClock;
import de.amr.yt.pacman.lib.Sounds;
import de.amr.yt.pacman.lib.Vector2;

/**
 * TODO: should not have dependency to controller package
 *
 * @author Armin Reichert
 */
public class GameModel {

	/** Speed in pixels/tick at 100%. */
	public static final float BASE_SPEED = 1.25f;

	public final Vector2 pacManHome = v(13, 26);
	public final Vector2[] ghostHomes = { v(13, 14), v(13, 17), v(11, 17), v(15, 17) };
	public final Vector2[] ghostScatterTargets = { v(25, 0), v(2, 0), v(27, 34), v(0, 34) };
	public final Direction[] ghostStartDirections = { Direction.LEFT, Direction.DOWN, Direction.UP, Direction.UP };
	public final Vector2 bonusTile = v(13, 20);

	public final World world;
	public final List<Integer> levelCounter = new ArrayList<>();
	public final PacMan pacMan;
	public final Ghost[] ghosts;
	public Bonus bonus;

	public GameLevel level;
	public long attackTimer;

	public volatile boolean paused;
	public volatile boolean pacSafe;
	public boolean levelStarted;
	public boolean chasingPhase;
	public boolean mazeFlashing;
	public boolean powerPelletsBlinking;

	public int score;
	public int lives;
	public int ghostsKilledByEnergizer;
	public int pacManLosingPowerTicks = 120; // TODO just a guess

	public GameModel() {
		world = new World();
		pacMan = new PacMan(this);
		ghosts = new Ghost[] { //
				new Ghost(this, Ghost.BLINKY), //
				new Ghost(this, Ghost.PINKY), //
				new Ghost(this, Ghost.INKY), //
				new Ghost(this, Ghost.CLYDE) //
		};
		setLevel(1);
	}

	public void setLevel(int levelNumber) {
		if (levelNumber < 1) {
			throw new IllegalArgumentException("Level number must be at least 1");
		}
		level = switch (levelNumber) {
		//@formatter:off
		case  1 -> new GameLevel(levelNumber, Bonus.CHERRIES,    100, 0.80f, 0.75f, 0.40f,  20, 0.80f, 10, 0.85f, 0.90f, 0.50f, 6, 5);
		case  2 -> new GameLevel(levelNumber, Bonus.STRAWBERRY,  300, 0.90f, 0.85f, 0.45f,  30, 0.90f, 15, 0.95f, 0.95f, 0.55f, 5, 5);
		case  3 -> new GameLevel(levelNumber, Bonus.PEACH,       500, 0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 4, 5);
		case  4 -> new GameLevel(levelNumber, Bonus.PEACH,       500, 0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 3, 5);
		case  5 -> new GameLevel(levelNumber, Bonus.APPLE,       700, 1.00f, 0.95f, 0.50f,  40, 1.00f, 20, 1.05f, 1.00f, 0.60f, 2, 5);
		case  6 -> new GameLevel(levelNumber, Bonus.APPLE,       700, 1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 5, 5);
		case  7, 
		      8 -> new GameLevel(levelNumber, Bonus.GRAPES,     1000, 1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 2, 5);
		case  9 -> new GameLevel(levelNumber, Bonus.GALAXIAN,   2000, 1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 1, 3);
		case 10 -> new GameLevel(levelNumber, Bonus.GALAXIAN,   2000, 1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 5, 5);
		case 11 -> new GameLevel(levelNumber, Bonus.BELL,       3000, 1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 2, 5);
		case 12 -> new GameLevel(levelNumber, Bonus.BELL,       3000, 1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 13 -> new GameLevel(levelNumber, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 14 -> new GameLevel(levelNumber, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 3, 5);
		case 15, 
		     16 -> new GameLevel(levelNumber, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 17 -> new GameLevel(levelNumber, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 0.00f, 0.00f, 0, 0);
		case 18 -> new GameLevel(levelNumber, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 19, 
		     20 -> new GameLevel(levelNumber, Bonus.KEY,        5000, 1.00f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		default -> new GameLevel(levelNumber, Bonus.KEY,        5000, 0.90f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		//@formatter:on
		};

		if (levelNumber == 1) {
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

	public synchronized void reset() {
		Sounds.stopAll();
		setLevel(1);
	}

	public void getReadyToRumble() {
		bonus = null;
		chasingPhase = true;
		powerPelletsBlinking = false;
		ghostsKilledByEnergizer = 0;

		pacMan.placeAtTile(pacManHome, World.HT, 0);
		pacMan.wishDir = pacMan.moveDir = Direction.LEFT;
		pacMan.speed = level.playerSpeed;
		pacMan.state = PacManState.NO_POWER;
		pacMan.visible = true;
		pacMan.animation = pacMan.animStanding;
		pacMan.animation.reset();

		for (var ghost : ghosts) {
			ghost.placeAtTile(ghostHomes[ghost.id], World.HT, 0);
			ghost.wishDir = ghost.moveDir = ghostStartDirections[ghost.id];
			ghost.speed = level.ghostSpeed;
			ghost.targetTile = null;
			ghost.state = GhostState.LOCKED;
			ghost.visible = true;
			ghost.animation = ghost.animWalking;
			ghost.animation.reset();
			ghost.animation.setEnabled(false);
		}
	}

	/**
	 * @param points points scored
	 * @return <code>true</code> if an extra life has been won
	 */
	public boolean score(int points) {
		int oldScore = score;
		score += points;
		if (oldScore < 10_000 && score >= 10_000) {
			lives++;
			return true;
		}
		return false;
	}

	private static int index(long[] array, long value) {
		for (int i = 0; i < array.length; ++i) {
			if (array[i] == value) {
				return i;
			}
		}
		return -1;
	}

	public void updateAttackWave() {
		if (!chasingPhase) {
			// is next chasing phase due?
			int phaseStart = index(level.chaseStartTicks, attackTimer);
			if (phaseStart != -1) {
				startChasingPhase(phaseStart);
			}
		} else {
			// is next scattering phase due?
			int phaseStart = index(level.scatterStartTicks, attackTimer);
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
				ghost.reverseDirection = true;
			}
			chasingPhase = false;
		}
		log("Scatter phase %d started at %s", phase + 1, GameClock.get());
	}

	private void startChasingPhase(int phase) {
		for (var ghost : ghosts) {
			if (ghost.state == GhostState.SCATTERING) {
				ghost.state = GhostState.CHASING;
				ghost.reverseDirection = true;
			}
			chasingPhase = true;
		}
		log("Chasing phase %d started at %s", phase + 1, GameClock.get());
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
		if (world.consumePelletAt(pacMan.tile())) {
			pacMan.restCountdown = 1;
			score(10);
			checkBonusAwarded();
			checkBlinkyElroyState();
			return true;
		}
		return false;
	}

	public boolean checkPowerPelletEaten() {
		if (world.consumePowerPelletAt(pacMan.tile())) {
			pacMan.state = PacManState.POWER;
			pacMan.powerCountdown = sec(level.ghostFrightenedSeconds);
			pacMan.restCountdown = 3;
			score(50);
			checkBonusAwarded();
			checkBlinkyElroyState();
			ghostsKilledByEnergizer = 0;
			for (var ghost : ghosts) {
				if (ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING) {
					ghost.state = GhostState.FRIGHTENED;
					ghost.animFrightened.setEnabled(true);
					ghost.reverseDirection = true;
				}
			}
			log("Pac-Man gets power for %d ticks", pacMan.powerCountdown);
			return true;
		}
		return false;
	}

	private void checkBlinkyElroyState() {
		int foodRemaining = world.totalFoodCount - world.eatenFoodCount;
		if (foodRemaining == level.elroy2DotsLeft) {
			ghosts[Ghost.BLINKY].elroyState = 2;
		} else if (foodRemaining == level.elroy1DotsLeft) {
			ghosts[Ghost.BLINKY].elroyState = 1;
		}
	}

	public boolean checkAllPelletsEaten() {
		return world.eatenFoodCount == world.totalFoodCount;
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
		if (bonus != null && !bonus.eaten && pacMan.tile().equals(bonusTile)) {
			bonus.timer = sec(2);
			bonus.eaten = true;
			score(bonus.value);
			return true;
		}
		return false;
	}

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