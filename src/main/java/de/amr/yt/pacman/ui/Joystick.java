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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Optional;

import de.amr.yt.pacman.lib.Direction;

/**
 * @author Armin Reichert
 */
public class Joystick extends KeyAdapter {

	private volatile Direction state;

	public Optional<Direction> state() {
		return Optional.ofNullable(state);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP -> {
			if (state == null)
				state = Direction.UP;
		}
		case KeyEvent.VK_DOWN -> {
			if (state == null)
				state = Direction.DOWN;
		}
		case KeyEvent.VK_LEFT -> {
			if (state == null)
				state = Direction.LEFT;
		}
		case KeyEvent.VK_RIGHT -> {
			if (state == null)
				state = Direction.RIGHT;
		}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP -> {
			if (state == Direction.UP)
				state = null;
		}
		case KeyEvent.VK_DOWN -> {
			if (state == Direction.DOWN)
				state = null;
		}
		case KeyEvent.VK_LEFT -> {
			if (state == Direction.LEFT)
				state = null;
		}
		case KeyEvent.VK_RIGHT -> {
			if (state == Direction.RIGHT)
				state = null;
		}
		}
	}
}