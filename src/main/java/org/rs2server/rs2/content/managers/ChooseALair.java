package org.rs2server.rs2.content.managers;

import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;

public class ChooseALair {
	/**
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handleLair(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {"|||||1: Empty|2: Kraken|3: Zulrah|4: Kalphite Queen|5: Cerberus|6: Abyssal Sire|7: Skotizo", "Choose a lair", 0}, "Iss");
		player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
		player.setAttribute("choose_a_lair", true);
	}
	
	/**
	 * Handles the Options
	 */
	public static boolean handleLairOptions(Player player, int option) {
		switch (option) {
		
		
		}
		return false;
	}

}