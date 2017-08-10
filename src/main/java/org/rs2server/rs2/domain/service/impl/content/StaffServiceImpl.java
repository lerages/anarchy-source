package org.rs2server.rs2.domain.service.impl.content;

import org.rs2server.rs2.domain.service.api.content.StaffService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.player.Player;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * @author Clank1337
 */
public class StaffServiceImpl implements StaffService {


	@Override
	public int removeRune(@Nonnull Player player, @Nonnull Item item) {
		Item weapon = player.getEquipment().get(Equipment.SLOT_WEAPON);
		if (weapon != null) {
			Optional<Staff> staffOptional = Staff.of(weapon.getId());
			Optional<Runes> runesOptional = Runes.of(item.getId());
			if (staffOptional.isPresent() && runesOptional.isPresent()) {
				Staff staff = staffOptional.get();
				Runes runes = runesOptional.get();
				if (staff.getRunes().contains(runes)) {
					return 0;
				}
			}
		}
		if (player.getRunePouch().getCount(item.getId()) >= item.getCount()) {
			return player.getRunePouch().remove(item);
		}
		return player.getInventory().remove(item);
	}

	@Override
	public boolean containsRune(@Nonnull Player player, @Nonnull Item item) {
		Item weapon = player.getEquipment().get(Equipment.SLOT_WEAPON);
		if (weapon != null) {
			Optional<Staff> staffOptional = Staff.of(weapon.getId());
			Optional<Runes> runesOptional = Runes.of(item.getId());
			if (staffOptional.isPresent() && runesOptional.isPresent()) {
				Staff staff = staffOptional.get();
				Runes runes = runesOptional.get();
				if (staff.getRunes().contains(runes)) {
					return true;
				}
			}
		}
		return player.getInventory().getCount(item.getId()) >= item.getCount() || player.getRunePouch().getCount(item.getId()) >= item.getCount();
	}

	@Override
	public int getCount(@Nonnull Player player, @Nonnull Item item) {
		Item weapon = player.getEquipment().get(Equipment.SLOT_WEAPON);
		if (weapon != null) {
			Optional<Staff> staffOptional = Staff.of(weapon.getId());
			Optional<Runes> runesOptional = Runes.of(item.getId());
			if (staffOptional.isPresent() && runesOptional.isPresent()) {
				Staff staff = staffOptional.get();
				Runes runes = runesOptional.get();
				if (staff.getRunes().contains(runes)) {
					return 999999;
				}
			}
		}
		return player.getInventory().getCount(item.getId()) + player.getRunePouch().getCount(item.getId());
	}
}
