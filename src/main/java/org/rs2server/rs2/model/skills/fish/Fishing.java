package org.rs2server.rs2.model.skills.fish;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.impl.SkillAction;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.Sound;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.npc.Pet.Pets;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.util.Misc;
import org.rs2server.util.CycleState;

public class Fishing extends SkillAction {

	/**
	 * The npc.
	 */
	private final NPC npc;

	/**
	 * The fishing spot instance.
	 */
	private final FishingSpot fishingSpot;

	/**
	 * The amount of ticks.
	 */
	private int ticks;

	/**
	 * The amount of fish caught already.
	 */
	private int count;

	/**
	 * The harvest id.
	 */
	private int harvestId;

	/**
	 * Constructs a new {@code Fishing} {@code Object}.
	 *
	 * @param player      The player.
	 * @param npc         The fishing spot npc.
	 * @param optionId    The option id (first option - 1, second option - 2).
	 * @param fishingSpot The fishing spot.
	 */
	public Fishing(Player player, NPC npc, FishingSpot fishingSpot) 
	{
		super(player);
		this.npc = npc;
		this.fishingSpot = fishingSpot;
	}

	/**
	 * Checks if the player is executing a Fishing action.
	 *
	 * @param player The player.
	 * @param npc    The npc.
	 * @param option The option clicked (first/second).
	 * @return The fishing instance, or null if the action wasn't a fishing action.
	 */
	public static Fishing isAction(Player player, NPC npc, int option) {
		FishingSpot spot = FishingSpot.forId(npc.getId() | (option << 24));
		if (spot != null) {
			return new Fishing(player, npc, spot);
		}
		return null;
	}

	@Override
	public boolean commence(Player player) {
		harvestId = getCalculatedHarvest();
		if (player.getSkills().getLevel(Skills.FISHING) < fishingSpot.getHarvest()[harvestId].getLevel()) {
			player.getActionSender().sendMessage("You need a fishing level of " + fishingSpot.getHarvest()[harvestId].getLevel() + ".");
			return false;
		}
		if (!player.getInventory().contains(fishingSpot.getItem())) {
			player.getActionSender().sendMessage("You need a " + new Item(fishingSpot.getItem()).getDefinition2().getName().toLowerCase() + " to fish here.");
			return false;
		}
		if (!player.getInventory().contains(fishingSpot.getBait()) && fishingSpot.getBait() > 0) {
			player.getActionSender().sendMessage("You don't have the required bait to fish here.");
			return false;
		}
		player.setAttribute("fishing", true);
		this.ticks = (byte) getCalculatedTicks(harvestId);
		player.getActionSender().sendMessage("You cast out your " + new Item(fishingSpot.getItem(), 1).getDefinition2().getName().toLowerCase() + "...");
		return true;
	}

	@Override
	public boolean execute(Player player) {
		if (!(Boolean) player.getAttribute("fishing")) {
			stop();
			return false;
		}
		if (player.getInventory().freeSlots() < 1) {
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "Not enough space in your inventory.");
			player.getActionSender().sendMessage("Not enough space in your inventory.");
			player.getActionSender().playSound(Sound.NO_INVENTORY_SPACE);
			stop();
			player.playAnimation(RESET_ANIM);
			player.removeAttribute("fishing");
			return false;
		}
		player.playAnimation(fishingSpot.getAnimation());
		player.face(npc.getLocation());
		return ticks-- < 1;
	}

	@Override
	public boolean finish(Player player) {
		if (player.getInventory().freeSlots() < 1) {
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "Not enough space in your inventory.");
			player.getActionSender().sendMessage("Not enough space in your inventory.");
			player.getActionSender().playSound(Sound.NO_INVENTORY_SPACE);
			stop();
			player.playAnimation(RESET_ANIM);
			player.removeAttribute("fishing");
			return false;
		}
		if (!player.getInventory().contains(fishingSpot.getItem())) {
			player.getActionSender().sendMessage("You need a " + new Item(fishingSpot.getItem()).getDefinition2().getName().toLowerCase() + " to fish here.");
			stop();
			player.playAnimation(RESET_ANIM);
			player.removeAttribute("fishing");
			return false;
		}
		if (!player.getInventory().contains(fishingSpot.getBait()) && fishingSpot.getBait() > 0) {
			player.getActionSender().sendMessage("You don't have the required bait to fish here.");
			stop();
			player.playAnimation(RESET_ANIM);
			player.removeAttribute("fishing");
			return false;
		}
		
		Item harvest = new Item(fishingSpot.getHarvest()[harvestId].getId(), 1);
		player.getActionSender().sendMessage("You manage to catch some " + harvest.getDefinition2().getName().toLowerCase() + ".");
		player.getInventory().add(new Item(harvest.getId(), 1));
		Pet.skillingPet(player, Pets.HERON, fishingSpot.getPetChance());
		player.getSkills().addExperience(Skills.FISHING, fishingSpot.getHarvest()[harvestId].getXp());
		player.getSkills().addExperience(Skills.STRENGTH, fishingSpot.getHarvest()[harvestId].getBarbXp());
		player.getSkills().addExperience(Skills.AGILITY, fishingSpot.getHarvest()[harvestId].getBarbXp());
		if(player.getPerks()[10].isOwned() && Misc.random(9) == 0)
		{
			player.getInventory().add(new Item(fishingSpot.getHarvest()[harvestId].getId(), 1));
			player.getSkills().addExperience(Skills.FISHING, fishingSpot.getHarvest()[harvestId].getXp());
			player.getSkills().addExperience(Skills.STRENGTH, fishingSpot.getHarvest()[harvestId].getBarbXp());
			player.getSkills().addExperience(Skills.AGILITY, fishingSpot.getHarvest()[harvestId].getBarbXp());
			player.sendMessage("You manage to catch an additional fish.");
		}
		
		//if (fishingSpot == fishingSpot.BARBFISH) {
		//	player.getSkills().addExperience(Skills.STRENGTH, 500);
		//	player.getSkills().addExperience(Skills.AGILITY, 200);
		//}
		
		if (fishingSpot.getBait() > 0) {
			player.getInventory().remove(new Item(fishingSpot.getBait(), 1));
			if (!player.getInventory().contains(fishingSpot.getBait())) {
				stop();
				player.getActionSender().sendMessage("You have run out of bait.");
				player.playAnimation(RESET_ANIM);
				return true;
			}
		}
		
		if (RANDOM.nextInt(350) < ++count) {
			System.out.println("Fishing spot should move now. " + count + ".");
			player.playAnimation(RESET_ANIM);
			return true;
		}
		
		harvestId = getCalculatedHarvest();
		this.ticks = (byte) getCalculatedTicks(harvestId);
		setCycleState(CycleState.EXECUTE);
		return false;
	}

	void heronPet(Player player, FishingSpot spot) {
		int random = Misc.random(spot.getPetChance());
		if (random == 0) {
			Pet.Pets pets = Pet.Pets.HERON;
			if (player.getPet() != null) {
				return;
			}
			PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
			Pet pet = new Pet(player, pets.getNpc());
			player.setPet(pet);
			settings.setPetSpawned(true);
			settings.setPetId(pets.getNpc());
			World.getWorld().register(pet);
			World.getWorld().sendWorldMessage("<col=880000><img=33> " + player.getName() + " has just received a pet Heron.");
		}
	}

	/**
	 * Gets the amount of ticks for this session.
	 *
	 * @return The amount of ticks.
	 */
	private int getCalculatedTicks(int harvestId) {
		int skill = getPlayer().getSkills().getLevel(Skills.FISHING);
		int level = fishingSpot.getHarvest()[harvestId].getLevel();
		int modifier = fishingSpot.getHarvest()[harvestId].getLevel();
		int randomAmt = RANDOM.nextInt(4);
		double cycleCount = 1;
		cycleCount = Math.ceil((level * 80 - skill * 10) / modifier * 0.25 - randomAmt * 7);// 30 is modifier
		if (cycleCount < 1) {
			cycleCount = 1;
		}
		return (int) cycleCount + 1;
	}

	/**
	 * Gets the calculated harvest id.
	 *
	 * @return The harvest id.
	 */
	private int getCalculatedHarvest() {
		int randomHarvest = RANDOM.nextInt(fishingSpot.getHarvest().length);
		int difference = getPlayer().getSkills().getLevel(Skills.FISHING) - fishingSpot.getHarvest()[randomHarvest].getLevel();
		if (difference < -1) {
			return randomHarvest = 0;
		}
		if (randomHarvest < -1) {
			return randomHarvest = 0;
		}
		return randomHarvest;
	}
}
