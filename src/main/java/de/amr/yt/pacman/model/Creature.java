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

	/**
	 * @param value      some value
	 * @param exactValue the exact value
	 * @param delta      the allowed difference
	 * @return {@code true} if the value differs at most by delta from the exact value
	 */
	public static boolean about(float value, float exactValue, float delta) {
		return Math.abs(value - exactValue) <= delta;
	}

	public final World world;
	public float x = Float.MIN_VALUE;
	public float y = Float.MIN_VALUE;
	public float speed = 0;
	public boolean enteredNewTile = false;
	public boolean canReverse = false;
	public boolean stuck = false;
	public boolean animated = false;
	public int animFrame = 0;
	public boolean visible = true;
	public Direction moveDir = Direction.LEFT;
	public Direction wishDir = Direction.LEFT;

	protected Creature(World world) {
		this.world = world;
	}

	/**
	 * @param tile some tile
	 * @return Tells if this creature can enter the given tile in the current situation. This may depend on the creature's
	 *         (e.g. ghost's) current state, location etc. or on the current game state.
	 */
	protected abstract boolean canEnterTile(Vector2 tile);

	/**
	 * @return the current speed (in pixels per frame)
	 */
	protected abstract float currentSpeed();

	public void placeAtTile(Vector2 tile, float offset_x, float offset_y) {
		x = tile.x * World.TS + World.HTS + offset_x;
		y = tile.y * World.TS + World.HTS + offset_y;
	}

	public Vector2 tile() {
		return tile(x, y);
	}

	public Vector2 tile(float x, float y) {
		return new Vector2(col(), row());
	}

	public int col() {
		return (int) (x / World.TS);
	}

	public int row() {
		return (int) (y / World.TS);
	}

	public float offsetX() {
		return x - col() * World.TS;
	}

	public float offsetY() {
		return y - row() * World.TS;
	}

	public float centerX() {
		return col() * World.TS + World.HTS;
	}

	public float centerY() {
		return row() * World.TS + World.HTS;
	}

	public void reverse() {
		wishDir = moveDir.opposite();
		if (tryMove(moveDir, wishDir)) {
			moveDir = wishDir;
		}
		enteredNewTile = true;
	}

	public void moveThroughWorld() {
		// teleport?
		if (x > World.COLS * World.TS + World.HTS) {
			x = 0;
			return;
		}
		if (x < 0) {
			x = World.COLS * World.TS + World.HTS;
			return;
		}

		var tileBeforeMove = tile();
		speed = currentSpeed();
		stuck = false;
		boolean couldMove = false;
		if (canReverse || wishDir != moveDir.opposite()) {
			couldMove = tryMove(moveDir, wishDir);
		}
		if (couldMove) {
			moveDir = wishDir;
		} else {
			couldMove = tryMove(moveDir, moveDir);
			if (!couldMove) {
				x = centerX();
				y = centerY();
				stuck = true;
			}
		}
		enteredNewTile = !tileBeforeMove.equals(tile());
	}

	public void move(Direction dir) {
		x += dir.vector.x * speed;
		y += dir.vector.y * speed;
	}

	protected boolean tryMove(Direction currentDir, Direction newDir) {
		boolean canMove = false;
		var tile = tile();
		if (tile.x < 0 || tile.x >= World.COLS) {
			// when teleporting, we cannot move sidewards
			canMove = currentDir == newDir || currentDir == newDir.opposite();
		} else if (canEnterTile(tile.neighbor(newDir))) {
			// check if accessible neighbor tile can be entered now
			if (newDir == currentDir || newDir == currentDir.opposite()) {
				canMove = true;
			} else {
				// can we move sidewards into accessible neighbor?
				canMove = canTurnSidewards(currentDir);
				if (canMove) {
					// align exactly inside tile
					x = centerX();
					y = centerY();
				}
			}
		} else {
			// check if we can move further towards an inaccessible neighbor tile
			canMove = switch (newDir) {
			case UP -> newDir.vector.y * speed + offsetY() > World.HTS;
			case DOWN -> newDir.vector.y * speed + offsetY() < World.HTS;
			case LEFT -> newDir.vector.x * speed + offsetX() > World.HTS;
			case RIGHT -> newDir.vector.x * speed + offsetX() < World.HTS;
			};
		}
		if (canMove) {
			move(newDir);
		}
		return canMove;
	}

	protected boolean canTurnSidewards(Direction direction) {
		return direction.isVertical() ? Math.abs(offsetY() - World.HTS) <= 1 : Math.abs(offsetX() - World.HTS) <= 1;
	}
}