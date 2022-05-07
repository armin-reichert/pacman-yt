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
	public GhostState state;
	public Vector2 targetTile;
	public long eatenTimer;
	public int eatenValue;

	public Ghost(int id) {
		this.id = id;
		canReverse = false;
	}

	public void update(GameModel game) {

		switch (state) {
		case LEAVING_HOUSE -> leaveGhostHouse(game);
		case CHASING -> {
			moveThroughMaze(game);
		}
		case SCATTERING -> {
			moveThroughMaze(game);
		}
		case FRIGHTENED -> {
			moveThroughMaze(game);
		}
		case EATEN -> {
			moveThroughMaze(game);
			if (eatenTimer > 0) {
				--eatenTimer;
			}
			if (tile().equals(game.world.ghostHouseEntryTile)) {
				state = game.chasing ? GhostState.CHASING : GhostState.SCATTERING;
			}
		}
		default -> {
		}
		}
	}

	private void moveThroughMaze(GameModel game) {
		if (enteredNewTile) {
			computeTargetTile(game);
			steer(game.world);
		}
		updateSpeed(game);
		move(game.world);
	}

	private void updateSpeed(GameModel game) {
		if (state == GhostState.EATEN) {
			speed = eatenTimer > 0 ? 0 : 2 * game.ghostSpeed; // TODO correct?
		} else if (game.world.isTunnel(tile())) {
			speed = game.ghostSpeedTunnel;
		} else if (state == GhostState.FRIGHTENED) {
			speed = game.ghostSpeedFrightened;
		} else {
			speed = game.ghostSpeed;
		}
	}

	@Override
	protected boolean canEnter(World world, Vector2 tile) {
		if (world.isBlocked(tile)) {
			return false;
		}
		if (world.isGhostHouse(tile)) {
			return state == GhostState.EATEN;
		}
		if (state != GhostState.FRIGHTENED && wishDir == Direction.UP && world.isOneWayDown(tile)) {
			return false;
		}
		return true;
	}

	private void computeTargetTile(GameModel game) {
		switch (state) {

		case LEAVING_HOUSE -> {
			// TODO
		}

		case EATEN -> {
			targetTile = new Vector2(13, 14);
		}

		case FRIGHTENED -> {
			List<Direction> directions = Arrays.asList(Direction.values());
			Collections.shuffle(directions);
			for (Direction direction : directions) {
				Vector2 neighbor = tile().neighbor(direction);
				if (canEnter(game.world, neighbor)) {
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

	// TODO real implementation
	private void leaveGhostHouse(GameModel game) {
		placeAtTile(13, 14, World.HTS, 0);
		wishDir = Direction.LEFT;
		enteredNewTile = true;
		state = game.chasing ? GhostState.CHASING : GhostState.SCATTERING;
	}

	private void steer(World world) {
		Vector2 tile = tile();
		if (targetTile == null) {
			return;
		}
		double min = Double.MAX_VALUE;
		for (Direction direction : DIR_ORDER) {
			if (direction == moveDir.opposite()) {
				continue;
			}
			Vector2 neighbor = tile.neighbor(direction);
			if (!canEnter(world, neighbor)) {
				continue;
			}
			double dist = neighbor.dist(targetTile);
			if (dist < min) {
				min = dist;
				wishDir = direction;
			}
		}
	}

	@Override
	public String toString() {
		return String.format("Ghost[id=%d, x=%.2f, y=%.2f, moveDir=%s, wishDir=%s speed=%.2f]", id, x, y, moveDir, wishDir,
				speed);
	}
}