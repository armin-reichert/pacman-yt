package de.amr.yt.pacman.model;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public abstract class Creature {

	public float x;
	public float y;
	public float speed;
	public boolean enteredNewTile;
	public boolean canReverse;
	public boolean stuck;
	public boolean animated;
	public int animFrame;
	public boolean visible;
	public Direction moveDir;
	public Direction wishDir;

	public Creature() {
		wishDir = moveDir = Direction.LEFT;
		animated = true;
		visible = true;
	}

	public void placeAtTile(int tile_x, int tile_y) {
		placeAtTile(tile_x, tile_y, 0, 0);
	}

	public void placeAtTile(int tile_x, int tile_y, float offset_x, float offset_y) {
		x = tile_x * World.TS + World.HTS + offset_x;
		y = tile_y * World.TS + World.HTS + offset_y;
	}

	public Vector2 tile() {
		return tile(x, y);
	}

	public Vector2 tile(float x, float y) {
		return new Vector2(tileX(), tileY());
	}

	private int tileX() {
		return (int) (x / World.TS);
	}

	private int tileY() {
		return (int) (y / World.TS);
	}

	public float offsetX() {
		return x - tileX() * World.TS;
	}

	public float offsetY() {
		return y - tileY() * World.TS;
	}

	public float centerX() {
		return tileX() * World.TS + World.HTS;
	}

	public float centerY() {
		return tileY() * World.TS + World.HTS;
	}

	public void move(World world) {
		stuck = false;
		Vector2 tile = tile();
		boolean success = false;
		if (wishDir != moveDir.opposite() || canReverse) {
			success = tryMove(world, moveDir, wishDir);
		}
		if (!success) {
			success = tryMove(world, moveDir, moveDir);
			if (!success) {
				align(Direction.LEFT);
				align(Direction.UP);
				stuck = true;
			}
		} else {
			moveDir = wishDir;
		}
		// teleport
		if (x > World.COLS * World.TS + World.HTS) {
			x = 0;
		} else if (x < 0) {
			x = World.COLS * World.TS + World.HTS;
		}
		enteredNewTile = !tile.equals(tile());
//		Logging.log("%s", this);
	}

	protected abstract boolean canEnter(World world, Vector2 tile);

	protected boolean tryMove(World world, Direction currentDir, Direction newDir) {
		boolean canMove = canMove(world, currentDir, newDir);
		if (canMove) {
			move(newDir);
		}
		return canMove;
	}

	protected void move(Direction direction) {
		x += direction.vector.x * speed;
		y += direction.vector.y * speed;
	}

	protected void align(Direction direction) {
		if (direction.isHorizontal()) {
			y = centerY();
		} else {
			x = centerX();
		}
	}

	protected boolean canMove(World world, Direction currentDir, Direction newDir) {
		Vector2 tile = tile();
		if (tile.x < 0 || tile.x >= World.COLS) {
			// no sidewards turn in teleport tunnel
			return currentDir == newDir || currentDir == newDir.opposite();
		}
		if (canEnter(world, tile.neighbor(newDir))) {
			if (currentDir == newDir || currentDir == newDir.opposite()) {
				return true;
			}
			boolean turn = canTurn90Degrees(currentDir);
			if (turn) {
				align(currentDir);
				align(newDir);
			}
			return turn;
		}
		float vx = newDir.vector.x * speed;
		float vy = newDir.vector.y * speed;
		switch (newDir) {
		case UP -> {
			return vy + offsetY() > World.HTS;
		}
		case DOWN -> {
			return vy + offsetY() < World.HTS;
		}
		case LEFT -> {
			return vx + offsetX() > World.HTS;
		}
		case RIGHT -> {
			return vx + offsetX() < World.HTS;
		}
		}
		return true;
	}

	protected boolean canTurn90Degrees(Direction direction) {
		return direction.isVertical() ? Math.abs(offsetY() - World.HTS) <= 1 : Math.abs(offsetX() - World.HTS) <= 1;
	}
}