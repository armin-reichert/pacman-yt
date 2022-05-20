package de.amr.yt.pacman.test.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.amr.yt.pacman.lib.Animation;

/**
 * @author Armin Reichert
 */
public class TestSpriteAnimation {

	@Test
	public void testFrameGeneration() {
		// 0 0 1 1 2 2 ...
		assertEquals(0, Animation.frame(0, 3, 2));
		assertEquals(0, Animation.frame(1, 3, 2));
		assertEquals(1, Animation.frame(2, 3, 2));
		assertEquals(1, Animation.frame(3, 3, 2));
		assertEquals(2, Animation.frame(4, 3, 2));
		assertEquals(2, Animation.frame(5, 3, 2));
		assertEquals(0, Animation.frame(6, 3, 2));
		assertEquals(0, Animation.frame(7, 3, 2));
		assertEquals(1, Animation.frame(8, 3, 2));
		assertEquals(1, Animation.frame(9, 3, 2));
		assertEquals(2, Animation.frame(10, 3, 2));
		assertEquals(2, Animation.frame(11, 3, 2));
	}

}
