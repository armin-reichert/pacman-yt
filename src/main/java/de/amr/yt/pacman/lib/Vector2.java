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

import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class Vector2 {

	public final int x;
	public final int y;

	@Override
	public String toString() {
		return String.format("[x=%d,y=%d]", x, y);
	}

	public Vector2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Vector2 neighbor(Direction direction) {
		return switch (direction) {
		case UP -> new Vector2(x, y - 1);
		case DOWN -> new Vector2(x, y + 1);
		case LEFT -> new Vector2(x - 1, y);
		case RIGHT -> new Vector2(x + 1, y);
		};
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector2 other = (Vector2) obj;
		return x == other.x && y == other.y;
	}

	public double dist(Vector2 v) {
		return Math.hypot(x - v.x, y - v.y);
	}

	public Vector2 plus(Vector2 v) {
		return new Vector2(x + v.x, y + v.y);
	}

	public Vector2 minus(Vector2 v) {
		return new Vector2(x - v.x, y - v.y);
	}

	public Vector2 times(int n) {
		return new Vector2(n * x, n * y);
	}
}