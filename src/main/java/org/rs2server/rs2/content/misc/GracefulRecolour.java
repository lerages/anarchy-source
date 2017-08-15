package org.rs2server.rs2.content.misc;

import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.Constants;

/**
 * Created by Zaros
 */
public class GracefulRecolour {
	
	private static final int MARK_OF_GRACE = 11849;

	public static void recolourGraceful(Player player, int[] colour)
	{
		int[] owned_graceful = getOwnedGraceful(player);
		
		if(owned_graceful != null && player.getInventory().containsItems(owned_graceful))
		{
			if(player.getInventory().getCount(MARK_OF_GRACE) >= 90)
			{
				player.getInventory().removeItems(owned_graceful);
				player.getInventory().remove(new Item(MARK_OF_GRACE, 90));
				player.getInventory().addItems(colour);
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11850, null, 
						"You recolour your completed graceful outfit.");
			} else {
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, MARK_OF_GRACE, null, 
						"You don't have enough marks of grace to recolour your graceful outfit.");
			}
		} else {
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11850, null, 
					"You don't have a completed graceful outfit to recolour.");
		}
	}
	
	private static int[] getOwnedGraceful(Player player)
	{
		if(player.getInventory().containsItems(Constants.PURPLE_GRACEFUL))
			return Constants.PURPLE_GRACEFUL;
		else if(player.getInventory().containsItems(Constants.TEAL_GRACEFUL))
			return Constants.TEAL_GRACEFUL;
		else if(player.getInventory().containsItems(Constants.YELLOW_GRACEFUL))
			return Constants.YELLOW_GRACEFUL;
		else if(player.getInventory().containsItems(Constants.RED_GRACEFUL))
			return Constants.RED_GRACEFUL;
		else if(player.getInventory().containsItems(Constants.GREEN_GRACEFUL))
			return Constants.GREEN_GRACEFUL;
		else if(player.getInventory().containsItems(Constants.WHITE_GRACEFUL))
			return Constants.WHITE_GRACEFUL;
		else if(player.getInventory().containsItems(Constants.GRACEFUL))
			return Constants.GRACEFUL;
		else 
			return null;
	}
}
