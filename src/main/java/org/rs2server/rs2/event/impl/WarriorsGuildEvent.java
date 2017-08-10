package org.rs2server.rs2.event.impl;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.minigame.warriorsguild.WarriorsGuild;
import org.rs2server.rs2.model.player.Player;

import java.util.Iterator;

public class WarriorsGuildEvent extends Event{

	public WarriorsGuildEvent() {
		super(60000);
	}

	private static final Item REMOVE = new Item(WarriorsGuild.TOKENS, 10);
	
	@Override
	public void execute() {
		for (Player p : WarriorsGuild.IN_GAME) {
			if (Constants.hasAttackCape(p)) {
				continue;
			}
			p.getInventory().remove(REMOVE);
			p.getActionSender().sendMessage("Ten of your tokens crumble away.");
			Item item = p.getInventory().getById(WarriorsGuild.TOKENS);
			if (item == null || item.getCount() < 10) {
				p.getWarriorsGuild().outOfTokens();
			}
		}
	}

}
