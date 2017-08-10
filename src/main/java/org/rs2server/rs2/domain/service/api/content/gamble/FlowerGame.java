package org.rs2server.rs2.domain.service.api.content.gamble;

import org.rs2server.rs2.model.GameObject;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;

public class FlowerGame {
	
	public int[] flowerGame = {15846, 15872};
	int flower = flowerGame[Misc.random(1)];

	/**
	 * If you meet the coin requirements the game starts
	 */
	public void execute(final Player player) {
		if (player.getInventory().hasItem(new Item(995, player.flowerBetAmount))) {
			playGame(player);
		} else {
			broke(player);
		}
	}
	
	/**
	 * Handles Playing of Flower Game.
	 */
	public void playGame(final Player player) {
			if (player.flowerGuess == flower) {
				win(player);
			} else {
				loser(player);
			}
		}
	
	/**
	 * Handles winning of Flower Game.
	 */
	public void win(final Player player) {
		player.getInventory().addItems(995, player.flowerBetAmount*2);
		player.getActionSender().sendMessage("Congratulations, you've won!");
		reset(player);
		placeObject(player);
	}
	
	/**
	 * Handles losing of Flower Game.
	 */
	public void loser(final Player player) {
		player.getInventory().remove(995, player.flowerBetAmount);
		player.getActionSender().sendMessage("Sorry, you've lost.");
		reset(player);
		placeObject(player);
	}
	
	/**
	 * Handles Placement of Flower.
	 */
	public void placeObject(final Player player) {
		player.getActionSender().closeAll();
		new GameObject(gameObject.getLocation(), flower, 10, 0, false);
	}
	
	/**
	 * When you don't have enough coins this is where its handled.
	 */
	public void broke(final Player player) {
		player.getActionSender().closeAll();
		player.getActionSender().sendMessage("Their are not enough coins in your inventory.");
	}
	
	/**
	 * Reset bet amount
	 */
	
	public void reset(final Player player) {
		player.flowerBetAmount -= player.flowerBetAmount;
	}
	
		/**
		 * Others
		 */
		private GameObject gameObject;
	}