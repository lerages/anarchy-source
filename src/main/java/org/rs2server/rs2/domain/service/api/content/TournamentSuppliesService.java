package org.rs2server.rs2.domain.service.api.content;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.rs2server.rs2.content.api.GameInterfaceButtonEvent;
import org.rs2server.rs2.content.api.GamePlayerLogoutEvent;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

public interface TournamentSuppliesService {
	
	enum TournamentRewards {
		Ahrims_robetop(4712, 1000),
		Ahrims_hood(4708, 1000),
		Ahrims_robeskirt(4714, 1000),
		Ahrims_staff(4710, 1000),
		Dharoks_platebody(4720, 1000),
		Dharoks_helm(4716, 1000),
		Dharoks_platelegs(4722, 1000),
		Dharoks_greateaxe(4718, 1000);
//		ARCHER_HELM(3749, 234000),
//		FARSEER_HELM(3755, 234000),
//		MYSTIC_ROBE_TOP(4091, 360000),
//		MYSTIC_ROBE_BOTTOM(4093, 240000),
//		MYSTIC_HAT(4089, 45000),
//		MYSTIC_GLOVES(4095, 30000),
//		MYSTIC_BOOTS(4097, 30000),
//		RUNE_PLATEBODY(1127, 255000),
//		RUNE_PLATELEGS(1079, 192000),
//		RUNE_PLATESKIRT(1093, 192000),
//		BOLT_RACK(4740, 360),
//		RUNE_ARROW(892, 600),
//		ADAMANT_ARROW(890, 240),
//		CLIMBING_BOOTS(3105, 5400),
//		FROZEN_WHIP_MIX(12769, 500000),
//		VOLCANIC_WHIP_MIX(12771, 500000),
//		RUNE_POUCH(12791, 1200000),
//		LOOTING_BAG(11941, 150000);


		private final int id;
		private final int cost;

		TournamentRewards(int id, int cost) {
			this.id = id;
			this.cost = cost;
		}

		public static Optional<Integer> cost(Item item) {
			return Arrays.stream(values()).filter(r -> r.id == item.getId()).map(r -> r.cost).findFirst();
		}

		public final int getId() {
			return id;
		}

		public final int getCost() {
			return cost;
		}
	}
	@SuppressWarnings("unused")
	void onTournamentShopClick(@Nonnull GameInterfaceButtonEvent event);


	void openTournamentShop(@Nonnull Player player);

}