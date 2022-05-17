/**
 * 
 */
package de.amr.yt.pacman.test.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.amr.yt.pacman.model.GameModel;

/**
 * @author Armin Reichert
 */
public class TestWorld {

	private GameModel game;

	@Before
	public void setup() {
		game = new GameModel();
	}

	@Test
	public void testFood() {
		assertEquals(244, game.world.totalFoodCount);
		assertEquals(0, game.world.eatenFoodCount);
	}
}