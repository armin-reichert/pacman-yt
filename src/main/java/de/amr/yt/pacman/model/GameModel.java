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

	public float normalGhostSpeed = 1.0f;
	public float frightenedGhostSpeed = 0.55f;
	public float eatenGhostSpeed = 2.0f;
	public float tunnelGhostSpeed = 0.5f;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("GameModel[\n");
		sb.append(pac);
		sb.append(",\n");
		sb.append(ghosts[BLINKY]);
		sb.append(",\n");
		sb.append(ghosts[PINKY]);
		sb.append(",\n");
		sb.append(ghosts[INKY]);
		sb.append(",\n");
		sb.append(ghosts[CLYDE]);
		sb.append("\n]");
		return sb.toString();
	}

	public GameModel() {
		world = new World();
		pac = new PacMan();
		ghosts = new Ghost[] { new Ghost(BLINKY), new Ghost(PINKY), new Ghost(INKY), new Ghost(CLYDE) };
		bonus = -1;
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
		ghosts[BLINKY].target = null;
		ghosts[BLINKY].state = GhostState.SCATTERING;

		ghosts[INKY].placeAtTile(11, 17, World.HTS, 0);
		ghosts[INKY].wishDir = Direction.UP;
		ghosts[INKY].moveDir = Direction.UP;
		ghosts[INKY].animated = false;
		ghosts[INKY].target = null;
		ghosts[INKY].state = GhostState.SCATTERING;

		ghosts[PINKY].placeAtTile(13, 17, World.HTS, 0);
		ghosts[PINKY].wishDir = Direction.DOWN;
		ghosts[PINKY].moveDir = Direction.DOWN;
		ghosts[PINKY].animated = false;
		ghosts[PINKY].target = null;
		ghosts[PINKY].state = GhostState.SCATTERING;

		ghosts[CLYDE].placeAtTile(15, 17, World.HTS, 0);
		ghosts[CLYDE].wishDir = Direction.UP;
		ghosts[CLYDE].moveDir = Direction.UP;
		ghosts[CLYDE].animated = false;
		ghosts[CLYDE].target = null;
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