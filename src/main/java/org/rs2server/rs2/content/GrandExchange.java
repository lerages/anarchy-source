package org.rs2server.rs2.content;

import org.rs2server.rs2.model.player.Player;

public class GrandExchange {
	
	public static void open(Player player) {
		player.getActionSender()
		.sendCS2Script(828, new Object[]{1}, "i")
		.sendCS2Script(917, new Object[]{-1, -1}, "ii")
		.sendInterfaceInventory(467)
		.sendInterface(465, false)
		.sendGEAccess(2, 2, 465, 6, 6)
		.sendGEAccess(3, 4, 465, 6, 2)
		.sendGEAccess(2, 2, 465, 7, 6)
		.sendGEAccess(3, 4, 465, 7, 2)
		.sendGEAccess(2, 2, 465, 8, 6)
		.sendGEAccess(3, 4, 465, 8, 2)
		.sendGEAccess(2, 2, 465, 9, 6)
		.sendGEAccess(3, 4, 465, 9, 2)
		.sendGEAccess(2, 2, 465, 10, 6)
		.sendGEAccess(3, 4, 465, 10, 2)
		.sendGEAccess(2, 2, 465, 11, 6)
		.sendGEAccess(3, 4, 465, 11, 2)
		.sendGEAccess(2, 2, 465, 12, 6)
		.sendGEAccess(3, 4, 465, 12, 2)
		.sendGEAccess(2, 2, 465, 13, 6)
		.sendGEAccess(3, 4, 465, 13, 2)
		.sendGEAccess(0, 0, 465, 21, 2)
		.sendGEAccess(2, 3, 465, 22, 1038)
		.sendGEAccess(0, 0, 465, 5, 6)
		.sendGEAccess(0, 13, 465, 23, 2)
		.sendGEAccess(0, 27, 467, 0, 1026)
		.sendConfig(1043, 0)
		.sendConfig(563, 0)
		.sendConfig(375, 0)
		.sendConfig(1151, -1)
		/*.sendString(465, 24, "Click the icon on the left to search for items.")
		.sendString(465, 25, "")*/
		//.sendCS2Script(915, new Object[]{3}, "i")
		;
		
		/* Update Items:
		Interface Id: -1, Child: 63784, Type: 518
		Interface Id: -1, Child: 63783, Type: 519
		Interface Id: -1, Child: 63782, Type: 520
		Interface Id: -1, Child: 63781, Type: 521
		Interface Id: -1, Child: 63780, Type: 522
		Interface Id: -1, Child: 63779, Type: 523
		Interface Id: -1, Child: 63763, Type: 539
		Interface Id: -1, Child: 63762, Type: 540
		 */
	}

}
