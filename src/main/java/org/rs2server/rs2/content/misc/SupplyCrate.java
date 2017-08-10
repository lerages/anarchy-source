package org.rs2server.rs2.content.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

public class SupplyCrate {
	 private static final List<Item> REWARD_ITEMS = new ArrayList<>();
	    private static final List<Item> RARE_ITEMS = new ArrayList<>();

	    private final Player player;
	    private final PlayerService service;
	    private static final Item SUPPLY_CRATE = new Item(20703);
	    private final Random random;

	    public SupplyCrate(Player player) {
	        this.player = player;
	        this.service = Server.getInjector().getInstance(PlayerService.class);
	        this.random = new Random();
	    }

	    public Optional<Item> getRewardItem() {
	        List<Item> items = new ArrayList<>();
	        if (!player.getInventory().hasItem(SUPPLY_CRATE)) {
	            return Optional.empty();
	        }
	        items.addAll(getRandom() > 2 ? REWARD_ITEMS : RARE_ITEMS);
	        Collections.shuffle(items);
	        return Optional.of(items.get(0));
	    }


	    private int getRandom() {
	        return random.nextInt(100);
	    }


	    static {
	    	REWARD_ITEMS.add(new Item(1522, 148));
	    	REWARD_ITEMS.add(new Item(1520, 17));
	    	REWARD_ITEMS.add(new Item(1518, 16));
	    	REWARD_ITEMS.add(new Item(6334, 59));
	    	REWARD_ITEMS.add(new Item(1516, 49));
	    	REWARD_ITEMS.add(new Item(1514, 20));
	    	REWARD_ITEMS.add(new Item(1624, 5));
	    	REWARD_ITEMS.add(new Item(1622, 5));
	    	REWARD_ITEMS.add(new Item(1618, 5));
	    	REWARD_ITEMS.add(new Item(1620, 5));
	    	REWARD_ITEMS.add(new Item(454, 12));
	    	REWARD_ITEMS.add(new Item(441, 8));
	    	REWARD_ITEMS.add(new Item(443, 12));
	    	REWARD_ITEMS.add(new Item(445, 70));
	    	REWARD_ITEMS.add(new Item(448, 7));
	    	REWARD_ITEMS.add(new Item(450, 15));
	    	REWARD_ITEMS.add(new Item(452, 2));
	    	REWARD_ITEMS.add(new Item(199, 6));
	    	REWARD_ITEMS.add(new Item(201, 6));
	    	REWARD_ITEMS.add(new Item(211, 3));
	    	REWARD_ITEMS.add(new Item(215, 3));
	    	REWARD_ITEMS.add(new Item(207, 3));
	    	REWARD_ITEMS.add(new Item(5312, 1));
	    	REWARD_ITEMS.add(new Item(5313, 2));
	    	REWARD_ITEMS.add(new Item(5295, 3));
	    	REWARD_ITEMS.add(new Item(5293, 3));
	    	REWARD_ITEMS.add(new Item(5296, 3));
	    	REWARD_ITEMS.add(new Item(5294, 3));
	    	REWARD_ITEMS.add(new Item(5300, 4));
	    	REWARD_ITEMS.add(new Item(5284, 3));
	    	REWARD_ITEMS.add(new Item(5314, 3));
	    	REWARD_ITEMS.add(new Item(5315, 2));
	    	REWARD_ITEMS.add(new Item(5316, 1));
	    	REWARD_ITEMS.add(new Item(5317, 1));
	    	REWARD_ITEMS.add(new Item(5321, 7));
	    	REWARD_ITEMS.add(new Item(322, 12));
	    	REWARD_ITEMS.add(new Item(336, 12));
	    	REWARD_ITEMS.add(new Item(332, 12));
	    	REWARD_ITEMS.add(new Item(378, 9));
	    	REWARD_ITEMS.add(new Item(360, 12));
	    	REWARD_ITEMS.add(new Item(372, 21));
	    	REWARD_ITEMS.add(new Item(384, 21));
	    	REWARD_ITEMS.add(new Item(995, 9048));
	    	REWARD_ITEMS.add(new Item(13422, 24));
	    	REWARD_ITEMS.add(new Item(3212, 7));


	    	REWARD_ITEMS.add(new Item(7937, 391));
	    	REWARD_ITEMS.add(new Item(20718, 31));



	        RARE_ITEMS.add(new Item(20714));//Tome of fire
	        RARE_ITEMS.add(new Item(20720));//Bruma Torch
	        RARE_ITEMS.add(new Item(20693));//Phoenix
	        //RARE_ITEMS.add(new Item(6739));//Dragon axe
	        RARE_ITEMS.add(new Item(20708));//Pyromancer hood
	        RARE_ITEMS.add(new Item(20704));//Pyromancer garb
	        RARE_ITEMS.add(new Item(20706));//Pyromancer robe
	        RARE_ITEMS.add(new Item(20710));//Pyromancer boots
	        RARE_ITEMS.add(new Item(20712));//Warm gloves

	    }
	}