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

	private static GameClock theClock = new GameClock(60);

	public static GameClock get() {
		return theClock;
	}

	public static int sec(double seconds) {
		return (int) (theClock.frequency * seconds);
	}

	public long ticks;

	private int frequency;
	private Runnable onTick = () -> Logging.log("Tick");
	private Thread thread;
	private boolean running;
	private long lastFrameRate;
	private long frameCount;
	private long frameCountStart;

	public GameClock(int frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		return "tick %d (second %.2f)".formatted(ticks, ticks / (float) frequency);
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public long getFrameRate() {
		return lastFrameRate;
	}

	public void start(Runnable onTick) {
		this.onTick = onTick;
		running = true;
		frameCountStart = System.nanoTime();
		thread = new Thread(() -> {
			while (running) {
				tick();
			}
		}, "GameClock");
		thread.start();
	}

	private void tick() {
		long start = System.nanoTime();
		onTick.run();
		long end = System.nanoTime();
		long duration = end - start;
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
		if (end - frameCountStart >= 1_000_000_000) {
			lastFrameRate = frameCount;
			frameCount = 0;
			frameCountStart = end;
		}
	}

	public void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}