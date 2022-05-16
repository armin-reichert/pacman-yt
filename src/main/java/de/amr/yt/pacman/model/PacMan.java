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

import static de.amr.yt.pacman.controller.GameController.sec;

import de.amr.yt.pacman.lib.SpriteAnimation;
import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class PacMan extends Creature {

	static byte[] multiplied(byte[] frames, int n) {
		byte[] result = new byte[frames.length * n];
		for (int i = 0; i < frames.length; ++i) {
			for (int j = 0; j < n; ++j) {
				result[n * i + j] = frames[i];
			}
		}
		return result;
	}

	static final byte[] STANDING_ANIMATION = { 2 };
	static final byte[] STUCK_ANIMATION = { 1 };
	static final byte[] WALKING_ANIMATION = { 1, 1, 0, 0, 1, 1, 2, 2 };
	static final byte[] DYING_ANIMATION = multiplied(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, 4);

	public final SpriteAnimation standingAnimation = new SpriteAnimation("pacman-standing", STANDING_ANIMATION, false);
	public final SpriteAnimation stuckAnimation = new SpriteAnimation("pacman-stuck", STUCK_ANIMATION, false);
	public final SpriteAnimation walkingAnimation = new SpriteAnimation("pacman-walking", WALKING_ANIMATION, true);
	public final SpriteAnimation dyingAnimation = new SpriteAnimation("pacman-dying", DYING_ANIMATION, false);

	public final GameModel game;
	public PacManState state;
	public int powerCountdown;
	public int idleCountdown;
	public final int losingPowerDuration = sec(2);

	public SpriteAnimation animation;

	public PacMan(GameModel game) {
		super(game.world);
		this.game = game;
		reset();
	}

	@Override
	public void reset() {
		super.reset();
		canReverse = true;
		state = PacManState.NORMAL;
		powerCountdown = 0;
		idleCountdown = 0;
		animation = standingAnimation;
	}

	public void update() {
		switch (state) {
		case NORMAL -> {
			restOrWalk();
			animation = stuck ? stuckAnimation : speed == 0 ? standingAnimation : walkingAnimation;
		}
		case POWER -> {
			restOrWalk();
			if (powerCountdown == 0) {
				game.onPacPowerEnding();
				state = PacManState.NORMAL;
			} else {
				--powerCountdown;
			}
			animation = stuck ? stuckAnimation : walkingAnimation;
		}
		case DEAD -> {
			animation = dyingAnimation;
		}
		}
	}

	@Override
	public boolean canEnterTile(Vector2 tile) {
		return !world.isBlocked(tile) && !world.isGhostHouse(tile);
	}

	@Override
	public float currentSpeed() {
		return hasPower() ? game.playerSpeedPowered : game.playerSpeed;
	}

	@Override
	public String toString() {
		return "PacMan[x=%.2f, y=%.2f tile=%s, offX=%.2f, offY=%.2f, moveDir=%s, wishDir=%s]".formatted(x, y, tile(),
				offsetX(), offsetY(), moveDir, wishDir);
	}

	public boolean hasPower() {
		return powerCountdown > 0;
	}

	public boolean isLosingPower() {
		return 0 < powerCountdown && powerCountdown <= losingPowerDuration;
	}

	private void restOrWalk() {
		if (idleCountdown > 0) {
			--idleCountdown;
		} else {
			moveThroughWorld();
		}
	}
}