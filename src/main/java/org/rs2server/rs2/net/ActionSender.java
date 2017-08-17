package org.rs2server.rs2.net;

import org.rs2server.Server;

import org.rs2server.cache.format.loaders.WorldMapObjectsLoader;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.ScriptManager;
import org.rs2server.rs2.content.QuestTab;
import org.rs2server.rs2.content.StarterMap;
import org.rs2server.rs2.content.api.GamePlayerRegionEvent;
import org.rs2server.rs2.domain.model.player.PlayerEntity;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.PestControlService;
import org.rs2server.rs2.domain.service.api.content.RunePouchService;
import org.rs2server.rs2.domain.service.api.content.bounty.BountyHunterService;
import org.rs2server.rs2.domain.service.api.content.gamble.FlowerGame;
import org.rs2server.rs2.domain.service.api.skill.experience.ExperienceDropService;
import org.rs2server.rs2.domain.service.impl.BankPinServiceImpl;
import org.rs2server.rs2.domain.service.impl.SlayerServiceImpl;
import org.rs2server.rs2.domain.service.impl.skill.FarmingServiceImpl;
import org.rs2server.rs2.model.Animation.FacialAnimation;
import org.rs2server.rs2.model.Palette.PaletteTile;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.bit.BitConfig;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.boundary.BoundaryManager;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.container.*;
import org.rs2server.rs2.model.container.impl.EquipmentContainerListener;
import org.rs2server.rs2.model.container.impl.InterfaceContainerListener;
import org.rs2server.rs2.model.container.impl.RunePouchContainerListener;
import org.rs2server.rs2.model.container.impl.WeaponContainerListener;
import org.rs2server.rs2.model.map.DynamicTile;
import org.rs2server.rs2.model.map.DynamicTileBuilder;
import org.rs2server.rs2.model.minigame.fightcave.FightCave;
import org.rs2server.rs2.model.minigame.fightcave.Wave;
import org.rs2server.rs2.model.minigame.rfd.RFDWave;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.player.PrivateChat;
import org.rs2server.rs2.model.player.PrivateChat.ClanRank;
import org.rs2server.rs2.model.player.RequestManager.RequestState;
import org.rs2server.rs2.model.quests.impl.CooksAssistant;
import org.rs2server.rs2.model.quests.impl.CooksAssistantState;
import org.rs2server.rs2.model.quests.impl.DTStates;
import org.rs2server.rs2.model.quests.impl.DesertTreasure;
import org.rs2server.rs2.model.quests.impl.LunarDiplomacy;
import org.rs2server.rs2.model.quests.impl.LunarStates;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.model.region.RegionManager;
import org.rs2server.rs2.model.skills.crafting.Tanning;
import org.rs2server.rs2.model.sound.Music;
import org.rs2server.rs2.net.Packet.Type;
import org.rs2server.rs2.tickable.StoppingTick;
import org.rs2server.rs2.tickable.impl.VenomDrainTick;
import org.rs2server.rs2.util.Misc;
import org.rs2server.rs2.util.NameUtils;
import org.rs2server.rs2.util.TextUtils;
import org.rs2server.rs2.varp.PlayerVariable;
import org.rs2server.util.MapXTEA;
import org.rs2server.util.PlayersOnline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A utility class for sending packets.
 *
 * @author Graham Edgecombe
 */
public class ActionSender {

    private static final Logger logger = LoggerFactory.getLogger(ActionSender.class);

    public static final String ADMINS[] = {"zero", "zaros", "nomac"};
    public static final String MODERATORS[] = {"iron titan", "ice"};
	public static final String HELPERS[] = {"", ""};
	
	//private static final PlayerVariable RIGOUR = PlayerVariable.of(5451);
	//private static final PlayerVariable AUGURY = PlayerVariable.of(5452);
	//private static final PlayerVariable PRESERVE = PlayerVariable.of(5453);
	
	private static final PlayerVariable NOTE_VARIABLE = PlayerVariable.of(3958);
	private static final PlayerVariable BANK_TAB_VARIABLE = PlayerVariable.of(4150);
	private static final PlayerVariable SWAP_VARIABLE = PlayerVariable.of(3959);
	private static final PlayerVariable LAST_WITHDRAW_VARIABLE = PlayerVariable.of(3960);
	private final PlayerVariableService variableService;
	private final PestControlService pestControlService;

    public static int NO_BLACKOUT = 0, BLACKOUT_ORB = 1, BLACKOUT_MAP = 2,
            BLACKOUT_ORB_AND_MAP = 5;

    private final PermissionService permissionService;
    private final HookService hookService;
    private final ExperienceDropService experienceDropService;
	private final BankPinService bankPinService;
	private final RunePouchService pouchService;

    /**
     * The player.
     */
    private Player player;

    /**
     * The player's ping count.
     */
    private int pingCount;

    /**
     * Creates an action sender for the specified player.
     *
     * @param player The player to create the action sender for.
     */
    public ActionSender(Player player) {
        this.player = player;
        this.permissionService = Server.getInjector().getInstance(PermissionService.class);
        this.hookService = Server.getInjector().getInstance(HookService.class);
        this.experienceDropService = Server.getInjector().getInstance(ExperienceDropService.class);
		this.variableService = Server.getInjector().getInstance(PlayerVariableService.class);
		this.pestControlService = Server.getInjector().getInstance(PestControlService.class);
		this.bankPinService = Server.getInjector().getInstance(BankPinService.class);
		this.pouchService = Server.getInjector().getInstance(RunePouchService.class);
    }
    public ActionSender sendSlayerReward() {
        sendInterface(426, false);
        sendAccessMask(2, 426, 8, 0, 37);
        sendAccessMask(1052, 426, 23, 0, 3);
        return this;
    }

    public ActionSender sendSkillMenu(int skill) {
        sendCS2Script(917, new Object[]{80, 4600861}, "ii")
                .sendInterface(214, false)
                .sendConfig(965, getSkillForButton(skill));
        player.setAttribute("viewingSkill", getSkillForButton(skill));
        return this;
    }

    private int getSkillForButton(int skill) {
        switch (skill) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 5;
            case 4:
                return 3;
            case 5:
                return 7;
            case 6:
                return 4;
            case 7:
                return 12;
            case 8:
                return 22;
            case 9:
                return 6;
            case 10:
                return 8;
            case 11:
                return 9;
            case 12:
                return 10;
            case 13:
                return 11;
            case 14:
                return 19;
            case 15:
                return 20;
            case 16:
                return 23;
            case 17:
                return 13;
            case 18:
                return 14;
            case 19:
                return 15;
            case 20:
                return 16;
            case 21:
                return 17;
            case 22:
                return 18;
            case 23:
                return 21;
        }
        return 0;
    }

    public ActionSender sendLogin() {
        //player.setRights(Rights .getRights(player.getFor));

        for (String admins : ADMINS) {
            if (player.getName().toLowerCase().equals(admins)) {
				player.getDetails().setForumRights(2);
                permissionService.give(player, PermissionService.PlayerPermissions.ADMINISTRATOR);
            }
        }
		for (String admins : MODERATORS) {
			if (player.getName().toLowerCase().equals(admins)) {
				player.getDetails().setForumRights(1);
				permissionService.give(player, PermissionService.PlayerPermissions.MODERATOR);
			}
		}
		for (String admins : HELPERS) {
			if (player.getName().toLowerCase().equals(admins)) {
				player.getDetails().setForumRights(10);
				permissionService.give(player, PermissionService.PlayerPermissions.HELPER);
			}
		}
	
		for(int i = 0; i < player.getDatabaseEntity().getOwnedPerks().length; i++)
		{
			if(player.getDatabaseEntity().getOwnedPerks()[i] == true)
			{
				player.getPerks()[i].givePerk();
			}
		}
        sendMapRegion();
		player.setActive(true);
        player.setAttribute("transparentChat", false);
        sendWindowPane(165);
        sendSideBar();
        init(player);
        player.setAttribute("atMenu", true);
        
        ScriptManager.getScriptManager().invoke("sendLogin", player);
        
        
        PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
        if (settings.isPetSpawned()) {
            Pet pet = new Pet(player, settings.getPetId());
            player.setPet(pet);
            World.getWorld().register(pet);
        }
        
        /*variableService.set(player, RIGOUR, 1);
        variableService.set(player, AUGURY, 1);
        variableService.set(player, PRESERVE, 1);
        
        variableService.send(player, RIGOUR);
        variableService.send(player, AUGURY);
        variableService.send(player, PRESERVE);*/
        
        //sendConfig(101, 0);//Quest Points
        
        player.sendAccess(Access.of(399, 7, NumberRange.of(0, 18), AccessBits.ALLOW_LEFT_CLICK));
        player.sendAccess(Access.of(399, 8, NumberRange.of(0, 110), AccessBits.ALLOW_LEFT_CLICK));
        player.sendAccess(Access.of(399, 9, NumberRange.of(0, 12), AccessBits.ALLOW_LEFT_CLICK));
        
        player.sendAccess(Access.of(259, 4, NumberRange.of(0, 12), AccessBits.ALLOW_LEFT_CLICK));
        
        sendConfig(1043, 0);
        sendConfig(563, 0);
        sendConfig(375, 0);
        sendConfig(1151, -1);
        
        sendConfig(1002, 536870920);
        
        sendConfig(1055, 8784);//74304
        
        sendConfig(1017, 8192);
        sendConfig(1045, 1883184128);
        sendConfig(1046, -268369792);
        sendConfig(1047, 5120);
        
        //sendConfig(150, 160);//The Grand Tree

        sendConfig(1050, 4113);//4113 4096

        sendConfig(1224, 172395521);//172395521   172395585
        sendConfig(1225, 379887846);//379887846
        sendConfig(1226, 12);
        sendConfig(1227, player.getDropdown().hashCode());

        sendConfig(313, 255); //SkillCape Off + First Part Of Emotes
        sendConfig(465, 511); // 2nd part Of Emotes
        sendConfig(802, 511); // 3rd Part Of Emotes
        sendConfig(1085, 700); // 4th Part Of Emotes
        
		if (permissionService.is(player, PermissionService.PlayerPermissions.IRON_MAN)) {
			sendConfig(499, 1621114888);
		}

		if (permissionService.is(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN)) {
			sendConfig(499, 24);
		}
		
		if (permissionService.is(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN)) {
			sendConfig(499, -2147483632);
		}

		//updateQuestText();

		
        InterfaceContainerListener inventoryListener = new InterfaceContainerListener(
                player, Inventory.INTERFACE, 0, 93);
        player.getInventory().addListener(inventoryListener);

        InterfaceContainerListener equipmentListener = new InterfaceContainerListener(
                player, -1, 64208, 94);
        player.getEquipment().addListener(equipmentListener);

		RunePouchContainerListener pouchListener = new RunePouchContainerListener(player);

		player.getRunePouch().addListener(pouchListener);

        player.getEquipment().addListener(
                new EquipmentContainerListener(player));

        player.setLastLocation(player.getLocation());

        player.getCombatState().calculateBonuses();
        if (player.getPrivateChat() != null) {
            player.getPrivateChat().updateFriendList(true);
        } else {
            World.getWorld().getPrivateChat()
                    .put(player.getName(), new PrivateChat(player.getName(), ""));
        }
        sendInteractionOption("null", 1, true); // null or fight
        sendInteractionOption("null", 2, false); // challenge = duel arena
        sendInteractionOption("Follow", 3, false);
        sendInteractionOption("Trade with", 4, false);

        player.getEquipment().addListener(new WeaponContainerListener(player));

        sendFriends();//f
        sendIgnores();
        player.checkForSkillcapes();

		pouchService.updatePouchInterface(player);
        Access emoteAccess = Access.of(216, 1, NumberRange.of(0, 43), AccessBits.ALLOW_LEFT_CLICK);
        player.sendAccess(emoteAccess);


        sendAccessMask(2, 216, 1, 0, 42);
        sendAccessMask(2, 239, 1, 0, 504);

        for (int i = 0; i < 100; i++) {
            sendAccessMask(2, 261, i, 0, 8);
        }
        if (player.getCombatState().getSpellBook() != MagicCombatAction.SpellBook.MODERN_MAGICS.getSpellBookId()) { 
        	sendConfig(439, player.getCombatState().getSpellBook() == MagicCombatAction.SpellBook.ANCIENT_MAGICKS.getSpellBookId() ? 1 : 3);//1 : 3
        }
        

		if (player.getDatabaseEntity().getPlayerSettings().getTeleBlockTimer() == 0 && player.getDatabaseEntity().getPlayerSettings().isTeleBlocked()) {
			player.getDatabaseEntity().getPlayerSettings().setTeleBlocked(false);
		}
      sendGlobalCC();
        updateSplitPrivateChatConfig();
       // updateAutoRetaliateConfig();
        updateBankConfig();
       // updateClickPriority();
		//updateSoundVolume();
        Prayers.refreshQuickPrayers(player);
        boolean starter = player.getAttribute("starter");
        if (starter) {
            String IP = player.getSession().getRemoteAddress().toString().split(":")[0].replaceFirst("/", "");
            int count = StarterMap.getSingleton().getCount(IP);
            if (count > Constants.MAX_STARTER_COUNT) {
            	sendMessage("Due to the number of accounts you have created, you are no longer "
            			+ "elegible to receive any starting items on newly created accounts.");
                //World.getWorld().sendWorldMessage("<col=ff0000><img=21>Player: " + player.getName() +" has just joined OS-Anarchy!");
                //sendGlobalCC();
                updateSplitPrivateChatConfig();
                return this;
            } else {
                StarterMap.getSingleton().addIP(IP);
            }
            sendInterface(269, false);
            player.setQueuedSwitching(false);
        }
        if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "RFD")) {
            if (player.getRFD().getWave() == null) {
                RFDWave wave = new RFDWave();
                player.getRFD().setWave(wave);
                wave.set(player.getSettings().getRFDState());
            }
            player.getRFD().setStarted(true);
            player.getRFD().setStartedWave(false);
        }
        if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "FightCaves")) {
            if (player.getFightCave().getWave() == null) {
                Wave wave = new Wave();
                player.getFightCave().setWave(wave);
                wave.set(player.getSettings().getFightCaveState());
            }
            FightCave.IN_CAVES.add(player);
            player.getFightCave().setStarted(true);
            player.getFightCave().setStartTime(10);
        }
		if (player.getDatabaseEntity().getCombatEntity().getVenomDamage() >= 6) {
			player.setAttribute("venom", true);
			player.setVenomDrainTick(new VenomDrainTick(player));
			World.getWorld().submit(player.getVenomDrainTick());
		}
		sendGameObjectsInArea();
        return this;
    }

    public ActionSender sendGlobalCC() {
        if (World.getWorld().privateIsRegistered("Help")) {
            PrivateChat chat = World.getWorld().getPrivateChat().get("Help");
            if (chat != null && player != null) {
                chat.addClanMember(player);
            }
        }
        return this;
    }


    public ActionSender sendScreenBrightness() {
        return sendConfig(166, player.getDatabaseEntity().getPlayerSettings().getPlayerScreenBrightness());
    }

    public ActionSender packet153(int i) {
        player.getSession().write(new PacketBuilder(153).putShort(i).toPacket());
        return this;
    }

    public ActionSender packet170(int i) {
        player.getSession().write(new PacketBuilder(170).put((byte) i).toPacket());
        return this;
    }

    public ActionSender switchTab(int i) {
        player.getSession().write(new PacketBuilder(171).put((byte) i).toPacket());
        return this;
    }

    public ActionSender sendRandom(int interfaceId, int child, int unknown) {
        PacketBuilder bldr = new PacketBuilder(192);
        bldr.putShort(unknown)
                .putInt(interfaceId << 16 | child);
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends an inventory interface.
     *
     * @param interfaceId          The interface id.
     * @param inventoryInterfaceId The inventory interface id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendInterfaceInventory(int inventoryInterfaceId) {//56
		player.getInterfaceState().inventoryInterfaceOpened(inventoryInterfaceId);
        return sendInterface(player.getAttribute("tabmode"), (int) player.getAttributes().get("tabmode") == 548 ? 61 : (int) player.getAttributes().get("tabmode") == 164 ? 57 : 59, inventoryInterfaceId,
                false);
    }
    
    public ActionSender sendInterfaceLogout(int logoutInterfaceId) {//69
		player.getInterfaceState().inventoryInterfaceOpened(logoutInterfaceId);
        return sendInterface(player.getAttribute("tabmode"), (int) player.getAttributes().get("tabmode") == 548 ? 71 : (int) player.getAttributes().get("tabmode") == 164 ? 71 : 71, logoutInterfaceId,
                false);
    }
    
    public ActionSender sendConstructMapRegion1(Palette palette, Location location) {
		player.setLastKnownRegion(player.getLocation());
		PacketBuilder bldr = new PacketBuilder(241, Type.VARIABLE_SHORT);
		bldr.putShortA(player.getLocation().getRegionY() + 6);
		bldr.startBitAccess();
		for(int z = 0; z < 4; z++) {
			for(int x = 0; x < 13; x++) {
				for(int y = 0; y < 13; y++) {
					PaletteTile tile = palette.getTile(x, y, z);
					bldr.putBits(1, tile != null ? 1 : 0);
					if(tile != null) {
						bldr.putBits(26, tile.getX() << 14 | tile.getY() << 3 | tile.getZ() << 24 | tile.getRotation() << 1);
					}
				}
			}
		}
		bldr.finishBitAccess();
		bldr.putShort(player.getLocation().getRegionX() + 6);
		//player.write(bldr.toPacket());
		return this;
	}
    
    /**
	* Sends the packet to construct a map region.
	* @param palette The palette of map regions.
	* @param newLocation The new location of the player.
	* @return The action sender instance, for chaining.
	*/
	public ActionSender sendConstructMapRegion(Palette palette, Location newLocation) {
		player.setLastKnownRegion(player.getLocation());
		player.setLocation(newLocation);
		PacketBuilder bldr = new PacketBuilder(27, Type.VARIABLE_SHORT);
		bldr.putLEShort(player.getLocation().getLocalY());
		bldr.putShortA(player.getLocation().getRegionX());
		bldr.putLEShort(player.getLocation().getRegionY());
		bldr.putByteS((byte) (player.getLocation().getZ()));
		bldr.putShort(player.getLocation().getLocalX());
		bldr.startBitAccess();
		for (int height = 0; height < 4; height++) {
			for (int xCalc = 0; xCalc < 13; xCalc++) {
				for (int yCalc = 0; yCalc < 13; yCalc++) {
					PaletteTile tile = palette.getTile(xCalc, yCalc, height);
					//System.out.println(tile);
					if(tile != null) {
						bldr.putBits(1, 1);
						bldr.putBits(26, tile.getX() << 14 | tile.getY() << 3 | tile.getZ() << 24 | tile.getRotation() << 1);
						// bldr.putBits(26, tile.getRotation() << 1 | 0 | (z << 24) | (x << 14) | (y << 3));
					} else {
						bldr.putBits(1, 0);
					}
				}
			}
		}
		bldr.finishBitAccess();
		int[] sent = new int[4 * 13 * 13];
		int sentIndex = 0;
		for (int height = 0; height < 4; height++) {
			for (int xCalc = 0; xCalc < 13; xCalc++) {
outer:
				for (int yCalc = 0; yCalc < 13; yCalc++) {
					PaletteTile tile = palette.getTile(xCalc, yCalc, height);
					if(tile != null) {
						//if (zPallete[height][xCalc][yCalc] != -1 && xPallete[height][xCalc][yCalc] != -1 && yPallete[height][xCalc][yCalc] != -1) {
						int x = tile.getX() / 8;
						int y = tile.getY() / 8;
						int region = y + (x << 8);
						for (int i = 0; i < sentIndex; i++) {
							if (sent[i] == region) {
								break outer;
							}
						}
						sent[sentIndex] = region;
						sentIndex++;
						bldr.putInt(0);
						bldr.putInt(0);
						bldr.putInt(0);
						bldr.putInt(0);
					}
				}
			}
		}
		player.getSession().write(bldr.toPacket());
		return this;
	}

    /**
     * Sends a specific sidebar interface.
     *
     * @param icon        The sidebar icon.
     * @param interfaceId The interface id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendSidebarInterface(int tabId, int interfaceId) {
        sendInterface(player.getAttribute("tabmode"), tabId, interfaceId, true);
        return this;
    }

    public ActionSender sendInterface(int windowId, int childId,
                                      int interfaceId, boolean walkable) {
        if (windowId == Constants.MAIN_WINDOW
                && childId == Constants.GAME_WINDOW) {
            player.getInterfaceState().interfaceOpened(interfaceId, walkable);
        } else if (windowId == Constants.MAIN_WINDOW
                && childId == Constants.SIDE_TABS) {
            player.getInterfaceState().setSidetabInterface(interfaceId);
        } else if (interfaceId == 660) {
            player.setInterfaceAttribute("oldAppearance", player
                    .getAppearance().getLook());
        } else if (interfaceId == 90 && walkable) {
            player.getInterfaceState().setWalkableInterface(true);
        }
        PacketBuilder pb = new PacketBuilder(66);
        pb.putShort(interfaceId);
        pb.writeIntV1(windowId << 16 | childId);
        pb.putByteS((byte) (walkable ? 1 : 0));
        player.getSession().write(pb.toPacket());
        return this;
    }

    public ActionSender sendInterfaceModel(int interfaceId, int childId,
                                           int size, int itemId) {
        /*
         * PacketBuilder bldr = new PacketBuilder(67); MODEL I THINK
		 * bldr.putLEShortA(itemId); bldr.putLEInt(interfaceId << 16 | childId);
		 * player.write(bldr.toPacket());
		 */
        PacketBuilder bldr = new PacketBuilder(197);
        bldr.putLEShort(itemId);
        bldr.putLEInt(interfaceId << 16 | childId);
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * zoom is usually 175
     */
    public ActionSender sendItemOnInterface(int interfaceId, int childId,
                                            int itemId, int zoom) {
        PacketBuilder bldr = new PacketBuilder(224);
        bldr.putInt(interfaceId << 16 | childId);
        bldr.putLEShortA(itemId);
        bldr.putLEInt(zoom);
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender removeWalkableInterface() {
        player.getInterfaceState().setWalkableInterface(false);
        return removeInterface((int) player.getAttributes().get("tabmode") == 548 ? 12 : 10);
    }

    public ActionSender sendWalkableInterface(int interfaceId) {
        if (interfaceId == -1) {
            player.getInterfaceState().interfaceClosed();
            return removeInterface();
        }
        player.getInterfaceState().setWalkableInterface(true);
        return sendInterface((int) player.getAttributes().get("tabmode"), (int) player.getAttributes().get("tabmode") == 548 ? 12 : 10, interfaceId, true);
    }

    public ActionSender sendInterface(int interfaceId, boolean interfaceClosed) {
        if (interfaceClosed) {
            player.getInterfaceState().interfaceClosed();
        }
        player.getInterfaceState().interfaceOpened(interfaceId, false);
        return sendInterface((int) player.getAttributes().get("tabmode"), (int) player.getAttributes().get("tabmode") == 548 ? 19 : 12, interfaceId, false);
    }


    // public void sendInterface(int interfaceId) {
    // for (int i = 0; i < 50; i++) {
    // sendInterface(Constants.MAIN_WINDOW, i, interfaceId, false);
    // }
    // }

    public ActionSender sendChatInterface(int interfaceId) {
        return sendInterface(Constants.MAIN_WINDOW, Constants.CHAT_BOX,
                interfaceId, false);
    }

    public ActionSender sendDefaultChatbox() {
        player.write(new PacketBuilder(75)
                .put((byte) player.getInterfaceState().getPublicChat())
                .put((byte) player.getInterfaceState().getPrivateChat())
                .put((byte) player.getInterfaceState().getTrade()).toPacket());
        // player.write(new PacketBuilder(17).putByteA((byte)
        // 0).putLEShort(137).putShort(90).putShort(Constants.MAIN_WINDOW).toPacket());
        return this;
    }

    public ActionSender packet138() {
        player.write(new PacketBuilder(138).toPacket());
        return this;
    }

    /**
     * Sends all the login packets.
     *
     * @return The action sender instance, for chaining.
     * <p>
     * PANE 548 = FIXED 161 = Resizable 165 = Welcome screen
     */

    public ActionSender sendInterfaceSetting(int to, int from, int setting, int hash) { //how the fk u search with this ide
        PacketBuilder bldr = new PacketBuilder(196);
        bldr.putLEShortA(to).putShort(from).putInt2(setting).writeIntV1(hash);
        player.getSession().write(bldr.toPacket());
        return this;
    }


    public void sendSideTab(int set, int interfaceId) {
        sendInterface(165, set, interfaceId, true);
    }

    
/*public ActionSender updateQuestText() {
    	
    	StringBuilder builder = new StringBuilder();
    	
    	builder.append("<col=a04203>Players Online: " + World.getWorld().getPlayers().size());//   <img=4>  + "<img=4>"
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
    	builder.append("<br>");
		
		sendString(399, 0, builder.toString());
		
        return this;
    }*/

	public ActionSender closeAll() {
		if (player.getInterfaceState().isInterfaceOpen(FarmingServiceImpl.INTERFACE_TOOL_STORE_ID)) {
			player.getActionSender().removeInventoryInterface();
		}

		if (player.getInterfaceState().isInterfaceOpen(BankPinServiceImpl.BANK_PIN_WIDGET) || player.getInterfaceState().isInterfaceOpen(BankPinServiceImpl.BANK_SETTINGS_WIDGET)) {
			bankPinService.onClose(player);
		}

		if (player.getInterfaceState().isInterfaceOpen(PriceChecker.PRICE_INVENTORY_INTERFACE)) {
			PriceChecker.returnItems(player);
		}

		if (player.getInterfaceState().isEnterAmountInterfaceOpen()) {
			player.getActionSender().removeEnterAmountInterface();
		}

		if (player.getAttribute("bank_searching") != null) {
			player.getActionSender().removeEnterAmountInterface();
			player.removeAttribute("bank_searching");
		}

		if (player.getInterfaceState().isInterfaceOpen(Equipment.SCREEN) || player.getInterfaceState().isInterfaceOpen(Bank.BANK_INVENTORY_INTERFACE) || player.getInterfaceState().isInterfaceOpen(Trade.TRADE_INVENTORY_INTERFACE) || player.getInterfaceState().isInterfaceOpen(Shop.SHOP_INVENTORY_INTERFACE) || player.getInterfaceState().isInterfaceOpen(PriceChecker.PRICE_INVENTORY_INTERFACE)) {
			player.getActionSender().removeInventoryInterface();
			player.resetInteractingEntity();
		}

		player.getInterfaceState().setOpenShop(-1);
		player.getActionSender().removeAllInterfaces().removeInterface();
		return this;
	}

    public ActionSender loadClientSettings() {
        player.getSession().write(
                new PacketBuilder(39).toPacket());
        return this;
    }

    public ActionSender sendSideBar() {
        sendSideTab(1, 162);//chat box
        sendSideTab(4, 122);//exp counter
        sendSideTab(22, 163);//game window?
        sendSideTab(23, 160);//orbs
		sendSideTab(26, 378);//welcome screen
		sendSideTab(27, 50);//black screen
        sendSideTab(2, 162);//chat box?
        sendSideTab(8, 320);//skills
        sendSideTab(9, 399);//274 399
        sendSideTab(10, 149);//inventory
        sendSideTab(11, 387);//worn equipment
        sendSideTab(12, 541);//prayer - 271
        sendSideTab(13, 218);//spellbook
        sendSideTab(15, 429);//friends list
        sendSideTab(16, 432);//ignore list
        sendSideTab(17, 182);//logout
        sendSideTab(18, 261);//settings
        sendSideTab(19, 216);//emotes
        sendSideTab(20, 239);//music
        sendSideTab(14, 589);//clanchat
        sendSideTab(7, 593);//att style
        return this;
    }

    /**
     * Sends the 'Window Pane'
     *
     * @param interfaceId the interfaceId to make the window pane
     * @return The action sender instance for chaining.
     */
    public ActionSender sendWindowPane(int interfaceId) {
        player.getSession().write(
                new PacketBuilder(108).putLEShortA(interfaceId).toPacket());
        return this;
    }

    /**
     * Sends the player's skills.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendSkills() {
        for (int i = 0; i < Skills.SKILL_COUNT; i++) {
            sendSkill(i);
        }
        return this;
    }

    /**
     * Sends a specific skill.
     *
     * @param skill The skill to send.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendSkill(int skill) {
        PacketBuilder bldr = new PacketBuilder(14);
        if (skill == Skills.PRAYER) {
            bldr.put((byte) (Math.ceil(player.getSkills().getPrayerPoints())));
        } else {
            bldr.put((byte) player.getSkills().getLevel(skill));
        }
        bldr.putByteA((byte) skill);
        bldr.putLEInt((int) player.getSkills().getExperience(skill));
        player.getSession().write(bldr.toPacket());
        return this;
    }

    public ActionSender removeSidebarInterfaces() {
        int pane = player.getAttribute("tabmode");
        switch (pane) {
            case 164:
                for (int i = 54; i <= 67; i++) {
                    removeInterfaces(pane, i);
                }
                break;
            case 548:
                for (int i = 60; i <= 73; i++) {
                    removeInterfaces(pane, i);
                }
                break;
        }
        return this;
    }

    /**
     * Sends all the sidebar interfaces.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendSidebarInterfaces() {
        int pane = player.getAttribute("tabmode");
        switch (pane) {
            case 161:

                sendSidebarInterface(22, 162);
                sendSidebarInterface(10, 163);

                sendSidebarInterface(15, 160);

                sendSidebarInterface(61, 593);
                sendSidebarInterface(62, 320);
                sendSidebarInterface(63, 399);//274
                sendSidebarInterface(64, 149);
                sendSidebarInterface(65, 387);
                sendSidebarInterface(66, 541);
                sendSidebarInterface(67, 218);
                sendSidebarInterface(68, 589);
                sendSidebarInterface(69, 429);
                sendSidebarInterface(70, 432);
                sendSidebarInterface(71, 182);
                sendSidebarInterface(72, 261);
                sendSidebarInterface(73, 216);
                sendSidebarInterface(74, 239);

                break;
            case 164:
                sendSidebarInterface(24, 162);
                sendSidebarInterface(21, 160);
//
                sendSidebarInterface(9, 163);
//
                sendSidebarInterface(59, 593);
                sendSidebarInterface(60, 320);
                sendSidebarInterface(61, 399);//274 399
                sendSidebarInterface(62, 149);
                sendSidebarInterface(63, 387);
                sendSidebarInterface(64, 541);
                sendSidebarInterface(65, 218);
                sendSidebarInterface(66, 589);
                sendSidebarInterface(67, 429);
                sendSidebarInterface(68, 432);
                sendSidebarInterface(69, 182);
                sendSidebarInterface(70, 261);
                sendSidebarInterface(71, 216);
                sendSidebarInterface(72, 239);
                break;
            case 548:
                sendSidebarInterface(21, 162);
                sendSidebarInterface(14, 163);
                sendSidebarInterface(9, 160);

                sendSidebarInterface(63, 593);//
                sendSidebarInterface(64, 320);//
                sendSidebarInterface(65, 399);//274 399
                sendSidebarInterface(66, 149);//
                sendSidebarInterface(67, 387);//
                sendSidebarInterface(68, 541);//
                sendSidebarInterface(69, 218);
                sendSidebarInterface(70, 589);//
                sendSidebarInterface(71, 429);//
                sendSidebarInterface(72, 432);//
                sendSidebarInterface(73, 182);//
                sendSidebarInterface(74, 261);//
                sendSidebarInterface(75, 216);
                sendSidebarInterface(76, 239);//
                break;
        }
        return this;
    }

    /**
     * Sends a specific sidebar interface.
     *
     * @param icon        The sidebar icon.
     * @param interfaceId The interface id.
     * @return The action sender instance, for chaining.
     */
    /*
	 * public ActionSender sendSidebarInterface(int tabId, int childId) {
	 * sendInterface(1, 548, tabId, childId); return this; }
	 */
    public ActionSender sendMessage(String message, int type,
                                    boolean extraData, String extra) {
        PacketBuilder bldr = new PacketBuilder(79, Type.VARIABLE)
                .putSmart(type).put((byte) (extraData ? 1 : 0));
        if (extraData)
            bldr.putRS2String(extra);
        bldr.putRS2String(message);
        player.write(bldr.toPacket());
        return this;
    }

    public static void init(Player player) {
        player.getActionSender().sendString(
                378,
                15,
                "OS-Anarchy Staff will NEVER email you. "
                        + "We use the message centre on this website instead.");
        int messages = 0;
        player.getActionSender()
                .sendString(
                        378,
                        16,
                        "You have <col="
                                + (messages > 0 ? "55ff00" : "")
                                + ">"
                                + messages
                                + "<col=ffea00> unread messages in your<br>message centre.");
        player.getActionSender().sendString(378, 12,
                "Welcome to " + Constants.SERVER_NAME);
        
        player.getActionSender()
                .sendString(
                        378,
                        14, //changed
                        player.getRecoveryQuestionsLastSet().equalsIgnoreCase(
                                "never") ? "We are still in beta. Game <col=ff7000>contains bugs <col=ffea00>."
                                + "please do  <col=ff7000>report any bugs found<col=ffea00>on www.forums.os-anarchy.com."
                                : "Recovery Questions Last Set:<br>"
                                + player.getRecoveryQuestionsLastSet());
        player.getActionSender()
                .sendString(378, 18,
                        "You do not have a Bank PIN. Please visit a bank if you would like one.");
        String colourForMembers = "<col=ffea00>";
        if (player.getDaysOfMembership() <= 7) {
            colourForMembers = "<col=ff0000>";
        } else if (player.getDaysOfMembership() >= 20) {
            colourForMembers = "<col=55ff00>";
        }
        player.getActionSender()
                .sendString(
                        378,
                        19,
                        player.isMembers() ? "You have "
                                + colourForMembers
                                + player.getDaysOfMembership()
                                + " <col=ffea00>days of member credit remaining."
                                : "You are not a member. Choose to subscribe and you'll get loads of extra benefits and features.");
        String lastLoggedInPrefix = player.getLastLoggedInDays() + " days ago";
        if (player.getLastLoggedIn() - System.currentTimeMillis() < 0x5265c00L) {
            lastLoggedInPrefix = "earlier today";
        } else if (player.getLastLoggedIn() - System.currentTimeMillis() < (0x5265c00L * 2)) {
            lastLoggedInPrefix = "yesterday";
        }
        if (player.getLastLoggedInFrom().equals("")) {
            player.getActionSender().sendString(
                    378,
                    13,
                    "This is your first time playing OS-Anarchy!"
                            + Constants.SERVER_NAME + "!");
        } else {
            player.getActionSender().sendString(
                    378,
                    13,
                    "You last logged in <col=ff0000>" + lastLoggedInPrefix
                            + " <col=000000>from: "
                            + player.getLastLoggedInFrom());
        }
        //sends data to sql db where php displays it on index
        PlayersOnline.updateWebPCount("login");
        //UPDATE WEBSITE
        player.getActionSender().sendString(50, 3, Constants.WELCOME_SCREEN[Misc.random(Constants.WELCOME_SCREEN.length - 1)]);

    }

    /**
     * Sends a message.
     *
     * @param message The message to send.
     * @return The action sender instance, for chaining.
	 * Known Types; 14 = broadcast, 103 and 104 duel/clan wars?
     */
    public ActionSender sendMessage(String message) {
        return sendMessage(message, 0, false, "");
    }

    public ActionSender sendClanMessage(String message) {
        return sendMessage(message, 11, false, "");
    }

    public ActionSender sendTradeRequest(String message, String user) {
        return sendMessage(message, 101, true, user);
    }

	public ActionSender sendClanRequest(String message, String user) {
		return sendMessage(message, 104, true, user);
	}

	public ActionSender sendDuelRequest(String message, String user) { return sendMessage(message, 103, true, user); }

    /**
     * Sends a debug message.
     *
     * @param message The message to send.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendDebugMessage(String message) {
        return player.isDebugMode() ? sendMessage("<col=ff0000>" + message)
                : this;
    }

    /**
     * Sends a debug message.
     *
     * @param message The message to send.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendDebugPacket(int opCode, String description,
                                        Object[] params) {
        String paramString = "";
        for (Object object : params) {
            paramString += object.toString() + "    ";
        }
        return sendDebugMessage(
                "------------------------------------------------------------------------------------------")
                .sendDebugMessage(
                        "Pkt            " + opCode + "  " + description)
                .sendDebugMessage(
                        "------------------------------------------------------------------------------------------")
                .sendDebugMessage("Params    " + paramString)
                .sendDebugMessage(
                        "------------------------------------------------------------------------------------------");
    }

    public ActionSender sendDynamicRegion(DynamicTileBuilder builder) {
        player.setLastKnownRegion(player.getLocation());
        PacketBuilder pb = new PacketBuilder(117, Type.VARIABLE_SHORT);

        pb.startBitAccess();

        DynamicTile[][][] tiles = builder.getTiles();
        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < tiles[z].length; x++) {
                for (int y = 0; y < tiles[z][x].length; y++) {

                    DynamicTile tile = tiles[z][x][y];

                    if (tile == null) {
                        pb.putBits(1, 0);
                        continue;
                    }

                    pb.putBits(1, 1);
                    pb.putBits(26, tile.hashCode());
                }
            }
        }
        pb.finishBitAccess();

        int[] regions = new int[DynamicTileBuilder.HEIGHT_MAP_SIZE * DynamicTileBuilder.PALETTE_SIZE * DynamicTileBuilder.PALETTE_SIZE];
        int encodedAmount = 0;
        for (int z = 0; z < tiles.length; z++) {
            for (int x = 0; x < tiles[z].length; x++) {
                for (int y = 0; y < tiles[z][x].length; y++) {
                    DynamicTile tile = tiles[z][x][y];

                    if (tile != null) {
                        int hash = tile.hashCode();

                        final int rx = (hash >> 14) & 0x3ff;
                        final int ry = (hash >> 3) & 0x7ff;

                        int region = ((rx >> 3) << 8) + (ry >> 3);

                        for (int i = 0; i < encodedAmount; i++) {
                            if (regions[i] == region) {
                                region = -1;
                                break;
                            }
                        }

                        if (region != -1) {
                            for (int key : tile.getXTEA()) {
                                pb.writeIntV1(key);
                            }
                            regions[encodedAmount++] = region;
                        }
                    }
                }
            }
        }
        pb.putShortA(player.getLocation().getLocalX());
        pb.put((byte) player.getLocation().getZ());
        pb.putShortA(player.getLocation().getRegionX());
        pb.putShortA(player.getLocation().getLocalY());
        pb.putShort(player.getLocation().getRegionY());
        player.getSession().write(pb.toPacket());
        return this;
    }

    /**
     * Sends the map region load command.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendMapRegion() {
        player.setLastKnownRegion(player.getLocation());
        PacketBuilder pb = new PacketBuilder(133, Type.VARIABLE_SHORT);

        boolean forceSend = true;
        if ((((player.getLocation().getRegionX() / 8) == 48) || ((player
                .getLocation().getRegionX() / 8) == 49))
                && ((player.getLocation().getRegionY() / 8) == 48)) {
            forceSend = false;
        }
        if (((player.getLocation().getRegionX() / 8) == 48)
                && ((player.getLocation().getRegionY() / 8) == 148)) {
            forceSend = false;
        }
        pb.putLEShortA(player.getLocation().getLocalX());
        for (int xCalc = (player.getLocation().getRegionX() - 6) / 8; xCalc <= (player
                .getLocation().getRegionX() + 6) / 8; xCalc++) {
            for (int yCalc = (player.getLocation().getRegionY() - 6) / 8; yCalc <= (player
                    .getLocation().getRegionY() + 6) / 8; yCalc++) {
                int region = yCalc + (xCalc << 8);
                final int[] mapData = MapXTEA.getKey(region);
                if (forceSend
                        || ((yCalc != 49) && (yCalc != 149) && (yCalc != 147)
                        && (xCalc != 50) && ((xCalc != 49) || (yCalc != 47)))) {
                    for (int key : mapData)
                        pb.putInt(key);
                }
            }
        }
        pb.putLEShort(player.getLocation().getLocalY());
        pb.putByteC((byte) player.getLocation().getZ());
        pb.putShortA(player.getLocation().getRegionX());
        pb.putLEShortA(player.getLocation().getRegionY());

        player.getSession().write(pb.toPacket());

        Music.playMusic(player);
		hookService.post(new GamePlayerRegionEvent(player));
        WorldMapObjectsLoader.loadMaps(player.getLocation());
        player.getActionSender().sendRemovedObjects();
        // GroundItemController.refresh(player);
        // player.getActionSender().sendGroundItemsInArea();
        return this;
    }

    /**
     * Sends the logout packet.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendLogout() {
        if (player.getCombatState().getLastHitTimer() > System
                .currentTimeMillis()) {
            sendMessage("You can't logout until 10 seconds after the end of combat.");
            return this;
        }
        if (player.getBountyHunter() != null
                && player.getBountyHunter().getLeavePenalty() > 0) {
            sendMessage("You can't logout while on penalty.");
            return this;
        }
        if (player.getInstancedNPCs() != null && player.getInstancedNPCs().size() > 0) {
            player.getInstancedNPCs().clear();
        }
        
        PlayersOnline.updateWebPCount("logout");
        
        
        if(player.getInterfaceState().getClan().length() > 1)
        {
        	 World.getWorld().getPrivateChat().get(player.getInterfaceState().getClan()).removeClanMember(player);
        }
        if (player.getSession() == null) {
            logger.error("Encountered null session while sending logout for player {}!", player.getName());
        } else {
            if (player.getSession().getRemoteAddress() != null) {
                player.setLastLoggedInFrom(player.getSession().getRemoteAddress().toString());
            }
            player.getSession().write(new PacketBuilder(168).toPacket());
        }
        return this;
    }

    /**
     * Disconnect and reconnect instantly.
     *
     * @return
     */
    public ActionSender sendDisconnect() {
        if (player.getCombatState().getLastHitTimer() > System
                .currentTimeMillis()) {
            sendMessage("You can't logout until 10 seconds after the end of combat.");
            return this;
        }
        if (player.getBountyHunter() != null
                && player.getBountyHunter().getLeavePenalty() > 0) {
            sendMessage("You can't logout while on penalty.");
            return this;
        }
        player.getSession().write(new PacketBuilder(244).toPacket());
        return this;
    }

    /**
     * Sends a packet to update a group of items.
     *
     * @param interfaceId The interface id.
     * @param items       The items.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendUpdateItems(int interfaceId, int childId, int type,
                                        Item[] items) {
        PacketBuilder bldr = new PacketBuilder(33, Type.VARIABLE_SHORT);
        bldr.putInt(interfaceId << 16 | childId);
        bldr.putShort(type);
        bldr.putShort(items.length);
        for (Item item : items) {
            if (item != null) {
                int count = item.getCount();
                bldr.putLEShortA(item.getId() + 1);
                if (count > 254) {
                    bldr.putByteA((byte) 255);
                    bldr.putInt(count);
                } else {
                    bldr.putByteA((byte) count);
                }
            } else {
                bldr.putLEShortA(0);
                bldr.putByteA((byte) 0);
            }
        }
        player.getSession().write(bldr.toPacket());
        return this;
    }


    /**
     * Sends a packet to update a single item.
     *
     * @param interfaceId The interface id.
     * @param slot        The slot.
     * @param item        The item.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendUpdateItem(int interfaceId, int childId, int type,
                                       int slot, Item item) {
        PacketBuilder bldr = new PacketBuilder(33, Type.VARIABLE_SHORT);
        bldr.putInt(interfaceId << 16 | childId);
        bldr.putShort(type);
        bldr.putShort(slot);
        if (item != null) {
            bldr.putLEShortA(item.getId() + 1);
            int count = item.getCount();
            if (count > 254) {
                bldr.putByteA((byte) 255);
                bldr.putInt(count);
            } else {
                bldr.putByteA((byte) count);
            }
        } else {
            bldr.putLEShortA(0);
            bldr.putByteA((byte) -1);
        }
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends a packet to update multiple (but not all) items.
     *
     * @param interfaceId The interface id.
     * @param slots       The slots.
     * @param items       The item array.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendUpdateItems(int interfaceId, int childId, int type,
                                        int[] slots, Item[] items) {
        PacketBuilder bldr = new PacketBuilder(33, Type.VARIABLE_SHORT);
        bldr.putInt(interfaceId << 16 | childId);
        bldr.putShort(type);
        for (int slot : slots) {
            Item item = items[slot];
            bldr.putShort(slot);
            if (item != null) {
                bldr.putLEShortA(item.getId() + 1);
                int count = item.getCount();
                if (count > 254) {
                    bldr.putByteA((byte) 255);
                    bldr.putInt(count);
                } else {
                    bldr.putByteA((byte) count);
                }
            } else {
                bldr.putLEShortA(0);
                bldr.putByteA((byte) -1);
            }
        }
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends the enter amount interface.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendEnterAmountInterface() {
        player.getActionSender().sendCS2Script(
                Constants.NUMERICAL_INPUT_INTERFACE,
                new Object[]{"Enter amount:"}, "s");
        return this;
    }

    public ActionSender sendRemoveAmountInterface() {
        player.getActionSender().sendCS2Script(108, new Object[]{}, "");
        return this;
    }

    /**
     * Sends the enter amount interface.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendEnterTextInterface(String question) {
        player.getActionSender().sendCS2Script(
                Constants.ALPHA_NUMERICAL_INPUT_INTERFACE,
                new Object[]{question}, "s");
        return this;
    }

    /**
     * Sends the player an option.
     *
     * @param slot The slot to place the option in the menu.
     * @param top  Flag which indicates the item should be placed at the top.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendInteractionOption(String option, int slot,
                                              boolean top) {
        PacketBuilder bldr = new PacketBuilder(131, Type.VARIABLE);
        bldr.putByteC(top ? (byte) 1 : (byte) 0);
        bldr.putRS2String(option);
        bldr.putByteS((byte) slot);
        player.getSession().write(bldr.toPacket());
        return this;
    }

    /**
     * Sends a string.
     *
     * @param id     The interface id.
     * @param string The string.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendString(int interfaceId, int child, String string) {
        PacketBuilder bldr = new PacketBuilder(157, Type.VARIABLE_SHORT);
        bldr.putInt(interfaceId << 16 | child);
        bldr.putRS2String(string);
        player.write(bldr.toPacket());
        return this;
    }
    
    public ActionSender sendBackwardsString(String string, int interfaceId, int child) {
        return sendString(interfaceId, child, string);
    }

    /**
     * Sends a model in an interface.
     *
     * @param id    The interface id.
     * @param zoom  The zoom.
     * @param model The model id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendInterfaceModel(int id, int zoom, int model) {
        PacketBuilder bldr = new PacketBuilder(246);
        bldr.putLEShort(id).putShort(zoom).putShort(model);
        //player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends a specific skill.
     *
     * @param skill The skill to send.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendPing() {
        PacketBuilder bldr = new PacketBuilder(238, Type.VARIABLE_SHORT);
        bldr.putInt(pingCount++ > 0xF42400 ? pingCount = 1 : pingCount);
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sets a client configuration.
     *
     * @param config The bit config.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendConfig(BitConfig config) {
        sendConfig(config.getId(), config.getValue());
        return this;
    }

    /**
     * Sets a client configuration.
     *
     * @param id    The id.
     * @param value The value.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendConfig(int id, int value) {
        if (value >= 128) {
            PacketBuilder bldr = new PacketBuilder(249);
            bldr.writeIntV1(value);
            bldr.putShortA(id);
            player.getSession().write(bldr.toPacket());
        } else {
            PacketBuilder bldr = new PacketBuilder(56);
            bldr.putLEShort(id);
            bldr.put((byte) value);
            player.getSession().write(bldr.toPacket());
        }
        return this;
    }

    /**
     * Sets a config on an interface.
     *
     * @param interfaceId The interface id.
     * @param childId     The child id.
     * @param hidden      The hidden flag.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendInterfaceConfig(int interfaceId, int childId,
                                            boolean hidden) {
        PacketBuilder bldr = new PacketBuilder(246);
        bldr.putByteA((byte) (hidden ? 1 : 0));
        bldr.putInt2(interfaceId << 16 | childId);
        player.getSession().write(bldr.toPacket());
        return this;
    }

    /**
     * Updates the player's running state.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateRunningConfig() {
        sendConfig(173, player.getWalkingQueue().isRunningToggled() ? 1 : 0);
        // sendInterfaceConfig(261, 51, !player.getWalkingQueue()
        // .isRunningToggled());
        // sendInterfaceConfig(261, 52, player.getWalkingQueue()
        // .isRunningToggled());
        return this;
    }

    public ActionSender sendCreationInterface(int itemId) {
        this.sendDialogue("Man", DialogueType.NPC, 1, FacialAnimation.ANGER_1,
                "Yo whats up!");
        return this;
    }

    /**
     * Sends the player's split private chat flag.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateSplitPrivateChatConfig() {
        sendConfig(287, player.getSettings().splitPrivateChat() ? 1 : 0);
        this.sendCS2Script(83, new Object[0], "");
        return this;
    }

    /**
     * Sends the player's accept aid flag.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateAcceptAidConfig() {
        sendConfig(427, player.getSettings().isAcceptingAid() ? 1 : 0);
        sendInterfaceConfig(261, 59, !player.getSettings().isAcceptingAid());
        sendInterfaceConfig(261, 60, player.getSettings().isAcceptingAid());
        return this;
    }

    /**
     * Sends the player's two mouse buttons flag.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateMouseButtonsConfig() {
        sendConfig(170, player.getSettings().twoMouseButtons() ? 0 : 1);
        sendInterfaceConfig(261, 57, !player.getSettings().twoMouseButtons());
        sendInterfaceConfig(261, 58, player.getSettings().twoMouseButtons());
        return this;
    }

    /**
     * Sends the player's special bar configurations.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateSpecialConfig() {
        sendConfig(300, player.getCombatState().getSpecialEnergy() * 10);
        sendConfig(301, player.getCombatState().isSpecialOn() ? 1 : 0);
        return this;
    }

    /**
     * Sends the player's two mouse buttons flag.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateChatEffectsConfig() {
        sendConfig(171, player.getSettings().chatEffects() ? 0 : 1);
        sendInterfaceConfig(261, 53, !player.getSettings().chatEffects());
        sendInterfaceConfig(261, 54, player.getSettings().chatEffects());
        return this;
    }

    /**
     * Sends the player's banking configs.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateBankConfig() {
		variableService.set(player, NOTE_VARIABLE, player.getSettings().isWithdrawingAsNotes() ? 1 : 0);
		variableService.set(player, BANK_TAB_VARIABLE, player.getBanking().getCurrentTab());
		variableService.set(player, SWAP_VARIABLE, player.getSettings().isSwapping() ? 0 : 1);
		variableService.set(player, LAST_WITHDRAW_VARIABLE, player.getSettings().getLastWithdrawnValue());

		variableService.send(player, NOTE_VARIABLE);
		variableService.send(player, SWAP_VARIABLE);
//		sendConfig(304, (player.getSettings().getLastWithdrawnValue() * 2) | (player.getSettings().isSwapping() ? 0 : 1));
//        sendConfig(115, player.getSettings().isWithdrawingAsNotes() ? 1 : (player.getBanking().getCurrentTab() * 4));
//		sendConfig(304, player.getSettings().getLastWithdrawnValue());
        return this;
    }

    /**
     * Sends the player's auto retaliate config.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender updateAutoRetaliateConfig() {
        return sendConfig(172, player.getSettings().isAutoRetaliating() ? 0 : 1);
    }

    /**
     * Sends your location to the client.
     *
     * @param location The location.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendArea(Location location, int xOffset, int yOffset) {
		/*
		 * PacketBuilder bldr = new PacketBuilder(52); int localX =
		 * location.getRegionX() - (player.getLastKnownRegion().getRegionX() -
		 * 6) * 8; int localY = location.getRegionY() -
		 * (player.getLastKnownRegion().getRegionY() - 6) * 8; bldr.put((byte)
		 * ((localX + xOffset))); bldr.putByteC((byte) (localY + yOffset));
		 * //bldr.put((byte) (location.getZ())); player.write(bldr.toPacket());
		 */
        return this;
    }

	public ActionSender sendGroundItem(GroundItemService.GroundItem item) {
		if (item == null || item.getItem().getId() < 1
				|| item.getLocation().getX() < 1
				|| item.getLocation().getY() < 1
				|| item.getItem().getCount() < 1 || item.getLocation().getZ() != player.getLocation().getZ()) {
			return this;
		}
		sendLocalCoordinates(item.getLocation(), 0, 0);
		PacketBuilder bldr = new PacketBuilder(205);
		bldr.put(((byte) 0));
		bldr.putShort(item.getItem().getCount());
		bldr.putLEShortA(item.getItem().getId());
		player.write(bldr.toPacket());
		return this;
	}

	public ActionSender removeGroundItem(GroundItemService.GroundItem item) {
		if (item == null || item.getItem().getId() < 1
				|| item.getLocation().getX() < 1
				|| item.getLocation().getY() < 1
				|| item.getItem().getCount() < 1 || item.getLocation().getZ() != player.getLocation().getZ()) {
			return this;
		}
		sendLocalCoordinates(item.getLocation(), 0, 0);
		PacketBuilder bldr = new PacketBuilder(83);
		bldr.putByteC((byte) 0);
		bldr.putLEShort(item.getItem().getId());
		player.write(bldr.toPacket());
		return this;
	}

    /**
     * Shows an item on the ground.
     *
     * @param item The ground item.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendGroundItem(GroundItem item) {
        if (item == null || item.getItem().getId() < 1
                || item.getLocation().getX() < 1
                || item.getLocation().getY() < 1
                || item.getItem().getCount() < 1 || item.getLocation().getZ() != player.getLocation().getZ()) {
            return this;
        }
        if (item.getItem().getId() == 2412 || item.getItem().getId() == 2413 || item.getItem().getId() == 2414) {
            player.setAttribute("droppedGodCape", true);
        }
        System.out.println("gets here");
        item.setSpawned(true);
        sendLocalCoordinates(item.getLocation(), 0, 0);
        PacketBuilder bldr = new PacketBuilder(205);
        bldr.put(((byte) 0));
        bldr.putShort(item.getItem().getCount());
        bldr.putLEShortA(item.getItem().getId());
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender sendGroundItem2(GroundItemDefinition item) {
        if (item == null || item.getId() < 1
                || item.getLocation().getX() < 1
                || item.getLocation().getY() < 1
                || item.getCount() < 1 || item.getLocation().getZ() != player.getLocation().getZ()) {
            return this;
        }
        if (item.getId() == 2412 || item.getId() == 2413 || item.getId() == 2414) {
            player.setAttribute("droppedGodCape", true);
        }
        sendLocalCoordinates(item.getLocation(), 0, 0);
        PacketBuilder bldr = new PacketBuilder(205);
        bldr.put(((byte) 0));
        bldr.putShort(item.getCount());
        bldr.putLEShortA(item.getId());
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Shows an item on the ground.
     *
     * @param item The ground item.
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeGroundItem(GroundItem item) {
        if (item == null || item.getItem().getId() < 1
                || item.getLocation().getX() < 1
                || item.getLocation().getY() < 1
                || item.getItem().getCount() < 1 || item.getLocation().getZ() != player.getLocation().getZ()) {
            return this;
        }
        if (item.getItem().getId() == 2412 || item.getItem().getId() == 2413 || item.getItem().getId() == 2414) {
            player.removeAttribute("droppedGodCape");
        }
        sendLocalCoordinates(item.getLocation(), 0, 0);
        PacketBuilder bldr = new PacketBuilder(83);
        bldr.putByteC((byte) 0);
        bldr.putLEShort(item.getItem().getId());
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender removeGroundItem2(GroundItemDefinition item) {
        if (item == null || item.getId() < 1
                || item.getLocation().getX() < 1
                || item.getLocation().getY() < 1
                || item.getCount() < 1 || item.getLocation().getZ() != player.getLocation().getZ()) {
            return this;
        }
        if (item.getId() == 2412 || item.getId() == 2413 || item.getId() == 2414) {
            player.removeAttribute("droppedGodCape");
        }
        sendLocalCoordinates(item.getLocation(), 0, 0);
        PacketBuilder bldr = new PacketBuilder(83);
        bldr.putByteC((byte) 0);
        bldr.putLEShort(item.getId());
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends all the ground items in a players area.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendGroundItemsInArea() {
        // TODO check distance!!
        for (Region r : World.getWorld().getRegionManager()
                .getSurroundingRegions(player.getLocation())) {
            for (GroundItem item : r.getGroundItems()) {
               // if (/*item.getItem().getDefinition().isTradable() && */item.isOwnedBy(player.getName())) {
                    sendGroundItem(item);
              //  }
            }
        }
        return this;
    }

    public ActionSender removeInventoryInterface() {
        int tab = (int) player.getAttribute("tabmode") == 164 ? 57 : (int) player.getAttribute("tabmode") == 161 ? 59 : 61;
		player.getInterfaceState().inventoryInterfaceClosed();
        player.getActionSender().removeInterfaces(player.getAttribute("tabmode"), tab);
        return this;
    }

    /**
     * Sends all the game objects in a players area.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendGameObjectsInArea() {
        // TODO check distance!!
        Region[] regions = World.getWorld().getRegionManager()
                .getSurroundingRegions(player.getLocation());
        for (Region r : regions) {
            for (GameObject obj : r.getGameObjects()) {
                if (obj.getLocation().distance(player.getLocation()) <= RegionManager.REGION_SIZE && !obj.isLoadedInLandscape()) {
                    sendObject(obj);
                }
            }
        }
        return this;
    }

    /**
     * Sends the players bonuses to the client.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendBonuses() {

        int id = 23;
        for (int index = 0; index < player.getCombatState().getBonuses().length; index++) {
            if (id == 28 || id == 34)
                id++;
            String bonus = (Constants.BONUSES[index] + ": "
                    + (player.getCombatState().getBonus(index) >= 0 ? "+" : "") + player
                    .getCombatState().getBonus(index)); // bonus = "" + id + "/"
            // + index;
            sendString(84, id++, bonus);
        }
        sendString(84, 36, Constants.BONUSES[11] + ": +"
                + player.getCombatState().getBonus(12));
        sendString(84, 38, Constants.BONUSES[13] + ": +"
                + player.getCombatState().getBonus(11));
        sendString(84, 37, Constants.BONUSES[12] + ": 0");
        sendString(84, 40, Constants.BONUSES[14] + ": 0");
        sendString(84, 41, Constants.BONUSES[15] + ": 0");
        return this;
    }

    /**
     * Removes the chatbox interface.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeChatboxInterface() {
        removeInterfaces(162, 546);
        player.getInterfaceState().setChatboxInterface(-1);
		if (player.getDialogueChain() != null) {
			player.getDialogueChain().fireOnClose(player);
			player.setDialogueChain(null);
		}
        return this;
    }

    public ActionSender sendChatboxInterface(int inventoryInterfaceId) {
        sendInterface(162, 546, inventoryInterfaceId, true);
        player.getInterfaceState().setChatboxInterface(inventoryInterfaceId);
        return this;
    }

    public ActionSender sendBoltEnchantInterface() {
        sendCS2Script(917, new Object[]{-1, -1}, "ii");
        sendInterface(80, false);
        return this;
    }

    /**
     * Removes the side tab interface.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeSidetabInterface() {
        if (player.getInterfaceState().hasSideTabInterfaceOpen()) {
            player.getInterfaceState().setSidetabInterface(-1);
            // player.write(new PacketBuilder(167).putInt(Constants.MAIN_WINDOW
            // << 16 | Constants.SIDE_TABS).toPacket()); //tabs screen
        }
        return this;
    }

    /**
     * Removes the side tab interface.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeInterface() {
        if (player.getInterfaceState().isWalkableInterface() && player.getInterfaceState().getCurrentInterface() == -1) {
            return this;
        }
        if (player.hasAttribute("viewingSkill")) {
            player.removeAttribute("viewingSkill");
        }
        player.getInterfaceState().interfaceClosed();
        int child = (int) player.getAttribute("tabmode") == 548 ? 19 : 12;
        player.write(new PacketBuilder(65).putInt(
                (int) player.getAttribute("tabmode") << 16 | child).toPacket());
        return this;
    }

    public ActionSender removeInterface(int child) {
        if (player.getInterfaceState().isWalkableInterface()) {
            return this;
        }
        if (player.hasAttribute("viewingSkill")) {
            player.removeAttribute("viewingSkill");
        }
        player.getInterfaceState().interfaceClosed();
        player.write(new PacketBuilder(65).putInt(
                (int) player.getAttribute("tabmode") << 16 | child).toPacket());
        return this;
    }

    public ActionSender removeInterface2() {
        //		if (player.getInterfaceState().isWalkableInterface()) {
        //			return this;
        //		}
        //		player.getInterfaceState().interfaceClosed(); // 96
        //		int child = (int) player.getAttribute("tabmode") == 548 ? 14 : 6;
        //		player.write(new PacketBuilder(65).putInt(
        //				(int) player.getAttribute("tabmode") << 16 | child).toPacket()); // main game
        //		// screen
        return this;
    }

    /**
     * Removes an open interface.
     *
     * @param id    The interface id.
     * @param child The interface child id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeAllInterfaces() {
        sendInterfacesRemovedClientSide();
        // removeSidetabInterface();
        if (player.getInterfaceState().getCurrentInterface() == 660) {
            player.getAppearance().setLook(
                    (int[]) player.getInterfaceAttribute("oldAppearance"));
        }
        if (player.getInterfaceState().getCurrentInterface() != -1) {
            removeInterface();
            //removeInterface2();
        }
		if (player.getInterfaceState().getCurrentInventoryInterface() != -1) {
			removeInventoryInterface();
		}

        if (player.getInterfaceState().hasChatboxInterfaceOpen())
            removeChatboxInterface();

        return this;
    }

    /**
     * Removes an open interface.
     *
     * @param id    The interface id.
     * @param child The interface child id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeInterfaces(int id, int child) {
        if (id == Constants.MAIN_WINDOW && child == Constants.GAME_WINDOW) {
            sendInterfacesRemovedClientSide();
        }
        player.write(new PacketBuilder(65).putInt(id << 16 | child).toPacket());
        return this;
    }

    /**
     * Sends a clientscript to the client.
     *
     * @param id     The id.
     * @param params Any parameters in the scrips.
     * @param types  The script types
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendCS2Script(int id, Object[] params, String types) {
        PacketBuilder bldr = new PacketBuilder(167, Type.VARIABLE_SHORT);
        bldr.putRS2String(types);
        if (params.length > 0) {
            int j = 0;
            for (int i = types.length() - 1; i >= 0; i--, j++) {// it reverses it in packet lol?
                if (types.charAt(i) == 's') {
                    bldr.putRS2String((String) params[j]);
                } else {
                    bldr.putInt((Integer) params[j]); //
                }
            }
        }
        bldr.putInt(id);// done ok how can we dump the params for that? good
        // question rip
        player.getSession().write(bldr.toPacket());
        return this;
    }
    
//    public ActionSender sendExecuteScript(int id, Object[] params, String types) {
//        PacketBuilder bldr = new PacketBuilder(167, Type.VARIABLE_SHORT);
//        bldr.putRS2String(types);
//        List<Object> l = Arrays.asList(params);
//		Collections.reverse(l);
////        if (params.length > 0) {
////            int j = 0;
////            for (int i = types.length() - 1; i >= 0; i--, j++) {// it reverses it in packet lol?
////                if (types.charAt(i) == 's') {
////                    bldr.putRS2String((String) params[j]);
////                } else {
////                    bldr.putInt((Integer) params[j]);
////                }
////            }
////        }
//        bldr.putInt(id);// done ok how can we dump the params for that? good
//        // question rip
//        player.getSession().write(bldr.toPacket());
//        return this;
//    }

    /**
     * Sends an access mask to the client.
     *
     * @param set         The set.
     * @param interfaceId The interface id.
     * @param child       The window
     * @param offset      The offset.
     * @param length      The length.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendAccessMask(int set, int interfaceId, int child, int offset, int length) {
        PacketBuilder bldr = new PacketBuilder(196);
        bldr.putLEShortA(offset).putShort(length).putInt2(set) //1 second... thinking 
                .writeIntV1(interfaceId << 16 | child);
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender sendGEAccess(int offset, int length, int interfaceId, int child, int set) {
        return sendAccessMask(set, interfaceId, child, offset, length);
    }

    public ActionSender sendAccessMask(int a1, int a2, int a3, int a4) {
        PacketBuilder bldr = new PacketBuilder(196);
        bldr.putLEShortA(a1).putShort(a2).putInt2(a3).writeIntV1(a4);
		//a1 = offset, a2 = length, a3 = set, a4 = interfaceId << 16 | child;
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender format(int offset, int length, int set,
                               int interfaceId, int window) {
        return sendAccessMask(set, interfaceId, window, offset, length);
    }

    /**
     * Interfaces are removed clientside (do not send any data to the client or
     * this will cause a bug with opening interfaces such as Report Abuse).
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendInterfacesRemovedClientSide() {
        if (player.getRequestManager().getAcquaintance() != null) {
            if (player.getRequestManager().getState() != RequestState.COMMENCING
                    && player.getRequestManager().getAcquaintance()
                    .getRequestManager().getState() != RequestState.COMMENCING
                    && player.getRequestManager().getState() != RequestState.ACTIVE
                    && player.getRequestManager().getAcquaintance()
                    .getRequestManager().getState() != RequestState.ACTIVE
                    && player.getRequestManager().getState() != RequestState.FINISHED
                    && player.getRequestManager().getAcquaintance()
                    .getRequestManager().getState() != RequestState.FINISHED) {
                player.getRequestManager().cancelRequest();
            } else if (player.getRequestManager().getState() == RequestState.FINISHED) {
                // if(player.getStake().size() > 0) {
                // for(Item item : player.getStake().getContents()) {
                // if(item != null) {
                // player.getInventory().add(item);
                // }
                // }
                // player.getStake().clear();
                // }
                // player.getRequestManager().getAcquaintance().getRequestManager().setRequestType(null);
                // player.getRequestManager().getAcquaintance().getRequestManager().setAcquaintance(null);
                // player.getRequestManager().getAcquaintance().getRequestManager().setState(RequestState.NORMAL);
                // player.getRequestManager().setRequestType(null);
                // player.getRequestManager().setAcquaintance(null);
                // player.getRequestManager().setState(RequestState.NORMAL);
            }
        }
        if (player.getInterfaceState().isWalkableInterface()) {
            return this;
        }
        sendAreaInterface(null, player.getLocation());
        return this;
    }

    /**
     * Creates a game object on the players screen.
     *
     * @param obj The object to create.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendObject(GameObject obj) {// show me in client
        if (obj.getId() == -1) {
            return removeObject(obj);
        }
        if (player.getLocation().getZ() != obj.getLocation().getZ()) {
            return this;
        }
        int localX = obj.getLocation().getX()
                - (player.getLastKnownRegion().getRegionX() - 6) * 8;
        int localY = obj.getLocation().getY()
                - (player.getLastKnownRegion().getRegionY() - 6) * 8;
        PacketBuilder bldr = new PacketBuilder(229, Packet.Type.VARIABLE_SHORT);//now we gotta implement this packet
        bldr.put((byte) ((localX)));
        bldr.putByteS((byte) (localY));
        bldr.put((byte) 215); // subpacket id
        bldr.putShort(obj.getId());
        bldr.putByteS(((byte) 0));//((obj.getLocation().getX() - player.getLocation().getX()) << 4 | ((obj.getLocation().getY() - player.getLocation().getY()) & 0x7))));
        bldr.putByteA((byte) ((obj.getType() << 2) | (obj.getDirection() & 3)));
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Removes a game object on a players screen.
     *
     * @param obj The object to remove.
     * @return The action sender instance, for chaining.
     */
    public ActionSender removeObject(GameObject obj) {
        if (player.getLocation().getZ() != obj.getLocation().getZ()) {
            return this;
        }
        int localX = obj.getLocation().getX()
                - (player.getLastKnownRegion().getRegionX() - 6) * 8;
        int localY = obj.getLocation().getY()
                - (player.getLastKnownRegion().getRegionY() - 6) * 8;
        PacketBuilder bldr = new PacketBuilder(229, Packet.Type.VARIABLE_SHORT);
        bldr.put((byte) ((localX)));
        bldr.putByteS((byte) (localY));
        bldr.put((byte) 241); // subpacket id
        int ot = ((obj.getType() << 2) + (obj.getDirection() & 3));
        bldr.put((byte) 0);
        bldr.put((byte) ot);
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Animates an object.
     *
     * @param obj The object.
     * @return The action sender instance, for chaining.
     */
    public ActionSender animateObject(GameObject obj, int animationId) {
        //		if (player.getLocation().getZ() != obj.getLocation().getZ()) {
        //			return this;
        //		}
        sendLocalCoordinates(obj.getLocation(), 0, 0);
        PacketBuilder spb = new PacketBuilder(25);
        int ot = ((obj.getType() << 2) + (obj.getDirection() & 3));
        spb.putByteC((byte) 0);
        spb.putShort(animationId);
        spb.putByteS((byte) ot);
        //		spb.putByteA((byte) ot);
        //		spb.put((byte) 0);
        //		spb.putShort(animationId);
        player.write(spb.toPacket());
        return this;
    }

    /**
     * Sends an animation of an interface.
     *
     * @param emoteId     The emote id.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendInterfaceAnimation(int emoteId, int interfaceId,
                                               int childId) {
        PacketBuilder bldr = new PacketBuilder(192);
        bldr.putShort(emoteId);
        bldr.putInt(interfaceId << 16 | childId);
        player.write(bldr.toPacket());
        // 248 is another one
		/*
		 * PacketBuilder bldr = new PacketBuilder(197);
		 * bldr.putLEShort(emoteId); bldr.putLEInt(interfaceId << 16 | childId);
		 * player.write(bldr.toPacket());* not sure ???/ /* PacketBuilder bldr
		 * = new PacketBuilder(67); MODEL I THINK bldr.putLEShortA(itemId);
		 * bldr.putLEInt(interfaceId << 16 | childId);
		 * player.write(bldr.toPacket());
		 */
        return this;
    }

    /**
     * Sends the player's head onto an interface.
     *
     * @param interfaceId The interface id.
     * @param childId     The child id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendPlayerHead(int interfaceId, int childId) {
        player.getSession().write(
                new PacketBuilder(31).writeIntV1(interfaceId << 16 | childId)
                        .toPacket());
        return this;
    }

    public ActionSender sendRemovedObjects() {
        for (GameObject obj : World.getWorld().getRemovedObjects()) {
            player.getActionSender().removeObject(obj);
        }
        return this;
    }

    /**
     * Sends an NPC's head onto an interface.
     *
     * @param npcId       The NPC's id.
     * @param interfaceId The interface id.
     * @param childId     The child id.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendNPCHead(int npcId, int interfaceId, int childId) {
        player.write(new PacketBuilder(10).putLEShortA(npcId)
                .putInt(interfaceId << 16 | childId).toPacket());
        return this;
    }

    public ActionSender sendSong(int song) {
//        player.write(new PacketBuilder(88).putTriByte(0).putLEShort(song).toPacket());
        player.write(new PacketBuilder(160).putLEShortA(song).toPacket());
        return this;
    }

	public ActionSender sendProjectile(Projectile projectile) {
		return sendProjectile(projectile.getStart(), projectile.getFinish(), projectile.getId(), projectile.getDelay(), projectile.getAngle(), projectile.getSpeed(), projectile.getStartHeight(), projectile.getEndHeight(), projectile.getLockon(), projectile.getSlope(), projectile.getRadius());
	}

	public enum DialogueType {
		NPC, PLAYER, OPTION, MESSAGE_DMM, MESSAGE, MESSAGE_MODEL_LEFT, AGILITY_LEVEL_UP, ATTACK_LEVEL_UP, COOKING_LEVEL_UP, CRAFTING_LEVEL_UP, DEFENCE_LEVEL_UP, FARMING_LEVEL_UP, FIREMAKING_LEVEL_UP, FISHING_LEVEL_UP, FLETCHING_LEVEL_UP, HERBLORE_LEVEL_UP, HITPOINT_LEVEL_UP, MAGIC_LEVEL_UP, MINING_LEVEL_UP, PRAYER_LEVEL_UP, RANGING_LEVEL_UP, RUNECRAFTING_LEVEL_UP, SLAYER_LEVEL_UP, SMITHING_LEVEL_UP, STRENGTH_LEVEL_UP, THIEVING_LEVEL_UP, BANK_PIN_ERROR, WOODCUTTING_LEVEL_UP, 
		TELEPORT_INTERFACE, WORLD_SWITCHER
    }

    public ActionSender sendDialogue(String title, DialogueType dialogueType,
                                     int entityId, FacialAnimation animation, String... text) {
        int interfaceId = -1;
        switch (dialogueType) {
            case NPC:
                if (text.length > 4 || text.length < 1) {
                    return this;
                }
                interfaceId = 231;
                //System.out.println("Interface: " + interfaceId);
                sendInterfaceAnimation(animation.getAnimation().getId(),
						interfaceId, 0);
                sendString(interfaceId, 1, title);
                sendString(interfaceId, 3, text[0]);
                sendString(231, 2, "Click here to continue.");
                //sendChatboxInterface(interfaceId);
                sendInterface(162, 546, interfaceId, false);
                sendRandom(interfaceId, 0, 554);
                sendCS2Script(600, new Object[]{15138819, 16, 1, 1}, "iiiI");
                player.sendAccess(Access.of(interfaceId, 2, AccessBits.CLICK_CONTINUE));
               //sendAccessMask(1, interfaceId, 2, -1, -1);
                sendNPCHead(entityId, interfaceId, 0);
                break;
            case PLAYER:
                if (text.length > 4 || text.length < 1) {
                    return this;
                }
                interfaceId = 217;
                sendPlayerHead(interfaceId, 0);
                sendInterfaceAnimation(animation.getAnimation().getId(),
                        interfaceId, 0);
                sendString(interfaceId, 1, title);
                sendString(217, 3, text[0]);
                sendString(217, 2, "Click here to continue.");
                sendInterface(162, 546, interfaceId, false);
                sendCS2Script(600, new Object[]{14221315, 16, 1, 1}, "iiiI");
//                sendAccessMask(1, interfaceId, 2, -1, -1);
                player.sendAccess(Access.of(interfaceId, 2, AccessBits.CLICK_CONTINUE));
                break;
        	case TELEPORT_INTERFACE:
    			sendInterface(187, false);
    			//sendString(187, 3, text[0]);
    			sendCS2Script(217, new Object[] {text[0], title, /*Keyboard toggle*/ 0}, "Iss");
    			player.sendAccess(Access.of(187, 3, NumberRange.of(0, 5), AccessBits.CLICK_CONTINUE));
        		break;
        	case WORLD_SWITCHER:
        		int pane = player.getAttribute("tabmode");
            	int tabId = pane == 548 ? 73 : pane == 161 ? 73 : 73;
                sendSidebarInterface(tabId, 69);
//            	sendAccessMask(1, 69, 7, 0, 419);// 17
//            	sendAccessMask(0, 69, 14, 0, 420);
				sendConfig(477, 0);
				//sendCS2Script(747, new Object[] {}, "");
				//sendCS2Script(748, new Object[] {}, "");
//				sendCS2Script(764, new Object[] { 0, 19268, "Vernox Economy" }, "sIi");
//				sendCS2Script(764, new Object[] { 1, 19335,	"Zenyte OS" }, "sIi");
    			player.sendAccess(Access.of(69, 7, NumberRange.of(0, 419), AccessBits.WORLDSWITCH));
    			player.sendAccess(Access.of(69, 14, NumberRange.of(0, 420), AccessBits.WORLDSWITCH));
        		break;
            case OPTION:
                interfaceId = 219;
                sendString(interfaceId, 0, title);
                sendCS2Script(58, new Object[]{text[0], "Select an Option"}, "ss");
                sendInterface(162, 546, interfaceId, false);
                String[] textLength = text[0].split("|");
//                sendAccessMask(1, interfaceId, 0, 1, textLength.length);
                player.sendAccess(Access.of(interfaceId, 0, NumberRange.of(1, textLength.length), AccessBits.CLICK_CONTINUE));
                break;
            case MESSAGE_DMM:
                interfaceId = 229;


                sendCS2Script(600, new Object[]{15007744, 0, 1, 1}, "iiiI");
//                sendAccessMask(1, 229, 1, -1, -1);
                player.sendAccess(Access.of(interfaceId, 1, AccessBits.CLICK_CONTINUE));
                sendString(interfaceId, 0, text[0]);
                sendString(interfaceId, 1, "Click here to continue.");
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
				sendInterface(162, 546, interfaceId, false);
                break;
            case MESSAGE:
                interfaceId = 229;


                sendCS2Script(600, new Object[]{15007744, 0, 1, 1}, "iiiI");
//                sendAccessMask(1, 229, 1, -1, -1);
                player.sendAccess(Access.of(interfaceId, 1, AccessBits.CLICK_CONTINUE));
                sendString(interfaceId, 0, text[0]);
                sendString(interfaceId, 1, "Click here to continue.");
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
				sendInterface(162, 546, interfaceId, false);
                break;
            case MESSAGE_MODEL_LEFT:
			/*
			 * Window Id: 162, Child: 546, Interface Id: 193, Walkable: 0
Offset: -1, Length: -1, Set: 1, Interface: 193, Child: 2
Offset: -1, Length: -1, Set: 0, Interface: 193, Child: 3
Offset: -1, Length: -1, Set: 0, Interface: 193, Child: 4
			 */
                interfaceId = 193;

                sendInterface(162, 546, interfaceId, false);
                sendAccessMask(1, 193, 2, -1, -1);
                sendAccessMask(0, 193, 3, -1, -1);
                sendAccessMask(0, 193, 4, -1, -1);

                sendString(interfaceId, 1, text[0]);
                sendString(interfaceId, 2, "Click here to continue.");
                sendItemOnInterface(193, 0, entityId, 150);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case AGILITY_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 3, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case ATTACK_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 5, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case COOKING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 11, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case CRAFTING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 13, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case DEFENCE_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 16, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case FARMING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 18, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case FIREMAKING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 20, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case FISHING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 22, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case FLETCHING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 24, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case HERBLORE_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 27, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case HITPOINT_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 29, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case MAGIC_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 31, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case MINING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 33, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case PRAYER_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 35, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case RANGING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 37, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case RUNECRAFTING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 40, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case SLAYER_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 42, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case SMITHING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 44, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case STRENGTH_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 46, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case THIEVING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 48, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
            case WOODCUTTING_LEVEL_UP:
                sendInterface(162, 546, 233, false);
                sendString(233, 0, text[0]);
                sendString(233, 1, text[1]);
                sendInterfaceConfig(233, 50, false);
                player.setInterfaceAttribute("message_dialogue", Boolean.TRUE);
                break;
        }
        return this;
    }

    int[] skills = {3, 5, 8, 11, 13, 16, 18, 20, 22, 24, 27, 29, 31, 33, 35, 37, 40, 42, 44, 46, 48, 50};

    public ActionSender resetSkillInterface() {
        for (int i = 0; i < skills.length; i++) {
            sendInterfaceConfig(233, skills[i], true);
        }
        return this;
    }

    /**
     * Sends a projectile to a location.
     *
     * @param start       The starting location.
     * @param finish      The finishing location.
     * @param id          The graphic id.
     * @param delay       The delay before showing the projectile.
     * @param angle       The angle the projectile is coming from.
     * @param speed       The speed the projectile travels at.
     * @param startHeight The starting height of the projectile.
     * @param endHeight   The ending height of the projectile.
     * @param lockon      The lockon index of the projectile, so it follows them if they
     *                    move.
     * @param slope       The slope at which the projectile moves.
     * @param radius      The radius from the centre of the tile to display the
     *                    projectile from.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendProjectile(Location start, Location to, int id,
                                       int delay, int angle, int speed, int startHeight, int endHeight,
                                       int slope, int radius, int lockon) {
        int offsetX = to.getX() - start.getX();
        int offsetY = to.getY() - start.getY();

        sendLocalCoordinates(start, 0, 0);
        PacketBuilder bldr = new PacketBuilder(49);
        bldr.put((byte) 0);
        bldr.put((byte) offsetX);
        bldr.put((byte) offsetY);
        bldr.putShort(lockon);
        bldr.putShort(id);
        bldr.put((byte) (startHeight));
        bldr.put((byte) (endHeight));
        bldr.putShort(delay);
        bldr.putShort(speed);
        bldr.put((byte) slope);
        bldr.put((byte) radius);

        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends the hint arrow ontop of an entity
     *
     * @param entity   The entity.
     * @param height   The height of the arrow.
     * @param position The position on the tile of the arrow (2 = middle, 3 = west, 4
     *                 = east, 5 = south, 6 = north).
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendHintArrow(Entity entity, int height, int position) {
		PacketBuilder bldr = new PacketBuilder(48);
		if(entity.isNPC() || entity.isPlayer()) {
			bldr.put((byte) (entity.isNPC() ? 1 : 10));
			bldr.putShort(entity.getClientIndex());
			bldr.putShort((byte) 0);
			bldr.put((byte) 0);
			bldr.put((byte) 0);
		} else if(entity.isObject()) {
			bldr.put((byte) position);
			bldr.putShort(entity.getLocation().getX());
			bldr.putShort(entity.getLocation().getY());
			bldr.put((byte) height);
		}
		player.write(bldr.toPacket());
		return this;
    }

	public ActionSender sendEntityHint(Player player) {
		PacketBuilder bldr = new PacketBuilder(137);
		bldr.put((byte) 10);//10 for Player's.
		bldr.putShort(player.getClientIndex());
		bldr.putShort(player.getClientIndex());
		player.write(bldr.toPacket());
		return this;
	}

	public ActionSender sendSetTargetEntity(Mob entity) {
		PacketBuilder bldr = new PacketBuilder(137);
		if(entity instanceof Player) {
			bldr.put((byte) 10);
		} else if(entity instanceof NPC) {
			bldr.put((byte) 1);
		}
		bldr.putShort(entity.getIndex());
		for(int i = 0; i < 3; i++) {
			bldr.put((byte) 0);
		}
		player.write(bldr.toPacket());
		return this;
	}

	public ActionSender resetTargetEntity() {
		PacketBuilder bldr = new PacketBuilder(137);
		bldr.put((byte) 1);
		bldr.putShort(-1);
		for(int i = 0; i < 3; i++) {
			bldr.put((byte) 0);
		}
		player.write(bldr.toPacket());
		return this;
	}

	public ActionSender sendHintAtLocation(Location location, int orientation) {// its not objects
		PacketBuilder bldr = new PacketBuilder(137);

		bldr.put((byte) orientation);
		bldr.putShort(location.getX());
		bldr.putShort(location.getY());
		bldr.put((byte) location.getZ());
		player.write(bldr.toPacket());
		return this;
	}

    /**
     * Gradually moves the camera to a specified location;
     *
     * @param location      The new camera location.
     * @param zPos          The new cameraHeight, in positions.
     * @param constantSpeed The constant linear camera movement speed as positions per cycle.
     * @param variableSpeed The variable linear camera movement speed as promile of what's left. Max 99, 100 is instant.
     * @return The action sender instance, for chaining.
     */
    public ActionSender moveCameraToLocation(Location loc, int zPos, int constantSpeed, int variableSpeed) {
        PacketBuilder bldr = new PacketBuilder(113);
        bldr.put((byte) (loc.getLocalX(player.getLocation())));
        bldr.put((byte) (loc.getLocalY(player.getLocation())));
        bldr.putShort(zPos);
        bldr.put((byte) constantSpeed);
        bldr.put((byte) variableSpeed);
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Instantly moves the camera to a specified location;
     *
     * @param location The new camera location.
     * @param zPos     The new cameraHeight, in positions.
     * @return The action sender instance, for chaining.
     */
    public ActionSender moveCameraToLocation(Location location, int zPos) {
        return moveCameraToLocation(location, zPos, 0, 100);
    }

    /**
     * Instantly turns the camera to look at a specified location;
     *
     * @param location The new camera location.
     * @param zPos     The new cameraHeight, in positions.
     * @return The action sender instance, for chaining.
     */
    public ActionSender turnCameraToLocation(Location location, int zPos) {
        return turnCameraToLocation(location, zPos, 0, 100);
    }

    /**
     * Gradually turns the camera to look at a specified location;
     *
     * @param location      The new camera location.
     * @param zPos          The new cameraHeight, in positions.
     * @param constantSpeed The constant linear camera movement speed as angular units per cycle.
     *                      In the XY angle there are 2048 units in 360 degrees.
     *                      In the Z angle is restricted between 128 and 383, which are the min and max ZAngle.
     * @param variableSpeed The variable linear camera movement speed as promile of what's left. Max 99, 100 is instant.
     * @return The action sender instance, for chaining.
     */
    public ActionSender turnCameraToLocation(Location location, int zPos, int constantSpeed, int variableSpeed) {
        PacketBuilder bldr = new PacketBuilder(186);
        bldr.put((byte) (location.getLocalX(player.getLocation())));
        bldr.put((byte) (location.getLocalY(player.getLocation())));
        bldr.putShort(zPos);
        bldr.put((byte) constantSpeed);
        bldr.put((byte) variableSpeed);
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Stops all cinematics (including earthquakes)
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender stopCinematics() {
        player.write(new PacketBuilder(147).toPacket());
        return this;
    }

    /**
     * Sends a sound to the client.
     *
     * @param sound The sound to play.
     * @return The action sender instance, for chaining.
     */
	public ActionSender playSound(Sound sound) {
		PacketBuilder bldr = new PacketBuilder(38);//243  136
		bldr.putShort(sound.getId()).put((byte) sound.getVolume()).putShort(sound.getDelay());
		player.write(bldr.toPacket());
		return this;
	}

    /**
     * Sends to the client that this player has sent a private message.
     *
     * @param nameAsLong The recepient's name, as a long.
     * @param unpacked   The unpacked message.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendSentPrivateMessage(long nameAsLong, String message) {
        byte[] bytes = new byte[256];
        int length = TextUtils.encryptPlayerChat(bytes, 0, 0, message.length(),
                message.getBytes());
        player.getSession().write(
                new PacketBuilder(35, Type.VARIABLE_SHORT)
                        .putRS2String(NameUtils.longToName(nameAsLong))
                        .putSmart(message.length()).put(bytes, 0, length)
                        .toPacket());
        return this;
    }

	public ActionSender sendSentPrivateMessage(String name, String message) {
		byte[] bytes = new byte[256];
		int length = TextUtils.encryptPlayerChat(bytes, 0, 0, message.length(),
				message.getBytes());
		player.getSession().write(
				new PacketBuilder(35, Type.VARIABLE_SHORT)
						.putRS2String(name)
						.putSmart(message.length()).put(bytes, 0, length)
						.toPacket());
		return this;
	}

    /**
     * Sends to the client that this player has receives a private message.
     *
     * @param nameAsLong The senders name, as a long.
     * @param rights     The rank the sender has.
     * @param message    The unpacked message.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendRecievedPrivateMessage(long nameAsLong, int rights,
                                                   String message) {
        // int messageCounter = player.getPrivateChat().getLastMessageIndex();
        byte[] bytes = new byte[256];
        bytes[0] = (byte) message.length();
        int length = 1 + TextUtils.encryptPlayerChat(bytes, 0, 1,
                message.length(), message.getBytes());
        PacketBuilder bldr = new PacketBuilder(59, Type.VARIABLE_SHORT)
                .putRS2String(NameUtils.longToName(nameAsLong));
        for (int i = 0; i < 5; i++)
            bldr.put((byte) Misc.random(255));
        bldr.put((byte) rights);
        bldr.put(bytes, 0, length);
        player.getSession().write(bldr.toPacket());
        return this;
    }

	public ActionSender sendRecievedPrivateMessage(String name, int rights,
												   String message) {
		// int messageCounter = player.getPrivateChat().getLastMessageIndex();
		byte[] bytes = new byte[256];
		bytes[0] = (byte) message.length();
		int length = 1 + TextUtils.encryptPlayerChat(bytes, 0, 1,
				message.length(), message.getBytes());
		PacketBuilder bldr = new PacketBuilder(59, Type.VARIABLE_SHORT)
				.putRS2String(name);
		for (int i = 0; i < 5; i++)
			bldr.put((byte) Misc.random(255));
		bldr.put((byte) rights);
		bldr.put(bytes, 0, length);
		player.getSession().write(bldr.toPacket());
		return this;
	}

	public ActionSender sendFriend(String name, int world, int clanRank) {
		PacketBuilder bldr = new PacketBuilder(6, Type.VARIABLE_SHORT);
		bldr.put((byte) 0);
		bldr.putNewString(name);

		final PlayerEntity friend = Server.getInjector().getInstance(PersistenceService.class).getPlayerByDisplayName(name);
//        bldr.putNewString(friend != null ? friend.getPreviousDisplayName() : "");
		bldr.putNewString("");

		bldr.putShort(world);//318, Server.worldId - 1   > 0 ? Server.worldId : 0
		bldr.put((byte) clanRank);
		bldr.put((byte) 0);
		if (world > 0) {
			bldr.putNewString("");
			bldr.put((byte) 0);
			bldr.putInt(0);
		}
		bldr.putNewString("");
		player.write(bldr.toPacket());
		return this;
	}

    /**
     * Sends to the client that a player is logged in on a world.
     *
     * @param nameAsLong The player's name, as a long.
     * @param world      The world they are on.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendFriend(long nameAsLong, int world, int clanRank) {
        PacketBuilder bldr = new PacketBuilder(6, Type.VARIABLE_SHORT);
        bldr.put((byte) 0);
        String name = NameUtils.longToName(nameAsLong);
        bldr.putNewString(NameUtils.formatName(name));

        final PlayerEntity friend = Server.getInjector().getInstance(PersistenceService.class).getPlayerByDisplayName(name);
//        bldr.putNewString(friend != null ? friend.getPreviousDisplayName() : "");
		bldr.putNewString("");

        bldr.putShort(world);//318   > 0 ? Server.worldId : 0
        bldr.put((byte) clanRank);
        bldr.put((byte) 0);
        if (world > 0) {
            bldr.putNewString("");
            bldr.put((byte) 0);
            bldr.putInt(0);
        }
        bldr.putNewString("");
        player.write(bldr.toPacket());
        return this;
    }

    /**
     * Sends to the client what state the player's friend list loading is at.
     *
     * @param status Loading = 0 Connecting = 1 OK = 2
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendFriendServer(int status) {
        player.getSession().write(
                new PacketBuilder(6, Type.VARIABLE_SHORT).toPacket());
        return this;
    }

    /**
     * Sends all of the player's ignore list.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendFriends() {
        if (player.getPrivateChat() != null && player.getPrivateChat().getFriends() != null) {
            for (long l : player.getPrivateChat().getFriends().keySet()) {
                if (World.getWorld().getPlayerNames().get(l) != null) {
                    Player p = World.getWorld().getPlayerNames().get(l);
                    if ((p.getInterfaceState().getPrivateChat() == 0 || p
                            .getInterfaceState().getPrivateChat() == 1
                            && p.getPrivateChat().getFriends()
                            .containsKey(player.getNameAsLong()))
                            && !p.getPrivateChat().getIgnores()
                            .contains(player.getNameAsLong())) {
                        sendFriend(p.getNameAsLong(), Server.worldId, player.getPrivateChat()//Server.worldId
                                .getFriends().get(p.getNameAsLong()).getId());
                    }
                    if (p.getPrivateChat().getFriends()
                            .containsKey(player.getNameAsLong())
                            && player.getInterfaceState().getPrivateChat() == 1) {
                        p.getActionSender().sendFriend(
                                p.getNameAsLong(),
                                1,
                                p.getPrivateChat().getFriends()
                                        .get(player.getNameAsLong()).getId());
                    }
                    sendFriend(p.getNameAsLong(), Server.worldId, player.getPrivateChat()//Server.worldId
                            .getFriends().get(l).getId());
                } else if (player.getPrivateChat().getFriends().get(l) != null){
                    sendFriend(l, 0, player.getPrivateChat()
                            .getFriends().get(l).getId());
                }
            }
        } else {
            World.getWorld().getPrivateChat()
                    .put(player.getName(), new PrivateChat(player.getName(), ""));
        }
        return this;
    }

    /**
     * Sends all of the player's ignore list.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendIgnores() {
        if (player.getPrivateChat() != null && player.getPrivateChat().getIgnores() != null) {
            for (long l : player.getPrivateChat().getIgnores()) {
                sendIgnore(l);
            }
        } else {
            World.getWorld().getPrivateChat()
                    .put(player.getName(), new PrivateChat(player.getName(), ""));
        }
        return this;
    }

	public ActionSender sendIgnore(String name) {
		PacketBuilder bldr = new PacketBuilder(206, Type.VARIABLE_SHORT);
		bldr.put((byte) 0);
		bldr.putRS2String(name);
		bldr.putRS2String(""); // previous name
		bldr.putRS2String("");
		player.write(bldr.toPacket());
		return this;
	}

    /**
     * Sends to the client an ignored player's name.
     *
     * @param nameAsLong The player's name, as a long.
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendIgnore(long nameAsLong) {

        PacketBuilder bldr = new PacketBuilder(206, Type.VARIABLE_SHORT);
        bldr.put((byte) 0);
        bldr.putRS2String(NameUtils.longToName(nameAsLong));
        bldr.putRS2String(""); // previous name
        bldr.putRS2String("");
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender sendClanChannel(String owner, String channelName,
                                        boolean inChannel, List<Player> members, Map<Long, ClanRank> friends) {
        PacketBuilder bldr = new PacketBuilder(208, Type.VARIABLE_SHORT);
        bldr.putRS2String(owner);
        bldr.putLong(TextUtils.playerNameToLong(channelName));
        bldr.put((byte) (inChannel ? 1 : 0));
        bldr.put((byte) (inChannel ? members.size() : 0));
        if (inChannel) {
            for (Player p : members) {
                bldr.putRS2String(p.getName());// player name
                if (Server.worldId == 1) {
                bldr.putShort(1);// world 318
                }if (Server.worldId == 2) {
                    bldr.putShort(2);// world 318
                    }if (Server.worldId == 3) {
                        bldr.putShort(3);// world 318
                    }
                if (permissionService.is(p, PermissionService.PlayerPermissions.ADMINISTRATOR)
                        && !p.getName().equals(owner)) {
                    bldr.put((byte) 127);
                } else {
                    bldr.put((byte) (friends.containsKey(p.getNameAsLong()) ? friends
                            .get(p.getNameAsLong()).getId() : (p.getName()
                            .equals(owner) ? ClanRank.OWNER.getId() : -1))); // rank
                }
                bldr.putRS2String("");
            }
        }
        player.getSession().write(bldr.toPacket());
        return this;
    }

    public ActionSender sendClanMessage(String name, String channelName,
                                        String message, int rights) {
        if (player.getPrivateChat() != null) {
            int messageCounter = player.getPrivateChat().getLastMessageIndex();
            byte[] bytes = new byte[256];
            bytes[0] = (byte) message.length();
            int length = 1 + TextUtils.encryptPlayerChat(bytes, 0, 1,
                    message.length(), message.getBytes());
            player.getSession()
                    .write(new PacketBuilder(204, Type.VARIABLE)
                            .putRS2String(name)
                            .putLong(NameUtils.nameToLong(channelName))
                            .putShort(messageCounter >> 32)
                            .putTriByte(
                                    messageCounter - ((messageCounter >> 32) << 32))
                            .put((byte) rights).put(bytes, 0, length).toPacket());
        } else {
            World.getWorld().getPrivateChat()
                    .put(player.getName(), new PrivateChat(player.getName(), ""));
        }
        return this;
    }

    public ActionSender sendAreaInterface(Location before, Location after) {
        if (player.getAttribute("tabmode") == null) {
            return this;
        }
        int child = (int) player.getAttribute("tabmode") == 548 ? 12 : 9;
		if (pestControlService.containsPlayer(player)) {
			return this;
		}
		if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "ClanWarsFFA")) {
			sendInteractionOption("Attack", 1, true);
			sendInteractionOption("null", 2, false);
		}
//		if (World.getWorld().getType() == WorldType.DEADMAN_MODE) {
//			sendString(90, 1, "Deadman: 3-126");
//			sendInteractionOption("Attack", 1, true);
//			sendInteractionOption("null", 2, false);
//			sendWalkableInterface(90).sendInterfaceConfig(90, 39, false).sendInterfaceConfig(90, 38, false).sendInterfaceConfig(90, 40, true);
//		}
		else if (player.isInWilderness() || player.getBountyTarget() != null) {
			if (World.getWorld().getType() == WorldType.DEADMAN_MODE) {
				sendString(90, 1, "Deadman: 3-126");
				sendWalkableInterface(90).sendInterfaceConfig(90, 39, false).sendInterfaceConfig(90, 38, false).sendInterfaceConfig(90, 40, true);
			}
			if (player.isInWilderness()) {
				int wildernessLevel = 1 + (player.getLocation().getY() - 3520) / 8;
				sendInteractionOption("Attack", 1, true);
				sendInteractionOption("null", 2, false);
				sendString(90, 25, "Level: " + wildernessLevel);
				//child 28
			}
			BountyHunterService bountyHunterService = Server.getInjector().getInstance(BountyHunterService.class);
			bountyHunterService.openWidget(player);
        }
		else if (!BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "Barrows") || (player.getBounty() == null && player.getBountyTarget() == null) && player.getInterfaceState().isWalkableInterface()) {
			/*int[][] bval = {{ 1672, 1 }, { 1673, 2 }, { 1674, 4 }, { 1675, 8 }, { 1676, 16 }, { 1677, 32 }};
            int btot = 0;
            for(int i = 0; i < bval.length; i++){
                if(player.getKilledBrothers().get(bval[i][0])){
                    btot += bval[i][1];
                }
            }
            player.getActionSender().sendConfig(453, btot);*/
            player.getActionSender().removeWalkableInterface();
            player.getActionSender().sendInterfaceConfig(90, 23, true);
            player.getActionSender().removeInterfaces(24, 9);
            sendInteractionOption("null", 1, true);
            sendInteractionOption("null", 2, false);
        }
		if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "DuelArena")) {
			sendWalkableInterface(201);
			sendInteractionOption("Challenge", 2, false);
		}
        if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "Barrows")) {
			sendWalkableInterface(24);
            //sendString(24, 9, "Kill Count: " + player.getKC());
            int[][] bval = {{ 1672, 1 }, { 1673, 2 }, { 1674, 4 }, { 1675, 8 }, { 1676, 16 }, { 1677, 32 }};
            int btot = 0;
            for(int i = 0; i < bval.length; i++){
                if(player.getKilledBrothers().get(bval[i][0])){
                    btot += bval[i][1];
                }
            }
            player.getActionSender().sendConfig(453, btot);
            sendInterfaceConfig(24, 0, false);
        }
        if (BoundaryManager.isWithinBoundaryNoZ(after, "MultiCombat") || player.inMulti()) {
            sendConfig(1021, 32);
        } else {
            sendConfig(1021, 0);
        }
        return this;
    }


    public void sendSkillLevels() {
        for (int i = 0; i < Skills.SKILL_COUNT; i++) {
            sendSkillLevel(i);
        }
    }

    public void sendSkillLevel(int skill) {
        byte level;
        if (skill == Skills.PRAYER) {
            level = (byte) (Math.ceil(player.getSkills().getPrayerPoints()));
        } else {
            level = (byte) player.getSkills().getLevel(skill);
        }
        player.getSession()
                .write(new PacketBuilder(14)
                        .put(level)
                        .putByteA(skill)
                        .putLEInt((int) player.getSkills().getExperience(skill))
                        .toPacket());
    }

    public void sendEnergy() {
        player.getSession()
                .write(new PacketBuilder(140).put(
                        (byte) player.getWalkingQueue().getEnergy()).toPacket());
    }

    /**
     * Sends the player's energy.
     *
     * @return The action sender instance, for chaining.
     */
    public ActionSender sendRunEnergy() {
        player.getSession()
                .write(new PacketBuilder(140).put(
                        (byte) player.getWalkingQueue().getEnergy()).toPacket());
        return this;
    }

    public void setHintIcon(int targetType, int targetId, int arrowId,
                            int playerModel) {
        PacketBuilder bldr = new PacketBuilder(182);
        bldr.put((byte) (0 << 6 | targetType));
        if (targetType > 0) {
            bldr.put((byte) arrowId);
            if (targetType == 1 || targetType == 10) {
                bldr.putShort(targetId);
                bldr.put((byte) 0);
                bldr.put((byte) 0);
                bldr.put((byte) 0);
            }
            bldr.putShort(playerModel);
        } else {
            // bldr.skip(11);
        }
        player.getSession().write(bldr.toPacket());
    }

    public void updateMinimap(int state) {
        // Done
        if (state == ActionSender.BLACKOUT_MAP) {
            player.getInterfaceState().setBlackout(true);
        }
        player.getSession().write(
                new PacketBuilder(155).put((byte) state).toPacket());
    }

    public ActionSender sendSystemUpdate(int time) {
        player.getSession().write(new PacketBuilder(201).putLEShortA(time).toPacket());
        return this;
    }

    public ActionSender sendLocalCoordinates(Location loc, int offsetX,
                                             int offsetY) {
        player.write(new PacketBuilder(154)
                .putByteS(loc.getLocalX(player.getLastKnownRegion()) + offsetX)
                .putByteA(loc.getLocalY(player.getLastKnownRegion()) + offsetY)
                .toPacket());
        return this;
    }

    public enum TabMode {
        FIXED(548), RESIZE(161), REARRANGED(164);
        private int pane;

        TabMode(int pane) {
            this.pane = pane;
        }

        public int getPane() {
            return pane;
        }
    }

    public ActionSender removeEnterAmountInterface() {
        player.getActionSender().packet138();
        return this;
    }

    public void sendTanningInterface() {
        for (int i = 108; i <= 123; i++) {
            sendString(324, i, Tanning.TANNING_INTERFACE[i - 108]);
        }
        sendCS2Script(917, new Object[]{-1, -1}, "ii");
        for (int i = 100; i <= 107; i++) {
            sendItemOnInterface(324, i, Tanning.TANNING_ITEMS[i - 100], 250);
        }
        sendInterface(324, false);
    }

    public void sendCraftingInterface() {
        sendCS2Script(917, new Object[]{-1, -1}, "ii");
        sendInterface(154, false);
    }
      
    public void sendTournament() {
    	sendInterface(100, false);
    	sendAccessMask(1054, 100, 3, 0, 293).sendCS2Script(150, Constants.BUY_PARAMETERS, Constants.TRADE_TYPE_STRING);//274 293
    	//sendAccessMask(1278, 100, 3, 0, 293);
    	//Access 1054 = unlock
    	player.getInterfaceState().addListener(player.getTourn(), new InterfaceContainerListener(player, -1, 64251, 51));//64207
    }


    public void updateClickPriority() {
        sendConfig(1107, player.getDatabaseEntity().getPlayerSettings().getPlayerAttackPriority());
		sendConfig(1306, player.getDatabaseEntity().getPlayerSettings().getNpcAttackPriority());
    }

	public void updateSoundVolume() {
		sendConfig(168, player.getDatabaseEntity().getPlayerSettings().getPlayerMusicVolume());
		sendConfig(169, player.getDatabaseEntity().getPlayerSettings().getPlayerSoundEffectVolume());
		sendConfig(872, player.getDatabaseEntity().getPlayerSettings().getPlayerAreaSoundVolume());
	}

    public ActionSender sendStillGFX(final int id, final int height, final Location loc) {
        sendLocalCoordinates(loc, 0, 0);
        PacketBuilder bldr = new PacketBuilder(239);
        bldr.put((byte) 0);
        bldr.putShort(id);
        bldr.put((byte) height);
        bldr.putShort(0);
        player.write(bldr.toPacket());
        return this;
    }

    public ActionSender sendUpdateLog() {
        sendString(530, 2, "Update Log:<br>October 13th, 2015<br>Worked out some banking bugs<br>Fixed make x for leather crafting<br>Implemented highscores");
        sendString(530, 4, "");
        sendInterface(530, false);
        return this;
    }

    public ActionSender sendDestroyItem(final Item item) {
        player.getInterfaceState().setDestroyItemId(item.getId());
        sendInterfaceConfig(94, 8, false);
        sendInterfaceConfig(94, 9, true);
        sendUpdateItem(94, 0, 133, 1, item);
        sendChatboxInterface(94);
		sendString(94, 3, "Are you sure you want to destroy this item?");
		sendString(94, 13, item.getDefinition2().getName());
        sendString(94, 4, "Yes.");
        sendString(94, 5, "No.");
		sendString(94, 10, "This item is valuable, you will not<br> get it back once lost.");
		return this;
    }

	public ActionSender sendTextListInterface(final String title, final String ... lines) {
		sendInterface(275, false);
		sendString(275, 2, title);
		for (int i = 4; i < 130; i++) {
			int line = i - 4;
			if (line >= lines.length) {
				sendString(275, i, "");
			} else {
				sendString(275, i, lines[line]);
			}
		}
		return this;
	}
	
	public ActionSender sendTeleportListInterface(final String title, final String ... lines) {
		sendInterface(70, false);
		sendString(70, 2, title);
		for (int i = 4; i < 130; i++) {
			int line = i - 4;
			if (line >= lines.length) {
				sendString(70, i, "");
			} else {
				sendString(70, i, lines[line]);
			}
		}
		return this;
	}

	public ActionSender replaceObject(final GameObject original, final GameObject replacement, int ticks) {
		removeObject(original);
		replacement.setLocation(replacement.getSpawnLocation() != null ? replacement.getSpawnLocation()
				: replacement.getLocation());
		sendObject(replacement);
		World.getWorld().submit(new StoppingTick(ticks) {
			@Override
			public void executeAndStop() {
				replacement.setLocation(replacement.getSpawnLocation() != null ? replacement.getSpawnLocation()
						: replacement.getLocation());
				removeObject(replacement);
				sendObject(original);
			}
		});
		return this;
	}
	
	public FlowerGame getFlowerGame() {
		return flowerGame;
	}
	private FlowerGame flowerGame;
	public void sendCreateLocalObject(int id, int type, int direction, Location local) {
		// TODO Auto-generated method stub
		
	}

	public void sendDestroyLocalObject(int type, int direction, Location local) {
		// TODO Auto-generated method stub
		
	}

}
