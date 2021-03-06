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
	public static final int HT = 4;

	//@formatter:off
	public static final byte SPACE           = 0;
	public static final byte WALL            = 1;
	public static final byte TUNNEL          = 2;
	public static final byte PELLET          = 3;
	public static final byte ENERGIZER       = 4;
	public static final byte PELLET_EATEN    = 5;
	public static final byte ENERGIZER_EATEN = 6;
	//@formatter:on

	public static int t(int n) {
		return n * TS;
	}

	private byte[][] map = {
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

	/** Left house entry tile */
	public final Vector2 houseEntryTile = v(13, 14);
	/** Pixel position of house entry */
	public final Vector2 houseEntry = v(t(14), t(14) + World.HT);
	public final float houseTop = t(17);
	public final float houseBottom = t(18);

	public final int totalFoodCount;
	public int eatenFoodCount;

	public World() {
		int count = 0;
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				if (hasUneatenPelletAt(row, col) || hasUneatenPowerPelletAt(row, col)) {
					++count;
				}
			}
		}
		totalFoodCount = count;
	}

	private boolean inRange(int begin, int end, int value) {
		return begin <= value && value <= end;
	}

	private boolean inMapRange(int row, int col) {
		return inRange(0, ROWS - 1, row) && inRange(0, COLS - 1, col);
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
		return inMapRange(row, col) && map[row][col] == WALL;
	}

	public boolean isBlocked(Vector2 tile) {
		return isBlocked(tile.y, tile.x);
	}

	public boolean isTunnel(Vector2 tile) {
		return inMapRange(tile.y, tile.x) && map[tile.y][tile.x] == TUNNEL;
	}

	public boolean isGhostHouse(Vector2 tile) {
		return inRange(10, 17, tile.x) && inRange(15, 19, tile.y);
	}

	public boolean isOneWayDown(Vector2 tile) {
		return tile.equals(12, 13) || tile.equals(15, 13) || tile.equals(12, 25) || tile.equals(15, 25);
	}

	public boolean hasUneatenPelletAt(Vector2 tile) {
		return hasUneatenPelletAt(tile.y, tile.x);
	}

	public boolean hasUneatenPelletAt(int row, int col) {
		return inMapRange(row, col) && map[row][col] == PELLET;
	}

	public boolean hasUneatenPowerPelletAt(Vector2 tile) {
		return hasUneatenPowerPelletAt(tile.y, tile.x);
	}

	public boolean hasUneatenPowerPelletAt(int row, int col) {
		return inMapRange(row, col) && map[row][col] == ENERGIZER;
	}

	public boolean consumePelletAt(Vector2 tile) {
		if (hasUneatenPelletAt(tile)) {
			map[tile.y][tile.x] = PELLET_EATEN;
			eatenFoodCount++;
			return true;
		}
		return false;
	}

	public boolean consumePowerPelletAt(Vector2 tile) {
		if (hasUneatenPowerPelletAt(tile)) {
			map[tile.y][tile.x] = ENERGIZER_EATEN;
			eatenFoodCount++;
			return true;
		}
		return false;
	}

	public void resetFood() {
		for (int row = 0; row < ROWS; ++row) {
			for (int col = 0; col < COLS; ++col) {
				if (map[row][col] == PELLET_EATEN) {
					map[row][col] = PELLET;
				} else if (map[row][col] == ENERGIZER_EATEN) {
					map[row][col] = ENERGIZER;
				}
			}
		}
		eatenFoodCount = 0;
	}
}