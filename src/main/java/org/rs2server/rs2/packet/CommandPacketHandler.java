package org.rs2server.rs2.packet;

import org.apache.commons.compress.archivers.dump.DumpArchiveEntry.PERMISSION;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.rs2server.GitCommitFetcher;
import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.cache.format.CacheNPCDefinition;
import org.rs2server.cache.format.CacheObjectDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.ScriptManager;
import org.rs2server.rs2.content.BossKillLog;
import org.rs2server.rs2.content.PlayerInfoManager;
import org.rs2server.rs2.content.PresetManager;
import org.rs2server.rs2.content.SlayerKillLog;
import org.rs2server.rs2.content.TeleportInterface;
import org.rs2server.rs2.content.TimedPunishment.PunishmentType;
import org.rs2server.rs2.content.api.GameDiceRequestEvent;
import org.rs2server.rs2.domain.dao.api.PlayerEntityDao;
import org.rs2server.rs2.domain.model.player.PlayerEntity;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.model.player.treasuretrail.clue.ClueScrollRewards;
import org.rs2server.rs2.domain.model.player.treasuretrail.clue.ClueScrollType;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.clojure.ClojureService;
import org.rs2server.rs2.domain.service.api.content.*;
import org.rs2server.rs2.domain.service.api.content.bounty.BountyHunterService;
import org.rs2server.rs2.domain.service.api.content.cerberus.CerberusService;
import org.rs2server.rs2.domain.service.api.loot.Loot;
import org.rs2server.rs2.domain.service.api.loot.LootGenerationService;
import org.rs2server.rs2.domain.service.api.loot.LootTable;
import org.rs2server.rs2.domain.service.impl.BankPinServiceImpl;
import org.rs2server.rs2.domain.service.impl.PermissionServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.BankDepositBoxServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.gamble.DiceGameTransaction;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.bit.BitConfig;
import org.rs2server.rs2.model.bit.BitConfigBuilder;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.BitPackedValue;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.cm.Content;
import org.rs2server.rs2.model.cm.impl.CerberusContent;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.impl.InterfaceContainerListener;
import org.rs2server.rs2.model.event.EventListener.ClickOption;
import org.rs2server.rs2.model.event.impl.object.BarrowsTunnelListener;
import org.rs2server.rs2.model.map.DynamicTileBuilder;
import org.rs2server.rs2.model.map.RegionClipping;
import org.rs2server.rs2.model.map.path.ClippingFlag;
import org.rs2server.rs2.model.map.path.astar.ObjectReachedPrecondition;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.npc.NPCLoot;
import org.rs2server.rs2.model.npc.NPCLootTable;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.player.Perk;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.player.RequestManager;
import org.rs2server.rs2.model.quests.impl.CooksAssistant;
import org.rs2server.rs2.model.quests.impl.CooksAssistantState;
import org.rs2server.rs2.model.quests.impl.DTStates;
import org.rs2server.rs2.model.quests.impl.DesertTreasure;
import org.rs2server.rs2.model.quests.impl.LunarDiplomacy;
import org.rs2server.rs2.model.quests.impl.LunarStates;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.Packet;
import org.rs2server.rs2.tickable.StoppingTick;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.tickable.impl.SystemUpdateTick;
import org.rs2server.rs2.util.Misc;
import org.rs2server.rs2.util.NameUtils;
import org.rs2server.rs2.util.TextUtils;
import org.rs2server.rs2.varp.PlayerVariable;
import org.rs2server.tools.ReadCharacterFiles;
import org.rs2server.util.DonationManager;
import org.rs2server.util.*;
import org.rs2server.util.XMLController;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import org.rs2server.util.*;

public class CommandPacketHandler implements PacketHandler {

	private final PersistenceService persistenceService;
	private final PlayerService playerService;
	private final SlayerService slayerService;
	private final EngineService engineService;
	private final PestControlService pestControlService;
	private final PlayerEntityDao playerEntityDao;
	private final DebugService debugService;
	private final BankPinService bankPinService;
	private final TreasureTrailService treasureTrailService;
	private final PermissionService permissionService;
	private final PlayerVariableService playerVariableService;
	private final MusicService musicService;
	private final ClojureService clojureService;
	private final RunePouchService runePouchService;
	private BountyHunterService bountyHunterService;
	private final MonsterExamineService examineService;
	private final CerberusService cerberusService;

	public CommandPacketHandler() {
		persistenceService = Server.getInjector().getInstance(PersistenceService.class);
		playerService = Server.getInjector().getInstance(PlayerService.class);
		slayerService = Server.getInjector().getInstance(SlayerService.class);
		engineService = Server.getInjector().getInstance(EngineService.class);
		pestControlService = Server.getInjector().getInstance(PestControlService.class);
		playerEntityDao = Server.getInjector().getInstance(PlayerEntityDao.class);
		debugService = Server.getInjector().getInstance(DebugService.class);
		bankPinService = Server.getInjector().getInstance(BankPinService.class);
		treasureTrailService = Server.getInjector().getInstance(TreasureTrailService.class);
		permissionService = Server.getInjector().getInstance(PermissionService.class);
		playerVariableService = Server.getInjector().getInstance(PlayerVariableService.class);
		musicService = Server.getInjector().getInstance(MusicService.class);
		bountyHunterService = Server.getInjector().getInstance(BountyHunterService.class);
		clojureService = Server.getInjector().getInstance(ClojureService.class);
		runePouchService = Server.getInjector().getInstance(RunePouchService.class);
		examineService = Server.getInjector().getInstance(MonsterExamineService.class);
		cerberusService = Server.getInjector().getInstance(CerberusService.class);
	}

	@Override
	public void handle(Player player, Packet packet) throws IOException {
		String commandString = packet.getRS2String();

		// clojureService.invoke("commands.clj", commandString, player,
		// permissionService.getHighestPermission(player)); ha lol

		if (player.getAttribute("cutScene") != null) {
			return;
		}
		
		commandString = commandString.replaceAll(":", "");
		String[] args = commandString.split(" ");
		String command = args[0].toLowerCase();

		/*
		 * if (command.equals("home")) {
		 * player.setTeleportTarget(Entity.DEFAULT_LOCATION); }
		 */

		/*
		 * if (command.equals("forums")) { player.getActionSender().sendCS2Script(745,
		 * ,22609930, "https://www.Kronos-os.com"); }
		 */
		//if (command.equals("empty")) {
		//	player.getInventory().clear();
		//}
		
		// if (command.equals("npc")) {
		// NPC npc = new NPC(Integer.parseInt(args[1]),
		// Location.create(player.getLocation().getX(), player.getLocation().getY(),
		// player.getLocation().getZ()),
		// player.getLocation(),
		// player.getLocation(), 6);
		// World.getWorld().register(npc);
		// System.out.println(npc.getIndex());
		// }
		// if (command.equals("npcemote")) {
		// Mob mob = World.getWorld().getNPCs().get(Integer.parseInt(args[1]));
		// int anim = Integer.parseInt(args[2]);
		// if (mob.isNPC()) {
		// NPC n = (NPC) mob;
		// n.playAnimation(Animation.create(anim));
		// }
		// }
		//if (command.equals("claimdonation")) {
		//	try {
		//		DonationManager.rspsdata(player, player.getName());
		//		return;
		//	} catch (Exception e) {
		//	}
	//	}
		
		if (command.equalsIgnoreCase("claim")) {
			 new Thread() {
			  public void run() {
			   try {
			    com.everythingrs.donate.Donation[] donations = com.everythingrs.donate.Donation.donations("uzj48k1e63q5uxklexqwfusor6oth4vo8mff0k7uj0kv2huxr3sb1hjxwg9lgptzbbn123uwhfr",
			     player.getName());
			    if (donations.length == 0) {
			     player.sendMessage("You currently don't have rewards to claim. Please donate first!");
			     return;
			    }
			    if (donations[0].message != null) {
			     player.sendMessage(donations[0].message);
			     return;
			    }
			    for (com.everythingrs.donate.Donation donate: donations) {
			    int id = 0;
			    	switch (donate.product_id) {
			      case 536:
			    	  //Lazy Bones
			    	  id = 0;
			       break;
			      case 2351:
			    	  //No holds barred
			    	  id = 1;
			       break;
			      case 995:
			    	  //Gold Digger
			    	  id = 2;
			       break;
			      case 9768:
			    	  //Superior Orders
			    	  id = 3;
			       break;
			      case 9788:
			       //Slayer Betrayer
			    	  id = 4;
			       break;
			      case 1556:
			       //Teacher's pet
			    	  id = 5;
			       break;
			      case 11849:
			       //Good Graces
			    	 id = 6;
			       break;
			      case 20014:
			    	 id = 8;
			       //More Ore
			       break;
			      case 20011:
			    	 id = 9;
			       //Chop Shop
			       break;
			      case 26:
			       //Fish Wish
			    	  id = 10;
			       break;
			      case 2631:
			       //5 Finger Discount
			    	 id = 7;
			       break;
			      case 0:
			    	  //Rune Doubloon
			    	  id = 11;
			    	  break;
			     }
			    	  player.getPerks()[id].givePerk();
			    	  player.getDatabaseEntity().setOwnedPerks(id, true);
			    	  player.sendMessage("You now own " + NameUtils.formatName(player.getPerks()[id].getName()));
			    }
			    player.sendMessage("<col=ff0000>Thank you for showing your support!</col>");
			   } catch (Exception e) {
			    player.sendMessage("Our donation system is currently offline. Please try again later.");
			    e.printStackTrace();
			   }
			  }
			 }.start();
			}
		
		if(command.startsWith("commands"))
		{
				final List<String> commands = new ArrayList<>();
				commands.add("::players");
				commands.add("::lock skillName");
				commands.add("::unlock skillName");
				commands.add("::changepass newPassword");
				commands.add("::blocktask");
				commands.add("::unblockslot slotNumber");
				commands.add("::perks");
				commands.add("::viewperks targetPlayerName");
				commands.add("::claim");
				player.getActionSender().sendTextListInterface("<u>OS-Anarchy Commands</u>",
						commands.toArray(new String[commands.size()]));
		}
		
		
		if(command.startsWith("donaterewards"))
		{
				final List<String> text = new ArrayList<>();
				text.add("When you donate to OS-Anarchy, you can choose a perk of equal");
				text.add("value to the amount that you donated.The perk will be added to your");
				text.add("account and you will instantly start gaining its benefits. Additionally");
				text.add("the total amount that you have donated is saved to your account.");
				text.add("");
				text.add("Once you have donated at least $20 you receive additional benefits");
				text.add(" for free along with any perks you currently have:");
				text.add("- Instant home teleport spell");
				text.add("- No cost for using bones on the altar in Edgeville");
				text.add("- No cost for having Bob Barter decant potions");
				text.add("- No cost for using 'Last-teleport'");
				player.getActionSender().sendTextListInterface("<u>OS-Anarchy Donator Rewards</u>",
						text.toArray(new String[text.size()]));
		}
		
		if(command.startsWith("viewperks"))
		{
			final String playerName = NameUtils.formatName(args[1]);
			final Player foundPlayer = playerService.getPlayer(playerName);
			if(foundPlayer != null)
			{
				final List<String> perks = new ArrayList<>();
				for (final Perk perk : foundPlayer.getPerks()) 
				{
					if (perk != null && perk.isOwned() == true)
					{	
						perks.add("");
						perks.add("<u>" + perk.getName() + "</u>");
						perks.add(perk.getDescription());
					}
				}
				player.getActionSender().sendTextListInterface("<u>" + foundPlayer.getName() + "'s Perks</u>",
						perks.toArray(new String[perks.size()]));
			} else {
				player.sendMessage(playerName + " could not be found. Either they are offline or do not exist.");
			}
		}
		
		if(command.startsWith("perks"))
		{
			final List<String> perks = new ArrayList<>();
			
			perks.add("");
			perks.add("Owned perks are displayed in <col=00ff00><shad=000000>green</shad></col>.");
			perks.add("Unowned perks are displayed in <col=ff0000><shad=000000>red</shad></col>.");
			
			for (final Perk perk : player.getPerks()) 
			{
				if (perk != null && perk.isOwned() == true)
				{	
					perks.add("");
					perks.add("<col=00ff00><shad=000000><u>" + perk.getName() + "</u></shad></col>");
					perks.add("<col=00ff00><shad=000000>" + perk.getDescription() + "</shad></col>");
				} 
				else if (perk != null && perk.isOwned() == false) 
				{
					perks.add("");
					perks.add("<col=ff0000><shad=000000><u>" + perk.getName() + "</u></shad></col>");
					perks.add("<col=ff0000><shad=000000>" + perk.getDescription() + "</shad></col>");
				}
			}
			player.getActionSender().sendTextListInterface("<u>Your Perks</u>",
					perks.toArray(new String[perks.size()]));
		}
		if(command.startsWith("down"))
		{
			if(player.getLocation().getX() == 3755 && player.getLocation().getY() == 5675)
			{
				player.setTeleportTarget(Location.create(3755, 5672, 0));
			} else {
				player.sendMessage("You need to be stood by the ladder in the Motherlode mine to use this.");
			}
			
		}
		
		if(command.startsWith("blocktask"))
		{
			if(player.getSlayer().getSlayerTask() != null)
			{
				slayerService.blockTask(player);
				slayerService.sendConfigs(player);
			} else {
				player.sendMessage("You do not have a slayer task to block.");
			}
			
		}
		
		if(command.startsWith("unblockslot"))
		{
			int buttonIndex = Integer.parseInt(args[1]);
			if(buttonIndex >= 1 && buttonIndex < 6)
			{
				buttonIndex -= 1;
				slayerService.unblockTask(player, buttonIndex);
				slayerService.sendConfigs(player);
			} else { 
				player.sendMessage("Syntax: ::unblockslot #; where # is 1-5.");
			}
			
		}
		/*
		 * if (command.startsWith("specpl0x0r")) {
		 * player.getCombatState().setSpecialEnergy(9500);
		 * player.getActionSender().sendConfig(300, 1000); } if
		 * (command.equals("godpl0x0r")) { player.getSkills().setLevel(Skills.ATTACK,
		 * Integer.MAX_VALUE); player.getSkills().setLevel(Skills.STRENGTH,
		 * Integer.MAX_VALUE); player.getSkills().setLevel(Skills.DEFENCE,
		 * Integer.MAX_VALUE); player.getSkills().setLevel(Skills.RANGE,
		 * Integer.MAX_VALUE); player.getSkills().setLevel(Skills.MAGIC,
		 * Integer.MAX_VALUE); player.getSkills().setLevel(Skills.PRAYER,
		 * Integer.MAX_VALUE); player.getSkills().setLevel(3, Integer.MAX_VALUE); }
		 */

		// if (command.equals("resettask")) {
		// player.getSlayer().setSlayerTask(null);
		// }
		/*
		 * if (command.equals("resettask")) { player.getSlayer().setSlayerTask(null); }
		 */
		//
		// if (command.startsWith("setplayerlvl")) {
		// final String playerName = NameUtils.formatName(args[1]);
		// final int skill = Integer.parseInt(args[2]);
		// final int level = Integer.parseInt(args[3]);
		// final Player target = playerService.getPlayer(playerName);
		// if (target == null) {
		// player.getActionSender().sendMessage("No player found for name '" +
		// playerName + "'");
		// return;
		// }
		// target.getSkills().setLevel(skill, level);
		// if (skill == Skills.PRAYER) {
		// target.getSkills().setPrayerPoints(level, true);
		// }
		// target.getSkills().setExperience(
		// skill,
		// target.getSkills().getExperienceForLevel(
		// level));
		// target.getActionSender().sendMessage(Skills.SKILL_NAME[skill]
		// + " level is now "
		// + level + ".");
		// target.getActionSender().sendString(593, 2, "Combat lvl: " +
		// target.getSkills().getCombatLevel());
		// }

		// if (command.equals("itemn")) {// &&
		// (player.getName().toLowerCase().equals("canine") ||
		// player.getName().toLowerCase().equals("deer low"))) {// that would make it so
		// admins cant use the command but everyone else could lol u just seen me do the
		// command. theirs 2 of them for some fucked reason lol its not fucked i told u
		// why look there is a difference
		// String name = commandString.substring(6);
		// if (name.contains("null")) {
		// return;
		// }
		// Optional<org.rs2server.cache.format.CacheItemDefinition> option =
		// org.rs2server.cache.format.CacheItemDefinition.CACHE
		// .values()
		// .stream()
		// .filter(i -> i.name != null
		// && i.name.toLowerCase().startsWith(
		// name.toLowerCase())).findFirst();
		//
		// if (option.isPresent()) {
		// org.rs2server.cache.format.CacheItemDefinition def = option.get();
		// if (player.getInventory().add(new Item(def.id, 1))) {
		// player.getActionSender().sendMessage("You have just spawned 1x " + def.name +
		// ". id=" + def.id);
		// } else {
		// player.getActionSender().sendMessage("Error adding item.");
		// }
		// } else {
		// player.getActionSender().sendMessage("Failed to look up an item by that name.
		// Syntax is ::itemn [name of item]");
		// player.getActionSender().sendMessage("or ::itemn [name of item] for less
		// specific queries.");
		// }
		// }
		// if (command.equals("pmaster")) {
		// // if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(),
		// // "PvP Zone")) {
		// if (player.getBountyHunter() != null) {
		// player.getActionSender().sendMessage(
		// "You can't do that while in Bounty Hunter.");
		// return;
		// }
		// player.getSkills().setPrayerPoints(99, true);
		// for (int i = 0; i < Skills.SKILL_COUNT; i++) {
		// player.getSkills().setLevel(i, 99);
		// player.getSkills().setExperience(i,
		// player.getSkills().getExperienceForLevel(99));
		// }
		// player.getActionSender().sendSkillLevels();
		// player.getActionSender().sendString(593, 2, "Combat lvl: " +
		// player.getSkills().getCombatLevel());
		// }
		// if (command.equals("tele")) {
		// if (args.length == 3 || args.length == 4) {
		// int x = Integer.parseInt(args[1]);
		// int y = Integer.parseInt(args[2]);
		// int z = player.getLocation().getZ();
		// if (args.length == 4) {
		// z = Integer.parseInt(args[3]);
		// }
		// player.setTeleportTarget(Location.create(x, y, z));
		// } else {
		// player.getActionSender().sendMessage(
		// "Syntax is ::tele [x] [y] [z].");
		// }
		// }
		// if (command.equals("easy")) {
		// treasureTrailService.finishTreasureTrail(player,
		// treasureTrailService.generateTreasureTrail(ClueScrollType.EASY));
		// final List<Loot> loot =
		// ClueScrollRewards.EASY_REWARDS_TABLE.getRandomLoot(1000);
		// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		// }
		//
		// if (command.equals("medium")) {
		// treasureTrailService.finishTreasureTrail(player,
		// treasureTrailService.generateTreasureTrail(ClueScrollType.MEDIUM));
		// final List<Loot> loot =
		// ClueScrollRewards.EASY_REWARDS_TABLE.getRandomLoot(1000);
		// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		// }
		//
		// if (command.equals("hard")) {
		// //treasureTrailService.finishTreasureTrail(player,
		// treasureTrailService.generateTreasureTrail(ClueScrollType.HARD));
		// final List<Loot> loot =
		// ClueScrollRewards.HARD_REWARDS_TABLE.getRandomLoot(1000);
		// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		// }
		// if (command.equals("elite")) {
		// //treasureTrailService.finishTreasureTrail(player,
		// treasureTrailService.generateTreasureTrail(ClueScrollType.HARD));
		// final List<Loot> loot =
		// ClueScrollRewards.ELITE_REWARDS_TABLE.getRandomLoot(1000);
		// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		// }
		// if (command.equals("spawn")) {// &&
		// (player.getName().toLowerCase().equals("canine")||
		// player.getName().toLowerCase().equals("deer low"))) {
		// // if
		// // (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(),
		// // "PvP Zone")) {
		// if (player.getBountyHunter() != null) {
		// player.getActionSender()
		// .sendMessage(
		// "You can't do that while being inside Bounty Hunter.");
		// return;
		// }
		// if (player.getCombatState().getLastHitTimer() > System
		// .currentTimeMillis()) {
		// player.getActionSender().sendMessage(
		// "You can't spawn during combat!");
		// return;
		// }
		// if (args.length == 2 || args.length == 3) {
		// int id = Integer.parseInt(args[1]);
		// if (org.rs2server.cache.format.CacheItemDefinition
		// .get(id) == null) {
		// // player.getActionSender().sendMessage("That item is currently not in our
		// database.");
		// return;
		// }
		// int count = 1;
		// if (args.length == 3) {
		// count = Integer.parseInt(args[2]);
		// }
		// if (!CacheItemDefinition.get(id).stackable &&
		// !CacheItemDefinition.get(id).isNoted()) {
		// if (count > player.getInventory().freeSlots()) {
		// count = player.getInventory().freeSlots();
		// }
		// }
		// Item item = new Item(id, count);
		// player.getInventory().add(
		// player.checkForSkillcape(item));
		// } else {
		// player.getActionSender().sendMessage(
		// "Syntax is ::item [id] [count].");
		// }
		// }

		/*
		 * if (command.equals("myperms")) {
		 * player.sendMessage(Arrays.toString(player.getDatabaseEntity().getPermissions(
		 * ).toArray())); }
		 */
		
		 if (commandString.equalsIgnoreCase("pcommands")) 
		 {
		 }
			
		 
		if (command.equals("changepass")) {
			if (!player.isEnteredPinOnce() && player.getDatabaseEntity().getPlayerSettings().isBankSecured()) {
				player.getActionSender()
						.sendMessage("Please go to a bank and enter your pin before changing your password.");
				return;
			}
			if (args.length > 2) {
				player.getActionSender().sendMessage("Password may not contain spaces.");
				return;
			}
			String pass = args[1];
			player.setPassword(pass);
			player.getActionSender().sendMessage("Password is now: " + pass);
		}

		if (command.equals("lock")) {
			String name = TextUtils.upperFirst(args[1]);
			Optional<Integer> lock = Misc.forSkillName(name);
			if (lock.isPresent()) {
				int skillId = lock.get();
				List<Integer> locked = player.getDatabaseEntity().getPlayerSettings().getLockedSkills();
				if (locked.contains(skillId)) {
					player.getActionSender().sendMessage("This skill is already locked.");
					return;
				}
				locked.add(skillId);
				player.getActionSender().sendMessage("You have locked your experience for " + name + ".");
			}
		}
		if (command.equals("unlock")) {
			String name = TextUtils.upperFirst(args[1]);
			Optional<Integer> lock = Misc.forSkillName(name);
			if (lock.isPresent()) {
				int skillId = lock.get();
				List<Integer> locked = player.getDatabaseEntity().getPlayerSettings().getLockedSkills();
				if (locked.contains(skillId)) {
					locked.remove(locked.indexOf(skillId));
					player.getActionSender().sendMessage("You have unlocked your experience for " + name + ".");
				} else {
					player.getActionSender().sendMessage("This skill is currently unlocked.");
				}
			}
		}

		if (permissionService.isAny(player, PermissionService.PlayerPermissions.IRON_MAN,
				PermissionService.PlayerPermissions.PLAYER, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN,
				PermissionService.PlayerPermissions.HARDCORE_IRON_MAN)) 
		{
			if (commandString.equalsIgnoreCase("players")) 
			{
				int players = World.getWorld().getPlayers().size();
				if (players > 1)
					player.getActionSender().sendMessage("There are currently " + players + " players online.");
				else
					player.getActionSender().sendMessage("There is currently " + players + " player online.");
				
				if(permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)	|| permissionService.is(player, PermissionService.PlayerPermissions.MODERATOR))
				{
					String img = "";
					final List<String> list_of_players = new ArrayList<>();
					for (final Player list_player : World.getWorld().getPlayers()) {
						if (list_player != null) {
							if(permissionService.is(list_player, PermissionService.PlayerPermissions.MODERATOR))
								img = "<img=0>";
							else if(permissionService.is(list_player, PermissionService.PlayerPermissions.ADMINISTRATOR))
								img = "<img=1>";
							else if(permissionService.is(list_player, PermissionService.PlayerPermissions.IRON_MAN))
								img = "<img=2>";
							else if(permissionService.is(list_player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN))
								img = "<img=3>";
							else if(permissionService.is(list_player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN))
								img = "<img=10>";
							else
								img = "";
							
							list_of_players.add(img + list_player.getName() + " (Level-" + list_player.getSkills().getCombatLevel() + ")");
						}
					}
					player.getActionSender().sendTextListInterface("<u>Online Players</u>: " + players,
							list_of_players.toArray(new String[list_of_players.size()]));
				}
			}
			/*
			 * if (commandString.equalsIgnoreCase("commands")) {
			 * player.getActionSender().sendMessage("For player commands type - ::pcommands"
			 * );
			 * player.getActionSender().sendMessage("For helper commands type - ::hcommands"
			 * ); player.getActionSender().
			 * sendMessage("For moderator commands type - ::mcommands");
			 * player.getActionSender().
			 * sendMessage("For administrator commands type - ::acommands");
			 * 
			 * }
			 */
			/*
			 * if (commandString.equalsIgnoreCase("pcommands")) {
			 * player.getActionSender().sendInterface(345, false);
			 * player.getActionSender().sendConfig(375, 8);
			 * player.getActionSender().sendCS2Script(917, new Object[]{-1, -1}, "ii")
			 * .sendCS2Script(603, new Object[]{"Loading...", 22609929, 22609930, 22609931,
			 * 22609921, "<img=35>Kronos Player Commands"}, "sIIIIs") .sendCS2Script(604,
			 * new Object[]{"", 22609928, 22609927}, "IIs") .sendCS2Script(604, new
			 * Object[]{"", 22609926, 22609925}, "IIs") .sendCS2Script(604, new Object[]{"",
			 * 22609924, 22609923}, "IIs") .sendCS2Script(609, new Object[]{22609930, 5, 12,
			 * 495, 495, 0, 16750623, "<img=35>Kronos Player Commands List."}, "siidfiiI")
			 * .sendCS2Script(610, new Object[]{22609930, "aaa", "::players"}, "ssI")
			 * .sendCS2Script(610, new Object[]{22609930, "aaa",
			 * "::placeholder true/false - Enable or Disable Placeholders"}, "ssI")
			 * .sendCS2Script(610, new Object[]{22609930, "aaa", "::changepass"}, "ssI")
			 * .sendCS2Script(610, new Object[]{22609930, "aaa",
			 * "::lock - Locks your experience"}, "ssI") .sendCS2Script(610, new
			 * Object[]{22609930, "aaa", "::unlock - Unlocks your experience"}, "ssI")
			 * .sendCS2Script(609, new Object[]{22609930, 5, 12, 495, 495, 0, 16750623, ""},
			 * "siidfiiI") .sendCS2Script(618, new Object[]{1, 22609929, 22609931,
			 * 22609930}, "III1") // .sendCS2Script(604, new Object[]{"History", 22609928,
			 * 22609927}, "IIs") // .sendCS2Script(604, new Object[]{"Refresh", 22609926,
			 * 22609925}, "IIs") .sendString(345, 2,
			 * "<img=35>Weclome to Kronos's Command List 1.0") // .sendCS2Script(604, new
			 * Object[]{"Vote", 22609924, 22609923}, "IIs") ; }
			 */
			/*
			 * if (command.equals("placeholder")) { if (args.length > 2 ||
			 * !args[1].equalsIgnoreCase("false") && !args[1].equalsIgnoreCase("true")) {
			 * return; } boolean enabled = Boolean.parseBoolean(args[1]);
			 * player.getDatabaseEntity().getPlayerSettings().setPlaceHolderEnabled(enabled)
			 * ; player.getActionSender().sendMessage("Placeholders; " + (enabled ?
			 * " Enabled " : "Disabled")); if (!enabled) {
			 * player.getBank().stream().filter(Objects::nonNull).filter(i -> i.getCount()
			 * == 0).forEach(i -> { int slot = player.getBank().getSlotById(i.getId()); int
			 * tabId = player.getBanking().getTabByItemSlot(slot);
			 * player.getBank().set(slot, null);
			 * player.getBanking().decreaseTabStartSlots(tabId); });
			 * player.getBank().shift(); } }
			 */
			/*
			 * if (permissionService.is(player,
			 * PermissionService.PlayerPermissions.DONATOR)) {//if
			 * (permissionService.is(player, PermissionService.PlayerPermissions.DONATOR)) {
			 * if (command.startsWith("yell")) { String msg = commandString.substring(5);
			 * int highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=30><col=04a00c>[Donator]") : "<col=04a00c>") + "<col=04a00c>" +
			 * player.getName() + "<col=04a00c>: <col=04a00c>" + msg); } }
			 */

			/*
			 * if (permissionService.is(player, PermissionService.PlayerPermissions.SUPER))
			 * {//if (permissionService.is(player,
			 * PermissionService.PlayerPermissions.DONATOR)) { if
			 * (command.startsWith("yell")) { String msg = commandString.substring(5); int
			 * highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=28><col=6f04a0>[Super]") : "<col=6f04a0>") + "<col=6f04a0>" +
			 * player.getName() + "<col=276BD9>: <col=6f04a0>" + msg);("<col=276BD9>[<img="
			 * + highest + ">") : "Super" + "<col=276BD9>]" + player.getName() +
			 * "<col=276BD9>]: <col=276BD9>" + msg)); } }
			 * 
			 * if (permissionService.is(player,
			 * PermissionService.PlayerPermissions.EXTREME)) {//if
			 * (permissionService.is(player, PermissionService.PlayerPermissions.DONATOR)) {
			 * if (command.startsWith("yell")) { String msg = commandString.substring(5);
			 * int highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=30><col=04a00c>[Donator]") : "<col=04a00c>") + "<col=04a00c>" +
			 * player.getName() + "<col=04a00c>: <col=04a00c>" + msg);("<col=ff0000>[<img="
			 * + highest + ">") : "Extreme" + "<col=ff0000>]" + player.getName() +
			 * "<col=ff0000>]: <col=ff0000>" + msg)); } }
			 * 
			 * if (permissionService.is(player,
			 * PermissionService.PlayerPermissions.SPONSOR)) {//if
			 * (permissionService.is(player, PermissionService.PlayerPermissions.DONATOR)) {
			 * if (command.startsWith("yell")) { String msg = commandString.substring(5);
			 * int highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=31><col=efcd09>[Sponsor]") : "<col=efcd09>") + "<col=efcd09>" +
			 * player.getName() + "<col=efcd09>: <col=efcd09>" + msg);("<col=ff0000>[<img="
			 * + highest + ">") : "Extreme" + "<col=ff0000>]" + player.getName() +
			 * "<col=ff0000>]: <col=ff0000>" + msg)); } }
			 * 
			 * if (permissionService.is(player, PermissionService.PlayerPermissions.COM))
			 * {//if (permissionService.is(player,
			 * PermissionService.PlayerPermissions.DONATOR)) { if
			 * (command.startsWith("yell")) { String msg = commandString.substring(5); int
			 * highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=45><col=057993>[Manager]") : "<col=057993>") + "<col=057993>" +
			 * player.getName() + "<col=057993>: <col=057993>" + msg);("<col=ff0000>[<img="
			 * + highest + ">") : "Extreme" + "<col=ff0000>]" + player.getName() +
			 * "<col=ff0000>]: <col=ff0000>" + msg)); } }
			 */

			/*
			 * if (command.startsWith("yell") && permissionService.is(player,
			 * PermissionService.PlayerPermissions.HELPER)) { String msg =
			 * command.substring(5); int highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=46><shad=000000><col=0440a0>[Helper]") : "<shad=000000><col=0440a0>")
			 * + "<shad=000000><col=0440a0>" + player.getName() +
			 * "<shad=000000><col=0440a0>: <shad=000000><col=0440a0>" +
			 * msg);/*("<col=0440a0>[<img=" + highest + ">") : "Helper" + "<col=0440a0>" +
			 * player.getName() + "<col=0440a0>]: <col=0440a0>" + msg)); }
			 */

			/*
			 * if (permissionService.is(player,
			 * PermissionService.PlayerPermissions.YOUTUBER)) {//if
			 * (permissionService.is(player, PermissionService.PlayerPermissions.DONATOR)) {
			 * if (command.startsWith("yell")) { String msg = commandString.substring(5);
			 * int highest =
			 * Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			 * World.getWorld().sendWorldMessage((highest != -1 ?
			 * ("<img=27><col=880000>[YouTuber]") : "<col=880000>") + "<col=880000>" +
			 * player.getName() + "<col=880000>: <col=880000>" + msg);("<col=880000>[<img="
			 * + highest + ">") : "Youtuber" + "<col=880000>]" + player.getName() +
			 * "<col=880000>]: <col=880000>" + msg)); } }
			 */

		}

		if (permissionService.isAny(player, PermissionServiceImpl.SPECIAL_PERMISSIONS)) {
			// if (command.equals("placeholder")) {
			// if (args.length > 2 || !args[1].equalsIgnoreCase("false") &&
			// !args[1].equalsIgnoreCase("true")) {
			// return;
			// }
			// boolean enabled = Boolean.parseBoolean(args[1]);
			// player.getDatabaseEntity().getPlayerSettings().setPlaceHolderEnabled(enabled);
			// player.getActionSender().sendMessage("Placeholders; " + (enabled ? " Enabled
			// " : "Disabled"));
			// if (!enabled) {
			// player.getBank().stream().filter(Objects::nonNull).filter(i -> i.getCount()
			// == 0).forEach(i -> {
			// int slot = player.getBank().getSlotById(i.getId());
			// int tabId = player.getBanking().getTabByItemSlot(slot);
			// player.getBank().set(slot, null);
			// player.getBanking().decreaseTabStartSlots(tabId);
			// });
			// player.getBank().shift();
			// }
			// }
		}

		if (permissionService.is(player, PermissionService.PlayerPermissions.HELPER)) {
			handleHelperCommands(player, args, commandString);
		}

		if (permissionService.is(player, PermissionService.PlayerPermissions.MODERATOR)) {
			handleModeratorCommands(player, commandString, args, commandString);
		}

		if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
			handleModeratorCommands(player, commandString, args, commandString);
			handleAdminCommands(player, commandString, args);
		}
	}

	public void handleHelperCommands(Player player, String[] args, String commandString) {
		String command = args[0].toLowerCase();
		if (command.equals("timedmute")) {
			String playerName = args[1].replace("_", " ");
			Player punish = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					punish = p;
					break;
				}
			}
			if (punish != null) {
				try {
					int days = Integer.parseInt(args[2]);
					int hours = Integer.parseInt(args[3]);
					int minutes = Integer.parseInt(args[4]);
					punish.getPunishment().punish(PunishmentType.MUTE,
							new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(),
									DateTime.now().getDayOfMonth() + days, DateTime.now().getHourOfDay() + hours,
									DateTime.now().getMinuteOfHour() + minutes, DateTime.now().getSecondOfMinute()));
					player.getActionSender().sendMessage(punish.getName() + " for: " + days + " days and " + hours
							+ " hours and " + minutes + " minutes.");
					File file = new File("data/mutedUsers.xml");
					List<String> mutedUsers = XMLController.readXML(file);
					mutedUsers.add(playerName);
					XMLController.writeXML(mutedUsers, file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (commandString.equalsIgnoreCase("hcommands")) {
			player.getActionSender().sendInterface(345, false);
			player.getActionSender().sendConfig(375, 8);
			player.getActionSender().sendCS2Script(917, new Object[] { -1, -1 }, "ii")
					.sendCS2Script(603,
							new Object[] { "Loading...", 22609929, 22609930, 22609931, 22609921,
									"<img=35>Kronos Helper Commands" },
							"sIIIIs")
					.sendCS2Script(604, new Object[] { "", 22609928, 22609927 }, "IIs")
					.sendCS2Script(604, new Object[] { "", 22609926, 22609925 }, "IIs")
					.sendCS2Script(604, new Object[] { "", 22609924, 22609923 }, "IIs")
					.sendCS2Script(609,
							new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623,
									"<img=35>Kronos Helper Commands List." },
							"siidfiiI")
					.sendCS2Script(610, new Object[] { 22609930, "aaa", "::players" }, "ssI")
					.sendCS2Script(610,
							new Object[] { 22609930, "aaa",
									"::placeholder true/false - Enable or Disable Placeholders" },
							"ssI")
					.sendCS2Script(610, new Object[] { 22609930, "aaa", "::changepass" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "aaa", "::lock - Locks your experience" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "aaa", "::unlock - Unlocks your experience" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "aaa", "::yell messagehere" }, "ssI")
					.sendCS2Script(610,
							new Object[] { 22609930, "aaa", "::kick playername - Kicks a player from game" }, "ssI")
					.sendCS2Script(609, new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "" }, "siidfiiI")
					.sendCS2Script(618, new Object[] { 1, 22609929, 22609931, 22609930 }, "III1")
					// .sendCS2Script(604, new Object[]{"History", 22609928, 22609927}, "IIs")
					// .sendCS2Script(604, new Object[]{"Refresh", 22609926, 22609925}, "IIs")
					.sendString(345, 2, "<img=35>Weclome to Kronos's Command List 1.0")
			// .sendCS2Script(604, new Object[]{"Vote", 22609924, 22609923}, "IIs")
			;
		}
		if (command.equals("kick")) {
			String playerName = NameUtils.formatName(args[1]);
			Player kick = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					kick = p;
					break;
				}
			}
			if (kick == null) {
				player.getActionSender().sendMessage("That player is not online.");
			} else if (kick.getCombatState().getLastHitTimer() > System.currentTimeMillis()) {
				player.getActionSender()
						.sendMessage("Please wait for that player to leave combat before kicking them.");
			} else {
				kick.getActionSender().sendLogout();
				World.getWorld().unregister(kick);
				player.getActionSender().sendMessage("Successfully kicked " + kick.getName() + ".");
			}
		}
	}

	public void handleModeratorCommands(Player player, String playerCommand, String[] args, String commandString) {
		try {
			String command = args[0].toLowerCase();
			if (command.equals("checkbank")) {
				String playerName = NameUtils.formatName(commandString.substring(10).trim());
				Player ban = null;
				for (Player p : World.getWorld().getPlayers()) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						ban = p;
						break;
					}
				}
				if (ban != null) {
					player.getBanking().openPlayerBank(ban);
				}
			}
			if (commandString.equalsIgnoreCase("mcommands")) {
				player.getActionSender().sendInterface(345, false);
				player.getActionSender().sendConfig(375, 8);
				player.getActionSender().sendCS2Script(917, new Object[] { -1, -1 }, "ii")
						.sendCS2Script(603,
								new Object[] { "Loading...", 22609929, 22609930, 22609931, 22609921,
										"<img=35>Kronos Moderator Commands" },
								"sIIIIs")
						.sendCS2Script(604, new Object[] { "", 22609928, 22609927 }, "IIs")
						.sendCS2Script(604, new Object[] { "", 22609926, 22609925 }, "IIs")
						.sendCS2Script(604, new Object[] { "", 22609924, 22609923 }, "IIs")
						.sendCS2Script(609,
								new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623,
										"<img=35>Kronos Moderator Commands List." },
								"siidfiiI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::players" }, "ssI")
						.sendCS2Script(610,
								new Object[] { 22609930, "aaa",
										"::placeholder true/false - Enable or Disable Placeholders" },
								"ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::changepass" }, "ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::lock - Locks your experience" }, "ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::unlock - Unlocks your experience" },
								"ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::yell messagehere" }, "ssI")
						.sendCS2Script(610,
								new Object[] { 22609930, "aaa", "::kick playername - Kicks a player from ingame" },
								"ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::mute playername - Mutes the player" },
								"ssI")
						.sendCS2Script(610,
								new Object[] { 22609930, "aaa", "::unmute playername - Unmutes the player" }, "ssI")
						.sendCS2Script(610,
								new Object[] { 22609930, "aaa",
										"::checkinv playername - Checks the players inventory" },
								"ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::xteletome playername" }, "ssI")
						.sendCS2Script(610, new Object[] { 22609930, "aaa", "::xteleto x y z (coordinates)" }, "ssI")
						.sendCS2Script(609, new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "" }, "siidfiiI")
						.sendCS2Script(618, new Object[] { 1, 22609929, 22609931, 22609930 }, "III1")
						.sendString(345, 2, "<img=35>Weclome to Kronos's Command List 1.0");
			}
			if (command.startsWith("yell")
					&& permissionService.is(player, PermissionService.PlayerPermissions.MODERATOR)) {
				String msg = commandString.substring(5);
				int highest = Misc.getModIconForPerm(permissionService.getHighestPermission(player));
				World.getWorld()
					.sendWorldMessage("<img=0>[<col=880088>Moderator</col>] " + player.getName() + ": "+ msg);
			}
			
			if (command.equals("checkinv")) 
			{
				String playerName = NameUtils.formatName(commandString.substring(9).trim());
				Player ban = null;
				for (Player p : World.getWorld().getPlayers()) 
				{
					if (p.getName().equalsIgnoreCase(playerName)) 
					{
						ban = p;
						break;
					}
				}
				
				if (ban != null) 
				{
						final List<String> inventory_items = new ArrayList<>();
						for (final Item item : ban.getInventory().toArray()) 
						{
							if (item != null) 
							{							
								inventory_items.add(item.getDefinition2().getName() + " x " + item.getCount());
							}
						}
						player.getActionSender().sendTextListInterface("<u>" + ban.getName() + "'s inventory</u>",
								inventory_items.toArray(new String[inventory_items.size()]));
				}
					//player.getActionSender().sendMessage("--Start of " + ban.getName() + "'s Inventory--");
					//for (Item item : ban.getInventory().toArray()) {
					//	if (item != null) {
					//		player.getActionSender().sendMessage(item.getCount() + "x " + item.getDefinition2().getName());
					//	}
					//}
					//player.getActionSender().sendMessage("--End of " + ban.getName() + "'s Inventory--");
			}
			
			if (command.equals("timedmute")) {
				String playerName = args[1].replace("_", " ");
				Player punish = null;
				for (Player p : World.getWorld().getPlayers()) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						punish = p;
						break;
					}
				}
				if (punish != null) {
					try {
						int days = Integer.parseInt(args[2]);
						int hours = Integer.parseInt(args[3]);
						int minutes = Integer.parseInt(args[4]);
						punish.getPunishment().punish(PunishmentType.MUTE,
								new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(),
										DateTime.now().getDayOfMonth() + days, DateTime.now().getHourOfDay() + hours,
										DateTime.now().getMinuteOfHour() + minutes,
										DateTime.now().getSecondOfMinute()));
						player.getActionSender().sendMessage(punish.getName() + " for: " + days + " days and " + hours
								+ " hours and " + minutes + " minutes.");
						File file = new File("data/mutedUsers.xml");
						List<String> mutedUsers = XMLController.readXML(file);
						mutedUsers.add(playerName);
						XMLController.writeXML(mutedUsers, file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (command.equals("timedban")) {
				String playerName = args[1].replace("_", " ");
				Player punish = null;
				for (Player p : World.getWorld().getPlayers()) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						punish = p;
						break;
					}
				}
				if (punish != null) {
					int days = Integer.parseInt(args[2]);
					int hours = Integer.parseInt(args[3]);
					int minutes = Integer.parseInt(args[4]);
					punish.getPunishment().punish(PunishmentType.BAN,
							new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(),
									DateTime.now().getDayOfMonth() + days, DateTime.now().getHourOfDay() + hours,
									DateTime.now().getMinuteOfHour() + minutes, DateTime.now().getSecondOfMinute()));
					player.getActionSender().sendMessage(punish.getName() + " for: " + days + " days and " + hours
							+ " hours and " + minutes + " minutes.");
				}
			}

			if (command.equals("checkonline")) {
				final String search = StringUtils.join(args, " ", 1, args.length).toLowerCase();
				Player target = playerService.getPlayer(search);
				if (target != null) {
					List<String> names = new ArrayList<>();
					World.getWorld().getPlayers().stream().filter(Objects::nonNull)
							.filter(p -> p.getIP().equals(target.getIP())).forEach(p -> names.add(p.getName()));
					player.getActionSender()
							.sendMessage(target.getIP() + " accounts: [" + Arrays.toString(names.toArray()) + " ]");
				}
				// Optional.of(playerService.getPlayer(search)).ifPresent(o ->
				// o.getDetails().getOnlineAccountsFromAddress().forEach(p ->
				// player.getActionSender().sendMessage(o.getName() + " - " +
				// o.getSession().getRemoteAddress())));
			}

			// if (command.equals("fakedrop")) {
			// final String search = args[1].replaceAll("_", " ");
			// final String itemName = args[2].replaceAll("_", " ");
			// Player target = playerService.getPlayer(search);
			// if (target != null) {
			// World.getWorld().sendWorldMessage("<col=880000><img=33>Drops: <col=880000>" +
			// target.getName() + " has just received 1x " + itemName + ".");
			// }
			// }
			if (command.startsWith("teleto")) {
				String playerName = NameUtils.formatName(commandString.substring(8).trim());
				Player teleTo = null;
				for (Player p : World.getWorld().getPlayers()) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						teleTo = p;
						break;
					}
				}
				if (teleTo != null) {
					if (teleTo.isInWilderness()) {
						player.getActionSender().sendMessage("Player is in the Wilderness.");
						return;
					}
					player.setTeleportTarget(teleTo.getLocation());
				}
			}
			if (command.equals("kick")) {
				String playerName = playerCommand.substring(5);
				Player kick = null;
				for (Player p : World.getWorld().getPlayers()) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						kick = p;
						break;
					}
				}
				if (kick == null) {
					player.getActionSender().sendMessage("That player is not online.");
				} else if (kick.getCombatState().getLastHitTimer() > System.currentTimeMillis()) {
					player.getActionSender()
							.sendMessage("Please wait for that player to leave combat before kicking them.");
				} else {
					kick.getActionSender().sendLogout();
					World.getWorld().unregister(kick);
					player.getActionSender().sendMessage("Successfully kicked " + kick.getName() + ".");
				}
			}
			/*
			 * if (command.equals("online")) {
			 * World.getWorld().getPlayers().stream().filter(Objects::nonNull).forEach(p ->
			 * player.getActionSender().sendMessage(p.getName() + " [UID=" +
			 * p.getDetails().getUUID() + "]")); }
			 */
			/*
			 * if (command.equals("ipban")) { String playerName =
			 * playerCommand.substring(6); Player ban = null; for (Player p :
			 * World.getWorld().getPlayers()) { if
			 * (p.getName().equalsIgnoreCase(playerName)) { ban = p; break; } } if (ban !=
			 * null && ban.getCombatState().getLastHitTimer() > System .currentTimeMillis())
			 * { player.getActionSender() .sendMessage(
			 * "Please wait for that player to leave combat before banning them."); } else {
			 * if (ban == null) { player.getActionSender() .sendMessage(
			 * "That player is not online, but will be unable to login now."); } else { File
			 * file = new File("data/ipBannedUsers.xml"); List<String> ipBannedUsers =
			 * XMLController.readXML(file); ipBannedUsers.add(ban.getIP());
			 * XMLController.writeXML(ipBannedUsers, file);
			 * ban.getActionSender().sendLogout(); player.getActionSender().sendMessage(
			 * "Successfully ip banned " + playerName + "."); } } } if
			 * (command.equals("ipmute")) { String playerName = playerCommand.substring(7);
			 * Player ban = null; for (Player p : World.getWorld().getPlayers()) { if
			 * (p.getName().equalsIgnoreCase(playerName)) { ban = p; break; } } if (ban ==
			 * null) { player.getActionSender() .sendMessage( "That player is not online.");
			 * } else { File file = new File("data/ipMutedUsers.xml"); List<String>
			 * ipMutedUsers = XMLController.readXML(file); ipMutedUsers.add(ban.getIP());
			 * XMLController.writeXML(ipMutedUsers, file); ban.getSettings().setMuted(true);
			 * player.getActionSender().sendMessage( "Successfully ip muted " + playerName +
			 * "."); } } if (command.equals("ban")) { String playerName =
			 * playerCommand.substring(4); Player ban = null; for (Player p :
			 * World.getWorld().getPlayers()) { if
			 * (p.getName().equalsIgnoreCase(playerName)) { ban = p; break; } } if (ban !=
			 * null && ban.getCombatState().getLastHitTimer() > System .currentTimeMillis())
			 * { player.getActionSender() .sendMessage(
			 * "Please wait for that player to leave combat before banning them."); } else {
			 * File file = new File("data/bannedUsers.xml"); List<String> bannedUsers =
			 * XMLController.readXML(file); bannedUsers.add(playerName);
			 * XMLController.writeXML(bannedUsers, file); if (ban == null) {
			 * player.getActionSender() .sendMessage(
			 * "That player is not online, but will be unable to login now."); } else {
			 * ban.getActionSender().sendLogout(); } player.getActionSender().sendMessage(
			 * "Successfully banned " + playerName + "."); } } else if
			 * (command.equals("unban")) { String playerName = playerCommand.substring(6);
			 * Player ban = null; for (Player p : World.getWorld().getPlayers()) { if
			 * (p.getName().equalsIgnoreCase(playerName)) { ban = p; break; } } if (ban !=
			 * null && ban.getCombatState().getLastHitTimer() > System .currentTimeMillis())
			 * { player.getActionSender() .sendMessage(
			 * "Please wait for that player to leave combat before banning them."); } else {
			 * File file = new File("data/bannedUsers.xml"); List<String> bannedUsers =
			 * XMLController.readXML(file); bannedUsers.remove(playerName);
			 * XMLController.writeXML(bannedUsers, file); if (ban == null) {
			 * player.getActionSender() .sendMessage(
			 * "That player is not online, but will be un-banned"); }
			 * player.getActionSender().sendMessage( "Successfully un-banned " + playerName
			 * + "."); } } else if (command.equals("mute")) { // TODO String playerName = //
			 * NameUtils.formatName(commandString.substring(4).trim()); String playerName =
			 * playerCommand.substring(5); Player ban = null; for (Player p :
			 * World.getWorld().getPlayers()) { if
			 * (p.getName().equalsIgnoreCase(playerName)) { ban = p; break; } } if (ban !=
			 * null && ban.getCombatState().getLastHitTimer() > System .currentTimeMillis())
			 * { player.getActionSender() .sendMessage(
			 * "Please wait for that player to leave combat before banning them."); } else {
			 * File file = new File("data/mutedUsers.xml"); List<String> mutedUsers =
			 * XMLController.readXML(file); mutedUsers.add(playerName);
			 * XMLController.writeXML(mutedUsers, file); if (ban == null) {
			 * player.getActionSender() .sendMessage(
			 * "That player is not online, but will be unable to login now."); } else {
			 * ban.getSettings().setMuted(true); } player.getActionSender().sendMessage(
			 * "Successfully muted " + playerName + ".");
			 * ban.getActionSender().sendMessage("You have been muted by: " +
			 * player.getName() + ", To Appeal this visit the forums."); } } else if
			 * (command.equals("unmute")) { String playerName = playerCommand.substring(7);
			 * playerName.replaceAll("_", " "); Player ban = null; for (Player p :
			 * World.getWorld().getPlayers()) { if
			 * (p.getName().equalsIgnoreCase(playerName)) { ban = p; break; } } if (ban !=
			 * null && ban.getCombatState().getLastHitTimer() > System .currentTimeMillis())
			 * { player.getActionSender() .sendMessage(
			 * "Please wait for that player to leave combat before banning them."); } else {
			 * File file = new File("data/mutedUsers.xml"); List<String> mutedUsers =
			 * XMLController.readXML(file); mutedUsers.remove(playerName);
			 * XMLController.writeXML(mutedUsers, file); if (ban == null) {
			 * player.getActionSender() .sendMessage( "That player is not online"); } else {
			 * ban.getSettings().setMuted(false); } player.getActionSender().sendMessage(
			 * "Successfully un-muted " + playerName + ".");
			 * ban.getActionSender().sendMessage("You have been un-muted by: " +
			 * player.getName() + "."); } }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Player player, String playerCommand, String[] args, String commandString
	public void handleAdminCommands(Player player, String commandString, String[] args) throws IOException {
		String command = args[0].toLowerCase();
		if (command.equals("sound")) {
			int soundId = Integer.parseInt(args[1]);
			player.getActionSender().playSound(Sound.create(Integer.parseInt(args[1]), 0));
		}
		if (command.equals("song")) {
			RegionMusicService region = Server.getInjector().getInstance(RegionMusicService.class);
			region.updateRegionMusic(player, player.getRegionId());
			musicService.play(player, Song.of(Integer.parseInt(args[1])));
		}

		if (command.equals("opendmm")) {
			Container[] itemsKeptOnDeath = null;
			int count = 3;
			ItemService itemService = Server.getInjector().getInstance(ItemService.class);
			itemsKeptOnDeath = itemService.getItemsKeptOnDeath(player);
			Object[] keptItems = new Object[] { -1, -1, "null", 0, 0,
					(itemsKeptOnDeath[0].size() >= 4 && itemsKeptOnDeath[0].get(3) != null)
							? itemsKeptOnDeath[0].get(3).getId()
							: -1,
					(itemsKeptOnDeath[0].size() >= 3 && itemsKeptOnDeath[0].get(2) != null)
							? itemsKeptOnDeath[0].get(2).getId()
							: -1,
					(itemsKeptOnDeath[0].size() >= 2 && itemsKeptOnDeath[0].get(1) != null)
							? itemsKeptOnDeath[0].get(1).getId()
							: -1,
					(itemsKeptOnDeath[0].size() >= 1 && itemsKeptOnDeath[0].get(0) != null)
							? itemsKeptOnDeath[0].get(0).getId()
							: -1,
					count, 0 };
			player.getActionSender()
					// .sendAccessMask(2, 102, 18, 0, 4)
					.sendAccessMask(2, 226, 16, 0, 28).sendCS2Script(118, keptItems, "iiooooiisii")
					.sendInterface(226, true);
		}

		if (command.equals("testimg")) {
			int crownId = Integer.parseInt(args[1]);
			player.getActionSender().sendMessage("<img=" + crownId + ">");
		}

		if (command.equals("openbank")) {
			ScriptManager.getScriptManager().invoke("openbankcmd", player);
			// Bank.open(player);
		}
		if (command.equals("pouch")) {
			runePouchService.openPouchInterface(player);
		}
		if (command.equals("fillbank")) {
			for (int i = 0; i < Bank.SIZE; i++) {
				player.getBank().add(new Item(i, 1));
			}
		}
		if (command.equals("removetb")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.getDatabaseEntity().getPlayerSettings().setTeleBlocked(false);
				foundPlayer.getDatabaseEntity().getPlayerSettings().setTeleBlockTimer(0);
			}
		}
		if (command.equals("ex")) {
			// examineService.openMonsterExamine(player, Integer.parseInt(args[1]));
		}
		if (command.equals("debug")) {
			ScriptManager.getScriptManager().invoke("debugcmd", player);
			// player.setDebugMode(!player.isDebugMode());
			// player.sendMessage("Debug: " + player.isDebugMode());
		}
		if (command.equals("testraid")) {
			int pane = player.getAttribute("tabmode");
			int tabId = pane == 548 ? 65 : pane == 161 ? 56 : 56;
			player.getActionSender().sendSidebarInterface(tabId, 500);// Raid Sidebar interface
			player.getActionSender().sendConfig(1055, 8768);// Changes tab icon sprite
			player.getActionSender().sendConfig(1430, 1336071168);// Raid Preferred combat level 90 + Preferred skill
																	// total of 1000
			player.getActionSender().sendConfig(1432, 1);// Raid party size
			player.getActionSender().sendInterface(513, false);
			player.getActionSender().sendMessage("<col=E172E5>A new test raid has begun!");
			// player.getActionSender().sendBroadcast("<col=E172E5> " + player.getName() +"
			// has just started a new raid instance!");
		}
		if (command.equals("rls")) {
			ScriptManager.getScriptManager().loadScripts(Constants.SCRIPTS_DIRECTORY);
			player.getActionSender().sendMessage("Reloaded scripts");
		}
		if (command.equals("reloadscripts")) {
			ScriptManager.getScriptManager().loadScripts(Constants.SCRIPTS_DIRECTORY);
			player.getActionSender().sendMessage("Reloaded scripts");
		}
		if (command.equals("kk")) {
			LootGenerationService lootService = Server.getInjector().getInstance(LootGenerationService.class);
			player.getBank().clear();
			LootGenerationService.NpcLootTable npcLootTable = lootService.getNpcTable(Integer.parseInt(args[1]));
			for (int x = 0; x < 200; x++) {
				LootTable table = lootService.getRandomTable(npcLootTable);
				List<Loot> loot = table.generateNpcDrop(Integer.parseInt(args[1]), npcLootTable.getRolls());

				loot.stream().filter(Objects::nonNull).forEach(i -> player.getBank()
						.add(new Item(i.getItemId(), Misc.random(i.getMinAmount(), i.getMaxAmount()))));
			}

		}
		if (command.equals("npcinfo")) {
			int id = Integer.parseInt(args[1]);
			CacheNPCDefinition def = CacheNPCDefinition.get(id);
			if (def == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("Name: ").append(def.getName()).append(", Level: ").append(def.getCombatLevel())
					.append(", Stand: ").append(def.stanceAnimation);
			player.getActionSender().sendMessage(sb.toString());
		}
		if (command.equals("nemote")) {
			String npc = args[1];
			int anim = Integer.parseInt(args[2]);
			World.getWorld().getNPCs().stream().filter(n -> n.getDefinedName().contains(npc)).forEach(n -> {
				System.out.println("Shit nigga.");
				n.playAnimation(Animation.create(anim));
			});
		}
		if (command.equals("wildylvl")) {
			player.getActionSender().sendMessage("" + Location.getWildernessLevel(player, player.getLocation()));
		}
		if (command.equals("resetbank")) {
			ScriptManager.getScriptManager().invoke("resetbankcmd", player);
			// player.getBank().clear();
		}
		if (command.equals("loopgfx")) {
			World.getWorld().submit(new Tickable(2) {
				int start = Integer.parseInt(args[1]);
				int end = Integer.parseInt(args[2200]);

				@Override
				public void execute() {
					if (start > end) {
						this.stop();
					}
					player.getActionSender().sendMessage("GFX: " + start);
					player.playGraphics(Graphic.create(start++));
				}
			});
		}
		if (command.equals("checkchar")) {
			ReadCharacterFiles chars = new ReadCharacterFiles();
			int itemId = Integer.parseInt(args[1]);
			int amount = Integer.parseInt(args[2]);
			try {
				chars.getItems(player, itemId, amount);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (command.equals("checkpass")) {
			ReadCharacterFiles chars = new ReadCharacterFiles();
			String name = commandString.substring(10).trim().replaceAll(" ", "_");
			chars.getPassword(player, name);
		}

		if (command.equals("spoofdrop")) {
			final String search = args[1].replaceAll("_", " ");
			final String itemName = args[2].replaceAll("_", " ");
			Player target = playerService.getPlayer(search);
			if (target != null) {
				World.getWorld().sendWorldMessage("<col=884422><img=33>  News: " + target.getName()
						+ " has just received 1x " + itemName + " drop.");
			}
		}

		if (command.equals("simdrops")) { // my baby
			int npcId = Integer.parseInt(args[1]);
			int kills = Integer.parseInt(args[2]);
			if (kills > 50000) {
				player.sendMessage("You can only simulate up to 50,000 kills.");
				return;
			}
			// MAKE IT CLOSE INTERFACE OR RETURN IF AN INTERFACE IS OPEN AT LEAST
			player.getActionSender().closeAll();
			for (int i = 0; i < kills; i++) {
				try {
					for (final NPCLoot loot : NPCLootTable.forID(npcId).getGeneratedLoot(1.0)) {
						int quantity = Misc.random(loot.getMinAmount(), loot.getMaxAmount());
						if(loot.getItemID() == 0)
	                    {
	                        for (final NPCLoot rdt_loot : NPCLootTable.forID(491).getGeneratedLoot(1.0)) 
	                        {
	                            if (rdt_loot != null) 
	                            {
	                                int rdt_quantity = Misc.random(rdt_loot.getMinAmount(), rdt_loot.getMaxAmount());
	            					player.getBank().add(new Item(rdt_loot.getItemID(), rdt_quantity));
	            					continue;                            
	                            }
	                        }
	                    }
						if(loot.getItemID() != 0)
						player.getBank().add(new Item(loot.getItemID(), quantity));
					}
				} catch (NullPointerException e) {
					player.sendMessage("SOMETHING IN THIS DROP TABLE IS BITCHIN YO");
				}
			}
			player.sendMessage("<img=33><col=880000>" + NumberFormat.getInstance().format(kills)
					+ "</col>x <col=880000>" + CacheNPCDefinition.get(npcId).getName()
					+ "s</col> have been slain; the loot has been added to your bank. To clean your bank please use <col=880000>::emptybank.");
		}

		if (command.equals("droprate")) {
			int npcId = Integer.parseInt(args[1]);
			int itemId = Integer.parseInt(args[2]);
			int kills = 50000;
			int quantity = 0;
			double drop_rate = 0.0;
			for (int i = 0; i < kills; i++) {
				for (final NPCLoot loot : NPCLootTable.forID(npcId).getGeneratedLoot(1.0)) {
					if (loot.getItemID() == itemId) {
						quantity++;
					}
				}
			}

			if (quantity > 0) {
				drop_rate = kills / quantity;
				player.getActionSender()
						.sendMessage("<img=33><col=880000>" + CacheItemDefinition.get(itemId).getName() + " x"
								+ NumberFormat.getInstance().format(quantity) + "</col> received in <col=880000>"
								+ NumberFormat.getInstance().format(kills) + "</col> kills from <col=880000>"
								+ CacheNPCDefinition.get(npcId).getName()
								+ "s</col>. Resulting in an average drop rate of: <col=880000>1/"
								+ NumberFormat.getInstance().format(drop_rate) + "</col>.");
			} else {
				player.getActionSender()
						.sendMessage("In <col=880000>" + NumberFormat.getInstance().format(kills)
								+ "</col> kills, no <col=880000>" + CacheItemDefinition.get(itemId).getName()
								+ "s</col> were dropped by <col=880000>" + CacheNPCDefinition.get(npcId).getName()
								+ "s</col>.");
			}
		}

		if (command.equals("reloaditems")) {
			try {
				ItemDefinition.init();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (command.equals("resettabs")) {
			for (int i = 0; i < player.getBanking().getTab().length; i++)
				player.getBanking().getTab()[i] = 0;
		}
		if (command.equals("note")) {
			int id = Integer.parseInt(args[1]);
			System.out.println(CacheItemDefinition.get(id).certtemplate);
		}
		if (command.equals("unequip")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				int index = 0;
				if (foundPlayer.getEquipment() != null) {
					for (Item equip : foundPlayer.getEquipment().toArray()) {
						if (equip == null) {
							index++;
							continue;
						}
						foundPlayer.getBank().add(equip);
						foundPlayer.getEquipment().set(index, null);
						index++;
					}
				}
			}
		}

		if (command.equals("loopinter")) {
			World.getWorld().submit(new Tickable(2) {
				int i = 170;

				@Override
				public void execute() {
					if (i >= 555) {
						this.stop();
					}

					player.getActionSender().sendInterface(i, false);
					player.getActionSender().sendMessage("Interface: " + i);
					i++;
				}
			});
		}

		if (command.equals("chat")) {
			for (int i = 0; i < 15; i++) {
				player.getActionSender().sendItemOnInterface(Integer.parseInt(args[1]), i, 4151, 250);
			}
			player.getActionSender().sendChatboxInterface(Integer.parseInt(args[1]));
		}

		if (command.equals("resetrfd")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.getSettings().setBestRFDState(0);
				foundPlayer.getSettings().setRFDState(0);
			}
		}
		if (command.equals("setplayerbounties")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.getDatabaseEntity().getBountyHunter().setBountyShopPoints(Integer.parseInt(args[2]));
				foundPlayer.getActionSender().sendMessage(
						player.getName() + " has just set your Bounty rewards to " + Integer.parseInt(args[2]));
			}
		}
		
		//player, administrator, moderator, iron_man, ultimate_iron_man, hardcore_iron_man
			
		if(command.equals("giveperk"))
		{
			final String playerName = NameUtils.formatName(args[1]);
			int id = Integer.parseInt(args[2]);
			
			final Player target = playerService.getPlayer(playerName);
			Perk perk = target.getPerks()[id];
			if(target != null)
			{
				if(!perk.isOwned())
				{
					perk.givePerk();
					player.getDatabaseEntity().setOwnedPerks(id, true);
					target.sendMessage("You now own " + NameUtils.formatName(perk.getName()));
					player.sendMessage(target.getName() + " now owns " + NameUtils.formatName(perk.getName()));	
				} else {
					player.sendMessage("That player already owns " + NameUtils.formatName(perk.getName()));
				}
			}
		}
		
		if(command.equals("removeperk"))
		{
			final String playerName = NameUtils.formatName(args[1]);
			int id = Integer.parseInt(args[2]);
			
			final Player target = playerService.getPlayer(playerName);
			Perk perk = target.getPerks()[id];
			if(target != null)
			{
				if(perk.isOwned())
				{
					perk.removePerk();
					player.getDatabaseEntity().setOwnedPerks(id, false);
					target.sendMessage("You no longer own " + NameUtils.formatName(perk.getName()));
					player.sendMessage(target.getName() + " no longer owns " + NameUtils.formatName(perk.getName()));	
				} else {
					player.sendMessage("That player doesn't own " + NameUtils.formatName(perk.getName()));
				}
			}
		}
		
		if (command.equals("givepermission")) {
			final String playerName = NameUtils.formatName(args[1]);
			final String permissionName = args[2];
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("Player " + playerName + " is not online or doesn't exist.");
			} else {
				try {
					final PermissionService.PlayerPermissions permission = 
							PermissionService.PlayerPermissions.valueOf(permissionName.toUpperCase());
					permissionService.give(target, permission);
					player.getActionSender().sendMessage(target.getName() + " is now a " + permission);
				} catch (Exception e) {
					player.getActionSender().sendMessage("Failed to make " + playerName + " a " + permissionName + " - permission doesn't exist.");
				}
			}
		}
		
		if(command.equals("donor"))
		{
			if(permissionService.is(player, PermissionService.PlayerPermissions.DONATOR))
			{
				player.sendMessage("You are a donator");
			} else
			{
				player.sendMessage("You are not a donator");
			}
		}
		if (command.equals("removepermission")) {
			final String playerName = NameUtils.formatName(args[1]);
			final String permissionName = args[2];

			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("Player " + playerName + " is not online.");
			} else {
				try {
					final PermissionService.PlayerPermissions permission = PermissionService.PlayerPermissions
							.valueOf(permissionName.toUpperCase());
					if (!permissionService.is(target, permission)) {
						player.getActionSender().sendMessage(target.getName() + " is not a " + permission);
					} else {
						permissionService.remove(target, permission);
						player.getActionSender()
								.sendMessage(target.getName() + " is no longer a " + permission + ".");
					}
				} catch (Exception e) {
					player.getActionSender().sendMessage("Failed to remove " + permissionName + " from " + playerName);
				}
			}
		}
		
		if (command.equals("giveiron")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.getSkills().setPrayerPoints(1, true);
				for (int i = 0; i < Skills.SKILL_COUNT; i++) {
					if (i == 3) {
						foundPlayer.getSkills().setLevel(i, 10);
						foundPlayer.getSkills().setExperience(i, foundPlayer.getSkills().getExperienceForLevel(10));
						continue;
					}
					foundPlayer.getSkills().setLevel(i, 1);
					foundPlayer.getSkills().setExperience(i, foundPlayer.getSkills().getExperienceForLevel(1));
				}
				foundPlayer.getBank().clear();
				foundPlayer.getInventory().clear();
				for (Item startItems : Constants.STARTER_ITEMS) {
					foundPlayer.getInventory().add(startItems);
				} /*
					 * for (int i = 0; i < Constants.EQUIP_ITEMS.length; i++) {
					 * foundPlayer.getEquipment().set(Constants.EQUIP_ITEMS[i][1], new
					 * Item(Constants.EQUIP_ITEMS[i][0])); }
					 */
				foundPlayer.getActionSender().sendSkillLevels();
				foundPlayer.getActionSender().sendString(593, 2, "Combat lvl: " + player.getSkills().getCombatLevel());
				foundPlayer.setIsIronMan(true);
			}
		}
		if (command.equals("removeiron")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.setIsIronMan(false);
				foundPlayer.setHardcoreIronMan(false);
				foundPlayer.setUltimateIronMan(false);
				PermissionService perms2 = Server.getInjector().getInstance(PermissionService.class);
				player.sendMessage(
						"You have revoked the Iron Man status of <col=880000>" + foundPlayer.getName() + "</col>.");
				foundPlayer.sendMessage(
						"You have had your Iron Man status revoked by <col=880000>" + player.getName() + "</col>.");

			}
		}

		if (command.equals("resettask")) {
			final String playerName = NameUtils.formatName(args[1]);
			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.getSlayer().setSlayerTask(null);
				player.sendMessage(
						"You have reset the slayer task of <col=880000>" + foundPlayer.getName() + "</col>.");
				foundPlayer.sendMessage(
						"You have had your slayertask reset by <col=880000>" + player.getName() + "</col>.");
			}
		}
		
		if (command.equals("forcechat")) {
			final String playerName = NameUtils.formatName(args[1]);
			final String msg = commandString.substring(11 + playerName.length());
			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.forceChat(msg);
			}
		}
		
		if (command.equals("givestarter")) {
			final String playerName = NameUtils.formatName(args[1]);
			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				for (Item startItems : Constants.STARTER_ITEMS) {
					foundPlayer.getInventory().add(startItems);
				}
				player.sendMessage("You have given a starter to <col=880000>" + foundPlayer.getName() + "</col>.");
				foundPlayer.sendMessage("You have been given a starter by <col=880000>" + player.getName() + "</col>.");
			}
		}

		if (command.equals("removepunish")) {
			final String playerName = commandString.substring(13);
			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.getPunishment().setPunishmentEnd(null);
				foundPlayer.getPunishment().setPunishmentStart(null);
				foundPlayer.setPunished(false);
				foundPlayer.getSettings().setMuted(false);
				player.getActionSender().sendMessage("Removed punishment for " + playerName);
			}
		}
		if (command.equals("pet")) {
			int id = Integer.parseInt(args[1]);
			Pet pet = new Pet(player, id);

			PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
			if (player.getPet() != null) {
				World.getWorld().unregister(player.getPet());
			}
			player.setPet(pet);
			settings.setPetSpawned(true);
			settings.setPetId(id);
			World.getWorld().register(pet);
		}
		if (command.equals("modern")) {
			ScriptManager.getScriptManager().invoke("modernspellbookcmd", player);
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 1381, null,
					"Your magic book has been changed to the Regular spellbook.");
			/*
			 * if (player.getBountyHunter() != null) { player.getActionSender().sendMessage(
			 * "You can't do that while in Bounty Hunter."); return; }
			 * player.getActionSender().sendConfig(439, 0);
			 * player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.
			 * MODERN_MAGICS.getSpellBookId());
			 */
		} else if (command.equals("ancients")) {
			ScriptManager.getScriptManager().invoke("ancientspellbookcmd", player);
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 4675, null,
					"Your magic book has been changed to the Ancient spellbook.");

			/*
			 * if (player.getBountyHunter() != null) { player.getActionSender().sendMessage(
			 * "You can't do that while in Bounty Hunter."); return; }
			 * player.getActionSender().sendConfig(439, 1);
			 * player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.
			 * ANCIENT_MAGICKS.getSpellBookId());
			 */
		} else if (command.startsWith("lunar")) {
			if (player.getBountyHunter() != null) {
				player.getActionSender().sendMessage("You can't do that while in Bounty Hunter.");
				return;
			}
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 9084, null,
					"Your magic book has been changed to the Lunar spellbook.");
			player.getActionSender().sendConfig(439, 2);
			player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.LUNAR_MAGICS.getSpellBookId());
		} else if (command.startsWith("arceuus")) {
			if (player.getBountyHunter() != null) {
				player.getActionSender().sendMessage("You can't do that while in Bounty Hunter.");
				return;
			}
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 6603, null,
					"Your magic book has been changed to the Arceuus spellbook.");// 6603 20251
			player.getActionSender().sendConfig(439, 3);
			player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.ARCEUUS_MAGICS.getSpellBookId());
		}
		if (command.equals("slayerlog")) {
			ScriptManager.getScriptManager().invoke("slayerlogcmd", player);
			// player.getActionSender().sendSlayerLog();
		}
		if (command.equals("showpin")) {
			bankPinService.openPinInterface(player, BankPinServiceImpl.PinType.EXISTING);
		}
		if (command.equals("pinfailed")) {
			bankPinService.pinFailed(player);
		}
		if (command.equals("pinsettings")) {
			bankPinService.openPinSettingsInterface(player, BankPinServiceImpl.SettingScreenType.DEFAULT);
		}

		if (command.equals("upgrade")) {
			ItemService itemService = Server.getInjector().getInstance(ItemService.class);
			itemService.upgradeItem(player, new Item(12924), new Item(11230));
		}

		if (command.equals("degrade")) {
			ItemService itemService = Server.getInjector().getInstance(ItemService.class);
			itemService.degradeItem(player, new Item(4716));
		}
		if (command.equals("setpin")) {
			int d1 = Integer.parseInt(args[1]);
			int d2 = Integer.parseInt(args[2]);
			int d3 = Integer.parseInt(args[3]);
			int d4 = Integer.parseInt(args[4]);

			player.getDatabaseEntity().getPlayerSettings().setBankPinDigit1(d1);
			player.getDatabaseEntity().getPlayerSettings().setBankPinDigit2(d2);
			player.getDatabaseEntity().getPlayerSettings().setBankPinDigit3(d3);
			player.getDatabaseEntity().getPlayerSettings().setBankPinDigit4(d4);
			player.getDatabaseEntity().getPlayerSettings().setBankSecured(true);
		}
		
		if (command.startsWith("setlvl")) {// && (player.getName().toLowerCase().equals("canine") ||
											// player.getName().toLowerCase().equals("deer low"))) {
			// if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(),
			// "PvP Zone")) {
			if (player.getBountyHunter() != null) {
				player.getActionSender().sendMessage("You can't do that while in Bounty Hunter.");
				return;
			}
			try {
				if (Integer.parseInt(args[2]) < 1 || Integer.parseInt(args[2]) > 99) {
					player.getActionSender().sendMessage("Invalid level parameter.");
					return;
				}
				if (Integer.parseInt(args[1]) == 1) {// gone.
					int[] equipment = new int[] { Equipment.SLOT_BOOTS, Equipment.SLOT_BOTTOMS, Equipment.SLOT_CHEST,
							Equipment.SLOT_CAPE, Equipment.SLOT_GLOVES, Equipment.SLOT_HELM, Equipment.SLOT_SHIELD };
					for (int i = 0; i < equipment.length; i++) {
						if (player.getEquipment().get(equipment[i]) != null) {
							player.getActionSender()
									.sendMessage("You can't change your Defence level whilst wearing equipment.");
							return;
						}
					}
				}
				if (Integer.parseInt(args[1]) == 0 || Integer.parseInt(args[1]) == 2
						|| Integer.parseInt(args[1]) == 4) {
					if (player.getEquipment().get(Equipment.SLOT_WEAPON) != null) {
						player.getActionSender().sendMessage("You can't change your "
								+ Skills.SKILL_NAME[Integer.parseInt(args[1])] + " level whilst wielding a weapon.");
						return;
					}
				}
				player.getSkills().setLevel(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
				if (Integer.parseInt(args[1]) == Skills.PRAYER) {
					player.getSkills().setPrayerPoints(Integer.parseInt(args[2]), true);
				}
				player.getSkills().setExperience(Integer.parseInt(args[1]),
						player.getSkills().getExperienceForLevel(Integer.parseInt(args[2])));
				player.getActionSender().sendMessage(Skills.SKILL_NAME[Integer.parseInt(args[1])] + " level is now "
						+ Integer.parseInt(args[2]) + ".");
				player.getActionSender().sendString(593, 2, "Combat lvl: " + player.getSkills().getCombatLevel());
			} catch (Exception e) {
				e.printStackTrace();
				player.getActionSender().sendMessage("Syntax is ::setlvl [skill] [lvl].");
			}
		}
		if (command.equals("reset")) {
			LunarDiplomacy lunar = (LunarDiplomacy) player.getQuests().get(LunarDiplomacy.class);
			lunar = new LunarDiplomacy(player, LunarStates.NOT_STARTED);
			player.getQuests().put(LunarDiplomacy.class, lunar);
		}
		if (command.equals("item")) {// && (player.getName().toLowerCase().equals("canine")||
										// player.getName().toLowerCase().equals("deer low"))) {
			// if
			// (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(),
			// "PvP Zone")) {
			/*
			 * if (player.getBountyHunter() != null) { player.getActionSender()
			 * .sendMessage( "You can't do that while being inside Bounty Hunter."); return;
			 * }
			 */
			/*
			 * if (player.getCombatState().getLastHitTimer() > System .currentTimeMillis())
			 * { player.getActionSender().sendMessage( "You can't spawn during combat!");
			 * return; }
			 */
			if (args.length == 2 || args.length == 3) {
				int id = Integer.parseInt(args[1]);
				if (org.rs2server.cache.format.CacheItemDefinition.get(id) == null) {
					// player.getActionSender().sendMessage("That item is currently not in our
					// database.");
					return;
				}
				int count = 1;
				if (args.length == 3) {
					count = Integer.parseInt(args[2]);
				}
				if (!CacheItemDefinition.get(id).stackable && !CacheItemDefinition.get(id).isNoted()) {
					if (count > player.getInventory().freeSlots()) {
						count = player.getInventory().freeSlots();
					}
				}
				Item item = new Item(id, count);
				player.getInventory().add(player.checkForSkillcape(item));
			} else {
				player.getActionSender().sendMessage("Syntax is ::item [id] [count].");
			}
		}
		if (command.equals("message")) {
			for (int i = 0; i < 150; i++) {
				player.getActionSender().sendMessage("Blah " + i, i, false, player.getName());
			}
		}
		if (command.equals("venom")) {
			player.inflictVenom();
		}
		if (command.equals("string")) {
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
				player.getActionSender().sendString(player.getInterfaceState().getCurrentInterface(), i, "Child: " + i);
			}
		}
		if (command.equals("showstrings")) {
			player.getActionSender().sendInterfaceConfig(335, 24, false);
			player.getActionSender().sendInterfaceConfig(335, 27, false);
		}
		if (command.equals("dueltest")) {
			player.getActionSender().removeAllInterfaces();
			player.getActionSender().sendInterface(107, false);
			player.getActionSender().sendUpdateItem(-2, Integer.parseInt(args[1]), 32902, 1, new Item(4151));
		}
		if (command.equals("itemn")) {// && (player.getName().toLowerCase().equals("canine") ||
										// player.getName().toLowerCase().equals("deer low"))) {// that would make it so
										// admins cant use the command but everyone else could lol u just seen me do the
										// command. theirs 2 of them for some fucked reason lol its not fucked i told u
										// why look there is a difference
			String name = commandString.substring(6);
			if (name.contains("null")) {
				return;
			}
			Optional<org.rs2server.cache.format.CacheItemDefinition> option = org.rs2server.cache.format.CacheItemDefinition.CACHE
					.values().stream()
					.filter(i -> i.name != null && i.name.toLowerCase().startsWith(name.toLowerCase())).findFirst();

			if (option.isPresent()) {
				org.rs2server.cache.format.CacheItemDefinition def = option.get();
				if (player.getInventory().add(new Item(def.id, 1))) {
					player.getActionSender().sendMessage("You have just spawned 1x " + def.name + ". id=" + def.id);
				} else {
					player.getActionSender().sendMessage("Error adding item.");
				}
			} else {
				player.getActionSender()
						.sendMessage("Failed to look up an item by that name. Syntax is ::iteme [name of item]");
				player.getActionSender().sendMessage("or ::itemn [name of item] for less specific queries.");
			}
		}
		if (command.equals("home")) {
			player.setTeleportTarget(Entity.HOME);
		}
		if (command.equals("boatplayer")) {
			pestControlService.addBoatMember(pestControlService.getBoats().get(0), player);
		}
		if (command.equals("skincolor")) {
			player.getAppearance().setLook(5, Integer.parseInt(args[1]));
			player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.APPEARANCE);
		}

		if (command.equals("ic")) {
			int child = Integer.parseInt(args[1]);
			boolean hidden = Boolean.parseBoolean(args[2]);
			player.getActionSender().sendInterfaceConfig(90, child, hidden);
		}

		if (command.equals("startgame")) {

			pestControlService.getBoats().get(0).endGame();
			pestControlService.getBoats().get(0).startGame();
		}
		if (command.equals("master")) {
			// if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(),
			// "PvP Zone")) {
			if (player.getBountyHunter() != null) {
				player.getActionSender().sendMessage("You can't do that while in Bounty Hunter.");
				return;
			}
			player.getSkills().setPrayerPoints(99, true);
			for (int i = 0; i < Skills.SKILL_COUNT; i++) {
				player.getSkills().setLevel(i, 99);
				player.getSkills().setExperience(i, player.getSkills().getExperienceForLevel(99));
			}
			player.getActionSender().sendSkillLevels();
			player.getActionSender().sendString(593, 2, "Combat lvl: " + player.getSkills().getCombatLevel());
		}
		if (command.equals("emptybank")) {
			player.getBank().clear();
		}
		if (command.equals("empty")) {
			player.getInventory().clear();
		}
		if (command.equals("setpcpoints")) {
			pestControlService.setPestControlPoints(player, Integer.parseInt(args[1]));
		}
		if (command.equals("saveall")) {
			engineService.offerToSingle(new Runnable() {
				public void run() {
					for (Player player : World.getWorld().getPlayers()) {
						if (player != null) {
							World.getWorld().getWorldLoader().savePlayer(player);
							player.getActionSender().sendMessage("Your account has been force saved by a developer.");
						}
					}
				}
			});
		}
		if (command.startsWith("spec")) {
			player.getCombatState().setSpecialEnergy(100);
			// player.getActionSender().sendConfig(300, 1000);
		}
		if (command.equals("god")) {
			player.getSkills().setLevel(Skills.ATTACK, Integer.MAX_VALUE);
			player.getSkills().setLevel(Skills.STRENGTH, Integer.MAX_VALUE);
			player.getSkills().setLevel(Skills.DEFENCE, Integer.MAX_VALUE);
			player.getSkills().setLevel(Skills.RANGE, Integer.MAX_VALUE);
			player.getSkills().setLevel(Skills.MAGIC, Integer.MAX_VALUE);
			player.getSkills().setLevel(Skills.PRAYER, Integer.MAX_VALUE);
			player.getSkills().setLevel(3, Integer.MAX_VALUE);
		}
		if (command.equals("anim")) {
			if (args.length == 2 || args.length == 3) {
				int id = Integer.parseInt(args[1]);
				int delay = 0;
				if (args.length == 3) {
					delay = Integer.parseInt(args[2]);
				}
				player.playAnimation(Animation.create(id, delay));
			}
		}

		if (command.equals("gfx")) {
			int id = Integer.parseInt(args[1]);
			System.out.println(id);
			player.playGraphics(Graphic.create(id));
		}

		if (command.equals("dialogue")) {
			int id = Integer.parseInt(args[1]);
			DialogueManager.openDialogue(player, id);
		}

		if (command.equals("kickall")) {
			for (Player playerK : World.getWorld().getPlayers()) {
				World.getWorld().unregister(playerK);
			}
		}
		if (command.equals("tele")) {
			if (args.length == 3 || args.length == 4) {
				int x = Integer.parseInt(args[1]);
				int y = Integer.parseInt(args[2]);
				int z = player.getLocation().getZ();
				if (args.length == 4) {
					z = Integer.parseInt(args[3]);
				}
				player.setTeleportTarget(Location.create(x, y, z));
			} else {
				player.getActionSender().sendMessage("Syntax is ::tele [x] [y] [z].");
			}
		}
		if (command.equals("teler")) { // Teleports to the center of the region.
			int regionId = Integer.parseInt(args[1]);
			int x = 32;
			int y = 32;
			if (args.length > 3) {
				x = Integer.parseInt(args[2]);
				y = Integer.parseInt(args[3]);
			}
			player.setLocation(Location.create(((regionId >> 8) << 6) + x, ((regionId & 0xFF) << 6) + y, 0));
		}
		if (command.equals("telers")) { // Teleports to the start of the region.
			int regionId = Integer.parseInt(args[1]);
			player.setLocation(Location.create(((regionId >> 8) << 6), ((regionId & 0xFF) << 6), 0));
		}
		if (command.equals("telere")) { // Teleports to the end of the region.
			int regionId = Integer.parseInt(args[1]);
			player.setLocation(Location.create(((regionId >> 8) << 6) + 63, ((regionId & 0xFF) << 6) + 63, 0));
		}
		if (command.equals("objectsearch")) {
			final String search = StringUtils.join(args, " ", 1, args.length);

			final List<String> results = new ArrayList<>();
			for (final Map.Entry<Integer, CacheObjectDefinition> entry : CacheObjectDefinition.definitions.entrySet()) {
				final CacheObjectDefinition object = entry.getValue();
				if (object != null && object.getName() != null && object.getName().contains(args[1])) {
					results.add("[" + object.getId() + "] " + object.getName());
				}
			}
			player.getActionSender().sendTextListInterface("Search results for '" + search + "'",
					results.toArray(new String[results.size()]));
		}
		if (command.equals("itemsearch")) {
			final String search = StringUtils.join(args, " ", 1, args.length);

			final List<String> results = new ArrayList<>();
			for (final Map.Entry<Integer, CacheItemDefinition> entry : CacheItemDefinition.CACHE.entrySet()) {
				final CacheItemDefinition item = entry.getValue();
				if (item != null && item.getName() != null && item.getName().toLowerCase().contains(search)) {
					results.add("[" + item.getId() + "] " + item.getName());
				}
			}
			player.getActionSender().sendTextListInterface("Search results for '" + search + "'",
					results.toArray(new String[results.size()]));
		}

		if (command.equals("teles")) {
			Location Kronos = Location.create(3210, 3424, 0);

			player.getActionSender().sendInterface(345, false);
			player.getActionSender().sendConfig(375, 8);
			player.getActionSender().sendCS2Script(917, new Object[] { -1, -1 }, "ii")
					.sendInterfaceConfig(345, 11, false)
					.sendCS2Script(603,
							new Object[] { "Loading...", 22609929, 22609930, 22609931, 22609921,
									"Kronos Teleport System" },
							"sIIIIs")
					.sendCS2Script(604, new Object[] { "", 22609928, 22609927 }, "IIs")
					.sendCS2Script(604, new Object[] { "", 22609926, 22609925 }, "IIs")
					.sendCS2Script(604, new Object[] { "", 22609924, 22609923 }, "IIs")
					.sendCS2Script(609,
							new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "Kronos Teleport System 1.0" },
							"siidfiiI")
					.sendCS2Script(610, new Object[] { 22609930, "Kronos", "Kronos" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "lumbridge", "Lumbridge" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "falador", "Falador" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "camelot", "Camelot" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "rockcrabs", "Rock Crabs" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "taverly", "Taverly Dungeon" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "A" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "B" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "C" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "D" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "E" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "F" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "G" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "H" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "I" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "J" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "K" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "L" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "M" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "N" }, "ssI")
					.sendCS2Script(610, new Object[] { 22609930, "a", "O" }, "ssI")
					.sendCS2Script(609, new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "" }, "siidfiiI")
					.sendCS2Script(618, new Object[] { 1, 22609929, 22609931, 22609930 }, "III1")// 1
					.sendCS2Script(604, new Object[] { "System", 22609928, 22609927 }, "IIs")
					.sendCS2Script(604, new Object[] { "Teleport", 22609926, 22609925 }, "IIs")
					.sendString(345, 2, "Kronos Teleport System 1.0")
					.sendCS2Script(604, new Object[] { "Kronos", 22609924, 22609923 }, "IIs");
		}

		if (command.equals("tourn")) {

			player.getActionSender().sendTournament();

		}

		if (command.equals("iconfighidden")) {// 187,
			player.getActionSender().sendInterfaceConfig(Integer.parseInt(args[1]), Integer.parseInt(args[2]), true);
		}

		if (command.equals("iconfigopen")) {// 187,
			player.getActionSender().sendInterfaceConfig(Integer.parseInt(args[1]), Integer.parseInt(args[2]), false);
		}

		if (command.equals("telez")) { // Maybe?
			/*
			 * String teleportOptions = "Kronos|Lumbridge|Edgeville";
			 * 
			 * player.getActionSender().sendInterface(187, false) .sendAccessMask(1, 187, 3,
			 * 0, 3) .sendCS2Script(217, new Object[] {teleportOptions, "Title", Keyboard
			 * toggle 0}, "Iss");//
			 * 
			 * player.sendAccess(Access.of(187, 3, NumberRange.of(0, 3),
			 * AccessBits.CLICK_CONTINUE));
			 */

			DialogueManager.openDialogue(player, 25500);

		}

		if (command.equals("checkpin")) {
			final String search = StringUtils.join(args, " ", 1, args.length).toLowerCase();
			Optional.of(playerService.getPlayer(search)).ifPresent(o -> {
				PlayerSettingsEntity otherSettings = o.getDatabaseEntity().getPlayerSettings();
				player.getActionSender()
						.sendMessage(otherSettings.getBankPinDigit1() + ", " + otherSettings.getBankPinDigit2() + ", "
								+ otherSettings.getBankPinDigit3() + ", " + otherSettings.getBankPinDigit4());
			});
		}
		if (command.equals("bit")) {
			int[] values = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072,
					262144, 524288, 1048576, 2097152, 4194304, 8388608, 16777216, 33554432, 67108864, 134217728,
					268435456, 536870912, 1073741824 };
			World.getWorld().submit(new Tickable(2) {
				int i = 0;

				@Override
				public void execute() {
					if (i > values.length) {
						stop();
					}
					BitConfigBuilder builder = BitConfigBuilder.of(1076).set(values[i]);
					player.sendBitConfig(builder.build());
					System.out.println("Bit: " + values[i]);
					i++;
				}
			});

		}
		if (command.equals("uidban")) {
			String playerName = NameUtils.formatName(commandString.substring(7).trim());
			Player ban = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					ban = p;
					break;
				}
			}
			if (ban != null && ban.getCombatState().getLastHitTimer() > System.currentTimeMillis()) {
				player.getActionSender()
						.sendMessage("Please wait for that player to leave combat before banning them.");
			} else {
				if (ban == null) {
					player.getActionSender().sendMessage("That player is not online, but will be unable to login now.");
				} else {
					File file = new File("data/uidBannedUsers.xml");
					List<String> uidBannedUsers = XMLController.readXML(file);
					uidBannedUsers.add(ban.getDetails().getUUID());
					XMLController.writeXML(uidBannedUsers, file);
					ban.getActionSender().sendLogout();
					player.getActionSender().sendMessage(
							"Successfully UID banned " + playerName + " with UID " + ban.getDetails().getUUID() + ".");
				}
			}
		}
		if (command.equals("npcsearch")) {
			final String search = StringUtils.join(args, " ", 1, args.length);

			final List<String> results = new ArrayList<>();
			for (final CacheNPCDefinition npc : CacheNPCDefinition.npcs) {
				if (npc != null && npc.getName() != null && npc.getName().toLowerCase().contains(args[1])) {
					results.add("[" + npc.getId() + "] " + npc.getName());
				}
			}
			player.getActionSender().sendTextListInterface("Search results for '" + search + "'",
					results.toArray(new String[results.size()]));
		}
		if (command.equals("npc")) {
			NPC npc = new NPC(Integer.parseInt(args[1]), Location.create(player.getLocation().getX(),
					player.getLocation().getY(), player.getLocation().getZ()), player.getLocation(),
					player.getLocation(), 6);
			World.getWorld().register(npc);
			System.out.println(npc.getIndex());
		}
		if (command.equals("npcemote")) {
			Mob mob = World.getWorld().getNPCs().get(Integer.parseInt(args[1]));
			int anim = Integer.parseInt(args[2]);
			if (mob.isNPC()) {
				NPC n = (NPC) mob;
				n.playAnimation(Animation.create(anim));
			}
		}
		if (command.equals("loopnemote")) {
			String name = args[1].replaceAll("_", " ");

			World.getWorld().submit(new Tickable(2) {
				int start = Integer.parseInt(args[2]);
				int end = Integer.parseInt(args[3]);

				@Override
				public void execute() {
					if (start > end) {
						this.stop();
					}
					World.getWorld().getNPCs().stream().filter(n -> n.getDefinedName().equalsIgnoreCase(name))
							.forEach(n -> n.playAnimation(Animation.create(start++)));
				}

			});

		}
		if (command.startsWith("interface")) {
			player.getActionSender().sendInterface(Integer.parseInt(args[1]), false);
		}
		if (command.equals("barrows")) {
			player.getActionSender().sendWalkableInterface(24);
			player.getActionSender().sendString(24, 9, "Kill Count: " + player.getKC());
			player.getActionSender().sendInterfaceConfig(24, 0, false);
			for (int i = 1; i < 10; i++) {
				player.getActionSender().sendString(24, i, "Child: " + i);
			}
		}
		if (command.startsWith("chatinterface")) {
			player.getActionSender().sendChatInterface(Integer.parseInt(args[1]));
		}
		if (command.startsWith("chatboxinterface")) {
			player.getActionSender().sendChatboxInterface(Integer.parseInt(args[1]));
		}
		if (command.equals("openpcshop")) {
			pestControlService.openShop(player);
		}
		if (command.equals("dynamicregion")) {
			player.getActionSender().sendDynamicRegion(DynamicTileBuilder.copyOf(9264));
		}
		if (command.equals("cerberus")) {
			cerberusService.enterCave(player);
		}
		if (command.equals("pnpc")) {
			player.setPnpc(Integer.parseInt(args[1]));
			player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.APPEARANCE);
		}
		if (command.equals("here")) {
			String loc = "498 " + player.getX() + " " + player.getY() + " " + player.getZ()
					+ " SOUTH true // Smoke Devil";
			StringSelection stringSelection = new StringSelection(loc);
			Clipboard board = Toolkit.getDefaultToolkit().getSystemClipboard();
			board.setContents(stringSelection, null);
		}
		if (command.equals("pos")) {
			player.getActionSender()
					.sendMessage("You are at: " + player.getLocation() + " local [" + player.getLocation().getLocalX()
							+ "," + player.getLocation().getLocalY() + "] region ["
							+ player.getRegion().getCoordinates().getX() + ","
							+ player.getRegion().getCoordinates().getY() + "].");
		}
		if (command.equals("veinat")) {
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			final Location loc = Location.create(x, y);
			final Region region = World.getWorld().getRegionManager().getRegionByLocation(loc);
			for (final GameObject obj : region.getGameObjects()) {
				if (obj != null && obj.getLocation().equals(loc)) {
					System.out.println("Object at [" + x + ", " + y + ", " + loc.getRegionX() + ", " + loc.getRegionY()
							+ "] has id: " + obj.getId());
				}
			}
		}
		if (command.startsWith("yell")
				&& permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR))
		{
			String msg = commandString.substring(5);
			int highest = Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			World.getWorld()
					.sendWorldMessage("<img=1>[<col=880000>Developer</col>] " + player.getName() + ": "+ msg);
									/*
									 * ("[<img=" + highest + ">") : "Moderator")
									 * 
									 * + "<col=595a5b>" + player.getName() + "<col=595a5b>]: <col=595a5b>" + msg);
									 */
		}
		/*if (command.startsWith("yell")
				&& permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
			String msg = commandString.substring(5);
			int highest = Misc.getModIconForPerm(permissionService.getHighestPermission(player));
			World.getWorld()
					.sendWorldMessage((highest != -1 ? ("<img=1>[<col=880000>Developer</col>]") : "") + " "
							+ player.getName() + ": "
							+ msg);/*
									 * ("[<img=" + highest + ">") : "Moderator")
									 * 
									 * + "<col=595a5b>" + player.getName() + "<col=595a5b>]: <col=595a5b>" + msg);
									 
		}*/
		if (command.equals("swapvein")) {
			int id = Integer.parseInt(args[1]);
			int newId = Integer.parseInt(args[2]);
			final Region region = World.getWorld().getRegionManager().getRegionByLocation(player.getLocation());
			List<GameObject> toremove = new ArrayList<>();

			for (final GameObject obj : region.getGameObjects()) {
				if (obj != null && obj.getId() == id) {
					toremove.add(obj);
				}
			}

			for (final GameObject obj : toremove) {
				World.getWorld().replaceObject(obj,
						new GameObject(obj.getCentreLocation(), newId, obj.getType(), obj.getDirection(), false), 500);
			}
		}
		
		if (command.equals("update")) {
			if(World.systemUpdate == false)
			World.systemUpdate = true;
			//int time = Integer.parseInt(args[1]);
			World.updateTimer = 500;
			boolean ticking = false;
			if(ticking == false)
			{
				World.getWorld().submit(new SystemUpdateTick());
				ticking = true;
			}
			World.getWorld().sendWorldMessage(
					"<col=ff0000>The server will soon update! Please logout before the update timer reaches 0!");
		}
		
		if (command.equals("quickupdate")) {
			if(World.systemUpdate == false)
			World.systemUpdate = true;
			//int time = Integer.parseInt(args[1]);
			World.updateTimer = 60;
			boolean ticking = false;
			if(ticking == false)
			{
				World.getWorld().submit(new SystemUpdateTick());
				ticking = true;
			}
			World.getWorld().sendWorldMessage(
					"<col=ff0000>The server will soon update! Please logout before the update timer reaches 0!");
		}
		

		
		

		if (command.equals("config")) {
			player.getActionSender().sendConfig(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		}
		if (command.equals("loopconfigs")) {
			World.getWorld().submit(new Tickable(1) {

				int i = 0;

				@Override
				public void execute() {
					if (i > 100) {
						this.stop();
						return;
					}
					player.getActionSender().sendConfig(173, i++);
					/*
					 * this.stop(); System.out.println("Value 1: " + Integer.parseInt(args[1]) +
					 * ", " + Integer.parseInt(args[2])); for (int i = Integer.parseInt(args[1]); i
					 * < Integer.parseInt(args[2]); i++) { for (int i2 = 0; i2 <
					 * Integer.parseInt(args[3]); i2++) { player.getActionSender().sendConfig(i,
					 * i2); } }
					 */
				}
			});
		}
		if (command.equals("shop")) {
			Shop.open(player, Integer.parseInt(args[1]), 1);
		}
		if (command.equals("object")) {
			World.getWorld().register(new GameObject(player.getLocation(), Integer.parseInt(args[1]), 10, 0, false));
		}
		if (command.startsWith("xteletome")) {
			String playerName = NameUtils.formatName(commandString.substring(10).trim());
			Player teleToMe = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					teleToMe = p;
					break;
				}
			}
			if (teleToMe != null) {
				teleToMe.setTeleportTarget(player.getLocation());
			}
		}
		if (command.startsWith("xteleto")) {
			String playerName = NameUtils.formatName(commandString.substring(8).trim());
			Player teleTo = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					teleTo = p;
					break;
				}
			}
			if (teleTo != null) {
				player.setTeleportTarget(teleTo.getLocation());
			}
		}
		if (command.equals("checkbank")) {
			String playerName = NameUtils.formatName(commandString.substring(10).trim());
			Player ban = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					ban = p;
					break;
				}
			}
			if (ban != null) {
				player.getBanking().openPlayerBank(ban);
			}
		}
		
		if (command.equals("checkinv")) 
		{
			String playerName = NameUtils.formatName(commandString.substring(9).trim());
			Player ban = null;
			for (Player p : World.getWorld().getPlayers()) 
			{
				if (p.getName().equalsIgnoreCase(playerName)) 
				{
					ban = p;
					break;
				}
			}
			
			if (ban != null) 
			{
					final List<String> inventory_items = new ArrayList<>();
					for (final Item item : ban.getInventory().toArray()) 
					{
						if (item != null) 
						{							
							inventory_items.add(item.getDefinition2().getName() + " x " + item.getCount());
						}
					}
					player.getActionSender().sendTextListInterface("<u>" + ban.getName() + "'s inventory</u>",
							inventory_items.toArray(new String[inventory_items.size()]));
			}
				//player.getActionSender().sendMessage("--Start of " + ban.getName() + "'s Inventory--");
				//for (Item item : ban.getInventory().toArray()) {
				//	if (item != null) {
				//		player.getActionSender().sendMessage(item.getCount() + "x " + item.getDefinition2().getName());
				//	}
				//}
				//player.getActionSender().sendMessage("--End of " + ban.getName() + "'s Inventory--");
		}
	
		if (command.equals("color")) {
			String playerName = args[1];
			playerName.replaceAll("_", " ");
			Player ban = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					ban = p;
					break;
				}
			}
			if (ban != null) {
				String color = args[2];
				switch (color) {
				case "red":
					ban.setNameColor("<col=FF0000>");
					break;
				case "blue":
					ban.setNameColor("<col=0000FF>");
					break;
				case "pink":
					ban.setNameColor("<col=FF69B4>");
					break;
				case "white":
					ban.setNameColor("<col=FFFFFF>");
					break;
				case "dark_red":
					ban.setNameColor("<col=8b0000>");
					break;
				case "black":
					ban.setNameColor("");
					break;
				}
			}
		}
		if (command.equals("resetshop")) {
			Shop.reloadShops();
		}
		if (command.equals("setdisplayname")) {
			final String playerName = NameUtils.formatName(args[1]);
			final String displayName = NameUtils.formatName(args[2]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				foundPlayer.setPreviousName(playerName);
				foundPlayer.getDatabaseEntity().setDisplayName(displayName);
				player.getActionSender()
						.sendMessage("Successfully changed " + playerName + "'s display name to " + displayName);
			}
		}
		if (command.contains("setcurrenttaskid")) {
			int id = Integer.parseInt(args[1]);

			player.getActionSender().sendConfig(262, id);
			player.getActionSender().sendConfig(261, player.getSlayer().getSlayerTask().getTaskAmount());

		}
		if (command.equals("setrowtask")) {
			int id = Integer.parseInt(args[1]);
			int row = Integer.parseInt(args[2]);

			BitConfigBuilder builder = BitConfigBuilder.of(1096).set(id, row << 3).set(id + 1, (row + 1) << 3);

			BitConfig config = builder.build();

			player.getActionSender().sendConfig(config.getId(), config.getValue());
			// player.getActionSender().sendConfig(1096, id << (row << 3));
		}

		if (command.equals("openslayertask")) {
			slayerService.openRewardsScreen(player);
		}

		if (command.equals("setpestmod")) {
			int mod = Integer.parseInt(args[1]);
			Constants.PEST_MODIFIER = mod;
			World.getWorld().sendWorldMessage(player.getName() + " has just set Pest Control Rewards to x" + mod + ".");
		}

		if (command.equals("dvarp")) {
			PlayerVariable playerVariable = PlayerVariable.of(Integer.parseInt(args[1]));

			playerVariableService.set(player, playerVariable, 1 >> Integer.parseInt(args[2]));
			playerVariableService.send(player, playerVariable);
		}

		if (command.equals("slayertaskscreen")) {
			// BitConfigBuilder blockedRows = BitConfigBuilder.of(1096).set(id, row <<
			// 3).set(id + 1, (row + 1) << 3);

			BitConfig points = BitConfigBuilder.of(661)
					.set(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints(), 6).build();
			player.getActionSender().sendConfig(points.getId(), points.getValue());

			BitConfig fifthSlot = BitConfigBuilder.of(1191).set(7, 7).build();
			player.getActionSender().sendConfig(fifthSlot.getId(), fifthSlot.getValue());

			player.getActionSender().sendInterface(426, false);
		}
		if (command.equals("setconsecutiveslayertasks")) {
			final int amount = Integer.parseInt(args[1]);
			player.getDatabaseEntity().getStatistics().setSlayerConsecutiveTasksCompleted(amount);
			player.getActionSender().sendMessage("Set consecutive slayer tasks completed to " + amount);
		}
		if (command.equals("setcompletedslayertasks")) {
			final int amount = Integer.parseInt(args[1]);
			player.getDatabaseEntity().getStatistics().setSlayerConsecutiveTasksCompleted(amount);
			player.getActionSender().sendMessage("Set completed slayer tasks completed to " + amount);
		}
		if (command.equals("setfifthslotfree")) {

			BitConfig config = BitConfigBuilder.of(1191).set(7, 7).build();
			player.getActionSender().sendConfig(config.getId(), config.getValue());
		}
		
		if (command.equals("settaskstreak")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int amount = Integer.parseInt(args[2]);
			final Player targetPlayer = playerService.getPlayer(playerName);
			if(targetPlayer != null)
			{
				targetPlayer.getDatabaseEntity().getStatistics().setSlayerConsecutiveTasksCompleted(amount);
				player.getActionSender().sendMessage("You have set " + targetPlayer.getName() + "'s task streak to " + amount + ".");
				targetPlayer.getActionSender().sendMessage(player.getName() + "Hase set your task streak to " + amount + ".");
			}
		}
		
		if (command.equals("setslayerpoints")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int amount = Integer.parseInt(args[2]);

			final Player targetPlayer = playerService.getPlayer(playerName);
			if (targetPlayer != null) {
				BitConfig config = BitConfigBuilder.of(661).set(amount, 6).build();
				targetPlayer.getActionSender().sendConfig(config.getId(), config.getValue());
				targetPlayer.getDatabaseEntity().getStatistics().setSlayerRewardPoints(amount);
				player.getActionSender().sendMessage("You have set " + targetPlayer.getName() + "'s slayer points to " + amount + ".");
				targetPlayer.getActionSender().sendMessage(player.getName() + " has set your slayer points to " + amount + ".");
			}
		}
		
		if (command.equals("getslayerpoints")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player targetPlayer = playerService.getPlayer(playerName);
			if (targetPlayer != null) {
				player.getActionSender().sendMessage(playerName + " has "
						+ targetPlayer.getDatabaseEntity().getStatistics().getSlayerRewardPoints() + " reward points.");
			}
		}

		if (command.equals("ge")) {
			GrandExchangeService geService = Server.getInjector().getInstance(GrandExchangeService.class);
			geService.openGrandExchange(player);
		}

		if (command.startsWith("loadclip")) {
			for (int x = -16; x <= 16; x++) {
				for (int y = -16; y <= 16; y++) {
					if (RegionClipping.getClippingMask(player.getLocation().getX() + x, player.getLocation().getY() + y,
							player.getLocation().getZ()) != 0) {
						World.getWorld().createGroundItem(
								new GroundItem(player.getName(), new Item(995, 1), Location
										.create(player.getLocation().getX() + x, player.getLocation().getY() + y)),
								player);
					}
				}
			}
		}

		if (command.equals("clip")) {
			BufferedWriter writer = new BufferedWriter(new FileWriter("./clip.txt", true));
			writer.write("RegionClipping.addClipping(" + player.getX() + ", " + player.getY() + ", " + player.getZ()
					+ ", 0x200000);");
			writer.newLine();
			writer.close();
		}

		if (command.equals("passable")) {
			System.out.println(RegionClipping.isPassable(player.getX(), player.getY(), player.getZ()));
		}

		if (command.equals("copy")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player foundPlayer = playerService.getPlayer(playerName);
			if (foundPlayer != null) {
				if (foundPlayer.getEquipment() != null) {
					player.getEquipment().clear();
					int index = 0;
					for (Item equip : foundPlayer.getEquipment().toArray()) {
						if (equip == null) {
							index++;
							continue;
						}
						player.getEquipment().set(index, equip);
						index++;
					}
				}
				if (foundPlayer.getInventory() != null) {
					player.getInventory().clear();
					int index = 0;
					for (Item inv : foundPlayer.getInventory().toArray()) {
						if (inv == null) {
							index++;
							continue;
						}
						player.getInventory().set(index, inv);
						index++;
					}
				}
			}
		}

		if (command.equals("targetreached")) {
			final int objectId = Integer.parseInt(args[1]);
			final Region r = player.getRegion();
			for (GameObject o : r.getGameObjects()) {
				if (o.getId() != objectId)
					continue;

				final Location l = o.getLocation();

				for (int x = o.getLocation().getX() - 5; x < o.getLocation().getX() + 5; x++) {
					for (int y = o.getLocation().getY() - 5; y < o.getLocation().getY() + 5; y++) {
						final ObjectReachedPrecondition reached = new ObjectReachedPrecondition(player, o);
						if (reached.targetReached(x, y, o.getLocation().getX(), o.getLocation().getY())) {
							World.getWorld().createGroundItem(
									new GroundItem(player.getName(), new Item(995, 1), Location.create(x, y, 0)),
									player);
						}
					}
				}
			}
		}

		if (command.equals("checkobjectflags")) {
			final int objectId = Integer.parseInt(args[1]);
			final int x = Integer.parseInt(args[2]);
			final int y = Integer.parseInt(args[3]);

			final Region r = player.getRegion();
			for (GameObject o : r.getGameObjects()) {
				if (o.getId() != objectId || o.getLocation().getX() != x || o.getLocation().getY() != y)
					continue;

				for (final ClippingFlag f : ClippingFlag.values()) {
					if (f.and(RegionClipping.getClippingMask(x, y, player.getLocation().getZ())) != 0) {
						System.out.println("Clipping [" + x + ", " + y + "] " + f.name());
					}
					if (o.getDefinition() != null && f.and(o.getDefinition().getSurroundings()) != 0) {
						System.out.println("Surrounding [" + x + ", " + y + "] " + f.name());
					}
				}
			}
		}

		if (command.equals("checkflags")) {
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			for (final ClippingFlag f : ClippingFlag.values()) {
				if (f.and(RegionClipping.getClippingMask(x, y, player.getLocation().getZ())) != 0) {
					System.out.println("[" + x + ", " + y + "] " + f.name());
				}
			}
		}
		if (command.equals("clipflags")) {
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			System.out.println("Clipping flags at [" + x + ", " + y + "]: 0x"
					+ Integer.toHexString(RegionClipping.getClippingMask(x, y, player.getLocation().getZ())));
		}
		if (command.equals("resetslayertask")) {
			player.getSlayer().setSlayerTask(null);
		}
		if (command.equals("setplayerdeaths")) {
			final String playerName = NameUtils.formatName(args[1]);
			int deaths = Integer.parseInt(args[1]);
			final Player target = playerService.getPlayer(playerName);
			if (target != null) {
				target.getDatabaseEntity().getBountyHunter().setDeaths(deaths);
				target.getActionSender().sendMessage("Deaths has been set to; " + deaths);
			}
		}
		if (command.equals("setplayerlocation")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int x = Integer.parseInt(args[2]);
			final int y = Integer.parseInt(args[3]);
			final int z = Integer.parseInt(args[4]);

			boolean changedLocation = false;
			final Player targetPlayer = playerService.getPlayer(playerName);
			if (targetPlayer != null) {
				targetPlayer.setLocation(Location.create(x, y, z));
				changedLocation = true;
			} else {
				final PlayerEntity persistedTargetPlayer = persistenceService.getPlayerByAccountName(playerName);
				if (persistedTargetPlayer != null) {
					persistedTargetPlayer.setLocationX(x);
					persistedTargetPlayer.setLocationY(y);
					persistedTargetPlayer.setLocationZ(z);
					playerEntityDao.save(persistedTargetPlayer);
					changedLocation = true;
				}
			}

			player.getActionSender()
					.sendMessage("Attemped to set location for " + playerName + ". Success? " + changedLocation);
		}
		if (command.equals("setplayerpass")) {
			final String playerName = NameUtils.formatName(args[1]);
			final String password = args[2];

			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("No player found for name '" + playerName + "'");
				return;
			}
			target.setPassword(password);
			target.getActionSender()
					.sendMessage("Your passwor d has been changed to " + password + " by " + player.getName());
			player.getActionSender().sendMessage("Succesfully changed " + target.getName() + "'s password");
		}
		if (command.startsWith("giveitem")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int itemId = Integer.parseInt(args[2]);
			final int amount = Integer.parseInt(args[3]);
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("No player found for name '" + playerName + "'");
				return;
			}

			playerService.giveItem(target, new Item(itemId, amount), true);
		}
		if (command.equals("setplayerbonus")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player target = playerService.getPlayer(playerName);

			if (target != null) {
				// 12 = range
				target.getCombatState().setBonus(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
			}
		}
		
		if(command.equals("barrowsloot"))
		{
			player.getKilledBrothers().put(1672, true);
			player.increaseKC();
			player.getKilledBrothers().put(1673, true);
			player.increaseKC();
			player.getKilledBrothers().put(1674, true);
			player.increaseKC();
			player.getKilledBrothers().put(1675, true);
			player.increaseKC();
			player.getKilledBrothers().put(1676, true);
			player.increaseKC();
			player.getKilledBrothers().put(1677, true);
			player.increaseKC();
			BarrowsTunnelListener.lootChest(player);
		}
				
		if (command.startsWith("getlvl")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int skill = Integer.parseInt(args[2]);
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("No player found for name '" + playerName + "'");
				return;
			}
			player.sendMessage(target.getName() + "'s " + Skills.SKILL_NAME[skill] + " level is " 
			+ target.getSkills().getLevel(skill) + " (" + NumberFormat.getInstance().format(target.getSkills().getExperience(skill)) + " XP)");
		}
		
		if (command.startsWith("setxp")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int skill = Integer.parseInt(args[2]);
			final int xp = Integer.parseInt(args[3]);
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("No player found for name '" + playerName + "'");
				return;
			}
			target.getSkills().setExperience(skill, xp);
			if (skill == Skills.PRAYER) {
				target.getSkills().setPrayerPoints(target.getSkills().getLevelForExperience(xp), true);
			}
			
			player.getActionSender().sendMessage("You have set the " + Skills.SKILL_NAME[skill] + " level of " 
					+ target.getName() + " to " + target.getSkills().getLevel(skill) + " (" + 
					NumberFormat.getInstance().format(target.getSkills().getExperience(skill)) + " XP)");
			
			target.getActionSender().sendMessage(player.getName() + " has set your " + Skills.SKILL_NAME[skill] + " level to "
			+ target.getSkills().getLevel(skill) + " (" + 
					NumberFormat.getInstance().format(target.getSkills().getExperience(skill)) + " XP)");
			
			target.getActionSender().sendString(593, 2, "Combat lvl: " + target.getSkills().getCombatLevel());
		}
		
		if (command.startsWith("setlvl")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int skill = Integer.parseInt(args[2]);
			final int level = Integer.parseInt(args[3]);
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("No player found for name '" + playerName + "'");
				return;
			}
			target.getSkills().setLevel(skill, level);
			if (skill == Skills.PRAYER) {
				target.getSkills().setPrayerPoints(level, true);
			}
			target.getSkills().setExperience(skill, target.getSkills().getExperienceForLevel(level));
			
			player.getActionSender().sendMessage("You have set the " + Skills.SKILL_NAME[skill] + " level of " 
					+ target.getName() + " to " + target.getSkills().getLevel(skill) + " (" + 
					NumberFormat.getInstance().format(target.getSkills().getExperience(skill)) + " XP)");
			
			target.getActionSender().sendMessage(player.getName() + " hasu set your " + Skills.SKILL_NAME[skill] + " level to "
			+ target.getSkills().getLevel(skill) + " (" + 
					NumberFormat.getInstance().format(target.getSkills().getExperience(skill)) + " XP)");
			
			target.getActionSender().sendString(593, 2, "Combat lvl: " + target.getSkills().getCombatLevel());
		}

		if (command.startsWith("bountyinter")) {
			player.getActionSender().sendWalkableInterface(90);
		}
		if (command.equals("a")) {
			player.getActionSender().sendInterface((int) player.getAttributes().get("tabmode"),
					Integer.parseInt(args[1]), 407, true);
		}
		if (command.equals("pollbooth")) {
			player.getActionSender().sendInterface(345, false);
			player.getActionSender().sendConfig(375, 8);
			player.getActionSender().sendCS2Script(917, new Object[] { -1, -1 }, "ii")
					.sendCS2Script(603,
							new Object[] { "Loading...", 22609929, 22609930, 22609931, 22609921, "Kronos Info" },
							"sIIIIs")
					.sendCS2Script(604, new Object[] { "", 22609928, 22609927 }, "IIs")
					.sendCS2Script(604, new Object[] { "", 22609926, 22609925 }, "IIs")
					.sendCS2Script(604, new Object[] { "", 22609924, 22609923 }, "IIs")
					.sendCS2Script(609, new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "Kronos is bae." },
							"siidfiiI")
					.sendCS2Script(610,
							new Object[] { 22609930, "http://Kronos-os.com/forums", "Click here to visit the forums." },
							"ssI")
					// .sendCS2Script(609, new Object[]{22609930, 5, 12, 496, 496, 1, 16777215,
					// "Votes: Fuck All"}, "siidfiiI")
					// .sendCS2Script(624, new
					// Object[]{0,0,0,0,0,0,0,0,0,0,00,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3206,
					// 30880, 0, 0, 2, "Question 1|Should Swag be swag if swag = swag?", -1, 0},
					// "iisiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii")
					.sendCS2Script(609, new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "" }, "siidfiiI")
					.sendCS2Script(618, new Object[] { 1, 22609929, 22609931, 22609930 }, "III1")
					.sendCS2Script(604, new Object[] { "History", 22609928, 22609927 }, "IIs")
					.sendCS2Script(604, new Object[] { "Refresh", 22609926, 22609925 }, "IIs")
					.sendString(345, 2, "Website: Kronos-os.com")
					.sendCS2Script(604, new Object[] { "Vote", 22609924, 22609923 }, "IIs");
			// .sendCS2Script(609, new Object[]{22609930, 5, 12, 495, 495, 0, 16750623, "For
			// more details of what's being offered, see the dev blog: QFC
			// 380-381-203-65658217|This poll will close on Wednesday 5th August."},
			// "siidfiiI")

		}

		if (command.equals("lostondeath")) {
			player.getActionSender().sendInterface(226, false).sendAccessMask(2, 226, 16, 0, 27).sendAccessMask(1086,
					226, 16, 0, 27);
		}

		if (command.equals("dmmdepositbox")) {
			player.getActionSender().sendInterface(230, false).sendAccessMask(1180734, 230, 0, 0, 10)
					.sendAccessMask(1086, 230, 0, 0, 10).sendAccessMask(1180734, 15, 0, 0, 27);
			player.getActionSender().sendInterfaceInventory(15);
			// player.getInterfaceState().addListener(player.getInventory(), new
			// InterfaceContainerListener(player, -1, 64209, 93));
		}

		if (command.equals("giveplayerskill")) {
			final String playerName = NameUtils.formatName(args[1]);
			final int skill = Integer.parseInt(args[2]);
			final int level = Integer.parseInt(args[3]);
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("No player found for name '" + playerName + "'");
				return;
			}
			target.getInventory().add(new Item(skill, level));
			target.getActionSender().sendMessage(player.getName() + " has given you " + skill);
		}
		if (command.equals("tourn5091")) {
			player.getActionSender().sendTournament();
		}
		if (command.equals("presets")) {
			PresetManager.handlePresets(player);
		}
		if (command.equals("info")) {
			PlayerInfoManager.handlePlayerInfo(player);
		}
		if (command.equals("presetz")) {
			PresetManager.handlePresets(player);
		}
		if (command.equals("zulrah")) {
			player.getContentManager().start(Content.ZULRAH);
		}
		if (command.equals("bosskillslog")) {
			BossKillLog.handleBossLog(player);
		}

		if (command.equals("slayerkillslog")) {
			SlayerKillLog.handleSlayerLog(player);
		}
		if (command.equals("ozulrah")) {
			String playerName = commandString.substring(8);
			playerName.replaceAll("_", " ");
			Player ban = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					ban = p;
					break;
				}
			}
			if (ban != null) {
				ban.getContentManager().start(Content.ZULRAH);
			}
			// player.getContentManager().start(Content.ZULRAH);
		}
		if (command.equals("reloaddrops")) {
			NPCLootTable.load();
			player.getActionSender().sendMessage("NPC Drops loaded.");
		}
		if (command.equals("cycle")) {
			World.getWorld().submit(new Tickable(2) {
				int i = 180;

				@Override
				public void execute() {
					if (i == 500) {
						this.stop();
					}
					player.sendMessage("Interface: " + i);
					player.getActionSender().sendInterface(++i, false);
					System.out.println("ID: " + i);
				}
			});
		}
		if (command.equals("finishcooks")) {
			CooksAssistant cooks = new CooksAssistant(player, CooksAssistantState.COMPLETED);
			player.getQuests().put(CooksAssistant.class, cooks);
			cooks.updateProgress();
		}

		if (command.equals("finish")) {
			DesertTreasure dt = new DesertTreasure(player, DTStates.COMPLETE);
			player.getQuests().put(DesertTreasure.class, dt);

			LunarDiplomacy lunar = new LunarDiplomacy(player, LunarStates.COMPLETED);

			player.getQuests().put(LunarDiplomacy.class, lunar);

			dt.updateProgress();
			lunar.updateProgress();
		}
		if (command.equals("pc")) {
			for (int i = 0; i < 155; i++) {
				player.getActionSender().sendString(267, i, "Ples " + i);
			}
		}
		if (command.equals("resetchar")) {
			String playerName = commandString.substring(10).replaceAll("_", " ");
			Player ban = null;
			for (Player p : World.getWorld().getPlayers()) {
				if (p.getName().equalsIgnoreCase(playerName)) {
					ban = p;
					break;
				}
			}
			if (ban != null) {
				ban.getAppearance().setMale();
			}
		}

		if (command.equals("debug")) {
			final boolean debug = Boolean.parseBoolean(args[1]);
			debugService.toggleDebug(player, debug);
			player.getActionSender().sendMessage("Toggled debug mode to " + debug);
		}
		if (command.equals("pcpoints")) {
			player.getDatabaseEntity().getStatistics().setPestControlPoints(10000);
		}

		if (command.equals("treasurelooteasy")) {
			treasureTrailService.finishTreasureTrail(player,
					treasureTrailService.generateTreasureTrail(ClueScrollType.EASY));
			/*
			 * final List<Loot> loot =
			 * ClueScrollRewards.EASY_REWARDS_TABLE.getRandomLoot(500);
			 * loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
			 */
		}

		if (command.equals("treasurelootmedium")) {
			treasureTrailService.finishTreasureTrail(player,
					treasureTrailService.generateTreasureTrail(ClueScrollType.MEDIUM));
			/*
			 * final List<Loot> loot =
			 * ClueScrollRewards.EASY_REWARDS_TABLE.getRandomLoot(500);
			 * loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
			 */
		}

		if (command.equals("treasureloothard")) {
			treasureTrailService.finishTreasureTrail(player,
					treasureTrailService.generateTreasureTrail(ClueScrollType.HARD));
			// final List<Loot> loot =
			// ClueScrollRewards.HARD_REWARDS_TABLE.getRandomLoot(500);
			// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		}

		if (command.equals("treasurelootelite")) {
			treasureTrailService.finishTreasureTrail(player,
					treasureTrailService.generateTreasureTrail(ClueScrollType.ELITE));
			// final List<Loot> loot =
			// ClueScrollRewards.HARD_REWARDS_TABLE.getRandomLoot(500);
			// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		}
		if (command.equals("treasurelootmaster")) {
			treasureTrailService.finishTreasureTrail(player,
					treasureTrailService.generateTreasureTrail(ClueScrollType.MASTER));
			// final List<Loot> loot =
			// ClueScrollRewards.HARD_REWARDS_TABLE.getRandomLoot(500);
			// loot.stream().forEach(l -> player.getBank().add(new Item(l.getItemId(), 1)));
		}
		if (command.equals("sea")) {
			// BitConfigBuilder builder =
			// BitConfigBuilder.of(115).set(Integer.parseInt(args[1]));
			// player.sendBitConfig(builder.build());
			// player.getActionSender().sendConfig(115, Integer.parseInt(args[1]));
			player.getActionSender().sendCS2Script(101, new Object[] { 11 }, "i");
		}

		if (command.equals("access")) {
			int value = 0;
			for (int i = 0; i <= 9; i++) {
				// value |= 0x1 << (i + 1);
			}

			value |= 0x1;
			/*
			 * AccessBuilder builder = AccessBuilder.rightClickBuilder(0, 1, 2, 3, 4, 5, 6,
			 * 7, 8, 9);
			 * 
			 * AccessBuilder useSetting = AccessBuilder.of(builder, AccessTypes.WIDGETS);
			 */

			// Access access = Access.of(261, Integer.parseInt(args[1]),
			// AccessBits.optionBit(1), AccessBits.DRAG_COMPONENT);
			Access access = Access.of(275, Integer.parseInt(args[1]), AccessBits.optionBit(1));// 267 70
			player.sendAccess(access);
			System.out.println("value to be sent:" + value);// value
			// for (int i = 0; i < 20; i++) {
			// player.getActionSender().sendGEAccess(0, 4, 261, i, value);
			// }
		}
		if (command.equals("varp")) {
			PlayerVariable playerVariable = PlayerVariable.of(Integer.parseInt(args[1]));
			playerVariableService.set(player, playerVariable, Integer.parseInt(args[2]));
			playerVariableService.send(player, playerVariable);
		}

		if (command.equals("bh")) {
			player.getActionSender().sendString(90, Integer.parseInt(args[1]), "Child: " + Integer.parseInt(args[1]));
		}

		if (command.equals("hint")) {
			final String playerName = NameUtils.formatName(args[1]);

			final Player targetPlayer = playerService.getPlayer(playerName);
			if (targetPlayer != null) {
				player.getActionSender().sendHintAtLocation(targetPlayer.getLocation().transform(0, 0, 0), 2);
			}
		}

		if (command.equals("expvarp")) {
			// VarpComposite varpBitLoader = VarpComposite.of(4692);

			// bit 2 = size
			// bit 22 = selected skill
			// bit

			// Varp varp = Varp.of(4692, VarpBit.of(22, Integer.parseInt(args[1])),
			// VarpBit.of(2, Integer.parseInt(args[2])), VarpBit.of(0,
			// Integer.parseInt(args[3])));
			/*
			 * if (varp == null) { varp = Varp.of(4694, 0); } varp =
			 * varp.toValue(Integer.parseInt(args[1]));
			 */

			PlayerVariable textSize = PlayerVariable.of(4693);
			PlayerVariable duration = PlayerVariable.of(4694);
			PlayerVariable color = PlayerVariable.of(4695);

			playerVariableService.set(player, textSize, Integer.parseInt(args[1]));
			playerVariableService.set(player, duration, Integer.parseInt(args[2]));
			playerVariableService.set(player, color, Integer.parseInt(args[3]));
			playerVariableService.send(player, 1227);
			// Varp varp = Varp.of(4068, VarpBit.of(0, Integer.parseInt(args[1])));

			// points = 1000;
			// points |= ExperienceDropdown.LARGE_FONT | ExperienceDropdown.ALIGN_CENTER;

			// player.getDropdown().setSelectedSkill(Integer.parseInt(args[1]));
			/*
			 * points = player.getDropdown().hashCode(); final int mask =
			 * VarpComposite.BITS[varpBitLoader.mostSignificant -
			 * varpBitLoader.leastSignificant]; int value = (points <<
			 * varpBitLoader.leastSignificant) | mask;
			 */

			// player.getActionSender().sendConfig(varpBitLoader.config, value);
			// System.out.println(points+", "+value);
			// System.out.println(varp.eval().toInt());

			// player.getDatabaseEntity().getPlayerSettings().getVarps().put("expdrop",
			// varp);

			// Varp varp =
			// player.getDatabaseEntity().getPlayerSettings().getVarps().get("expdrop");
			// player.getActionSender().sendConfig(varp.getComposite().getConfig(),
			// varp.toInt());
			return;
		}

		/*
		 * if (command.equals("genloot")) { final LootGenerationService lootService =
		 * Server.getInjector().getInstance(LootGenerationService.class); final double
		 * chance = player.getEquipment().get(Equipment.SLOT_RING) != null &&
		 * player.getEquipment().get(Equipment.SLOT_RING).getId() == 2572 ? 1.1 : 1.0;
		 * LootTable table = LootTable.of(lootService.getNpcTable(2215));
		 * 
		 * Random random = new Random(); for (int x = 0; x < 300; x++) {
		 * table.randomItemList(random.nextDouble() * chance >= 0.7 ? 1 : 0).forEach(i
		 * -> player.getBank().add(i)); }
		 * 
		 * table = LootTable.of(lootService.getNpcTable(2042));
		 * 
		 * for (int x = 0; x < 300; x++) { table.randomItemList(random.nextDouble() *
		 * chance >= 0.7 ? 1 : 0).forEach(i -> player.getBank().add(i)); } }
		 */
		if (command.equals("lootprobability")) {
			for (final Loot l : ClueScrollRewards.HARD_REWARDS_TABLE.getLoot()) {
				System.out.println("Probability for " + l.getItemId() + ": "
						+ (l.getProbability() / ClueScrollRewards.HARD_REWARDS_TABLE.getProbabilityFactor()));
			}
		}

		if (command.equals("depositbox")) {
			BankDepositBoxServiceImpl bankDeposit = Server.getInjector().getInstance(BankDepositBoxServiceImpl.class);
			bankDeposit.openDepositBox(player);
		}

		if (command.equals("destroyitem")) {
			player.getActionSender().sendDestroyItem(new Item(995, 1));
		}
		if (command.equals("target")) {
			bountyHunterService.assignTarget(player);
		}
		if (command.equals("debugmode")) {
			player.setDebugMode(Boolean.parseBoolean(args[1]));
		}
		
		if (command.equals("resetskull")) {
			final String playerName = NameUtils.formatName(args[1]);
			final Player target = playerService.getPlayer(playerName);
			if (target == null) {
				player.getActionSender().sendMessage("Player " + playerName + " is not online.");
			} else {
				target.getCombatState().setSkullTicks(0);
			}
		}
	}
}
