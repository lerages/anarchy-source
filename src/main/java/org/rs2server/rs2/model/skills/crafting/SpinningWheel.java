package org.rs2server.rs2.model.skills.crafting;

import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.action.impl.ProductionAction;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.Sound;

import java.util.HashMap;
import java.util.Map;

public class SpinningWheel extends ProductionAction {
	
	private final Animation ANIMATION = Animation.create(894);
	
	public enum SpinItem {
		//USED, NEW, LEVEL, XP
		
		BALL_OF_WOOL(1737, 1759, 1, 2),
		
		BOW_STRING(1779, 1777, 10, 10),
		
		MAGIC_AMULET_STRING(6051, 6038, 19, 30),
		
		CROSSBOW_STRING(9436, 9438, 10, 15),
		
		ROPE(10814, 954, 30, 25);
		
		
		private int remove;
		private int add;
		private int requiredLevel;
		private int xp;
		
		SpinItem(int remove, int add, int requiredLevel, int xp) {
			this.remove = remove;
			this.add = add;
			this.requiredLevel = requiredLevel;
			this.xp = xp;
		}
		
		private static Map<Integer, SpinItem> items = new HashMap<Integer, SpinItem>();


		public static SpinItem forId(int item) {
			return items.get(item);
		}

		static {
			for (SpinItem item : SpinItem.values()) {
				items.put(item.remove, item);
			}
		}
		
		
		public int getRemove() {
			return remove;
		}
		
		public int getAdd() {
			return add;
		}
		
		public int getRequiredLevel() {
			return requiredLevel;
		}
		
		public int getXP() {
			return xp * 2;
		}
	}

	private SpinItem item;
	private int amount;
	
	public SpinningWheel(Mob mob, SpinItem item, int amount) {
		super(mob);
		this.item = item;
		this.amount = amount;
	}

	@Override
	public int getCycleCount() {
		return 4;
	}

	@Override
	public int getProductionCount() {
		return amount;
	}

	@Override
	public Item[] getRewards() {
		return new Item[] {new Item(item.getAdd())};
	}

	@Override
	public Item[] getConsumedItems() {
			return new Item[] {new Item(item.getRemove())};
	}

	@Override
	public int getSkill() {
		return Skills.CRAFTING;
	}

	@Override
	public int getRequiredLevel() {
		return item.getRequiredLevel();
	}

	@Override
	public double getExperience() {
		return item.getXP();
	}

	@Override
	public String getLevelTooLowMessage() {
		return "You need a Crafting level of " + item.getRequiredLevel() + " to craft this item.";
	}

	@Override
	public String getSuccessfulProductionMessage() {
		return CacheItemDefinition.get(item.getAdd()) != null ? "You successfully craft a " + CacheItemDefinition.get(item.getAdd()).getName() : "";
	}

	@Override
	public Animation getAnimation() {
		return ANIMATION;
	}

	@Override
	public Graphic getGraphic() {
		return null;
	}

	@Override
	public boolean canProduce() {
		return true;
	}

	@Override
	public boolean isSuccessfull() {
		return true;
	}

	@Override
	public String getFailProductionMessage() {
		return null;
	}

	@Override
	public Item getFailItem() {
		return null;
	}

	@Override
	public Sound getSound() {
		// TODO Auto-generated method stub
		return Sound.SPINNING_WHEEL;
	}

}
