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

import static de.amr.yt.pacman.lib.Logging.log;
import static de.amr.yt.pacman.model.World.t;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.amr.yt.pacman.controller.GameController;
import de.amr.yt.pacman.controller.GameState;
import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.FPSCounter;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class GameWindow extends JFrame {

	public static final double SCALE_MAX = -1;

	public boolean showInfo = false;

	private final GameModel game;
	private final FPSCounter fpsCounter;
	private final Spritesheet ss = new Spritesheet();
	private final IntroScene introScene;
	private final PlayScene playScene;
	private GameScene previousScene;

	public GameWindow(GameController gameController, GameModel game, FPSCounter fpsCounter, double scale) {
		this.game = game;
		this.fpsCounter = fpsCounter;
		introScene = new IntroScene(ss, game);
		playScene = new PlayScene(ss, game);
		setTitle("Pac-Man");
		setResizable(false);
		if (scale == SCALE_MAX) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			scale = screenSize.getHeight() / (World.ROWS * World.TS);
		}
		add(createCanvas(scale));
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP -> gameController.steerPacMan(Direction.UP);
				case KeyEvent.VK_DOWN -> gameController.steerPacMan(Direction.DOWN);
				case KeyEvent.VK_LEFT -> gameController.steerPacMan(Direction.LEFT);
				case KeyEvent.VK_RIGHT -> gameController.steerPacMan(Direction.RIGHT);
				case KeyEvent.VK_I -> showInfo = !showInfo;
				case KeyEvent.VK_P -> game.paused = !game.paused;
				case KeyEvent.VK_Q -> gameController.initGame();
				case KeyEvent.VK_S -> game.pacSafe = !game.pacSafe;
				case KeyEvent.VK_SPACE -> gameController.startLevel();
				}
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				gameController.stopGameLoop();
				System.exit(0);
			}
		});
	}

	private JComponent createCanvas(double scale) {
		return new JComponent() {

			{
				Dimension size = new Dimension((int) (scale * t(World.COLS)), (int) (scale * t(World.ROWS)));
				setPreferredSize(size);
				setSize(size);
				log("Game canvas size=%dx%s", getWidth(), getHeight());
			}

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
				drawPauseText(g);
			}
		};
	}

	public void update() {
		GameScene scene = currentScene();
		if (previousScene != scene) {
			scene.init();
			log("Scene changed from %s to %s", previousScene, scene);
			previousScene = scene;
		}
		currentScene().update();
	}

	public GameScene currentScene() {
		return switch (game.state) {
		case INTRO -> introScene;
		default -> playScene;
		};
	}

	private void drawCurrentScene(Graphics2D g) {
		drawScores(g);
		currentScene().draw(g);
		drawInfo(g);
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
		if (!showInfo) {
			return;
		}
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 6));
		g.drawString("%2d FPS".formatted(fpsCounter.getFrameRate()), t(1), t(2));
		if (game.paused) {
			g.drawString("(PAUSED)", t(6), t(2));
		}
		String text = "%s (%d)".formatted(game.state.name(), game.stateTimer);
		if (game.state == GameState.PLAYING) {
			if (game.chasingPhase) {
				text += " CHASING (%d)".formatted(game.attackTimer);
			} else {
				text += " SCATTERING (%d)".formatted(game.attackTimer);
			}
		}
		g.drawString(text, t(1), t(3));
		if (game.pacSafe) {
			g.drawString("Pac-Man is safe", t(18), t(3));
		}
		if (game.state != GameState.INTRO) {
			playScene.drawInfo(g);
		}
	}

	private void drawPauseText(Graphics g) {
		if (game.paused) {
			String text = "PAUSED";
			g.setColor(Color.RED);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
			int sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (getWidth() - sw) / 2, getHeight() * 3 / 4);
		}
	}
}