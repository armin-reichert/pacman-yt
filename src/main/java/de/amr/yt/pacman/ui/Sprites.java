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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * @author Armin Reichert
 */
public class Sprites {

	private static BufferedImage sheet;

	public static BufferedImage mazeImage;
	public static List<BufferedImage> bonusSymbols;
	public static Map<Integer, BufferedImage> bonusValues;
	public static BufferedImage liveCount;

	public static void load() {
		try {
			sheet = image("/sprites.png");
			mazeImage = image("/maze_empty.png");
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
			log("Sprites loaded successfully");
		} catch (Exception x) {
			x.printStackTrace();
			System.exit(42);
		}
	}

	public static BufferedImage s(int col, int row) {
		return s(16 * col, 16 * row, 16, 16);
	}

	public static BufferedImage s(int x, int y, int w, int h) {
		return sheet.getSubimage(x, y, w, h);
	}

	public static List<BufferedImage> stripe(int col, int row, int numCols) {
		ArrayList<BufferedImage> stripe = new ArrayList<>();
		for (int i = 0; i < numCols; ++i) {
			stripe.add(s(col + i, row));
		}
		stripe.trimToSize();
		return stripe;
	}

	private static BufferedImage image(String path) throws IOException {
		InputStream is = Sprites.class.getResourceAsStream(path);
		if (is == null) {
			throw new RuntimeException("Resource '%s' does not exist or is not accessible".formatted(path));
		}
		return ImageIO.read(is);
	}
}