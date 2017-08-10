package org.rs2server.rs2.content.misc;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

import java.util.*;

/**
 * Created by Tim on 11/8/2015.
 */
public class MysteryBox {


    private static final List<Item> REWARD_ITEMS = new ArrayList<>();
    private static final List<Item> RARE_ITEMS = new ArrayList<>();

    private final Player player;
    private final PlayerService service;
    private static final Item MYSTERY_BOX = new Item(6199);
    private final Random random;

    public MysteryBox(Player player) {
        this.player = player;
        this.service = Server.getInjector().getInstance(PlayerService.class);
        this.random = new Random();
    }

    public Optional<Item> getRewardItem() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(MYSTERY_BOX)) {
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
        /**
         * Full skeleton
         */
        for (int i = 9921; i <= 9925; i++) {
            REWARD_ITEMS.add(new Item(i));
        }
        /**
         * Full chicken
         */
        for (int i = 11019; i <= 11022; i++) {
            REWARD_ITEMS.add(new Item(i));
        }
        /**
         * Full camo
         */
        for (int i = 6654; i <= 6656; i++) {
            REWARD_ITEMS.add(new Item(i));
        }
        /**
         * Silly jester
         */
        for (int i = 10836; i <= 10839; i++) {
            REWARD_ITEMS.add(new Item(i));
        }
        REWARD_ITEMS.add(new Item(6666));//Flipper
        REWARD_ITEMS.add(new Item(7003));//Camel mask
        REWARD_ITEMS.add(new Item(9920));//Jack'O lantern mask
        REWARD_ITEMS.add(new Item(10507));//Reindeer hat
        REWARD_ITEMS.add(new Item(1037));//Bunny ears
        REWARD_ITEMS.add(new Item(5607));//Sack of grain
        REWARD_ITEMS.add(new Item(1419));//Scythe
        REWARD_ITEMS.add(new Item(6856));//Bobble hat
        REWARD_ITEMS.add(new Item(6857));//Bobble scarf
        REWARD_ITEMS.add(new Item(6860));//Tri-jester hat
        REWARD_ITEMS.add(new Item(6861));//Tri-jester scarf
        REWARD_ITEMS.add(new Item(6862));//Woolly hat
        REWARD_ITEMS.add(new Item(6863));//Woolly scarf
        REWARD_ITEMS.add(new Item(9470));//Gnome scarf
        REWARD_ITEMS.add(new Item(10394));//Flared Trousers
        REWARD_ITEMS.add(new Item(995, 50000));//100k coins
		REWARD_ITEMS.add(new Item(5509));//rune pouches
		REWARD_ITEMS.add(new Item(5510));//rune pouches
		REWARD_ITEMS.add(new Item(5512));//rune pouches
		REWARD_ITEMS.add(new Item(5514));//rune pouches
		REWARD_ITEMS.add(new Item(1333));//Rune scimitar
		REWARD_ITEMS.add(new Item(4587));//Dragon scimitar
		REWARD_ITEMS.add(new Item(4675));//Ancient staff
		REWARD_ITEMS.add(new Item(5698));//DDS
		REWARD_ITEMS.add(new Item(1079));//Rune platelegs
		REWARD_ITEMS.add(new Item(1127));//Rune platebody
		REWARD_ITEMS.add(new Item(10507));//reindeer hat
//		REWARD_ITEMS.add(new Item(4585));//Dragon plateskirt
//        REWARD_ITEMS.add(new Item(1187));//Dragon sq shield
        REWARD_ITEMS.add(new Item(2572));//Ring of wealth
        REWARD_ITEMS.add(new Item(10394));//flared trousers
        REWARD_ITEMS.add(new Item(20217));//team i cape
        REWARD_ITEMS.add(new Item(11918));//nunchaku
        REWARD_ITEMS.add(new Item(12514));//explorer backpack
        REWARD_ITEMS.add(new Item(20211));//team cape zero
        REWARD_ITEMS.add(new Item(20214));//Rteam cape x
        REWARD_ITEMS.add(new Item(20000));//Dragon scim (or)
        REWARD_ITEMS.add(new Item(20779));//hunters knife
        REWARD_ITEMS.add(new Item(20773));//banshee mask
        REWARD_ITEMS.add(new Item(20756));//ourge club
        REWARD_ITEMS.add(new Item(20272));//cabbage shield

    }
}
