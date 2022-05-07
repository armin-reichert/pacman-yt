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

import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	private static final Direction[] DIR_ORDER = { Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT };

	public final int id;
	public final GameModel game;
	public GhostState state;
	public Vector2 targetTile;
	public long eatenTimer;
	public int eatenValue;

	public Ghost(GameModel game, int id) {
		super(game.world);
		this.game = game;
		this.id = id;
		canReverse = false;
	}

	@Override
	public String toString() {
		return "Ghost[id=%d, x=%.2f, y=%.2f, moveDir=%s, wishDir=%s speed=%.2f]".formatted(id, x, y, moveDir, wishDir,
				speed);
	}

	@Override
	protected boolean canEnter(Vector2 tile) {
		if (world.isBlocked(tile)) {
			return false;
		}
		if (world.isGhostHouse(tile)) {
			return state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE;
		}
		if (state != GhostState.FRIGHTENED && wishDir == Direction.UP && world.isOneWayDown(tile)) {
			return false;
		}
		return true;
	}

	public void update() {
		switch (state) {
		case LOCKED -> bounce();
		case ENTERING_HOUSE -> enterGhostHouse();
		case LEAVING_HOUSE -> leaveGhostHouse();
		case CHASING -> {
			moveThroughMaze();
		}
		case SCATTERING -> {
			moveThroughMaze();
		}
		case FRIGHTENED -> {
			moveThroughMaze();
		}
		case EATEN -> {
			moveThroughMaze();
			if (eatenTimer > 0) {
				--eatenTimer;
			}
			if (Math.abs(x - game.world.ghostHouseEntry.x) <= 1 && y == game.world.ghostHouseEntry.y) {
				state = GhostState.ENTERING_HOUSE;
			}
		}
		default -> {
		}
		}
	}

	private void moveThroughMaze() {
		if (enteredNewTile) {
			computeTargetTile();
			steer();
		}
		updateSpeed();
		moveThroughWorld();
	}

	private void updateSpeed() {
		if (state == GhostState.LOCKED) {
			speed = 0.33f * GameModel.BASE_SPEED;
		} else if (state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE) {
			speed = game.ghostSpeedFrightened;
		} else if (state == GhostState.EATEN) {
			speed = eatenTimer > 0 ? 0 : 2 * game.ghostSpeed; // TODO correct?
		} else if (game.world.isTunnel(tile())) {
			speed = game.ghostSpeedTunnel;
		} else if (state == GhostState.FRIGHTENED) {
			speed = game.ghostSpeedFrightened;
		} else {
			speed = game.ghostSpeed;
		}
	}

	private void computeTargetTile() {
		switch (state) {

		case LOCKED, ENTERING_HOUSE, LEAVING_HOUSE -> {
			targetTile = null;
		}

		case EATEN -> {
			targetTile = new Vector2(13, 14);
		}

		case FRIGHTENED -> {
			List<Direction> directions = Arrays.asList(Direction.values());
			Collections.shuffle(directions);
			for (Direction direction : directions) {
				Vector2 neighbor = tile().neighbor(direction);
				if (canEnter(neighbor)) {
					targetTile = neighbor;
					return;
				}
			}
		}

		case SCATTERING -> {
			targetTile = switch (id) {
			case BLINKY -> new Vector2(25, 0);
			case PINKY -> new Vector2(2, 0);
			case INKY -> new Vector2(27, 34);
			case CLYDE -> new Vector2(0, 34);
			default -> null;
			};
		}

		case CHASING -> {
			switch (id) {
			case BLINKY -> {
				targetTile = game.pac.tile();
			}

			case PINKY -> {
				Vector2 pacPlus4 = game.pac.tile().plus(game.pac.moveDir.vector.times(4));
				if (game.pac.moveDir == Direction.UP) {
					pacPlus4 = pacPlus4.plus(new Vector2(-4, 0));
				}
				targetTile = pacPlus4;
			}

			case INKY -> {
				Vector2 pacPlus2 = game.pac.tile().plus(game.pac.moveDir.vector.times(2));
				if (game.pac.moveDir == Direction.UP) {
					pacPlus2 = pacPlus2.plus(new Vector2(-2, 0));
				}
				Vector2 blinkyTile = game.ghosts[BLINKY].tile();
				targetTile = pacPlus2.times(2).minus(blinkyTile);
			}

			case CLYDE -> {
				targetTile = tile().dist(game.pac.tile()) < 8 ? new Vector2(0, 34) : game.pac.tile();
			}

			}
		}

		}
	}

	private void leaveGhostHouse() {
		updateSpeed();
		Vector2 entry = game.world.ghostHouseEntry;
		if (wishDir == Direction.UP && y <= entry.y) {
			// out of house
			wishDir = Direction.LEFT;
			state = game.chasing ? GhostState.CHASING : GhostState.SCATTERING;
		} else if (Math.abs(x - entry.x) <= 1) {
			wishDir = moveDir = Direction.UP;
			x = entry.x;
			move(wishDir);
		} else if (x < entry.x) {
			wishDir = moveDir = Direction.RIGHT;
			move(wishDir);
		} else if (x > entry.x) {
			wishDir = moveDir = Direction.LEFT;
			move(wishDir);
		}
	}

	private void enterGhostHouse() {
		updateSpeed();
		Vector2 entry = game.world.ghostHouseEntry;
		if (y <= entry.y) {
			// start falling down
			x = entry.x;
			wishDir = moveDir = Direction.DOWN;
			move(wishDir);
		} else if (y <= entry.y + 3 * World.TS) {
			// reached bottom
			move(wishDir);
		} else if (id == INKY) {
			// go left
			wishDir = Direction.LEFT;
			move(wishDir);
			if (x <= entry.x - 2 * World.TS) {
				state = GhostState.LEAVING_HOUSE;
			}
		} else if (id == CLYDE) {
			// go right
			wishDir = Direction.RIGHT;
			move(wishDir);
			if (x >= entry.x + 2 * World.TS) {
				state = GhostState.LEAVING_HOUSE;
			}
		} else {
			state = GhostState.LEAVING_HOUSE;
		}
	}

	private void bounce() {
		if (id == BLINKY) {
			return;
		}
		if (y >= 18 * World.TS) {
			moveDir = wishDir = Direction.UP;
		} else if (y <= 17 * World.TS) {
			moveDir = wishDir = Direction.DOWN;
		}
		updateSpeed();
		move(wishDir);
	}

	private void steer() {
		if (targetTile == null) {
			return;
		}
		double min = Double.MAX_VALUE;
		for (Direction direction : DIR_ORDER) {
			if (direction == moveDir.opposite()) {
				continue;
			}
			Vector2 neighbor = tile().neighbor(direction);
			if (canEnter(neighbor)) {
				double dist = neighbor.dist(targetTile);
				if (dist < min) {
					min = dist;
					wishDir = direction;
				}
			}
		}
	}
}