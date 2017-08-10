package org.rs2server.rs2.tickable.impl;

import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.Tickable;

public class SystemUpdateTick extends Tickable{

	public SystemUpdateTick() {
		super(2);
	}

	@Override
	public void execute() {
		if (World.systemUpdate) {
			World.updateTimer -= 1;
			for (Player player : World.getWorld().getPlayers())
			{
				if (player == null)
					continue;
				if (!player.isActive())
					continue;
				player.getActionSender().sendSystemUpdate(World.updateTimer * 2);
				if (World.updateTimer == -1) {
					World.getWorld().getWorldLoader().savePlayer(player);
					player.getActionSender().sendLogout();
				}
			}
		}
		if (World.updateTimer == -1 && World.systemUpdate) {
			for (Player player : World.getWorld().getPlayers()) 
			{
				if (player != null) 
				{
					World.getWorld().getWorldLoader().savePlayer(player);
				}
			}
			
			World.systemUpdate = false;
			stop();
			System.exit(-1);
		}
	}

}
