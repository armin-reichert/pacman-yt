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

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class World {

	public static final int ROWS = 36;
	public static final int COLS = 28;

	public static final int TS = 8;
	public static final int HTS = 4;

	//@formatter:off
	public static final byte SPACE           = 0;
	public static final byte WALL            = 1;
	public static final byte TUNNEL          = 2;
	public static final byte PELLET          = 3;
	public static final byte ENERGIZER       = 4;
	public static final byte PELLET_EATEN    = 5;
	public static final byte ENERGIZER_EATEN = 6;
	//@formatter:on

	protected byte[][] map = {
		//@formatter:off
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,4,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,1,1,1,0,1,1,0,1,1,1,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,0,0,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{2,2,2,2,2,2,3,0,0,0,1,0,0,0,0,0,0,1,0,0,0,3,2,2,2,2,2,2,},
		{1,1,1,1,1,1,3,1,1,0,1,0,0,0,0,0,0,1,0,1,1,3,1,1,1,1,1,1,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,0,0,0,0,0,0,0,0,0,1,1,3,1,0,0,0,0,0,},
		{0,0,0,0,0,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,0,0,0,0,0,},
		{1,1,1,1,1,1,3,1,1,0,1,1,1,1,1,1,1,1,0,1,1,3,1,1,1,1,1,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,1,1,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,3,1,1,1,1,3,1,1,1,1,1,3,1,1,3,1,1,1,1,1,3,1,1,1,1,3,1,},
		{1,4,3,3,1,1,3,3,3,3,3,3,3,0,0,3,3,3,3,3,3,3,1,1,3,3,4,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,1,1,3,1,1,3,1,1,3,1,1,1,1,1,1,1,1,3,1,1,3,1,1,3,1,1,1,},
		{1,3,3,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,1,1,3,3,3,3,3,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,1,1,1,1,1,1,1,1,3,1,},
		{1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,},
		//@formatter:on
	};

	// home tiles
	public final Vector2 pacManHomeTile = v(13, 26);
	public final Vector2 blinkyHomeTile = v(13, 14);
	public final Vector2 pinkyHomeTile = v(13, 17);
	public final Vector2 inkyHomeTile = v(11, 17);
	public final Vector2 clydeHomeTile = v(15, 17);

	// scattering targets
	public final Vector2 leftUpperTarget = v(2, 0);
	public final Vector2 rightUpperTarget = v(25, 0);
	public final Vector2 leftLowerTarget = v(0, 34);
	public final Vector2 rightLowerTarget = v(27, 34);

	public final Vector2 bonusTile = v(13, 20);
	public final Vector2 houseEntryTile = v(13, 14); // left house entry tile
	public final Vector2 houseEntry = v(112, 116); // pixel position between both entry tiles
	public final float houseTop = 17 * World.TS;
	public final float houseBottom = 18 * World.TS;

	public int eatenFoodCount;

	private boolean inRange(int row, int col) {
		return 0 <= row && row < ROWS && 0 <= col && col < COLS;
	}

	public boolean isWaypoint(Vector2 tile) {
		int freeNeighbors = 0;
		for (Direction direction : Direction.values()) {
			if (!isBlocked(tile.neighbor(direction))) {
				freeNeighbors++;
			}
		}
		return freeNeighbors >= 3;
	}

	public boolean isBlocked(int row, int col) {
		return inRange(row, col) && map[row][col] == WALL;
	}

	public boolean isBlocked(Vector2 tile) {
		return isBlocked(tile.y, tile.x);
	}

	public boolean isTunnel(Vector2 tile) {
		return inRange(tile.y, tile.x) && map[tile.y][tile.x] == TUNNEL;
	}

	public boolean isGhostHouse(Vector2 tile) {
		return 10 <= tile.x && tile.x <= 17 && 15 <= tile.y && tile.y <= 19;
	}

	public boolean isOneWayDown(Vector2 tile) {
		return (tile.x == 12 && tile.y == 13) || (tile.x == 15 && tile.y == 13) || (tile.x == 12 && tile.y == 25)
				|| (tile.x == 15 && tile.y == 25);
	}

	public boolean isPellet(Vector2 tile) {
		return isPellet(tile.y, tile.x);
	}

	public boolean isPellet(int row, int col) {
		return inRange(row, col) && map[row][col] == PELLET;
	}

	public boolean isPowerPellet(Vector2 tile) {
		return isPowerPellet(tile.y, tile.x);
	}

	public boolean isPowerPellet(int row, int col) {
		return inRange(row, col) && map[row][col] == ENERGIZER;
	}

	public boolean isEatenPellet(int row, int col) {
		return inRange(row, col) && map[row][col] == PELLET_EATEN;
	}

	public boolean isEatenPowerPellet(int row, int col) {
		return inRange(row, col) && map[row][col] == ENERGIZER_EATEN;
	}

	public boolean eatPellet(Vector2 tile) {
		if (isPellet(tile)) {
			map[tile.y][tile.x] = PELLET_EATEN;
			eatenFoodCount++;
			return true;
		}
		return false;
	}

	public boolean eatPowerPellet(Vector2 tile) {
		if (isPowerPellet(tile)) {
			map[tile.y][tile.x] = ENERGIZER_EATEN;
			eatenFoodCount++;
			return true;
		}
		return false;
	}

	public boolean allFoodEaten() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				if (isPellet(row, col)) {
					return false;
				} else if (isPowerPellet(row, col)) {
					return false;
				}
			}
		}
		return true;
	}

	public void resetFood() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				if (isEatenPellet(row, col)) {
					map[row][col] = PELLET;
				} else if (isEatenPowerPellet(row, col)) {
					map[row][col] = ENERGIZER;
				}
			}
		}
		eatenFoodCount = 0;
	}
}