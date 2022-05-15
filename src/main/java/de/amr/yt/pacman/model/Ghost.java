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

import static de.amr.yt.pacman.lib.Vector2.v;
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;
import static de.amr.yt.pacman.model.World.t;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	static final Direction[] DIR_ORDER = { //
			Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT };

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
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		canReverse = false;
		state = GhostState.CHASING;
		targetTile = null;
		eatenTimer = 0;
		eatenValue = 0;
	}

	@Override
	public String toString() {
		return "Ghost[id=%d, x=%.2f, y=%.2f, tile=%s, moveDir=%s, wishDir=%s speed=%.2f]".formatted(id, x, y, tile(),
				moveDir, wishDir, speed);
	}

	@Override
	protected boolean canEnterTile(Vector2 tile) {
		if (world.isBlocked(tile)) {
			return false;
		}
		if (world.isGhostHouse(tile)) {
			return state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE;
		}
		if (wishDir == Direction.UP && world.isOneWayDown(tile)) {
			return state == GhostState.FRIGHTENED || state == GhostState.EATEN;
		}
		return true;
	}

	public void update() {
		switch (state) {
		case LOCKED -> {
			if (id != BLINKY)
				bounce(world.houseBottom, world.houseTop);
		}
		case ENTERING_HOUSE -> enterGhostHouse(world.houseEntry);
		case LEAVING_HOUSE -> leaveGhostHouse(world.houseEntry);
		case CHASING, SCATTERING, FRIGHTENED -> aimTowardsTarget();
		case EATEN -> returnToGhostHouse(world.houseEntry);
		}
	}

	private void aimTowardsTarget() {
		if (enteredNewTile) {
			targetTile = computeTargetTile();
			takeDirectionTowardsTarget();
		}
		moveThroughWorld();
	}

	private void takeDirectionTowardsTarget() {
		if (targetTile == null) {
			return;
		}
		double minDist = Double.MAX_VALUE;
		for (Direction direction : DIR_ORDER) {
			if (direction == moveDir.opposite()) {
				continue;
			}
			Vector2 neighbor = tile().neighbor(direction);
			if (canEnterTile(neighbor)) {
				double dist = neighbor.dist(targetTile);
				if (dist < minDist) {
					minDist = dist;
					wishDir = direction;
				}
			}
		}
	}

	@Override
	protected float currentSpeed() {
		// TODO: some speed values are just guesses
		if (state != GhostState.EATEN && game.world.isTunnel(tile())) {
			return game.ghostSpeedTunnel;
		} else {
			return switch (state) {
			case LOCKED, LEAVING_HOUSE -> 0.33f * GameModel.BASE_SPEED;
			case ENTERING_HOUSE, EATEN -> eatenTimer > 0 ? 0 : 2 * game.ghostSpeed;
			case FRIGHTENED -> game.ghostSpeedFrightened;
			case CHASING, SCATTERING -> game.ghostSpeed;
			};
		}
	}

	private Vector2 computeTargetTile() {
		return switch (state) {
		case LOCKED, ENTERING_HOUSE, LEAVING_HOUSE -> null;
		case EATEN -> world.houseEntryTile;
		case FRIGHTENED -> computeRandomNeighborTile();
		case SCATTERING -> computeScatteringTargetTile();
		case CHASING -> computeChasingTargetTile();
		};
	}

	private Vector2 computeRandomNeighborTile() {
		for (Direction direction : Direction.valuesShuffled()) {
			if (direction == moveDir.opposite()) {
				continue;
			}
			Vector2 neighbor = tile().neighbor(direction);
			if (canEnterTile(neighbor)) {
				return neighbor;
			}
		}
		return null;
	}

	private Vector2 computeScatteringTargetTile() {
		return switch (id) {
		case BLINKY -> world.rightUpperTarget;
		case PINKY -> world.leftUpperTarget;
		case INKY -> world.rightLowerTarget;
		case CLYDE -> world.leftLowerTarget;
		default -> null;
		};
	}

	private Vector2 computeChasingTargetTile() {
		return switch (id) {
		case BLINKY -> game.pacMan.tile();
		case PINKY -> {
			Vector2 pacPlus4 = game.pacMan.tile().plus(game.pacMan.moveDir.vector.times(4));
			if (game.pacMan.moveDir == Direction.UP) {
				// simulate overflow bug from Arcade game
				pacPlus4 = pacPlus4.plus(v(-4, 0));
			}
			yield pacPlus4;
		}
		case INKY -> {
			Vector2 pacPlus2 = game.pacMan.tile().plus(game.pacMan.moveDir.vector.times(2));
			if (game.pacMan.moveDir == Direction.UP) {
				// simulate overflow bug from Arcade game
				pacPlus2 = pacPlus2.plus(v(-2, 0));
			}
			yield pacPlus2.times(2).minus(game.ghosts[BLINKY].tile());
		}
		case CLYDE -> tile().dist(game.pacMan.tile()) < 8 ? world.leftLowerTarget : game.pacMan.tile();
		default -> null;
		};
	}

	private void leaveGhostHouse(Vector2 entry) {
		if (y <= entry.y) { // out of house
			y = entry.y;
			wishDir = Direction.LEFT;
			state = game.chasingPhase ? GhostState.CHASING : GhostState.SCATTERING;
			return;
		}
		if (about(x, entry.x, 1)) {
			x = entry.x;
			wishDir = moveDir = Direction.UP;
		} else if (x < entry.x) {
			wishDir = moveDir = Direction.RIGHT;
		} else if (x > entry.x) {
			wishDir = moveDir = Direction.LEFT;
		}
		speed = currentSpeed();
		move(wishDir);
	}

	private void enterGhostHouse(Vector2 entry) {
		speed = currentSpeed();
		if (y == entry.y) {
			// start falling down
			x = entry.x;
			wishDir = moveDir = Direction.DOWN;
			move(wishDir);
		} else if (y <= entry.y + t(3)) {
			// keep falling
			move(wishDir);
		} else if (ghostHousePosition() == Direction.LEFT) {
			// move left
			if (x <= entry.x - t(2)) {
				// reached left seat
				state = GhostState.LEAVING_HOUSE;
			} else {
				wishDir = Direction.LEFT;
				move(wishDir);
			}
		} else if (ghostHousePosition() == Direction.RIGHT) {
			// move right
			if (x >= entry.x + t(2)) {
				// reached right seat
				state = GhostState.LEAVING_HOUSE;
			} else {
				wishDir = Direction.RIGHT;
				move(wishDir);
			}
		} else {
			state = GhostState.LEAVING_HOUSE;
		}
	}

	private Direction ghostHousePosition() {
		return switch (id) {
		case INKY -> Direction.LEFT;
		case CLYDE -> Direction.RIGHT;
		default -> null;
		};
	}

	private void returnToGhostHouse(Vector2 entry) {
		if (about(x, entry.x, 1) && y == entry.y) {
			state = GhostState.ENTERING_HOUSE;
		} else {
			aimTowardsTarget();
			if (eatenTimer > 0) {
				--eatenTimer;
			}
		}
	}

	private void bounce(float bottomY, float topY) {
		if (y >= bottomY) {
			moveDir = wishDir = Direction.UP;
		} else if (y <= topY) {
			moveDir = wishDir = Direction.DOWN;
		}
		speed = currentSpeed();
		move(wishDir);
	}
}