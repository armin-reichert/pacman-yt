/**
 * 
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

	public double dist(Vector2 tile) {
		return Math.hypot(x - tile.x, y - tile.y);
	}

	public Vector2 plus(Vector2 tile) {
		return new Vector2(x + tile.x, y + tile.y);
	}

	public Vector2 minus(Vector2 tile) {
		return new Vector2(x - tile.x, y - tile.y);
	}

	public Vector2 times(int n) {
		return new Vector2(n * x, n * y);
	}
}