package org.rs2server.rs2.content;

import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender;

/**
 * @author Scripts
 * @since 01/13/2017
 */
public class TeleportManager {
	/**
	 * @author Scripts
	 * Sends the Interface/CS2Script/Access Mask to the entity.
	 */
	public static void handleTeleport(Player player) {
		player.getActionSender().sendInterface(187, false);
		player.getActionSender().sendCS2Script(217, new Object[] {""
				+ "||||||||<col=ff><u=000000>Training|Rock Crabs|Yaks|Experiments|Hill Giants|Sand Crabs|"
				+ "<col=ff><u=000000>Dungeons|Edgeville|Taverly|Brimhaven|Fremminick Slayer Cave|"
				+ "Stronghold Slayer Cave|Asgarnia|Baxtorian Falls|Mos'le Harmless|Kourend Catacombs|Crashsite Caverns|Ancient Caverns"
				+ "|<col=ff><u=000000>Minigames|Barrows|Pest Control|Warriors Guild|Duel Arena|Fight Caves|Wintertodt|"
				+ "<col=ff><u=000000>Skilling|Woodcutting Guild|Catherby Pier|Piscatoris Fishing Colony|Fishing Guild|"
				+ "Varrock Smithing|Varrock Mining|Mining Guild|Falador Mining|Motherlode Mines|Puro Puro|Gnome Agility Course<col=880000>(Lvl 1 Agility)|"
				+ "Draynor Rooftop Course<col=880000>(Lvl 10 Agility)|Varrock Rooftop Course<col=880000>(Lvl 30 Agility)|"
				+ "Barbarian Agility Course<col=880000>(Lvl 35 Agility)|Seer's Rooftop Course<col=880000>(Lvl 60 Agility)|"
				+ "Ardougne Rooftop Course<col=880000>(Lvl 90 Agility)|Ardougne Market<col=880000>(Level 1 Thieving)|"
				+ "Rogues Den<col=880000>(Level 50 Thieving)|"
				+ "<col=ff><u=000000>Bosses|Kalphite Queen|King Black Dragon<col=880000>(Lvl 42 wildy)|Corporeal Beast|"
				+ "Abyssal Sire|Godwars|Lizardman Shaman|Zulrah|Dagannoth Kings|Giant Mole|Raids|"
				+ "<col=880000><u=000000>Wilderness|Mage Bank<col=48f442>(Safe)|Lava Dragons<col=880000>(Lvl 43 Wildy)|"
				+ "PvP Castle<col=e58106>(Lvl 15 Wildy)|Wilderness Resource Dungeon<col=880000>(Lvl 55 Wildy)|"
				+ "West Dragons<col=e58106>(Lvl 10 Wildy)|East Dragons<col=e58106>(Lvl 19 Wildy)|Edgeville Wildy|"
				+ "<col=ff><u=000000>Miscellaneous|Entrana|Lunar Altar|Ancient Altar|Dark Altar", "Wizard Distentor's Destinations", 0}, "Iss");//Iss
		player.sendAccess(Access.of(187, 3, NumberRange.of(0, 170), AccessBits.CLICK_CONTINUE));
	}
	
	/**
	 * @author Scripts
	 * Handles the Options for the Teleports
	 * Do not use Options 0 -> 5; Using those Options will break the Dialogue system.
	 */
	public static boolean handleTeleportAction(Player player, int option) {
		switch (option) {
		case 8:
			player.getActionSender().sendMessage("Teleports that take you to various combat training areas.");
			break;
		case 9://Monster Teleports Option
			player.teleport(Location.create(2673, 3714, 0), 0, 0, false);//rock crabs
			player.getActionSender().closeAll();
			break;
		case 10:
			player.teleport(Location.create(2321, 3804, 0), 0, 0, false);//yak
			player.getActionSender().closeAll();
			break;
		case 11:
			player.teleport(Location.create(3577, 9927, 0), 0, 0, false);//experiments
			player.getActionSender().closeAll();
			break;
		case 12:
			player.teleport(Location.create(3117, 9860, 0), 0, 0, false);//Hill Giants
			player.getActionSender().closeAll();
			break;
		case 13:
			player.teleport(Location.create(1695, 3477, 0), 0, 0, false);//Sand crabs
			player.getActionSender().closeAll();
			break;
		case 14:
			player.getActionSender().sendMessage("Teleports that take you to various Dungeons.");
			break;
		case 15:
			player.teleport(Location.create(3097, 9868, 0), 0, 0, false);//edge dungeon
			player.getActionSender().closeAll();
			break;
		case 16:
			player.teleport(Location.create(2884, 9798, 0), 0, 0, false);//taverly dungeon
			player.getActionSender().closeAll();
			break;
		case 17:
			player.teleport(Location.create(2744, 3148, 0), 0, 0, false);//brimhaven dungeon
			player.getActionSender().closeAll();
			break;
		case 18:
			player.teleport(Location.create(2807, 10003, 0), 0, 0, false);//fremmenik dungeon
			player.getActionSender().closeAll();
			break;
		case 19:
			player.teleport(Location.create(2433, 3423, 0), 0, 0, false);//slayer dungeon
			player.getActionSender().closeAll();
			break;
		case 20:
			player.teleport(Location.create(3007, 9549, 0), 0, 0, false);//asgarnian dungeon
			player.getActionSender().closeAll();
//			player.teleport(Location.create(2603, 3402, 0), 0, 0, false);
//			player.getActionSender().closeAll();
//			player.getActionSender().sendMessage("Teleporting to Fishing Guild.");
			break;
		case 21:
			player.teleport(Location.create(2575, 9862, 0), 0, 0, false);//waterfall dungeon
			player.getActionSender().closeAll();
			break;
		case 22:
			player.teleport(Location.create(3747, 9373, 0), 0, 0, false);//mos'le dungeon
			player.getActionSender().closeAll();
			break;
		case 23:
			player.teleport(Location.create(1665, 10051, 0), 0, 0, false);//Kourend catacombs
			player.getActionSender().closeAll();
			break;
			
		case 24:
			player.teleport(Location.create(2027, 5611, 0), 0, 0, false);//Crashsite caverns
			player.getActionSender().closeAll();
			break;
		case 25:
			player.teleport(Location.create(1747, 5324, 0), 0, 0, false);//ancient caverns
			player.getActionSender().closeAll();
			break;
		case 26://Minigames
			player.getActionSender().sendMessage("Teleports that take you to various Minigames.");
			player.getActionSender().closeAll();
			break;
		case 27://Warrior's guild
			player.teleport(Location.create(3565, 3316, 0), 0, 0, false);
			player.getActionSender().closeAll();
			/*player.teleport(Location.create(2841, 3538, 0), 0, 0, false);
			player.getActionSender().closeAll()*/;
			break;
		case 28:
			player.teleport(Location.create(2659, 2659, 0), 0, 0, false);//pest control
			player.getActionSender().closeAll();
			break;
			
		case 29:
			player.teleport(Location.create(2841, 3538, 0), 0, 0, false);//warriors guild
			player.getActionSender().closeAll();
			/*player.teleport(Location.create(1630, 3983, 0), 0, 0, false);
			player.getActionSender().closeAll();
			player.getActionSender().sendMessage("<col=ff>Welcome to the Wintertodt.");
			player.getActionSender().sendMessage("<col=ff>Their is a bank chest and a bank deposit box to the south.");
			player.getActionSender().sendMessage("<col=ff>Gather 'Survival Tokens' to purchase Supply crates from 'Ignisia'.");*/
			break;
			
		case 30:
			player.teleport(Location.create(3316, 3235, 0), 0, 0, false);//duel arena
			player.getActionSender().closeAll();
			break;
			
		case 31:	
			player.teleport(Location.create(2441, 5171, 0), 0, 0, false);//fight caves
			player.getActionSender().closeAll();
			break;
		case 32:
			//player.teleport(Location.create(1630, 3983, 0), 0, 0, false);//wintertodt
			player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 20703, null, "Wintertodt has not yet been added.");
			player.getActionSender().closeAll();
			break;
			
		case 33:
			player.getActionSender().sendMessage("Teleports that take you to various Skilling locations.");
			player.getActionSender().closeAll();
			break;
		case 34:
			player.teleport(Location.create(1657, 3505, 0), 0, 0, false);//woodcuttin guild
			player.getActionSender().closeAll();
			break;
		case 35:
			player.teleport(Location.create(2808, 3434, 0), 0, 0, false);//catherby pier
			player.getActionSender().closeAll();
			break;
		case 36:
			player.teleport(Location.create(2344, 3690, 0), 0, 0, false);//piscatoris colony
			player.getActionSender().closeAll();
			break;
		case 37:
			player.teleport(Location.create(2590, 3416, 0), 0, 0, false);//fishing guild
			player.getActionSender().closeAll();
			break;
		case 38:
			player.teleport(Location.create(3183, 3430, 0), 0, 0, false);//Varrock Smithing
			player.getActionSender().closeAll();
			break;
		case 39:
			player.teleport(Location.create(3285, 3372, 0), 0, 0, false);//Varrock Mining
			player.getActionSender().closeAll();
			break;
		case 40:
			player.teleport(Location.create(3023, 3339, 0), 0, 0, false);//Mining Guild 
			player.getActionSender().closeAll();
			break;
		case 41:
			player.teleport(Location.create(3053, 9774, 0), 0, 0, false);//Falador Mining
			player.getActionSender().closeAll();
			break;
		case 42:
			player.teleport(Location.create(3726, 5687, 0), 0, 0, false);//Motherlode Mine
			player.getActionSender().closeAll();
			break;
		case 43:
			player.teleport(Location.create(2592, 4317, 0), 0, 0, false);//purro purro
			player.getActionSender().closeAll();
			break;
		case 44:
			player.teleport(Location.create(2474, 3436, 0), 0, 0, false);//Gnome agility
			player.getActionSender().closeAll();
			break;
		case 45:
			player.teleport(Location.create(3107, 3279, 0), 0, 0, false);//Draynor rooftop
			player.getActionSender().closeAll();
			break;
		case 46:
			player.teleport(Location.create(3223, 3414, 0), 0, 0, false);//varrock rooftop
			player.getActionSender().closeAll();
			break;
		case 47:
			player.teleport(Location.create(2543, 3568, 0), 0, 0, false);//Barb agility
			player.getActionSender().closeAll();
			break;
		case 48:
			player.teleport(Location.create(2729, 3485, 0), 0, 0, false);//Seer's rooftop
			player.getActionSender().closeAll();
			break;
		case 49:
			player.teleport(Location.create(2673, 3294, 0), 0, 0, false);//Ardougne rooftop
			player.getActionSender().closeAll();
			break;
		case 50:
			player.teleport(Location.create(2662, 3305, 0), 0, 0, false);//ardougne market
			player.getActionSender().closeAll();
			break;
		case 51:
			player.teleport(Location.create(3047, 4974, 1), 0, 0, false);//Rogue's den
			player.getActionSender().closeAll();
			break;
		case 52:
			player.getActionSender().sendMessage("Teleports that take you to various Bosses.");
			player.getActionSender().closeAll();
			break;
		case 53:
			player.teleport(Location.create(3506, 9493, 0), 0, 0, false);//Kalphite Queen
			player.getActionSender().closeAll();
			break;
		case 54:
			player.teleport(Location.create(2997, 3849, 0), 0, 0, false);//kbd
			player.getActionSender().closeAll();
			break;
		case 55:
			player.teleport(Location.create(2948, 4385, 2), 0, 0, false);//Corp
			player.getActionSender().closeAll();
			break;
		case 56:
			player.teleport(Location.create(3037, 4766, 0), 0, 0, false);//Abyssal sire
			player.getActionSender().closeAll();
			break;
		case 57:
			player.teleport(Location.create(2880, 5311, 2), 0, 0, false);//Godwars
			player.getActionSender().closeAll();
			break;
		case 58:
			player.teleport(Location.create(1464, 3688, 0), 0, 0, false);//shaman
			player.getActionSender().closeAll();
			break;
		case 59:
			player.teleport(Location.create(2199, 3056, 0), 0, 0, false);//Zulrah
			player.getActionSender().closeAll();
			break;
		case 60:
			player.teleport(Location.create(1910, 4367, 0), 0, 0, false);//Dag kings
			player.getActionSender().closeAll();
			break;
		case 61:
			player.teleport(Location.create(1761, 5197, 0), 0, 0, false);//Giant mole
			player.getActionSender().closeAll();
			break;
		case 62:
			player.teleport(Location.create(1258, 3564, 0), 0, 0, false);//Raids
			player.getActionSender().closeAll();
			break;
		case 63:
			player.getActionSender().sendMessage("Teleports that take you to various points in the Wilderness.");
			player.getActionSender().closeAll();
			break;
		case 64:
			player.teleport(Location.create(2539, 4716, 0), 0, 0, false);//Mage bank
			player.getActionSender().closeAll();
			break;
		case 65:
			player.teleport(Location.create(3202, 3859, 0), 0, 0, false);//Lava dragons
			player.getActionSender().closeAll();
			break;
		case 66:
			player.teleport(Location.create(3012, 3632, 0), 0, 0, false);//PvP Castle
			player.getActionSender().closeAll();
			break;
		case 67:
			player.teleport(Location.create(3184, 3953, 0), 0, 0, false);//Resource dungeon
			player.getActionSender().closeAll();
			break;
		case 68:
			player.teleport(Location.create(2985, 3596, 0), 0, 0, false);//west dragons
			player.getActionSender().closeAll();
			break;
		case 69:
			player.teleport(Location.create(3351, 3670, 0), 0, 0, false);//east dragons
			player.getActionSender().closeAll();
			break;
		case 70:
			player.teleport(Location.create(3104, 3518, 0), 0, 0, false);//edgeville wild
			player.getActionSender().closeAll();
			break;
		case 71:
			player.getActionSender().sendMessage("Teleports that take you to other miscellaneous places.");
			player.getActionSender().closeAll();
			break;
		case 72:
			player.teleport(Location.create(2834, 3335, 0), 0, 0, false);//Entrana  3235, 9312
			player.getActionSender().closeAll();
			break;
		case 73:
			player.teleport(Location.create(2154, 3866, 0), 0, 0, false);//Lunar Altar
			player.getActionSender().closeAll();
			break;
		case 74:
			player.teleport(Location.create(3235, 9312, 0), 0, 0, false);//Ancient Altar
			player.getActionSender().closeAll();
			break;
		case 75:
			player.teleport(Location.create(1712, 3883, 0), 0, 0, false);//Dark Altar
			player.getActionSender().closeAll();
			break;
		}
		return false;
	}

}
