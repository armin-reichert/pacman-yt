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
public class GameWindow {

	public static final double SCALE_MAX = -1;

	public boolean showInfo = false;

	private final GameModel game;
	private final IntroScene introScene;
	private final PlayScene playScene;
	private final FPSCounter fpsCounter;
	private final JFrame frame;
	private GameScene previousScene;
	private boolean scoreVisible;

	public GameWindow(GameController gameController, GameModel game, FPSCounter fpsCounter, double scaleValue) {
		this.game = game;
		this.fpsCounter = fpsCounter;
		final double scale = (scaleValue == SCALE_MAX)
				? Toolkit.getDefaultToolkit().getScreenSize().getHeight() / t(World.ROWS)
				: scaleValue;
		introScene = new IntroScene(game);
		playScene = new PlayScene(game);
		frame = new JFrame("Pac-Man");
		frame.addKeyListener(new KeyAdapter() {
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
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				gameController.stopGameLoop();
				System.exit(0);
			}
		});
		var canvas = new JComponent() {
			{
				Dimension size = new Dimension((int) (scale * t(World.COLS)), (int) (scale * t(World.ROWS)));
				setPreferredSize(size);
				setSize(size);
				log("Game canvas size=%dx%s", getWidth(), getHeight());
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				drawCurrentGameScene(scale, g);
				drawPauseText(g);
			}
		};
		frame.add(canvas);
		frame.setResizable(false);
	}

	public void show() {
		frame.setVisible(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.requestFocus();
	}

	public void render() {
		frame.repaint();
	}

	public void update() {
		scoreVisible = game.state != GameState.INTRO;
		GameScene scene = currentScene();
		if (previousScene != scene) {
			scene.init();
			log("Scene changed from %s to %s", previousScene, scene);
			previousScene = scene;
		}
		scene.update();
	}

	public GameScene currentScene() {
		return switch (game.state) {
		case INTRO -> introScene;
		default -> playScene;
		};
	}

	private void drawPauseText(Graphics g) {
		if (game.paused) {
			String text = "PAUSED";
			g.setColor(Color.RED);
			g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 36));
			int sw = g.getFontMetrics().stringWidth(text);
			g.drawString(text, (frame.getWidth() - sw) / 2, frame.getHeight() * 3 / 4);
		}
	}

	private void drawCurrentGameScene(double scale, Graphics g) {
		Graphics2D g2D = (Graphics2D) g.create();
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2D.scale(scale, scale);
		drawScore(g2D);
		currentScene().draw(g2D);
		drawInfo(g2D);
		g2D.dispose();
	}

	private void drawScore(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(Sprites.get().arcadeFont);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("LEVEL", t(18), t(1));
		if (scoreVisible) {
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
}