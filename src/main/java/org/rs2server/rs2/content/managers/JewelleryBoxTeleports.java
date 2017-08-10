package org.rs2server.rs2.content.managers;

import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;

public class JewelleryBoxTeleports {
	
	/**
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handleJewelBox(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {"|||||1: Ring of Dueling: Al Kharid Arena|2: Ring of Dueling: Castle Wars Arena|3: Ring of Dueling: Clan Wars Arena|4: Necklace of Minigames: Burthorpe|5: Necklace of Minigames: Barbarian Outpost|6: Necklace of Minigames: Corporeal Beast|7: Necklace of Minigames: Tears of Guthix|8: Combat bracelet: Warriors' Guild|9: Combat bracelet: Champions' Guild|A: Combat bracelet: Monastery|B: Combat bracelet: Ranging Guild|C: Skills necklace: Fishing Guild|D: Skills necklace: Motherlode Mine|E: Skills necklace: Crafting Guild|F: Skills necklace: Cooking Guild|G: Skills necklace: Woodcutting Guild|H: Ring of Wealth: Miscellania|I: Ring of Wealth: Grand Exchange|J: Ring of Wealth: Falador Park|K: Ring of Wealth: Dondakan's Rock|L: Amulet of Glory: Edgeville|M: Amulet of Glory: Karamja|N: Amulet of Glory: Draynor Village|O: Amulet of Glory: Al Kharid", "Jewellery teleports", 0}, "Iss");//Iss
		player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
		player.setAttribute("jewellery_box_teleport", true);
	}
	
	/**
	 * Handles the Options
	 */
	public static boolean handleJewelBoxOptions(Player player, int option) {
		switch (option) {
		
		
		}
		return false;
	}

}