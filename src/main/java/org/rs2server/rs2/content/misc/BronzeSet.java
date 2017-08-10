package org.rs2server.rs2.content.misc;

//import org.rs2server.Server;
//import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

import java.util.*;

/**
 * Created by Paine
 */
public class BronzeSet {


    private static final List<Item> TOP = new ArrayList<>();
    private static final List<Item> LEGS = new ArrayList<>();

    private final Player player;
    private static final Item BRONZE_SET = new Item(12960);

    public BronzeSet(Player player) {
        this.player = player;
    }

    public Optional<Item> getRewardItem() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(BRONZE_SET)) {
            return Optional.empty();
        }
//        items.addAll(getRandom() > 4 ? TOP : LEGS);
        Collections.shuffle(items);
        return Optional.of(items.get(0));
    }


    static {
        
        TOP.add(new Item(1155));//helm
        TOP.add(new Item(1117));//plate
        LEGS.add(new Item(1075));//legs
        LEGS.add(new Item(1189));//shield

    }
}
