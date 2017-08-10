package org.rs2server.rs2.content.managers;

import java.text.NumberFormat;
import java.util.Locale;

import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;

public class PlayerInfoManager {
	/**
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handlePlayerInfo(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {
				"Players Online: " + World.getWorld().getPlayers().size() 
				+ "|<img=24> Kills: " + player.getDatabaseEntity().getBountyHunter().getKills() 
				+ "|<img=23> Deaths: " + player.getDatabaseEntity().getBountyHunter().getDeaths() 
				+ "|<img=25> K/D ratio: " + player.getKDR() 
				+ "|<img=41> Bounty Points: " + NumberFormat.getNumberInstance(Locale.ENGLISH).format(player.getDatabaseEntity().getBountyHunter().getBountyShopPoints()) 
				+ "|<img=49> Slayer Points: " + player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() 
				+ "|<img=49> Slayer Consecutive Tasks Completed: " + player.getDatabaseEntity().getStatistics().getSlayerConsecutiveTasksCompleted() 
				+ "|<img=45> Pest Control Points: " + player.getDatabaseEntity().getStatistics().getPestControlPoints() 
				+ "|<img=45> Barrows Chest Count: " + player.getDatabaseEntity().getStatistics().getBarrowsChestCount()
				
				, player.getName() + "'s Info", 0}, "Iss");//Iss
	//	player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
		player.setAttribute("player_info_options", true);
	}
	
	/**
	  * Handles the Options
	 */
	public static boolean handlePlayerInfoOptions(Player player, int option) {
		switch (option) {
		
		
		}
		return false;
	}

}