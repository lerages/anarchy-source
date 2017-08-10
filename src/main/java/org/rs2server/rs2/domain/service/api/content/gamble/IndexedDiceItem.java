package org.rs2server.rs2.domain.service.api.content.gamble;

import lombok.Value;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;

/**
 * @author Clank1337
 */
public final @Value
class IndexedDiceItem {

	private final Item item;
	private final int index;
	public int getIndex() {
		// TODO Auto-generated method stub
		return index;
	}
	public Item getItem() {
		// TODO Auto-generated method stub
		return item;
	}

	public IndexedDiceItem (final Item item, int index) {
		this.item = item;
		this.index = index;
	}
	
}
