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

import static de.amr.yt.pacman.lib.Logging.log;

import de.amr.yt.pacman.lib.Animation;
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

	public World world;
	public float x;
	public float y;
	public float speed;
	public boolean enteredNewTile;
	public boolean canReverse;
	public boolean reverseDirection;
	public boolean stuck;
	public boolean visible;
	public Direction moveDir;
	public Direction wishDir;

	private Animation<?> animation;

	protected Creature(World world) {
		this.world = world;
		x = Float.MIN_VALUE;
		y = Float.MIN_VALUE;
		speed = 0;
		enteredNewTile = false;
		canReverse = false;
		reverseDirection = false;
		stuck = false;
		visible = true;
		moveDir = Direction.LEFT;
		wishDir = Direction.LEFT;
	}

	public Animation<?> animation() {
		return animation;
	}

	protected void setAnimation(Animation<?> animation) {
		if (this.animation != animation) {
			this.animation = animation;
			animation.reset();
			log("Animation set to '%s' for %s", animation, this);
		}
	}

	public void reset() {
		x = Float.MIN_VALUE;
		y = Float.MIN_VALUE;
		speed = 0;
		enteredNewTile = false;
		canReverse = false;
		stuck = false;
		visible = true;
		moveDir = Direction.LEFT;
		wishDir = Direction.LEFT;
	}

	/**
	 * @param tile some tile
	 * @return Tells if this creature can enter the given tile in the current situation. This may depend on the creature's
	 *         (e.g. ghost's) current state, location etc. or on the current game state.
	 */
	public abstract boolean canEnterTile(Vector2 tile);

	/**
	 * @return the current speed (in pixels per frame)
	 */
	public abstract float currentSpeed();

	public void placeAtTile(Vector2 tile, float offset_x, float offset_y) {
		x = tile.x * World.TS + World.HT + offset_x;
		y = tile.y * World.TS + World.HT + offset_y;
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
		return col() * World.TS + World.HT;
	}

	public float centerY() {
		return row() * World.TS + World.HT;
	}

	private void reverse() {
		wishDir = moveDir.opposite();
		if (tryMove(moveDir, wishDir)) {
			moveDir = wishDir;
		}
		enteredNewTile = true;
		reverseDirection = false;
	}

	public void exploreWorld() {
		if (reverseDirection && enteredNewTile) {
			reverse();
			return;
		}

		var tileBeforeMoving = tile();

		if (x > World.COLS * World.TS + World.HT) {
			// traverse portal on right edge of world
			x = 0;
			enteredNewTile = true;
			return;
		}

		if (x < 0) {
			// traverse portal on left edge of world
			x = World.COLS * World.TS + World.HT;
			enteredNewTile = true;
			return;
		}

		speed = currentSpeed();
		stuck = false;
		boolean moved = false;

		// first try moving towards the current wish direction
		if (canReverse || wishDir != moveDir.opposite()) {
			moved = tryMove(moveDir, wishDir);
		}

		if (moved) {
			// make the wish direction the new move direction
			moveDir = wishDir;
		} else {
			// moving towards the wish direction was not possible, try moving towards the current move direction
			moved = tryMove(moveDir, moveDir);
			if (!moved) {
				// we got stuck, center exactly on current tile
				stuck = true;
				x = centerX();
				y = centerY();
			}
		}
		enteredNewTile = !tile().equals(tileBeforeMoving);
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
			case UP -> newDir.vector.y * speed + offsetY() > World.HT;
			case DOWN -> newDir.vector.y * speed + offsetY() < World.HT;
			case LEFT -> newDir.vector.x * speed + offsetX() > World.HT;
			case RIGHT -> newDir.vector.x * speed + offsetX() < World.HT;
			};
		}
		if (canMove) {
			move(newDir);
		}
		return canMove;
	}

	protected boolean canTurnSidewards(Direction direction) {
		return direction.isVertical() ? Math.abs(offsetY() - World.HT) <= 1 : Math.abs(offsetX() - World.HT) <= 1;
	}
}