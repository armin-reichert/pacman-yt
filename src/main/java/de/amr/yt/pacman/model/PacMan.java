package de.amr.yt.pacman.model;

import static de.amr.yt.pacman.controller.GameController.sec;

import de.amr.yt.pacman.lib.Vector2;

/**
 * @author Armin Reichert
 */
public class PacMan extends Creature {

	public int lives;
	public int powerTime;
	public boolean losingPower;
	public boolean dead;
	public int dyingAnimationTimer;
	public final int dyingAnimationDuration = sec(1.5);

	public PacMan() {
		canReverse = true;
		powerTime = 0;
		losingPower = false;
		dead = false;
		dyingAnimationTimer = 0;
	}

	@Override
	protected boolean canEnter(World world, Vector2 tile) {
		return !world.isBlocked(tile) && !world.isGhostHouse(tile);
	}

	@Override
	public String toString() {
		return String.format("PacMan[x=%.2f, y=%.2f tile=%s, offX=%.2f, offY=%.2f, moveDir=%s, wishDir=%s]", x, y, tile(),
				offsetX(), offsetY(), moveDir, wishDir);
	}

	public void update(GameModel game) {
		if (dead) {
			if (dyingAnimationTimer > 0) {
				--dyingAnimationTimer;
			}
		} else {
			move(game.world);
			game.checkFood();
			game.checkPacKilledByGhost();
			updatePowerState(game);
		}
	}

	public void enterPowerState(GameModel game) {
		powerTime = sec(5);
		losingPower = false;
		for (Ghost ghost : game.ghosts) {
			if (ghost.state == GhostState.CHASING || ghost.state == GhostState.SCATTERING) {
				ghost.state = GhostState.FRIGHTENED;
				ghost.speed = ghost.frightenedSpeed;
			}
		}
		game.ghostsKilledByPowerPill = 0;
	}

	private void updatePowerState(GameModel game) {
		if (powerTime > 0) {
			game.checkGhostsKilledByPac();
			if (powerTime == sec(2)) {
				losingPower = true;
			}
			if (--powerTime == 0) {
				game.onPowerStateComplete();
			}
		}
	}
}