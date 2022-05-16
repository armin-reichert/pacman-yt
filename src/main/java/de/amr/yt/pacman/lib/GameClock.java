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

/**
 * @author Armin Reichert
 */
public class GameClock {

	private static GameClock theClock = new GameClock();

	public static GameClock get() {
		return theClock;
	}

	public static int sec(double seconds) {
		return (int) (theClock.frequency * seconds);
	}

	public int frequency = 60;
	public long ticks;
	public Runnable onTick = () -> Logging.log("Tick");

	private Thread thread;
	private boolean running;
	private long lastFameRate;
	private long frameCount;
	private long countStart;

	public long getFrameRate() {
		return lastFameRate;
	}

	public void start() {
		countStart = System.nanoTime();
		running = true;
		thread = new Thread(() -> {
			while (running) {
				tick();
			}
		});
		thread.run();
	}

	public void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void tick() {
		long start = System.nanoTime();
		onTick.run();
		long duration = System.nanoTime() - start;
		long period = 1_000_000_000L / frequency;
		if (duration < period) {
			long sleep = period - duration;
			try {
				Thread.sleep(sleep / 1_000_000);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		++ticks;
		++frameCount;
		if (System.nanoTime() - countStart >= 1_000_000_000) {
			lastFameRate = frameCount;
			frameCount = 0;
			countStart = System.nanoTime();
		}
	}
}