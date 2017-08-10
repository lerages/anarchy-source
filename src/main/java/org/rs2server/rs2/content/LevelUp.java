package org.rs2server.rs2.content;

import org.rs2server.rs2.model.DialogueManager;
import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;

public class LevelUp {
	
private static Graphic levelUp = Graphic.create(199, 2);
	
	/**
	 * Called when a player levels up.
	 * 
	 * @param player The player.
	 * @param skill The skill id.
	 */
	public static void levelUp(Player player, int skill) {
		player.setAttribute("leveledUp", skill);
		player.setAttribute("leveledUp[" + skill + "]", Boolean.TRUE);
		player.playGraphics(levelUp);
		DialogueManager.openDialogue(player, skill + 100);
		player.getActionSender().sendMessage("Congratulations, you just advanced a " + Skills.SKILL_NAME[skill] + " level");
		if (player.getSkills().getLevelForExperience(skill) >= 99) {
			World.getWorld().sendWorldMessage("<img=32><col=0407a3>News: " + player.getName() + " has just achieved 99 " + Skills.SKILL_NAME[skill] + ".");
		}
	}

}
