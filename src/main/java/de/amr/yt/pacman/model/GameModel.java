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

import java.util.List;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class GameModel {

	public static final int BLINKY = 0;
	public static final int PINKY = 1;
	public static final int INKY = 2;
	public static final int CLYDE = 3;

	public static final float BASE_SPEED = 1.25f;

	public static final int CHERRIES = 0, STRAWBERRY = 1, PEACH = 2, APPLE = 3, GRAPES = 4, GALAXIAN = 5, BELL = 6,
			KEY = 7;

	public World world;
	public PacMan pac;
	public Ghost[] ghosts;
	public int score;

	public int bonus;
	public int bonusTimer;
	public int bonusValue;
	public boolean bonusEaten;

	public GameState state;
	public long stateTimer;
	public long ticks;
	public int attackTimer;
	public List<Integer> scatterStartTicks = List.of(0, sec(27), sec(54), sec(79));
	public List<Integer> chaseStartTicks = List.of(sec(7), sec(34), sec(59), sec(84));
	public boolean chasing;
	public boolean mazeFlashing;
	public boolean powerPelletsBlinking;
	public int ghostsKilledByPowerPill;

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

	public GameModel() {
		world = new World();
		pac = new PacMan();
		ghosts = new Ghost[] { new Ghost(BLINKY), new Ghost(PINKY), new Ghost(INKY), new Ghost(CLYDE) };
		bonus = -1;
		setLevelNumber(1);
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

	public void setLevelNumber(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Level number must at least be 1");
		}
		this.levelNumber = n;
		switch (levelNumber) {
		//@formatter:off
		case 1 -> setLevel(CHERRIES,   0.80f, 0.75f, 0.40f,  20, 0.80f, 10, 0.85f, 0.90f, 0.50f, 6, 5);
		case 2 -> setLevel(STRAWBERRY, 0.90f, 0.85f, 0.45f,  30, 0.90f, 15, 0.95f, 0.95f, 0.55f, 5, 5);
		case 3 -> setLevel(PEACH,      0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 4, 5);
		case 4 -> setLevel(PEACH,      0.90f, 0.85f, 0.45f,  40, 0.90f, 20, 0.95f, 0.95f, 0.55f, 3, 5);
		case 5 -> setLevel(APPLE,      1.00f, 0.95f, 0.50f,  40, 1.00f, 20, 1.05f, 1.00f, 0.60f, 2, 5);
		case 6 -> setLevel(APPLE,      1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 5, 5);
		case 7, 
		     8 -> setLevel(GRAPES,     1.00f, 0.95f, 0.50f,  50, 1.00f, 25, 1.05f, 1.00f, 0.60f, 2, 5);
		case 9 -> setLevel(GALAXIAN,   1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 1, 3);
		case 10 -> setLevel(GALAXIAN,  1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 5, 5);
		case 11 -> setLevel(BELL,      1.00f, 0.95f, 0.50f,  60, 1.00f, 30, 1.05f, 1.00f, 0.60f, 2, 5);
		case 12 -> setLevel(BELL,      1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 13 -> setLevel(KEY,       1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 1, 3);
		case 14 -> setLevel(KEY,       1.00f, 0.95f, 0.50f,  80, 1.00f, 40, 1.05f, 1.00f, 0.60f, 3, 5);
		case 15, 
		     16 -> setLevel(KEY,       1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 17 -> setLevel(KEY,       1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 0.00f, 0.00f, 0, 0);
		case 18 -> setLevel(KEY,       1.00f, 0.95f, 0.50f, 100, 1.00f, 50, 1.05f, 1.00f, 0.60f, 1, 3);
		case 19, 
		     20 -> setLevel(KEY,       1.00f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		default /* 21... */
		        -> setLevel(KEY,       0.90f, 0.95f, 0.50f, 120, 1.00f, 60, 1.05f, 0.00f, 0.00f, 0, 0);
		//@formatter:on
		}
	}

	public void reset() {
		attackTimer = 0;
		chasing = false;
		powerPelletsBlinking = false;
		ghostsKilledByPowerPill = 0;

		pac.placeAtTile(13, 26, World.HTS, 0);
		pac.wishDir = Direction.LEFT;
		pac.moveDir = Direction.LEFT;
		pac.speed = 1.25f;
		pac.animFrame = 2;
		pac.animated = false;
		pac.dead = false;

		ghosts[BLINKY].placeAtTile(13, 14, World.HTS, 0);
		ghosts[BLINKY].wishDir = Direction.LEFT;
		ghosts[BLINKY].moveDir = Direction.LEFT;
		ghosts[BLINKY].animated = false;
		ghosts[BLINKY].targetTile = null;
		ghosts[BLINKY].state = GhostState.SCATTERING;

		ghosts[INKY].placeAtTile(11, 17, World.HTS, 0);
		ghosts[INKY].wishDir = Direction.UP;
		ghosts[INKY].moveDir = Direction.UP;
		ghosts[INKY].animated = false;
		ghosts[INKY].targetTile = null;
		ghosts[INKY].state = GhostState.SCATTERING;

		ghosts[PINKY].placeAtTile(13, 17, World.HTS, 0);
		ghosts[PINKY].wishDir = Direction.DOWN;
		ghosts[PINKY].moveDir = Direction.DOWN;
		ghosts[PINKY].animated = false;
		ghosts[PINKY].targetTile = null;
		ghosts[PINKY].state = GhostState.SCATTERING;

		ghosts[CLYDE].placeAtTile(15, 17, World.HTS, 0);
		ghosts[CLYDE].wishDir = Direction.UP;
		ghosts[CLYDE].moveDir = Direction.UP;
		ghosts[CLYDE].animated = false;
		ghosts[CLYDE].targetTile = null;
		ghosts[CLYDE].state = GhostState.SCATTERING;
	}

	public void onPowerStateComplete() {
		for (Ghost ghost : ghosts) {
			if (ghost.state != GhostState.EATEN) {
				ghost.state = chasing ? GhostState.CHASING : GhostState.SCATTERING;
			}
		}
	}

	public void checkBonus() {
		if (world.eatenFoodCount == 70 || world.eatenFoodCount == 170) {
			bonus = 0;
			bonusValue = 100; // TODO
			bonusTimer = sec(10);
			bonusEaten = false;
		}
	}

	public void checkExtraLife(int oldScore) {
		if (oldScore < 10_000 && score >= 10_000) {
			pac.lives++;
		}
	}

	public void checkFood() {
		Vector2 tile = pac.tile();
		int oldScore = score;
		if (world.eatPellet(tile)) {
			score += 10;
			checkBonus();
			checkExtraLife(oldScore);
		} else if (world.eatPowerPellet(tile)) {
			score += 50;
			checkBonus();
			checkExtraLife(oldScore);
			pac.enterPowerState(this);
		}
		if (bonus != -1 && !bonusEaten && tile.equals(world.bonusTile)) {
			score += bonusValue;
			bonusTimer = sec(2);
			bonusValue = 100;
			bonusEaten = true;
		}
	}

	public void checkPacKilledByGhost() {
		if (pac.powerTime == 0) {
			Vector2 tile = pac.tile();
			for (Ghost ghost : ghosts) {
				if (ghost.tile().equals(tile)) {
					pac.dead = true;
					return;
				}
			}
		}
	}

	public void checkGhostsKilledByPac() {
		Vector2 pacTile = pac.tile();
		for (Ghost ghost : ghosts) {
			if (ghost.state == GhostState.FRIGHTENED && ghost.tile().equals(pacTile)) {
				ghostsKilledByPowerPill++;
				ghost.eatenTimer = sec(1);
				ghost.eatenValue = switch (ghostsKilledByPowerPill) {
				case 1 -> 200;
				case 2 -> 400;
				case 3 -> 800;
				case 4 -> 1600;
				default -> 0;
				};
				ghost.state = GhostState.EATEN;
				score += ghost.eatenValue;
			}
		}
	}
}