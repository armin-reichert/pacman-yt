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

import static de.amr.yt.pacman.lib.Direction.DOWN;
import static de.amr.yt.pacman.lib.Direction.LEFT;
import static de.amr.yt.pacman.lib.Direction.RIGHT;
import static de.amr.yt.pacman.lib.Direction.UP;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import de.amr.yt.pacman.lib.Direction;
import de.amr.yt.pacman.lib.Logging;

/**
 * @author Armin Reichert
 */
public class Sprites {

	private static Sprites theSprites = new Sprites();

	public static Sprites get() {
		return theSprites;
	}

	public BufferedImage mazeImage;
	public BufferedImage sheetImage;

	// sprite caches
	public EnumMap<Direction, List<BufferedImage>> pacWalking = new EnumMap<>(Direction.class);
	public List<EnumMap<Direction, List<BufferedImage>>> ghosts = new ArrayList<>();
	public EnumMap<Direction, BufferedImage> ghostEyes = new EnumMap<>(Direction.class);
	public Map<Integer, BufferedImage> ghostValues;
	public List<BufferedImage> bonusSymbols;
	public Map<Integer, BufferedImage> bonusValues;
	public BufferedImage liveCount;

	public BufferedImage s(int col, int row) {
		return s(16 * col, 16 * row, 16, 16);
	}

	public BufferedImage s(int x, int y, int w, int h) {
		return sheetImage.getSubimage(x, y, w, h);
	}

	public List<BufferedImage> stripe(int col, int row, int numCols) {
		ArrayList<BufferedImage> stripe = new ArrayList<>();
		for (int i = 0; i < numCols; ++i) {
			stripe.add(s(col + i, row));
		}
		stripe.trimToSize();
		return stripe;
	}

	private BufferedImage image(String path) throws IOException {
		InputStream is = getClass().getResourceAsStream(path);
		if (is == null) {
			throw new RuntimeException("Resource '%s' does not exist or is not accessible".formatted(path));
		}
		return ImageIO.read(is);
	}

	private Sprites() {
		try {
			sheetImage = image("/sprites.png");
			mazeImage = image("/maze_empty.png");

			pacWalking.put(RIGHT, List.of(s(0, 0), s(1, 0), s(2, 0)));
			pacWalking.put(LEFT, List.of(s(0, 1), s(1, 1), s(2, 0)));
			pacWalking.put(UP, List.of(s(0, 2), s(1, 2), s(2, 0)));
			pacWalking.put(DOWN, List.of(s(0, 3), s(1, 3), s(2, 0)));

			EnumMap<Direction, List<BufferedImage>> redGhost = new EnumMap<>(Direction.class);
			redGhost.put(RIGHT, stripe(0, 4, 2));
			redGhost.put(LEFT, stripe(2, 4, 2));
			redGhost.put(UP, stripe(4, 4, 2));
			redGhost.put(DOWN, stripe(6, 4, 2));

			EnumMap<Direction, List<BufferedImage>> pinkGhost = new EnumMap<>(Direction.class);
			pinkGhost.put(RIGHT, stripe(0, 5, 2));
			pinkGhost.put(LEFT, stripe(2, 5, 2));
			pinkGhost.put(UP, stripe(4, 5, 2));
			pinkGhost.put(DOWN, stripe(6, 5, 2));

			EnumMap<Direction, List<BufferedImage>> cyanGhost = new EnumMap<>(Direction.class);
			cyanGhost.put(RIGHT, stripe(0, 6, 2));
			cyanGhost.put(LEFT, stripe(2, 6, 2));
			cyanGhost.put(UP, stripe(4, 6, 2));
			cyanGhost.put(DOWN, stripe(6, 6, 2));

			EnumMap<Direction, List<BufferedImage>> orangeGhost = new EnumMap<>(Direction.class);
			orangeGhost.put(RIGHT, stripe(0, 7, 2));
			orangeGhost.put(LEFT, stripe(2, 7, 2));
			orangeGhost.put(UP, stripe(4, 7, 2));
			orangeGhost.put(DOWN, stripe(6, 7, 2));

			ghosts = List.of(redGhost, pinkGhost, cyanGhost, orangeGhost);

			ghostEyes.put(RIGHT, s(8, 5));
			ghostEyes.put(LEFT, s(9, 5));
			ghostEyes.put(UP, s(10, 5));
			ghostEyes.put(DOWN, s(11, 5));

			ghostValues = Map.of( //
					200, s(0, 8), //
					400, s(1, 8), //
					800, s(2, 8), //
					1600, s(3, 8));

			bonusSymbols = stripe(2, 3, 7);

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

			Logging.log("Sprites loaded successfully");

		} catch (Exception x) {
			x.printStackTrace();
			System.exit(42);
		}
	}
}