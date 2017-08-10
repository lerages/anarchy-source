package org.rs2server.rs2.event.impl;

import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.Shop;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;

public class ShopDecayTick extends Event {

	public ShopDecayTick() {
		super(3600000);
	}

	@Override
	public void execute() {
		Shop.reloadShops();
	}

}
