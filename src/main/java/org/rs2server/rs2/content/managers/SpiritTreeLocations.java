package org.rs2server.rs2.content.managers;

import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;

public class SpiritTreeLocations {
	/**
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handleSpirit(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {"|||||1: Tree Gnome Village|2: Gnome Stronghold|3: Battlefield of Khazard|4: Grand Exchange|5: Port Sarim|6: Etceteria|7: Brimhaven|8: Kourend|9: Cancel", "Spirit Tree Locations", 0}, "Iss");//Iss
		player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
		player.setAttribute("spirit_tree_locations", true);
	}
	
	/**
	 * Handles the Options
	 */
	public static boolean handleSpiritOptions(Player player, int option) {
		switch (option) {
		
		
		}
		return false;
	}

}