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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import de.amr.yt.pacman.lib.Direction;

/**
 * @author Armin Reichert
 */
public class Spritesheet {

	public BufferedImage mazeImage;
	public BufferedImage sheetImage;
	public EnumMap<Direction, List<BufferedImage>> pac = new EnumMap<>(Direction.class);
	public List<BufferedImage> pacDeadAnimation;
	public List<EnumMap<Direction, List<BufferedImage>>> ghosts = new ArrayList<>();
	public List<BufferedImage> ghostFrightened;
	public EnumMap<Direction, BufferedImage> ghostEaten = new EnumMap<>(Direction.class);
	public Map<Integer, BufferedImage> ghostValues;
	public List<BufferedImage> bonusSymbols;
	public Map<Integer, BufferedImage> bonusValues;
	public BufferedImage liveCount;

	public Spritesheet() {
		try {
			sheetImage = ImageIO.read(getClass().getResource("/sprites.png"));
			mazeImage = ImageIO.read(getClass().getResource("/maze_empty.png"));

			pac.put(Direction.RIGHT, List.of(s(0, 0), s(1, 0), s(2, 0)));
			pac.put(Direction.LEFT, List.of(s(0, 1), s(1, 1), s(2, 0)));
			pac.put(Direction.UP, List.of(s(0, 2), s(1, 2), s(2, 0)));
			pac.put(Direction.DOWN, List.of(s(0, 3), s(1, 3), s(2, 0)));

			pacDeadAnimation = new ArrayList<>();
			for (int x = 3; x <= 13; ++x) {
				pacDeadAnimation.add(s(x, 0));
			}

			EnumMap<Direction, List<BufferedImage>> blinkySprites = new EnumMap<>(Direction.class);
			blinkySprites.put(Direction.RIGHT, List.of(s(0, 4), s(1, 4)));
			blinkySprites.put(Direction.LEFT, List.of(s(2, 4), s(3, 4)));
			blinkySprites.put(Direction.UP, List.of(s(4, 4), s(5, 4)));
			blinkySprites.put(Direction.DOWN, List.of(s(6, 4), s(7, 4)));

			EnumMap<Direction, List<BufferedImage>> pinkySprites = new EnumMap<>(Direction.class);
			pinkySprites.put(Direction.RIGHT, List.of(s(0, 5), s(1, 5)));
			pinkySprites.put(Direction.LEFT, List.of(s(2, 5), s(3, 5)));
			pinkySprites.put(Direction.UP, List.of(s(4, 5), s(5, 5)));
			pinkySprites.put(Direction.DOWN, List.of(s(6, 5), s(7, 5)));

			EnumMap<Direction, List<BufferedImage>> inkySprites = new EnumMap<>(Direction.class);
			inkySprites.put(Direction.RIGHT, List.of(s(0, 6), s(1, 6)));
			inkySprites.put(Direction.LEFT, List.of(s(2, 6), s(3, 6)));
			inkySprites.put(Direction.UP, List.of(s(4, 6), s(5, 6)));
			inkySprites.put(Direction.DOWN, List.of(s(6, 6), s(7, 6)));

			EnumMap<Direction, List<BufferedImage>> clydeSprites = new EnumMap<>(Direction.class);
			clydeSprites.put(Direction.RIGHT, List.of(s(0, 7), s(1, 7)));
			clydeSprites.put(Direction.LEFT, List.of(s(2, 7), s(3, 7)));
			clydeSprites.put(Direction.UP, List.of(s(4, 7), s(5, 7)));
			clydeSprites.put(Direction.DOWN, List.of(s(6, 7), s(7, 7)));

			ghosts = List.of(blinkySprites, pinkySprites, inkySprites, clydeSprites);

			ghostFrightened = List.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4));

			ghostEaten.put(Direction.RIGHT, s(8, 5));
			ghostEaten.put(Direction.LEFT, s(9, 5));
			ghostEaten.put(Direction.UP, s(10, 5));
			ghostEaten.put(Direction.DOWN, s(11, 5));

			ghostValues = Map.of( //
					200, s(0, 8), //
					400, s(1, 8), //
					800, s(2, 8), //
					1600, s(3, 8));

			bonusSymbols = List.of(s(2, 3), s(3, 3), s(4, 3), s(5, 3), s(6, 3), s(7, 3), s(8, 3));

			bonusValues = Map.of( //
					100, s(0, 9), //
					300, s(1, 9), //
					500, s(2, 9), //
					700, s(3, 9), //
					1000, s(64, 144, 19, 16), //
					2000, s(60, 160, 24, 16), //
					3000, s(60, 176, 24, 16), //
					5000, s(60, 192, 24, 16)); //

			liveCount = s(8, 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedImage s(int gridX, int gridY) {
		return sheetImage.getSubimage(16 * gridX, 16 * gridY, 16, 16);
	}

	private BufferedImage s(int x, int y, int w, int h) {
		return sheetImage.getSubimage(x, y, w, h);
	}
}