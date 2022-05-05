package de.amr.yt.pacman.lib;

/**
 * @author Armin Reichert
 */
public enum Direction {

	UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);

	public final Vector2 vector;

	private Direction(int dx, int dy) {
		vector = new Vector2(dx, dy);
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