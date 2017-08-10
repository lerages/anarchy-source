package org.rs2server.rs2.content.managers;

import java.util.List;
import java.util.Optional;

import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender.DialogueType;
import org.rs2server.rs2.util.Misc;
import org.rs2server.rs2.util.TextUtils;

public class PlayerCommandsManager {
	/**
	 * @author Greco
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handlePlayerCommands(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {"|||||||<col=880000><u=000000>Player Commands|Players Online|Lock Experience|Unlock Experience|Change password - Change your password", "Player Commands Manager", 0}, "Iss");//Iss
		player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
		player.setAttribute("playercommands_menu", true);
	}
	
	/**
	 * @author Greco
	 * Handles the Options for Player Commands
	 */
	public static boolean handlePlayerCommandOptions(Player player, int option) {
		switch (option) {
		case 8:
			player.getActionSender().closeAll();
			player.getActionSender().sendDialogue(null, DialogueType.MESSAGE, -1, null, "Their are currently " + World.getWorld().getPlayers().size() + " players online.");
			player.getActionSender().sendMessage("Their are currently " + World.getWorld().getPlayers().size() + " players online.");
			break;
		case 9:
			player.getActionSender().closeAll();
			String name = TextUtils.upperFirst(" ");
			Optional<Integer> lock = Misc.forSkillName(name);
			if (lock.isPresent()) {
				int skillId = lock.get();
				List<Integer> locked = player.getDatabaseEntity().getPlayerSettings().getLockedSkills();
				if (locked.contains(skillId)) {
					player.getActionSender().sendMessage("This skill is already locked.");
					return false;
				}
				locked.add(skillId);
				player.getActionSender().sendMessage("You have locked your experience for " + name + ".");
				player.getActionSender().sendDialogue(null, DialogueType.MESSAGE, -1, null, "You have locked your experience for " + name + ".");
			}
	//		player.getActionSender().closeAll();
	//		player.getActionSender().sendDialogue(null, DialogueType.MESSAGE, -1, null, "You have locked your experience for " + name + ".");

			break;
		case 10:
			player.getActionSender().closeAll();
			String name1 = TextUtils.upperFirst(" ");
			Optional<Integer> lock1 = Misc.forSkillName(name1);
			if (lock1.isPresent()) {
				int skillId = lock1.get();
				List<Integer> locked = player.getDatabaseEntity().getPlayerSettings().getLockedSkills();
				if (locked.contains(skillId)) {
					locked.remove(locked.indexOf(skillId));
					player.getActionSender().sendMessage("You have unlocked your experience for " + name1 + ".");
					player.getActionSender().sendDialogue(null, DialogueType.MESSAGE, -1, null, "You have unlocked your experience for " + name1 + ".");
				} else {
					player.getActionSender().sendMessage("This skill is currently unlocked.");
				}
			}
			break;
		case 11:
			player.getActionSender().sendMessage("In order to use this command please type ::changepass new password here");
			break;
		}
		return false;
	}

}