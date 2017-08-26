package org.rs2server.rs2.content.misc;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.GameObject;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Sound;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.util.Misc;

/**
 * Created by Zaros
 */
public class Picking {
   
	private static final Item FLAX = new Item(1779);
	private static final Item POTATO = new Item(1942);
	private static final Item CABBAGE = new Item(1965);
	private static final Item ONION = new Item(1957);
	private static final Item GRAIN = new Item(1947);
	
	private static final int FLAX_OBJECT_ID = 14896;
	private static final int POTATO_OBJECT_ID = 312;
	private static final int CABBAGE_OBJECT_ID = 1161;
	private static final int ONION_OBJECT_ID = 3366;
	private static final int GRAIN_OBJECT_ID = 15507;

    public static void Pick(Player player, GameObject object)
    {
    	if (System.currentTimeMillis() - player.getLastHarvest() > 600) {
            player.setLastHarvest(System.currentTimeMillis());
            if (player.getInventory().add(getPickable(object.getId()), 1)) {
                player.playAnimation(Animation.create(827));
                
                if(object.getId() == FLAX_OBJECT_ID || object.getId() == GRAIN_OBJECT_ID)
                	player.getActionSender().sendMessage("You manage to pick some " + object.getDefinition().getName() + "...");
                else
                	player.getActionSender().sendMessage("You manage to pick a " + object.getDefinition().getName() + "...");
                
                if (object.getId() == FLAX_OBJECT_ID && Misc.random(3) == 0)
                    World.getWorld().replaceObject(object, null, 37);
                else 
                	World.getWorld().replaceObject(object, null, 22);
                
                player.getActionSender().playSound(Sound.PICK_FLAX);
            }
        }
    }
    
    private static Item getPickable(int object_id)
    {
    	Item item = null;
    	switch(object_id)
    	{
    	case FLAX_OBJECT_ID:
    		item = FLAX;
    		break;
    	case GRAIN_OBJECT_ID:
    		item = GRAIN;
    		break;	
    	case ONION_OBJECT_ID:
    		item = ONION;
    		break;
    	case POTATO_OBJECT_ID:
    		item = POTATO;
    		break;
    	case CABBAGE_OBJECT_ID:
    		item = CABBAGE;
    		break;
    	}
    	
    	return item;
    }
    
}
