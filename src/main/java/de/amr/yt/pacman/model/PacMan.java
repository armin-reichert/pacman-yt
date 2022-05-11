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
import static de.amr.yt.pacman.lib.Logging.log;

import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class PacMan extends Creature {

	public final GameModel game;
	public boolean losingPower;
	public boolean dead;
	public int powerCountdown;
	public int idleCountdown;
	public int dyingAnimationCountdown;
	public final int dyingAnimationDuration = sec(1.5);
	public final int losingPowerDuration = sec(2);

	public PacMan(GameModel game) {
		super(game.world);
		this.game = game;
		canReverse = true;
	}

	@Override
	protected boolean canEnterTile(Vector2 tile) {
		return !world.isBlocked(tile) && !world.isGhostHouse(tile);
	}

	@Override
	protected float currentSpeed() {
		return powerCountdown == 0 ? game.playerSpeed : game.playerSpeedPowered;
	}

	@Override
	public String toString() {
		return "PacMan[x=%.2f, y=%.2f tile=%s, offX=%.2f, offY=%.2f, moveDir=%s, wishDir=%s]".formatted(x, y, tile(),
				offsetX(), offsetY(), moveDir, wishDir);
	}

	public void update() {
		if (dead) {
			if (dyingAnimationCountdown > 0) {
				--dyingAnimationCountdown;
			}
		} else {
			if (idleCountdown > 0) {
				--idleCountdown;
			} else {
				moveThroughWorld();
			}
			if (powerCountdown > 0) {
				if (powerCountdown == losingPowerDuration) {
					losingPower = true;
				}
				if (--powerCountdown == 0) {
					losingPower = false;
					game.onPacPowerEnding();
				}
			}
		}
	}

	public void enterPowerState() {
		powerCountdown = sec(game.ghostFrightenedSeconds);
		losingPower = false;
		log("Pac-Man gets power for %d seconds", game.ghostFrightenedSeconds);
	}
}