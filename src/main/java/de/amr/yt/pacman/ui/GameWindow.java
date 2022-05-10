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

import static de.amr.yt.pacman.model.World.t;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.FPSCounter;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class GameWindow extends JFrame {

	public boolean showInfo = true;

	private final GameModel game;
	private final FPSCounter fpsCounter;
	private final Spritesheet ss = new Spritesheet();
	private final IntroScene introScene;
	private final PlayScene playScene;
	private final JComponent canvas;

	public GameWindow(GameModel game, FPSCounter fpsCounter, double scale) {
		super("Pac-Man");
		this.game = game;
		this.fpsCounter = fpsCounter;
		introScene = new IntroScene(ss, game);
		playScene = new PlayScene(ss, game);
		canvas = createCanvas(scale);
		add(canvas);
		setResizable(false);
	}

	private JComponent createCanvas(double scale) {
		JComponent canvas = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2D = (Graphics2D) g.create();
				g2D.setColor(Color.BLACK);
				g2D.fillRect(0, 0, getWidth(), getHeight());
				g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2D.scale(scale, scale);
				drawCurrentScene(g2D);
				g2D.dispose();
			}
		};
		Dimension size = new Dimension((int) (World.COLS * World.TS * scale), (int) (World.ROWS * World.TS * scale));
		canvas.setPreferredSize(size);
		canvas.setMinimumSize(size);
		canvas.setSize(size);
		return canvas;
	}

	private void drawCurrentScene(Graphics2D g) {
		drawScores(g);
		switch (game.state) {
		case INTRO -> introScene.draw(g);
		default -> playScene.draw(g);
		}
		if (showInfo) {
			drawInfo(g);
		}
	}

	private void drawScores(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(ss.arcadeFont);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("LEVEL", t(18), t(1));
		if (game.state != GameState.INTRO) {
			g.drawString("%07d".formatted(game.score), t(7), t(1));
			g.drawString("%03d".formatted(game.levelNumber), t(24), t(1));
		}
	}

	private void drawInfo(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 6));
		g.drawString("%2d FPS".formatted(fpsCounter.getFrameRate()), 8, 16);
		if (game.paused) {
			g.drawString("(PAUSED)", 48, 16);
		}
		String state = game.state.name();
		if (game.state == GameState.PLAYING) {
			state = game.chasingPhase ? "PLAYING (CHASING)" : "PLAYING (SCATTERING)";
		}
		g.drawString(state + " " + game.stateTimer, 8, 24);
		if (game.pacSafe) {
			g.drawString("Pac-Man is safe", 144, 24);
		}
		if (game.state != GameState.INTRO) {
			playScene.drawInfo(g);
		}
	}
}