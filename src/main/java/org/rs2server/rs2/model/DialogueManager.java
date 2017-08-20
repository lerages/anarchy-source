package org.rs2server.rs2.model;

import org.apache.commons.lang3.text.WordUtils;
import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.cache.format.CacheNPCDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.content.StarterMap;
import org.rs2server.rs2.content.dialogue.Dialogue;
import org.rs2server.rs2.content.misc.GracefulRecolour;
import org.rs2server.rs2.domain.model.claim.ClaimType;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.GemBagService;
import org.rs2server.rs2.domain.service.api.content.LootingBagService;
import org.rs2server.rs2.domain.service.api.content.PotionDecanterService;
import org.rs2server.rs2.domain.service.api.content.bounty.BountyHunterService;
import org.rs2server.rs2.domain.service.api.content.gamble.FlowerGame;
import org.rs2server.rs2.domain.service.impl.BankPinServiceImpl;
import org.rs2server.rs2.domain.service.impl.PermissionServiceImpl;
import org.rs2server.rs2.model.Animation.FacialAnimation;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.LootingBag;
import org.rs2server.rs2.model.minigame.warriorsguild.WarriorsGuild;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.slayer.SlayerTask;
import org.rs2server.rs2.model.skills.slayer.SlayerTask.Master;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.ActionSender.DialogueType;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.util.DonationManager;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;


public class DialogueManager 
{

	public static void openDialogue(final Player player, int dialogueId)
	{
		final SlayerService slayerService = Server.getInjector().getInstance(SlayerService.class);
		final PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
		final GemBagService gemBagService = Server.getInjector().getInstance(GemBagService.class);
		boolean starter = player.getAttribute("starter");
		if (dialogueId == -1) {
			return; 
		}
		for (int i = 0; i < 5; i++) {
			player.getInterfaceState().setNextDialogueId(i, -1);
		}
		player.getInterfaceState().setOpenDialogueId(dialogueId);
		//NPC npc = (NPC) player.getInteractingEntity();
		switch (dialogueId) {
		case 3308:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT, 
					"Greetings, " + player.getName() + "! I am here to help you with anything related to donating. What can I do for you?");
			player.getInterfaceState().setNextDialogueId(0, 3309);
			break;
		case 3309:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"How can I donate?|What are the rewards for donating?|Where do the donations go?|I'd like to claim a donation|Nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 3310);
			player.getInterfaceState().setNextDialogueId(1, 3312);
			player.getInterfaceState().setNextDialogueId(2, 3318);
			player.getInterfaceState().setNextDialogueId(3, 3322);
			player.getInterfaceState().setNextDialogueId(4, -1);
			break;
		case 3310:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"How can I donate?");
			player.getInterfaceState().setNextDialogueId(0, 3311);
			break;
		case 3311:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Donating is real easy! All you have to do is head to our website at: os-anarchy.com and click the 'donate' tab at the top of the page."
					+ " After that, simply select the perks you would like to receive for donating!");
			player.getInterfaceState().setNextDialogueId(0, 3312);
			break;
		case 3312:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Perks are what we grant to players that are generous enough to donate to us. They are little additions that are added to your "
					+ "account that help you further enjoy the ways you play OS-Anarchy.");
			player.getInterfaceState().setNextDialogueId(0, 3312);
			break;
		case 3313:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Typically, they tend to be small changes that increase the convenience of otherwise tedious tasks. "
					+ "You can only gain each perk once. However, once you own a perk it lasts forever!");
			player.getInterfaceState().setNextDialogueId(0, 3314);
			break;
		case 3314:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Additionally, each time you donate the total amount you have donated to us increases. Once this surpasses"
					+ "$20 you receive the donator rank.");
			player.getInterfaceState().setNextDialogueId(0, 3315);
			break;
		case 3315:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"The rank doesn't give you any icon, but it does give you access to some cool additions on top of the"
					+ " perks you get for donating!");
			player.getInterfaceState().setNextDialogueId(0, 3316);
			break;
		case 3316:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Currently, donating $20 or more gives you access to the ::yell command. Also, it removes the cost of using the"
					+ "altar and decanter at home.");
			player.getInterfaceState().setNextDialogueId(0, 3317);
			break;
		case 3317:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Finally, being a donator also makes it free to use the 'Last-teleport' option of the teleporter by"
					+ " the well in Edgeville!");
			player.getInterfaceState().setNextDialogueId(0, 3318);
			break;
		case 3318:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Any donations made to us are strictly used to fund anything required to keep OS-Anarchy online or help it grow.");
			player.getInterfaceState().setNextDialogueId(0, 3319);
			break;
		case 3319:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"This could be anything from hosting costs to paid advertisements. Anything you can contribute to this cause"
					+ " is greatly appreciated. If you can't, thats okay too! Thanks for joining us anyway.");
			player.getInterfaceState().setNextDialogueId(0, 3320);
			break;
		case 3320:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"Whether you donate or not, we hope you choose to stay with us and enjoy your time. Do not hesitate to"
					+ " contanct Zero or Zaros if there's anything you need. Farewell!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 3322:
			player.getActionSender().sendDialogue("RuneScape Guide", DialogueType.NPC, 3308, FacialAnimation.DEFAULT,
					"You currently don't have a donation reward to claim, please donate first!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 13372:
			if(player.getInventory().hasItem(new Item(995, 500000))) { //500k
				player.flowerBetAmount += 500000;
				openDialogue(player, 13377);
			}
			break;
		case 13373:
			if(player.getInventory().hasItem(new Item(995, 1000000))) { //1m
				player.flowerBetAmount += 1000000;
				openDialogue(player, 13377);
			}
			break;
		case 13374:
			if(player.getInventory().hasItem(new Item(995, 2500000))) { //2.5m
				player.flowerBetAmount += 2500000;
				openDialogue(player, 13377);
			}
			break;
		case 13375:
			if(player.getInventory().hasItem(new Item(995, 5000000))) { //5m
				player.flowerBetAmount += 5000000;
				openDialogue(player, 13377);
			}
			break;
		case 13376:
			if(player.getInventory().hasItem(new Item(995, 10000000))) { //10m
				player.flowerBetAmount += 10000000;
				openDialogue(player, 13377);
			} 
			break;
			case 13377:
				player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"Red|Blue");
				player.getInterfaceState().setNextDialogueId(0, 13378);
				player.getInterfaceState().setNextDialogueId(1, 13379);
				break;
			case 13378:
				player.getActionSender().removeChatboxInterface();
				player.flowerGuess = 15846;
				player.getFlowerGame().execute(player);
				break;
			case 13379:
				player.getActionSender().removeChatboxInterface();
				player.flowerGuess = 15872;
				player.getFlowerGame().execute(player);
				break;
			case 21000:
				player.getActionSender().sendDialogue("Tyss", DialogueType.NPC, 7050, FacialAnimation.DEFAULT, "Hello, would you like to switch to Arceuus magics?");
				player.getInterfaceState().setNextDialogueId(0, 21001);
				break;
			case 21001:
				player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, null,
						"Yes, please.|No thank you.");
				player.getInterfaceState().setNextDialogueId(0, 21002);
				player.getInterfaceState().setNextDialogueId(1, 21003);
				break;
			case 21002:
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20251, null, "Your magic book has been changed to the Arceuus spellbook.");//6603 20251
				player.getActionSender().sendConfig(439, 3);
				player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.ARCEUUS_MAGICS.getSpellBookId());
				break;
			case 21003:
				player.getActionSender().removeChatboxInterface();
				break;
			case 20000:
				player.getActionSender().sendDialogue("Choose a Spellbook", DialogueType.OPTION, -1, null,
						"Modern Magics|Ancient Magiks|Lunar Magics|Nevermind");
				player.getInterfaceState().setNextDialogueId(0, 20001);
				player.getInterfaceState().setNextDialogueId(1, 20002);
				player.getInterfaceState().setNextDialogueId(2, 20003);
				player.getInterfaceState().setNextDialogueId(3, 20004);
				break;
			case 20001:
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 1381, null, "Your magic book has been changed to the Regular spellbook.");
				player.getActionSender().sendConfig(439, 0);
				player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.MODERN_MAGICS.getSpellBookId());
				break;
			case 20002:
				player.getActionSender().sendConfig(439, 1);
				player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.ANCIENT_MAGICKS.getSpellBookId());
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 4675, null, "An ancient wisdom fills your mind...");
				break;
			case 20003:
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 9084, null, "Lunar spells activated!");
				player.getActionSender().sendConfig(439, 2);
				player.getCombatState().setSpellBook(MagicCombatAction.SpellBook.LUNAR_MAGICS.getSpellBookId());
				break;
			case 20004:
				player.getActionSender().removeChatboxInterface();
				break;
		case 19000:
			player.getActionSender().sendDialogue("Choose a Gamemode", DialogueType.OPTION, -1, null,
					"No restrictions|<img=2>Ironman|<img=3>Ultimate Ironman|<img=10>Hardcore Ironman");
			player.getInterfaceState().setNextDialogueId(0, 19001);
			player.getInterfaceState().setNextDialogueId(1, 19002);
			player.getInterfaceState().setNextDialogueId(2, 19003);
			player.getInterfaceState().setNextDialogueId(3, 19004);
			break;
		case 19001:
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "You have selected <col=000088>no restrictions</col>, the default gamemode.");
			ClaimService claimService1 = Server.getInjector().getInstance(ClaimService.class);
			PlayerService playerService1 = Server.getInjector().getInstance(PlayerService.class);
			player.getInterfaceState().setNextDialogueId(0, 19104);
			break;
		case 19104:
			player.getActionSender().removeChatboxInterface();
			player.removeAttribute("busy");
			break;
		case 19002:
			ClaimService claimService2 = Server.getInjector().getInstance(ClaimService.class);
			PlayerService playerService2 = Server.getInjector().getInstance(PlayerService.class);;
			PermissionService perms = Server.getInjector().getInstance(PermissionService.class);
			player.setIsIronMan(true);
			player.setUltimateIronMan(false);
			player.setHardcoreIronMan(false);
			perms.give(player, PermissionService.PlayerPermissions.IRON_MAN);
			
			break;
		case 19003:
			ClaimService claimService3 = Server.getInjector().getInstance(ClaimService.class);
			PlayerService playerService3 = Server.getInjector().getInstance(PlayerService.class);;
			PermissionService perms1 = Server.getInjector().getInstance(PermissionService.class);
			player.setIsIronMan(false);
			player.setUltimateIronMan(true);
			player.setHardcoreIronMan(false);
			perms1.give(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
			player.getInterfaceState().setNextDialogueId(0, 19104);
			break;
		case 19004:
			ClaimService claimService4 = Server.getInjector().getInstance(ClaimService.class);
			PlayerService playerService4 = Server.getInjector().getInstance(PlayerService.class);;
			PermissionService perms2 = Server.getInjector().getInstance(PermissionService.class);
			player.setIsIronMan(false);
			player.setUltimateIronMan(false);
			player.setHardcoreIronMan(true);
			perms2.give(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN);
			player.getInterfaceState().setNextDialogueId(0, 19104);
			break;
			
		case 0:
			player.getActionSender().sendDialogue("Test", DialogueType.NPC, 2044, FacialAnimation.DEFAULT, "Hello, how may I help you?");
			player.getInterfaceState().setNextDialogueId(0, 1);
			break;
		case 1:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I'd like to bank my items.|Nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 2);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 2:
			Bank.open(player);
			player.getActionSender().removeChatboxInterface();
			break;
		case 3:
			player.getActionSender().sendDialogue("Frodo", DialogueType.NPC, 2898, FacialAnimation.DEFAULT, "What would you like to do?");
			player.getInterfaceState().setNextDialogueId(0, 4);
			break;
		case 4:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I'd like to go somewhere.|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 5);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 5:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "I'd like to go somewhere.");
			player.getInterfaceState().setNextDialogueId(0, 6);
			break;
		case 6:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Bosses|Minigames|Training Areas");
			player.getInterfaceState().setNextDialogueId(0, 7);
			player.getInterfaceState().setNextDialogueId(1, 12);
			player.getInterfaceState().setNextDialogueId(2, 17);
			break;
		case 7: // Bosses
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Bandos|Armadyl|Saradomin|Zamorak|More...");
			player.getInterfaceState().setNextDialogueId(0, 8);
			player.getInterfaceState().setNextDialogueId(1, 9);
			player.getInterfaceState().setNextDialogueId(2, 10);
			player.getInterfaceState().setNextDialogueId(3, 11);
			player.getInterfaceState().setNextDialogueId(4, -1);
			break;
		case 8:
			player.setAttribute("teleportTo", Location.create(2864, 5354, 2));

			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Good luck!");
			player.getInterfaceState().setNextDialogueId(0, 26);
			break;
		case 9:
			player.setAttribute("teleportTo", Location.create(2839, 5296, 2));
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Good luck!");
			player.getInterfaceState().setNextDialogueId(0, 26);
			break;
		case 10:
			player.setAttribute("teleportTo", Location.create(2907, 5265, 0));
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Good luck!");
			player.getInterfaceState().setNextDialogueId(0, 26);
			break;
		case 11:
			player.setAttribute("teleportTo", Location.create(2925, 5331, 2));
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Good luck!");
			player.getInterfaceState().setNextDialogueId(0, 26);
			break;
		case 12: // Minigames
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Nothing|is|here|yet");
			player.getInterfaceState().setNextDialogueId(0, 13);
			player.getInterfaceState().setNextDialogueId(1, 14);
			player.getInterfaceState().setNextDialogueId(2, 15);
			player.getInterfaceState().setNextDialogueId(3, 16);
			break;
		case 13:

			break;
		case 14:

			break;
		case 15:

			break;
		case 16:

			break;
		case 17: // Training areas
		player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
				"Stronghold of Security|Pioneer's Dungeon|Rock Crabs|Nevermind");
		player.getInterfaceState().setNextDialogueId(0, 18);
		player.getInterfaceState().setNextDialogueId(1, 19);
		player.getInterfaceState().setNextDialogueId(2, 20);
		player.getInterfaceState().setNextDialogueId(3, -1);
		break;
		case 18:
			player.setTeleportTarget(Location.create(1860, 5244, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 19:
			player.setTeleportTarget(Location.create(3167, 9572, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 20:
			player.setTeleportTarget(Location.create(2673, 3714, 0));
			player.getActionSender().removeChatboxInterface();
			break;

		case 21:
			player.getActionSender().sendDialogue("TzHaar-Mej-Jal", DialogueType.NPC, 2180, FacialAnimation.DEFAULT, 
					"You defeated TzTok-Jad, I am most impressed!<br>Please accept this gift as a reward.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 22:
			player.getActionSender().sendDialogue("TzHaar-Mej-Jal", DialogueType.NPC, 2180, FacialAnimation.DEFAULT, 
					"You're on your own now, Jalyt.<br>Prepare to fight for your life!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 23:
			player.getActionSender().sendDialogue(null, DialogueType.MESSAGE, -1, null, "Are you sure you want to merge the shards<br>and create a blade?");
			player.getInterfaceState().setNextDialogueId(0, 24);
			break;
		case 24:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 25);
			player.getInterfaceState().setNextDialogueId(1, 25000);
			break;
		case 25000:
			player.getActionSender().removeChatboxInterface();
			break;
		case 25:
			player.getActionSender().removeChatboxInterface();
			player.getInventory().remove(new Item(11818, 1));
			player.getInventory().remove(new Item(11820, 1));
			player.getInventory().remove(new Item(11822, 1));
			player.getInventory().add(new Item(11798, 1));
			player.getActionSender().sendMessage("You combine the shards into a Godsword Blade.");
			player.getSkills().addExperience(Skills.SMITHING, 200);
			break;
		case 26:
			final Location teleportTo = player.getAttribute("teleportTo");
			if (teleportTo != null) {
				player.playAnimation(Animation.create(714));
				player.playGraphics(Graphic.create(308, 48, 100));
				World.getWorld().submit(new Tickable(4) {
					@Override
					public void execute() {
						player.setTeleportTarget(teleportTo);
						player.playAnimation(Animation.create(-1));
						player.playAnimation(Animation.create(715));
						this.stop();
					}
				});
			}
			player.getActionSender().removeChatboxInterface();
			break;
		case 27:
			player.getActionSender().sendDialogue("Make-over mage", DialogueType.NPC, 1306, FacialAnimation.DEFAULT, "Hello there! I am known as the make-over mage! I have<br>spent many years researching magics that can change<br>your physical appearance!");
			player.getInterfaceState().setNextDialogueId(0, 28);
			break;
		case 28:
			player.getActionSender().sendDialogue("Make-over mage", DialogueType.NPC, 1306, FacialAnimation.DEFAULT, "I can alter your physical form for a small fee of only<br>3000 gold coins! Would you like me to perform my<br>magics upon you?");
			player.getInterfaceState().setNextDialogueId(0, 29);
			break;
		case 29:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 30);
			player.getInterfaceState().setNextDialogueId(1, 31);
			break;
		case 30:
			if (player.getInventory().hasItem(new Item(995, 3000))) {
				player.getActionSender().sendInterface(269, false);
				player.getInventory().remove(new Item(995, 3000));
			} else {
				player.getActionSender().sendMessage("You don't have enough gold to do this.");
			}
			player.getActionSender().removeChatboxInterface();
			break;
		case 31:
			player.getActionSender().removeChatboxInterface();
			break;
		case 32:// o.o?

			player.getActionSender().sendDialogue("Kamfreena", DialogueType.NPC, 2461, FacialAnimation.DEFAULT, "Time is up! You have ran out of Warrior Guild Tokens.<br>Please leave the room of Cyclopes as soon as possible.");
			player.getInterfaceState().setNextDialogueId(0, 33);
			break;
		case 33:
			player.getActionSender().removeChatboxInterface();
			break;
		case 34:
			player.getActionSender().sendDialogue("Kamfreena", DialogueType.NPC, 2461, FacialAnimation.ANGER_1, "I said TIME UP! Please leave by yourself next time.");
			player.getInterfaceState().setNextDialogueId(0, 35);
			break;
		case 35:
			player.getActionSender().removeChatboxInterface();
			break;
		case 36:
			player.getActionSender().sendDialogue("Shop keeper", DialogueType.NPC, 514, FacialAnimation.DEFAULT, "Hello! Would you like to see my wide variety of items?");
			player.getInterfaceState().setNextDialogueId(0, 37);
			break;
		case 37:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 38);
			player.getInterfaceState().setNextDialogueId(1, 39);
			break;
		case 38:
			Shop.open(player, 0, 1);
			player.getActionSender().removeChatboxInterface();
			break;
		case 39:
			player.getActionSender().removeChatboxInterface();
			break;
		case 40:
			player.getActionSender().sendDialogue("Shop keeper", DialogueType.NPC, 516, FacialAnimation.DEFAULT, "Hello! Would you like to see my wide variety of items?");
			player.getInterfaceState().setNextDialogueId(0, 41);
			break;
		case 41:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 42);
			player.getInterfaceState().setNextDialogueId(1, 43);
			break;
		case 42:
			Shop.open(player, 1, 1);
			player.getActionSender().removeChatboxInterface();
			break;
		case 43:
			player.getActionSender().removeChatboxInterface();
			break;
		case 44:
			player.getActionSender().sendDialogue("Shop keeper", DialogueType.NPC, 518, FacialAnimation.DEFAULT, "Hello! Would you like to see my wide variety of items?");
			player.getInterfaceState().setNextDialogueId(0, 45);
			break;
		case 45:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 46);
			player.getInterfaceState().setNextDialogueId(1, 47);
			break;
		case 46:
			Shop.open(player, 2, 1);
			player.getActionSender().removeChatboxInterface();
			break;
		case 47:
			player.getActionSender().removeChatboxInterface();
			break;
		case 48:
			player.getActionSender().sendDialogue("You've found a hidden tunnel, do you want to enter?", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 49);
			player.getInterfaceState().setNextDialogueId(1, 50);
			break;
		case 49:
			player.setTeleportTarget(Location.create(3551, 9694, 0));
			player.getActionSender().updateMinimap(ActionSender.BLACKOUT_MAP);
			player.getActionSender().removeChatboxInterface();
			break;
		case 50:
			player.getActionSender().removeChatboxInterface();
			break;
		case 51:
			player.getActionSender().sendDialogue("Shop keeper", DialogueType.NPC, 519, FacialAnimation.DEFAULT, "Hello! Would you like to see my wide variety of items?");
			player.getInterfaceState().setNextDialogueId(0, 52);
			break;
		case 52:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 53);
			player.getInterfaceState().setNextDialogueId(1, 54);
			break;
		case 53:
			Shop.open(player, 3, 1);
			player.getActionSender().removeChatboxInterface();
			break;
		case 54:
			player.getActionSender().removeChatboxInterface();
			break;
		case 55:
			player.getActionSender().sendDialogue("Emblem Trader", DialogueType.NPC, 315, FacialAnimation.DEFAULT, 
					"Hello, wanderer");
			player.getInterfaceState().setNextDialogueId(0, 101056);
			break;
		case 101056:
			player.getActionSender().sendDialogue("Emblem Trader", DialogueType.NPC, 315, FacialAnimation.DEFAULT, 
					"Don't suppose you've come across any strange... emblems along your journey?");
			player.getInterfaceState().setNextDialogueId(0, 101057);
			break;
		case 101057:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Not that I've seen.");
			player.getInterfaceState().setNextDialogueId(0, 101058);
			break;
		case 101058:
			player.getActionSender().sendDialogue("Emblem Trader", DialogueType.NPC, 315, FacialAnimation.DEFAULT, 
					"If you do, please do let me know. I'll reward you handsomely.");
			player.getInterfaceState().setNextDialogueId(0, 56);
			break;
		case 56:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"What rewards have you got?|Can I have a PK skull, please?|That's nice.");
			player.getInterfaceState().setNextDialogueId(0, 10000);
			player.getInterfaceState().setNextDialogueId(1, 58);
			player.getInterfaceState().setNextDialogueId(2, 59);
			break;
		case 57:
			player.getActionSender().sendDialogue("Emblem Trader", DialogueType.NPC, 315, FacialAnimation.DEFAULT, 
					"Do not ask stupid questions, adventurer. You might be disappointed with the response.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 58:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Can I have a PK skull please?");
			player.getInterfaceState().setNextDialogueId(0, 101059);
		break;
		case 101059:
			player.getActionSender().sendDialogue("<col=880000>A PK skull means you drop ALL your items on death", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Give me a PK skull.|Cancel");
			player.getInterfaceState().setNextDialogueId(0, 101060);
			break;
		case 101060:
			player.getCombatState().setSkullTicks(1000);
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 964, null, 
					"You are now skulled.");
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 59:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"That's nice.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			//player.setQueuedSwitching(!player.hasQueuedSwitching());
			//player.getActionSender().sendMessage("Switching type toggled. 07 mode: " + player.hasQueuedSwitching());
			//player.getActionSender().removeChatboxInterface();
			break;
		case 60:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Buy 10|Buy 50|Buy 100");
			player.getInterfaceState().setNextDialogueId(0, 61);
			player.getInterfaceState().setNextDialogueId(1, 62);
			player.getInterfaceState().setNextDialogueId(2, 63);
			break;
		case 61:
			if (player.getInterfaceState().getShopItem() != -1 && player.getInterfaceState().getShopSlot() != -1) {
				Shop.buyItem(player, player.getInterfaceState().getShopSlot(), player.getInterfaceState().getShopItem(), 10);
				player.getInterfaceState().setShopItem(-1, -1);
			}
			player.getActionSender().removeChatboxInterface();
			break;
		case 62:
			if (player.getInterfaceState().getShopItem() != -1 && player.getInterfaceState().getShopSlot() != -1) {
				Shop.buyItem(player, player.getInterfaceState().getShopSlot(), player.getInterfaceState().getShopItem(), 50);
				player.getInterfaceState().setShopItem(-1, -1);
			}
			player.getActionSender().removeChatboxInterface();
			break;
		case 63:
			if (player.getInterfaceState().getShopItem() != -1 && player.getInterfaceState().getShopSlot() != -1) {
				Shop.buyItem(player, player.getInterfaceState().getShopSlot(), player.getInterfaceState().getShopItem(), 100);
				player.getInterfaceState().setShopItem(-1, -1);
			}
			player.getActionSender().removeChatboxInterface();
			break;
		case 64:
			player.getActionSender().sendDialogue("Telekinetic Guardian", DialogueType.NPC, 5979, FacialAnimation.DEFAULT, "Hello, would you like me to teleport you somewhere?");
			player.getInterfaceState().setNextDialogueId(0, 65);
			break;
		case 65:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"East Dragons|West Dragons|Dark Castle");
			player.getInterfaceState().setNextDialogueId(0, 66);
			player.getInterfaceState().setNextDialogueId(1, 67);
			player.getInterfaceState().setNextDialogueId(2, 68);
			break;
		case 66:
			player.setTeleportTarget(Constants.EAST_DRAGONS);
			player.getActionSender().removeChatboxInterface();
			break;
		case 67:
			player.setTeleportTarget(Constants.WEST_DRAGONS);
			player.getActionSender().removeChatboxInterface();
			break;
		case 68:
			player.setTeleportTarget(Constants.DARK_CASTLE);
			player.getActionSender().removeChatboxInterface();
			break;
		case 69:
			player.getActionSender().removeChatboxInterface();
			break;
		case 70:
			player.getActionSender().sendDialogue("Captain Klemfoodle", DialogueType.NPC, 6092, FacialAnimation.DEFAULT, 
					"Hello human. What can I do for you?");
			player.getInterfaceState().setNextDialogueId(0, 71);
			break;
		case 6990:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "'Ello and what are you after then?");
			player.getInterfaceState().setNextDialogueId(0, 6991);
			break;
		case 6991:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I need another assignment.|Do you have anything for trade?|Other slayer masters.|Can you imbue slayer helmets?|Er...nothing...");
			player.getInterfaceState().setNextDialogueId(0, 6992);
			player.getInterfaceState().setNextDialogueId(1, 6993);
			player.getInterfaceState().setNextDialogueId(2, 6994);
			player.getInterfaceState().setNextDialogueId(3, 106995);
			player.getInterfaceState().setNextDialogueId(4, 6995);
			break;
		case 106995:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you imbue a slayer helmet to work with magic and ranged attacks?");
			player.getInterfaceState().setNextDialogueId(0, 106996);
			break;
		case 106996:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, 
					"Of course. Simply hand me a helmet and I'll make the necessary adjustments for you. You'll need an additional 400 slayer points, however.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 6992:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "I need another assignment.");
			player.getInterfaceState().setNextDialogueId(0, 512);
			break;
		case 6993:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Do you have anything for trade?");
			player.getInterfaceState().setNextDialogueId(0, 507);
			break;
		case 6994:
			player.getActionSender().sendDialogue("Select a Master", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Mazchna|Vannaka|Chaeldar|Nieve|Duradael");
			player.getInterfaceState().setNextDialogueId(0, 16994);
			player.getInterfaceState().setNextDialogueId(1, 16997);
			player.getInterfaceState().setNextDialogueId(2, 17003);
			player.getInterfaceState().setNextDialogueId(3, 17006);
			player.getInterfaceState().setNextDialogueId(4, 17009);
			break;
		case 16994:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you teleport me to Mazchna, please?");
			player.getInterfaceState().setNextDialogueId(0, 16995);
			break;
		case 16995:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, 
					"Certainly.");
			player.getInterfaceState().setNextDialogueId(0, 16996);
			break;
		case 16996:
			player.teleport(Constants.MAZCHNA, 1, 1, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 16997:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you teleport me to Vannaka, please?");
			player.getInterfaceState().setNextDialogueId(0, 16998);
			break;
		case 16998:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, 
					"Certainly.");
			player.getInterfaceState().setNextDialogueId(0, 16999);
			break;
		case 16999:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.VANNAKA, 1, 1, true);
			break;
		case 17003:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you teleport me to Chaeldar, please?");
			player.getInterfaceState().setNextDialogueId(0, 17004);
			break;
		case 17004:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, 
					"Certainly.");
			player.getInterfaceState().setNextDialogueId(0, 17005);
			break;
		case 17005:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.CHAELDAR, 1, 1, true);
			break;
		case 17006:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you teleport me to Nieve, please?");
			player.getInterfaceState().setNextDialogueId(0, 17007);
			break;
		case 17007:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, 
					"Certainly.");
			player.getInterfaceState().setNextDialogueId(0, 17008);
			break;
		case 17008:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.NIEVE, 1, 1, true);
			break;
		case 17009:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you teleport me to Duradal, please?");
			player.getInterfaceState().setNextDialogueId(0, 17010);
			break;
		case 17010:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, 
					"Certainly.");
			player.getInterfaceState().setNextDialogueId(0, 17011);
			break;
		case 17011:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.DURADEL, 1, 1, true);
			break;
		case 6995:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Er...nothing...");
			player.getInterfaceState().setNextDialogueId(0, 6996);
			break;
		case 6996:
			player.getActionSender().removeChatboxInterface();
			break;
		case 7000:
			player.getActionSender().sendDialogue("Estate Agent", DialogueType.NPC, 5419, FacialAnimation.DEFAULT, "'Ello mate, what can I do ye for?");
			player.getInterfaceState().setNextDialogueId(0, 7001);
			break;
		case 7001:
			player.getActionSender().sendDialogue("Select an option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"What can you teach me?|What is that cape you're wearing?|Oh, nevermind");
			player.getInterfaceState().setNextDialogueId(0, 7002);
			player.getInterfaceState().setNextDialogueId(1, 7003);
			player.getInterfaceState().setNextDialogueId(2, 7004);
			break;
		case 7002:
			player.getActionSender().sendDialogue("Estate Agent", DialogueType.NPC, 5419, FacialAnimation.DEFAULT,
					"Take this hammer and build me 2 chairs, and we'll talk.");
			break;
		case 14250:
			player.getActionSender().sendDialogue("Lesser Fanatic", DialogueType.NPC, 5514, FacialAnimation.DEFAULT,
					"Achievement diaries haven't been added yet. Please check back later!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 71:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Can you take me somewhere?|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 72);
			//player.getInterfaceState().setNextDialogueId(1, 737372);
			player.getInterfaceState().setNextDialogueId(2,  101072);
			break;
		case 101072:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Nothing.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 72:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Can you take me somewhere?");
			player.getInterfaceState().setNextDialogueId(0, 73);
			break;
		case 73:
			player.getActionSender().sendDialogue("Captain Klemfoodle", DialogueType.NPC, 6092, FacialAnimation.DEFAULT, 
					"Where to?");
			player.getInterfaceState().setNextDialogueId(0, 74);
			break;
		case 737372:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, 
					"Could you sell me some teleports?");
			player.getInterfaceState().setNextDialogueId(0, 737373);
			break;
		case 737373:
			player.getActionSender().sendDialogue("Captain Klemfoodle", DialogueType.NPC, 6092, FacialAnimation.DEFAULT, 
					"Please, browse at your leisure.");
			player.getInterfaceState().setNextDialogueId(0, 737374);
			break;
		case 737374:
			player.getActionSender().removeChatboxInterface();
			Shop.open(player, 10, 1);
			break;
		case 74:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Monsters|Cities|Minigames|Wilderness Regions|Miscellaneous");//Training Areas|Minigames|Boss Teleports|City Teleports|PvP Teleports
			player.getInterfaceState().setNextDialogueId(0, 75757575);
			player.getInterfaceState().setNextDialogueId(1, 401);
			player.getInterfaceState().setNextDialogueId(2, 76);
			player.getInterfaceState().setNextDialogueId(3, 402);
			player.getInterfaceState().setNextDialogueId(4, 403);
			break;
		case 75757575:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Training areas|Dungeons and caves|Bosses");//Training Areas|Minigames|Boss Teleports|City Teleports|PvP Teleports
			player.getInterfaceState().setNextDialogueId(0, 75);
			player.getInterfaceState().setNextDialogueId(1, 757576);
			player.getInterfaceState().setNextDialogueId(2, 77);
			break;
		case 757576:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Edgeville Dungeon|Taverly Dungeon|Brimhaven Dungeon|Waterfall Dungeon|More options...");
			player.getInterfaceState().setNextDialogueId(0, 757577);
			player.getInterfaceState().setNextDialogueId(1, 757578);
			player.getInterfaceState().setNextDialogueId(2, 757579);
			player.getInterfaceState().setNextDialogueId(3, 757580);
			player.getInterfaceState().setNextDialogueId(4, 757581);
			break;
		case 757577:
			player.teleport(Constants.EDGEVILLE_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757578:
			player.teleport(Constants.TAVERLY_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757579:
			player.teleport(Constants.BRIMHAVEN_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757580:
			player.teleport(Constants.WATERFALL_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757581:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Fremennik Slayer Dungeon|Slayer Tower|Stronghold Slayer Cave|Mos Le'Harmless Cave|More options...");
			player.getInterfaceState().setNextDialogueId(0, 757582);
			player.getInterfaceState().setNextDialogueId(1, 757583);
			player.getInterfaceState().setNextDialogueId(2, 757584);
			player.getInterfaceState().setNextDialogueId(3, 757585);
			player.getInterfaceState().setNextDialogueId(4, 757586);
			break;
		case 757582:
			player.teleport(Constants.FREMENNIK_SLAYER_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757583:
			player.teleport(Constants.SLAYER_TOWER, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757584:
			player.teleport(Constants.STRONGHOLD_SLAYER_CAVE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757585:
			player.teleport(Constants.MOS_LEHARMLESS_CAVE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757586:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Smoke Dungeon|Asgarnia Ice Dungeon|Abyssal Area|Mourner Tunnels|More options...");
			player.getInterfaceState().setNextDialogueId(0, 757587);
			player.getInterfaceState().setNextDialogueId(1, 757588);
			player.getInterfaceState().setNextDialogueId(2, 757589);
			player.getInterfaceState().setNextDialogueId(3, 757590);
			player.getInterfaceState().setNextDialogueId(4, 757594);
			break;
		case 757587:
			player.teleport(Constants.SMOKE_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757588:
			player.teleport(Constants.ICE_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757589:
			player.teleport(Constants.ABYSSAL_AREA, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757590:
			player.teleport(Constants.MOURNER_TUNNELS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757594:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Dagannoths|Troll Stronghold");
			player.getInterfaceState().setNextDialogueId(0, 757591);
			player.getInterfaceState().setNextDialogueId(1, 757595);
			break;
		case 757591:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Lighthouse Dungeon dagannoths (multi)|Waterbirth Isle dagannoths (single)");
			player.getInterfaceState().setNextDialogueId(0, 757592);
			player.getInterfaceState().setNextDialogueId(1, 757593);
			break;
		case 757592:
			player.teleport(Constants.LIGHTHOUSE_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757593:
			player.teleport(Constants.WATERBIRTH_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 757595:
			player.teleport(Constants.TROLL_STRONGHOLD, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 75:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Chickens|Sand Crabs|Experiments|Yaks|Ghouls");
			player.getInterfaceState().setNextDialogueId(0, 78);
			player.getInterfaceState().setNextDialogueId(1, 79);
			player.getInterfaceState().setNextDialogueId(2, 80);
			player.getInterfaceState().setNextDialogueId(3, 84);
			player.getInterfaceState().setNextDialogueId(4, 88);
			break;
		case 76:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Barrows|Warriors Guild|Pest Control");
			//player.getInterfaceState().setNextDialogueId(0, 81);
			player.getInterfaceState().setNextDialogueId(0, 81);
			player.getInterfaceState().setNextDialogueId(1, 86);
			player.getInterfaceState().setNextDialogueId(2, 10186);
			break;
		case 81:
			player.teleport(Constants.BARROWS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 86:
			player.teleport(Constants.WARRIORS_GUILD, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 10186:
			player.getActionSender().removeChatboxInterface();
			player.sendMessage("This miniegame hasn't been finished yet.");
			break;
		case 77:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Godwars|Dagannoth Kings|King Black Dragon|Zulrah|Lizardman Shamans");
			player.getInterfaceState().setNextDialogueId(0, 82);
			player.getInterfaceState().setNextDialogueId(1, 95);
			player.getInterfaceState().setNextDialogueId(2, 83);
			player.getInterfaceState().setNextDialogueId(3, 96);
			player.getInterfaceState().setNextDialogueId(4, 125);
			break;
		case 78:
			player.teleport(Constants.CHICKENS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 79:
			player.teleport(Constants.SAND_CRABS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 80:
			player.teleport(Constants.EXPERIMENTS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 82:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Armadyl|Bandos|Saradomin|Zamorak");
			player.getInterfaceState().setNextDialogueId(0, 90);
			player.getInterfaceState().setNextDialogueId(1, 91);
			player.getInterfaceState().setNextDialogueId(2, 87);
			player.getInterfaceState().setNextDialogueId(3, 92);
			break;
		case 83:
			player.teleport(Constants.KBD_LAIR, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 84:
			player.teleport(Constants.YAKS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 85:
			player.teleport(Constants.KBD_LAIR, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 87:
			player.teleport(Constants.SARA, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 88:
			player.teleport(Constants.GHOULS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 929299299:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Slayer Tower|Stronghold of Security|Waterfall Dungeon|Experiments|Next Page.");
			player.getInterfaceState().setNextDialogueId(0, 89);
			player.getInterfaceState().setNextDialogueId(1, 93);
			player.getInterfaceState().setNextDialogueId(2, 94);
			player.getInterfaceState().setNextDialogueId(3, 97);
			player.getInterfaceState().setNextDialogueId(4, 98);
			break;
		case 89:
			player.teleport(Constants.SLAYER_TOWER, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 90:
			player.teleport(Constants.ARMADYL, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 91:
			player.teleport(Constants.BANDOS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 92:
			player.teleport(Constants.ZAMMY, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 93:
			player.teleport(Location.create(1860, 5244, 0), 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 94:
			player.teleport(Constants.WATERFALL_DUNGEON, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 95:
			player.teleport(Constants.DAGANNOTH_KINGS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 96:
			player.teleport(Constants.ZULRAH, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 97:
			player.teleport(Location.create(3577, 9927, 0), 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 98:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Mourner Tunnels|Asgarnian Ice Dungeon|Sand Crabs|Dagannoth Lair");
			player.getInterfaceState().setNextDialogueId(0, 99);
			player.getInterfaceState().setNextDialogueId(1, 141);
			player.getInterfaceState().setNextDialogueId(2, 123);
			player.getInterfaceState().setNextDialogueId(3, 124);
			break;
		case 123:
			player.teleport(Constants.SAND_CRABS, 0, 0, true);
			break;
		case 124:
			player.teleport(Constants.DAGANNOTHS, 0, 0, true);
			break;
		case 99:
			player.setTeleportTarget(Location.create(2033, 4636));
			player.getActionSender().removeChatboxInterface();
			break;
		case 125:
			player.setTeleportTarget(Location.create(1464, 3688, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 141:
			player.setTeleportTarget(Location.create(3009, 9550, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 100:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.ATTACK_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>  Congratulations, you just advanced an Attack level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.ATTACK) + ".");
			break;
		case 101:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.DEFENCE_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080> Congratulations, you just advanced a Defence level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.DEFENCE) + ".");
			if (player.getSkills().getLevelForExperience(Skills.DEFENCE) > 98) {
				player.getInterfaceState().setNextDialogueId(0, 107);
			}
			break;
		case 102:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.STRENGTH_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Strength level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.STRENGTH) + ".");
			break;
		case 103:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.HITPOINT_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Hitpoints level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.HITPOINTS) + ".");
			break;
		case 104:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.RANGING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Ranged level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.RANGE) + ".");
			if (player.getSkills().getLevelForExperience(Skills.RANGE) > 98) {
				player.getInterfaceState().setNextDialogueId(0, 108);
			}
			break;
		case 105:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PRAYER_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Prayer level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.PRAYER) + ".");
			break;
		case 106:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.MAGIC_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Magic level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.MAGIC) + ".");
			break;
		case 107:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.COOKING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Cooking level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.COOKING) + ".");
			break;
		case 108:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.WOODCUTTING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Woodcutting level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.WOODCUTTING) + ".");
			break;
		case 109:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.FLETCHING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>  Congratulations, you just advanced a Fletching level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.FLETCHING) + ".");
			break;
		case 110:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.FISHING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Fishing level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.FISHING) + ".");
			break;
		case 111:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.FIREMAKING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Firemaking level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.FIREMAKING) + ".");
			break;
		case 112:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.CRAFTING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Crafting level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.CRAFTING) + ".");
			break;
		case 113:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.SMITHING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>  Congratulations, you just advanced a Smithing level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.SMITHING) + ".");
			break;
		case 114:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.MINING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>  Congratulations, you just advanced a Mining level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.MINING) + ".");
			break;
		case 115:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.HERBLORE_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Herblore level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.HERBLORE) + ".");
			break;
		case 116:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.AGILITY_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced an Agility level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.AGILITY) + ".");
			break;
		case 117:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.THIEVING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Thieving level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.THIEVING) + ".");
			break;
		case 118:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.SLAYER_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Slayer level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.SLAYER) + ".");
			break;
		case 119:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.FARMING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Farming level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.FARMING) + ".");
			break;
		case 120:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.RUNECRAFTING_LEVEL_UP, -1, FacialAnimation.DEFAULT,
					"<col=000080>Congratulations, you just advanced a Runecrafting level!",
					"You have now reached level " + player.getSkills().getLevelForExperience(Skills.RUNECRAFTING) + ".");
			break;

		case 122:
			player.setTeleportTarget(Location.create(2659, 2676));
			player.getActionSender().removeChatboxInterface();
			break;

		case 306:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Hello, I'm the Survival Guide, How can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 307);
			break;
		case 307:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"How do I get around the game?|How can I make money?|Where can I start training?|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 308);
			player.getInterfaceState().setNextDialogueId(1, 309);
			player.getInterfaceState().setNextDialogueId(2, 310);
			player.getInterfaceState().setNextDialogueId(3, 54);
			break;
		case 308:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "How do I get around the game?");
			player.getInterfaceState().setNextDialogueId(0, 311);
			break;
		case 309:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "How can I make money?");
			player.getInterfaceState().setNextDialogueId(0, 312);
			break;
		case 310:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Where can I start training?");
			player.getInterfaceState().setNextDialogueId(0, 313);
			break;
		case 311:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Use spell teleports, jewelry teleports and the Wizard Distentor in Falador center to access other game regions.");
			player.getInterfaceState().setNextDialogueId(0, 307);
			break;
		case 312:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Their are various methods of money methods, Skilling/PvM are the main methods to increase the amount of cash you have.");
			player.getInterfaceState().setNextDialogueId(0, 307);
			break;
		case 313:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "The basic training area's that you can use can be found by talking<br>to the Wizard in the Centre of Falador.");
			player.getInterfaceState().setNextDialogueId(0, 307);
			break;
		case 315:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Can I interest you in my skilling wares?");
			player.getInterfaceState().setNextDialogueId(0, 316);
			break;
		case 316:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes.|No thanks.");
			player.getInterfaceState().setNextDialogueId(0, 317);
			player.getInterfaceState().setNextDialogueId(0, 318);
			break;
		case 317:

		case 500:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "'Ello and what are you after then?");
			player.getInterfaceState().setNextDialogueId(0, 501);
			break;
		case 501:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I need another assignment.|Do you have anything for trade?|About the task system...|Er...nothing...");
			player.getInterfaceState().setNextDialogueId(0, 502);
			player.getInterfaceState().setNextDialogueId(1, 503);
			player.getInterfaceState().setNextDialogueId(2, 504);
			player.getInterfaceState().setNextDialogueId(3, 505);
			break;
		case 502:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "I need another assignment.");
			player.getInterfaceState().setNextDialogueId(0, 512);
			break;
		case 503:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Do you have anything for trade?");
			player.getInterfaceState().setNextDialogueId(0, 507);
			break;
		case 504:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Tell me about the Task System.|Sorry I was just leaving.");
			player.getInterfaceState().setNextDialogueId(0, 509);
			player.getInterfaceState().setNextDialogueId(1, 510);
			break;
		case 505:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Er...nothing...");
			player.getInterfaceState().setNextDialogueId(0, 506);
			break;
		case 506:
			player.getActionSender().removeChatboxInterface();
			break;
		case 507:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.HAPPY, "I have a wide selection of Slayer equipment; take a look!");
			player.getInterfaceState().setNextDialogueId(0, 508);
			break;
		case 508:
			Shop.open(player, 12, 1);
			player.getActionSender().removeChatboxInterface();
			break;
		case 509:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Tell me about the task system.");
			player.getInterfaceState().setNextDialogueId(0, 511);
			break;
		case 510:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Sorry I was just leaving.");
			player.getInterfaceState().setNextDialogueId(0, 506);
			break;
		case 511:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "There isn't much information on it now, come back later.");
			player.getInterfaceState().setNextDialogueId(0, 506);
			break;
		case 512:
			if (player.getSlayer().getSlayerTask() != null && !permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
				player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "You're still hunting " + WordUtils.capitalize(player.getSlayer().getSlayerTask().getName()) + "s; come back when you've finished your task.");
				player.getInterfaceState().setNextDialogueId(0, 506);
			} else {
				final Master master = Master.forId(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getId());
				final SlayerTask newTask = slayerService.assignTask(player, master);
				if (newTask != null) {
					player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT, "Great, you're doing great. Your new task is to kill<br>" + newTask.getTaskAmount() + " " + newTask.getName() + "s");
					player.getInterfaceState().setNextDialogueId(0, 506);
				}
			}
			break;
		case 513:
			player.getActionSender().sendDialogue(
					CacheNPCDefinition.get(player.getSlayer().getSlayerTask().getMaster().getId()).getName(), DialogueType.NPC, 
					player.getSlayer().getSlayerTask().getMaster().getId(), FacialAnimation.HAPPY, "Hello there, " + player.getName() + ", what can I help you with?");
			player.getInterfaceState().setNextDialogueId(0, 514);
			break;
		case 514:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"How am I doing so far?|Who are you?|Where are you?|Got any tips for me|Nothing really.");
			player.getInterfaceState().setNextDialogueId(0, 515);
			player.getInterfaceState().setNextDialogueId(1, 516);
			player.getInterfaceState().setNextDialogueId(2, 517);
			player.getInterfaceState().setNextDialogueId(3, 518);
			player.getInterfaceState().setNextDialogueId(4, 519);
			break;
		case 515:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "How am I doing so far?");
			player.getInterfaceState().setNextDialogueId(0, 520);
			break;
		case 516:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Who are you?");
			player.getInterfaceState().setNextDialogueId(0, 521);
			break;
		case 517:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Where are you?");
			player.getInterfaceState().setNextDialogueId(0, 522);
			break;
		case 518:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Got any tips for me?");
			player.getInterfaceState().setNextDialogueId(0, 523);
			break;
		case 519:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Nothing really.");
			player.getInterfaceState().setNextDialogueId(0, 73);
			break;
		case 520:
			if (player.getSlayer().getSlayerTask() != null) {
				player.getActionSender().sendDialogue(
						CacheNPCDefinition.get(player.getSlayer().getSlayerTask().getMaster().getId()).getName(), DialogueType.NPC, 
						player.getSlayer().getSlayerTask().getMaster().getId(), FacialAnimation.HAPPY, "You're current assigned to kill " + player.getSlayer().getSlayerTask().getName().toLowerCase() + "; only " + player.getSlayer().getSlayerTask().getTaskAmount() + " more", "to go.");
				player.getInterfaceState().setNextDialogueId(0, 514);
			} else {
				player.getActionSender().sendDialogue(
						CacheNPCDefinition.get(player.getSlayer().getSlayerTask().getMaster().getId()).getName(), DialogueType.NPC, 
						player.getSlayer().getSlayerTask().getMaster().getId(), FacialAnimation.HAPPY, "You currently have no task, come to me so I can assign you one.");
				player.getInterfaceState().setNextDialogueId(0, 514);
			}
			break;
		case 521:
			player.getActionSender().sendDialogue(
					CacheNPCDefinition.get(player.getSlayer().getSlayerTask().getMaster().getId()).getName(), DialogueType.NPC, 
					player.getSlayer().getSlayerTask().getMaster().getId(), FacialAnimation.HAPPY, "My name's Vannaka; I'm a Slayer Master.");
			player.getInterfaceState().setNextDialogueId(0, 514);
			break;
		case 522:
			player.getActionSender().sendDialogue(
					CacheNPCDefinition.get(player.getSlayer().getSlayerTask().getMaster().getId()).getName(), DialogueType.NPC, 
					player.getSlayer().getSlayerTask().getMaster().getId(), FacialAnimation.HAPPY, "You'll find me in the city of Edgeville.<br>I'll be here when you need a new task.");
			player.getInterfaceState().setNextDialogueId(0, 514);
			break;
		case 523:
			player.getActionSender().sendDialogue(
					CacheNPCDefinition.get(player.getSlayer().getSlayerTask().getMaster().getId()).getName(), DialogueType.NPC, 
					player.getSlayer().getSlayerTask().getMaster().getId(), FacialAnimation.HAPPY, "At the moment, no.");
			player.getInterfaceState().setNextDialogueId(0, 514);
			break;
		case 3666:
			player.getActionSender().sendDialogue("Prince Brand", DialogueType.NPC, 3666, FacialAnimation.HAPPY, "Oh thank you for supporting my kingdom, what can I do for you?");
			player.getInterfaceState().setNextDialogueId(0, 3667);
			break;
		case 3667:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"What is this place?|I'd like to browse your shop.|Nothing, I'll be going now.");
			player.getInterfaceState().setNextDialogueId(0, 3668);
			player.getInterfaceState().setNextDialogueId(1, 3669);
			player.getInterfaceState().setNextDialogueId(2, 3670);
			break;
		case 3668:
			player.getActionSender().sendDialogue("Prince Brand", DialogueType.NPC, 3666, FacialAnimation.HAPPY, "This land is the Extreme Donator Zone, only for the most elite supporters of , you'll find an abundance of activities you can do here.");
			player.getInterfaceState().setNextDialogueId(0, 3671);
			break;
		case 3671:
			player.getActionSender().sendDialogue("Prince Brand", DialogueType.NPC, 3666, FacialAnimation.HAPPY, "Most skills are trainable here but beware; some areas you will be vulnerable to player attacks!");
			player.getInterfaceState().setNextDialogueId(0, 3667);
			break;
		case 3669:
			player.getActionSender().sendDialogue("Prince Brand", DialogueType.NPC, 3666, FacialAnimation.HAPPY, "Oh of course, I've got a few items you may enjoy..");
			Shop.open(player, 14, 0);
			player.getActionSender().removeChatboxInterface();
			break;
		case 3670:
			player.getActionSender().sendDialogue("Prince Brand", DialogueType.NPC, 3666, FacialAnimation.HAPPY, "Farewell, come back soon.");
			player.getActionSender().removeChatboxInterface();
			break;
		case 687:
			player.getActionSender().sendDialogue("Cook", DialogueType.NPC, 4626, FacialAnimation.HAPPY, 
					"You were brilliant! You sure showed the Culinaromancer who's boss.");
			player.getInterfaceState().setNextDialogueId(0, 10688);
			break;
		case 10688:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Right... What exactly did I do again?");
			player.getInterfaceState().setNextDialogueId(0, 10699);
			break;
		case 10699:
			player.getActionSender().sendDialogue("Cook", DialogueType.NPC, 4626, FacialAnimation.HAPPY, 
					"I teleported you to the Culinaromancer. The Culinaromancer's creations came at you one by "
					+ "one and one by one you beat them all! Then he came for you himself...");
			player.getInterfaceState().setNextDialogueId(0, 10700);
			break;
		case 10700:
			player.getActionSender().sendDialogue("Cook", DialogueType.NPC, 4626, FacialAnimation.HAPPY, 
					"But that was no problem for you, you defeated the great culinaromancer himself! Impressive, truly. As I recall,"
					+ "every time you defeated a creature, a new set of metal gloves appeared before me.");
			player.getInterfaceState().setNextDialogueId(0, 10701);
			break;
		case 10701:
			player.getActionSender().sendDialogue("Cook", DialogueType.NPC, 4626, FacialAnimation.HAPPY, 
					"It was really quite bizzare... I'll happily sell you some of these metal gloves, if you'd like.");
			player.getInterfaceState().setNextDialogueId(0, 688);
			break;
		case 688:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I'd like to go back to where I fought the culinaromancer.|I'd like to buy some metal gloves.|Goodbye.");
			player.getInterfaceState().setNextDialogueId(0, 689);
			player.getInterfaceState().setNextDialogueId(1, 690);
			player.getInterfaceState().setNextDialogueId(2, -1);
			break;
		case 689:
			player.getRFD().start();
			player.getActionSender().removeChatboxInterface();
			break;
		case 690:
			Shop.open(player, 13, 2);
			player.getActionSender().removeChatboxInterface();
			break;
	//	case 691:
		//	player.getActionSender().removeChatboxInterface();
		//	break;
		case 822:
			player.getActionSender().sendDialogue("Oziach", DialogueType.NPC, 822, FacialAnimation.HAPPY, "Hello, What can I do for you?");
			player.getInterfaceState().setNextDialogueId(0, 823);
			break;
		case 823:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Can you combine my draconic visage and my anti-fire shield?|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 824);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 824:
			boolean hasItems = player.getInventory().contains(11286) && player.getInventory().contains(1540);
			if (hasItems) {
				player.getActionSender().sendDialogue("Oziach", DialogueType.NPC, 822, FacialAnimation.HAPPY, "Sure! It'll cost you 750,000 coins. Are you sure you want to pay this?");
				player.getInterfaceState().setNextDialogueId(0, 825);
			} else {
				player.getActionSender().sendDialogue("Oziach", DialogueType.NPC, 822, FacialAnimation.HAPPY, "You don't seem to have the visage and anti-fire shield with you.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			}
			break;
		case 825:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes please|No");
			player.getInterfaceState().setNextDialogueId(0, 826);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 826:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Yes please");
			player.getInterfaceState().setNextDialogueId(0, 827);
			break;
		case 827:
			hasItems = player.getInventory().contains(11286) && player.getInventory().contains(1540) && player.getInventory().getCount(995) >= 750000;
			if (hasItems) {
				player.getActionSender().removeChatboxInterface();
				player.getInventory().remove(new Item(11286, 1));
				player.getInventory().remove(new Item(1540, 1));
				player.getInventory().remove(new Item(995, 750000));
				player.getInventory().add(new Item(11283, 1));
				player.getActionSender().sendMessage("Oziach takes the items and combines them for you.");
			} else {
				player.getActionSender().sendDialogue("Oziach", DialogueType.NPC, 822, FacialAnimation.HAPPY, "You don't seem to have all the required items, Come back when you have them.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			}
			break;
		case 2461:
			String message = "I will release some cyclops to drop the next<br>defender for you.";
			int next = 2462;
			if (player.getInventory().getCount(WarriorsGuild.TOKENS) < 100 && !Constants.hasAttackCape(player)) {
				message = "You don't have enough tokens to enter.";
				next = 2463;
			}
			player.getActionSender().sendDialogue("Kamfreena", DialogueType.NPC, 2461, FacialAnimation.HAPPY, message);
			player.getInterfaceState().setNextDialogueId(0, next);
			break;
		case 2462:
			player.getWarriorsGuild().handleDoorClick(WarriorsGuild.GAME_DOOR_1);
			player.getActionSender().removeChatboxInterface();
			break;
		case 2463:
			player.getActionSender().removeChatboxInterface();
			break;
		case 1603:
			if (player.getSettings().completedMageArena()) {
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Hello, Kolodion.");
				player.getInterfaceState().setNextDialogueId(0, 1621);
			} else {
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Hello there. What is this place?");
				player.getInterfaceState().setNextDialogueId(0, 1604);
			}
			break;
		case 2414:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, 
					"Impressive work in the arena, mage. As a reward I will grant you a staff infused with the power of one of the Gods. Which will you use?");
			player.getInterfaceState().setNextDialogueId(0, 2415);
			break;
		case 2415:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Saradomin Staff|Guthix Staff|Zamorak Staff");
			player.getInterfaceState().setNextDialogueId(0, 2416);
			player.getInterfaceState().setNextDialogueId(1, 2417);
			player.getInterfaceState().setNextDialogueId(2, 2418);
			break;
		case 2416:
			if(player.getInventory().freeSlots() > 0)
				player.getInventory().add(new Item(2415));
			else
			World.getWorld().createGroundItem(new GroundItem(player.getName(), new Item(2415), Location.create(player.getLocation().getX(), player.getLocation().getY())),
						player);
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, 
					"You have chosen the Saradomin Staff. A wise choice.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 2417:
			if(player.getInventory().freeSlots() > 0)
				player.getInventory().add(new Item(2416));
			else
			World.getWorld().createGroundItem(new GroundItem(player.getName(), new Item(2416), Location.create(player.getLocation().getX(), player.getLocation().getY())),
						player);
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, 
					"You have chosen the Guthix Staff. A wise choice.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 2418:
			if(player.getInventory().freeSlots() > 0)
				player.getInventory().add(new Item(2417));
			else
			World.getWorld().createGroundItem(new GroundItem(player.getName(), new Item(2417), Location.create(player.getLocation().getX(), player.getLocation().getY())),
						player);
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, 
					"You have chosen the Zamorak Staff. A wise choice.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 1604:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "I am the great Kolodion, master of battle magic, and<br>this is my battle arena. Top wizards travel from all over<br>Survival to fight here.");
			player.getInterfaceState().setNextDialogueId(0, 1605);
			break;
		case 1605:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Can I fight here?|Fairwell!");
			player.getInterfaceState().setNextDialogueId(0, 1606);
			player.getInterfaceState().setNextDialogueId(1, 1607);
			break;
		case 1606:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Can I fight here?");
			player.getInterfaceState().setNextDialogueId(0, 1608);
			break;
		case 1607:
			player.getActionSender().removeChatboxInterface();
			break;
		case 1608:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "My arena is open to any high level wizard, but this is<br>no game. Many wizards fall in this arena, never to rise<br>again. The strongest mages have been destroyed.");
			player.getInterfaceState().setNextDialogueId(0, 1609);
			break;
		case 1609:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "If you're sure you want in?");
			player.getInterfaceState().setNextDialogueId(0, 1610);
			break;
		case 1610:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes indeedy.|No I don't.");
			player.getInterfaceState().setNextDialogueId(0, 1611);
			player.getInterfaceState().setNextDialogueId(1, 1607);
			break;
		case 1611:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Yes indeedy.");
			player.getInterfaceState().setNextDialogueId(0, 1612);
			break;
		case 1612:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "Good, good. You have a healthy sense of competition.");
			player.getInterfaceState().setNextDialogueId(0, 1613);
			break;
		case 1613:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "Remember, traveller - in my arena, hand-to-hand<br>combat is useless. Your strength will diminish as you<br>enter the arena, but the spells you can learn are<br>amonst the most powerful in all of Survival.");
			player.getInterfaceState().setNextDialogueId(0, 1614);
			break;
		case 1614:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "Before I can accept you in, we must duel.");
			player.getInterfaceState().setNextDialogueId(0, 1615);
			break;
		case 1615:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Okay, let's fight.|No thanks.");
			player.getInterfaceState().setNextDialogueId(0, 1616);
			player.getInterfaceState().setNextDialogueId(1, 1607);
			break;
		case 1616:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Okay, let's fight.");
			player.getInterfaceState().setNextDialogueId(0, 1617);
			break;
		case 1617:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "I must first check that you are up to scratch.");
			if (player.getSkills().getLevelForExperience(Skills.MAGIC) > 59) {
				player.getInterfaceState().setNextDialogueId(0, 1618);
			} else {
				player.getInterfaceState().setNextDialogueId(0, 1626);
			}
			break;
		case 1618:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "You don't need to worry about that.");
			player.getInterfaceState().setNextDialogueId(0, 1619);
			break;
		case 1619:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "Not just any magician can enter - only the most<br>powerfl and most feared. Before you can use the<br>power of this arena, you must prove yourself against me.");
			player.getInterfaceState().setNextDialogueId(0, 1620);
			break;
		case 1620:
			player.getActionSender().removeChatboxInterface();
			player.getMageArena().start();
			break;
		case 1621:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "Hello, young mage. You're a tough one.");
			player.getInterfaceState().setNextDialogueId(0, 1622);
			break;
		case 1622:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "What now?");
			player.getInterfaceState().setNextDialogueId(0, 1623);
			break;
		case 1623:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "Step into the magic pool. It will take you to a chamber.<br>There, you must decide which god you will represent in<br>the arena.");
			player.getInterfaceState().setNextDialogueId(0, 1624);
			break;
		case 1624:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.HAPPY, "Thanks, Kolodion");
			player.getInterfaceState().setNextDialogueId(0, 1625);
			break;
		case 1625:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "That's what I'm here for.");
			player.getInterfaceState().setNextDialogueId(0, 1607);
			break;
		case 1626:
			player.getActionSender().sendDialogue("Kolodion", DialogueType.NPC, 1603, FacialAnimation.HAPPY, "You don't seem to be a powerful enough magician yet.");
			player.getInterfaceState().setNextDialogueId(0, 1607);
			break;
		case 1712:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Edgeville|Karamja|Draynor Village|Al Kharid|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 1713);
			player.getInterfaceState().setNextDialogueId(1, 1714);
			player.getInterfaceState().setNextDialogueId(2, 1715);
			player.getInterfaceState().setNextDialogueId(3, 1716);
			player.getInterfaceState().setNextDialogueId(4, 1717);
			break;
		case 11105:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Fishing Guild|Mining Guild|Crafting Guild|Cooking Guild|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 11106);
			player.getInterfaceState().setNextDialogueId(1, 11107);
			player.getInterfaceState().setNextDialogueId(2, 11108);
			player.getInterfaceState().setNextDialogueId(3, 11109);
			player.getInterfaceState().setNextDialogueId(4, -1);
			break;
		case 11106:
			player.getJewellery().gemTeleport(player, Location.create(2611, 3391, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 11107:
			player.getJewellery().gemTeleport(player, Location.create(3022, 3338, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 11108:
			player.getJewellery().gemTeleport(player, Location.create(2933, 3285, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 11109:
			player.getJewellery().gemTeleport(player, Location.create(3142, 3441, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 1713:
			player.getJewellery().gemTeleport(player, Location.create(3089, 3496, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 1714:
			player.getJewellery().gemTeleport(player, Location.create(2918, 3176, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 1715:
			player.getJewellery().gemTeleport(player, Location.create(3105, 3249, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 1716:
			player.getJewellery().gemTeleport(player, Location.create(3293, 3163, 0));
			player.getActionSender().removeChatboxInterface();
			break;
		case 1717:
			player.getActionSender().removeChatboxInterface();
			break;
		case 1718:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Burthorpe Games Room|Barbarian Outpost|Nowhere");
			player.getInterfaceState().setNextDialogueId(0, 1719);
			player.getInterfaceState().setNextDialogueId(1, 1720);
			player.getInterfaceState().setNextDialogueId(2, 1721);
			break;
		case 1719:
			player.getJewellery().gemTeleport(player, Location.create(2926, 3559, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1720:
			player.getJewellery().gemTeleport(player, Location.create(2525, 3576, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1721:
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1722:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Al-Kharid Duel Arena|Castle wars Arena|Clan wars Arena|Nowhere");
			player.getInterfaceState().setNextDialogueId(0, 1724);
			player.getInterfaceState().setNextDialogueId(1, 1723);
			player.getInterfaceState().setNextDialogueId(2, 1725);
			player.getInterfaceState().setNextDialogueId(3, 1726);
			break;
		case 1723:
			player.getJewellery().gemTeleport(player, Location.create(2440, 3089, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1724:
			player.getJewellery().gemTeleport(player, Location.create(3316, 3235, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1725:
			player.getJewellery().gemTeleport(player, Location.create(3369, 3169, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1726:
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;

		case 1755:
			player.getActionSender().sendDialogue("Void Knight", DialogueType.NPC, 1755, FacialAnimation.HAPPY, "Would you like to upgrade your ring for 500 Pest Control Points?");
			player.getInterfaceState().setNextDialogueId(0, 1756);
			break;
		case 1756:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No");
			player.getInterfaceState().setNextDialogueId(0, 1757);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 1757:
			int pcPoints = player.getDatabaseEntity().getStatistics().getPestControlPoints();
			if (pcPoints < 500) {
				player.getActionSender().sendDialogue("Void Knight", DialogueType.NPC, 1755, FacialAnimation.ANGER_1, "You don't seem to have enough Pest Control Points to upgrade.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				if (player.getInterfaceAttribute("ring") != null &&
						player.getInventory().contains(player.getInterfaceAttribute("ring"))) {
					int item = player.getInterfaceAttribute("ring");
					Optional<Constants.ImbuedRings> ring = Constants.ImbuedRings.of(item);
					if (ring.isPresent()) {
						player.getActionSender().removeChatboxInterface();
						player.getInventory().remove(new Item(item));
						player.getInventory().add(new Item(ring.get().getImbued()));
						player.getDatabaseEntity().getStatistics().setPestControlPoints(pcPoints - 500);
						player.getActionSender().sendMessage("You imbue your " + CacheItemDefinition.get(item).name + " for 500 Pest Control Points.");
					}
				}
			}
			break;
		case 2000:
			boolean trimmed = player.trimmed();
			boolean skillMaster = player.getSkills().getLevelForExperience(getSkillId((int) player.getAttribute("talkingNpc"))) == 99;
			Item cape = null;
			Item hood = null;
			switch ((int) player.getAttribute("talkingNpc")) {
			case 2460://ajjat
				player.getActionSender().sendDialogue("Ajjat", DialogueType.NPC, 2460, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Attack skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in attack.");
				cape = new Item(trimmed ? 9748 : 9747, 1);
				hood = new Item(9749, 1);
				break;
			case 3216://melee tutor
				player.getActionSender().sendDialogue("Melee combat tutor", DialogueType.NPC, 3216, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Defence skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in defence.");
				cape = new Item(trimmed ? 9754 : 9753, 1);
				hood = new Item(9755, 1);
				break;
			case 2473://sloane
				player.getActionSender().sendDialogue("Sloane", DialogueType.NPC, 2473, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Strength skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in strength.");
				cape = new Item(trimmed ? 9751 : 9750, 1);
				hood = new Item(9752, 1);
				break;
			case 6059://armour salesman
				player.getActionSender().sendDialogue("Armour salesman", DialogueType.NPC, 6059, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Ranged skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in ranged.");
				cape = new Item(trimmed ? 9757 : 9756, 1);
				hood = new Item(9758, 1);
				break;
			case 2578://brother jered
				player.getActionSender().sendDialogue("Brother Jered", DialogueType.NPC, 2578, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Prayer skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in hitpoints.");
				cape = new Item(trimmed ? 9759 : 9760, 1);
				hood = new Item(9761, 1);
				break;
			case 2658://head chef
				player.getActionSender().sendDialogue("Head Chef", DialogueType.NPC, 2658, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Cooking skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in cooking.");
				cape = new Item(trimmed ? 9802 : 9801, 1);
				hood = new Item(9803, 1);
				break;
			case 1044://hickton
				player.getActionSender().sendDialogue("Hickton", DialogueType.NPC, 1044, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Fletching skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in fletching.");
				cape = new Item(trimmed ? 9784 : 9783, 1);
				hood = new Item(9785, 1);
				break;
			case 118://ignatius vulcan
				player.getActionSender().sendDialogue("Ignatius Vulcan", DialogueType.NPC, 118, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Firemaking skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in firemaking.");
				cape = new Item(trimmed ? 9805 : 9804, 1);
				hood = new Item(9806, 1);
				break;
			case 5045://Kaqemeex
				if (skillMaster) {
					player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
							"Can I see your shop?|Can I purchase a cape of Herblore");
					player.getInterfaceState().setNextDialogueId(0, 2003);
					player.getInterfaceState().setNextDialogueId(1, 2004);
				} else {
					player.getActionSender().removeChatboxInterface();
					Shop.open(player, 29, 0);
				}
				return;
			case 3193://Martin Thwait
				player.getActionSender().sendDialogue("Martin Thwait", DialogueType.NPC, 3193, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Thieving skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in thieving.");
				cape = new Item(trimmed ? 9778 : 9777, 1);
				hood = new Item(9779, 1);
				break;
			case 5810://Master Crafter
				player.getActionSender().sendDialogue("Master Crafter", DialogueType.NPC, 5810, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Crafting skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in crafting.");
				cape = new Item(trimmed ? 9781 : 9780, 1);
				hood = new Item(9782, 1);
				break;
			case 2913://Master fisher
				player.getActionSender().sendDialogue("Master fisher", DialogueType.NPC, 2913, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Fishing skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in fishing.");
				cape = new Item(trimmed ? 9799 : 9798, 1);
				hood = new Item(9800, 1);
				break;
			case 3249://Robe Store ADMINISTRATOR
				player.getActionSender().sendDialogue("Robe Store ADMINISTRATOR", DialogueType.NPC, 3249, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Magic skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in magic.");
				cape = new Item(trimmed ? 9763 : 9762, 1);
				hood = new Item(9764, 1);
				break;
			case 3343://Surgeon General Tafani
				player.getActionSender().sendDialogue("Surgeon General Tafani", DialogueType.NPC, 3343, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Hitpoints skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in hitpoints.");
				cape = new Item(trimmed ? 9769 : 9768, 1);
				hood = new Item(9770, 1);
				break;
			case 4733://Thurgo
				player.getActionSender().sendDialogue("Thurgo", DialogueType.NPC, 4733, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Smithing skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in smithing.");
				cape = new Item(trimmed ? 9796 : 9795, 1);
				hood = new Item(9797, 1);
				break;
			case 3226://Woodsman tutor
				player.getActionSender().sendDialogue("Woodsman tutor", DialogueType.NPC, 3226, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Woodcutting skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in woodcutting.");
				cape = new Item(trimmed ? 9808 : 9807, 1);
				hood = new Item(9809, 1);
				break;
			case 405://Duradel
				player.getActionSender().sendDialogue("Duradel", DialogueType.NPC, 405, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Slayer skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in slayer.");
				cape = new Item(trimmed ? 9787 : 9786, 1);
				hood = new Item(9788, 1);
				break;
			case 3363:
				player.getActionSender().sendDialogue("Dwarf", DialogueType.NPC, 3363, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Mining skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in mining.");
				cape = new Item(trimmed ? 9793 : 9792, 1);
				hood = new Item(9794, 1);
				break;
			case 5832:
				player.getActionSender().sendDialogue("Martin the Master Gardener", DialogueType.NPC, 5832, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Farming skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in farming.");
				cape = new Item(trimmed ? 9811 : 9810, 1);
				hood = new Item(9812, 1);
				break;
			case 637:
				player.getActionSender().sendDialogue("Aubury", DialogueType.NPC, 637, FacialAnimation.HAPPY, skillMaster ? "You seem to be a master of the Runecrafting skill do you want a skillcape for 99k?" : "Come speak to me when you're a master in runecrafting.");
				cape = new Item(trimmed ? 9766 : 9765, 1);
				hood = new Item(9767, 1);
				break;
			}
			player.getInterfaceState().setNextDialogueId(0, skillMaster ? 2001 : -1);
			player.setAttribute("cape", cape);
			player.setAttribute("hood", hood);
			break;
		case 2001:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 2002);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 2002:
			player.getActionSender().removeChatboxInterface();
			boolean hasGold = player.getInventory().getCount(995) > 99000;
			if (hasGold) {
				if (player.hasAttribute("cape") && player.hasAttribute("hood")) {
					if (player.getInventory().add(player.getAttribute("cape")) && player.getInventory().add(player.getAttribute("hood"))) {
						player.getInventory().remove(new Item(995, 99000));
						player.getActionSender().sendMessage("You purchase an attack skillcape for 99k");
					}
					player.removeAttribute("cape");
					player.removeAttribute("hood");
				}
			} else {
				player.getActionSender().sendMessage("Not enough coins to purchase this.");
			}
			break;
		case 2003:
			player.getActionSender().removeChatboxInterface();
			Shop.open(player, 29, 0);
			break;
		case 2004:
			trimmed = player.trimmed();
			player.getActionSender().sendDialogue("Kaqemeex", DialogueType.NPC, 5045, FacialAnimation.HAPPY, "You seem to be a master of the Herblore skill do you want a skillcape for 99k?");
			cape = new Item(trimmed ? 9775 : 9774, 1);
			hood = new Item(9776, 1);
			player.getInterfaceState().setNextDialogueId(0, 2001);
			player.setAttribute("cape", cape);
			player.setAttribute("hood", hood);
			break;

		case 2180:
			player.getActionSender().sendDialogue("Tzhaar-Mej-Jal", DialogueType.NPC, 2180, FacialAnimation.HAPPY, "Hello would you like to gamble your fire capes?");
			player.getInterfaceState().setNextDialogueId(0, 2181);
			break;
		case 2181:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 2182);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 2182:
			player.getActionSender().sendDialogue("Tzhaar-Mej-Jal", DialogueType.NPC, 2180, FacialAnimation.HAPPY, "How many would you like to gamble?");
			player.getInterfaceState().setNextDialogueId(0, 2183);
			break;
		case 2183:
			player.getActionSender().removeChatboxInterface();
			player.getActionSender().sendEnterAmountInterface();
			player.setInterfaceAttribute("gamble_firecape", true);
			break;

		case 9000:
			player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.HAPPY, "Welcome to Survival, I am Adam I can transform you into a Iron man if you'd wish?");
			player.getInterfaceState().setNextDialogueId(0, 9001);
			break;
		case 550:
			player.getActionSender().sendDialogue("Overseer", DialogueType.NPC, 5886, FacialAnimation.HAPPY, "Hello, human. Bring me your Bludgeon axon, bludgeon spine, and your bludgeon claw.");
			player.getInterfaceState().setNextDialogueId(0, 551);
			break;
		case 551:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Okay!|No.");
			player.getInterfaceState().setNextDialogueId(0, 552);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 552:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(13275) && player.getInventory().contains(13276) && player.getInventory().contains(13274)) {
				player.getInventory().remove(new Item(13275));
				player.getInventory().remove(new Item(13276));
				player.getInventory().remove(new Item(13274));
				player.getInventory().add(new Item(13263));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 13263, null, "The Overseer merges your parts together and Hands you a Abyssal Bludgeon.");
			}
			break;
		case 660:
			player.getActionSender().sendDialogue(" ", DialogueType.MESSAGE_MODEL_LEFT, 13273, FacialAnimation.HAPPY, "You place the Unsired into the Font of Consumption...");
			player.getInterfaceState().setNextDialogueId(0, 661);
			break;
		case 661:
			player.getActionSender().sendDialogue(" ", DialogueType.MESSAGE_MODEL_LEFT, 13273, FacialAnimation.HAPPY, "The Font consumes the Unsired and returns you a reward.");
			break;
		case 9001:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 9002);
			player.getInterfaceState().setNextDialogueId(1, 9003);
			break;
		case 9002:
			player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.HAPPY, "Are you sure? If you become a Iron man your account will be restricted.");
			player.getInterfaceState().setNextDialogueId(0, 9004);
			break;
		case 9003:
			player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.HAPPY, "That's fine feel free to speak to the Guide to learn about Survival!");
			player.removeAttribute("busy");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 9004:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 9005);
			player.getInterfaceState().setNextDialogueId(1, 9006);
			break;
		case 9005:
			player.removeAttribute("busy");
			player.getActionSender().sendDialogue("", DialogueType.MESSAGE, -1, null,
					"You are now an Iron Man.");
			permissionService.give(player, PermissionService.PlayerPermissions.IRON_MAN);
			break;
		case 9006:
			player.removeAttribute("busy");
			player.getActionSender().removeChatboxInterface();
			break;

		case 1340:
			if (permissionService.isAny(player, PermissionService.PlayerPermissions.ADMINISTRATOR, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
				player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"Edit Name Color|I'd like to see the donator shop|Collect donation");
				player.getInterfaceState().setNextDialogueId(0, 1341);
				player.getInterfaceState().setNextDialogueId(1, 1342);
				player.getInterfaceState().setNextDialogueId(2, 1347);
			} else {
				player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"I'd like to claim my donation.|I'd like to see the donator shop|Who are you?");
				player.getInterfaceState().setNextDialogueId(0, 1347);
				player.getInterfaceState().setNextDialogueId(1, 1342);
				player.getInterfaceState().setNextDialogueId(2, 1348);
			}
			break;
		case 1341:
			if (permissionService.isAny(player, PermissionService.PlayerPermissions.ADMINISTRATOR, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
				player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"Red|Blue|Pink|White|Black");
				player.getInterfaceState().setNextDialogueId(0, 1342);
				player.getInterfaceState().setNextDialogueId(1, 1343);
				player.getInterfaceState().setNextDialogueId(2, 1344);
				player.getInterfaceState().setNextDialogueId(3, 1345);
				player.getInterfaceState().setNextDialogueId(4, 1346);
			}
			break;
		case 103364:
			player.getActionSender().sendDialogue("Dwarf", DialogueType.NPC, 3363, FacialAnimation.HAPPY, "You need a mining level of 60 to go down there.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 1342:
			player.getActionSender().removeChatboxInterface();
			//				Shop.open(player, 41, 0);
			player.setNameColor("<col=FF0000>");
			break;
		case 1343:
			player.getActionSender().removeChatboxInterface();
			player.setNameColor("<col=0000FF>");
			break;
		case 1344:
			player.getActionSender().removeChatboxInterface();
			player.setNameColor("<col=FF69B4>");
			break;
		case 1345:
			player.getActionSender().removeChatboxInterface();
			player.setNameColor("<col=FFFFFF>");
			break;
		case 1346:
			player.getActionSender().removeChatboxInterface();
			player.setNameColor("");
			break;
		/*case 1347:
			if (!DonationManager.rspsdata(player, player.getName())) {
				player.getActionSender().sendDialogue("Matthias", DialogueType.NPC, 1340, FacialAnimation.HAPPY, "You don't seem to have anything waiting for you.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			}
			break;*/
		case 1348:
			player.getActionSender().sendDialogue("Matthias", DialogueType.NPC, 1340, FacialAnimation.HAPPY, "Hello, I am a servant to donators,<br>Become a donator today and enjoy all the awesome benefits that come with it!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;

			/* Ranged combat tutor */
		case 1349:
			player.getActionSender().sendDialogue("Ranged combat tutor", DialogueType.NPC, 3217, FacialAnimation.DEFAULT,
					"Here's your ranged starter kit. You only receive one; so put it go good use.");
			break;
		case 1350:
			player.getActionSender().sendDialogue("Ranged combat tutor", DialogueType.NPC, 3217, FacialAnimation.DEFAULT,
					"You have already received your ranged starter kit.");
			break;

			/* Magic combat tutor */
		case 1351:
			player.getActionSender().sendDialogue("Magic combat tutor", DialogueType.NPC, 3218, FacialAnimation.DEFAULT,
					"Here's your magic starter kit. You only receive one; so put it to good use.");
			break;
		case 1352:
			player.getActionSender().sendDialogue("Magic combat tutor", DialogueType.NPC, 3218, FacialAnimation.DEFAULT,
					"You have already received your magic starter kit.");
			break;
		case 1353: // Slayer ring
			/*if (Location.getWildernessLevel(player, player.getLocation()) > 20) {
					player.getActionSender().sendMessage("You cannot use this above level 20 Wilderness.");
					player.getActionSender().removeChatboxInterface();
					return;
				}*/
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Slayer Cave|Cave Horrors|Abyssal Demons");
			player.getInterfaceState().setNextDialogueId(0, 1354);
			player.getInterfaceState().setNextDialogueId(1, 1355);
			player.getInterfaceState().setNextDialogueId(2, 1356);
			player.getInterfaceState().setNextDialogueId(3, 1357);
			break;
		case 1354:
			player.getJewellery().gemTeleport(player, Location.create(2438, 9822, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1355:
			player.getJewellery().gemTeleport(player, Location.create(3747, 9374, 0));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1356:
			player.getJewellery().gemTeleport(player, Location.create(3424, 3549, 2));
			player.getActionSender().removeAllInterfaces().removeChatboxInterface();
			break;
		case 1357:
			break;

				/*SKIPPY*/
			case 101370:
				player.getActionSender().sendDialogue("Skippy", DialogueType.NPC, 3320, FacialAnimation.DEFAULT,
						"Hey. Do you wanna skip the Tutorial? If you do, you won't be able to come back here afterwards."
						+ " It's a one-way trip. What do you say?");
				player.getInterfaceState().setNextDialogueId(0, 101371);
				break;
			case 101371:
				player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"Send me to the mainland now.|Who are you?|Can I decide later?|I'll stay here for the Tutorial.");
				player.getInterfaceState().setNextDialogueId(0, 101372);
				player.getInterfaceState().setNextDialogueId(1, 101382);
				player.getInterfaceState().setNextDialogueId(2, 101392);
				player.getInterfaceState().setNextDialogueId(3, 101402);
				break;
			case 101372:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"Send me to the mainland now.");
				player.getInterfaceState().setNextDialogueId(0, 101373);
				break;
			case 101373:				
				player.getActionSender().sendDialogue("Skippy", DialogueType.NPC, 3320, FacialAnimation.DEFAULT,
					"Are you sure? You can't change your gamemode after leaving this island. So if you've still yet to do that, "
					+ "I advise talking to either Paul, Adam or Jaun now before leaving.");
			player.getInterfaceState().setNextDialogueId(0, 101374);
				break;
			case 101374:
				player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"I'm happy with my gamemode. Send me to the mainland.|I tihnk I'll stay a little longer to review my gamemode.");
				player.getInterfaceState().setNextDialogueId(0, 101375);
				player.getInterfaceState().setNextDialogueId(1, 101377);
				break;
			case 101375:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"I am happy with my gamemode. I'd like to go to the mainland.");
				player.getInterfaceState().setNextDialogueId(0, 101376);
				break;
			case 101376:
				player.getActionSender().sendDialogue("Skippy", DialogueType.NPC, 3320, FacialAnimation.DEFAULT,
						"As you wish.");
				player.getInterfaceState().setNextDialogueId(0, 101380);
				break;
			case 101377:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"Actually, I think I'l stay here for a bit longer to review my gamemode.");
				player.getInterfaceState().setNextDialogueId(0, 101378);
				break;
			case 101378:
				player.getActionSender().sendDialogue("Skippy", DialogueType.NPC, 3320, FacialAnimation.DEFAULT,
						"Talk to me again if you wish to leave.");
				player.getInterfaceState().setNextDialogueId(0, -1);
				break;
			case 101380:
		       	player.getActionSender().removeChatboxInterface();
				player.setTeleportTarget(Entity.HOME);
				
				boolean starter_allowed = player.getAttribute("starter");
				
				 if (starter_allowed) {
			            String IP = player.getSession().getRemoteAddress().toString().split(":")[0].replaceFirst("/", "");
			            int count = StarterMap.getSingleton().getCount(IP);
			            if (count <= Constants.MAX_STARTER_COUNT) {
							for (Item startItems : Constants.STARTER_ITEMS) {
			                    player.getInventory().add(startItems);
			                }
			            }
			        }
				break;
			case 101382:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"Who are you?");
				player.getInterfaceState().setNextDialogueId(0, -1);
				break;
			case 101392:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"Can I decide later?");
				player.getInterfaceState().setNextDialogueId(0, -1);
				break;
			case 101402:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"I'll stay here for the Tutorial.");
				player.getInterfaceState().setNextDialogueId(0, -1);
				break;
		/*ADAM*/
		case 101360:
			player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.DEFAULT,
					"Hello, how can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 101361);
			break;
			/*PAUL*/
			case 1360:
				player.getActionSender().sendDialogue("Paul", DialogueType.NPC, 317, FacialAnimation.DEFAULT,
						"Hello, how can I help you?");
				player.getInterfaceState().setNextDialogueId(0, 101362);
				break;
			/*Juan*/
		case 111360:
			player.getActionSender().sendDialogue("Juan", DialogueType.NPC, 3369, FacialAnimation.DEFAULT,
					"Hello, how can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 1101360);
			break;
		case 101361:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I want to become an Iron Man|I want to claim my Iron Man armour|You can't, nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 1362);
			player.getInterfaceState().setNextDialogueId(1, 101363); //im
			player.getInterfaceState().setNextDialogueId(2, -1);
			break;
		case 101362:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I want to become an Iron Man|I want to claim my Iron Man armour|You can't, nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 1362);
			player.getInterfaceState().setNextDialogueId(1, 101364); //ult
			player.getInterfaceState().setNextDialogueId(2, -1);
			break;
		case 1101360:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I want to become an Iron Man|I want to claim my Iron Man armour|You can't, nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 1362);
			player.getInterfaceState().setNextDialogueId(1, 101365); //hc
			player.getInterfaceState().setNextDialogueId(2, -1);
			break;
		case 1362:
			player.getActionSender().removeChatboxInterface();
			ClaimService claimed = Server.getInjector().getInstance(ClaimService.class);
			if (claimed.hasClaimed(player, ClaimType.IRONMAN_ARMOUR)) 
			{
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null,
						"You cannot change your Iron Man settings after claiming armour.");
			}
			else 
			{
				player.getActionSender().sendInterface(215, false);
			}
			break;
		case 101363:
			final boolean normalIronman = permissionService.is(player, PermissionService.PlayerPermissions.IRON_MAN);
			ClaimService claimService = Server.getInjector().getInstance(ClaimService.class);
			PlayerService playerService = Server.getInjector().getInstance(PlayerService.class);
			if (!normalIronman) {
				player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.DEFAULT,
						"You are not an Iron Man. The armour I provide is strictly for <img=2>Iron Men");
				player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				if (claimService.hasClaimed(player, ClaimType.IRONMAN_ARMOUR)) {
					player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.DEFAULT,
							"You have already received your armour.");
					player.getInterfaceState().setNextDialogueId(0, -1);
				} else {
					player.getActionSender().sendDialogue("Adam", DialogueType.NPC, 311, FacialAnimation.DEFAULT,
							"Here's your armour. Take good care of it, wear it with pride!");
					if (normalIronman) {
						playerService.giveItem(player, new Item(12810, 1), true);
						playerService.giveItem(player, new Item(12811, 1), true);
						playerService.giveItem(player, new Item(12812, 1), true);
					}
					claimService.claim(player, ClaimType.IRONMAN_ARMOUR);
					player.getInterfaceState().setNextDialogueId(0, -1);
				}
			}
			break;
		case 101364:
			final boolean UltimateIronman = permissionService.is(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
			ClaimService ultClaimService = Server.getInjector().getInstance(ClaimService.class);
			PlayerService ultPlayerService = Server.getInjector().getInstance(PlayerService.class);
			if (!UltimateIronman) {
				player.getActionSender().sendDialogue("Paul", DialogueType.NPC, 317, FacialAnimation.DEFAULT,
						"You are not an Ultimate Iron Man. The armour I provide is strictly for <img=3>Ultimate Iron Men");
				player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				if (ultClaimService.hasClaimed(player, ClaimType.IRONMAN_ARMOUR)) {
					player.getActionSender().sendDialogue("Paul", DialogueType.NPC, 317, FacialAnimation.DEFAULT,
							"You have already received your armour.");
					player.getInterfaceState().setNextDialogueId(0, -1);
				} else {
					player.getActionSender().sendDialogue("Paul", DialogueType.NPC, 317, FacialAnimation.DEFAULT, "Here's your armour. Take good care of it, wear it with pride!");
					if (UltimateIronman) {
						ultPlayerService.giveItem(player, new Item(12813, 1), true);
						ultPlayerService.giveItem(player, new Item(12814, 1), true);
						ultPlayerService.giveItem(player, new Item(12815, 1), true);
					}
					ultClaimService.claim(player, ClaimType.IRONMAN_ARMOUR);
					player.getInterfaceState().setNextDialogueId(0, -1);
				}
			}
			break;
		case 101365:
			final boolean HardcoreIronman = permissionService.is(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN);
			ClaimService HCclaimService = Server.getInjector().getInstance(ClaimService.class);
			PlayerService HCplayerService = Server.getInjector().getInstance(PlayerService.class);
			if (!HardcoreIronman) {
				player.getActionSender().sendDialogue("Jaun", DialogueType.NPC, 3369, FacialAnimation.DEFAULT,
						"You are not a Hardcore Iron Man. The armour I provide is strictly for <img=10>Hardcore Iron Men.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				if (HCclaimService.hasClaimed(player, ClaimType.IRONMAN_ARMOUR)) {
					player.getActionSender().sendDialogue("Jaun", DialogueType.NPC, 3369, FacialAnimation.DEFAULT,
							"You have already received your armour.");
					player.getInterfaceState().setNextDialogueId(0, -1);
				} else {
					player.getActionSender().sendDialogue("Jaun", DialogueType.NPC, 3369, FacialAnimation.DEFAULT,
							"Here's your armour. Take good care of it, wear it with pride!");
					if (HardcoreIronman) {
						HCplayerService.giveItem(player, new Item(20792, 1), true);
						HCplayerService.giveItem(player, new Item(20794, 1), true);
						HCplayerService.giveItem(player, new Item(20796, 1), true);
					}
					HCclaimService.claim(player, ClaimType.IRONMAN_ARMOUR);
					player.getInterfaceState().setNextDialogueId(0, -1);
				}
			}
			break;
		case 1364:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes I want to become an Ultimate Iron man|No!!!");
			player.getInterfaceState().setNextDialogueId(0, 1365);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 1365:
			PermissionService perms3 = Server.getInjector().getInstance(PermissionService.class);
			GroundItemService groundItems = Server.getInjector().getInstance(GroundItemService.class);
			if (permissionService.is(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN)) {
				player.getActionSender().sendDialogue("Paul", DialogueType.NPC, 317, FacialAnimation.DEFAULT,
						"You are already an Ultimate Iron Man.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				if (player.getSkills().getTotalLevel() != 32) {
					player.getActionSender().sendDialogue("Paul", DialogueType.NPC, 317, FacialAnimation.DEFAULT,
							"You must have not leveled your account to become an Iron Man.");
					player.getInterfaceState().setNextDialogueId(0, -1);
				} else {
					player.getActionSender().removeChatboxInterface();
					player.getBank().clear();
					player.getInventory().clear();
					player.getSkills().resetStats();
					player.setIsIronMan(false);
					player.setUltimateIronMan(true);
					perms3.give(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
					groundItems.getPlayerDroppedItems(player.getName()).forEach(groundItems::removeGroundItem);
				}
			}
			break;
			/* END: Paul (Iron Man) */
		
		//BOB
		case 1405:
			player.getActionSender().sendDialogue("Bob", DialogueType.NPC, 505, FacialAnimation.DEFAULT,
					"Hello there! Would you like to take a look at my skilling supplies? Or if you'd like,"
					+ " I can repair your damaged items for you. For a fee, of course.");
			player.getInterfaceState().setNextDialogueId(0, 1406);
			break;
		case 1406:
			player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I'd like to see your supplies.|Repair my items for me.|Nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 1407);
			player.getInterfaceState().setNextDialogueId(1, 1410);
			player.getInterfaceState().setNextDialogueId(2, 1412);
			break;
		case 1407:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"I'd like to see your supplies.");
			player.getInterfaceState().setNextDialogueId(0, 1408);
			break;
		case 1408:
			player.getActionSender().sendDialogue("Bob", DialogueType.NPC, 505, FacialAnimation.DEFAULT,
					"Certainly.");
			player.getInterfaceState().setNextDialogueId(0, 1409);
			break;
		case 1409:
			player.getActionSender().removeChatboxInterface();
			Shop.open(player, 9, 1);
			break;
		case 1410:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Repair my items for me.");
			player.getInterfaceState().setNextDialogueId(0, 1411);
			break;
		case 1411:
			player.getActionSender().sendDialogue("Bob", DialogueType.NPC, 505, FacialAnimation.DEFAULT,
					"Hand me the item you need repairing and I'll take a look.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 1412:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Nevermind.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		//ZEKE
		case 1415:
			player.getActionSender().sendDialogue("Zeke", DialogueType.NPC, 527, FacialAnimation.DEFAULT,
					"Hello, effendi. I offer you the finest weapons you'll ever see.");
			player.getInterfaceState().setNextDialogueId(0, 1416);
			break;
		case 1416:
			player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Show me your weapons|No thanks.");
			player.getInterfaceState().setNextDialogueId(0, 1417);
			player.getInterfaceState().setNextDialogueId(1, 1420);
			break;
		case 1417:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Show me your weapons.");
			player.getInterfaceState().setNextDialogueId(0, 1418);
			break;
		case 1418:
			player.getActionSender().sendDialogue("Zeke", DialogueType.NPC, 527, FacialAnimation.DEFAULT,
					"Of course.");
			player.getInterfaceState().setNextDialogueId(0, 1419);
			break;
		case 1419:
			player.getActionSender().removeChatboxInterface();
			Shop.open(player, 8, 1);
			break;
		case 1420:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"No thanks.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		
		case 6797:
			player.getActionSender().sendDialogue("Nieve", DialogueType.NPC, 6797, FacialAnimation.DEFAULT,
					"You can only attack creatures that you've been assigned as a slayer task in here.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
			//Horvik
			case 1425:
				player.getActionSender().sendDialogue("Horvik", DialogueType.NPC, 535, FacialAnimation.DEFAULT,
						"Hey! If you need armour, you've come to the right place.");
				player.getInterfaceState().setNextDialogueId(0, 1426);
				break;
			case 1426:
				player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"I'd like to buy some armour please.|I don't need armour.");
				player.getInterfaceState().setNextDialogueId(0, 1427);
				player.getInterfaceState().setNextDialogueId(1, 1430);
				break;
			case 1427:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"I'd like to buy some armour please.");
				player.getInterfaceState().setNextDialogueId(0, 1428);
				break;
			case 1428:
				player.getActionSender().sendDialogue("Horvik", DialogueType.NPC, 535, FacialAnimation.DEFAULT,
						"You won't be disappointed.");
				player.getInterfaceState().setNextDialogueId(0, 1429);
				break;
			case 1429:
				player.getActionSender().removeChatboxInterface();
				Shop.open(player, 3, 1);
				break;
			case 1430:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"I don't need armour.");
				player.getInterfaceState().setNextDialogueId(0, 1431);
				break;	
			case 1431:
				player.getActionSender().sendDialogue("Horvik", DialogueType.NPC, 535, FacialAnimation.DEFAULT,
						"Do come back if you change your mind.");
				player.getInterfaceState().setNextDialogueId(0, -1);
				break;
	
				//Zaff
				case 1445:
					player.getActionSender().sendDialogue("Zaff", DialogueType.NPC, 532, FacialAnimation.DEFAULT,
							"You want to buy some magic supplies. From me, specifically.");
					player.getInterfaceState().setNextDialogueId(0, 1446);
					break;
				case 1446:
					player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
							"You're right, I do.|Actually, I don't.");
					player.getInterfaceState().setNextDialogueId(0, 1447);
					player.getInterfaceState().setNextDialogueId(1, 1449);
					break;
				case 1447:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"You're right... I do want to buy magic supplies from you.");
					player.getInterfaceState().setNextDialogueId(0, 100001648);
					break;
				case 100001648:
					player.getActionSender().sendDialogue("Zaff", DialogueType.PLAYER, 532, FacialAnimation.DEFAULT,
							"Go figure.");
					player.getInterfaceState().setNextDialogueId(0, 1448);
					break;
				case 1448:
					player.getActionSender().removeChatboxInterface();
					Shop.open(player, 7, 1);
					break;
				case 1449:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Actually, I-");
					player.getInterfaceState().setNextDialogueId(0, 1450);
					break;
				case 1450:
					player.getActionSender().sendDialogue("Zaff", DialogueType.NPC, 532, FacialAnimation.DEFAULT,
							"Wrong.");
					player.getInterfaceState().setNextDialogueId(0, 1451);
					break;
				case 1451:
					player.getActionSender().removeChatboxInterface();
					Shop.open(player, 7, 1);
					break;
			
				//Makeover mage
				case 1455:
					player.getActionSender().sendDialogue("Make-over Mage", DialogueType.NPC, 1306, FacialAnimation.DEFAULT,
							"Greetings. What can I do for you?");
					player.getInterfaceState().setNextDialogueId(0, 1456);
					break;
				case 1456:
					player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
							"Can you give me a makeover?|Cool amulet! Can I have one?|Nothing.");
					player.getInterfaceState().setNextDialogueId(0, 1457);
					player.getInterfaceState().setNextDialogueId(1, 1464);
					player.getInterfaceState().setNextDialogueId(2, 1470);
					break;
				case 1457:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Can you give me a makeover?");
					player.getInterfaceState().setNextDialogueId(0, 1458);
					break;
				case 1458:
					player.getActionSender().sendDialogue("Make-over Mage", DialogueType.NPC, 1306, FacialAnimation.DEFAULT,
							"I can change your appearance for you! For the fair fee of 3,000 coins. What do you say?");
					player.getInterfaceState().setNextDialogueId(0, 1459);
					break;
				case 1459:
					player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
							"Sure.|3,000 is too much for me.");
					player.getInterfaceState().setNextDialogueId(0, 1460);
					player.getInterfaceState().setNextDialogueId(1, 1462);
					break;
				case 1460:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Sure.");
					player.getInterfaceState().setNextDialogueId(0, 1461);
					break;
				case 1461:
					if(player.getInventory().getCount(995) >= 3000)
					{
						player.getInventory().remove(new Item(995, 3000));
						player.getActionSender().removeChatboxInterface();
						player.getActionSender().sendInterface(269, false);
					}
					else
					{
						player.getActionSender().sendDialogue("Make-over Mage", DialogueType.NPC, 1306, FacialAnimation.DEFAULT,
								"Hmm, you don't seem to have enough gold right now. Come back when you do and I'll be happy to give you a makeover.");
						player.getInterfaceState().setNextDialogueId(0, -1);
						break;
					}
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Not right now.");
					player.getInterfaceState().setNextDialogueId(0, -1);
					break;
				case 1462:
					//CALC
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"That's too expensive for me.");
					player.getInterfaceState().setNextDialogueId(0, 1463);
					break;
				case 1463:
					player.getActionSender().sendDialogue("Make-over Mage", DialogueType.NPC, 1306, FacialAnimation.DEFAULT,
							"Suit yourself.");
					player.getInterfaceState().setNextDialogueId(0, -1);
					break;
				case 1464:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Cool amulet! Could I have one like that?");
					player.getInterfaceState().setNextDialogueId(0, 1465);
					break;
				case 1465:
					player.getActionSender().sendDialogue("Make-over Mage", DialogueType.NPC, 1306, FacialAnimation.HAPPY,
							"Why thank you! This amulet has magical properties. I could sell you a replica for 100 coins, however. ");
					player.getInterfaceState().setNextDialogueId(0, 1466);
					break;
				case 1466:
					player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
							"Pay 100 coins.|Decline the amulet.");
					player.getInterfaceState().setNextDialogueId(0, 1467);
					player.getInterfaceState().setNextDialogueId(1, 1469);
					break;
				case 1467:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Yes please!.");
					player.getInterfaceState().setNextDialogueId(0, 1468);
					break;
				case 1468:
					if(player.getInventory().getCount(995) >= 100)
					{
						player.getInventory().remove(new Item(995, 100));
						player.getInventory().add(new Item(7803));
						player.getActionSender().removeChatboxInterface();
					}
					else
					{
						player.getActionSender().sendDialogue("Make-over Mage", DialogueType.NPC, 1306, FacialAnimation.SAD,
								"You dont have the gold with you right now, come back when you do.");
					}
					player.getInterfaceState().setNextDialogueId(0, -1);
					break;
				case 1469:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Replica? No thanks.");
					player.getInterfaceState().setNextDialogueId(0, -1);
					break;
				case 1470:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, 1306, FacialAnimation.DEFAULT,
							"Nothing.");
					player.getInterfaceState().setNextDialogueId(0, -1);
					break;
					
					//Alfonse
				case 1475:
					player.getActionSender().sendDialogue("Alfonse the Waiter", DialogueType.NPC, 4920, FacialAnimation.DEFAULT,
							"Good day, adveturer. Could I interest you in a bite to eat? I've procured some of the finest sea food you'll see.");
					player.getInterfaceState().setNextDialogueId(0, 1476);
					break;
				case 1476:
					player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
							"Yes please!|I'm okay, thank you");
					player.getInterfaceState().setNextDialogueId(0, 1477);
					player.getInterfaceState().setNextDialogueId(1, 1480);
					break;
				case 1477:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Yes please! I'd love some food.");
					player.getInterfaceState().setNextDialogueId(0, 1478);
					break;
				case 1478:
					player.getActionSender().sendDialogue("Alfonse the Waiter", DialogueType.NPC, 4920, FacialAnimation.DEFAULT,
							"But of course.");
					player.getInterfaceState().setNextDialogueId(0, 1479);
					break;
				case 1479:
					player.getActionSender().removeChatboxInterface();
					Shop.open(player, 11, 1);
					break;
				case 1480:
					player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
							"Not right now.");
					player.getInterfaceState().setNextDialogueId(0, 1481);
					break;	
				case 1481:
					player.getActionSender().sendDialogue("Alfonse the Waiter", DialogueType.NPC, 4920, FacialAnimation.DEFAULT,
							"Do come back if you happen to reconsider.");
					player.getInterfaceState().setNextDialogueId(0, -1);
					break;
			//Lowe
			case 1435:
				player.getActionSender().sendDialogue("Lowe", DialogueType.NPC, 536, FacialAnimation.DEFAULT,
						"Lowe's Archery Emporium is the best place for all your archery supplies. Care to browse?");
				player.getInterfaceState().setNextDialogueId(0, 1436);
				break;
			case 1436:
				player.getActionSender().sendDialogue("<col=880000>What would you like to say?</col>", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
						"Sure.|Not right now.");
				player.getInterfaceState().setNextDialogueId(0, 1437);
				player.getInterfaceState().setNextDialogueId(1, 1439);
				break;
			case 1437:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"Sure");
				player.getInterfaceState().setNextDialogueId(0, 1438);
				break;
			case 1438:
				player.getActionSender().removeChatboxInterface();
				Shop.open(player, 5, 1);
				break;
			case 1439:
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
						"Not right now.");
				player.getInterfaceState().setNextDialogueId(0, -1);
				break;
		case 300:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Camelot Woodcutting|Varrock Mining|Falador Mine|Piscatoris Fishing colony|Next Page.");//Edgeville|Karamja|Gnome Stronghold|Piscatoris Fishing colony|Next Page.
			player.getInterfaceState().setNextDialogueId(0, 301);
			player.getInterfaceState().setNextDialogueId(1, 302);
			player.getInterfaceState().setNextDialogueId(2, 303);
			player.getInterfaceState().setNextDialogueId(3, 304);
			player.getInterfaceState().setNextDialogueId(4, 3006);
			break;
		case 301://Camelot Woodcutting
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2731, 3485));
			break;
		case 302://Varrock Mining
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(3283, 3371));
			break;
		case 303://Falador Mine
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(3053, 9774));
			break;
		case 304://Piscatoris Fishing colony
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2332, 3681));
			break;
		case 305://Agility
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Gnome Agility Course|Draynor Rooftop|Seer's Village Rooftop");
			player.getInterfaceState().setNextDialogueId(0, 390);
			player.getInterfaceState().setNextDialogueId(1, 391);
			player.getInterfaceState().setNextDialogueId(2, 392);
			break;
		case 390://Gnome Agility Course
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2471, 3437));
			break;
		case 391://Draynor Rooftop
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(3104, 3264));
			break;	
		case 392://Seer's Village Rooftop
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2731, 3485));
			break;	



		case 400:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Cities|Wilderness Regions|Miscellaneous");
			player.getInterfaceState().setNextDialogueId(0, 401);
			player.getInterfaceState().setNextDialogueId(1, 402);
			player.getInterfaceState().setNextDialogueId(2, 403);
			break;
		case 401:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Al-Kharid|Ape Atoll|Ardougne|Camelot|More options...");
			player.getInterfaceState().setNextDialogueId(0, 409);
			player.getInterfaceState().setNextDialogueId(1, 410);
			player.getInterfaceState().setNextDialogueId(2, 411);
			player.getInterfaceState().setNextDialogueId(3, 412);
			player.getInterfaceState().setNextDialogueId(4, 413);
			break;
		case 409:
			player.teleport(Constants.AL_KHARID, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 410:
			player.teleport(Constants.APE_ATOLL, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 411:
			player.teleport(Constants.ARDOUGNE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 412:
			player.teleport(Constants.CAMELOT, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 413:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Draynor Village|Falador|Musa Point|Lumbridge|More options...");
			player.getInterfaceState().setNextDialogueId(0, 414);
			player.getInterfaceState().setNextDialogueId(1, 415);
			player.getInterfaceState().setNextDialogueId(2, 416);
			player.getInterfaceState().setNextDialogueId(3, 417);
			player.getInterfaceState().setNextDialogueId(4, 418);
			break;
		case 414:
			player.teleport(Constants.DRAYNOR_VILLAGE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 415:
			player.teleport(Constants.FALADOR, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 416:
			player.teleport(Constants.KARAMJA, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 417:
			player.teleport(Constants.LUMBRIDGE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 418:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Shilo Village|Varrock|Yanille|Zanaris|More options...");
			player.getInterfaceState().setNextDialogueId(0, 419);
			player.getInterfaceState().setNextDialogueId(1, 420);
			player.getInterfaceState().setNextDialogueId(2, 421);
			player.getInterfaceState().setNextDialogueId(3, 422);
			player.getInterfaceState().setNextDialogueId(4, 423);
			break;
		case 419:
			player.teleport(Constants.SHILO_VILLAGE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 420:
			player.teleport(Constants.VARROCK, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 421:
			player.teleport(Constants.YANILLE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 422:
			player.teleport(Constants.ZANARIS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 423:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Lletya|Dorgesh-Kaan|Lunar Isle|Pollnivneach|More options...");
			player.getInterfaceState().setNextDialogueId(0, 424);
			player.getInterfaceState().setNextDialogueId(1, 425);
			player.getInterfaceState().setNextDialogueId(2, 426);
			player.getInterfaceState().setNextDialogueId(3, 427);
			player.getInterfaceState().setNextDialogueId(4, 101427);
			break;
		case 424:
			player.teleport(Constants.LLETYA, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 425:
			player.teleport(Constants.DORG, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 426:
			player.teleport(Constants.LUNAR_ISLE, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 427:
			player.teleport(Constants.POLLNIVNEACH, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 101427:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Rellekka|Gnome Stronghold|Piscatoris Fishing Colony|TzHaar|Port Phasmatys");
			player.getInterfaceState().setNextDialogueId(0, 101424);
			player.getInterfaceState().setNextDialogueId(1, 101425);
			player.getInterfaceState().setNextDialogueId(2, 101426);
			player.getInterfaceState().setNextDialogueId(3, 101428);
			player.getInterfaceState().setNextDialogueId(4, 101429);
			break;
		case 101424:
			player.teleport(Constants.RELLEKKA, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 101425:
			player.teleport(Constants.GNOME_STRONGHOLD, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 101426:
			player.teleport(Constants.PISCATORIS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 101428:
			player.teleport(Constants.TZHAAR, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 101429:
			player.teleport(Constants.PORT_PHASMATYS, 0, 0, true);
			player.getActionSender().removeChatboxInterface();
			break;
		case 402:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"West Dragons <col=888800>(Level-10)|East Dragons <col=887700>(Level-19)"
					+ "|Rogue's Castle <col=883300>(Level-51)|Resource Area <col=882200>(Level-55)");
			player.getInterfaceState().setNextDialogueId(0, 406);
			player.getInterfaceState().setNextDialogueId(1, 405);
			player.getInterfaceState().setNextDialogueId(2, 407);
			player.getInterfaceState().setNextDialogueId(3, 408);
			break;
		case 405: //easts
			player.getActionSender().removeChatboxInterface();
			player.teleport(Location.create(3351, 3670), 0, 0, true);
			break;
		case 406://wests
			player.getActionSender().removeChatboxInterface();
			player.teleport(Location.create(2985, 3596),0, 0, true);
			break;
		case 407://chaos ele
			player.getActionSender().removeChatboxInterface();
			player.teleport(Location.create(3287, 3923), 0, 0, true);
			break;
		case 408://widly skill area
			player.getActionSender().removeChatboxInterface();
			player.teleport(Location.create(3184, 3952), 0, 0, true);
			break;
		case 403:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Ancient Altar|Lunar Altar|Dark Altar|Rogue's Den|Woodcutting Guild");
			player.getInterfaceState().setNextDialogueId(0, 9409);
			player.getInterfaceState().setNextDialogueId(1, 9410);
			player.getInterfaceState().setNextDialogueId(2, 9411);
			player.getInterfaceState().setNextDialogueId(3, 9412);
			player.getInterfaceState().setNextDialogueId(4, 9413);
			break;
		case 9409:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.ANCIENT_ALTAR, 0, 0, true);
			break;
		case 9410:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.LUNAR_ALTAR, 0, 0, true);
			break;
		case 9411:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.DARK_ALTAR, 0, 0, true);
			break;
		case 9412:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.ROGUES_DEN, 0, 0, true);
			break;
		case 9413:
			player.getActionSender().removeChatboxInterface();
			player.teleport(Constants.WOODCUTTING_GUILD, 0, 0, true);
			break;
		case 404:
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(3189, 3959));
			break;

		case 450://member npc
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DISTRESSED,
					"Hi|I'm not finished|Come back later");
			player.getInterfaceState().setNextDialogueId(0, 5000);
			player.getInterfaceState().setNextDialogueId(0, 5001);
			player.getInterfaceState().setNextDialogueId(0, 5002);

		case 3006:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Agility|Tai Bwo Wannai|Entrana|Shilo Village|Next Page.");
			player.getInterfaceState().setNextDialogueId(0, 305);
			player.getInterfaceState().setNextDialogueId(1, 3007);
			player.getInterfaceState().setNextDialogueId(2, 3008);
			player.getInterfaceState().setNextDialogueId(3, 3009);
			player.getInterfaceState().setNextDialogueId(4, 3010);
			break;
		case 3010:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Crafting Guild");
			player.getInterfaceState().setNextDialogueId(0, 3018);
			break;
		case 3018:
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2933, 3290));//3081
			break;
		case 3007:
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2802, 3078));//3081
			break;
		case 3008:
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2834, 3335));
			break;
		case 3009:
			player.getActionSender().removeChatboxInterface();
			player.setTeleportTarget(Location.create(2865, 2956));
			break;

		case 2040:
			player.getActionSender().sendDialogue("Zul-Areth", DialogueType.NPC, 2040, FacialAnimation.DEFAULT,
					"Hello, How can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 2041);
			break;
		case 2041:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Can I collect my items?");
			player.getInterfaceState().setNextDialogueId(0, 2042);
			break;
		case 2042:
			boolean required = player.getDatabaseEntity().getZulrahState().getItemsLostZulrah().isEmpty();
			int amount = permissionService.isAny(player, PermissionServiceImpl.SPECIAL_PERMISSIONS) ? 37500 : 75000;
			//s int amount = player.getRights() != Player.Rights.PLAYER && player.getRights() != Player.Rights.IRON_MAN ? 37500 : 75000;
			player.getActionSender().sendDialogue("Zul-Areth", DialogueType.NPC, 2040, FacialAnimation.DEFAULT,
					required ? "You currently don't have any items awaiting you." : "Absolutely, that'll be " + amount + " coins.");
			player.getInterfaceState().setNextDialogueId(0, required ? 2043 : 2044);
			break;
		case 2043:
			player.getActionSender().removeChatboxInterface();
			break;
		case 2044:
			player.getActionSender().removeChatboxInterface();
			player.getZulAreth().claimItems();
			break;
		case 2045:
			player.getActionSender().sendDialogue("Zul-Areth", DialogueType.NPC, 2040, FacialAnimation.DEFAULT,
					"You don't have enough coins.");
			player.getInterfaceState().setNextDialogueId(0, 2046);
			break;
		case 2046:
			player.getActionSender().removeChatboxInterface();
			break;

		case 2914:
			player.getActionSender().sendDialogue("Otto Godblessed", DialogueType.NPC, 2914, FacialAnimation.DEFAULT,
					"Hello how can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 2915);
			break;
		case 2915:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Can you make my Zamorakian Spear into a hasta?|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 2916);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 2916:
			player.getActionSender().sendDialogue("Otto Godblessed", DialogueType.NPC, 2914, FacialAnimation.DEFAULT,
					"For a fee of 300,000 coins, Is that okay?");
			player.getInterfaceState().setNextDialogueId(0, 2917);
			break;
		case 2917:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes.|No.");
			player.getInterfaceState().setNextDialogueId(0, 2918);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 2918:
			boolean hasSpear = player.getInventory().contains(11824);
			boolean hasCoins = player.getInventory().getCount(995) >= 300000;
			player.getActionSender().removeChatboxInterface();
			if (!hasSpear) {
				player.getActionSender().sendMessage("You seem to be missing the Zamorakian Spear.");
			} else if (!hasCoins) {
				player.getActionSender().sendMessage("You don't have enough coins to complete this.");
			} else {
				player.getInventory().remove(new Item(995, 300000));
				player.getInventory().remove(new Item(11824));
				player.getInventory().add(new Item(11889));
			}
			break;

		case 3227:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"Good day, how may I help you?");
			player.getInterfaceState().setNextDialogueId(0, 3228);
			break;
		case 3228:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"How do i use the bank?|I'd like to access my bank account please.|I'd like to check my PIN settings.|I'd like to collect items.");
			player.getInterfaceState().setNextDialogueId(0, 3229);
			player.getInterfaceState().setNextDialogueId(1, 3230);
			player.getInterfaceState().setNextDialogueId(2, 3258);
			player.getInterfaceState().setNextDialogueId(3, -1);
			break;
		case 3229:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Using the bank itself.|Using Bank deposit boxes.|What's this PIN thing that people keep talking about?|Goodbye.");
			player.getInterfaceState().setNextDialogueId(0, 3230);
			player.getInterfaceState().setNextDialogueId(1, 3238);
			player.getInterfaceState().setNextDialogueId(2, 3241);
			player.getInterfaceState().setNextDialogueId(3, -1);
			break;
		case 3230:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Using the bank itself. I'm not sure how....?");
			player.getInterfaceState().setNextDialogueId(0, 3231);
			break;
		case 3231:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"Speak to any banker and ask to see your bank<br>account. If you have set a PIN you will be asked for<br>it, then all the belongings you have placed in the bank<br>will appear in the window. To withdraw one item, left-");
			player.getInterfaceState().setNextDialogueId(0, 3232);
			break;
		case 3232:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"click on it once.");
			player.getInterfaceState().setNextDialogueId(0, 3233);
			break;
		case 3233:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"To withdraw many, right-click on the item and select<br>from the menu. The same for depositing, left-click on<br>the item in your inventory to deposit it in the bank.<br>Right-click on it to deposit many of the same items.");
			player.getInterfaceState().setNextDialogueId(0, 3234);
			break;
		case 3234:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"To move things around in your bank: firstly select<br>Swap or Insert as your default moving mode, you can<br>find these buttons on the bank window itself. Then click<br>and drag an item to where you want it to appear.");
			player.getInterfaceState().setNextDialogueId(0, 3235);
			break;
		case 3235:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"You may withdraw 'notes' or 'certificates' when the<br>items you are trying to withdraw do not stack in your<br>ivnentory. This will only work for items which are<br>tradable.");
			player.getInterfaceState().setNextDialogueId(0, 3236);
			break;
		case 3236:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"For instance, if you wanted to sell 100 logs to another<br>player, they would not fin in one inventory and you<br>would need to do multiple trades. Instead, click the<br>Note button to do withdraw the logs as 'certs' or 'notes'.");
			player.getInterfaceState().setNextDialogueId(0, 3237);
			break;
		case 3237:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"then withdraw the items you need.");
			player.getInterfaceState().setNextDialogueId(0, 3229);
			break;
		case 3238:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Using Bank deposit boxes.... what are they?");
			player.getInterfaceState().setNextDialogueId(0, 3239);
			break;
		case 3239:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"They look like grey pillars, there's one just over there,<br>Bank Deposit boxes save so much time. If you're<br>simply wanting to deposit a single item, 'Use'<br>it on the deposit box.");
			player.getInterfaceState().setNextDialogueId(0, 3240);
			break;
		case 3240:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"Otherwise, simply click once on the box and it will give<br>you a choice of what to deposit in an interface very<br>similiar to the bank itself. Very quick for when you're<br>simply fishing or mining etc.");
			player.getInterfaceState().setNextDialogueId(0, 3229);
			break;
		case 3241:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"What's this PIN thing that people keep talking about?");
			player.getInterfaceState().setNextDialogueId(0, 3242);
			break;
		case 3242:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"The PIN - Personal Identification Number - can be<br>set on your bank account to protect the items there in<br>case someone finds out your account password. It<br>consists of four numbers that you remember and tell");
			player.getInterfaceState().setNextDialogueId(0, 3243);
			break;
		case 3243:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"no one.");
			player.getInterfaceState().setNextDialogueId(0, 3244);
			break;
		case 3244:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"So if someone did manage to get your password they<br>couldn't steal your items if they were in the bank.");
			player.getInterfaceState().setNextDialogueId(0, 3245);
			break;
		case 3245:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"How do I set my PIN?|How do I remove my PIN?|What happens if I forget my PIN?|I know about the PIN, tell me about the bank.|Goodbye.");
			player.getInterfaceState().setNextDialogueId(0, 3246);
			player.getInterfaceState().setNextDialogueId(1, 3252);
			player.getInterfaceState().setNextDialogueId(2, 3255);
			player.getInterfaceState().setNextDialogueId(3, 3229);
			player.getInterfaceState().setNextDialogueId(4, -1);
			break;
		case 3246:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"How do I set my PIN?");
			player.getInterfaceState().setNextDialogueId(0, 3247);
			break;
		case 3247:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"You can set your PIN by talking to any banker, they<br>will allow you to access your bank pin settings. Here<br>you can choose to set your pin and recovery delay.<br>Remember not to set it to anything personal such as");
			player.getInterfaceState().setNextDialogueId(0, 3248);
			break;
		case 3248:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"your real life bank PIN or birthday.");
			player.getInterfaceState().setNextDialogueId(0, 3249);
			break;
		case 3249:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"The recovery delay is to protect your banked items<br>from account thieves. If someone stole your account<br>and asked to have the PIN deleted, they would have to<br>wait a few days before accessing your bank account to");
			player.getInterfaceState().setNextDialogueId(0, 3250);
			break;
		case 3250:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"steal your items. This will give you time to recover<br>your account.");
			player.getInterfaceState().setNextDialogueId(0, 3251);
			break;
		case 3251:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"There will also be a delay in actually setting the PIN<br>to be used, this is so that if your account is stolen and<br>a PIN set, you can cancel it before it comes into use!");
			player.getInterfaceState().setNextDialogueId(0, 3245);
			break;
		case 3252:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"How do I remove my PIN?");
			player.getInterfaceState().setNextDialogueId(0, 3253);
			break;
		case 3253:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"Talking to any banker will enable you to access your<br>PIN settings. There you can cancel or change your<br>PIN, but you will need to wait for your recovery<br>delay to expire to be able to access your bank. This");
			player.getInterfaceState().setNextDialogueId(0, 3254);
			break;
		case 3254:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"can be set in the settings page and will protect your<br>items should your account be stolen.");
			player.getInterfaceState().setNextDialogueId(0, 3245);
			break;
		case 3255:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"What happens if I forget my PIN?");
			player.getInterfaceState().setNextDialogueId(0, 3256);
			break;
		case 3256:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"If you find yourself faced with the PIN keypad and<br>you don't know the PIN, just look on the right-hand<br>side for a button marked 'I don't know it'. Click this<br>button. Your PIN will be deleted (after a delay of a");
			player.getInterfaceState().setNextDialogueId(0, 3257);
			break;
		case 3257:
			player.getActionSender().sendDialogue(CacheNPCDefinition.get(player.getAttribute("talkingNpc")).getName(), DialogueType.NPC, player.getAttribute("talkingNpc"), FacialAnimation.DEFAULT,
					"few days) and you'll be able to use your bank as<br>before. You may still use the bank deposit box without<br>your PIN.");
			player.getInterfaceState().setNextDialogueId(0, 3245);
			break;
		case 3258:
			BankPinService service = Server.getInjector().getInstance(BankPinService.class);

			player.getActionSender().removeChatboxInterface();
			service.openPinSettingsInterface(player, BankPinServiceImpl.SettingScreenType.DEFAULT);
			break;

		case 5919:
			player.getActionSender().sendDialogue("Grace", DialogueType.NPC, 5919, FacialAnimation.DEFAULT,
					"Hello, How can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 5920);
			break;
		case 5920:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"I'd like to color my graceful clothing.|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 5921);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 5921:
			player.getActionSender().sendDialogue("Grace", DialogueType.NPC, 5919, FacialAnimation.DEFAULT,
					"What color would you like to upgrade to?");
			player.getInterfaceState().setNextDialogueId(0, 5922);
			break;
		case 5922:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Arceeus (Purple)|Piscarilius (Teal)|Lovakengj (Yellow) |Shayzien (Red) |Hosidius (Green)|");
			player.getInterfaceState().setNextDialogueId(0, 5923);
			player.getInterfaceState().setNextDialogueId(1, 5924);
			player.getInterfaceState().setNextDialogueId(2, 5925);
			player.getInterfaceState().setNextDialogueId(3, 5926);
			player.getInterfaceState().setNextDialogueId(4, 5927);
			break;
		case 5923:
			//Purple
			GracefulRecolour.recolourGraceful(player, Constants.PURPLE_GRACEFUL);
			break;
		case 5924:
			//Blue
			GracefulRecolour.recolourGraceful(player, Constants.TEAL_GRACEFUL);
			break;
		case 5925:
			//Yellow
			GracefulRecolour.recolourGraceful(player, Constants.YELLOW_GRACEFUL);
			break;
		case 5926:
			//Red
			GracefulRecolour.recolourGraceful(player, Constants.RED_GRACEFUL);
			break;
		case 5927:
			//Green
			GracefulRecolour.recolourGraceful(player, Constants.GREEN_GRACEFUL);
			break;
		case 12954:
			int interfaceId1 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId1, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId1, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId1, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 12954, 150);

			player.getInterfaceState().setNextDialogueId(0, 12955);
			break;
		case 12955:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 12956);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 12956:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(12954) && player.getInventory().contains(20143)) {
				player.getInventory().remove(new Item(12954));
				player.getInventory().remove(new Item(20143));
				player.getInventory().add(new Item(19722));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 19722, null, "You merge the two together to make an Dragon Defender (t).");
				player.getActionSender().sendMessage("You merge the two together to make a Dragon Defender (t).");
			}
			break;

		case 11920:
			int interfaceId2 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId2, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId2, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId2, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11920, 150);

			player.getInterfaceState().setNextDialogueId(0, 11921);
			break;
		case 11921:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11922);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11922:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11920) && player.getInventory().contains(12800)) {
				player.getInventory().remove(new Item(11920));
				player.getInventory().remove(new Item(12800));
				player.getInventory().add(new Item(12797));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12797, null, "You merge the two together to make an Dragon Pickaxe.");
				player.getActionSender().sendMessage("You merge the two together to make a Dragon Pickaxe.");
			}
			break;

		case 11335:
			int interfaceId3 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId3, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId3, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId3, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11335, 150);

			player.getInterfaceState().setNextDialogueId(0, 11336);
			break;
		case 11336:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11337);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11337:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11335) && player.getInventory().contains(12538)) {
				player.getInventory().remove(new Item(11335));
				player.getInventory().remove(new Item(12538));
				player.getInventory().add(new Item(12417));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12417, null, "You merge the two together to make an Dragon fullhelm (g).");
				player.getActionSender().sendMessage("You merge the two together to make a Dragon fullhelm (g).");
			}
			break;

		case 1187:
			int interfaceId4 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId4, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId4, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId4, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 1187, 150);

			player.getInterfaceState().setNextDialogueId(0, 1188);
			break;
		case 1188:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 1189);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 1189:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(1187) && player.getInventory().contains(12532)) {
				player.getInventory().remove(new Item(1187));
				player.getInventory().remove(new Item(12532));
				player.getInventory().add(new Item(12418));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12418, null, "You merge the two together to make an Dragon sq shield (g).");
				player.getActionSender().sendMessage("You merge the two together to make a Dragon sq shiel (g).");
			}
			break;

		case 11787:
			int interfaceId5 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId5, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId5, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId5, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11787, 150);

			player.getInterfaceState().setNextDialogueId(0, 11788);
			break;
		case 11788:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11789);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11789:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11787) && player.getInventory().contains(12798)) {
				player.getInventory().remove(new Item(11787));
				player.getInventory().remove(new Item(12798));
				player.getInventory().add(new Item(12795));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12795, null, "You merge the two together to make an Steam Battlestaff.");
				player.getActionSender().sendMessage("You merge the two together to make a Steam Battlestaff.");
			}
			break;

		case 11924:
			int interfaceId6 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId6, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId6, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId6, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11924, 150);

			player.getInterfaceState().setNextDialogueId(0, 11925);
			break;
		case 11925:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11926);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11926:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11924) && player.getInventory().contains(12802)) {
				player.getInventory().remove(new Item(11924));
				player.getInventory().remove(new Item(12802));
				player.getInventory().add(new Item(12806));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12806, null, "You merge the two together to make an Malediction ward.");
				player.getActionSender().sendMessage("You merge the two together to make a Malediction ward.");
			}
			break;

		case 11927:
			int interfaceId7 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId7, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId7, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId7, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11926, 150);

			player.getInterfaceState().setNextDialogueId(0, 11928);
			break;
		case 11928:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11929);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11929:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11926) && player.getInventory().contains(12802)) {
				player.getInventory().remove(new Item(11926));
				player.getInventory().remove(new Item(12802));
				player.getInventory().add(new Item(12807));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12807, null, "You merge the two together to make an Odium ward.");
				player.getActionSender().sendMessage("You merge the two together to make a Odium ward.");
			}
			break;

		case 4587:
			int interfaceId8 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId8, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId8, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId8, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 4587, 150);

			player.getInterfaceState().setNextDialogueId(0, 4588);
			break;
		case 4588:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 4589);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 4589:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(4587) && player.getInventory().contains(20002)) {
				player.getInventory().remove(new Item(4587));
				player.getInventory().remove(new Item(20002));
				player.getInventory().add(new Item(20000));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20000, null, "You merge the two together to make an Dragon Scimitar (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Dragon Scimitar (or).");
			}
			break;

		case 19553:
			int interfaceId9 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId9, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId9, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId9, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 19553, 150);

			player.getInterfaceState().setNextDialogueId(0, 19554);
			break;
		case 19554:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 19555);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 19555:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(19553) && player.getInventory().contains(20062)) {
				player.getInventory().remove(new Item(19553));
				player.getInventory().remove(new Item(20062));
				player.getInventory().add(new Item(20366));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20366, null, "You merge the two together to make an Amulet of Torture (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Amulet of Torture (or).");
			}
			break;

		case 12002:
			int interfaceId10 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId10, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId10, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId10, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 12002, 150);

			player.getInterfaceState().setNextDialogueId(0, 12003);
			break;
		case 12003:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 12004);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 12004:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(12002) && player.getInventory().contains(20065)) {
				player.getInventory().remove(new Item(12002));
				player.getInventory().remove(new Item(20065));
				player.getInventory().add(new Item(19720));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 19720, null, "You merge the two together to make an Occult necklace (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Occult necklace (or).");
			}
			break;

		case 11804:
			int interfaceId11 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId11, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId11, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId11, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11804, 150);

			player.getInterfaceState().setNextDialogueId(0, 11805);
			break;
		case 11805:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11806);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11806:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11804) && player.getInventory().contains(20071)) {
				player.getInventory().remove(new Item(11804));
				player.getInventory().remove(new Item(20071));
				player.getInventory().add(new Item(20370));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20370, null, "You merge the two together to make an Bandos godsword (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Bandos godsword (or).");
			}
			break;
		  case 13280:
	            
	            player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
	                    "Warriors' Guild|Crafting Guild|POH|Fishing Guild|Ardougne Farm");
	            player.getInterfaceState().setNextDialogueId(0, 13281);
	            player.getInterfaceState().setNextDialogueId(1, 13282);
	            player.getInterfaceState().setNextDialogueId(2, 13283);
	            player.getInterfaceState().setNextDialogueId(3, 13284);
				player.getInterfaceState().setNextDialogueId(4, 13285);
	        	break;
	        	
	        case 13281://Warriors' Guild
	        	player.teleport(Constants.WARRIORS_GUILD, 0, 0, false);
	            player.getActionSender().removeChatboxInterface();
	            break;	
	            
	        case 13282://Crafting Guild
	        	player.teleport(Location.create(2933, 3285, 0), 0, 0, false);
	        	player.getActionSender().removeChatboxInterface();
	        	break;
	            
	        case 13283://POH
	        	player.getActionSender().sendMessage("This feature is coming soon.");
	        	player.getActionSender().removeChatboxInterface();
	        	break;	
	        	
	        case 13284://Fishing Guild
	        	player.teleport(Location.create(2603, 3402, 0), 0, 0, false);
	        	player.getActionSender().removeChatboxInterface();
	        	break;	
	        	
	        case 13285://Ardougne Farm
	        	player.teleport(Location.create(2661, 3371, 0), 0, 0, false);
	        	player.getActionSender().removeChatboxInterface();
	        	break;	
	        	
	        case 13286:
	            
	            player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
	                    "Pestle and Mortar|Spellbook Swap|Grapple");
	            player.getInterfaceState().setNextDialogueId(0, 13287);
	            player.getInterfaceState().setNextDialogueId(1, 13288);
	            player.getInterfaceState().setNextDialogueId(2, 13289);
	        	break;	
	        	
	        case 13287://Pestle and Mortar
	        	player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 233, null, "You find a Pestle and Mortar.");
	        	player.getInventory().add(new Item(233));
	        	break;
	        	
	        case 13288://Spellbook Swap
	        	DialogueManager.openDialogue(player, 13293);
	        	break;	
	        	
	        case 13289://Grapple
	        	player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 9419, null, "You find a Mith grapple.");
	        	player.getInventory().add(new Item(9419));
	        	break;
			  case 13293:
	          	player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
	                      "Normal|Ancients|Lunars");
	              player.getInterfaceState().setNextDialogueId(0, 13294);
	              player.getInterfaceState().setNextDialogueId(1, 13295);
	              player.getInterfaceState().setNextDialogueId(2, 13296);
	          	break;
	          
	          case 13294:
					player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
							1381, null, "Your magic book has been changed to the Regular spellbook.");
					player.getActionSender().sendConfig(439, 0);
					player.getCombatState()
							.setSpellBook(MagicCombatAction.SpellBook.MODERN_MAGICS.getSpellBookId());
	          	break;	
	          	
	          case 13295:
					player.getActionSender().sendConfig(439, 1);
					player.getCombatState()
							.setSpellBook(MagicCombatAction.SpellBook.ANCIENT_MAGICKS.getSpellBookId());
					player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
							4675, null, "An ancient wisdom fills your mind...");
	          	break;
	          	
	          case 13296:
					player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
							9084, null, "Lunar spells activated!");
					player.getActionSender().sendConfig(439, 2);
					player.getCombatState()
							.setSpellBook(MagicCombatAction.SpellBook.LUNAR_MAGICS.getSpellBookId());
	          	break;	


		case 11807:
			int interfaceId12 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId12, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId12, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId12, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11806, 150);

			player.getInterfaceState().setNextDialogueId(0, 11808);
			break;
		case 11808:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11809);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11809:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11806) && player.getInventory().contains(20074)) {
				player.getInventory().remove(new Item(11806));
				player.getInventory().remove(new Item(20074));
				player.getInventory().add(new Item(20372));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20372, null, "You merge the two together to make an Saradomin godsword (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Saradomin godsword (or).");
			}
			break;

		case 11810:
			int interfaceId13 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId13, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId13, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId13, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11806, 150);

			player.getInterfaceState().setNextDialogueId(0, 11811);
			break;
		case 11811:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11812);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11812:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11806) && player.getInventory().contains(20074)) {
				player.getInventory().remove(new Item(11806));
				player.getInventory().remove(new Item(20074));
				player.getInventory().add(new Item(20372));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20372, null, "You merge the two together to make an Saradomin godsword (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Saradomin godsword (or).");
			}
			break;

		case 11813:
			int interfaceId14 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId14, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId14, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId14, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11808, 150);

			player.getInterfaceState().setNextDialogueId(0, 11814);
			break;
		case 11814:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11815);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11815:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11808) && player.getInventory().contains(20077)) {
				player.getInventory().remove(new Item(11808));
				player.getInventory().remove(new Item(20077));
				player.getInventory().add(new Item(20374));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20374, null, "You merge the two together to make an Zamorak godsword (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Zamorak godsword (or).");
			}
			break;

		case 11816:
			int interfaceId15 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId15, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId15, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId15, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11802, 150);

			player.getInterfaceState().setNextDialogueId(0, 11817);
			break;
		case 11817:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11818);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11818:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11802) && player.getInventory().contains(20068)) {
				player.getInventory().remove(new Item(11802));
				player.getInventory().remove(new Item(20068));
				player.getInventory().add(new Item(20368));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20368, null, "You merge the two together to make an Armadyl godsword (or).");
				player.getActionSender().sendMessage("You merge the two together to make a Armadyl godsword (or).");
			}
			break;

		case 11819:
			int interfaceId16 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId16, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId16, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId16, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11235, 150);

			player.getInterfaceState().setNextDialogueId(0, 11820);
			break;
		case 11820:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11821);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11821:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11235) && player.getInventory().contains(12757)) {
				player.getInventory().remove(new Item(11235));
				player.getInventory().remove(new Item(12757));
				player.getInventory().add(new Item(12766));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12766, null, "You merge the two together to make an Blue Dark bow.");
				player.getActionSender().sendMessage("You merge the two together to make a Blue Dark bow.");
			}
			break;

		case 11822:
			int interfaceId17 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId17, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId17, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId17, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11235, 150);

			player.getInterfaceState().setNextDialogueId(0, 11823);
			break;
		case 11823:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11824);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11824:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11235) && player.getInventory().contains(12759)) {
				player.getInventory().remove(new Item(11235));
				player.getInventory().remove(new Item(12759));
				player.getInventory().add(new Item(12765));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12765, null, "You merge the two together to make an Green Dark bow.");
				player.getActionSender().sendMessage("You merge the two together to make a Green Dark bow.");
			}
			break;

		case 11825:
			int interfaceId18 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId18, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId18, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId18, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11235, 150);

			player.getInterfaceState().setNextDialogueId(0, 11826);
			break;
		case 11826:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11827);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11827:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11235) && player.getInventory().contains(12761)) {
				player.getInventory().remove(new Item(11235));
				player.getInventory().remove(new Item(12761));
				player.getInventory().add(new Item(12767));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12767, null, "You merge the two together to make an Yellow Dark bow.");
				player.getActionSender().sendMessage("You merge the two together to make a Yellow Dark bow.");
			}
			break;

		case 11828:
			int interfaceId19 = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId19, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId19, 1, "Would you like to merge these items?");
			player.getActionSender().sendString(interfaceId19, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 11235, 150);

			player.getInterfaceState().setNextDialogueId(0, 11829);
			break;
		case 11829:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 11830);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11830:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(11235) && player.getInventory().contains(12763)) {
				player.getInventory().remove(new Item(11235));
				player.getInventory().remove(new Item(12763));
				player.getInventory().add(new Item(12768));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12768, null, "You merge the two together to make an White Dark bow.");
				player.getActionSender().sendMessage("You merge the two together to make a White Dark bow.");
			}
			break;

		case 4151:
			int interfaceId = 193;

			player.getActionSender().sendInterface(162, 546, interfaceId, false);
			player.getActionSender().sendAccessMask(1, 193, 2, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 3, -1, -1);
			player.getActionSender().sendAccessMask(0, 193, 4, -1, -1);

			player.getActionSender().sendString(interfaceId, 1, "Would you like to merge these items? This is irreverisble.");
			player.getActionSender().sendString(interfaceId, 2, "Click here to continue.");
			player.getActionSender().sendItemOnInterface(193, 0, 4151, 150);

			player.getInterfaceState().setNextDialogueId(0, 4152);
			break;
		case 4152:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes i'd like to merge them.|No.");
			player.getInterfaceState().setNextDialogueId(0, 4153);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 4153:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(4151) && player.getInventory().contains(12004)) {
				player.getInventory().remove(new Item(4151));
				player.getInventory().remove(new Item(12004));
				player.getInventory().add(new Item(12006));
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12006, null, "You merge the two together to make an Abyssal Tentacle.");
				player.getActionSender().sendMessage("You merge the two together to make an Abyssal Tentacle.");
			}
			break;

		case 5074:
			player.getActionSender().sendDialogue("Kent", DialogueType.NPC, 5074, FacialAnimation.DEFAULT,
					"Hello, How can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 5075);
			break;
		case 5075:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Who are you?|Can I claim my vote reward?|Nothing");
			player.getInterfaceState().setNextDialogueId(0, 5076);
			player.getInterfaceState().setNextDialogueId(1, 5077);
			player.getInterfaceState().setNextDialogueId(2, 5078);
			break;
		case 5076:
			player.getActionSender().sendDialogue("Kent", DialogueType.NPC, 5074, FacialAnimation.DEFAULT,
					"My name is Kent, Lord Clank has put me<br> in charge of handling vote rewards.");
			player.getInterfaceState().setNextDialogueId(0, 5075);
			break;
		case 5077:
			player.getActionSender().sendDialogue("Kent", DialogueType.NPC, 5074, FacialAnimation.DEFAULT,
					"Absolutely!");
			player.getInterfaceState().setNextDialogueId(0, 5079);
			break;
		case 5078:
			player.getActionSender().removeChatboxInterface();
			break;
		case 5079:// make that on login for player
			player.getActionSender().removeChatboxInterface();
			break;
		case 5080:
			player.getActionSender().sendDialogue("Kent", DialogueType.NPC, 5074, FacialAnimation.DEFAULT,
					"You don't seem to have any rewards waiting for you...");
			player.getInterfaceState().setNextDialogueId(0, 5078);
			break;//hey1 sec gotta help mom with groceries.

			/** Bob Barter Decanting **/
		case 5449:
			player.getActionSender().sendDialogue("Bob Barter", DialogueType.NPC, 5449, FacialAnimation.DEFAULT,
					"Hello, what can I do for you?");
			player.getInterfaceState().setNextDialogueId(0, 5450);
			break;
		case 5450:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Could you decant my options for me?|Nothing");
			player.getInterfaceState().setNextDialogueId(0, 5451);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 5451:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Could you decant my options for me?");
			player.getInterfaceState().setNextDialogueId(0, 5452);
			break;
		case 5452:
			boolean donator = permissionService.isAny(player, PermissionService.PlayerPermissions.DONATOR);
			player.getActionSender().sendDialogue("Bob Barter", DialogueType.NPC, 5449, FacialAnimation.DEFAULT,
					donator ? "I'll decant an inventory of potions for you for free!" : 
						"I'll decant an inventory of potions for you for 10,000 coins.");
			player.getInterfaceState().setNextDialogueId(0, 5453);
			break;
		case 5453:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Decant my potions|Nevermind");
			player.getInterfaceState().setNextDialogueId(0, 5454);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 5454:
			if (permissionService.isAny(player, PermissionService.PlayerPermissions.DONATOR)) 
			{
				PotionDecanterService potionDecanterService = Server.getInjector().getInstance(PotionDecanterService.class);
				potionDecanterService.decantPotions(player);
				player.getActionSender().sendDialogue("Bob Barter", DialogueType.NPC, 5449, FacialAnimation.DEFAULT,
						"Thank you, come again!");
					player.getInterfaceState().setNextDialogueId(0, -1);
			}
			else if (!permissionService.isAny(player, PermissionService.PlayerPermissions.DONATOR) && player.getInventory().getCount(995) > 10000) 
			{
				player.getInventory().remove(new Item(995, 10000));
				PotionDecanterService potionDecanterService = Server.getInjector().getInstance(PotionDecanterService.class);
				potionDecanterService.decantPotions(player);
				player.getActionSender().sendDialogue("Bob Barter", DialogueType.NPC, 5449, FacialAnimation.DEFAULT,
						"Thank you, come again!");
					player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				player.getActionSender().sendDialogue("Bob Barter", DialogueType.NPC, 5449, FacialAnimation.DEFAULT,
						"You don't have enough money, come back when you do.");
					player.getInterfaceState().setNextDialogueId(0, -1);
			}
			break;
		case 6599:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Who are you and what is this place?");
			player.getInterfaceState().setNextDialogueId(0, 6600);
			break;
		case 6600:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"My name is Mandrith.");
			player.getInterfaceState().setNextDialogueId(0, 6601);
			break;
		case 6601:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"I collect valuable resources and pawn off access to them<br>to foolish adventurers, like yourself.");
			player.getInterfaceState().setNextDialogueId(0, 6602);
			break;
		case 6602:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"You should take a look inside my arena. There's an<br> abundance of valuable resources inside.");
			player.getInterfaceState().setNextDialogueId(0, 6603);
			break;
		case 6603:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"And I can take whatever I want?");
			player.getInterfaceState().setNextDialogueId(0, 6604);
			break;
		case 6604:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"It's all yours. All i ask is you pay the upfront fee of 30,000 coins.");
			player.getInterfaceState().setNextDialogueId(0, 6605);
			break;
		case 6605:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Will others be able to kill me inside?");
			player.getInterfaceState().setNextDialogueId(0, 6606);
			break;
		case 6606:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"Yes. These walls will only hold them back for so long.");
			player.getInterfaceState().setNextDialogueId(0, 6607);
			break;
		case 6607:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"You'll endGame them though, right?");
			player.getInterfaceState().setNextDialogueId(0, 6608);
			break;
		case 6608:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"Haha! For the right price, I won't deny any one access<br>to my arena. Even if their intention is to kill you.");
			player.getInterfaceState().setNextDialogueId(0, 6609);
			break;
		case 6609:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT,
					"Right...");
			player.getInterfaceState().setNextDialogueId(0, 6610);
			break;
		case 6610:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"My arena holds many treasures that I've acquired at<br>great expense, adventurer. Their bounty can come at a<br>price.");
			player.getInterfaceState().setNextDialogueId(0, 6611);
			break;
		case 6611:
			player.getActionSender().sendDialogue("Mandrith", DialogueType.NPC, 6599, FacialAnimation.DEFAULT,
					"One day, adventurer, I will boast ADMINISTRATORship of a much<br>larger, much more dangerous arena than this. Take<br>advantage of this offer while it lasts.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;

		case 6481:
			player.getActionSender().sendDialogue("Mac ", DialogueType.NPC, 6481, FacialAnimation.DEFAULT,
					"Hello, how can I help you?");
			player.getInterfaceState().setNextDialogueId(0, 6482);
			break;
		case 6482:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Can I get a Max Cape?|Nothing.");
			player.getInterfaceState().setNextDialogueId(0, 6483);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 6483:
			int totalLevel = player.getSkills().getTotalLevel();
			String text = totalLevel >= Constants.MAX_LEVEL ? "Sure you seem to be a master of all skills, It'll cost 2m is that alright?" : "Sorry, please come back when you're a master of all skills";
			int nextDialogue = totalLevel >= Constants.MAX_LEVEL ? 6484 : -1;
			player.getActionSender().sendDialogue("Mac ", DialogueType.NPC, 6481, FacialAnimation.DEFAULT,
					text);
			player.getInterfaceState().setNextDialogueId(0, nextDialogue);
			break;
		case 6484:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 6485);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 6485:
			player.getActionSender().removeChatboxInterface();
			hasGold = player.getInventory().getCount(995) >= 2000000;
			if (hasGold) {
				if (player.getInventory().add(new Item(13280)) && player.getInventory().add(new Item(13281))) {
					player.getInventory().remove(new Item(995, 2000000));
					player.getActionSender().sendMessage("You pay 2.5m for the Max Cape and hood.");
				}
			} else {
				player.getActionSender().sendMessage("You don't have enough gold to purchase the Max cape and hood.");
			}
			break;
			/**
			 * Emblem trader bullshit
			 */
		case 10000:
			player.getActionSender().removeChatboxInterface();
			int pointsPrior = player.getDatabaseEntity().getBountyHunter().getBountyShopPoints();
			for (Item inv : player.getInventory().toArray()) {
				if (inv == null) {
					continue;
				}
				BountyHunterService.Emblems.of(inv.getId()).ifPresent(e -> {
					player.getInventory().remove(inv);
					int points = player.getDatabaseEntity().getBountyHunter().getBountyShopPoints();
					player.getDatabaseEntity().getBountyHunter().setBountyShopPoints(e.getCost() + points);
				});
			}
			int addedPoints = player.getDatabaseEntity().getBountyHunter().getBountyShopPoints() - pointsPrior;
			player.getActionSender().sendMessage("You sell all your emblems for " + NumberFormat.getNumberInstance(Locale.ENGLISH).format(addedPoints) + " Bounties.");
			break;

		case 11865:
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, 11864, null, 
					"The slayer master will imbue your helmet for you for 400 slayer points. Would you like them to?");//6603 20251
			player.getInterfaceState().setNextDialogueId(0, 11865);
			break;
		case 11864:
			player.getActionSender().sendDialogue("Select an option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Have the master imbue your slayer helmet|Nevermind.");
			player.getInterfaceState().setNextDialogueId(0, 11866);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 11866:
			int points = player.getDatabaseEntity().getStatistics().getSlayerRewardPoints();
			if (points < 400) {
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null,
						"You need 400 slayer points to imbue your slayer helmet.");
				player.getInterfaceState().setNextDialogueId(0, -1);
			} else {
				player.getActionSender().removeChatboxInterface();
				if (player.getInventory().hasItem(new Item(11864))) {
					player.getInventory().remove(new Item(11864));
					player.getInventory().add(new Item(11865));
					player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(points - 400);
					player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11865, null, 
							"The slayer master imbues your helmet and hands it back to you; its effect will now pertain to magic and ranged attacks on slayer targets");//6603 20251
					player.getInterfaceState().setNextDialogueId(1, -1);
				}
			}
			break;

		case 11941:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"One|Five|All");
			player.getInterfaceState().setNextDialogueId(0, 11942);
			player.getInterfaceState().setNextDialogueId(1, 11943);
			player.getInterfaceState().setNextDialogueId(2, 11944);
			break;
		case 11942:
			Item one = player.getInterfaceAttribute("lootingBagItem");
			int oneIndex = player.getInterfaceAttribute("lootingBagIndex");
			LootingBagService lootingBagService = Server.getInjector().getInstance(LootingBagService.class);
			if (one != null && oneIndex != -1) {
				lootingBagService.deposit(player, oneIndex, one.getId(), 1);
			}
			break;
		case 11943:
			Item five = player.getInterfaceAttribute("lootingBagItem");
			int fiveIndex = player.getInterfaceAttribute("lootingBagIndex");
			lootingBagService = Server.getInjector().getInstance(LootingBagService.class);
			if (five != null && fiveIndex != -1) {
				lootingBagService.deposit(player, fiveIndex, five.getId(), 5);
			}
			break;
		case 11944:
			Item all = player.getInterfaceAttribute("lootingBagItem");
			int allIndex = player.getInterfaceAttribute("lootingBagIndex");
			lootingBagService = Server.getInjector().getInstance(LootingBagService.class);
			if (all != null && allIndex != -1) {
				lootingBagService.deposit(player, allIndex, all.getId(), player.getInventory().getCount(all.getId()));
			}
			break;


		case 12020:
			Map<GemBagService.Gems, Integer> gemBag = player.getDatabaseEntity().getGemBag();
			if (gemBag.size() <= 0) {
				return;
			}
			List<GemBagService.Gems> gems = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (Map.Entry pair : gemBag.entrySet()) {
				GemBagService.Gems key = (GemBagService.Gems) pair.getKey();
				int value = (int) pair.getValue();
				sb.append(key.getName()).append(" (").append(value).append(")|");
				gems.add(key);
			}
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					sb.toString());
			for (int i = 0; i < gems.size(); i++) {
				player.getInterfaceState().setNextDialogueId(i, 12021 + i);
				player.setInterfaceAttribute("gemBagType" + i, gems.get(i));
			}
			break;
		case 12021:
			player.getActionSender().removeChatboxInterface();
			GemBagService.Gems gemOne = player.getInterfaceAttribute("gemBagType0");
			if (gemOne != null) {
				gemBagService.withdraw(player, gemOne);
			}
			break;
		case 12022:
			player.getActionSender().removeChatboxInterface();
			GemBagService.Gems gemTwo = player.getInterfaceAttribute("gemBagType1");
			if (gemTwo != null) {
				gemBagService.withdraw(player, gemTwo);
			}
			break;
		case 12023:
			player.getActionSender().removeChatboxInterface();
			GemBagService.Gems gemThree = player.getInterfaceAttribute("gemBagType2");
			if (gemThree != null) {
				gemBagService.withdraw(player, gemThree);
			}
			break;
		case 12024:
			player.getActionSender().removeChatboxInterface();
			GemBagService.Gems gemFour = player.getInterfaceAttribute("gemBagType3");
			if (gemFour != null) {
				gemBagService.withdraw(player, gemFour);
			}
			break;
		case 12025:
			player.getActionSender().removeChatboxInterface();
			GemBagService.Gems gemFive = player.getInterfaceAttribute("gemBagType4");
			if (gemFive != null) {
				gemBagService.withdraw(player, gemFive);
			}
			break;


		case 12929:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "If I do this i'll lose my mutagen.... Should I continue?");
			player.getInterfaceState().setNextDialogueId(0, 12930);
			break;
		case 12930:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 12931);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 12931:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(13196)) {
				if (player.getInventory().add(new Item(12929))) {
					player.getInventory().remove(new Item(13196));
				}
			}
			break;

		case 12932:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "If I do this i'll lose my mutagen.... Should I continue?");
			player.getInterfaceState().setNextDialogueId(0, 12933);
			break;
		case 12933:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 12934);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		case 12934:
			player.getActionSender().removeChatboxInterface();
			if (player.getInventory().contains(13198)) {
				if (player.getInventory().add(new Item(12929))) {
					player.getInventory().remove(new Item(13198));
				}
			}
			break;

		case 13190:

			player.getActionSender().sendInterface(162, 546, 193, false)
			.sendAccessMask(1, 193, 2, -1, -1)
			.sendAccessMask(0, 193, 3, -1, -1)
			.sendAccessMask(0, 193, 4, -1, -1)

			.sendString(193, 1, "Are you sure you want to redeem this for premium status?")
			.sendString(193, 2, "Click here to continue.")
			.sendItemOnInterface(193, 0, 13190, 150);
			player.getInterfaceState().setNextDialogueId(0, 13191);
			break;
		case 13191:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 13192);
			player.getInterfaceState().setNextDialogueId(1, -1);
			break;
		/*case 13192:
			player.getActionSender().removeChatboxInterface();
			try {
				if (player.getInventory().contains(13190)) {
					player.getInventory().remove(new Item(13190));
					DonationManager.execute(player);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;*/
		case 13193:
			if (!player.hasAttribute("otherPlayer")) {
				player.getActionSender().removeChatboxInterface();
				return;
			}
			Player other = player.getAttribute("otherPlayer");
			player.getActionSender().sendInterface(162, 546, 193, false)
			.sendAccessMask(1, 193, 2, -1, -1)
			.sendAccessMask(0, 193, 3, -1, -1)
			.sendAccessMask(0, 193, 4, -1, -1)

			.sendString(193, 1, "Are you sure you want to redeem this premium status for " + other.getName() + "?")
			.sendString(193, 2, "Click here to continue.")
			.sendItemOnInterface(193, 0, 13190, 150);
			player.getInterfaceState().setNextDialogueId(0, 13194);
			break;
		case 13194:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Yes|No.");
			player.getInterfaceState().setNextDialogueId(0, 13195);
			player.getInterfaceState().setNextDialogueId(1, 13196);
			break;
		/*case 13195:
			if (!player.hasAttribute("otherPlayer")) {
				player.getActionSender().removeChatboxInterface();
				return;
			}
			other = player.getAttribute("otherPlayer");
			player.getActionSender().removeChatboxInterface();
			try {
				if (player.getInventory().contains(13190)) {
					player.getInventory().remove(new Item(13190));
					player.getActionSender().sendMessage("Successfully gave " + other.getName() + " premium status.");
					DonationManager.execute(other);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			player.removeAttribute("otherPlayer");
			break;*/
		case 13196:
			player.getActionSender().removeChatboxInterface();
			player.removeAttribute("otherPlayer");
			break;

			/**
			 * Teleport options
			 */

		case 25500:
			player.getActionSender().sendDialogue(" Teleports", ActionSender.DialogueType.TELEPORT_INTERFACE, -1, null, "Varrock|Edgeville|Rock crabs|Barrows");
			//    			player.getInterfaceState().setNextDialogueId(0, 15001);//15001
			//                player.getInterfaceState().setNextDialogueId(1, 15002);//15002
			//                player.getInterfaceState().setNextDialogueId(2, -1);//15003
			//                player.getInterfaceState().setNextDialogueId(3, -1);//15004
			//                player.getInterfaceState().setNextDialogueId(4, -1);//15005
			break;
		
			/**
			 * Pets
			 */
		case 16000: //Vet'ion Jr.
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Who is the true lord and king of the lands?");
			player.getInterfaceState().setNextDialogueId(0, 16001);
			break;
		case 16001:
			player.getActionSender().sendDialogue("Vet'ion Jr.", DialogueType.NPC, 5536, FacialAnimation.DEFAULT, 
					"The mighty heir and lord of the Wilderness.");
			player.getInterfaceState().setNextDialogueId(0, 16002);
			break;
		case 16002:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Where is he? Why hasn't he lifted your burden?");
			player.getInterfaceState().setNextDialogueId(0, 16003);
			break;
		case 16003:
			player.getActionSender().sendDialogue("Vet'ion Jr.", DialogueType.NPC, 5536, FacialAnimation.DEFAULT, 
					"I have not fulfilled my purpose.");
			player.getInterfaceState().setNextDialogueId(0, 16004);
			break;
		case 16004:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"What is your purpose?");
			player.getInterfaceState().setNextDialogueId(0, 16005);
			break;
		case 16005:
			player.getActionSender().sendDialogue("Vet'ion Jr.", DialogueType.NPC, 5536, FacialAnimation.DEFAULT, 
					"Not what is, what was. A great war tore this land apart and, for my failings in protecting this land, I carry the burden of its waste.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16050: //Callisto Cub
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Why the grizzly face?");
			player.getInterfaceState().setNextDialogueId(0, 16051);
			break;
		case 16051:
			player.getActionSender().sendDialogue("Callisto Cub", DialogueType.NPC, 497, FacialAnimation.DEFAULT, 
					"You're not funny...");
			player.getInterfaceState().setNextDialogueId(0, 16052);
			break;
		case 16052:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"You should get in the.... sun more.");
			player.getInterfaceState().setNextDialogueId(0, 16053);
			break;
		case 16053:
			player.getActionSender().sendDialogue("Callisto Cub", DialogueType.NPC, 497, FacialAnimation.DEFAULT, 
					"You're really not funny...");
			player.getInterfaceState().setNextDialogueId(0, 16054);
			break;
		case 16054:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"One second, let me take a picture of you with my.... kodiak camera.");
			player.getInterfaceState().setNextDialogueId(0, 16055);
			break;
		case 16055:
			player.getActionSender().sendDialogue("Callisto Cub", DialogueType.NPC, 497, FacialAnimation.DEFAULT, 
					".....");
			player.getInterfaceState().setNextDialogueId(0, 16056);
			break;
		case 16056:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
					"Feeling.... blue.");
			player.getInterfaceState().setNextDialogueId(0, 16057);
			break;
		case 16057:
			player.getActionSender().sendDialogue("Callisto Cub", DialogueType.NPC, 497, FacialAnimation.DEFAULT, 
					"If you don't stop, I'm going to leave some... brown... at your feet, human.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16060://venenatis Spiderling
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "It's a damn good thing I don't have arachnophobia.");
			player.getInterfaceState().setNextDialogueId(0, 16061);
			break;
		case 16061:
			player.getActionSender().sendDialogue("Venenatis Spiderling", DialogueType.NPC, 495, FacialAnimation.DEFAULT, "We're misunderstood. Without us in your house, you'd be infested with flies and other REAL nasties.");
			player.getInterfaceState().setNextDialogueId(0, 16062);
			break;
		case 16062:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Thanks for that enlightening fact.");
			player.getInterfaceState().setNextDialogueId(0, 16063);
			break;
		case 16063:
			player.getActionSender().sendDialogue("Venenatis Spiderling", DialogueType.NPC, 495, FacialAnimation.DEFAULT, "Everybody gets one.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16064://Scorpia's Offspring
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "At night time, if I were to hold ultraviolet light over you, would you glow?");
			player.getInterfaceState().setNextDialogueId(0, 16065);
			break;
		case 16065:
			player.getActionSender().sendDialogue("Scorpia's Offspring", DialogueType.NPC, 5547, FacialAnimation.DEFAULT, "Two things wrong there, human.");
			player.getInterfaceState().setNextDialogueId(0, 16066);
			break;
		case 16066:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Oh?");
			player.getInterfaceState().setNextDialogueId(0, 16067);
			break;
		case 16067:
			player.getActionSender().sendDialogue("Scorpia's Offspring", DialogueType.NPC, 5547, FacialAnimation.DEFAULT, "One, When has it ever been night time here?");
			player.getInterfaceState().setNextDialogueId(0, 16068);
			break;
		case 16068:
			player.getActionSender().sendDialogue("Scorpia's Offspring", DialogueType.NPC, 5547, FacialAnimation.DEFAULT, "Two, When have you ever seen ultraviolet light around here?");
			player.getInterfaceState().setNextDialogueId(0, 16069);
			break;
		case 16069:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Hm...");
			player.getInterfaceState().setNextDialogueId(0, 16070);
			break;
		case 16070:
			player.getActionSender().sendDialogue("Scorpia's Offspring", DialogueType.NPC, 5547, FacialAnimation.DEFAULT, "In answer to your question though. Yes I, like every scorpion, would glow.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16071://Prince Black Dragon
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Shouldn't a prince only have two heads?");
			player.getInterfaceState().setNextDialogueId(0, 16072);
			break;
		case 16072:
			player.getActionSender().sendDialogue("Prince Black Dragon", DialogueType.NPC, 6652, FacialAnimation.DEFAULT, "Why is that?");
			player.getInterfaceState().setNextDialogueId(0, 16073);
			break;
		case 16073:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Well, a standard Black dragon has one, the King has three so inbetween must have two?");
			player.getInterfaceState().setNextDialogueId(0, 16074);
			break;
		case 16074:
			player.getActionSender().sendDialogue("Prince Black Dragon", DialogueType.NPC, 6652, FacialAnimation.DEFAULT, "You're overthinking this.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16075://Pet Chaos Elemental
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Is it true a level 3 skiller caught one of your siblings?");
			player.getInterfaceState().setNextDialogueId(0, 16076);
			break;
		case 16076:
			player.getActionSender().sendDialogue("Chaos Elemental Jr.", DialogueType.NPC, 5907, FacialAnimation.DEFAULT, "Yes, they killed my mummy, kidnapped my brother, smiled about it and went to sleep");
			player.getInterfaceState().setNextDialogueId(0, 16077);
			break;
		case 16077:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Aww, well you have me now! I shall call you Squishy and you shall be mine and you shall be my Squishy," + 
					" Come on, Squishy come on, little Squishy!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16078://Pet Snakeling
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Hey little snake!");
			player.getInterfaceState().setNextDialogueId(0, 16079);
			break;
		case 16079:
			player.getActionSender().sendDialogue("Snakeling", DialogueType.NPC, 2127, FacialAnimation.DEFAULT, "Soon, Zulrah shall establish dominion over this plane.");
			player.getInterfaceState().setNextDialogueId(0, 16080);
			break;
		case 16080:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Wanna play fetch?");
			player.getInterfaceState().setNextDialogueId(0, 16081);
			break;
		case 16081:
			player.getActionSender().sendDialogue("Snakeling", DialogueType.NPC, 2127, FacialAnimation.DEFAULT, "Submit to the almighty Zulrah.");
			player.getInterfaceState().setNextDialogueId(0, 16082);
			break;
		case 16082:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Walkies? Or slidies...?");
			player.getInterfaceState().setNextDialogueId(0, 16083);
			break;
		case 16083:
			player.getActionSender().sendDialogue("Snakeling", DialogueType.NPC, 2127, FacialAnimation.DEFAULT, "Zulrah's wilderness as a God will soon be demonstrated.");
			player.getInterfaceState().setNextDialogueId(0, 16084);
			break;
		case 16084:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "I give up...");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16085://Kree'arra Jr.
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Huh... that's odd... I thought that would be big news.");
			player.getInterfaceState().setNextDialogueId(0, 16086);
			break;
		case 16086:
			player.getActionSender().sendDialogue("Kree'arra Jr.", DialogueType.NPC, 6643, FacialAnimation.DEFAULT, "You thought what would be big news?");
			player.getInterfaceState().setNextDialogueId(0, 16087);
			break;
		case 16087:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Well there seems to be an absence of a certain ornithological piece: a headline regarding mass awareness of a certain avian variety.");
			player.getInterfaceState().setNextDialogueId(0, 16088);
			break;
		case 16088:
			player.getActionSender().sendDialogue("Kree'arra Jr.", DialogueType.NPC, 6643, FacialAnimation.DEFAULT, "What are you talking about?");
			player.getInterfaceState().setNextDialogueId(0, 16089);
			break;
		case 16089:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Oh have you not heard? It was my understanding that everyone had heard....");
			player.getInterfaceState().setNextDialogueId(0, 16090);
			break;
		case 16090:
			player.getActionSender().sendDialogue("Kree'arra Jr.", DialogueType.NPC, 6643, FacialAnimation.DEFAULT, "Heard wha...... OH NO!!!!?!?!!?!");
			player.getInterfaceState().setNextDialogueId(0, 16091);
			break;
		case 16091:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "OH WELL THE BIRD, BIRD, BIRD, BIRD BIRD IS THE WORD. OH WELL THE BIRD, BIRD, BIRD, BIRD BIRD IS THE WORD.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16092://General Graardor
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Not sure this is going to be worth my time but... how are you?");
			player.getInterfaceState().setNextDialogueId(0, 16093);
			break;
		case 16093:
			player.getActionSender().sendDialogue("General Graardor Jr.", DialogueType.NPC, 6644, FacialAnimation.DEFAULT, "SFudghoigdfpDSOPGnbSOBNfdbdnopbdnopbddfnopdfpofhdARRRGGGGH");
			player.getInterfaceState().setNextDialogueId(0, 16094);
			break;
		case 16094:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Nope. Not worth it.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			
		case 16095:
			if (player.getInventory().contains(11806) || player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 11806) {
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "I FOUND THE GODSWORD!");
				player.getInterfaceState().setNextDialogueId(0, 16097);
			} else {
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "FIND THE GODSWORD!");
				player.getInterfaceState().setNextDialogueId(0, 16099);
			}
			break;
		case 16097:
			player.getActionSender().sendDialogue("Zilyana Jr.", DialogueType.NPC, 6646, FacialAnimation.DEFAULT, "GOOD!!!!!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 16099:
			player.getActionSender().sendDialogue("Zilyana Jr.", DialogueType.NPC, 6646, FacialAnimation.DEFAULT, "FIND THE GODSWORD!");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 16100:
			if(player.getInventory().contains(950))
			{
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, 
						"Hello. I have some fine silk from Al-Kharid to sell to you.");
				player.getInterfaceState().setNextDialogueId(0, 16102);
			}
			else
			{
				player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Hey.");
				player.getInterfaceState().setNextDialogueId(0, 16101);
			}
			break;
		case 16101:
			player.getActionSender().sendDialogue("Silk Merchant", DialogueType.NPC, 1043, FacialAnimation.DEFAULT, "Hi.");
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 16102:
			player.getActionSender().sendDialogue("Silk Merchant", DialogueType.NPC, 1043, FacialAnimation.DEFAULT, 
					"Ah I may be interested. What sort of price were you looking at per piece of silk?");
			player.getInterfaceState().setNextDialogueId(0, 16103);
			break;
		case 16103:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"20 coins.|80 coins.|120 coins.|200 coins.");
			player.getInterfaceState().setNextDialogueId(0, 16104);
			player.getInterfaceState().setNextDialogueId(1, -1);
			player.getInterfaceState().setNextDialogueId(2, 16106);
			player.getInterfaceState().setNextDialogueId(3, -1);
			break;
		case 16104:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "20 coins.");
			player.getInterfaceState().setNextDialogueId(0, 16105);
			break;
		case 16105:
			player.getActionSender().sendDialogue("Silk Merchant", DialogueType.NPC, 1043, FacialAnimation.DEFAULT, 
					"Deal.");
			player.sellSilk(20);
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 16106:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "120 coins.");
			player.getInterfaceState().setNextDialogueId(0, 16107);
			break;
		case 16107:
			player.getActionSender().sendDialogue("Silk Merchant", DialogueType.NPC, 1043, FacialAnimation.DEFAULT, 
					"You'll never get that much for it. I'll be generous and give you 50 for it.");
			player.getInterfaceState().setNextDialogueId(0, 16108);
			break;
		case 16108:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT,
					"Ok, I guess 50 will do.|I'll give it to you for 60.|No that is not enough.");
			player.getInterfaceState().setNextDialogueId(0, 16109);
			player.getInterfaceState().setNextDialogueId(1, 16110);
			player.getInterfaceState().setNextDialogueId(2, -1);
			break;
		case 16109:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Ok, I guess 50 will do.");
			player.getInterfaceState().setNextDialogueId(0, 16111);
			break;
		case 16110:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "I'll give it to you for 60.");
			player.getInterfaceState().setNextDialogueId(0, 16112);
			break;
		case 16111:
			player.getActionSender().sendDialogue("Silk Merchant", DialogueType.NPC, 1043, FacialAnimation.DEFAULT, 
					"Deal.");
			player.sellSilk(50);
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
		case 16112:
			player.getActionSender().sendDialogue("Silk Merchant", DialogueType.NPC, 1043, FacialAnimation.DEFAULT, 
					"Deal.");
			player.sellSilk(60);
			player.getInterfaceState().setNextDialogueId(0, -1);
			break;
			/**
			 * World Switcher (Widget 69)
			 */
		case 6969:
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.WORLD_SWITCHER, -1, null, "");//
			break;

		}
	}

	public static void advanceDialogue(Player player, int index) {
        int dialogueId = player.getInterfaceState().getNextDialogueId(index);
        if (dialogueId == -1) {
            player.getActionSender().removeChatboxInterface();
            return;
        }
        openDialogue(player, dialogueId);
    }

	public static int getSkillId(int npcId) {
		switch (npcId) {
		case 2460:
			return Skills.ATTACK;
		case 3216:
			return Skills.DEFENCE;
		case 2473:
			return Skills.STRENGTH;
		case 2578:
			return Skills.PRAYER;
		case 6059:
			return Skills.RANGE;
		case 2658:
			return Skills.COOKING;
		case 1044:
			return Skills.FLETCHING;
		case 118:
			return Skills.FIREMAKING;
		case 5045:
			return Skills.HERBLORE;
		case 3193:
			return Skills.THIEVING;
		case 5810:
			return Skills.CRAFTING;
		case 2913:
			return Skills.FISHING;
		case 3249:
			return Skills.MAGIC;
		case 3343:
			return Skills.HITPOINTS;
		case 4733:
			return Skills.SMITHING;
		case 3226:
			return Skills.WOODCUTTING;
		case 405:
			return Skills.SLAYER;
		case 3363:
			return Skills.MINING;
		case 5382:
			return Skills.FARMING;
		case 637:
			return Skills.RUNECRAFTING;
		}
		return 0;
	}

}