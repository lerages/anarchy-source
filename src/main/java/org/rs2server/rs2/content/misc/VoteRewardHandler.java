package org.rs2server.rs2.content.misc;

import com.rspserver.motivote.MotivoteHandler;
import com.rspserver.motivote.Reward;
import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.domain.service.impl.PermissionServiceImpl;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Clank1337
 */
public class VoteRewardHandler extends MotivoteHandler<Reward> {

    /**
     * Logging class.
     */
    private static final Logger logger = LoggerFactory.getLogger(World.class);
    private final PermissionService permissionService;

    public VoteRewardHandler() {
        permissionService = Server.getInjector().getInstance(PermissionService.class);
    }


    private enum VoteReward {
        MYSTERY_BOX("Mystery Box", 6199, 1),

        BOW_STRING("Bow String", 1778, 50),

        COAL("Coal", 454, 100),

        HERB_BOX("Herb Box", 11738, 1),

		COINS("100k GP", 995, 100000),

		PRAYER_POTS("15 Prayer Pots", 2435, 15),

		SHARK("100 Sharks", 386, 100),

		SUPER_COMBAT("10 Super Combats", 12696, 10);


        private String name;
        private int id;
        private int amount;

        VoteReward(String name, int id, int amount) {
            this.name = name;
            this.id = id;
            this.amount = amount;
        }

        private static Map<String, VoteReward> rewards = new HashMap<>();

        public static VoteReward forName(String name) {
            return rewards.get(name);
        }

        static {
            for (VoteReward reward : VoteReward.values()) {
                rewards.put(reward.name, reward);
            }
        }

        public int getId() {
            return id;
        }

        public int getAmount() {
            return amount;
        }
    }


    @Override
    public void onCompletion(Reward reward) {

        World.getWorld().getPlayers().stream().filter(Objects::nonNull)
                .filter(p -> p.getName().equalsIgnoreCase(reward.username())).findAny().ifPresent(p -> {
            VoteReward voteReward = VoteReward.forName(reward.rewardName());
            if (voteReward != null) {
                logger.info("Found reward for player " + reward.username() + ", with Reward: " + reward.rewardName());
                Inventory.addDroppable(p, new Item(voteReward.getId(), permissionService.isNotAny(p, PermissionServiceImpl.SPECIAL_PERMISSIONS)
                        ? voteReward.getAmount() : voteReward.getAmount() * 2));
                World.getWorld().sendWorldMessage(reward.username() + " has just voted!");
                reward.complete();
            }
        });
    }
}
