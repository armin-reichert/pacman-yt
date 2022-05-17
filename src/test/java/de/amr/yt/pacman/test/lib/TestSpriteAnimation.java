package de.amr.yt.pacman.test.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.yt.pacman.lib.SpriteAnimation;

/**
 * @author Armin Reichert
 */
public class TestSpriteAnimation {

	@Test
	public void testFrameGeneration() {
		// 0 0 1 1 2 2 ...
		assertEquals(0, SpriteAnimation.frame(0, 3, 2));
		assertEquals(0, SpriteAnimation.frame(1, 3, 2));
		assertEquals(1, SpriteAnimation.frame(2, 3, 2));
		assertEquals(1, SpriteAnimation.frame(3, 3, 2));
		assertEquals(2, SpriteAnimation.frame(4, 3, 2));
		assertEquals(2, SpriteAnimation.frame(5, 3, 2));
		assertEquals(0, SpriteAnimation.frame(6, 3, 2));
		assertEquals(0, SpriteAnimation.frame(7, 3, 2));
		assertEquals(1, SpriteAnimation.frame(8, 3, 2));
		assertEquals(1, SpriteAnimation.frame(9, 3, 2));
		assertEquals(2, SpriteAnimation.frame(10, 3, 2));
		assertEquals(2, SpriteAnimation.frame(11, 3, 2));
	}

}
