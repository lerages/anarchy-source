package org.rs2server.rs2;

import org.rs2server.rs2.content.dialogue.DialogueChain;
import org.rs2server.rs2.content.dialogue.TalkingDialogue;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.container.Equipment;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

/**
 * Holds global server constants.
 * @author Graham Edgecombe
 *
 */
public class Constants 
{

	/**
	 * The Lost-Isle website URL.
	 */
	public static final String WEBSITE_URL = "http://os-anarchy.com";
	//websites online

	/**
	 * Connects to LoginServer if true. More work needs to be done also,
	 */
	public static boolean CONNNECT_TO_LOGIN_SERVER = true;
	
	public static int PEST_MODIFIER = 1;
	
	/**
	 * The exp modifier
	 */
	public static final int EXP_MODIFIER = 1;//
	
	/**
	 * Combat EXP Modifier
	 */
	public static final int COMBAT_EXP = 1;
	
	/**
	 * Skill EXP Modifier
	 */
	public static final int SKILL_EXP = 10;
	
	/**
	 * The main screen window pane.
	 */
	public static final int MAIN_WINDOW = 548;
	
	/**
	 * The game window area.
	 */
	public static final int GAME_WINDOW = 77;
	
	/**
	 * The side tabs area.
	 */
	public static final int SIDE_TABS = 165;//126; i think
	
	/**
	 * The chat box area.
	 */
	public static final int CHAT_BOX = 120;
	
	/**
	 * The login screen window pane.
	 */
	public static final int LOGIN_SCREEN = 549;
	
	public static final Object[] GE_PARAMS = new Object[] { "", "", "", "Examine", "Offer", -1, 0, 7, 4, 93, 7012370 };
	
	/**
	 * The parameters for the equipment screen.
	 */
	public static final Object[] EQUIPMENT_PARAMETERS = new Object[] { "", "", "", "", "", "", "", "", "Wear<col=ff9040>", -1, 0, 7, 4, 98, 43909120 };

	/**
	 * The sting to send on the equipment interface run script.
	 */
	public final static String EQUIPMENT_TYPE_STRING = "IviiiIsssssssss";
	
	/**
	 * The message of the week, as displayed on the login screen.
	 */
	public static final String MESSAGE_OF_THE_WEEK = Constants.SERVER_NAME + " is currently in the beta stage, please report any bugs you find.";
	
	/**
	 * The interface sent in a run script for numerical input.
	 */
	public static final int NUMERICAL_INPUT_INTERFACE = 108;
	
	/**
	 * The interface sent in a run script for alphabetical & numerical input.
	 */
	public static final int ALPHA_NUMERICAL_INPUT_INTERFACE = 110;
	
	/**
	 * The interface sent to remove chat box interface input.
	 */
	public static final int REMOVE_INPUT_INTERFACE = 101;

	/**
	 * The first set of trade parameters.
	 */
	public final static Object[] TRADE_PARAMETERS_1 = new Object[] { -2, 0, 7, 4, 80, 335 << 16 | 29};

	/**
	 * The offer parameters.
	 */
	public final static Object[] OFFER_PARAMETERS = new Object[] { "", "", "", "", "Offer-X", "Offer-All", "Offer-10", "Offer-5", "Offer", -1, 0, 7, 4, 82, 22020096};

	/**
	 * The second set of trade parameters.
	 */
	public final static Object[] TRADE_PARAMETERS_2 = new Object[] { "", "", "", "", "Remove-X", "Remove-All", "Remove-10", "Remove-5", "Remove", -1, 0, 7, 4, 81, 335 << 16 | 27};

	/**
	 * The sting to send on the trade interface run script.
	 */
	public final static String TRADE_TYPE_STRING = "IviiiIsssssssss";

	/**
	 * The set of sell parameters for shopping.
	 */
	public static final Object[] SELL_PARAMETERS = new Object[] { "Sell 50<col=ff9040>", "Sell 10<col=ff9040>", "Sell 5<col=ff9040>", "Sell 1<col=ff9040>", "Value<col=ff9040>", -1, 0, 7, 4, 93, 19726336};

	/**
	 * The set of buy parameters for shopping.
	 */
	public static final Object[] BUY_PARAMETERS = new Object[] { "", "", "", "", "Buy-X", "Buy-X", "Buy-5", "Buy-1", "Value", -1, 0, 4, 10, 91, 40632344 };//40632344

	/**
	 * The sting to send on the shopping interface run script.
	 */
	public final static String MAIN_STOCK_TYPE_STRING = "vg";

	/**
	 * The sting to send on the shopping interface run script.
	 */
	public final static String MAIN_STOCK_OPTIONS_STRING = "IviiiIsssssssss";
	
	/**
	 * The examine option for run scripts.
	 */
	public static final int SCRIPT_OPTIONS_EXAMINE = 1278;
	
	/**
	 * The media displayed on the message of the week.
	 * 16 = Moving cogs
	 * 17 = Question marks
	 * 18 = Drama faces
	 * 19 = Bank pin vaults
	 * 20 = Bank pin question marks
	 * 21 = Player scamming
	 * 22 = Bank pin vaults with moving key
	 * 23 = Christmas presents & Santa
	 * 24 = Killcount TODO: Useful in future
	 */
	
	public static final int MESSAGE_OF_THE_WEEK_SCREEN = 16;//17
	
	/**
	 * The server's name.
	 */
	public static final String SERVER_NAME = "OS-Anarchy";
	
	/**
	 * Bonuses as displayed on the equipment screen.
	 */
	public static final String[] BONUSES = { "Stab", "Slash", "Crush", "Magic", "Range", "Stab", "Slash", "Crush", "Magic", "Range",
		"Melee strength", "Ranged strength", "Magic damage", "Prayer", "Undead", "Slayer"
	};
	
	/**
	 * The directory for the engine scripts.
	 */
	public static final String SCRIPTS_DIRECTORY = "./data/scripts/";
	
	/**
	 * Difference in X coordinates for directions array.
	 */
	public static final byte[] DIRECTION_DELTA_X = new byte[] {-1, 0, 1, -1, 1, -1, 0, 1};
	
	/**
	 * Difference in Y coordinates for directions array.
	 */
	public static final byte[] DIRECTION_DELTA_Y = new byte[] {1, 1, 1, 0, 0, -1, -1, -1};
	
	/**
	 * Default sidebar interfaces array.
	 */
	public static final int SIDEBAR_INTERFACES[][] = new int[][] {
		new int[] {
			8, 18, 19, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73
		},
		new int[] {
			160, 162, 163, 593, 320, 274, 216, 387, 541, 218, 589, 429, 432, 182, 261, 216, 239//271
		}
	};
	
	public static final int[] SKILL_IDS = {1, 2};
	
	public static final boolean CHECK_FORUM_REGISTRATION = false;//true
	
	/**
	 * Incoming packet sizes array.
	 */
	public static final int[] PACKET_SIZES = new int[256];
	
	public static final int MAX_LEVEL = 2081;

	static {
		for (int i = 0; i < PACKET_SIZES.length; i++)
			PACKET_SIZES[i] = -3;
		PACKET_SIZES[177] = -1;
		PACKET_SIZES[118] = -1; // mystery
		PACKET_SIZES[106] = 1; // some boolean flag
		PACKET_SIZES[132] = 0; // unidentified
		
		PACKET_SIZES[89] = 6; 
		PACKET_SIZES[161] = -1;
		PACKET_SIZES[235] = -1;
		PACKET_SIZES[83] = -2;
		PACKET_SIZES[55] = -1;
		PACKET_SIZES[250] = 3;
		PACKET_SIZES[255] = 8;
		PACKET_SIZES[198] = 8;
		PACKET_SIZES[128] = -1;
		PACKET_SIZES[171] = -2;
		PACKET_SIZES[111] = 3;
		PACKET_SIZES[199] = 0;
		PACKET_SIZES[65] = 3; //player option 5
        PACKET_SIZES[232] = 11; //item on npc
        PACKET_SIZES[95] = 0; //idle packet
		PACKET_SIZES[44] = 14; //magic on item
		PACKET_SIZES[74] = 13; //magic on ground item
		PACKET_SIZES[121] = 9; //magic on npc
		PACKET_SIZES[206] = 2; //ge packet
		PACKET_SIZES[3] = 6; //dialogue handler
		PACKET_SIZES[78] = 4; //welcome screen
		PACKET_SIZES[176] = 16; //item on item
		PACKET_SIZES[29] = -1; //clan ranking
		PACKET_SIZES[204] = -3; //clan kick
		PACKET_SIZES[136] = 3; //npc option 1
		PACKET_SIZES[212] = 3; //npc option 2
		PACKET_SIZES[52] = 3; //npc option trade
		PACKET_SIZES[202] = 2; //npc examine
		PACKET_SIZES[233] = 13; //Character Design
		PACKET_SIZES[114] = 8; //shop value
		PACKET_SIZES[158] = 8; //shop option 1
		PACKET_SIZES[122] = 8; //shop option 5
		PACKET_SIZES[215] = 8; //shop option 10
		PACKET_SIZES[238] = 15; // item on object
		PACKET_SIZES[64] = 4; //enter amount
		PACKET_SIZES[166] = 7; //object option 1
		PACKET_SIZES[5] = 7; // pickup item
		PACKET_SIZES[183] = 8; // drop item
		PACKET_SIZES[129] = 5; //resize packet
		PACKET_SIZES[239] = 3;//follow player
		PACKET_SIZES[39] = 3;//challenge player
		PACKET_SIZES[156] = 13; //spell on object
		PACKET_SIZES[188] = 7; //object option 2
		PACKET_SIZES[218] = 7; //object option 3
		PACKET_SIZES[101] = 2; //object examine
		PACKET_SIZES[150] = 9; //magic on player
		PACKET_SIZES[49] = -1; // remove friend
		PACKET_SIZES[179] = -1; // add ignore
		PACKET_SIZES[180] = -1; // remove ignore
		PACKET_SIZES[0] = 8; // idk
		PACKET_SIZES[51] = 3; // Friends list status
		
		PACKET_SIZES[149] = 8; // item option 1
		PACKET_SIZES[194] = 8; // item option 2
		PACKET_SIZES[159] = 8; // item option 3
		PACKET_SIZES[245] = 8; // item option 4
		PACKET_SIZES[116] = 2; // item option 4
		PACKET_SIZES[46] = 8; // item option 5
		PACKET_SIZES[59] = 16; // move items
		PACKET_SIZES[6] = 9; // move items

        PACKET_SIZES[170] = 11; // item on player

        PACKET_SIZES[157] = -1; // set clan prefix
		PACKET_SIZES[241] = -1; // join clan chat
		
		PACKET_SIZES[2] = -1; // enter input
	}
	
	/**
	 * 474 Update Reference Keys
	 */
	public static final int[] UPDATE_KEYS_INT = { 0xff, 0x0, 0xff, 0x0, 0x0, 0x0,
	    0x0, 0x80, 0xfe, 0xbb, 0xa4, 0x5f, 0x0, 0x0, 0x0, 0x0, 0x2b, 0x3d,
	    0x5c, 0xd8, 0x0, 0x0, 0x0, 0x0, 0xf9, 0xb4, 0x1a, 0xe1, 0x0, 0x0,
	    0x0, 0xfe, 0x5c, 0xb0, 0x6b, 0xd7, 0x0, 0x0, 0x0, 0x6c, 0x5a, 0x62,
	    0xe0, 0x19, 0x0, 0x0, 0x0, 0x14, 0xa6, 0x84, 0x2e, 0x77, 0x0, 0x0,
	    0x0, 0x54, 0xa, 0xe4, 0x31, 0x30, 0x0, 0x0, 0x0, 0x0, 0x67, 0xf7,
	    0x9b, 0x5a, 0x0, 0x0, 0x0, 0x74, 0x2a, 0x13, 0x9d, 0xf8, 0x0, 0x0,
	    0x0, 0x19, 0xc9, 0xa3, 0x46, 0x3a, 0x0, 0x0, 0x0, 0x3, 0x2e, 0xcb,
	    0xa4, 0xad, 0x0, 0x0, 0x0, 0x0, 0x1e, 0x2c, 0xdd, 0x62, 0x0, 0x0,
	    0x0, 0x0, 0x81, 0xc7, 0xcc, 0x8a, 0x0, 0x0, 0x0, 0x53, 0x7, 0x8e,
	    0x6a, 0x3e, 0x0, 0x0, 0x0, 0x1, 0xa3, 0x8c, 0xf6, 0x94, 0x0, 0x0,
	    0x0, 0x1, 0xb8, 0xf2, 0x4d, 0x21, 0x0, 0x0, 0x0, 0x0 };
	
	public static final byte[] UPDATE_KEYS = new byte[UPDATE_KEYS_INT.length];
	
	static {
		int index = 0;
		for (int i : UPDATE_KEYS_INT)
			UPDATE_KEYS[index++] = (byte) i;
	}

	/**
	 * The player cap.
	 */
	public static final int MAX_PLAYERS = 2000;
	
	/**
	 * The NPC cap.
	 */
	public static final int MAX_NPCS = 32000;
	
	/**
	 * An array of valid characters in a long username.
	 */
	public static final char VALID_CHARS[] = { '_', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
		'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '!', '@', '#', '$', '%', '^', '&',
		'*', '(', ')', '-', '+', '=', ':', ';', '.', '>', '<', ',', '"',
		'[', ']', '|', '?', '/', '`' };
	
	/**
	 * Packed text translate table.
	 */
	public static final char XLATE_TABLE[] = { ' ', 'e', 't', 'a', 'o', 'i', 'h', 'n',
		's', 'r', 'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p', 'b',
		'v', 'k', 'x', 'j', 'q', 'z', '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', ' ', '!', '?', '.', ',', ':', ';', '(', ')', '-',
		'&', '*', '\\', '\'', '@', '#', '+', '=', '\243', '$', '%', '"',
		'[', ']' };
	
	public static final int[] VERSION_TABLE = new int[] {
	    0xFF, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x80, 
	    0x2D, 0xE3, 0xC7, 0xB7, 0x00, 0x00, 0x00, 0x00, 
	    0x05, 0xF9, 0x00, 0x72, 0x00, 0x00, 0x00, 0x00, 
	    0xDC, 0x0B, 0x2A, 0xE9, 0x00, 0x00, 0x03, 0x0B, 
	    0xF2, 0x89, 0x0D, 0x78, 0x00, 0x00, 0x01, 0x4B, 
	    0x94, 0x8D, 0x54, 0xE5, 0x00, 0x00, 0x00, 0x0F, 
	    0x3D, 0xDC, 0x62, 0x24, 0x00, 0x00, 0x00, 0xB7, 
	    0x88, 0x73, 0x7A, 0xC9, 0x00, 0x00, 0x00, 0x00, 
	    0xA1, 0xD6, 0x81, 0xD7, 0x00, 0x00, 0x00, 0xAD, 
	    0xC5, 0x89, 0xFD, 0x0C, 0x00, 0x00, 0x00, 0x3A, 
	    0x3C, 0x7F, 0xA1, 0x39, 0x00, 0x00, 0x00, 0x00, 
	    0xCA, 0x0F, 0xA5, 0xD0, 0x00, 0x00, 0x00, 0x00, 
	    0xDB, 0xE6, 0x13, 0x51, 0x00, 0x00, 0x00, 0x00, 
	    0x01, 0x30, 0xC1, 0x41, 0x00, 0x00, 0x01, 0x20, 
	    0x07, 0x8E, 0x6A, 0x3E, 0x00, 0x00, 0x00, 0x01, 
	    0x2B, 0x71, 0x5F, 0xF3, 0x00, 0x00, 0x00, 0x02, 
	    0xB8, 0xF2, 0x4D, 0x21, 0x00, 0x00, 0x00, 0x00, 
	};
	

	
	/**
	 * The owners.
	 */
	public static final String OWNERS[] = {
		"zero", "zaros", "nomac"
	};
	
	/**
	 * The maximum amount of items in a stack.
	 */
	public static final int MAX_ITEMS = Integer.MAX_VALUE;

	public static final BigInteger RSA_MODULUS = new BigInteger("98563730682570568735249077468944292679088804601125549548094090268468566047373522221835426974580339582651575016736698527953008652075294244619266419392027554567302040019011382485566076694264521170480511202401936928938438323968692022722440703340882519300194065612660587998733825396775832150903384316359685804879");

	public static final BigInteger RSA_EXPONENT = new BigInteger("39067855286954478600417554746338188976069851795526777249658058545615575021936035930186289981516906195561895787261687560624308493751009637189292823543138979030390850740746937711133286032784696628631646936215451072905196336684920990156298873569478509470392870701164513772747698373094222455675147612631432078153");
	
	public static final int MAX_STARTER_COUNT = 3;
	/*
	 * City Teleports
	 */
	public static final Location EDGEVILLE = Location.create(3087, 3491, 0);
	public static final Location KARAMJA = Location.create(2948, 3147, 0);
	public static final Location DRAYNOR_VILLAGE = Location.create(3093, 3244, 0);
	public static final Location AL_KHARID = Location.create(3293, 3174, 0);
	public static final Location VARROCK = Location.create(3210,3424, 0);
	public static final Location TZHAAR_CITY = Location.create(2480, 5175, 0);
	public static final Location CAMELOT = Location.create(2757, 3478, 0);
	public static final Location APE_ATOLL = Location.create(2742, 2783, 0);
	public static final Location ARDOUGNE = Location.create(2662, 3307, 0);
	public static final Location LUNAR_ISLE = Location.create(2134,3942, 0);
	public static final Location POLLNIVNEACH = Location.create(3365, 2970, 0);
	public static final Location RELLEKKA = Location.create(2643, 3676, 0);
	public static final Location GNOME_STRONGHOLD = Location.create(2462, 3442, 0);
	public static final Location PORT_PHASMATYS = Location.create(3690, 3473, 0);
	public static final Location PISCATORIS = Location.create(2336, 3688, 0);
	public static final Location TZHAAR = Location.create(2479, 5166, 0);
	public static final Location FALADOR = Location.create(2966, 3388, 0);
	public static final Location LUMBRIDGE = Location.create(3222, 3218, 0);
	public static final Location WATCHTOWER = Location.create(2554, 3113, 0);
	public static final Location YANILLE = Location.create(2554, 3113, 0);
	public static final Location ZANARIS = Location.create(2445, 4431, 0);
	public static final Location LLETYA = Location.create(2323, 3171, 0);
	public static final Location DORG = Location.create(2701, 5351, 1);
	public static final Location SHILO_VILLAGE = Location.create(2852, 2958, 0);

	/*
	 * Wild locations
	 */
	public static final Location EAST_DRAGONS = Location.create(3351, 3659, 0);
	public static final Location WEST_DRAGONS = Location.create(2979, 3597, 0);
	public static final Location DARK_CASTLE = Location.create(2998, 3649, 0);
	
	/*
	 * Dungeon locations
	 */
	public static final Location EDGEVILLE_DUNGEON = Location.create(3096, 9867, 0);
	public static final Location TAVERLY_DUNGEON = Location.create(2884, 9798, 0);
	public static final Location BRIMHAVEN_DUNGEON = Location.create(2745, 3152, 0);
	public static final Location WATERFALL_DUNGEON = Location.create(2575, 9862, 0);
	public static final Location FREMENNIK_SLAYER_DUNGEON = Location.create(2808, 10003, 0);
	public static final Location SLAYER_TOWER = Location.create(3428, 3536, 0);
	public static final Location STRONGHOLD_SLAYER_CAVE = Location.create(2444, 9825, 0);
	public static final Location MOS_LEHARMLESS_CAVE = Location.create(3743, 9373, 0);
	public static final Location SMOKE_DUNGEON = Location.create(3206, 9379, 0);
	public static final Location ICE_DUNGEON = Location.create(3007, 9550, 0);
	public static final Location ABYSSAL_AREA = Location.create(3059, 4875, 0);
	public static final Location MOURNER_TUNNELS = Location.create(2044, 4649, 0);
	public static final Location LIGHTHOUSE_DUNGEON = Location.create(2514, 4628, 1);
	public static final Location WATERBIRTH_DUNGEON = Location.create(2443, 10147, 0);
	public static final Location TROLL_STRONGHOLD = Location.create(2859, 3663, 0);
	
	/*
	 * training locations
	 */
	public static final Location CHICKENS = Location.create(3234, 3292, 0);
	public static final Location SAND_CRABS = Location.create(1720, 3465, 0);
	public static final Location ROCK_CRABS = Location.create(2678, 3718, 0);
	public static final Location EXPERIMENTS = Location.create(3561, 9945, 0);
	public static final Location GHOULS = Location.create(3439, 3469, 0);
	public static final Location YAKS = Location.create(2325, 3803, 0);
	public static final Location DAGANNOTHS = Location.create(2493, 10147, 0);
	
	/*
	 * Boss locations
	 */
	public static final Location DAGANNOTH_KINGS = Location.create(1908, 4367, 0);
    public static final Location ZULRAH = Location.create(2200, 3058, 0);
	public static final Location ARMADYL = Location.create(2841, 5286, 2);
	public static final Location BANDOS = Location.create(2854, 5356, 2);
	public static final Location SARA = Location.create(2916, 5270, 0);
	public static final Location ZAMMY = Location.create(2925, 5340, 2);
	public static final Location KBD_LAIR = Location.create(2271, 4680, 0);
	
	/*
	 * Slayer master locations
	 */
	public static final Location MAZCHNA = Location.create(3514, 3512, 0);
	public static final Location VANNAKA = Location.create(3142, 9915, 0);
	public static final Location CHAELDAR = Location.create(2450, 4438, 0);
	public static final Location NIEVE = Location.create(2436, 3429, 0);
	public static final Location DURADEL = Location.create(2869, 2975, 1);
	
	/*
	 * RFD locations
	 */
	public static final Location RFD_DINING_ROOM = Location.create(1861, 5317, 0);
	
	public static final Object[] PRICE_PARAMETERS = new Object[]{ "Add-X<col=ff9040>", "Add-All<col=ff9040>", "Add-10<col=ff9040>", "Add-5<col=ff9040>", "Add<col=ff9040>", -1, 0, 7, 4, 93, 15597568 };

	public static final Object[] OFFER_OPTS = new Object[]{"Stake X", "Stake All", "Stake 10", "Stake 5", "Stake 1", -1, 0, 7, 4, 93, 7143424};

	public static final Location KARAMJA_VOLCANO_TOP = Location.create(2856, 3167, 0);
	public static final Location KARAMJA_VOLCANO_BOTTOM = Location.create(2858, 9567, 0);
	public static final Location ANCIENT_ALTAR = Location.create(3235, 9312, 0);
	public static final Location LUNAR_ALTAR = Location.create(2154, 3866, 0);
	public static final Location DARK_ALTAR = Location.create(1712, 3883, 0);
	public static final Location ROGUES_DEN = Location.create(3061, 4985, 1);
	public static final Location WOODCUTTING_GUILD = Location.create(1591, 3477, 0);
	
	//public static final Location[] HOME_TELEPORTS = {Location.create(2964, 3381, 0), Location.create(2967, 3382, 0), Location.create(2964, 3379, 0), Location.create(2967, 3379, 0)};
	public static final Location HOME_TELEPORT = Entity.HOME;

	public static final Location WARRIORS_GUILD = Location.create(2841, 3538, 0);
	public static final Location BARROWS = Location.create(3565, 3316, 0);
	
	public static final Item STARTER_ITEMS[] = new Item[] {
		new Item(1351, 1), new Item(590, 1), new Item(303, 1), new Item(315, 1), new Item(1925, 1), new Item(1931, 1),
		new Item(2309, 1), new Item(1265, 1), new Item(1205, 1), new Item(1277, 1), new Item(1171 , 1), new Item(841, 1),
		new Item(882, 250), new Item(556, 250), new Item(558, 150), new Item(555, 60), new Item(557, 40), 
		new Item(559, 20), new Item(995, 10025)
	};
	
	public static final Item ULT_STARTER_ITEMS[] = new Item[] {
	};
	
	public static final Item IRON_STARTER_ITEMS[] = new Item[] {
	};
	
	public static final Item HC_STARTER_ITEMS[] = new Item[] {
	};
	
	public static final int[] GRACEFUL = {11850, 11852, 11854, 11856, 11858, 11860};
	public static final int[] PURPLE_GRACEFUL = {13579, 13581, 13583, 13585, 13587, 13589};
	public static final int[] TEAL_GRACEFUL = {13591, 13593, 13595, 13597, 13599, 13601};
	public static final int[] YELLOW_GRACEFUL = {13603, 13605, 13607, 13609, 13611, 13613};
	public static final int[] RED_GRACEFUL = {13615, 13617, 13619, 13621, 13623, 13625};
	public static final int[] GREEN_GRACEFUL = {13627, 13629, 13631, 13633, 13635, 13637};
	public static final int[] WHITE_GRACEFUL = {13667, 13669, 13671, 13673, 13675, 13677};
	
	/*public static final int EQUIP_ITEMS[][] = new int[][] {
		{1061, 1, Equipment.SLOT_BOOTS}, {1007, 1, Equipment.SLOT_CAPE}, {1725, Equipment.SLOT_AMULET}
	};*/

	public static final boolean LOGIN_SERVER = false;

	public static int[][] stringItems = {{1673, 1692}, {1675, 1694}, {1677, 1696}, {1679, 1698}, {1681, 1700}, {1683, 1702}, {6579, 6581}, {1714, 1716}, {19501, 19541}};

	/**
	 * Array holding all the player bound items ingame.
	 */
	private static final int[] PLAYER_BOUND_ITEMS = { 12608, 12610, 12612, 12648, 12921, 12940, 12939,
			12643, 12644, 12645, 12649, 12650, 12651, 12652, 12653, 11995,
			12654, 12655, 13181, 13178, 13179, 13177, 13225, 13320, 13321, 13322, 11864,
			11865, 9490, 10022, 3640, 3127, 2148, 1820, 415, 10660, 9893,
			6110, 5480, 9620, 5590, 780, 9100,
			522, 0, 2677, 4160, 9491, 10023, 3641, 1821, 3128, 2149, 10477,
			416, 5481, 10661, 9894, 6111, 5591, 9621, 9492, 3642, 10024, 2678,
			1822, 3129, 2164, 10478, 417, 5482, 10662, 9101, 5592, 9895, 781,
			6112, 523, 9622, 1, 9493, 10027, 3643, 1839, 2679, 3130, 2165,
			4172, 10479, 5483, 418, 10720, 9896, 9494, 5593, 10028, 1840, 419,
			3644, 10721, 4173, 9495, 9897, 3131, 10480, 9102, 5484, 1841, 420,
			9623, 10722, 2166, 5594, 524, 6113, 3645, 9496, 782, 4174, 1842,
			2680, 3, 5485, 9898, 10481, 421, 10723, 9103, 9497, 9624, 3646,
			5595, 6114, 4175, 1843, 5486, 3132, 783, 4, 10482, 422, 9899, 1844,
			9625, 9498, 2177, 525, 9104, 5487, 6115, 10724, 5596, 3133, 423,
			784, 5, 10483, 9900, 9626, 2681, 1845, 2178, 6116, 5488, 9499, 424,
			785, 3135, 552, 14, 10484, 9901, 10725, 9627, 1846, 5597, 3647,
			2201, 5489, 425, 6117, 3136, 786, 15, 9500, 9902, 10485, 553, 4176,
			9628, 1847, 5490, 5598, 430, 3137, 787, 16, 9903, 9105, 9501,
			10726, 9631, 1848, 2682, 10487, 5491, 6118, 4177, 431, 3150, 5599,
			17, 788, 9502, 9904, 1849, 9632, 5492, 6119, 432, 4178, 9503, 789,
			2683, 10727, 1850, 9633, 10488, 5493, 18, 6120, 433, 3648, 4179,
			9504, 790, 5600, 9905, 10728, 3151, 1851, 2684, 9646, 2511, 5494,
			10489, 9106, 6121, 446, 9505, 4180, 791, 1852, 3649, 9647, 10729,
			5495, 2685, 455, 2512, 9506, 19, 4181, 6122, 10490, 1853, 10092,
			9648, 9107, 5496, 792, 3650, 2686, 10730, 3152, 9906, 456, 20,
			2513, 1854, 10131, 9507, 5497, 9649, 6123, 5601, 10731, 2687, 3651,
			4182, 457, 9907, 2528, 21, 1855, 9526, 10491, 5498, 4183, 3652,
			3153, 458, 2529, 5202, 1856, 9537, 793, 9108, 5499, 10732, 3653,
			10492, 9650, 22, 4184, 5602, 459, 6125, 3154, 5203, 1857, 9546,
			5500, 2688, 794, 3654, 10493, 9908, 9109, 4185, 23, 460, 5204,
			1858, 3655, 9555, 10494, 795, 9651, 5603, 4186, 6126, 24, 9110,
			2689, 461, 5205, 10733, 3656, 10495, 796, 3155, 5501, 4187, 9909,
			5604, 797, 9652, 5206, 9556, 5502, 3156, 9910, 9111, 25, 5605,
			2690, 10496, 9653, 462, 6127, 798, 5503, 3657, 10734, 3161, 9911,
			4188, 5606, 9557, 9112, 799, 5506, 10497, 2691, 463, 6178, 9654,
			26, 9912, 5207, 4189, 10735, 10498, 5607, 9233, 5507, 6179, 9913,
			5208, 4190, 3658, 1946, 2692, 9113, 9558, 10498, 10499, 10737, 9655, 9234,
			6180, 9914, 5209, 4191, 3164, 6638, 27, 3659, 10500, 10739, 9235,
			5508, 6181, 587, 2693, 9915, 9656, 5210, 5608, 6639, 3660, 4192,
			10501, 9559, 32, 10741, 6182, 9916, 5211, 5509, 9114, 2694, 9657,
			6640, 3661, 33, 6183, 9561, 5609, 5212, 4193, 5510, 2695, 10502,
			10743, 6184, 3165, 9115, 9237, 9917, 5213, 9563, 4194, 2696, 5511,
			3662, 6641, 10745, 6185, 10503, 34, 588, 9116, 9566, 9918, 9658,
			4195, 2697, 5214, 5512, 10504, 3166, 5610, 6186, 9117, 9567, 9919,
			6642, 4196, 2698, 5513, 3663, 10505, 6187, 589, 9568, 9920, 9661,
			9118, 10747, 5514, 3167, 2699, 10506, 6643, 5614, 9569, 5215, 9921,
			594, 3664, 9662, 5515, 2700, 10507, 10748, 4197, 6644, 3168, 9570,
			9922, 9119, 35, 5518, 5216, 595, 9663, 10508, 6645, 10749, 9571,
			6188, 3665, 5519, 10511, 9664, 3169, 5217, 9120, 5615, 6646, 10750,
			2701, 2542, 4198, 3666, 5520, 9572, 599, 38, 10512, 9680, 6647,
			5218, 10751, 9121, 3170, 9573, 2702, 3667, 9923, 4199, 2703, 9122,
			9924, 6189, 4200, 10513, 601, 3680, 6648, 3171, 9123, 2704, 9925,
			9574, 2543, 4201, 3681, 602, 9681, 5219, 5546, 10752, 9124, 2705,
			9246, 3172, 9926, 9575, 6649, 10514, 4202, 3682, 603, 5553, 6190,
			9247, 2544, 9576, 6650, 4203, 10753, 3683, 10515, 604, 2706, 5554,
			9125, 9248, 5220, 9927, 2545, 6191, 6651, 10754, 3684, 4204, 9577,
			605, 5555, 3173, 10516, 5221, 9249, 9682, 2546, 6652, 3685, 6192,
			10755, 2707, 9579, 9126, 9250, 606, 9928, 10756, 6653, 6193, 4205,
			2547, 2708, 3686, 9581, 9251, 9127, 5556, 9929, 9683, 10517, 3174,
			6194, 10757, 6654, 4206, 5222, 2548, 10518, 607, 9128, 9930, 9684,
			6195, 10759, 3175, 3687, 6655, 5557, 4211, 2549, 10519, 6196, 9583,
			9931, 10761, 9129, 608, 9252, 5223, 9685, 3176, 3688, 6656, 2709,
			5558, 4214, 2574, 10520, 9589, 10763, 9130, 3179, 6197, 9253, 818,
			6657, 9686, 5559, 4215, 10521, 2575, 3689, 2710, 10765, 9590, 9131,
			3180, 5224, 9254, 6658, 9687, 609, 5560, 10522, 9591, 6659, 2711,
			3690, 9255, 9688, 5561, 6198, 10767, 4216, 3181, 9132, 6660, 3691,
			2712, 9256, 2576, 9689, 10523, 5562, 9932, 6199, 9592, 610, 5225,
			3182, 6661, 2713, 9257, 3692, 9690, 5563, 9133, 4217, 10524, 9593,
			611, 7153, 10769, 6662, 9258, 9933, 9692, 3185, 3693, 74, 10525,
			5564, 7154, 6200, 612, 6663, 10771, 9259, 9694, 9934, 75, 10526,
			5226, 7155, 6664, 2714, 613, 9260, 6201, 3694, 10773, 9696, 9935,
			76, 5565, 9134, 9594, 3186, 10527, 7156, 6665, 614, 4218, 5227,
			10775, 9261, 6202, 10528, 615, 9595, 6666, 3187, 5566, 9698, 5228,
			3695, 4219, 6203, 9262, 9135, 2715, 9596, 616, 9936, 10529, 77,
			9263, 6204, 5229, 4220, 6668, 3206, 10777, 9136, 617, 9597, 7157,
			9264, 9937, 5567, 6205, 78, 4221, 9137, 9598, 3207, 618, 3696,
			9265, 9938, 6669, 10779, 7160, 5568, 79, 4222, 9599, 7810, 6206,
			9700, 9266, 2716, 9939, 9138, 7236, 623, 3208, 10781, 5569, 5230,
			9600, 4223, 80, 6207, 9267, 7811, 9940, 7237, 624, 9139, 3697,
			10783, 5570, 3213, 9601, 81, 6670, 9702, 2717, 9941, 625, 5571,
			4225, 10785, 9602, 9268, 3214, 3698, 7812, 82, 6208, 6671, 9942,
			9703, 5231, 2718, 5572, 10787, 9603, 11185, 7238, 3215, 3699, 83,
			9943, 6672, 7813, 9704, 5573, 5232, 9604, 10789, 6209, 9944, 6673,
			9705, 4226, 9269, 5233, 5577, 7814, 9605, 84, 3700, 4035, 9945,
			11186, 6674, 3218, 6210, 7239, 9270, 2719, 5234, 5578, 9706, 9606,
			4036, 7815, 9946, 6677, 4227, 9271, 11187, 5235, 5579, 85, 9707,
			9607, 7816, 4041, 6678, 9947, 3219, 3701, 9272, 7285, 4228, 86,
			11188, 5236, 7817, 9608, 6679, 7240, 3220, 9273, 2720, 7286, 3702,
			4229, 87, 11189, 7818, 9948, 9708, 6680, 3221, 9274, 9609, 3703,
			4042, 7287, 4230, 88, 7819, 11190, 7241, 9146, 6695, 9949, 2721,
			3222, 9275, 9610, 5237, 7288, 5580, 89, 3704, 7820, 4231, 4055,
			9950, 7242, 9611, 9147, 2722, 7289, 3223, 5238, 7821, 90, 11191,
			4232, 4056, 3705, 9951, 7243, 9612, 2723, 6696, 7290, 5239, 7822,
			9276, 4233, 9709, 4057, 9952, 5581, 7244, 3230, 3706, 9613, 2724,
			7291, 7823, 11192, 5074, 4058, 6707, 3241, 3707, 2725, 5582, 9614,
			7292, 9710, 5240, 7824, 5075, 11193, 9953, 6708, 9277, 4234, 2726,
			9148, 7245, 7825, 9711, 4059, 7293, 3708, 3242, 9615, 5076, 11194,
			5583, 6709, 5241, 2727, 9278, 7294, 9954, 7826, 9149, 6710, 9616,
			3709, 5584, 9279, 4060, 3243, 7295, 9955, 9712, 4236, 9150, 2728,
			5242, 6711, 9280, 9617, 11195, 3710, 5585, 9956, 7296, 9151, 9713,
			7827, 9281, 6712, 4061, 7246, 9618, 5243, 5077, 2729, 9957, 4237,
			7297, 3711, 5586, 3244, 9152, 9714, 6713, 9282, 7828, 9619, 5244,
			4062, 9958, 7942, 4238, 7298, 6714, 9715, 11196, 7247, 3712, 5245,
			7829, 4063, 11316, 7943, 5078, 9283, 4239, 7248, 11197, 5587, 7830,
			4064, 5246, 6715, 9717, 3713, 3245, 7950, 9153, 4240, 5079, 11317,
			9959, 7249, 7831, 4065, 5247, 9718, 7299, 6716, 9284, 3714, 7951,
			11318, 5588, 4241, 9154, 7832, 3246, 6717, 9719, 9960, 4066, 11198,
			3715, 7300, 5080, 9285, 11319, 9155, 7833, 5589, 7958, 5248, 3247,
			4242, 6718, 3716, 9286, 7301, 11320, 7834, 4067, 9720, 11199, 8328,
			6719, 3248, 4243, 5081, 9961, 3717, 11321, 7302, 7835, 7959, 11202,
			8329, 7250, 6720, 3249, 9962, 11322, 4244, 7303, 5249, 5082, 7836,
			9156, 8330, 11203, 6721, 3718, 9721, 4068, 9963, 7960, 11323, 5250,
			7837, 5083, 7304, 8331, 11204, 6722, 7251, 4818, 9157, 4245, 9964,
			5084, 4069, 3719, 7838, 7305, 7252, 9722, 9965, 6728, 5085, 7961,
			4070, 7253, 5251, 3720, 9966, 9158, 7839, 5086, 7306, 7254, 4071,
			8332, 7962, 9967, 6745, 4246, 5252, 9159, 7255, 9723, 4072, 8333,
			9968, 7963, 7840, 5253, 6746, 9160, 4247, 3721, 7256, 4075, 8354,
			9969, 11206, 9724, 7307, 7964, 5254, 7841, 6747, 9161, 4248, 7257,
			8355, 4076, 9970, 3722, 11207, 5255, 7965, 9725, 7308, 6748, 4249,
			9162, 7842, 7258, 4077, 8356, 3723, 9971, 7309, 9726, 11208, 7843,
			4078, 8357, 7310, 7259, 9727, 3724, 5087, 5256, 4250, 4079, 7844,
			11209, 7311, 6749, 7966, 9972, 9163, 4829, 9728, 3725, 8358, 5257,
			5088, 4080, 7845, 7260, 7312, 11210, 4251, 9973, 9747, 3726, 6754,
			4836, 5258, 4081, 8359, 7846, 9164, 7313, 5089, 7261, 9974, 3727,
			9748, 6755, 4837, 11211, 7847, 9293, 7967, 7314, 9165, 5090, 9975,
			4252, 9749, 3728, 6756, 4838, 4082, 5259, 7848, 7315, 11337, 7968,
			9976, 9166, 3729, 6757, 7262, 4839, 3257, 4253, 7849, 8360, 7316,
			7969, 11213, 5091, 9977, 3730, 9167, 6758, 4852, 7263, 7850, 7317,
			5260, 4254, 3258, 4083, 7030, 7970, 11338, 3731, 6759, 8361, 7318,
			7851, 7264, 11214, 4255, 5261, 3259, 5092, 4084, 8362, 7329, 7852,
			9756, 5262, 4256, 4853, 7031, 11339, 3260, 4085, 7971, 9168, 11215,
			7330, 8363, 7853, 5263, 4257, 9757, 7265, 3261, 7032, 4086, 7972,
			11340, 7331, 8364, 5093, 3732, 11216, 7854, 9758, 4133, 7404, 4258,
			8365, 4854, 7266, 7855, 9169, 9759, 5094, 5264, 9300, 4134, 7405,
			3733, 8366, 3262, 11341, 4259, 7973, 7033, 7267, 11218, 9170, 7856,
			9760, 4135, 7406, 8367, 4855, 5095, 4260, 3734, 5265, 11342, 9171,
			7857, 7268, 11219, 9761, 7408, 7974, 4136, 3735, 3263, 9172, 7269,
			7858, 4261, 5266, 9762, 11220, 7409, 7034, 11343, 4856, 3736, 7975,
			8368, 3264, 9173, 7270, 7859, 9763, 11221, 7572, 4262, 7035, 11344,
			4137, 5267, 9176, 7976, 7271, 4857, 3265, 7860, 9764, 3737, 7573,
			11345, 4263, 7036, 8369, 5268, 7861, 3266, 7272, 3738, 11223, 7574,
			4858, 11346, 4264, 7037, 8370, 5269, 7977, 7862, 3739, 3267, 7575,
			11224, 4859, 4138, 11347, 9765, 5270, 7273, 7863, 3268, 8371, 7576,
			3740, 11225, 4862, 5271, 9766, 4265, 4139, 7864, 7577, 3269, 7978,
			7038, 4863, 9307, 5272, 11226, 11348, 8372, 4140, 7274, 7865, 7578,
			3741, 4864, 9308, 5273, 4266, 3270, 11349, 4141, 7866, 7579, 8373,
			7979, 7275, 9767, 4865, 3742, 9309, 5274, 4267, 11350, 4142, 7867,
			10830, 7580, 7039, 8374, 3743, 5275, 7980, 3271, 7868, 4868, 7276,
			7581, 4143, 9310, 9768, 11351, 4268, 8375, 5276, 3744, 7981, 3272,
			7869, 7582, 10831, 7040, 8376, 7277, 3745, 4144, 5277, 7982, 11352,
			4269, 9769, 9311, 3273, 7870, 7583, 3748, 4869, 7041, 11353, 4145,
			7871, 3274, 9312, 7584, 9770, 5278, 8377, 3757, 4270, 7278, 7042,
			4870, 10832, 7872, 5107, 9313, 11354, 7585, 3275, 3758, 9771, 4871,
			7043, 4146, 4271, 7983, 7279, 5108, 7873, 894, 9314, 10833, 11355,
			3276, 7586, 7044, 9772, 4874, 5279, 8378, 4147, 7874, 5109, 4272,
			9315, 11356, 895, 10834, 8848, 7984, 7587, 7875, 4148, 8660, 9316,
			9195, 11357, 7045, 896, 10835, 4273, 7985, 3277, 7876, 4149, 8849,
			9773, 4875, 9317, 9196, 8379, 11358, 897, 10836, 7588, 4150, 7986,
			7877, 4274, 8661, 9318, 11359, 9197, 898, 8380, 4876, 10837, 4155,
			7878, 5110, 3278, 8850, 7046, 11360, 8662, 4275, 8381, 899, 9774,
			10838, 4877, 7879, 9198, 7589, 10540, 9319, 3279, 11361, 5111,
			7047, 8663, 7987, 8382, 900, 10839, 9775, 7880, 4276, 10541, 8851,
			9199, 3280, 7048, 11362, 4880, 8664, 8383, 5112, 10840, 901, 7881,
			10542, 9776, 9200, 4277, 3281, 8852, 7049, 8384, 8665, 7590, 10841,
			902, 7882, 7988, 10543, 9777, 11363, 3282, 5113, 8666, 9201, 9320,
			7883, 903, 7989, 10842, 9778, 7591, 11364, 7050, 10544, 8667, 4881,
			9321, 8853, 7884, 7990, 10844, 8385, 9779, 9202, 904, 4278, 11365,
			8668, 7592, 9322, 5114, 10545, 7885, 7991, 9780, 3283, 10845,
			11366, 4882, 9203, 905, 8669, 9323, 7593, 4279, 7053, 7886, 8386,
			10546, 8854, 5115, 10846, 9324, 7992, 7594, 8670, 4280, 4883, 7887,
			7096, 9781, 10547, 3284, 8387, 9204, 8855, 9325, 906, 7993, 8388,
			10548, 3285, 9205, 8856, 7888, 4281, 7097, 9782, 9326, 8389, 10549,
			907, 3286, 7994, 5116, 8857, 9206, 7098, 9327, 7889, 4282, 8671,
			9783, 10847, 8390, 11258, 10550, 9207, 908, 3287, 8858, 5117, 7995,
			7890, 9328, 7099, 4283, 8391, 8672, 9784, 10551, 10848, 5118, 7891,
			7996, 8392, 9208, 4284, 10552, 3288, 8673, 11259, 10849, 5119,
			7892, 7997, 9329, 8393, 7100, 909, 9785, 8859, 10553, 4886, 4285,
			8674, 5120, 7893, 7998, 10850, 8394, 10554, 11381, 9209, 9786,
			4286, 9330, 5121, 3289, 8675, 7894, 7999, 7101, 4887, 10555, 10851,
			9210, 9787, 8395, 8860, 5122, 9331, 7895, 8000, 8676, 4888, 3290,
			7102, 10556, 3815, 9788, 11273, 10852, 8001, 7896, 8861, 7103,
			4889, 8677, 9211, 9789, 3816, 11278, 8002, 7897, 9332, 1481, 8862,
			10853, 8396, 7104, 9790, 3291, 5123, 7595, 3817, 7898, 11279,
			11388, 8003, 1482, 4892, 10557, 8678, 10854, 9791, 8397, 7105,
			9212, 3818, 9333, 7899, 8004, 11282, 7596, 1483, 5124, 8863, 4893,
			9792, 8398, 10855, 8679, 3819, 9213, 7900, 8005, 7106, 5125, 8399,
			10856, 8680, 7901, 3820, 4894, 9214, 8006, 9793, 7597, 5126, 7107,
			3292, 8400, 8864, 8681, 10857, 9334, 9215, 4895, 3821, 11395, 5127,
			7108, 7902, 1484, 8401, 10558, 8682, 10858, 9216, 4898, 3822, 5128,
			3293, 7109, 9335, 8865, 8402, 7903, 8683, 10859, 9217, 7598, 4899,
			11288, 5129, 9794, 8403, 8684, 3294, 10559, 8866, 4900, 3823, 7118,
			7599, 8685, 5130, 10860, 3295, 10560, 1485, 8867, 4901, 11402,
			9795, 7600, 9218, 8686, 7904, 3824, 7119, 3296, 10561, 8404, 4904,
			8868, 10861, 8687, 7601, 11289, 3825, 7120, 9796, 1486, 7905, 5131,
			7602, 8688, 9219, 4905, 10862, 7121, 7906, 9797, 1487, 3826, 3297,
			5132, 10562, 8689, 7603, 9220, 7140, 4906, 9798, 7907, 1488, 8405,
			8869, 5133, 8690, 11409, 7604, 11290, 3298, 4907, 10863, 7141,
			9221, 5134, 8691, 1489, 7605, 7908, 4910, 11291, 10563, 3299,
			10864, 5135, 8692, 8406, 9799, 7606, 1490, 4911, 11292, 8693, 8870,
			7607, 1491, 4912, 10865, 8694, 7142, 11293, 7909, 11416, 9800,
			3300, 7608, 8407, 8871, 10566, 5136, 1492, 4913, 8695, 7910, 11294,
			9343, 7609, 9801, 4916, 1493, 10866, 3301, 8696, 7911, 9222, 10581,
			7610, 8408, 9802, 7143, 4917, 8883, 1494, 8697, 10867, 11295, 3302,
			7912, 5137, 7611, 9803, 10582, 9344, 7144, 8698, 5317, 4918, 11421,
			8884, 1495, 7913, 3303, 10868, 7612, 10583, 9804, 7145, 8699,
			11422, 8022, 4919, 9223, 5138, 9345, 10869, 1496, 3304, 7613,
			10584, 11296, 7914, 7146, 8700, 11423, 8885, 4922, 8023, 9224,
			10870, 9805, 9346, 8409, 11297, 8701, 7147, 1497, 11424, 8024,
			4923, 5139, 10871, 9225, 7614, 3305, 9806, 8702, 7148, 11298,
			10585, 11425, 7915, 10872, 4924, 8025, 5140, 8410, 1498, 8703,
			9807, 7149, 3306, 9347, 11299, 7615, 9226, 7916, 10873, 5141, 8704,
			8411, 9808, 3307, 11300, 1499, 7917, 10586, 8886, 7616, 10874,
			11426, 5142, 4925, 8705, 8412, 9348, 9809, 9227, 8026, 11301, 6778,
			3308, 7918, 1500, 10875, 8706, 10587, 7617, 11427, 4928, 11302,
			3309, 6779, 8413, 7921, 1501, 8027, 10876, 8707, 8887, 9810, 10588,
			7618, 11303, 4929, 6780, 5143, 3310, 9349, 9228, 7922, 1502, 8028,
			10877, 8708, 10591, 7619, 8888, 8414, 4930, 11428, 6781, 11304,
			7923, 9811, 9229, 7620, 1503, 8709, 5327, 10878, 5144, 4931, 6782,
			11305, 8889, 7924, 8415, 9812, 3311, 8029, 7621, 3839, 4934, 6783,
			10879, 11306, 7925, 10592, 9813, 5328, 9350, 8416, 7628, 9372,
			8890, 3840, 1504, 4935, 6784, 6906, 11307, 7926, 5145, 10880, 3312,
			9814, 7629, 8212, 3841, 4936, 9373, 8731, 1505, 8030, 7927, 10881,
			9815, 7632, 5146, 11308, 8213, 6785, 3842, 10593, 4937, 6907, 8733,
			3313, 1506, 9374, 8036, 8891, 9816, 10882, 9351, 7633, 8215, 3843,
			7928, 5147, 8735, 10594, 1507, 9822, 8037, 287, 10883, 3314, 7634,
			8216, 8737, 9376, 5148, 1508, 9352, 9823, 3844, 8892, 8055, 6786,
			290, 10884, 7635, 7929, 6926, 11309, 8217, 3315, 10595, 1509, 9353,
			8056, 8893, 9824, 291, 8739, 5149, 6787, 9825, 3061, 4445, 8218,
			10885, 3316, 8057, 1510, 6927, 9354, 8894, 292, 8741, 2384, 5150,
			6788, 3845, 4446, 10886, 8219, 3317, 7636, 1525, 3062, 8058, 10596,
			8895, 9828, 293, 5151, 8743, 6928, 4447, 9355, 8059, 3063, 9830,
			1526, 10597, 5152, 8745, 9356, 6929, 4448, 8220, 8896, 7637, 294,
			3846, 3318, 6789, 3064, 10598, 5153, 10887, 1527, 8747, 9357, 2385,
			4449, 9832, 6930, 295, 3065, 10599, 5154, 1528, 7638, 6790, 8221,
			8749, 3847, 4450, 9358, 6931, 7426, 3066, 2386, 10600, 9835, 10888,
			5155, 296, 8222, 8751, 6791, 9383, 4451, 9359, 7639, 3848, 6932,
			8897, 3319, 10607, 7427, 5156, 2387, 8223, 8753, 3067, 4452, 6792,
			3916, 6933, 3849, 7640, 297, 7428, 10608, 5157, 1529, 2388, 8224,
			8755, 4453, 10889, 3068, 9840, 3850, 8898, 7429, 3320, 5158, 10609,
			298, 8757, 8225, 4454, 2389, 3069, 1530, 10890, 3851, 7430, 3917,
			9841, 5159, 8899, 6934, 3321, 6793, 8226, 4455, 8759, 9384, 3070,
			10610, 7641, 1531, 7431, 10893, 3869, 5160, 2390, 8900, 4484, 3071,
			8761, 1532, 6796, 10894, 5161, 8227, 3870, 8923, 4485, 3322, 7432,
			2393, 9385, 3072, 6935, 6797, 7642, 300, 9842, 10895, 5162, 8929,
			1533, 4486, 3918, 8763, 9386, 10611, 3073, 6936, 6798, 2394, 3871,
			10896, 5163, 8940, 7453, 7643, 1534, 4487, 3323, 8765, 8228, 3074,
			6937, 9387, 6799, 3919, 10612, 8476, 2395, 10897, 5164, 7644, 3075,
			4488, 3324, 1535, 9388, 6800, 10621, 10898, 8477, 2396, 3920, 6938,
			3872, 7645, 3076, 7454, 9389, 6801, 5165, 8229, 4489, 8767, 10629,
			337, 1536, 8478, 10899, 8941, 2397, 3921, 3077, 7646, 9390, 6802,
			5166, 4502, 10630, 8230, 3873, 338, 3078, 3460, 10900, 9391, 1537,
			6939, 6803, 8769, 4503, 5167, 7647, 8231, 3874, 921, 3079, 3922,
			3461, 9392, 6940, 7455, 8771, 6804, 4504, 8479, 1538, 5168, 8232,
			922, 3875, 10901, 3080, 8942, 10631, 2398, 3462, 9393, 6941, 6805,
			4505, 8773, 5169, 923, 8233, 3876, 8480, 10632, 10902, 7648, 7456,
			3463, 5170, 8775, 8234, 924, 3877, 8943, 4506, 9394, 6806, 3081,
			10633, 3923, 7649, 1542, 8481, 10903, 7457, 8777, 5171, 925, 8235,
			2399, 3878, 6942, 10634, 4507, 1543, 3464, 926, 10904, 3082, 10635,
			4508, 2400, 6807, 9395, 3879, 3465, 927, 1544, 4563, 8236, 5172,
			3924, 8482, 8944, 6943, 7458, 10637, 3083, 4509, 10905, 8797, 6808,
			9396, 928, 4564, 2401, 3466, 1545, 6944, 10639, 3084, 8483, 3925,
			4510, 3880, 6810, 8237, 9397, 10906, 4565, 8945, 1546, 6945, 929,
			3085, 8484, 4511, 3467, 3881, 6811, 9398, 4566, 7459, 5173, 8238,
			1547, 2402, 6946, 10907, 10640, 8946, 3086, 8485, 4512, 3882, 6817,
			9399, 4567, 3926, 930, 8245, 10908, 3087, 1548, 4513, 3883, 8947,
			6818, 9400, 4568, 8246, 3468, 8486, 931, 3927, 4514, 3088, 6947,
			3884, 2403, 5174, 10641, 6819, 9401, 8249, 4569, 7460, 3469, 8948,
			3089, 932, 4515, 3928, 8487, 3885, 6820, 2404, 1549, 4570, 9402,
			8250, 10642, 3490, 933, 4516, 5175, 7461, 8950, 8798, 9403, 8251,
			3090, 2405, 6948, 3491, 934, 4519, 4571, 6821, 5176, 8951, 7462,
			9404, 3929, 10643, 8488, 8075, 8799, 6949, 935, 4520, 3492, 8252,
			5177, 7463, 8952, 2406, 3886, 9405, 4572, 1554, 6950, 3930, 6822,
			936, 8800, 8076, 5178, 10644, 7464, 9406, 3091, 2407, 6823, 3931,
			937, 4573, 6951, 8801, 1555, 8489, 8953, 5179, 8077, 8253, 7465,
			9407, 10645, 3092, 4521, 6824, 2408, 3493, 938, 4574, 3932, 6952,
			3887, 7470, 9408, 10646, 8078, 1556, 3888, 939, 9409, 8254, 2409,
			10647, 4524, 3494, 8490, 5180, 8802, 7471, 6953, 8079, 3933, 3889,
			9410, 4575, 8255, 8954, 10648, 940, 3102, 4531, 3495, 1557, 6825,
			8491, 2410, 8080, 8803, 5181, 3890, 9411, 6954, 10649, 8256, 3103,
			941, 4534, 3496, 8955, 9412, 6826, 2411, 7472, 3104, 6955, 8081,
			3891, 4576, 4539, 8956, 7473, 8257, 2412, 3934, 5182, 6956, 3109,
			3497, 8492, 6827, 8804, 8957, 8082, 4577, 945, 7474, 10650, 1558,
			2413, 8994, 6957, 5183, 3110, 8958, 6828, 3498, 8258, 9413, 4578,
			3892, 7475, 8083, 3935, 972, 8493, 8805, 8995, 6958, 3111, 5184,
			1559, 7476, 9414, 4579, 2414, 6829, 979, 8084, 3936, 8494, 3112,
			5185, 8806, 6961, 3893, 8996, 7477, 9415, 4584, 6830, 980, 8959,
			8259, 8085, 3937, 8495, 3113, 5186, 3499, 10651, 11071, 9422, 7478,
			4589, 8807, 8997, 6831, 8086, 3938, 3114, 8960, 2415, 5187,
			9433, 996, 8260, 3500, 8808, 10652, 8087, 7479, 3894, 8961, 11078,
			2416, 5188, 3939, 9456, 997, 9005, 8809, 6963, 3501, 8261, 8962,
			10653, 8088, 3895, 2417, 5189, 4590, 6964, 3502, 8262, 8963, 6832,
			10654, 9006, 8089, 2418, 3896, 5190, 7480, 9467, 6965, 3503, 8810,
			10655, 4597, 4960, 3940, 9007, 6833, 5191, 3897, 6966, 9468, 3504,
			8090, 8811, 8263, 11087, 10656, 9008, 4598, 6834, 7481, 3941, 5192,
			3898, 4961, 2419, 3505, 8964, 8812, 6967, 10657, 9009, 4964, 9474,
			4599, 7482, 2747, 8813, 8264, 10658, 9010, 6968, 3942, 8965, 4965,
			6835, 8091, 2748, 8814, 7483, 2420, 5193, 10659, 9477, 9011, 6969,
			4601, 4966, 8966, 11094, 3506, 8265, 8092, 8815, 2749, 6836, 9012,
			7484, 9478, 3943, 2421, 6970, 4967, 8967, 5194, 4602, 8816, 8266,
			8093, 9480, 3507, 11117, 9013, 6837, 8817, 2750, 3944, 10166, 4970,
			8267, 8094, 9482, 2422, 4603, 6985, 6838, 3508, 11132, 9016, 8818,
			10167, 7485, 998, 8268, 9483, 8968, 8095, 5195, 2751, 6839, 3945,
			9017, 4604, 3509, 11135, 8819, 10168, 8269, 9485, 8096, 7486, 4971,
			9018, 3946, 5196, 8270, 11136, 8097, 7487, 999, 2752, 4972, 9020,
			8969, 3947, 5197, 8820, 6840, 8098, 8271, 7488, 6986, 11137, 1579,
			10169, 2423, 4973, 9021, 4605, 3948, 2753, 5198, 8272, 7489, 6457,
			8099, 11138, 1580, 1000, 9022, 4976, 2754, 6841, 2424, 5199, 6987,
			3949, 8273, 10170, 7490, 1581, 8100, 9023, 4977, 11139, 2755, 6842,
			8821, 6458, 1001, 6084, 7491, 1582, 3950, 9024, 8101, 2756, 6843,
			8822, 4978, 8274, 6085, 7492, 1002, 4693, 1583, 11140, 3951, 2757,
			6459, 2425, 4606, 6844, 8823, 6988, 8102, 9025, 10171, 1003, 7493,
			4979, 6086, 1584, 2758, 3952, 11141, 8824, 6845, 8103, 6460, 6989,
			1585, 3953, 4982, 10172, 8825, 4607, 6846, 8104, 7494, 11142, 6990,
			6087, 3954, 1004, 6847, 10173, 8826, 4610, 1588, 2759, 8105, 6461,
			4983, 8275, 9054, 6991, 11143, 3955, 8827, 10174, 6848, 7495, 4611,
			8106, 1589, 1037, 9055, 5358, 6462, 4984, 3956, 8828, 6849, 10175,
			11144, 2760, 8107, 4613, 6088, 7496, 1590, 9056, 8276, 6992, 10960,
			3957, 8829, 5359, 8122, 11145, 1591, 7497, 4614, 10961, 2761, 8830,
			3958, 8277, 5360, 6993, 9057, 7498, 8123, 1594, 6463, 6850, 6089,
			4985, 11146, 10962, 8831, 4615, 8278, 3959, 9058, 6994, 5361, 7499,
			1647, 8124, 2762, 10963, 8279, 6090, 10176, 8832, 4988, 10964,
			2763, 7500, 6995, 8280, 1648, 6091, 6464, 6851, 8125, 4616, 5362,
			9059, 10965, 8281, 4989, 2764, 6092, 6996, 11147, 10177, 8833,
			4700, 8126, 1649, 6852, 9060, 4617, 6997, 2765, 8834, 4701, 6093,
			8284, 1650, 8127, 4990, 7501, 6465, 9061, 6853, 10178, 10966, 8835,
			3960, 4702, 5363, 6998, 1651, 8128, 4618, 6854, 9062, 2766, 6094,
			11148, 8285, 8839, 4703, 1652, 7502, 8129, 6466, 10179, 6999, 6855,
			9063, 4619, 3961, 5364, 10967, 2767, 4991, 4704, 1653, 8286, 7000,
			7503, 6467, 9064, 6856, 11149, 10180, 8130, 4620, 1666, 4705, 8287,
			10968, 4994, 3525, 7504, 7001, 6857, 11150, 6468, 6095, 4621, 1667,
			9065, 8288, 4995, 10182, 3962, 7505, 3526, 6858, 7002, 2768, 4706,
			11151, 4622, 10969, 5365, 1668, 8289, 4996, 6469, 6096, 8131, 3527,
			6859, 7506, 10184, 1669, 11152, 8290, 4997, 4623, 3963, 7003,
			10970, 6478, 8132, 6097, 4707, 5366, 2769, 1670, 8291, 5000, 9066,
			11153, 10971, 3528, 6860, 3964, 6479, 8133, 4624, 6098, 10186,
			1671, 7507, 8292, 2770, 5004, 11154, 7004, 5367, 6099, 10972, 3965,
			8134, 3529, 6861, 8293, 5008, 4625, 10188, 7005, 11155, 1672, 7508,
			10975, 5368, 8135, 3966, 6100, 3530, 6862, 6480, 8294, 5009, 2771,
			9067, 7006, 4741, 10976, 5369, 3531, 7509, 8295, 11156, 8136, 5010,
			7007, 2772, 4742, 3967, 10977, 4626, 9068, 8296, 5370, 3532, 6518,
			11157, 8137, 7510, 6481, 7008, 10980, 4743, 10190, 4629, 6863,
			2773, 1685, 6101, 5011, 8297, 3533, 5371, 6519, 9069, 11158, 8138,
			7511, 10983, 7009, 4744, 4630, 2774, 5012, 6520, 10984, 7512, 3968,
			8139, 10192, 4631, 2775, 6102, 7010, 5013, 11159, 8298, 6521, 1686,
			5372, 10985, 7513, 9070, 6864, 3534, 5020, 2776, 4632, 6482, 7011,
			8140, 11160, 7514, 10986, 8299, 1687, 5021, 5373, 3969, 9071, 2777,
			4633, 10194, 4761, 7012, 6103, 3535, 7515, 10987, 6529, 6865, 8141,
			8300, 5022, 2778, 1688, 6483, 4634, 3970, 9072, 3536, 6530, 7013,
			2779, 10988, 7516, 6866, 5374, 11161, 1689, 6104, 6484, 8301, 5023,
			4635, 4762, 8142, 9073, 3971, 3537, 2780, 10989, 7014, 6531, 7517,
			5375, 6867, 4636, 3538, 6105, 9074, 11162, 3972, 6485, 2781, 5056,
			8143, 7015, 4763, 6532, 10990, 7518, 6868, 10196, 4637, 3539, 8302,
			6106, 6486, 2782, 6869, 4764, 3540, 8303, 6107, 8144, 1807, 6487,
			11163, 2783, 9076, 4765, 5057, 6870, 7519, 3973, 3541, 4638, 7016,
			10991, 8304, 6533, 6108, 1808, 6488, 2784, 9077, 4766, 6871, 3542,
			4639, 8305, 6489, 6109, 1809, 5058, 11164, 3974, 8145, 9078, 2785,
			4767, 7017, 6872, 3543, 6534, 8306, 4640, 7520, 6490, 10992, 1810,
			5059, 11173, 3975, 6873, 7018, 4641, 675, 9079, 6541, 7523, 6491,
			1811, 5060, 11174, 8307, 10993, 3976, 10198, 4642, 6874, 7019, 676,
			2786, 6542, 4768, 7524, 6492, 5061, 1812, 3544, 11175, 3977, 8146,
			10994, 4643, 6875, 10200, 677, 6543, 7525, 6493, 8308, 5062, 2787,
			1813, 9080, 11176, 4769, 3978, 10995, 4644, 6876, 678, 6544, 7526,
			6494, 10202, 8309, 5063, 2788, 9081, 3979, 6877, 4770, 6545, 11177,
			7527, 10996, 8316, 3545, 5064, 8147, 4645, 9082, 3980, 6878, 1814,
			679, 6546, 4771, 10204, 7528, 2789, 6495, 8317, 8148, 9083, 3981,
			6879, 7777, 11178, 6547, 4772, 7529, 5065, 8318, 4646, 1815, 2790,
			9084, 10997, 3546, 7778, 8149, 3982, 6548, 6880, 7530, 8319, 11179,
			680, 5066, 4647, 2791, 9085, 10206, 10998, 1816, 7779, 6496, 6549,
			3547, 4774, 3983, 6881, 8150, 7531, 681, 5067, 9087, 4648, 2792,
			10999, 6550, 6497, 10208, 7532, 7780, 6882, 5068, 682, 2793, 11000,
			9088, 4649, 3984, 3548, 6498, 6551, 8151, 683, 1817, 5069, 6883,
			11001, 9089, 4650, 2794, 6499, 6552, 7533, 7781, 10210, 4775, 684,
			8152, 2795, 1818, 6884, 11002, 9090, 7534, 4651, 6553, 3985, 7782,
			3549, 685, 10212, 8153, 2796, 11003, 9091, 7535, 4652, 6554, 7783,
			686, 10214, 3550, 3986, 8154, 2797, 7536, 11004, 4653, 9092, 6555,
			10329, 1819, 7784, 687, 6885, 4776, 10216, 3551, 2798, 7537, 11005,
			4654, 8155, 9093, 6556, 3987, 688, 10218, 3552, 2799, 4777, 7538,
			6886, 11006, 6557, 689, 9094, 4655, 8156, 7785, 10220, 3988, 3553,
			2800, 7539, 11007, 6558, 9095, 4656, 690, 10222, 3989, 3554, 6887,
			8157, 7786, 2801, 4779, 11008, 6559, 9096, 691, 4657, 3555, 10224,
			6888, 8158, 7787, 3990, 2802, 4780, 11009, 6560, 9097, 692, 3556,
			4658, 10226, 8159, 2803, 7788, 3991, 4781, 11010, 6561, 9098, 693,
			3557, 4659, 10228, 2804, 7789, 8160, 3992, 4782, 11011, 6564, 9099,
			694, 3558, 4660, 2805, 10230, 3993, 7790, 8161, 11012, 6565, 695,
			3559, 4661, 2806, 4784, 10232, 11013, 3994, 7791, 6566, 8162, 696,
			3560, 4662, 2807, 4785, 11014, 10234, 6567, 3995, 7792, 8163, 3561,
			697, 4663, 2808, 4786, 11015, 10236, 6570, 7793, 3996, 8164, 698,
			4664, 3562, 2809, 4787, 11016, 10238, 4665, 7794, 3997, 699, 8165,
			3563, 2810, 11017, 10240, 3564, 700, 4666, 8166, 3998, 7795, 2811,
			4789, 11018, 10242, 3565, 4667, 701, 3999, 2812, 8167, 7796, 4790,
			11019, 10244, 4670, 3566, 7797, 702, 2813, 4000, 8168, 4791, 11020,
			10246, 3567, 4671, 703, 7798, 2814, 8169, 4001, 4792, 11021, 10248,
			3568, 4672, 704, 7799, 2815, 4004, 8170, 11022, 10250, 4673, 705,
			3569, 2816, 8171, 4794, 4005, 11023, 10252, 4674, 706, 3570, 4795,
			8172, 2817, 11024, 4006, 10254, 4677, 3571, 707, 4796, 8173, 2818,
			4007, 11025, 3572, 4678, 10256, 708, 4797, 8174, 2819, 11026, 4020,
			3573, 4679, 10258, 709, 8175, 11031, 2820, 4021, 3574, 10260, 710,
			4799, 11032, 8176, 2821, 4022, 10262, 3575, 711, 4800, 11033, 8177,
			2822, 4023, 10264, 3576, 712, 4801, 11034, 8178, 2823, 10266, 4024,
			3577, 713, 4802, 11035, 8179, 2824, 10268, 3578, 4025, 714, 11036,
			2825, 8180, 3579, 4026, 715, 2887, 4804, 11039, 8181, 2826, 3580,
			716, 4027, 4805, 2888, 11040, 8182, 3581, 2827, 717, 4806, 2889,
			4028, 11041, 3582, 8183, 2828, 718, 4807, 2892, 4029, 11042, 3583,
			4808, 8184, 2829, 719, 2893, 11043, 3584, 4809, 720, 8185, 2830,
			2944, 11044, 3585, 721, 8186, 2945, 2831, 11045, 3586, 722, 2946,
			3587, 723, 2947, 2832, 3588, 724, 2948, 3589, 8187, 725, 2949,
			2833, 3590, 8188, 726, 11047, 2950, 2834, 3591, 8189, 729, 2835,
			2951, 3592, 730, 11048, 2952, 2836, 3593, 11049, 731, 2953, 2837,
			3594, 732, 2954, 2838, 3595, 735, 2957, 2839, 3596, 736, 2958,
			2840, 3597, 737, 2963, 2841, 3598, 738, 2966, 2842, 3599, 739,
			2967, 2843, 3600, 740, 2968, 3601, 2844, 741, 2969, 3602, 2845,
			742, 2978, 3603, 2846, 743, 2979, 3604, 2847, 744, 2980, 3605,
			2848, 745, 2981, 3606, 2849, 746, 2982, 3607, 2850, 747, 2983,
			3608, 2851, 748, 2984, 3609, 749, 2852, 2985, 3610, 750, 2853,
			2986, 3611, 755, 2854, 2987, 3612, 756, 2988, 2855, 3613, 757,
			2989, 2856, 3614, 758, 2857, 3615, 771, 2858, 3616, 772, 3617, 773,
			3618, 774, 3619, 775, 3620, 776, 3621, 777, 3622, 778, 3623, 779,
			3624, 3625, 3626, 3627, 3628, 3629, 3630, 3631, 3632, 3633, 3634,
			3635, 3636, 3637, 3638, 3639, 12954, 13280, 13281, 13329, 20760, 20764, 13330, 13331, 13332, 13333, 13334, 13335, 13336, 13337, 13338, 13124 };
	
	/**
	 * Checks if a specific ground item is player bound.
	 * @return True if, false if not.
	 */
	public static boolean playerBoundItem(int id) {
		long time = System.nanoTime();
		for(int item : DESTROYABLE_ITEMS) {
			if(item == id) {
				return true;
			}
		}
		for(int item : PLAYER_BOUND_ITEMS) {
			if(item == id) {
				return true;
			}
		}
		//System.out.println("Spend: " + (System.nanoTime() - time) + " nano seconds.");
		return false;
	}
	
	/**
	 * Array containing all the destroyable items in the 459 cache.
	 */
	public static final int[] DESTROYABLE_ITEMS = { 0, 1, 3, 456, 457, 458,
			459, 460, 461, 462, 463, 616, 671, 672, 673, 763, 765, 769, 775,
			776, 777, 778, 1458, 2372, 2373, 2374, 2375, 2376, 2377, 2378,
			2379, 2380, 2381, 2382, 2383, 2384, 2389, 2390, 2393, 2399, 2400,
			2401, 3698, 3722, 3723, 3724, 3725, 3726, 3727, 3728, 3729, 3730,
			3731, 3732, 3733, 5545, 6112, 6113, 6118, 6453, 6454, 6455, 6456,
			6457, 6458, 6459, 6460, 6461, 6462, 6463, 6464, 6465, 6478, 6479,
			6541, 6542, 6543, 6544, 6545, 6546, 6547, 6548, 6635, 6639, 6640,
			6641, 6642, 6643, 6644, 6645, 6646, 6647, 6648, 6653, 6754, 6755,
			6756, 6757, 6758, 6759, 6769, 6770, 6817, 6818, 6819, 6820, 6821,
			6854, 6855, 6856, 6857, 6858, 6859, 6860, 6861, 6862, 6863, 6865,
			6866, 6867, 6885, 6886, 6887, 6891, 6945, 6946, 6947, 6948, 6949,
			6950, 6951, 6952, 6953, 6954, 6955, 6956, 6957, 6958, 6985, 6986,
			6987, 6988, 6989, 6990, 6991, 6992, 6993, 6994, 6995, 6996, 6997,
			6998, 6999, 7000, 7001, 7002, 7408, 7409, 7410, 7411, 7470, 7471,
			7473, 7474, 7475, 7476, 7477, 7478, 7479, 7498, 7542, 7543, 7544,
			7545, 7546, 7628, 7629, 7630, 7632, 7633, 7634, 7635, 7649, 7774,
			7775, 7776, 7958, 7959, 7966, 7968, 9005, 9006, 9013, 9025, 9076,
			9077, 9083, 9084, 9085, 9086, 9087, 9088, 9089, 9090, 9091, 9092,
			9093, 9096, 9097, 9098, 9099, 9100, 9101, 9102, 9103, 9104, 9433,
			9474, 9589, 9625, 9626, 9633, 9646, 9647, 9648, 9649, 9651, 9652,
			9653, 9654, 9655, 9656, 9657, 9658, 9659, 9660, 9662, 9682, 9684,
			9685, 9686, 9687, 9688, 9689, 9690, 9691, 9692, 9693, 9694, 9695,
			9696, 9697, 9698, 9699, 9702, 9703, 9704, 9705, 9706, 9901, 9902,
			9903, 9906, 9907, 9908, 9909, 9910, 9911, 9912, 9913, 9914, 9915,
			9916, 9917, 9918, 9919, 9920, 9921, 9922, 9923, 9924, 9925, 9932,
			9944, 9945, 9947, 10174, 10175, 10176, 10177, 10487, 10488, 10489,
			10490, 10491, 10492, 10493, 10494, 10495, 10498, 10499, 10500,
			10507, 10508, 10512, 10513, 10514, 10515, 10531, 10532, 10533,
			10539, 10540, 10541, 10542, 10543, 10544, 10545, 10546, 10547,
			10548, 10549, 10550, 10551, 10552, 10553, 10554, 10555, 10562,
			10581, 10582, 10583, 10584, 10585, 10586, 10587, 10595, 10596,
			10609, 10722, 10723, 10724, 10725, 10726, 10727, 10728, 10730,
			10829, 10830, 10831, 10832, 10833, 10834, 10835, 10836, 10837,
			10838, 10839, 10842, 10856, 10857, 10858, 10862, 10863, 10864,
			10865, 10883, 10887, 10889, 10890, 10934, 10935, 10936, 10942,
			10943, 10944, 10983, 10984, 10985, 10986, 10987, 10988, 10989,
			10990, 10991, 10992, 10993, 10994, 11003, 11006, 11007, 11008,
			11009, 11010, 11012, 11013, 11014, 11019, 11020, 11021, 11022,
			11023, 11024, 11027, 11028, 11029, 11030, 11031, 11032, 11033,
			11034, 11035, 11036, 11039, 11040, 11041, 11042, 11043, 11045,
			11136, 11137, 11138, 11139, 11140, 11141, 11151, 11152, 11153,
			11154, 11155, 11156, 11157, 11158, 11159, 11173, 11174, 11175,
			11185, 11186, 11187, 11188, 11189, 11196, 11197, 11198, 11199,
			11202, 11203, 11204, 11210, 11211, 11238, 11240, 11242, 11244,
			11246, 11248, 11250, 11252, 11254, 11256, 11258, 11259, 11273,
			11279, 8839, 8840, 8842, 11663, 11664, 11665, 11866, 11864, 11865 };

	public static final int[] QUEST_SHOP = {10551, 10548, 10828, 9096, 9097, 9098, 9099, 9100, 9101, 9102, 9104, 9084};

	public static final Location[] HOME_TELEPORTS = {Location.create(2964, 3383), Location.create(2968, 3383), Location.create(2968, 3379), Location.create(2963, 3378), Location.create(2964, 3380), Location.create(2966, 3380)};

	public static final String[] WELCOME_SCREEN = {"Welcome to OS-Anarchy"};
	
	public static final String[] TELEPORTS = { "Home", "Edgeville", "Varrock", "Draynor", "Lumbridge", "Zeah" };
	
	


	public static int getModification(int level) {
		if (level >= 20 && level <= 24) {
			return 4;
		}
		if (level >= 25 && level <= 29) {
			return 6;
		}
		if (level >= 30 && level <= 39) {
			return 7;
		}
		if (level >= 40 && level <= 49) {
			return 8;
		}
		if (level >= 50 && level <= 59) {
			return 11;
		}
		if (level >= 60 && level <= 69) {
			return 12;
		}
		if (level >= 70 && level <= 74) {
			return 13;
		}
		if (level >= 75 && level <= 79) {
			return 15;
		}
		if (level >= 80 && level <= 89) {
			return 16;
		}
		if (level >= 90 && level <= 92) {
			return 17;
		}
		if (level >= 93 && level <= 99) {
			return 22;
		}
		return 3;
	}

	public static boolean hasMaxCape(Mob mob) {
		return mob.getEquipment() != null && (mob.getEquipment().contains(13280) || mob.getEquipment().contains(13329) || mob.getEquipment().contains(13331) || mob.getEquipment().contains(13333) || mob.getEquipment().contains(13335) ||  mob.getEquipment().contains(13337)|| mob.getEquipment().contains(20760)|| mob.getEquipment().contains(20764));
	}

	public static boolean isMaxCape(int id) {
		switch (id) {
			case 13280:
			case 13329:
			case 13331:
			case 13333:
			case 20760:
			case 20764:
			case 13335:

		}
		return false;
	}

	public static boolean hasAttackCape(Mob mob) {
		return mob.getEquipment() != null && (mob.getEquipment().contains(9747) || mob.getEquipment().contains(9748) || hasMaxCape(mob));
	}

	public static boolean hasCookingCape(Mob mob) {
		return mob.getEquipment() != null && (mob.getEquipment().contains(9801) || mob.getEquipment().contains(9802) || hasMaxCape(mob));
	}

	public static boolean hasCraftingCape(Mob mob) {
		return mob.getEquipment() != null && (mob.getEquipment().contains(9780) || mob.getEquipment().contains(9781)) || hasMaxCape(mob);
	}

	public static boolean hasSerpHelm(Mob mob) {
		return mob.getEquipment() != null && (mob.getEquipment().contains(12931) || mob.getEquipment().contains(13197) || mob.getEquipment().contains(13199));
	}

	public enum ImbuedRings {
		SEERS(6731, 11770),

		ARCHER(6733, 11771),

		WARRIOR(6735, 11772),

		BERSERKER(6737, 11773);

		private final int normal;

		private final int imbued;

		ImbuedRings(int normal, int imbued) {
			this.normal = normal;
			this.imbued = imbued;
		}

		public static Optional<ImbuedRings> of(int itemId) {
			return Arrays.stream(ImbuedRings.values()).filter(i -> itemId == i.normal).findFirst();
		}

		public int getNormal() {
			return normal;
		}

		public int getImbued() {
			return imbued;
		}
	}
	
	public static final int MAX_USERNAME_COUNT = 12;
	
	public static boolean DEV_SERVER = false;
	
	public static final class ReturnCodes {
		public static final int DISPLAY_ADVERTISEMENT = 1;
		public static final int LOGIN_OK = 2;
		public static final int INVALID_PASSWORD = 3;
		public static final int BANNED = 4;
		public static final int ALREADY_ONLINE = 5;
		public static final int GAME_UPDATED_RELOAD = 6;
		public static final int WORLD_FULL = 7;
		public static final int LOGIN_SERVER_OFFLINE = 8;
		public static final int LOGIN_LIMIT_EXCEEDED = 9;
		public static final int BAD_SESSION_ID = 10;
		public static final int FORCE_CHANGE_PASSWORD = 11;
		public static final int MEMBERS_WORLD = 12;
		public static final int COULD_NOT_COMPLETE = 13;
		public static final int UPDATE_IN_PROGRESS = 14;
		public static final int MEMBERS_ONLY_AREA = 17;
		public static final int ERROR_LOADING_PROFILE = 24;
		public static final int USERNAME_TOO_LONG = 60;
	}
}
