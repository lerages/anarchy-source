package org.rs2server.rs2.model.skills;

import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.action.impl.HarvestingAction;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.content.ItemService;
import org.rs2server.rs2.domain.service.impl.content.ItemServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.npc.Pet.Pets;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.util.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Woodcutting extends HarvestingAction {

	/**
	 * The random number generator.
	 */
	private final Random random = new Random();

	/**
	 * The tree we are cutting down.
	 */
	private GameObject object;

	/**
	 * The hatchet we are using.
	 */
	private Hatchet hatchet;

	/**
	 * The tree we are cutting down.
	 */
	private Tree tree;

	private final ItemService itemService;

	public Woodcutting(Mob mob, GameObject object) {
		super(mob);
		this.object = object;
		this.tree = Tree.forId(object.getId());
		this.itemService = Server.getInjector().getInstance(ItemService.class);
	}

	/**
	 * Represents types of axe hatchets.
	 *
	 * @author Michael (Scu11)
	 */
	public static enum Hatchet {

		/**
		 * 3rd age axe.
		 */
		THIRD_AGE(20011, 61, Animation.create(7264)),
		
		/**
		 * Infernal axe.
		 */
		INFERNAL(13241, 61, Animation.create(2117)),

		/**
		 * Dragon axe.
		 */
		DRAGON(6739, 61, Animation.create(2846)),

		/**
		 * Rune axe.
		 */
		RUNE(1359, 41, Animation.create(867)),

		/**
		 * Adamant axe.
		 */
		ADAMANT(1357, 31, Animation.create(869)),

		/**
		 * Mithril axe.
		 */
		MITHRIL(1355, 21, Animation.create(871)),

		/**
		 * Black axe.
		 */
		BLACK(1361, 6, Animation.create(873)),

		/**
		 * Steel axe.
		 */
		STEEL(1353, 6, Animation.create(875)),

		/**
		 * Iron axe.
		 */
		IRON(1349, 1, Animation.create(877)),

		/**
		 * Bronze axe.
		 */
		BRONZE(1351, 1, Animation.create(879));

		/**
		 * The item id of this hatchet.
		 */
		private int id;

		/**
		 * The level required to use this hatchet.
		 */
		private int level;

		/**
		 * The animation performed when using this hatchet.
		 */
		private Animation animation;

		/**
		 * A list of hatchets.
		 */
		private static List<Hatchet> hatchets = new ArrayList<Hatchet>();

		/**
		 * Gets the list of hatchets.
		 *
		 * @return The list of hatchets.
		 */
		public static List<Hatchet> getHatchets() {
			return hatchets;
		}

		/**
		 * Populates the hatchet map.
		 */
		static {
			for (Hatchet hatchet : Hatchet.values()) {
				hatchets.add(hatchet);
			}
		}

		private Hatchet(int id, int level, Animation animation) {
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
	 * Represents types of tree.
	 *
	 * @author Michael
	 */
	public enum Tree {

		/**
		 * Normal tree.
		 */
		NORMAL(1511, 1, 25, 15, 1, new int[]{1276, 1277, 1278, 1279, 1280, 1282,
				1283, 1284, 1285, 1286, 1289, 1290, 1291, 1315, 1316, 1318,
				1319, 1330, 1331, 1332, 1365, 1383, 1384, 3033, 3034, 3035,
				3036, 3881, 3882, 3883, 5902, 5903, 5904}, 10000),

		/**
		 * Willow tree.
		 */
		WILLOW(1519, 30, 67.5, 22, 16, new int[]{1750, 1758, 1756, 1760, 7480, 7422, 7482, 7424}, 7000),

		/**
		 * Oak tree.
		 */
		OAK(1521, 15, 37.5, 22, 12, new int[]{1751, 7417}, 8000),

		/**
		 * Magic tree.
		 */
		MAGIC(1513, 75, 250, 150, 18, new int[]{1761, 7483}, 2500),

		/**
		 * Maple tree.
		 */
		MAPLE(1517, 45, 100, 60, 17, new int[]{1759, 7481}, 5000),
		
		HOLLOW(3239, 45, 82.5, 60, 5, new int[]{1757, 1752}, 8000),
		
		MATURE_JUNIPER(13355, 42, 180, 60, 7, new int[]{27499}, 8000),
		
		ARCTIC_PINE(10810, 40, 310, 60, 17, new int[]{3037}, 8000),
		
//		this.objects = objects;
//		this.level = level;
//		this.experience = experience;
//		this.respawnTimer = respawnTimer;
//		this.logCount = logCount;
//		this.log = log;
//		this.petRate = petRate;
		/**
		 * Mahogany tree.
		 */
		MAHOGANY(6332, 50, 125, 22, 12, new int[]{9034}, 10000),

		/**
		 * Teak tree.
		 */
		TEAK(6333, 35, 85, 22, 10, new int[]{9036}, 10000),

		/**
		 * Achey tree.
		 */
		ACHEY(2862, 1, 25, 22, 4, new int[]{2023}, 10000),
		
		/**
		 * Medium tree.
		 */
		LIGHT_JUNGLE(9010, 10, 35, 22, 6, new int[]{6281}, 100000),
		MEDIUM_JUNGLE(9015, 20, 45, 22, 5, new int[]{6283}, 100000),
		DENSE_JUNGLE(9020, 35, 55, 22, 4, new int[]{6285}, 100000),
		/**
		 * Yew tree.
		 */
		YEW(1515, 60, 175, 120, 16, new int[]{1753, 7419, 1754}, 4000),

		/**
		 * Dramen tree
		 */
		DRAMEN(771, 36, 30, 22, 4, new int[]{1292}, 10000),
		
		/**
		 * Dramen tree
		 */
		REDWOOD(19669, 90, 380, 380, 10, new int[]{29668, 29670}, 5000),
		
		/**
		   * Bruma root
		   */
		BRUMA(20695, 1, 2, 110, 110, new int[]{29311}, 6500);

		/**
		 * The object ids of this tree.
		 */
		private int[] objects;

		/**
		 * The level required to cut this tree down.
		 */
		private int level;

		/**
		 * The logging rewarded for each cut of the tree.
		 */
		private int log;

		/**
		 * The time it takes for this tree to respawn.
		 */
		private int respawnTimer;

		/**
		 * The amount of logs this tree contains.
		 */
		private int logCount;

		/**
		 * The experience granted for cutting a logging.
		 */
		private double experience;

		private int petRate;

		/**
		 * A map of object ids to trees.
		 */
		private static Map<Integer, Tree> trees = new HashMap<Integer, Tree>();

		/**
		 * Gets a tree by an object id.
		 *
		 * @param object The object id.
		 * @return The tree, or <code>null</code> if the object is not a tree.
		 */
		public static Tree forId(int object) {
			return trees.get(object);
		}

		static {
			for (Tree tree : Tree.values()) {
				for (int object : tree.objects) {
					trees.put(object, tree);
				}
			}
		}

		/**
		 * Creates the tree.
		 *
		 * @param log        The logging id.
		 * @param level      The required level.
		 * @param experience The experience per logging.
		 * @param objects    The object ids.
		 */
		Tree(int log, int level, double experience, int respawnTimer, int logCount, int[] objects, int petRate) {
			this.objects = objects;
			this.level = level;
			this.experience = experience;
			this.respawnTimer = respawnTimer;
			this.logCount = logCount;
			this.log = log;
			this.petRate = petRate;
		}

		/**
		 * Gets the logging id.
		 *
		 * @return The logging id.
		 */
		public int getLogId() {
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
		 * @return the logCount
		 */
		public int getLogCount() {
			return logCount;
		}

		public int getPetRate() {
			return petRate;
		}
	}

	@Override
	public Animation getAnimation() {
		return hatchet.getAnimation();
	}

	@Override
	public int getCycleCount() {
		int skill = getMob().getSkills().getLevel(Skills.WOODCUTTING);
		int level = tree.level;
		int modifier = hatchet.level;
		int randomAmt = Misc.random(3);
		double cycleCount = Math.ceil((level * 50 - skill * 10) / modifier * 0.25 - randomAmt * 4);
		if (cycleCount < 1) {
			cycleCount = 1;
		}
		return (int) cycleCount + 1;
	}

	@Override
	public double getExperience() {
		if (!getMob().isPlayer()) {
		}
		Player player = (Player) getMob();
		int nest_chance = player.getPerks()[12].isOwned() ? Misc.random(255) : Misc.random(122);
		Pet.skillingPet(player, Pets.BEAVER, tree.getPetRate());
		if(nest_chance == 0)
		{
			int r = Misc.random(4);
			Item nest = null;
			if(r == 4)
			nest = new Item(5070);
			if(r == 3)
			nest = new Item(5071);
			if(r == 2)
				nest = new Item(5072);
			if(r == 1)
				nest = new Item(5073);
			if(r == 0)
				nest = new Item(5074);
			
			player.playSound(Sound.BIRD_NEST);
			
			if(player.getPerks()[12].isOwned())
			{
				player.getBank().add(new Item(nest.getId(), 1));
				player.sendMessage("<col=ff0000>A bird's nest falls out of the tree. It has been sent directly to your bank.");
			}
			else
			{
				World.getWorld().register(new GroundItem(player.getName(), nest, player.getLocation()), player);	
				player.sendMessage("<col=ff0000>A bird's nest falls out of the tree.");
			}
			
		}
		if(player.getPerks()[9].isOwned() && Misc.random(9) == 0)
		{
			player.getInventory().add(new Item(tree.getLogId(), 1));
			player.getSkills().addExperience(Skills.WOODCUTTING, tree.getExperience());
			player.sendMessage("You manage to cut an additional log.");
		}
		return tree.getExperience();
	}

	@Override
	public GameObject getGameObject() {
		return object;
	}

	@Override
	public int getGameObjectMaxHealth() {
		return Misc.random(1, tree.getLogCount());
	}

	@Override
	public String getHarvestStartedMessage() {
		return "You swing your axe at the tree.";
	}

	@Override
	public String getLevelTooLowMessage() {
		return "You need a " + Skills.SKILL_NAME[getSkill()] + " level of " + tree.getRequiredLevel() + " to cut this tree.";
	}

	@Override
	public int getObjectRespawnTimer() {
		return tree.getRespawnTimer();
	}

	@Override
	public GameObject getReplacementObject() {

		return new GameObject(getGameObject().getLocation(), 1342, 10, 0, false);
	}

	@Override
	public int getRequiredLevel() {
		return tree.getRequiredLevel();
	}

	@Override
	public Item getReward() {
		if (hatchet == Hatchet.INFERNAL && Misc.random(8) == 0) {
			getMob().getSkills().addExperience(Skills.FIREMAKING, tree.getExperience() / 2);
			getMob().playGraphics(Graphic.create(86));
			return null;
		}
		return new Item(tree.getLogId(), 1);
	}

	@Override
	public int getSkill() {
		return Skills.WOODCUTTING;
	}

	@Override
	public String getSuccessfulHarvestMessage() {
		return "You get some " + CacheItemDefinition.get(tree.getLogId()).getName().toLowerCase() + ".";
	}

	@Override
	public boolean canHarvest() {
		for (Hatchet hatchet : Hatchet.values()) {
			if ((getMob().getInventory().contains(hatchet.getId()) || getMob().getEquipment().contains(hatchet.getId()))
					&& getMob().getSkills().getLevelForExperience(getSkill()) >= hatchet.getRequiredLevel()) {
				this.hatchet = hatchet;
				break;
			}
		}
		if (hatchet == null) {
			getMob().getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "You do not have an axe that you can use.");
			getMob().getActionSender().sendMessage("You do not have an axe that you can use.");
			return false;
		}
		return true;
	}

	@Override
	public String getInventoryFullMessage() {
		return "Your inventory is too full to hold any more " + CacheItemDefinition.get(tree.getLogId()).getName().toLowerCase() + ".";
	}

}
