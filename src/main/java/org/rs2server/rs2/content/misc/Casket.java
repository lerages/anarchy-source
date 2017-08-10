package org.rs2server.rs2.content.misc;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;

import java.util.*;

/**
 * Created by Paine
 */
public class Casket {


    private static final List<Item> REWARD_ITEMS = new ArrayList<>();
    private static final List<Item> RARE_ITEMS = new ArrayList<>();

    private final Player player;
    private final PlayerService service;
    private static final Item CASKET = new Item(405);
    private final Random random;

    public Casket(Player player) {
        this.player = player;
        this.service = Server.getInjector().getInstance(PlayerService.class);
        this.random = new Random();
    }

    public Optional<Item> getRewardItem() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(CASKET)) {
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
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 400)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 800)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 800)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 800)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 800)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 800)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 1350)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 1350)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 1350)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 1900)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 1900)));
        REWARD_ITEMS.add(new Item(995, Misc.random(8, 3000)));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1623, 1));
        REWARD_ITEMS.add(new Item(1621, 1));
        REWARD_ITEMS.add(new Item(1619, 1));
        REWARD_ITEMS.add(new Item(1454, 1));
        
        RARE_ITEMS.add(new Item(1617));
        RARE_ITEMS.add(new Item(985));
        RARE_ITEMS.add(new Item(987));

    }
}
