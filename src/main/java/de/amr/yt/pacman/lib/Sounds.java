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
package de.amr.yt.pacman.lib;

import static de.amr.yt.pacman.lib.Logging.log;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author Armin Reichert
 */
public class Sounds {

	private static Map<String, Clip> clipCache = new HashMap<>();

	public static Clip clip(String fileName) {
		if (clipCache.containsKey(fileName)) {
			log("Found clip '%s' in cache", fileName);
			return clipCache.get(fileName);
		}
		try {
			Clip clip = loadClip(fileName);
			clipCache.put(fileName, clip);
			return clip;
		} catch (Exception x) {
			log("Could not load clip '%s': %s", fileName, x.getMessage());
			return null;
		}
	}

	public static void play(String fileName) {
		Clip clip = clip(fileName);
		clip.setFramePosition(0);
		clip.start();
	}

	private static Clip loadClip(String fileName)
			throws LineUnavailableException, UnsupportedAudioFileException, IOException {
		URL url = Sounds.class.getResource("/sounds/" + fileName + ".wav");
		if (url == null) {
			String message = "Could not load audio resource, path='%s'";
			throw new RuntimeException(message);
		}
		Clip clip = AudioSystem.getClip();
		AudioInputStream ais = AudioSystem.getAudioInputStream(url);
		clip.open(ais);
		clipCache.put(fileName, clip);
		return clip;
	}
}