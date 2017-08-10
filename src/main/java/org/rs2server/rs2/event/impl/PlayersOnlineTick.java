package org.rs2server.rs2.event.impl;

import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.World;

public class PlayersOnlineTick extends Event {

	public PlayersOnlineTick() {
		super(120000);
	}

	@Override
	public void execute() {
		World.getWorld().sendWorldMessage("<img=32><col=42a4f4>News: There are " + World.getWorld().getPlayers().size() + " online.");
	}

}
