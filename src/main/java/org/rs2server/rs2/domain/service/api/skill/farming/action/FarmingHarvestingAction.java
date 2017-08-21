package org.rs2server.rs2.domain.service.api.skill.farming.action;

import org.rs2server.Server;
import org.rs2server.rs2.action.impl.InfiniteHarvestingAction;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPatchState;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPatchType;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingService;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.Skill;
import org.rs2server.rs2.model.Sound;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.npc.Pet.Pets;
import org.rs2server.rs2.model.player.Player;

/**
 * Picking a plantable. 
 *
 * @author tommo
 */
public class FarmingHarvestingAction extends InfiniteHarvestingAction {

	private static final Item ITEM_SPADE = new Item(952, 1);

	private final FarmingService farmingService = Server.getInjector().getInstance(FarmingService.class);
	private final FarmingPatchState patch;

	public FarmingHarvestingAction(Mob mob, FarmingPatchState patch) {
		super(mob);
		this.patch = patch;
	}

	@Override
	public void onSuccessfulHarvest(Item item) {
		patch.setYield(patch.getYield() - 1);
	}

	@Override
	public int getCycleCount() {
		return 3;
	}

	@Override
	public Item getReward() {
		return new Item(patch.getPlanted().getReward(), 1);
	}

	@Override
	public int getSkill() {
		return Skill.FARMING.getId();
	}

	@Override
	public int getRequiredLevel() {
		return 0;
	}

	@Override
	public double getExperience() {
		return patch.getPlanted() != null ? patch.getPlanted().getExperience() : 0;
	}

	@Override
	public String getLevelTooLowMessage() {
		return null;
	}

	@Override
	public String getHarvestStartedMessage() {
		return "You begin to harvest the " + patch.getPatch().getType().toString() + ".";
	}

	@Override
	public String getSuccessfulHarvestMessage() {
		return null;
	}

	@Override
	public String getInventoryFullMessage() {
		return "Your inventory is full.";
	}

	@Override
	public Animation getAnimation() {
		return patch.getPatch().getType().getYieldAnimation();
	}

	@Override
	public boolean canHarvest() {
		final Player player = (Player) getMob();
		if (patch.getYield() <= 0) {
			farmingService.clearPatch(player, patch);
			player.playAnimation(Animation.create(831));
			player.playSound(Sound.BURY_BONES);
			return false;
		}

		//if (patch.getPatch().getType() == FarmingPatchType.FRUIT_TREE_PATCH || 
			//	patch.getPatch().getType() == FarmingPatchType.TREE_PATCH) {
			if (!getMob().getInventory().hasItem(ITEM_SPADE)) {
				getMob().getActionSender().sendMessage("You need a spade to do that.");
				return false;
			//}
		}
			Pet.skillingPet(player, Pets.TANGLEROOT, 10000);
		return true;
	}
}
