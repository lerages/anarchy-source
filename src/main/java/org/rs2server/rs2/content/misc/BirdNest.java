package org.rs2server.rs2.content.misc;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.util.Misc;

/**
 * Created by Zaros
 */
public class BirdNest {
	
	static int seeds[] = {5291, 5292, 5293, 5294, 5295, 5296, 5297, 5298, 5299, 
			5291, 5292, 5293, 5294, 5300, 5301, 5302, 5303, 5304};
	
	static int rings[] = {1635, 1635, 1635, 1635, 1635, 1637, 1637, 1637, 1637, 
			1639, 1639, 1639, 1641, 1641, 1643};
	
	final static int EMPTY_NEST = 5075;
	final static int RED_EGG_NEST = 5070;
	final static int GREEN_EGG_NEST = 5071;
	final static int BLUE_EGG_NEST = 5072;
	final static int SEED_NEST = 5073;
	final static int RING_NEST = 5074;
	final static int RED_EGG = 5076;
	final static int BLUE_EGG = 5077;
	final static int GREEN_EGG = 5078;

    public static void empty(Player player, Item nest) 
    {
        if(!player.getInventory().contains(nest.getId()))
        {
        	return;
        }
        if(nest.getId() > 5069 && nest.getId() < 5075)
        {
        	if(player.getInventory().freeSlots() > 1)
        	{
        		player.getInventory().remove(new Item(nest.getId()));
        		player.getInventory().add(new Item(EMPTY_NEST));
            	switch(nest.getId())
            	{
            	case RED_EGG_NEST:
            		player.getInventory().add(new Item(RED_EGG));
            		break;
            	case GREEN_EGG_NEST:
            		player.getInventory().add(new Item(GREEN_EGG));
            		break;
            	case BLUE_EGG_NEST:
            		player.getInventory().add(new Item(BLUE_EGG));
            		break;
            	case SEED_NEST:
            		player.getInventory().add(new Item(getSeed()));
            		break;
            	case RING_NEST:
            		player.getInventory().add(new Item(getRing()));
            		break;
            	}
        	} else {
        		player.sendMessage("Not enough space in your inventory to empty the nest.");
        	}
        	
        }
        	
    }
    
    private static int getRing()
    {
    	int r = Misc.random(rings.length);
    	int ring = 0;
    	
    	ring = rings[r];
    	return ring;
    }
    
    private static int getSeed()
    {
    	int r = Misc.random(seeds.length);
    	int seed = 0;
    	
    	seed = seeds[r];
    	return seed;
    }
}
