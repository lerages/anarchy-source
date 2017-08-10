package org.rs2server.rs2.domain.service.impl.content;

import org.rs2server.rs2.domain.service.api.content.TeleportInterfaceService;
import org.rs2server.rs2.model.player.Player;

public class TeleportInterfaceServiceImpl implements TeleportInterfaceService {

	public static final int INTERFACE = 187;
	
	@Override
	public void openInterface(Player player) {
		player.getActionSender().sendInterface(INTERFACE, false)
		.sendInterfaceConfig(INTERFACE, 0, false)
		.sendInterfaceConfig(INTERFACE, 1, false)
		.sendInterfaceConfig(INTERFACE, 2, false)
		.sendInterfaceConfig(INTERFACE, 3, false)
		;
		
		
	}

	@Override
	public void handleInterfaceActions(Player player, int button, int childButton, int childButton2, int menuIndex) {
		switch(childButton) {
		
		case 0:
			switch(childButton2) {
			case 2:
				player.getActionSender().sendString(INTERFACE, 2, "Teleport Test");
			break;
			}
			break;
		case 3:
			switch(childButton2) {
			case 0:
				player.getActionSender().sendString(INTERFACE, 0, "Edgeville");
			break;
			case 1:
				player.getActionSender().sendString(INTERFACE, 1, "Varrock");
			break;
			case 2:
				player.getActionSender().sendString(INTERFACE, 2, "Falador");
			break;
			case 3:
				player.getActionSender().sendString(INTERFACE, 3, "Lumbridge");
			break;
			}
			break;
		
		}
		
	}

}
