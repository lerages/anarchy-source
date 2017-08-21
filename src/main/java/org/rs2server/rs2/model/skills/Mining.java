package org.rs2server.rs2.model.skills;

import org.rs2server.Server;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.impl.HarvestingAction;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.skill.MiningService;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.npc.Pet.Pets;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.smithing.SmithingUtils;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.util.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Mining extends HarvestingAction {

	private final MiningService miningService = Server.getInjector().getInstance(MiningService.class);
	
	/*
	 * 317 style rock replacements; 8828-8830
	 * 474+ style rock replacements; 
	 * 
	 * Replacements: [13456, 15198],
	 */

	/**
	 * The rock we are mining.
	 */
	private GameObject object;
	
	/**
	 * The pick axe we are using.
	 */
	private PickAxe pickaxe;
	
	/**
	 * The rock we are mining.
	 */
	private Rock rock;
	
	public Mining(Mob mob, GameObject object) {
		super(mob);
		this.object = object;
		this.rock = Rock.forId(object.getId());
	}
	
	/**
	 * Represents types of pick axes.
	 * @author Michael (Scu11)
	 *
	 */
	public static enum PickAxe {

		/**
		 * 3rd age pickaxe.
		 */
		THIRD_AGE(20014, 61, Animation.create(7283)),
		
		/**
		 * Infernal pickaxe.
		 */
		INFERNAL(13243, 61, Animation.create(4483)),
		
		/**
		 * Dragon pickaxe.
		 */
		DRAGON_OR(12797, 61, Animation.create(335)),
		
		DRAGON(11920, 61, Animation.create(7139)),

		/**
		 * Rune pickaxe.
		 */
		RUNE(1275, 41, Animation.create(624)),

		/**
		 * Adamant pickaxe.
		 */
		ADAMANT(1271, 31, Animation.create(628)),

		/**
		 * Mithril pickaxe.
		 */
		MITHRIL(1273, 21, Animation.create(629)),

		/**
		 * Steel pickaxe.
		 */
		STEEL(1269, 6, Animation.create(627)),

		/**
		 * Iron pickaxe.
		 */
		IRON(1267, 1, Animation.create(626)),

		/**
		 * Bronze pickaxe.
		 */
		BRONZE(1265, 1, Animation.create(625));
		
		/**
		 * The item id of this pick axe.
		 */
		private int id;

		/**
		 * The level required to use this pick axe.
		 */
		private int level;
		
		/**
		 * The animation performed when using this pick axe.
		 */
		private Animation animation;

		/**
         * A list of pick axes.
         */
        private static List<PickAxe> pickaxes = new ArrayList<PickAxe>();

        /**
         * Gets the list of pick axes.
         * @return The list of pick axes.
         */
        public static List<PickAxe> getPickaxes() {
            return pickaxes;
        }

        /**
         * Populates the pick axe map.
         */
        static {
            for(PickAxe pickaxe : PickAxe.values()) {
                pickaxes.add(pickaxe);
            }
        }
		
		private PickAxe(int id, int level, Animation animation) {
			this.id = id;
			this.level = level;
			this.animation = animation;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}
		
		/**
		 * @return the level
		 */
		public int getRequiredLevel() {
			return level;
		}
		
		/**
		 * @return the animation
		 */
		public Animation getAnimation() {
			return animation;
		}
	}
	
	/**
	 * Represents types of rocks.
	 * @author Michael
	 *
	 */
	public static enum Rock {

		/**
		 * Clay ore.
		 */
		CLAY(434, 1, 5, 2, 1, new int[] { 13456, 13457, 14176, 14181, 7454, 7487 }, new int[] { 13459, 13460, 10798, 10796,7468,7468}, 10000),
		
		/**
		 * Copper ore.
		 */
		COPPER(436, 1, 17, 4, 1, new int[] { 7453, 7484,  }, new int[] { 7468, 7469 }, 10000),
		
		/**
		 * Tin ore.
		 */
		TIN(438, 1, 17, 4, 1, new int[] { 7485, 7486, }, new int[] { 7468, 7469}, 10000),
		
		/**
		 * Iron ore.
		 */
		IRON(440, 15, 35, 10, 1, new int[] { 7488, 7455,  }, new int[] { 7469, 7468, }, 9000),
		
		/**
		 * Silver ore.
		 */
		SILVER(442, 20, 40, 100, 1, new int[] { 8976, 8977, 8978,  13439, 13440, 13447, 13438, 7457, 7490 }, new int[] { 8979, 8980, 8981, 13460, 13461, 13459, 13459,7468,7468}, 8500),
		
		/**
		 * Gold ore.
		 */
		GOLD(444, 40, 65, 100, 1, new int[] { 7458, 7491 }, new int[] { 7469, 7468 }, 7000),
		
		/**
		 * Coal ore.
		 */
		COAL(453, 30, 50, 50, 1, new int[] { 7456, 7489,  }, new int[] { 7469, 7468,  }, 5000),
		
		/**
		 * Blast mining wall
		 */
		//HARD_ROCK(13575, 85, 350, 300, 1, new int[] { 28580, 28579,  }, new int[] { 28582, 28582,  }, 3200),
		/**
		 * Mithril ore.
		 */
//		this.objects = objects;
//		this.level = level;
//		this.experience = experience;
//		this.respawnTimer = respawnTimer;
//		this.oreCount = oreCount;
//		this.log = log;
//		this.replacementRocks = replacementRocks;
//		this.petRate = petRate;
		MITHRIL(447, 55, 80, 200, 1, new int[] {7459, 7492 }, new int[] { 7468, 7469 }, 5000),
		
		/**
		 * Adamantite ore.
		 */
		ADAMANTITE(449, 70, 95, 400, 1, new int[] { 7460, 7493 }, new int[] { 7469 }, 5000),
		
		/**
		 * Rune ore.
		 */
		RUNE(451, 85, 215, 1000, 1, new int[] { 7418, 7419, 7494, 7461, }, new int[] { 7468, 7469, 7469, 7469, }, 5000),
		
		GEM_ROCK(1623, 40, 75, 50, 3, new int[] { 9030 }, new int[] { 9032 }, 5000),
		
		VOLCANIC_SULPHUR(13571, 42, 40, 40, 1, new int[] { 28498 }, new int[] { 28496, }, 5000),
		
		LOVAKITE(13356, 65, 76, 210, 1, new int[] { 28596, 28597 }, new int[] { 7468 }, 5000),/**
		 * Dense essence block
		 */
		DENSE_ESSENCE(13445, 38, 6, 50, 100, new int[] { 8981, 10796 }, new int[] { 8981, 10796 }, 9000),
		
		;
		
		/**
		 * The object ids of this rock.
		 */
		private int[] objects;
		
		/**
		 * The level required to mine this rock.
		 */
		private int level;
		
		/**
		 * The ore rewarded for mining the rock
		 */
		private int log;
		
		/**
		 * The time it takes for this rock to respawn.
		 */
		private int respawnTimer;

		/**
		 * The amount of ores this rock contains.
		 */
		private int oreCount;

		/**
		 * The experience granted for mining this rock.
		 */
		private double experience;
		
		/**
		 * The rocks to replace.
		 */
		private int[] replacementRocks;

		private final int petRate;
		
		/**
		 * A map of object ids to rocks.
		 */
		private static Map<Integer, Rock> rocks = new HashMap<Integer, Rock>();
		
		/**
		 * Gets a rock by an object id.
		 * @param object The object id.
		 * @return The rock, or <code>null</code> if the object is not a rock.
		 */
		public static Rock forId(int object) {
			return rocks.get(object);
		}
		
		static {
			for(Rock rock : Rock.values()) {
				for(int object : rock.objects) {
					rocks.put(object, rock);
				}
			}
		}

		/**
		 * Creates the rock.
		 * @param log The logging id.
		 * @param level The required level.
		 * @param experience The experience per logging.
		 * @param objects The object ids.
		 */
		private Rock(int log, int level, double experience, int respawnTimer, int oreCount, int[] objects, int[] replacementRocks, int petRate) {
			this.objects = objects;
			this.level = level;
			this.experience = experience;
			this.respawnTimer = respawnTimer;
			this.oreCount = oreCount;
			this.log = log;
			this.replacementRocks = replacementRocks;
			this.petRate = petRate;
		}

		/**
		 * @return the replacementRocks
		 */
		public int[] getReplacementRocks() {
			return replacementRocks;
		}

		/**
		 * Gets the logging id.
		 * 
		 * @return The logging id.
		 */
		public int getOreId() {
			return log;
		}

		/**
		 * Gets the object ids.
		 * 
		 * @return The object ids.
		 */
		public int[] getObjectIds() {
			return objects;
		}

		/**
		 * Gets the required level.
		 * 
		 * @return The required level.
		 */
		public int getRequiredLevel() {
			return level;
		}

		/**
		 * Gets the experience.
		 * 
		 * @return The experience.
		 */
		public double getExperience() {
			return experience;
		}
		
		/**
		 * @return the respawnTimer
		 */
		public int getRespawnTimer() {
			return respawnTimer;
		}
		
		/**
		 * @return the oreCount
		 */
		public int getOreCount() {
			return oreCount;
		}

		public int getPetRate() { return petRate; }
	}

	@Override
	public Animation getAnimation() {
		return pickaxe.getAnimation();
	}

	@Override
	public int getCycleCount() {
		int skill = getMob().getSkills().getLevel(getSkill());
		int level = rock.getRequiredLevel();
		int modifier = pickaxe.getRequiredLevel();
		double cycleCount = 1;
		cycleCount = Math.ceil((level * 50 - skill * 10) / modifier * 0.0625 * 4);
		if (cycleCount < 1) {
			cycleCount = 1;
		}
		return (int) cycleCount;
	}

	@Override
	public double getExperience() {
		if (!getMob().isPlayer()) {
		}
		Player player = (Player) getMob();
		
		Pet.skillingPet(player, Pets.ROCK_GOLEM, rock.getPetRate());
		if (rock == Rock.DENSE_ESSENCE) {
			getMob().getSkills().addExperience(Skills.CRAFTING, 8);
		}
	
		if(player.getPerks()[8].isOwned() && Misc.random(9) == 0)
		{
			player.getInventory().add(new Item(rock.getOreId(), 1));
			player.getSkills().addExperience(Skills.MINING, rock.getExperience());
			player.sendMessage("You manage to mine an additional ore.");
		}
		return rock.getExperience() * (getMob().isPlayer() ? miningService.getProspectorKitExperienceModifier((Player) getMob()) : 1f);
	}

	@Override
	public GameObject getGameObject() {
		return object;
	}

	@Override
	public int getGameObjectMaxHealth() {
		return rock.getOreCount();
	}

	@Override
	public String getHarvestStartedMessage() {
		return "You swing your pick at the rock.";
	}

	@Override
	public String getLevelTooLowMessage() {
		return "You need a " + Skills.SKILL_NAME[getSkill()] + " level of " + rock.getRequiredLevel() + " to mine this rock.";
	}

	@Override
	public int getObjectRespawnTimer() {
		return rock.getRespawnTimer();
	}

	@Override
	public GameObject getReplacementObject() {
		int index = 0;
		for(int i = 0; i < rock.getObjectIds().length; i++) {
			if(rock.getObjectIds()[i] == getGameObject().getDefinition().getId()) {
				index = i;
				break;
			}
		}
		return new GameObject(getGameObject().getLocation(), rock.getReplacementRocks()[index], getGameObject().getType(), getGameObject().getDirection(), false);
	}

	@Override
	public int getRequiredLevel() {
		return rock.getRequiredLevel();
	}

	@Override
	public Item getReward() {
		if (pickaxe == PickAxe.INFERNAL && Misc.random(8) == 0) {
			SmithingUtils.SmeltingBar bar = SmithingUtils.SmeltingBar.of(rock.getOreId());
			if (bar != null && getMob().getSkills().getLevelForExperience(Skills.SMITHING) >= bar.getLevelRequired()) {
				boolean hasItems = true;
				for (Item item : bar.getItemsRequired()) {
					if (item.getId() == rock.getOreId()) {
						continue;
					}
					if (!Inventory.hasItem(getMob(), item)) {
						hasItems = false;
					}
				}
				if (hasItems) {
					for (Item item : bar.getItemsRequired()) {
						if (item.getId() == rock.getOreId()) {
							continue;
						}
						Inventory.removeRune(getMob(), item);
					}
					getMob().getSkills().addExperience(Skills.SMITHING, bar.getExperience() / 2);
					getMob().playGraphics(Graphic.create(86));
					return new Item(bar.getProducedBar().getId(), 1);
				}

			}
			return new Item(rock.getOreId(), 1);
		}
		return new Item(rock.getOreId(), 1);
	}

	@Override
	public int getSkill() {
		return Skills.MINING;
	}

	@Override
	public String getSuccessfulHarvestMessage() {
		return "You manage to mine some " + getReward().getDefinition2().getName().toLowerCase().replaceAll(" ore", "") + ".";
	}

	@Override
	public boolean canHarvest() {
		for(PickAxe pickaxe : PickAxe.values()) {
			if((getMob().getInventory().contains(pickaxe.getId()) || getMob().getEquipment().contains(pickaxe.getId()))
							&& getMob().getSkills().getLevelForExperience(getSkill()) >= pickaxe.getRequiredLevel()) {
				this.pickaxe = pickaxe;
				break;
			}
		}
		if(pickaxe == null) {
			getMob().getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "You do not have a pickaxe that you can use.");
			getMob().getActionSender().sendMessage("You do not have a pickaxe that you can use.");
			return false;
		}
		return true;
	}
	
	@Override
	public String getInventoryFullMessage() {
		return "Your inventory is too full to hold any more " + getReward().getDefinition2().getName().toLowerCase().replaceAll(" ore", "") + ".";
	}

}
