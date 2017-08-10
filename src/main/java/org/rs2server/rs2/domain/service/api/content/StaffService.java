package org.rs2server.rs2.domain.service.api.content;

import com.google.common.collect.ImmutableList;
import com.mongodb.annotations.Immutable;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Clank1337
 */
public interface StaffService {

	enum Runes {
		FIRE(554),
		WATER(555),
		AIR(556),
		EARTH(557),
		MIND(558),
		BODY(559),
		DEATH(560),
		NATURE(561),
		CHAOS(562),
		LAW(563),
		COSMIC(564),
		BLOOD(565),
		SOUL(566),
		ASTRAL(9075);

		private final int itemId;

		Runes(int itemId) {
			this.itemId = itemId;// so in an enum its actually just made up of an array called values
		}

		public static Optional<Runes> of(int itemId) {
			return Arrays.stream(values()).filter(r -> r.getItemId() == itemId).findAny();
		}

		public int getItemId() {
			return itemId;
		}
	}

	enum Staff {
		/**
		 * Default staves
		 */
		AIR_STAFF(1381, ImmutableList.of(Runes.AIR)),
		WATER_STAFF(1383, ImmutableList.of(Runes.WATER)),
		FIRE_STAFF(1387, ImmutableList.of(Runes.FIRE)),
		EARTH_STAFF(1385, ImmutableList.of(Runes.EARTH)),
		TOME_OF_FIRE(20714, ImmutableList.of(Runes.FIRE)),
		

		/**
		 * Battle staves
		 */
		AIR_BATTLE_STAFF(1397, ImmutableList.of(Runes.AIR)),
		WATER_BATTLE_STAFF(1395, ImmutableList.of(Runes.WATER)),
		FIRE_BATTLE_STAFF(1393, ImmutableList.of(Runes.FIRE)),
		EARTH_BATTLE_STAFF(1399, ImmutableList.of(Runes.EARTH)),
		LAVA_BATTLE_STAFF(3053, ImmutableList.of(Runes.FIRE, Runes.EARTH)),
		/**
		 * Mystic staves
		 */
		SMOKE_BATTLESTAFF(11998, ImmutableList.of(Runes.FIRE, Runes.AIR)),
		MYSTIC_LAVA_STAFF(3054, ImmutableList.of(Runes.EARTH, Runes.FIRE)),
		MUD_BATTLESTAFF(6562, ImmutableList.of(Runes.WATER, Runes.EARTH)),
		STEAM_BATTLESTAFF(11787, ImmutableList.of(Runes.WATER, Runes.FIRE))
		;

		private final int staffId;
		private final ImmutableList<Runes> runes;

		Staff(int staffId, ImmutableList<Runes> runes) {
			this.staffId = staffId;
			this.runes = runes;
		}

		public static Optional<Staff> of(int staffId) {
			return Arrays.stream(values()).filter(s -> s.getStaffId() == staffId).findAny();
		}

		public int getStaffId() {
			return staffId;
		}

		public ImmutableList<Runes> getRunes() {
			return runes;
		}
	}

	int removeRune(@Nonnull Player player, @Nonnull Item item);

	boolean containsRune(@Nonnull Player player, @Nonnull Item item);

	int getCount(@Nonnull Player player, @Nonnull Item item);
}
