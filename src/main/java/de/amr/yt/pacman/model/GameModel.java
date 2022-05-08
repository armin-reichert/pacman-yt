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

import static de.amr.yt.pacman.controller.GameController.sec;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class GameModel {

	public static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	public static final float BASE_SPEED = 1.25f;

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	public final World world;
	public final PacMan pac;
	public final Ghost[] ghosts;

	public int bonus; // active bonus index or -1 if inactive
	public int bonusTimer;
	public boolean bonusEaten;

	public GameState state;
	public long stateTimer;
	public long ticks;
	public int attackTimer;

	public List<Integer> scatterStartTicks;
	public List<Integer> chaseStartTicks;

	public boolean chasingPhase;
	public boolean mazeFlashing;
	public boolean powerPelletsBlinking;
	public int ghostsKilledByPowerPill;
	public int score;

	public int levelNumber; // 1,2,...

	// level-specific settings:
	public int bonusSymbol;
	public float playerSpeed;
	public float ghostSpeed;
	public float ghostSpeedTunnel;
	public int elroy1DotsLeft;
	public float elroy1Speed;
	public int elroy2DotsLeft;
	public float elroy2Speed;
	public float playerSpeedPowered;
	public float ghostSpeedFrightened;
	public int ghostFrightenedSeconds;
	public int numFlashes;

	public final List<Integer> levelSymbols = new ArrayList<>();

	public GameModel() {
		world = new World();
		pac = new PacMan(this);
		ghosts = new Ghost[] { //
				new Ghost(this, BLINKY), //
				new Ghost(this, PINKY), //
				new Ghost(this, INKY), //
				new Ghost(this, CLYDE) };
		setLevelNumber(1);
		levelSymbols.add(bonusSymbol);
	}

	private void setLevel(Object... data) {
		bonusSymbol = (int) data[0];
		playerSpeed = (float) data[1] * BASE_SPEED;
		ghostSpeed = (float) data[2] * BASE_SPEED;
		ghostSpeedTunnel = (float) data[3] * BASE_SPEED;
		elroy1DotsLeft = (int) data[4];
		elroy1Speed = (float) data[5] * BASE_SPEED;
		elroy2DotsLeft = (int) data[6];
		elroy2Speed = (float) data[7] * BASE_SPEED;
		playerSpeedPowered = (float) data[8] * BASE_SPEED;
		ghostSpeedFrightened = (float) data[9] * BASE_SPEED;
		ghostFrightenedSeconds = (int) data[10];
		numFlashes = (int) data[11];
	}

	public void setLevelNumber(int number) {
		if (number < 1) {
			throw new IllegalArgumentException("Level number must be at least 1");
		}
		this.levelNumber = number;
		switch (levelNumber) {
		//@formatter:off
		case  1 -> setLevel(CHERRIES,   0.80f, 0.75f, 0.40f,  20, 0.80f, 10, 0.85f, 0.90f, 0.50f, 6, 5);
		case  2 -> setLevel(STRAWBERRY, 0.90f, 0.85f, 0.45f,  30, 0.90f, 15, 0.95f, 0.95f, 0.55f, 5, 5);
		case  3 -> setLevel(PEACH,      0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 4, 5);
		case  4 -> setLevel(PEACH,      0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 3, 5);
		case  5 -> setLevel(APPLE,      1.00f, 0.95f, 0.50f,  40, 1.00f, 20, 1.05f, 1.00f, 0.60f, 2, 5);
		case  6 -> setLevel(APPLE,      1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 5, 5);
		case  7, 
		      8 -> setLevel(GRAPES,     1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 2, 5);
		case  9 -> setLevel(GALAXIAN,   1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 1, 3);
		case 10 -> setLevel(GALAXIAN,   1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 5, 5);
		case 11 -> setLevel(BELL,       1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 2, 5);
		case 12 -> setLevel(BELL,       1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 13 -> setLevel(KEY,        1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 14 -> setLevel(KEY,        1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 3, 5);
		case 15, 
		     16 -> setLevel(KEY,        1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 17 -> setLevel(KEY,        1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 0.00f, 0.00f, 0, 0);
		case 18 -> setLevel(KEY,        1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 19, 
		     20 -> setLevel(KEY,        1.00f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		default -> setLevel(KEY,        0.90f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		//@formatter:on
		}

		if (levelNumber == 1) {
			scatterStartTicks = List.of(0, sec(27), sec(54), sec(79));
			chaseStartTicks = List.of(sec(7), sec(34), sec(59), sec(84));
		} else if (levelNumber <= 4) {
			scatterStartTicks = List.of(0, sec(27), sec(54), sec(1092));
			chaseStartTicks = List.of(sec(7), sec(34), sec(59), sec(1092) + 1);
		} else {
			scatterStartTicks = List.of(0, sec(25), sec(50), sec(1092));
			chaseStartTicks = List.of(sec(5), sec(30), sec(55), sec(1092) + 1);
		}
	}

	public void reset() {
		attackTimer = 0;
		bonus = -1;
		bonusTimer = 0;
		bonusEaten = false;
		chasingPhase = false;
		powerPelletsBlinking = false;
		ghostsKilledByPowerPill = 0;

		pac.placeAtTile(13, 26, World.HTS, 0);
		pac.wishDir = Direction.LEFT;
		pac.moveDir = Direction.LEFT;
		pac.speed = playerSpeed;
		pac.animFrame = 2;
		pac.animated = false;
		pac.dead = false;

		ghosts[BLINKY].placeAtTile(13, 14, World.HTS, 0);
		ghosts[BLINKY].wishDir = Direction.LEFT;
		ghosts[BLINKY].moveDir = Direction.LEFT;
		ghosts[BLINKY].speed = ghostSpeed;
		ghosts[BLINKY].animated = false;
		ghosts[BLINKY].targetTile = null;
		ghosts[BLINKY].state = GhostState.LOCKED;

		ghosts[INKY].placeAtTile(11, 17, World.HTS, 0);
		ghosts[INKY].wishDir = Direction.UP;
		ghosts[INKY].moveDir = Direction.UP;
		ghosts[INKY].speed = ghostSpeed;
		ghosts[INKY].animated = false;
		ghosts[INKY].targetTile = null;
		ghosts[INKY].state = GhostState.LOCKED;

		ghosts[PINKY].placeAtTile(13, 17, World.HTS, 0);
		ghosts[PINKY].wishDir = Direction.DOWN;
		ghosts[PINKY].moveDir = Direction.DOWN;
		ghosts[PINKY].speed = ghostSpeed;
		ghosts[PINKY].animated = false;
		ghosts[PINKY].targetTile = null;
		ghosts[PINKY].state = GhostState.LOCKED;

		ghosts[CLYDE].placeAtTile(15, 17, World.HTS, 0);
		ghosts[CLYDE].wishDir = Direction.UP;
		ghosts[CLYDE].moveDir = Direction.UP;
		ghosts[CLYDE].speed = ghostSpeed;
		ghosts[CLYDE].animated = false;
		ghosts[CLYDE].targetTile = null;
		ghosts[CLYDE].state = GhostState.LOCKED;
	}

	public void onPacPowerEnding() {
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED) {
				ghost.state = chasingPhase ? GhostState.CHASING : GhostState.SCATTERING;
			}
		}
	}

	public void checkBonus() {
		if (world.eatenFoodCount == 70 || world.eatenFoodCount == 170) {
			bonus = bonusSymbol;
			bonusTimer = sec(9 + new Random().nextDouble());
			bonusEaten = false;
		}
	}

	public void checkExtraLife(int oldScore) {
		if (oldScore < 10_000 && score >= 10_000) {
			pac.lives++;
		}
	}

	public void checkFood() {
		Vector2 pacTile = pac.tile();
		int oldScore = score;
		if (world.eatPellet(pacTile)) {
			score += 10;
			checkBonus();
			checkExtraLife(oldScore);
		} else if (world.eatPowerPellet(pacTile)) {
			score += 50;
			checkBonus();
			checkExtraLife(oldScore);
			pac.enterPowerState();
		}
		if (bonus != -1 && !bonusEaten && pacTile.equals(world.bonusTile)) {
			bonusTimer = sec(2);
			bonusEaten = true;
			score += bonusValue(bonusSymbol);
		}
	}

	public void checkPacKilledByGhost() {
		if (pac.powerTime == 0) {
			Vector2 pacTile = pac.tile();
			for (Ghost ghost : ghosts) {
				if ((ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING)
						&& ghost.tile().equals(pacTile)) {
					pac.dead = true;
					return;
				}
			}
		}
	}

	public void checkGhostsKilledByPac() {
		if (pac.powerTime == 0) {
			return;
		}
		Vector2 pacTile = pac.tile();
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED && ghost.tile().equals(pacTile)) {
				ghostsKilledByPowerPill++;
				ghost.state = GhostState.EATEN;
				ghost.eatenTimer = sec(1);
				ghost.eatenValue = switch (ghostsKilledByPowerPill) {
				case 1 -> 200;
				case 2 -> 400;
				case 3 -> 800;
				case 4 -> 1600;
				default -> 0;
				};
				score += ghost.eatenValue;
			}
		}
	}

	public int bonusValue(int symbol) {
		return switch (symbol) {
		case CHERRIES -> 100;
		case STRAWBERRY -> 300;
		case PEACH -> 500;
		case APPLE -> 700;
		case GRAPES -> 1000;
		case GALAXIAN -> 2000;
		case BELL -> 3000;
		case KEY -> 5000;
		default -> throw new IllegalArgumentException("Unknown symbol ID: " + symbol);
		};
	}
}