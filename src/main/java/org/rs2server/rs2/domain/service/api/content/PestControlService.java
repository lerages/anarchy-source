package org.rs2server.rs2.domain.service.api.content;

import org.rs2server.rs2.model.player.pc.PestControlBoat;
import org.rs2server.rs2.model.player.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a pest control service.
 * @author twelve
 */
public interface PestControlService {

	enum PestControlItem {
		VOID_TOP(94, 8839, 250),
		VOID_BOTTOM(95, 8840, 250),
		VOID_GLOVES(96, 8842, 150),
		VOID_MAGE_HELM(119, 11663, 200),
		VOID_RANGE_HELM(120, 11664, 200),
		VOID_MELEE_HELM(121, 11665, 200),
		HERB_PACK(97, 11738, 30),
		;

		private final int buttonId;
		private final int itemId;
		private final int cost;

		PestControlItem(int buttonId, int itemId, int cost) {
			this.buttonId = buttonId;
			this.itemId = itemId;
			this.cost = cost;
		}

		private static Map<Integer, PestControlItem> shopButtons = new HashMap<>();
		private static Map<Integer, PestControlItem> shopItems = new HashMap<>();

		public static PestControlItem ofButton(int id) {
			return shopButtons.get(id);
		}

		public static PestControlItem ofItem(int id) {
			return shopItems.get(id);
		}

		static {
			for (PestControlItem shopItem : PestControlItem.values()) {
				shopButtons.put(shopItem.getButtonId(), shopItem);
			}
			for (PestControlItem shopItem : PestControlItem.values()) {
				shopItems.put(shopItem.getItemId(), shopItem);
			}
		}

		public int getButtonId() {
			return buttonId;
		}

		public int getItemId() {
			return itemId;
		}

		public int getCost() {
			return cost;
		}
	}

    void setPestControlPoints(@Nonnull Player player, int points);

    void openShop(@Nonnull Player player);

    List<PestControlBoat> getBoats();

    void addBoatMember(PestControlBoat boat, @Nonnull Player player);

    void removeBoatMember(PestControlBoat boat, @Nonnull Player player);

	boolean containsPlayer(@Nonnull Player player);

	void handleDeath(@Nonnull Player player);

}
