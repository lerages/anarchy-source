package org.rs2server.rs2.content.misc;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.content.misc.YoungImpJar;

import java.util.*;

/**
 * Created by Paine on 25 Feb 17
 */
public class YoungImpJar {


    private static final List<Item> REWARD_ITEMS = new ArrayList<>();
    private static final List<Item> RARE_ITEMS = new ArrayList<>();

    private final Player player;
    private final PlayerService service;
    private static final Item YoungImpJar = new Item(11240);
    private final Random random;

    public YoungImpJar(Player player) {
        this.player = player;
        this.service = Server.getInjector().getInstance(PlayerService.class);
        this.random = new Random();
    }

    public Optional<Item> getRewardItem() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(YoungImpJar)) {
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
        REWARD_ITEMS.add(new Item(1539, 30));//nails
        REWARD_ITEMS.add(new Item(7936, 40));//pure ess
        REWARD_ITEMS.add(new Item(454, 5));//coal
        REWARD_ITEMS.add(new Item(1353, 1));//steel axe
        REWARD_ITEMS.add(new Item(232, 5));//snape
        REWARD_ITEMS.add(new Item(1744, 10));//hard leather
        REWARD_ITEMS.add(new Item(66, 1));//yew long
        
    }
}
