package de.amr.yt.pacman.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.amr.yt.pacman.model.Creature;
import de.amr.yt.pacman.model.GameModel;
import de.amr.yt.pacman.model.Ghost;
import de.amr.yt.pacman.model.GhostState;
import de.amr.yt.pacman.model.PacMan;
import de.amr.yt.pacman.model.PacManState;

/**
 * @author Armin Reichert
 */
public class CreatureRenderer {

	private final Spritesheet ss;
	private final GameModel game;

	public CreatureRenderer(Spritesheet ss, GameModel game) {
		this.ss = ss;
		this.game = game;
	}

	public void drawPacMan(Graphics2D g, PacMan pacMan) {
		if (pacMan.state == PacManState.DEAD) {
			drawPacManDead(g, pacMan);
		} else {
			drawPacManAlive(g, pacMan);
		}
	}

	private void drawPacManDead(Graphics2D g, PacMan pacMan) {
		BufferedImage sprite = null;
		if (pacMan.dyingAnimationCountdown > 0) {
			int frame = 10 - (10 * pacMan.dyingAnimationCountdown / pacMan.dyingAnimationDuration);
			sprite = ss.pacDeadAnimation.get(frame);
		} else if (!pacMan.animated) {
			sprite = ss.pac.get(pacMan.moveDir).get(2);
		}
		drawCreatureSprite(g, pacMan, sprite);
	}

	private void drawPacManAlive(Graphics2D g, PacMan pacMan) {
		if (pacMan.animated) {
			pacMan.animFrame = pacMan.stuck ? 0 : game.frame(Spritesheet.PACMAN_MOUTH_ANIMATION);
		}
		BufferedImage sprite = ss.pac.get(pacMan.moveDir).get(pacMan.animFrame);
		drawCreatureSprite(g, pacMan, sprite);
	}

	public void drawGhost(Graphics2D g, Ghost ghost) {
		BufferedImage sprite = null;
		if (ghost.state == GhostState.EATEN || ghost.state == GhostState.ENTERING_HOUSE) {
			sprite = ghost.eatenTimer > 0 ? ss.ghostValues.get(ghost.eatenValue) : ss.ghostEaten.get(ghost.moveDir);
		} else if (ghost.state == GhostState.FRIGHTENED
				|| ghost.state == GhostState.LOCKED && game.pacMan.powerCountdown > 0) {
			if (ghost.animated) {
				ghost.animFrame = game.frame(Spritesheet.GHOST_ANIMATION);
				if (game.pacMan.isLosingPower()) {
					int blink = game.frame(20, 2) == 0 ? 0 : 2;
					ghost.animFrame += blink;
				}
			}
			sprite = ss.ghostFrightened.get(ghost.animFrame);
		} else {
			if (ghost.animated) {
				ghost.animFrame = game.frame(Spritesheet.GHOST_ANIMATION);
			}
			sprite = ss.ghosts.get(ghost.id).get(ghost.moveDir).get(ghost.animFrame);
		}
		drawCreatureSprite(g, ghost, sprite);
	}

	private void drawGhost(Graphics2D g, Ghost ghost, BufferedImage sprite) {
		if (ghost.visible) {
			drawCreatureSprite(g, ghost, sprite);
		}
	}

	public void drawGhostNormal(Graphics2D g, Ghost ghost) {
		drawGhost(g, ghost, ss.ghosts.get(ghost.id).get(ghost.moveDir).get(game.frame(Spritesheet.GHOST_ANIMATION)));
	}

	public void drawGhostFrightened(Graphics2D g, Ghost ghost) {
		drawGhost(g, ghost, ss.ghostFrightened.get(game.frame(Spritesheet.GHOST_ANIMATION)));
	}

	public void drawGhostValue(Graphics2D g, Ghost ghost) {
		drawGhost(g, ghost, ss.ghostValues.get(ghost.id == 0 ? 200 : ghost.id == 1 ? 400 : ghost.id == 2 ? 800 : 1600));
	}

	public void drawCreatureSprite(Graphics2D g, Creature creature, BufferedImage sprite) {
		if (creature.visible && sprite != null) {
			int x = (int) creature.x - sprite.getWidth() / 2;
			int y = (int) creature.y - sprite.getHeight() / 2;
			g.drawImage(sprite, x, y, sprite.getWidth(), sprite.getHeight(), null);
		}
	}
}