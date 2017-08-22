package org.rs2server.rs2.event.impl;

import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.Shop;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;

public class TimedServerMessage extends Event {
	
	private int last_message_index;
	private String messages[] = 
	{
			"You can steal silk and sell it to the silk trader outside of Edgeville bank for "
			+ "some easy beginner money!",
			
			"The gnome glider is in many cities and can take you almost anywhere!",
			
			"You can donate and receive a variety of useful perks for your account visit "
			+ "www.os-anarchy.com/donations for more information!",
			
			"Please remember to vote; the more you vote, the more OS-Anarchy grows!",
			
			"You can view our community forums at www.forums.os-anarchy.com",
			
			"Do not hesitate to contact 'Zero' or 'Zaros' if you need administrative assistance.",	
			
			"Looking for the shops? You can find them in Edgeville's general store building!",
			
			"You can find monsters and resources in the exact same locations as you would in "
			+ "Oldschool Runescape; the gnome glider will help you get around.",
			
			"Did you know? Being a donator also has additional bonuses, "
			+ "speak to the donation guide at home for more information!"
	};
	public TimedServerMessage() {
		super(600000); //600000 = 10mins
	}

	@Override
	public void execute() {
		int r = Misc.random(0, messages.length - 1);
		if(r != last_message_index)
		{
			World.getWorld().sendWorldMessage("<img=32><col=012763> "+ messages[r]);
			last_message_index = r;
			System.out.println("TIMED MESSAG SENT: " + messages[r]);
		}
		
	}

}
