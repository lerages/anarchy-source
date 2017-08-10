package org.rs2server.rs2.content.managers;

import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;

public class DukesCombatTraining {
	
	/**
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handleDuke(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {"|||||1: Attack|2: Strength|3: Defence|4: Hitpoints|5: Magic|6: Ranging|7: (Cancel)", "Duke's combat training", 0}, "Iss");//Iss
		player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
		player.setAttribute("dukes_combat_training", true);
	}
	
	/**
	  * Handles the Options
	 */
	public static boolean handleDukeOptions(Player player, int option) {
		switch (option) {
		
		
		}
		return false;
	}

}