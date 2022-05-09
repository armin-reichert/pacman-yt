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
package de.amr.yt.pacman.ui;

import static de.amr.yt.pacman.controller.GameController.sec;
import static de.amr.yt.pacman.model.GameModel.BLINKY;
import static de.amr.yt.pacman.model.GameModel.CLYDE;
import static de.amr.yt.pacman.model.GameModel.INKY;
import static de.amr.yt.pacman.model.GameModel.PINKY;

import java.awt.Color;
import java.awt.Graphics2D;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class IntroScene {

	private final GameModel game;
	private final Spritesheet ss;
	private int animationStartTime = sec(2);

	public IntroScene(Spritesheet ss, GameModel game) {
		this.ss = ss;
		this.game = game;
	}

	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("CHARACTER", 6 * World.TS, 6 * World.TS);
		g.drawString("/", 16 * World.TS, 6 * World.TS);
		g.drawString("NICKNAME", 18 * World.TS, 6 * World.TS);

		int y = 6 * World.TS + World.HTS;
		for (int id = 0; id <= 3; ++id) {
			int t = animationStartTime + sec(2 * id);
			if (game.stateTimer >= t) {
				g.drawImage(ss.ghosts.get(id).get(Direction.RIGHT).get(0), 3 * World.TS, y, null);
				g.setColor(ss.ghostColor(id));
			}
			if (game.stateTimer >= t + sec(0.5)) {
				g.drawString("-" + ghostCharacter(id), 6 * World.TS, y + 12);
			}
			if (game.stateTimer >= t + sec(1.0)) {
				g.drawString("\"" + ghostNickname(id) + "\"", 17 * World.TS, y + 12);
			}
			y += 3 * World.TS;
		}
		if (game.stateTimer >= animationStartTime + sec(8)) {
			g.setColor(Color.PINK);
			int x = 10 * World.TS;
			y = 26 * World.TS;
			g.fillRect(x + 3, y, 2, 2);
			g.setFont(ss.arcadeFont);
			g.drawString("10", x + 16, y + 6);
			g.setFont(ss.arcadeFont.deriveFont(6.0f));
			g.drawString("PTS", x + 40, y + 6);
			y += 2 * World.TS;
			g.fillOval(10 * World.TS, y, 8, 8);
			g.setFont(ss.arcadeFont);
			g.drawString("50", x + 16, y + 6);
			g.setFont(ss.arcadeFont.deriveFont(6.0f));
			g.drawString("PTS", x + 40, y + 6);
		}
		if (game.stateTimer >= animationStartTime + sec(12)) {
			if (game.frame(30, 2) == 0) {
				g.setColor(Color.WHITE);
				g.setFont(ss.arcadeFont);
				g.drawString("PRESS SPACE TO PLAY", 4 * World.TS, 32 * World.TS);
			}
		}
	}

	private String ghostCharacter(int id) {
		return switch (id) {
		case BLINKY -> "SHADOW";
		case PINKY -> "SPEEDY";
		case INKY -> "BASHFUL";
		case CLYDE -> "POKEY";
		default -> null;
		};
	}

	private String ghostNickname(int id) {
		return switch (id) {
		case BLINKY -> "BLINKY";
		case PINKY -> "PINKY";
		case INKY -> "INKY";
		case CLYDE -> "CLYDE";
		default -> null;
		};
	}
}