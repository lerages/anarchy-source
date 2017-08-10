package org.rs2server.rs2.model.skills;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

import java.util.*;

/**
 * Created by Paine 26 Jan 17
 */
public class UnsiredRewards {


    private static final List<Item> REWARD_ITEMS1 = new ArrayList<>();
    private static final List<Item> RARE_ITEMS1 = new ArrayList<>();

    private final Player player;
    private final PlayerService service;
    private static final Item UNSIRED = new Item(13273);
    private final Random random;

    public UnsiredRewards(Player player) {
        this.player = player;
        this.service = Server.getInjector().getInstance(PlayerService.class);
        this.random = new Random();
    }

    public Optional<Item> getRewardItem() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(UNSIRED)) {
            return Optional.empty();
        }
        items.addAll(getRandom() > 1 ? RARE_ITEMS1 : RARE_ITEMS1);
        Collections.shuffle(items);
        return Optional.of(items.get(0));
    }

    private int getRandom() {
        return random.nextInt(100);
    }
    static {
        RARE_ITEMS1.add(new Item(7979));//abby head
        RARE_ITEMS1.add(new Item(13262));//orphan
        RARE_ITEMS1.add(new Item(4151));//whip
        RARE_ITEMS1.add(new Item(13275));//bludgeon claw
        RARE_ITEMS1.add(new Item(13274));//spine
        RARE_ITEMS1.add(new Item(13265));//dagger
        RARE_ITEMS1.add(new Item(13276));//azon
        RARE_ITEMS1.add(new Item(13277));//miasma

    }
}
