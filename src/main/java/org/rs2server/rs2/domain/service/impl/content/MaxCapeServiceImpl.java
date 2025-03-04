package org.rs2server.rs2.domain.service.impl.content;

import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.domain.service.api.content.MaxCapeService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Clank1337
 */
public class MaxCapeServiceImpl implements MaxCapeService {

	private static final int MAX_CAPE_ID = 13280;
	private static final int MAX_HOOD_ID = 13281;

	public enum MaxCapes {

		FIRE_MAX_CAPE(6570, 13329, 13330),
		MAX_CAPE(13280, 13280, 13281),
		SARA_MAX_CAPE(2412, 13331, 13332),
		ZAMORAK_MAX_CAPE(2414, 13333, 13334),
		GUTHIX_MAX_CAPE(2413, 13335, 13336),
		AVA_MAX_CAPE(10499, 13337, 13338),
		ARDOUGNE_MAX_CAPE(13124, 20760, 20764);

		private final int usedCapeId;
		private final int rewardCapeId;
		private final int rewardHoodId;

		MaxCapes(int usedCapeId, int rewardCapeId, int rewardHoodId) {
			this.usedCapeId = usedCapeId;
			this.rewardCapeId = rewardCapeId;
			this.rewardHoodId = rewardHoodId;
		}

		private static final Map<Integer, MaxCapes> capes = new HashMap<>();
		private static final Map<Integer, MaxCapes> maxCapes = new HashMap<>();
		private static final Map<Integer, MaxCapes> maxHoods = new HashMap<>();

		public static MaxCapes forCape(int itemId) {
			return capes.get(itemId);
		}
		public static MaxCapes forMaxCape(int itemId) {
			return maxCapes.get(itemId);
		}
		public static MaxCapes forMaxHood(int itemId) {
			return maxHoods.get(itemId);
		}

		static {
			for (MaxCapes items : MaxCapes.values()) {
				capes.put(items.getUsedCapeId(), items);
			}
			for (MaxCapes items : MaxCapes.values()) {
				maxCapes.put(items.getRewardCapeId(), items);
			}
			for (MaxCapes items : MaxCapes.values()) {
				maxHoods.put(items.getRewardHoodId(), items);
			}
		}

		public int getUsedCapeId() {
			return usedCapeId;
		}

		public int getRewardCapeId() {
			return rewardCapeId;
		}

		public int getRewardHoodId() {
			return rewardHoodId;
		}
	}


	@Override
	public boolean addToMaxCape(@Nonnull Player player, Item used, Item with) {
		if (used.getId() != MAX_CAPE_ID && with.getId() != MAX_CAPE_ID) { return false; }

		Item cape;

		if (used.getId() == MAX_CAPE_ID) {
			cape = with;
		} else {
			cape = used;
		}

		MaxCapes maxCapes = MaxCapes.forCape(cape.getId());
		if (maxCapes != null) {
			if (player.getInventory().contains(MAX_CAPE_ID) && player.getInventory().contains(MAX_HOOD_ID)) {
				player.getInventory().remove(cape);
				player.getInventory().remove(new Item(MAX_CAPE_ID));
				player.getInventory().remove(new Item(MAX_HOOD_ID));
				player.getInventory().add(new Item(maxCapes.getRewardCapeId()));
				player.getInventory().add(new Item(maxCapes.getRewardHoodId()));
				player.getActionSender().sendMessage("You combine your " + CacheItemDefinition.get(cape.getId()).getName() + " with your Max cape to make a " + CacheItemDefinition.get(maxCapes.getRewardCapeId()).getName() + ".");
			}
		}
		return true;
	}

	@Override
	public boolean destroyMaxCape(@Nonnull Player player, Item item) {

		MaxCapes cape = MaxCapes.forMaxCape(item.getId());
		MaxCapes hood = MaxCapes.forMaxHood(item.getId());

		/*if (cape != null) {
			if (player.getInventory().contains(cape.getRewardHoodId()) && player.getInventory().contains(cape.getRewardCapeId()) && player.getInventory().freeSlots() >= 1) {
				player.getInventory().remove(new Item(cape.getRewardCapeId()));
				player.getInventory().remove(new Item(cape.getRewardHoodId()));
				player.getInventory().add(new Item(MAX_CAPE_ID));
				player.getInventory().add(new Item(MAX_HOOD_ID));
				player.getInventory().add(new Item(cape.getUsedCapeId()));
				player.getActionSender().sendMessage("You revert your Max cape");
			} else if (player.getInventory().freeSlots() < 1) {
				player.getActionSender().sendMessage("Not enough inventory slots to complete this.");
			} else {
				player.getActionSender().sendMessage("You need both hood and cape to revert your Max cape.");
			}
			return true;
		} else if (hood != null) {
			if (player.getInventory().contains(hood.getRewardHoodId()) && player.getInventory().contains(hood.getRewardCapeId()) && player.getInventory().freeSlots() >= 1) {
				player.getInventory().remove(new Item(hood.getRewardCapeId()));
				player.getInventory().remove(new Item(hood.getRewardHoodId()));
				player.getInventory().add(new Item(MAX_CAPE_ID));
				player.getInventory().add(new Item(MAX_HOOD_ID));
				player.getInventory().add(new Item(hood.getUsedCapeId()));
				player.getActionSender().sendMessage("You revert your Max cape");
			} else if (player.getInventory().freeSlots() < 1) {
				player.getActionSender().sendMessage("Not enough inventory slots to complete this.");
			} else {
				player.getActionSender().sendMessage("You need both hood and cape to revert your Max cape.");
			}
			return true;
		}
*/
		return false;
	}
}
