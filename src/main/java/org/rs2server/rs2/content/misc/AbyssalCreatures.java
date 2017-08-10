package org.rs2server.rs2.content.misc;

import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.DialogueManager;
import org.rs2server.rs2.model.GroundItem;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.npc.MetalArmour;
import org.rs2server.rs2.model.npc.NPCDefinition;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.Agility;
import org.rs2server.rs2.net.ActionSender.DialogueType;
import org.rs2server.rs2.tickable.Tickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbyssalCreatures {

	private static final Random r = new Random();

	private Item pouchDrop = null;

	public void killedAbyssalCreature(Player attacker, Location pos) {
			if(r.nextInt(41) == 0) {
				//attacker.getActionSender().sendDialogue("", DialogueType.MESSAGE, -1, null, "You've received a defender, After picking it up leave and come back in to<br>work towards the next one!");
				pouchDrop = getPouchDrop(attacker);
				if(pouchDrop != null)
				{
					attacker.getActionSender().sendMessage("<col=880000>A pouch has been left behind from the remains of the abyssal creature.</col>");
					World.getWorld().register(new GroundItem(attacker.getName(), pouchDrop, pos), attacker);
				}
				else
				{
					attacker.getActionSender().sendMessage("<col=880000>The pouch degraded upon dropping and left a large stack of coins behind.</col>");
					World.getWorld().register(new GroundItem(attacker.getName(), new Item(995, 10000), pos), attacker);
				}
				
		}
	}
	
	private Item getPouchDrop(Player player) {
		Item bestPouch = null;
		boolean containsPouches = false;
		if(player.getBank().contains(5509) || player.getInventory().contains(5509))
		{
			bestPouch = new Item(5510);
			containsPouches = true;		
		}
			 
		if(player.getBank().contains(5510) || player.getInventory().contains(5510))
		{
			bestPouch = new Item(5512);
			containsPouches = true;
		}
			
		if(player.getBank().contains(5512) || player.getInventory().contains(5512))
		{
			bestPouch = new Item(5514);
			containsPouches = true;
		}
			
		if(player.getBank().contains(5514) || player.getInventory().contains(5514))
		{
			bestPouch = null;
			containsPouches = true;
		}
		if(containsPouches == false)	
		bestPouch = new Item(5509);
		
		return bestPouch;
	}
}

