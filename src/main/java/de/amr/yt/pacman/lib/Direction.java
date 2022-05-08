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
package de.amr.yt.pacman.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Armin Reichert
 */
public enum Direction {

	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

	public final Vector2 vector;

	private Direction(int dx, int dy) {
		vector = new Vector2(dx, dy);
	}

	public static List<Direction> valuesShuffled() {
		List<Direction> directions = Arrays.asList(Direction.values());
		Collections.shuffle(directions);
		return directions;
	}

	public Direction opposite() {
		return switch (this) {
		case DOWN -> UP;
		case LEFT -> RIGHT;
		case RIGHT -> LEFT;
		case UP -> DOWN;
		};
	}

	public boolean isHorizontal() {
		return this == LEFT || this == RIGHT;
	}

	public boolean isVertical() {
		return this == UP || this == DOWN;
	}
}