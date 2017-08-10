package org.rs2server.rs2.content.misc;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;

import java.util.*;

/**
 * Created by Paine
 */
public class BagFullOfGems {
   
    private static final int SAPPHIRE = 1624;
    private static final int EMERALD = 1622;
    private static final int RUBY = 1620;
    private static final int DIAMOND = 1618;
    private static final int DRAGONSTONE = 1632;
    private static final int ONYX = 6572;
    
    int gem_count = 40;
    private final Player player;
    private final PlayerService service;
    private static final Item BAG_FULL_OF_GEMS = new Item(19473);
    private final Random random;

    public BagFullOfGems(Player player) {
        this.player = player;
        this.service = Server.getInjector().getInstance(PlayerService.class);
        this.random = new Random();
    }

    public List<Item> getGems() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(BAG_FULL_OF_GEMS)) {
        	items = null;
            return items;
        }
        for(int i= 0; i < gem_count; i++)
        {
        	int r = Misc.random(2500000);
        	if(r < 1250000)
        	{
        		items.add(new Item(SAPPHIRE));
        	}
        	if(r >= 1250000 && r < 2110000)
        	{
        		items.add(new Item(EMERALD));
        	}
        	if(r >= 2110000 && r < 2410000)
        	{
        		items.add(new Item(RUBY));
        	}
        	if(r > 2410000 && r < 2490000)
        	{
        		items.add(new Item(DIAMOND));
        	}
        	if(r > 2490000 && r < 2499999)
        	{
        		items.add(new Item(DRAGONSTONE));
        	}
        	if(r == 2500000)
        	{
        		items.add(new Item(ONYX));
            	World.getWorld().sendWorldMessage("<col=884422><img=33> News:" + player.getName() + " has just received an uncut onyx.");	
        	}
        }
        //Collections.shuffle(items);
        return items;
    }
}
