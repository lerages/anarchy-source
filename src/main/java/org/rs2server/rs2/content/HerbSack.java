package org.rs2server.rs2.content;

import java.util.Arrays;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.ItemDefinition;
import org.rs2server.rs2.model.player.Player;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class HerbSack {

	private Player player;

	public HerbSack(Player player) {
		this.player = player;
	}

	private Multiset<Integer> herbSack = HashMultiset.create();

	public Multiset<Integer> getContents() {
		return herbSack;
	}

	public void handleFillSack() {
		player.getActionSender().sendMessage("You search your inventory for herbs appropriate to put in the sack...");
		if (Arrays.stream(player.getInventory().getContents()).noneMatch(isGrimyHerb())) {
			player.getActionSender().sendMessage("There is no herbs in your inventory that can be added to the sack.");
			return;
		}
		Arrays.stream(player.getInventory().getContents()).filter(isGrimyHerb()).forEach(herb -> {
			if (herbSack.count(herb.getId()) < 30) {
				player.getInventory().remove(herb);
				herbSack.add(herb.getId());
			}
		});
		player.getActionSender().sendMessage("You add the herb(s) to your sack.");
	}

	public void handleEmptySack() {
		if (herbSack.isEmpty()) {
			player.getActionSender().sendMessage("The herb sack is already empty.");
			return;
		}
		if (player.getInventory().freeSlots() <= 0) {
			player.getActionSender()
					.sendMessage("You don't have enough inventory space to empty the contents of this sack.");
			return;
		}
		for (Iterator<Integer> i = herbSack.iterator(); i.hasNext();) {
			if (player.getInventory().freeSlots() <= 0) {
				return;
			}
			int herb = i.next();
			player.getInventory().add(new Item(herb));
			i.remove();
		}
	}

	public void handleCheckSack() {
		player.getActionSender().sendMessage("You look in your herb sack and see:");
		if (herbSack.isEmpty()) {
			player.getActionSender().sendMessage("The herb sack is empty.");
			return;
		}
		for (int herbId : Multisets.copyHighestCountFirst(herbSack).elementSet()) {
			player.getActionSender()
					.sendMessage(herbSack.count(herbId) + " x " + CacheItemDefinition.get(herbId).getName());
		}
	}

	private Predicate<Item> isGrimyHerb() {
		return herb -> Objects.nonNull(herb) && herb.getDefinition2().getName().contains("Grimy");
	}

}