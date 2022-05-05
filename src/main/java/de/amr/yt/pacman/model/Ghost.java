package de.amr.yt.pacman.model;

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
	public Vector2 target;

	public GhostState state;

	public long eatenTimer;
	public int eatenValue;

	public float normalSpeed = 1.0f;
	public float frightenedSpeed = 0.55f;
	public float eatenSpeed = 2.0f;
	public float tunnelSpeed = 0.5f;

	public Ghost(int id) {
		this.id = id;
		canReverse = false;
	}

	public void update(GameModel game) {
		Vector2 tile = tile();
		if (game.world.isGhostHouse(tile)) {
			leaveGhostHouse();
			if (game.chasing) {
				state = GhostState.CHASING;
			} else {
				state = GhostState.SCATTERING;
			}
		} else {
			if (enteredNewTile) {
				computeTargetTile(game);
				steer(game.world);
			}
			updateSpeed(game);
			move(game.world);
		}
		if (eatenTimer > 0) {
			--eatenTimer;
		}
	}

	private void updateSpeed(GameModel game) {
		if (state == GhostState.EATEN) {
			speed = eatenTimer > 0 ? 0 : eatenSpeed;
		} else if (game.world.isTunnel(tile())) {
			speed = tunnelSpeed;
		} else if (state == GhostState.FRIGHTENED) {
			speed = frightenedSpeed;
		} else {
			speed = normalSpeed;
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
		if (state != GhostState.FRIGHTENED && wishDir == Direction.UP && isOneWayDown(tile)) {
			return false;
		}
		return true;
	}

	private boolean isOneWayDown(Vector2 tile) {
		return (tile.x == 12 && tile.y == 13 || tile.x == 15 && tile.y == 13 || tile.x == 12 && tile.y == 25
				|| tile.x == 15 && tile.y == 25);
	}

	private void computeTargetTile(GameModel game) {
		if (state == GhostState.EATEN) {
			target = new Vector2(13, 14);
			return;
		}

		if (state == GhostState.FRIGHTENED) {
			List<Direction> directions = Arrays.asList(Direction.values());
			Collections.shuffle(directions);
			for (Direction direction : directions) {
				Vector2 neighbor = tile().neighbor(direction);
				if (canEnter(game.world, neighbor)) {
					target = neighbor;
					return;
				}
			}
		}

		if (state == GhostState.SCATTERING) {
			target = switch (id) {
			case GameModel.BLINKY -> new Vector2(25, 0);
			case GameModel.PINKY -> new Vector2(2, 0);
			case GameModel.INKY -> new Vector2(27, 34);
			case GameModel.CLYDE -> new Vector2(0, 34);
			default -> null;
			};
			return;
		}

		// state == GhostState.CHASING:
		var pacTile = game.pac.tile();
		var moveDir = game.pac.moveDir;

		switch (id) {
		case GameModel.BLINKY -> {
			target = pacTile;
		}
		case GameModel.PINKY -> {
			Vector2 pacPlus4 = pacTile.plus(moveDir.vector.times(4));
			if (moveDir == Direction.UP) {
				pacPlus4 = pacPlus4.plus(new Vector2(-4, 0));
			}
			target = pacPlus4;
		}
		case GameModel.INKY -> {
			Vector2 pacPlus2 = pacTile.plus(moveDir.vector.times(2));
			if (moveDir == Direction.UP) {
				pacPlus2 = pacPlus2.plus(new Vector2(-2, 0));
			}
			Vector2 blinkyTile = game.ghosts[GameModel.BLINKY].tile();
			target = pacPlus2.times(2).minus(blinkyTile);
		}
		case GameModel.CLYDE -> {
			target = tile().dist(pacTile) < 8 ? new Vector2(0, 34) : pacTile;
		}
		}
	}

	private void leaveGhostHouse() {
		// TODO
		placeAtTile(13, 14, World.HTS, 0);
		wishDir = Direction.LEFT;
		enteredNewTile = true;
	}

	private void steer(World world) {
		Vector2 tile = tile();
		if (target == null) {
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
			double dist = neighbor.dist(target);
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