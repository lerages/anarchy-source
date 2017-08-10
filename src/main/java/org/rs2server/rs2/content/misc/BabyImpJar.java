package org.rs2server.rs2.content.misc;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.hunter.ImpData;
import org.rs2server.rs2.model.skills.hunter.ImplingJars;
import org.rs2server.rs2.model.skills.hunter.PuroPuro;
import org.rs2server.rs2.content.misc.BabyImpJar;

import java.util.*;

/**
 * Created by Paine on 25 Feb 17
 */
public class BabyImpJar {


    private static final List<Item> REWARD_ITEMS = new ArrayList<>();
    private static final List<Item> RARE_ITEMS = new ArrayList<>();

    private final Player player;
    private final PlayerService service;
    private static final Item BabyImpJar = new Item(11238);
    private final Random random;

    public BabyImpJar(Player player) {
        this.player = player;
        this.service = Server.getInjector().getInstance(PlayerService.class);
        this.random = new Random();
    }

    public Optional<Item> getRewardItem() {
        List<Item> items = new ArrayList<>();
        if (!player.getInventory().hasItem(BabyImpJar)) {
            return Optional.empty();
        }
        items.addAll(getRandom() > 2 ? REWARD_ITEMS : REWARD_ITEMS);
        Collections.shuffle(items);
        return Optional.of(items.get(0));
    }


    private int getRandom() {
        return random.nextInt(100);
    }


    static {
        REWARD_ITEMS.add(new Item(1755));//chisel
        REWARD_ITEMS.add(new Item(362, 20));//tuna
        REWARD_ITEMS.add(new Item(443, 20));//silver ore
        REWARD_ITEMS.add(new Item(1624, 5));//uncsapphire
        REWARD_ITEMS.add(new Item(378, 5));//raw lob
        REWARD_ITEMS.add(new Item(1744, 10));//hard leather
        REWARD_ITEMS.add(new Item(995, 3500));//coins
        
    }
}
