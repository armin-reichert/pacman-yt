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
import static de.amr.yt.pacman.ui.Renderer.drawGhostTargetTiles;

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
import de.amr.yt.pacman.lib.GameClock;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.GameState;
import de.amr.yt.pacman.model.World;

/**
 * @author Armin Reichert
 */
public class GameUI {

	public static final double SCALE_MAX = 0;

	public boolean showInfo = false;
	public boolean showTargetTiles = false;

	public final Joystick joystick = new Joystick();
	private final GameController gameController;
	private final GameModel game;
	private final IntroScene introScene;
	private final PlayScene playScene;
	private final JFrame frame;
	private JComponent canvas;
	private double canvasScaling;
	private GameScene previousScene;
	private boolean scoreVisible;

	public GameUI(GameController gameController, GameModel game, double scaling) {
		this.gameController = gameController;
		this.game = game;
		introScene = new IntroScene(game);
		playScene = new PlayScene(game);
		frame = new JFrame("Pac-Man");
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e.getKeyCode());
			}

		});
		frame.addKeyListener(joystick);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				GameClock.get().stop();
				System.exit(0);
			}
		});
		canvas = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				drawCurrentGameScene(canvasScaling, g);
				drawPauseText(g);
			}
		};
		setCanvasScaling(scaling);
		frame.add(canvas);
		frame.setResizable(false);
	}

	private void handleKeyPressed(int key) {
		switch (key) {
		case KeyEvent.VK_I -> showInfo = !showInfo;
		case KeyEvent.VK_P -> game.paused = !game.paused;
		case KeyEvent.VK_Q -> gameController.newGame();
		case KeyEvent.VK_S -> game.pacSafe = !game.pacSafe;
		case KeyEvent.VK_T -> showTargetTiles = !showTargetTiles;
		case KeyEvent.VK_SPACE -> {
			if (game.paused) {
				gameController.step(true);
			} else if (game.state == GameState.INTRO && game.state.timer >= IntroScene.READY_TO_PLAY_TIME) {
				game.setState(GameState.LEVEL_STARTING);
			}
		}
		case KeyEvent.VK_ENTER -> {
			if (!game.paused && game.state == GameState.INTRO) {
				game.setState(GameState.LEVEL_STARTING);
			}
		}
		case KeyEvent.VK_PLUS -> {
			GameClock.get().changeFrequency(5);
		}
		case KeyEvent.VK_MINUS -> {
			GameClock.get().changeFrequency(-5);
		}
		}
	}

	public void setCanvasScaling(double scaling) {
		canvasScaling = (scaling == SCALE_MAX)
				? 0.9 * Toolkit.getDefaultToolkit().getScreenSize().getHeight() / t(World.ROWS)
				: scaling;
		Dimension size = new Dimension((int) (canvasScaling * t(World.COLS)), (int) (canvasScaling * t(World.ROWS)));
		canvas.setPreferredSize(size);
		canvas.setSize(size);
		log("Game canvas size=%dx%s", canvas.getWidth(), canvas.getHeight());
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
		g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2D.scale(scale, scale);
		drawScore(g2D);
		currentScene().draw(g2D);
		if (showInfo) {
			drawInfo(g2D);
		}
		if (showTargetTiles) {
			drawGhostTargetTiles(g2D, game.ghosts);
		}
		g2D.dispose();
	}

	private void drawScore(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(Renderer.ARCADE_FONT);
		g.drawString("SCORE", t(1), t(1));
		g.drawString("LEVEL", t(18), t(1));
		if (scoreVisible) {
			g.drawString("%07d".formatted(game.score), t(7), t(1));
			g.drawString("%03d".formatted(game.level.number), t(24), t(1));
		}
	}

	private void drawInfo(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.DIALOG, Font.PLAIN, 6));

		g.drawString("%2d FPS (Target=%d)".formatted(GameClock.get().getFrameRate(), GameClock.get().getFrequency()), t(1),
				t(2));
		String text = "%s (%d)".formatted(game.state.name(), game.state.timer);
		if (game.state == GameState.PLAYING) {
			if (game.chasingPhase) {
				text += " CHASING (%d)".formatted(game.attackTimer);
			} else {
				text += " SCATTERING (%d)".formatted(game.attackTimer);
			}
		}
		String joystickText = "Joystick: %s".formatted(joystick.state().isEmpty() ? "middle" : joystick.state().get());
		g.drawString(joystickText, t(18), t(2));
		g.drawString(text, t(1), t(3));
		if (game.pacSafe) {
			g.drawString("Pac-Man is safe", t(18), t(3));
		}
		if (game.state != GameState.INTRO) {
			playScene.drawInfo(g);
		}
	}
}