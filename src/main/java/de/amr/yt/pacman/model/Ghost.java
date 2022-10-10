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

import static de.amr.yt.pacman.lib.Direction.DOWN;
import static de.amr.yt.pacman.lib.Direction.LEFT;
import static de.amr.yt.pacman.lib.Direction.RIGHT;
import static de.amr.yt.pacman.lib.Direction.UP;
import static de.amr.yt.pacman.lib.Logging.log;
import static de.amr.yt.pacman.lib.Vector2.v;
import static de.amr.yt.pacman.model.World.t;

import de.amr.yt.pacman.lib.AnimationMap;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class Ghost extends Creature {

	public enum AnimationKey {
		WALKING, FRIGHTENED, BLINKING, DEAD, VALUE
	};

	public static final int BLINKY = 0, PINKY = 1, INKY = 2, CLYDE = 3;

	private static final Direction[] DIR_ORDER = { UP, LEFT, DOWN, RIGHT };

	public final int id;
	public final GameModel game;

	public GhostState state;
	/** Elroy state: 0=off, 1=Elroy1, 2=Elroy2, -1=Elroy1 disabled, -2=Elroy2 disabled */
	public int elroyState;
	public Vector2 targetTile;
	public long valueTimer;
	public int value;

	public final AnimationMap<AnimationKey> animations = new AnimationMap<>(AnimationKey.class);

	public Ghost(GameModel game, int id) {
		super(game.world);
		this.game = game;
		this.id = id;
		reset();
	}

	public void selectAnimation(AnimationKey key) {
		if (animations.select(key)) {
			log("Select animation '%s' for %s", animations.selected(), this);
		}
	}

	@Override
	public void reset() {
		super.reset();
		canReverse = false;
		state = GhostState.CHASING;
		elroyState = 0;
		targetTile = null;
		valueTimer = 0;
		value = 0;
	}

	public void update() {
		switch (state) {
		case LOCKED -> {
			if (id != BLINKY) {
				bounce(world.houseTop, world.houseBottom);
			}
			if (game.pacMan.hasPower()) {
				if (game.pacMan.isLosingPower()) {
					selectAnimation(AnimationKey.BLINKING);
				} else {
					selectAnimation(AnimationKey.FRIGHTENED);
				}
			} else {
				selectAnimation(AnimationKey.WALKING);
			}
		}
		case ENTERING_HOUSE -> {
			enterGhostHouse(world.houseEntry);
			selectAnimation(AnimationKey.DEAD);
		}
		case LEAVING_HOUSE -> {
			leaveGhostHouse(world.houseEntry);
			selectAnimation(AnimationKey.WALKING);
		}
		case CHASING, SCATTERING -> {
			aimTowardsTarget();
			selectAnimation(AnimationKey.WALKING);
		}
		case FRIGHTENED -> {
			aimTowardsTarget();
			if (game.pacMan.isLosingPower()) {
				selectAnimation(AnimationKey.BLINKING);
			} else {
				selectAnimation(AnimationKey.FRIGHTENED);
			}
		}
		case EATEN -> {
			returnToGhostHouse(world.houseEntry);
			if (valueTimer > 0) {
				selectAnimation(AnimationKey.VALUE);
			} else {
				selectAnimation(AnimationKey.DEAD);
			}
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + state);
		}
		animations.selected().tick();
	}

	@Override
	public String toString() {
		return "Ghost[id=%d, state=%s, x=%.2f, y=%.2f, tile=%s, target=%s, moveDir=%s, wishDir=%s speed=%.2f]".formatted(id,
				state, x, y, tile(), targetTile, moveDir, wishDir, speed);
	}

	@Override
	public boolean canEnterTile(Vector2 tile) {
		if (world.isBlocked(tile)) {
			return false;
		}
		if (world.isGhostHouse(tile)) {
			return state == GhostState.ENTERING_HOUSE || state == GhostState.LEAVING_HOUSE || state == GhostState.LOCKED;
		}
		if (wishDir == UP && world.isOneWayDown(tile)) {
			return state == GhostState.FRIGHTENED || state == GhostState.EATEN;
		}
		return true;
	}

	@Override
	public float currentSpeed() {
		boolean tunnel = game.world.isTunnel(tile());
		return switch (state) {
		case CHASING, SCATTERING -> tunnel ? game.level.ghostSpeedTunnel : chasingOrScatteringSpeed();
		case EATEN -> 2 * game.level.ghostSpeed; // TODO guess
		case ENTERING_HOUSE -> 2 * game.level.ghostSpeed;// TODO guess
		case FRIGHTENED -> tunnel ? game.level.ghostSpeedTunnel : game.level.ghostSpeedFrightened;
		case LEAVING_HOUSE -> 0.4f * GameModel.BASE_SPEED;// TODO guess
		case LOCKED -> 0.4f * GameModel.BASE_SPEED;// TODO guess
		};
	}

	private void aimTowardsTarget() {
		if (enteredNewTile) {
			targetTile = targetTile();
			if (targetTile != null) {
				takeDirectionTowardsTarget();
			}
		}
		exploreWorld();
	}

	private void takeDirectionTowardsTarget() {
		double minDist = Double.MAX_VALUE;
		for (Direction direction : DIR_ORDER) {
			if (direction == moveDir.opposite()) {
				continue;
			}
			Vector2 neighbor = tile().neighbor(direction);
			if (canEnterTile(neighbor)) {
				double dist = neighbor.euclideanDist(targetTile);
				if (dist < minDist) {
					minDist = dist;
					wishDir = direction;
				}
			}
		}
	}

	private Vector2 targetTile() {
		return switch (state) {
		case CHASING -> chasingTargetTile();
		case EATEN -> world.houseEntryTile;
		case FRIGHTENED -> randomAllowedNeighborTile();
		case SCATTERING -> scatteringTargetTile();
		default -> null;
		};
	}

	private float chasingOrScatteringSpeed() {
		return switch (elroyState) {
		case 1 -> game.level.elroy1Speed;
		case 2 -> game.level.elroy2Speed;
		default -> game.level.ghostSpeed;
		};
	}

	private Vector2 scatteringTargetTile() {
		return switch (elroyState) {
		case 1, 2 -> chasingTargetTile();
		default -> game.ghostScatterTargets[id];
		};
	}

	private Vector2 randomAllowedNeighborTile() {
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

	private Vector2 chasingTargetTile() {
		var pacMan = game.pacMan;
		return switch (id) {
		case BLINKY -> pacMan.tile();
		case PINKY -> {
			Vector2 pacPlus4 = pacMan.tile().plus(pacMan.moveDir.vector.times(4));
			if (pacMan.moveDir == UP) { // simulate overflow bug from Arcade game
				pacPlus4 = pacPlus4.plus(v(-4, 0));
			}
			yield pacPlus4;
		}
		case INKY -> {
			Vector2 pacPlus2 = pacMan.tile().plus(pacMan.moveDir.vector.times(2));
			if (pacMan.moveDir == UP) { // simulate overflow bug from Arcade game
				pacPlus2 = pacPlus2.plus(v(-2, 0));
			}
			yield pacPlus2.times(2).minus(game.ghosts[BLINKY].tile());
		}
		case CLYDE -> tile().euclideanDist(pacMan.tile()) < 8 ? game.ghostScatterTargets[CLYDE] : pacMan.tile();
		default -> null;
		};
	}

	private void leaveGhostHouse(Vector2 houseEntry) {
		if (y <= houseEntry.y) {
			// out of house
			y = houseEntry.y;
			wishDir = LEFT;
			state = game.chasingPhase ? GhostState.CHASING : GhostState.SCATTERING;
			return;
		}
		if (about(x, houseEntry.x, 1)) {
			// at "lift" position
			x = houseEntry.x;
			wishDir = moveDir = UP;
		} else if (x < houseEntry.x) {
			// left of "lift" position
			wishDir = moveDir = RIGHT;
		} else if (x > houseEntry.x) {
			// right of "lift" position
			wishDir = moveDir = LEFT;
		}
		speed = currentSpeed();
		move(wishDir);
	}

	private void enterGhostHouse(Vector2 houseEntry) {
		speed = currentSpeed();
		if (about(x, houseEntry.x, 1) && y == houseEntry.y) {
			// enter "lift" downwards
			x = houseEntry.x;
			wishDir = moveDir = DOWN;
			move(wishDir);
		} else if (y <= houseEntry.y + t(3)) {
			// "lift" moves down
			move(wishDir);
		} else if (ghostHousePosition() == LEFT) {
			if (x <= houseEntry.x - t(2)) {
				// reached left home position
				state = GhostState.LEAVING_HOUSE;
			} else {
				// move left towards home position
				wishDir = LEFT;
				move(wishDir);
			}
		} else if (ghostHousePosition() == RIGHT) {
			if (x >= houseEntry.x + t(2)) {
				// reached right home position
				state = GhostState.LEAVING_HOUSE;
			} else {
				// move right towards home position
				wishDir = RIGHT;
				move(wishDir);
			}
		} else {
			// reached center home position
			state = GhostState.LEAVING_HOUSE;
		}
	}

	private Direction ghostHousePosition() {
		return switch (id) {
		case INKY -> LEFT;
		case CLYDE -> RIGHT;
		default -> null; // CENTER
		};
	}

	private void returnToGhostHouse(Vector2 entry) {
		if (about(x, entry.x, 1) && y == entry.y) {
			state = GhostState.ENTERING_HOUSE;
		} else {
			if (valueTimer > 0) {
				// don't move while displaying ghost value
				--valueTimer;
			} else {
				aimTowardsTarget();
			}
		}
	}

	private void bounce(float top, float bottom) {
		if (y >= bottom) {
			moveDir = wishDir = UP;
		} else if (y <= top) {
			moveDir = wishDir = DOWN;
		}
		speed = currentSpeed();
		move(wishDir);
	}
}