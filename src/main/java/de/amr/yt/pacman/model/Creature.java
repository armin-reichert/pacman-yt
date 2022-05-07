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
				x = centerX();
				y = centerY();
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
			x += newDir.vector.x * speed;
			y += newDir.vector.y * speed;
		}
		return canMove;
	}

	protected boolean canMove(World world, Direction currentDir, Direction newDir) {
		Vector2 tile = tile();
		if (tile.x < 0 || tile.x >= World.COLS) {
			// no sidewards turn in teleport tunnel
			return currentDir == newDir || currentDir == newDir.opposite();
		}
		if (canEnter(world, tile.neighbor(newDir))) {
			if (newDir == currentDir || newDir == currentDir.opposite()) {
				return true;
			}
			boolean canTurn90 = canTurn90Degrees(currentDir);
			if (canTurn90) {
				x = centerX();
				y = centerY();
			}
			return canTurn90;
		} else {
			switch (newDir) {
			case UP -> {
				return newDir.vector.y * speed + offsetY() > World.HTS;
			}
			case DOWN -> {
				return newDir.vector.y * speed + offsetY() < World.HTS;
			}
			case LEFT -> {
				return newDir.vector.x * speed + offsetX() > World.HTS;
			}
			case RIGHT -> {
				return newDir.vector.x * speed + offsetX() < World.HTS;
			}
			}
		}
		return true;
	}

	protected boolean canTurn90Degrees(Direction direction) {
		return direction.isVertical() ? Math.abs(offsetY() - World.HTS) <= 1 : Math.abs(offsetX() - World.HTS) <= 1;
	}
}