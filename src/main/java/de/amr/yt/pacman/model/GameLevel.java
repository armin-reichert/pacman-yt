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

import static de.amr.yt.pacman.lib.GameClock.sec;

/**
 * @author Armin Reichert
 */
public class GameLevel {
	public final int number;
	public final long[] scatterStartTicks;
	public final long[] chaseStartTicks;
	public final int bonusSymbol;
	public final int bonusValue;
	public final float playerSpeed;
	public final float ghostSpeed;
	public final float ghostSpeedTunnel;
	public final int elroy1DotsLeft;
	public final float elroy1Speed;
	public final int elroy2DotsLeft;
	public final float elroy2Speed;
	public final float playerSpeedPowered;
	public final float ghostSpeedFrightened;
	public final int ghostFrightenedSeconds;
	public final int numFlashes;

	public int ghostsKilled;

	public GameLevel(int levelNumber, Object... data) {
		number = levelNumber;
		bonusSymbol = (int) data[0];
		bonusValue = (int) data[1];
		playerSpeed = (float) data[2] * GameModel.BASE_SPEED;
		ghostSpeed = (float) data[3] * GameModel.BASE_SPEED;
		ghostSpeedTunnel = (float) data[4] * GameModel.BASE_SPEED;
		elroy1DotsLeft = (int) data[5];
		elroy1Speed = (float) data[6] * GameModel.BASE_SPEED;
		elroy2DotsLeft = (int) data[7];
		elroy2Speed = (float) data[8] * GameModel.BASE_SPEED;
		playerSpeedPowered = (float) data[9] * GameModel.BASE_SPEED;
		ghostSpeedFrightened = (float) data[10] * GameModel.BASE_SPEED;
		ghostFrightenedSeconds = (int) data[11];
		numFlashes = (int) data[12];

		switch (levelNumber) {
		case 1 -> {
			scatterStartTicks = new long[] { 0, sec(27), sec(54), sec(79) };
			chaseStartTicks = new long[] { sec(7), sec(34), sec(59), sec(84) };
		}
		case 2, 3, 4 -> {
			scatterStartTicks = new long[] { 0, sec(27), sec(54), sec(1092) };
			chaseStartTicks = new long[] { sec(7), sec(34), sec(59), sec(1092) + 1 };
		}
		default -> {
			scatterStartTicks = new long[] { 0, sec(25), sec(50), sec(1092) };
			chaseStartTicks = new long[] { sec(5), sec(30), sec(55), sec(1092) + 1 };
		}
		}
	}
}