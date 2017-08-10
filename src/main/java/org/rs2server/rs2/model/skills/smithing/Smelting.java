package org.rs2server.rs2.model.skills.smithing;

import org.rs2server.rs2.action.impl.ProductionAction;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.Sound;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.smithing.SmithingUtils.SmeltingBar;
import org.rs2server.rs2.net.ActionSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Smelting extends ProductionAction {
	
	private Mob mob;
	private SmeltingBar bar;
	private static final Animation SMELTING_ANIMATION = Animation.create(3243);
	private static final Random RANDOM = new Random();
	private int amount;
	
	public Smelting(Mob mob, SmeltingBar bar, int amount) {
		super(mob);
		this.mob = mob;
		this.bar = bar;
		this.amount = amount;
		for (int i = 0; i < getConsumedItems().length; i++) {
			if (!mob.getInventory().contains(getConsumedItems()[i].getId())) {
				mob.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "You don't have the required ore to smelt this bar.");
				mob.getActionSender().sendMessage("You don't have the required ore to smelt this bar.");
				return;
			}
		}
	}

	public static void furnaceInteraction(Player player) {// seems good yo
		ArrayList<SmeltingBar> bars = new ArrayList<SmeltingBar>();
		Collections.addAll(bars, SmeltingBar.values());
        int index = 0;
        for (SmeltingBar bar : bars) {
            player.getActionSender().sendItemOnInterface(311, index + 4, bar.getProducedBar().getId(), 150);
            index++;
        }
        player.getActionSender().sendInterface(162, 546, 311, false);
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
		return new Item[] {bar.getProducedBar()};
	}

	@Override
	public Item[] getConsumedItems() {
		return bar.getItemsRequired();
	}

	@Override
	public int getSkill() {
		return Skills.SMITHING;
	}

	@Override
	public int getRequiredLevel() {
		return bar.getLevelRequired();
	}

	@Override
	public double getExperience() {
		return mob.getEquipment().contains(776) && getConsumedItems()[0].getId() == 444 ? bar.getExperience() * 2 : bar.getExperience();
	}

	@Override
	public String getLevelTooLowMessage() {
		return "You need a Smithing level of " + bar.getLevelRequired() + " to make this bar.";
	}

	@Override
	public String getSuccessfulProductionMessage() {
		return "You retrieve a bar of " + bar.toString().toLowerCase() + ".";
	}

	@Override
	public Animation getAnimation() {
		return SMELTING_ANIMATION;
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
		if (bar == SmeltingBar.IRON) {
			if (mob.getEquipment().getSlot(Equipment.SLOT_RING) == 2568) { //ring of forging
				return true;
			}
			return RANDOM.nextInt(100) <= (mob.getSkills().getLevel(Skills.SMITHING) >= 45 ? 80 : 50);
		}
		return true;
	}

	@Override
	public String getFailProductionMessage() {
		return "The ore is too impure and you fail to refine it.";
	}

	@Override
	public Item getFailItem() {
		return null;
	}

	@Override
	public Sound getSound() {
		return Sound.SMELTING;
	}

}
