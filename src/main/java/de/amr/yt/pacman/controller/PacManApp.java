/**
 * 
 */
package de.amr.yt.pacman.controller;

/**
 * @author Armin Reichert
 */
public class PacManApp {
	public static void main(String[] args) {
		var gameController = new GameController();
		gameController.createAndShowUI();
		gameController.startSimulation();
	}
}
