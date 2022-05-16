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
package de.amr.yt.pacman.controller;

import de.amr.yt.pacman.lib.Logging;

/**
 * @author Armin Reichert
 */
public class GameClock {

	public static final int FPS = 60;

	public static long ticks;

	public static int sec(double n) {
		return (int) (n * FPS);
	}

	public static Runnable onTick = () -> System.out.println("Tick");

	private static final Thread thread = new Thread(GameClock::run);
	private static boolean running;

	public static void start() {
		Logging.log("Game clock starts");
		running = true;
		thread.run();
	}

	public static void stop() {
		running = false;
		try {
			thread.join();
			Logging.log("Game clock stopped");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void run() {
		while (running) {
			long frameStart = System.nanoTime();
			onTick.run();
			long frameDuration = System.nanoTime() - frameStart;
			long targetFrameDuration = 1_000_000_000L / FPS;
			if (frameDuration < targetFrameDuration) {
				long sleepMillis = (targetFrameDuration - frameDuration) / 1_000_000;
				try {
					Thread.sleep(sleepMillis);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			++ticks;
		}
	}
}