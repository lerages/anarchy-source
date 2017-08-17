package org.rs2server.rs2.packet;

import org.rs2server.Server;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.content.Jewellery;
import org.rs2server.rs2.content.TeleportInterface;
import org.rs2server.rs2.content.TeleportManager;
import org.rs2server.rs2.content.Jewellery.GemType;
import org.rs2server.rs2.content.SlayerKillLog;
import org.rs2server.rs2.content.Teleporting;
import org.rs2server.rs2.content.api.GameInterfaceButtonEvent;
import org.rs2server.rs2.content.api.GamePlayerEmoteEvent;
import org.rs2server.rs2.content.diary.DiaryType;
import org.rs2server.rs2.content.misc.DragonfireShield;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.*;
import org.rs2server.rs2.domain.service.impl.SlayerServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.BankDepositBoxServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.GrandExchangeServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.LootingBagServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.TeleportInterfaceServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.Animation.Emote;
import org.rs2server.rs2.model.Skills.SkillCape;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.boundary.BoundaryManager;
import org.rs2server.rs2.model.combat.CombatFormulae;
import org.rs2server.rs2.model.combat.CombatState.AttackType;
import org.rs2server.rs2.model.combat.CombatState.CombatStyle;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction.Spell;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction.SpellBook;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.container.PriceChecker;
import org.rs2server.rs2.model.player.ExperienceDropdown;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.player.PrivateChat.EntryRank;
import org.rs2server.rs2.model.player.PrivateChat.KickRank;
import org.rs2server.rs2.model.player.PrivateChat.TalkRank;
import org.rs2server.rs2.model.quests.impl.CooksAssistant;
import org.rs2server.rs2.model.quests.impl.CooksAssistantState;
import org.rs2server.rs2.model.quests.impl.DTStates;
import org.rs2server.rs2.model.quests.impl.DesertTreasure;
import org.rs2server.rs2.model.quests.impl.LunarDiplomacy;
import org.rs2server.rs2.model.quests.impl.LunarStates;
import org.rs2server.rs2.model.skills.Enchanting;
import org.rs2server.rs2.model.skills.Enchanting.BoltType;
import org.rs2server.rs2.model.skills.Smithing;
import org.rs2server.rs2.model.skills.crafting.GemCrafting;
import org.rs2server.rs2.model.skills.crafting.GemCrafting.CraftType;
import org.rs2server.rs2.model.skills.runecrafting.Talisman;
import org.rs2server.rs2.net.ActionSender.TabMode;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.Packet;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.varp.PlayerVariable;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;


/**
 * Handles clicking on most buttons in the interface.
 *
 * @author Graham Edgecombe
 */
public class ActionButtonPacketHandler implements PacketHandler {

    public static final HashMap<Integer, Integer> menuIdxs = new HashMap<Integer, Integer>();

    static {
        menuIdxs.put(255, 0);
        menuIdxs.put(149, 1);
        menuIdxs.put(194, 2);
        menuIdxs.put(159, 3);
        menuIdxs.put(148, 4);
        menuIdxs.put(0, 5);
        menuIdxs.put(245, 6);
        menuIdxs.put(77, 7);
        menuIdxs.put(153, 8);
        menuIdxs.put(46, 9);
        menuIdxs.put(148, 10);
    }

    private final SlayerService slayerService;
    private final PlayerService playerService;
    private final HookService hookService;
    private final BankDepositBoxService bankDepositBox;
    private final PermissionService permissionService;
	private final ItemService itemService;
	private final LootingBagService lootingBagService;
	private final PlayerVariableService variableService;
	private final RunePouchService runePouchService;

    public ActionButtonPacketHandler() {
        this.slayerService = Server.getInjector().getInstance(SlayerService.class);
        this.playerService = Server.getInjector().getInstance(PlayerService.class);
        this.hookService = Server.getInjector().getInstance(HookService.class);
        this.bankDepositBox = Server.getInjector().getInstance(BankDepositBoxService.class);
        this.permissionService = Server.getInjector().getInstance(PermissionService.class);
		this.itemService = Server.getInjector().getInstance(ItemService.class);
		this.lootingBagService = Server.getInjector().getInstance(LootingBagService.class);
		this.variableService = Server.getInjector().getInstance(PlayerVariableService.class);
		this.runePouchService = Server.getInjector().getInstance(RunePouchService.class);
    }

    @Override
    public void handle(final Player player, Packet packet) {
        final int interfaceId = packet.getShort() & 0xFFFF;
        final int button = packet.getShort() & 0xFFFF;
        final int childButton = packet.getShort() & 0xFFFF;
        final int childButton2 = packet.getShort() & 0xFFFF;
        int menuIndex = 0;
        if (menuIdxs.containsKey(packet.getOpcode())) {
            menuIndex = menuIdxs.get(packet.getOpcode());
        }
        if (player.getAttribute("cutScene") != null) {
            return;
        }
        if (packet.getOpcode() != 255) {
            if (player.getCombatState().isDead()) {
                return;
            }
            player.getActionQueue().clearRemovableActions();
        }
        Container[] itemsKeptOnDeath = null;
        if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
            System.out.println("Action button: [opcode=" + packet.getOpcode() + " id=" + interfaceId + " button=" + button + " childButton=" + childButton + " childButton2=" + childButton2 + " menuIndex=" + menuIndex + "]");
        }
        
		PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
		
        boolean defensive = (player.getInterfaceState().getOpenAutocastType() == 1);
        int config = (childButton) * 2;

        hookService.post(new GameInterfaceButtonEvent(player, interfaceId, button, childButton, childButton2, menuIndex));
        
        switch (interfaceId) {
        case 187:
        	switch (button) {
        	case 3://
        	switch (childButton) {
        	case 1://Varrock
        		player.setTeleportTarget(Location.create(3213, 3424, 0));
                player.getActionSender().closeAll();
        		break;
        	case 2://Camelot
        		player.setTeleportTarget(Location.create(2757, 3477, 0));
           	    player.getActionSender().closeAll();
        		break;
        	}
        	break;
        	}
        	break;
        
        
        
			case 548:
				switch (button) {
					case 33:
						Access emoteAccess = Access.of(216, 1, NumberRange.of(0, 43), AccessBits.ALLOW_LEFT_CLICK);
						player.sendAccess(emoteAccess);
						break;
				}
				break;	
            case GrandExchangeServiceImpl.GRAND_EXCHANGE_IVENTORY_WIDGET:
                GrandExchangeService geService = Server.getInjector().getInstance(GrandExchangeService.class);
                switch (button) {
                    case 0:
                        Item item = player.getInventory().get(childButton);
                        if (item != null) {
                            geService.sendSellScreen(player, item);
                        }
                        break;
                }
                break;
                
            case 69:
    			switch(button) {
    			case 9:
    			case 10:
    			case 11:
    			case 12:
    			case 13:
    				
    				break;
    			case 3:
    				int pane = player.getAttribute("tabmode");
                    int tabId = pane == 548 ? 73 : pane == 161 ? 73 : 73;
                    player.getActionSender().sendSidebarInterface(tabId, 182);
    				break;
    			case 19:
    				player.getActionSender().sendLogout();
    				break;
    			}
            	
            	break;
            	
    		case 215://Ironman Interface
    			switch(button) {
    			case 15://Ironman Rights
    				PermissionService perms = Server.getInjector().getInstance(PermissionService.class);
    				perms.give(player, PermissionService.PlayerPermissions.IRON_MAN);
    				perms.remove(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN);
    				perms.remove(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
    				player.setIsIronMan(true);
					player.setHardcoreIronMan(false);
					player.setUltimateIronMan(false);
    				player.getActionSender().sendConfig(499, 1621114888);
    				break;
    			case 19://Hardcore Ironman Rights
    				PermissionService perms3 = Server.getInjector().getInstance(PermissionService.class);
    				perms3.give(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN);
    				perms3.remove(player, PermissionService.PlayerPermissions.IRON_MAN);
    				perms3.remove(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
    				player.setIsIronMan(false);
					player.setHardcoreIronMan(true);
					player.setUltimateIronMan(false);
					player.getActionSender().sendConfig(499, 24);
    				break;
    			case 23://Ultimate Ironman Rights
    				PermissionService perms1 = Server.getInjector().getInstance(PermissionService.class);
    				perms1.give(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
    				perms1.remove(player, PermissionService.PlayerPermissions.IRON_MAN);
    				perms1.remove(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN);
    				player.setIsIronMan(false);
					player.setHardcoreIronMan(false);
					player.setUltimateIronMan(true);
					player.getActionSender().sendConfig(499, -2147483632);
    				break;
    			case 27://Normal Player Rights
    				PermissionService perms2 = Server.getInjector().getInstance(PermissionService.class);
    				perms2.give(player, PermissionService.PlayerPermissions.PLAYER);
    				perms2.remove(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN);
    				perms2.remove(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
    				perms2.remove(player, PermissionService.PlayerPermissions.IRON_MAN);
    				player.setIsIronMan(false);
					player.setHardcoreIronMan(false);
					player.setUltimateIronMan(false);
    				player.getActionSender().sendConfig(499, 0);
    				break;
    		//	default:
    		//		player.getActionSender().sendMessage("No support currently exists for this.");
    			}
    			break;     
                
    			/*
    			 * Quest tab/Achievement tab/Minigames tab/Kourand Tab
    			 */	
    		case 399://Quest 274 - 399
    			switch (button) {
    			case 1://11
    				int pane = player.getAttribute("tabmode");
                    int tabId = pane == 548 ? 65 : pane == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId, 259);
                    player.sendAccess(Access.of(259, 4, NumberRange.of(0, 10), AccessBits.ALLOW_LEFT_CLICK));
    				//player.getActionSender().sendConfig(1055, 8768);
    				break;
    			case 2://12
    				int pane1 = player.getAttribute("tabmode");
                    int tabId1 = pane1 == 548 ? 65 : pane1 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId1, 76);
                   // player.getActionSender().sendConfig(1055, 8764);
                    
    				//player.getActionSender().sendInterface(165, 9, 76, true);
    				break;
    			case 3://13
    				int pane2 = player.getAttribute("tabmode");
                    int tabId2 = pane2 == 548 ? 65 : pane2 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId2, 245);
                    //player.getActionSender().sendConfig(1055, 41536);
    				break;
    			case 7:
    				switch (childButton) {
    				
    				}
    			case 8:
    				switch (childButton) {
    				case 12:
       				// DesertTreasure quest = (DesertTreasure) player.getQuests().get(DesertTreasure.class);
                   //     if (quest == null) {
                    //        quest = new DesertTreasure(player, DTStates.NOT_STARTED);
                    //        player.getQuests().put(DesertTreasure.class, quest);
                   //     }
                    //    quest.showQuestInterface();
       				break;
    				case 52:
    					// LunarDiplomacy lunar = (LunarDiplomacy) player.getQuests().get(LunarDiplomacy.class);
                       //  if (lunar == null) {
                        //     lunar = new LunarDiplomacy(player, LunarStates.NOT_STARTED);
                       //      player.getQuests().put(LunarDiplomacy.class, lunar);
                       //  }
                        // lunar.showQuestInterface();
                         break;
    				
    				}
    				
    			}
    			break;
    		case 259://Achievement
    			switch (button) {
    			case 1://10
    				int pane = player.getAttribute("tabmode");
                    int tabId = pane == 548 ? 65 : pane == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId, 399);
                    player.sendAccess(Access.of(399, 7, NumberRange.of(0, 18), AccessBits.ALLOW_LEFT_CLICK));
                    player.sendAccess(Access.of(399, 8, NumberRange.of(0, 110), AccessBits.ALLOW_LEFT_CLICK));
                    player.sendAccess(Access.of(399, 9, NumberRange.of(0, 12), AccessBits.ALLOW_LEFT_CLICK));
    				//player.getActionSender().sendConfig(1055, 8768);
    				//player.getActionSender().updateQuestText();
    				break;
    			case 2://11
    				int pane1 = player.getAttribute("tabmode");
                    int tabId1 = pane1 == 548 ? 65 : pane1 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId1, 76);
                   // player.getActionSender().sendConfig(1055, 8768);
    				break;
    			case 3://12
    				int pane3 = player.getAttribute("tabmode");
                    int tabId3 = pane3 == 548 ? 65 : pane3 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId3, 245);
                  //  player.getActionSender().sendConfig(1055, 41536);
    				switch(button) {
    				case 4:
    					switch(childButton) {
    				  case 0:
    					player.getAchievementDiary().open(player);
    				  break;
    				}
    			  }
    			}
    			break;			
    		case 76://Minigame
    			switch (button) {
    			case 2:
    				int pane = player.getAttribute("tabmode");
                    int tabId = pane == 548 ? 65 : pane == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId, 399);
                    player.sendAccess(Access.of(399, 7, NumberRange.of(0, 18), AccessBits.ALLOW_LEFT_CLICK));
                    player.sendAccess(Access.of(399, 8, NumberRange.of(0, 110), AccessBits.ALLOW_LEFT_CLICK));
                    player.sendAccess(Access.of(399, 9, NumberRange.of(0, 12), AccessBits.ALLOW_LEFT_CLICK));
    				//player.getActionSender().sendConfig(1055, 8768);
    				//player.getActionSender().updateQuestText();
    				break;
    			case 3:
    				int pane2 = player.getAttribute("tabmode");
                    int tabId2 = pane2 == 548 ? 65 : pane2 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId2, 259);
                    player.sendAccess(Access.of(259, 4, NumberRange.of(0, 10), AccessBits.ALLOW_LEFT_CLICK));
    				break;
    			case 4:
    				int pane3 = player.getAttribute("tabmode");
                    int tabId3 = pane3 == 548 ? 65 : pane3 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId3, 259);
                    break;
    			case 5:
    				int pane4 = player.getAttribute("tabmode");
                    int tabId4 = pane4 == 548 ? 65 : pane4 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId4, 245);
                   // player.getActionSender().sendConfig(1055, 41536);
    				break;
    			}
    			break;		
                
    		case 245:
    			switch (button) {
    			case 2:
    				int pane = player.getAttribute("tabmode");
                    int tabId = pane == 548 ? 65 : pane == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId, 399);
                    player.sendAccess(Access.of(399, 7, NumberRange.of(0, 18), AccessBits.ALLOW_LEFT_CLICK));
                    player.sendAccess(Access.of(399, 8, NumberRange.of(0, 110), AccessBits.ALLOW_LEFT_CLICK));
                    player.sendAccess(Access.of(399, 9, NumberRange.of(0, 12), AccessBits.ALLOW_LEFT_CLICK));
    				//player.getActionSender().sendConfig(1055, 8768);
    				//player.getActionSender().updateQuestText();
    				break;
    			case 3:
    				int pane2 = player.getAttribute("tabmode");
                    int tabId2 = pane2 == 548 ? 65 : pane2 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId2, 259);
                    player.sendAccess(Access.of(259, 4, NumberRange.of(0, 10), AccessBits.ALLOW_LEFT_CLICK));
    				break;
    			case 4:
    				int pane3 = player.getAttribute("tabmode");
                    int tabId3 = pane3 == 548 ? 65 : pane3 == 161 ? 56 : 56;
                    player.getActionSender().sendSidebarInterface(tabId3, 76);
    				break;
    			case 23:
    				player.getActionSender().sendInterface(243, false);
    				break;
    			}
    			break;
    			
            
                
            case BankDepositBoxServiceImpl.DEPOSIT_INTERFACE:
                bankDepositBox.handleInterfaceActions(player, button, childButton, childButton2, menuIndex);
                break;
                
            case SlayerServiceImpl.TASK_WIDGET_ID://Rewards interface
            	//player.sendMessage("Interface: " + interfaceId + " Button: " + button + " childButton: " + childButton);
                if (button == 8) {
                	System.out.println("Unhandled action button : " + interfaceId + " - " + button + " - " + childButton);
                	switch(childButton)
                	{
                	case 5: // Unlock slayer helm
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 400) {
							player.getDatabaseEntity().getSlayerSkill().setUnlockedSlayerHelm(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 400);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
					break;
                	case 4: //Extend Dark beasts
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskDarkBeast(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 8: //Extend Ankous
                	case 9: //Extend Suqah
                	case 12: //Spritiual creatures
                	case 22: //aviansies
                	case 23: //Extend Cave Horror
                	case 20: //bloodveld
                	case 21: //abberant spectre
                	case 25: //dust devil
                	case 36: //scarabas
						player.sendMessage("This task cannot currently be extended.");
						break;
                	case 35://super creatures
                		//player.sendMessage("Superior creatures are currently not added.");
                		if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 150 && !player.getDatabaseEntity().getSlayerSkill().isUnlockedSuperiors()) {
							player.getDatabaseEntity().getSlayerSkill().setUnlockedSuperiors(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 150);
							player.sendMessage("<col=ff0000>Super slayer creatures have been unlocked.");
							//slayerService.openRewardsScreen(player);
                		} else if(player.getDatabaseEntity().getSlayerSkill().isUnlockedSuperiors()) {
                			player.sendMessage("<col=ff0000>Super slayer creatures have already been unlocked.");
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
                		break;
                	case 10: //Extend Black Dragon
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 50) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskBlackDragon(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 50);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 11://Extend Bronze, Iron, Steel dragons
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskMetalDragons(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 13://Extend Abyssal demons
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskAbyssalDemon(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 14: //Extend Black demons
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskBlackDemon(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 15: //Extend Greater demons
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskGreaterDemon(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 24: //Extend Cave Horrors
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskCaveHorror(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 26: //Extend Skeletal Wyvern
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskSkeletalWyvern(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 27: //Extend Gargoyle
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskGargoyle(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 28: //Extend Nechryaels
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskNechryael(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 29: //Extend Kraken
						if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 100) {
							player.getDatabaseEntity().getSlayerSkill().setExtendTaskCaveKraken(true);
							player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 100);
							//slayerService.openRewardsScreen(player);
						} else {
							player.sendMessage("You do not have enough points to buy that.");
						}
						break;
                	case 37:// Cancel task
                        slayerService.cancelTask(player, true);
                      //  slayerService.openRewardsScreen(player);
                        break;
                	case 38:// Block task
                		player.sendMessage("Block task button");
						slayerService.blockTask(player);
                    //  //  slayerService.openRewardsScreen(player);
                        break;
                	case 39:// Unblock task buttons [1..5]
                	case 40:
                	case 41:
                	case 42:
                	case 43:
                		player.sendMessage("Unblock task button: " + childButton);
                       int buttonIndex = childButton - 38;
						slayerService.unblockTask(player, buttonIndex);
                     // slayerService.openRewardsScreen(player);
                    break;
                	}
                	//player.getActionSender().sendMessage("InterfaceID : " + interfaceId + ". ButtonID: " + button + ". ChildButtonID: " + childButton);
                	} else if (button == 23) {
                    if (childButton == 0 && menuIndex == 1) { // Buy 1 slayer ring
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 75) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 75);
                            playerService.giveItem(player, new Item(11866, 1), true);
                          //  slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 1 && menuIndex == 1) { // Buy 1 broad bolts
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 35) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 35);
                            playerService.giveItem(player, new Item(11875, 250), true);
                          //  slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 2 && menuIndex == 1) { // Buy 1 broad arrows
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 35) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 35);
                            playerService.giveItem(player, new Item(4160, 250), true);
                          //  slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 3 && menuIndex == 1) { // Buy 1 herb sack
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 750) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 750);
                            playerService.giveItem(player, new Item(13226, 1), true);
                           // slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 0 && menuIndex == 2) { // Buy 5 slayer ring
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 375) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 375);
                            playerService.giveItem(player, new Item(11866, 5), true);
                          //  slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 0 && menuIndex == 3) { // Buy 10 slayer ring
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 750) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 750);
                            playerService.giveItem(player, new Item(11866, 10), true);
                          //  slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 1 && menuIndex == 2) { // Buy 5 broad bolts
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 175) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 175);
                            playerService.giveItem(player, new Item(11875, 1250), true);
                           // slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 2 && menuIndex == 2) { // Buy 5 broad arrows
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 175) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 175);
                            playerService.giveItem(player, new Item(4160, 1250), true);
                           // slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 1 && menuIndex == 3) { // Buy 10 broad bolts
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 350) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 350);
                            playerService.giveItem(player, new Item(11875, 2500), true);
                          //  slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                        }
                    } else if (childButton == 2 && menuIndex == 3) { // Buy 10 broad arrows
                        if (player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() >= 350) {
                            player.getDatabaseEntity().getStatistics().setSlayerRewardPoints(player.getDatabaseEntity().getStatistics().getSlayerRewardPoints() - 350);
                            playerService.giveItem(player, new Item(4160, 2500), true);
                           // slayerService.openRewardsScreen(player);
                        } else {
                            player.sendMessage("You do not have enough points to buy that.");
                          //  System.out.println("Unhandled action button : " + interfaceId + " - " + button + " - " + childButton);
                        }
                    }
                }
                slayerService.sendConfigs(player);
                break;

            case 320:
            	/*if (button < 0  && button > 6 || button > 6 && button != 9) {
    				player.getActionSender().sendMessage("You can only set combat stats.");
    				return;
    			}
    			if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "PvP Zone")) {
    				player.getActionSender().sendMessage("You can't do this in a PvP Zone.");
    				return;
    			}
    			int skill_selected = -1;
    			if (button == 1) {
    				skill_selected = 0;
    			} else if (button == 2) {
    				skill_selected = 2;
    			} else if (button == 3) {
    				skill_selected = 1;
    			} else if (button == 4) {
    				skill_selected = 4;
    			} else if (button == 5) {
    				skill_selected = 5;
    			} else if (button == 6) {
    				skill_selected = 6;
    			} else if (button == 9) {
    				skill_selected = 3;
    			}
    			player.setInterfaceAttribute("skillLevelChange", skill_selected);
    			player.getInterfaceState().openEnterAmountInterface(320, skill_selected, -1);*/
            	
            	player.getActionSender().sendSkillMenu(button);
                break;
            case 214:
                if (player.hasAttribute("viewingSkill")) {
                    int modifier = (button - 11) * 1024;
                    if (button == 11) {
                        player.getActionSender().sendConfig(965, player.getAttribute("viewingSkill"));
                        return;
                    }
                    player.getActionSender().sendConfig(965, (int) player.getAttribute("viewingSkill") + modifier);
                }
                break;
            case 80:
                BoltType boltType = BoltType.forId(button);
                if (boltType != null) {
                    player.getActionSender().removeInterface();
                    player.getActionQueue().addAction(new Enchanting(player, boltType));
                }
                break;
//            case 100:
//                switch (menuIndex) {
//                    case 0:
//                    	player.getInventory().add(new Item(childButton2, 1));
//                        //Shop.costItemTournament(player, childButton, childButton2);//Shop returns null ;/
//                        break;
//                    case 1:
//                    	player.getInventory().add(new Item(childButton2, 5));
//                        //Shop.buyItem(player, childButton, childButton2, 1);
//                        break;
//                    case 2:
//                    	player.getInventory().add(new Item(childButton2, 10));
//                        //Shop.buyItem(player, childButton, childButton2, 5);
//                        break;
//                    case 3:
//                    	player.getInventory().add(new Item(childButton2, 100));
//                       // DialogueManager.openDialogue(player, 60);
//                      //  player.getInterfaceState().setShopItem(childButton2, childButton);
//                        break;
//            	}
//                break;
            	
            
            case 300:
            	switch (menuIndex) {
                    case 0:
                        Shop.costItem(player, childButton, childButton2);
                        break;
                    case 1:
                        Shop.buyItem(player, childButton, childButton2, 1);
                        break;
                    case 2:
                        Shop.buyItem(player, childButton, childButton2, 5);
                        break;
                    case 3:
						Shop.buyItem(player, childButton, childButton2, 10);
                        //DialogueManager.openDialogue(player, 60);
                        //player.getInterfaceState().setShopItem(childButton2, childButton);
                        break;
            	}
                break;
            case 301:
                switch (menuIndex) {
                    case 0:
                        Shop.valueItem(player, childButton, childButton2);
                        break;
                    case 1:
                        Shop.sellItem(player, childButton, childButton2, 1);
                        break;
                    case 2:
                        Shop.sellItem(player, childButton, childButton2, 5);
                        break;
                    case 3:
                        Shop.sellItem(player, childButton, childButton2, 10);
                        break;
                    case 10:
                        Shop.sellItem(player, childButton, childButton2, 50);
                        break;
                }
                break;
            case 162:
                int before = player.getInterfaceState().getPrivateChat();
                switch (button) {
                    case 15:
                        switch (menuIndex) {
                            case 2:
                                player.getInterfaceState().setPrivateChat(0);
                                break;
                            case 3:
                                player.getInterfaceState().setPrivateChat(1);
                                break;
                            case 10:
                                player.getInterfaceState().setPrivateChat(2);
                                break;
                        }
                        break;
                }
                if (player.getInterfaceState().getPrivateChat() != before) {
                    player.getPrivateChat().updateFriendList(true);
                }
                break;
          //  case 274:
            //    switch (button) {
                    /*case 16:
                        DesertTreasure quest = (DesertTreasure) player.getQuests().get(DesertTreasure.class);
                        if (quest == null) {
                            quest = new DesertTreasure(player, DTStates.NOT_STARTED);
                            player.getQuests().put(DesertTreasure.class, quest);
                        }
                        quest.showQuestInterface();
                        break;
                    case 17:
                        LunarDiplomacy lunar = (LunarDiplomacy) player.getQuests().get(LunarDiplomacy.class);
                        if (lunar == null) {
                            lunar = new LunarDiplomacy(player, LunarStates.NOT_STARTED);
                            player.getQuests().put(LunarDiplomacy.class, lunar);
                        }
                        lunar.showQuestInterface();
                        break;
					case 19:
						player.setForceChat("I currently have " + player.getDatabaseEntity().getBountyHunter().getKills() + " Kills!");
						player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.FORCED_CHAT);
						break;
					case 20:
						player.setForceChat("I currently have " + player.getDatabaseEntity().getBountyHunter().getDeaths() + " Deaths!");
						player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.FORCED_CHAT);
						break;
					case 21:
						player.setForceChat("My KDR is: " + player.getKDR() + ".");
						player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.FORCED_CHAT);
						break;*/
             //   }
            //    break;
            case 109:
              /*  Item stakeItem = player.getInventory().get(childButton);
                if (stakeItem != null && stakeItem.getDefinition() != null) {
                    System.out.println("sweggington");
                    switch (packet.getOpcode()) {
                        case 255:
                            Duel.offerItem(player, childButton, stakeItem.getId(), 1);
                            break;
                        case 149:
                            Duel.offerItem(player, childButton, stakeItem.getId(), 5);
                            break;
                        case 194:
                            Duel.offerItem(player, childButton, stakeItem.getId(), 10);
                            break;
                        case 159:
                            Duel.offerItem(player, childButton, stakeItem.getId(), stakeItem.getCount());
                            break;
                        case 148:
                            break;
                    }
                }*/
                break;
            case 107:
               /* stakeItem = player.getDuelContainer().get(childButton);
                if (stakeItem != null && stakeItem.getDefinition() != null) {
                    System.out.println("sweggington");
                    switch (packet.getOpcode()) {
                        case 255:
                            Duel.removeItem(player, childButton, stakeItem.getId(), 1);
                            break;
                        case 149:
                            Duel.removeItem(player, childButton, stakeItem.getId(), 5);
                            break;
                        case 194:
                            Duel.removeItem(player, childButton, stakeItem.getId(), 10);
                            break;
                        case 159:
                            Duel.removeItem(player, childButton, stakeItem.getId(), stakeItem.getCount());
                            break;
                        case 148:
                            break;
                    }
                }*/
                break;
            case 216:
                if (!player.canAnimate() || !player.canEmote()) {
                    player.getActionSender().sendMessage("You can't perform that emote right now.");
                    return;
                }
                Tickable emotes = new Tickable(2) { //why a tickable? because if you run, then perform a skillcape emote, the timing is off ;)
                    @Override
                    public void execute() {
                        Emote emote = Emote.forId(childButton + 2);
                        if (emote != null) {
                            if (emote.getAnimation() != null) {
                                player.playAnimation(emote.getAnimation());
                            }
                            if (emote.getGraphic() != null) {
                                player.playGraphics(emote.getGraphic());
                            }
                            player.setEmote(true);
                            hookService.post(new GamePlayerEmoteEvent(player, emote));
                        } else {
                            switch (childButton) {
                                case 42:
                                    if (player.getEquipment().get(Equipment.SLOT_CAPE) == null) {
                                        player.setEmote(true);
                                        player.getActionSender().sendMessage("You aren't wearing a skillcape.");
                                        return;
                                    }
                                    SkillCape skillCape = SkillCape.forId(player.getEquipment().get(Equipment.SLOT_CAPE));
                                    if (skillCape == null) {
                                        player.setEmote(true);
                                        player.getActionSender().sendMessage("You aren't wearing a skillcape.");
                                    } else {
                                        if (skillCape.getAnimation() != null) {
                                            player.playAnimation(skillCape.getAnimation());
                                        }
                                        if (skillCape.getGraphic() != null) {
                                            player.playGraphics(skillCape.getGraphic());
                                        }
                                        World.getWorld().submit(new Tickable(skillCape.getAnimateTimer()) {
                                            @Override
                                            public void execute() {
                                                player.setEmote(true);
                                                this.stop();
                                            }
                                        });
                                    }
                                    break;
                                default:
                                    player.setEmote(true);
                                    System.out.println("Unhandled action button : " + interfaceId + " - " + button + " - " + childButton);
                                    break;
                            }
                        }
                        this.stop();
                    }
                };
                player.setEmote(false);
                if (player.getSprites().getPrimarySprite() != -1 || player.getSprites().getSecondarySprite() != -1) {
                    player.getWalkingQueue().reset();
                    World.getWorld().submit(emotes);
                } else {
                    emotes.execute();
                }
                break;
            case 446:
                Item craftItem = new Item(childButton2);
                CraftType type = CraftType.forId(craftItem);
                if (type != null) {
                    int amount = 0;
                    switch (menuIndex) {
                        case 0:
                            amount = 1;
                            break;
                        case 1:
                            amount = 5;
                            break;
                        case 2:
                            amount = 10;
                            break;
                    }
                    player.getActionQueue().addAction(new GemCrafting(player, type, amount));
                    player.getActionSender().removeAllInterfaces();
                }
                break;
            case 137:
                switch (button) 
                {
                    case 50://Position
                        switch (childButton) 
                        {
                            case 1:
                                player.getDropdown().setAlignment(ExperienceDropdown.ALIGN_RIGHT);
                                break;
                            case 2:
                                player.getDropdown().setAlignment(ExperienceDropdown.ALIGN_CENTER);
                                break;
                            case 3:
                                player.getDropdown().setAlignment(ExperienceDropdown.ALIGN_LEFT);
                                break;
                        }
                        break;
                    case 51://Size
                        switch (childButton) 
                        {
                            case 1:
                                player.getDropdown().setFontSize(ExperienceDropdown.SMALLEST_FONT);
                                break;
                            case 2:
                                player.getDropdown().setFontSize(ExperienceDropdown.MEDIUM_FONT);
                                break;
                            case 3:
                                player.getDropdown().setFontSize(ExperienceDropdown.LARGE_FONT);
                                break;
                        }
                        break;
                    case 53:
                        player.getDropdown().setSelectedSkill(childButton - 1);
                        break;
                        
                    case 54:
                        player.getDropdown().setExperienceBarSkill(childButton - 1);
                        break;
                }
                player.getActionSender().sendConfig(1227, player.getDropdown().hashCode());
                break;
            case Smithing.INTERFACE:
                Smithing.handleForgingOptions(player, button, menuIndex);
                break;
            case 81: // LOOTING BAG
                if (button != 2) {
                    switch (menuIndex) {
                        case 0:
                            lootingBagService.deposit(player, childButton, childButton2, 1);
                            break;
                        case 1:
							lootingBagService.deposit(player, childButton, childButton2, 5);
                            break;
                        case 2:
							lootingBagService.deposit(player, childButton, childButton2, player.getInventory().getCount(childButton2));
                            break;
                    }
                }
                break;
            case 465: // GRAND EXCHANGE
            	Item itemz = null;
                switch (button) {
                case 3:
                	player.getActionSender().sendInterface(383, false);
                	break;
                    case 7:
                    	switch (childButton) {
                        case 3:
                        player.setInterfaceAttribute("buy_screen", true);
                        player.getActionSender().sendCS2Script(750, new Object[]{-1, 1, "What would you like to buy?"}, "s1g")
                        		.sendConfig(375, 48)
                                .sendConfig(563, 0)
                                .sendConfig(1043, 0)
                                .sendConfig(563, 0)
                                .sendConfig(1151, -1)
                                .sendInterfaceConfig(162, 30, false)
                                .sendInterfaceConfig(162, 31, false)
                        		.sendInterfaceConfig(162, 32, true)
                        		.sendInterfaceConfig(162, 33, false)
                        		.sendInterfaceConfig(162, 34, false)
                        		.sendInterfaceConfig(162, 35, false)
                        		.sendInterfaceConfig(162, 36, false)
                        		.sendInterfaceConfig(162, 37, false)
                        		.sendInterfaceConfig(162, 38, false)
                        		.sendInterfaceConfig(162, 39, false)
                        		.sendInterfaceConfig(162, 40, true)
                        		.sendInterfaceConfig(162, 41, true)
                        		.sendInterfaceConfig(162, 42, true)
                        		.sendInterfaceConfig(162, 43, true)
                        		.sendInterfaceConfig(162, 44, true)
                        		.sendInterfaceConfig(162, 45, true)
                        		.sendInterfaceConfig(162, 46, true)
                        		.sendInterfaceConfig(162, 47, true)
                        		.sendInterfaceConfig(162, 48, true)
                        		.sendInterfaceConfig(162, 49, true)
                        		.sendInterfaceConfig(162, 50, true)
                        		.sendInterfaceConfig(162, 51, true)
                        		
                        		;
                        	
                        	
                        GrandExchangeService geServicebuy = Server.getInjector().getInstance(GrandExchangeService.class);
                   	 Item items = player.getInventory().get(childButton);
                        if (items != null) {
                        	geServicebuy.sendBuyScreen(player, items);
                        }
                        break;
                        case 4:
                        	 GrandExchangeService geServicesell = Server.getInjector().getInstance(GrandExchangeService.class);
                        	 Item item = player.getInventory().get(childButton);
                             if (item != null) {
                                 geServicesell.sendSellScreen(player, item);
                             }
                        	break;
                   	}
                    	 
                        break;
                    case 24:
                        switch (childButton) {
                            case 0:
                            	player.setInterfaceAttribute("buy_screen", true);
                                player.getActionSender().sendCS2Script(750, new Object[]{-1, 1, "What would you like to buy?"}, "s1g")
                                .sendConfig(375, 48)
                                .sendConfig(563, 0)
                                .sendConfig(1043, 0)
                                .sendConfig(563, 0)
                                .sendConfig(1151, -1)
                                .sendInterfaceConfig(162, 30, false)
                                .sendInterfaceConfig(162, 31, false)
                        		.sendInterfaceConfig(162, 32, true)
                        		.sendInterfaceConfig(162, 33, false)
                        		.sendInterfaceConfig(162, 34, false)
                        		.sendInterfaceConfig(162, 35, false)
                        		.sendInterfaceConfig(162, 36, false)
                        		.sendInterfaceConfig(162, 37, false)
                        		.sendInterfaceConfig(162, 38, false)
                        		.sendInterfaceConfig(162, 39, false)
                        		.sendInterfaceConfig(162, 40, true)
                        		.sendInterfaceConfig(162, 41, true)
                        		.sendInterfaceConfig(162, 42, true)
                        		.sendInterfaceConfig(162, 43, true)
                        		.sendInterfaceConfig(162, 44, true)
                        		.sendInterfaceConfig(162, 45, true)
                        		.sendInterfaceConfig(162, 46, true)
                        		.sendInterfaceConfig(162, 47, true)
                        		.sendInterfaceConfig(162, 48, true)
                        		.sendInterfaceConfig(162, 49, true)
                        		.sendInterfaceConfig(162, 50, true)
                        		.sendInterfaceConfig(162, 51, true)
                        		;
                                break;
                            case 7:
                            	player.setInterfaceAttribute("entergequantity", true);
                            	player.getActionSender().sendCS2Script(750, new Object[]{-1, 1, "How many do you wish to buy?"}, "s1g")
                            	.sendInterfaceConfig(162, 30, false)
                                .sendInterfaceConfig(162, 31, false)
                        		.sendInterfaceConfig(162, 32, false)//true
                        		.sendInterfaceConfig(162, 33, false)
                        		.sendInterfaceConfig(162, 34, false)//false
                        		.sendInterfaceConfig(162, 35, true)
                        		.sendInterfaceConfig(162, 36, true)
                        		.sendInterfaceConfig(162, 37, true)
                        		.sendInterfaceConfig(162, 38, true)
                        		.sendInterfaceConfig(162, 39, true)
                        		
                        		.sendInterfaceConfig(162, 40, true)
                        		.sendInterfaceConfig(162, 41, true)
                        		.sendInterfaceConfig(162, 42, true)
                        		.sendInterfaceConfig(162, 43, true)
                        		.sendInterfaceConfig(162, 44, true)
                        		.sendInterfaceConfig(162, 45, true)
                        		.sendInterfaceConfig(162, 46, true)
                        		.sendInterfaceConfig(162, 47, true)
                        		.sendInterfaceConfig(162, 48, true)
                        		.sendInterfaceConfig(162, 49, true)
                        		.sendInterfaceConfig(162, 50, true)
                        		.sendInterfaceConfig(162, 51, true);
                            	break;
                            case 11:
                            	player.getActionSender().sendConfig(1043, itemz.getDefinition().getStorePrice());
                            	break;
                        }
                        break;
                    case 23:
                        switch (childButton) {
                            case 0:
                                player.getActionSender().sendCS2Script(750, new Object[]{-1, 1, "What would you like to buy?"}, "s1g");
                                break;
                        }
                        break;
                    case 27:
                    	player.getActionSender().sendMessage("Confirm button plox?");
                    	break;
                }
                break;
            case PriceChecker.PRICE_INVENTORY_INTERFACE:
                switch (button) {
                    case 5: //search
						player.getActionSender().sendCS2Script(750, new Object[] {-1, 1, "Select an item to ask about its price:"}, "s1g");
						player.setInterfaceAttribute("priceSearch", true);
                        return;
                    case 10: //deposit all
                        for (int i = 0; i < Inventory.SIZE; i++) {
                            Item item = player.getInventory().get(i);
                            if (item != null) {
                                PriceChecker.deposit(player, i, item.getId(), item.getCount());
                            }
                        }
                        break;
                    default:
                        Item child = player.getPriceChecker().get(childButton);
                        if (child != null && child.getDefinition() != null) {
                            switch (menuIndex) {
                                case 0:
                                    PriceChecker.withdraw(player, childButton,
                                            child.getId(), 1);
                                    break;
                                case 1:
                                    PriceChecker.withdraw(player, childButton,
                                            child.getId(), 5);
                                    break;
                                case 2:
                                    PriceChecker.withdraw(player, childButton,
                                            child.getId(), 10);
                                    break;
                                case 3:
                                    PriceChecker.withdraw(player, childButton, child
                                                    .getId(),
                                            player.getPriceChecker()
                                                    .getCount(child.getId()));
                                    break;
                            }
                        }
                        break;
                }

                break;
            case PriceChecker.PLAYER_INVENTORY_INTERFACE:
                Item priceItem = player.getInventory().get(childButton);
                if (priceItem != null && priceItem.getDefinition() != null) {
                    switch (menuIndex) {
                        case 0:
                            PriceChecker.deposit(player, childButton,
                                    priceItem.getId(), 1);
                            break;
                        case 1:
                            PriceChecker.deposit(player, childButton,
                                    priceItem.getId(), 5);
                            break;
                        case 2:
                            PriceChecker.deposit(player, childButton,
                                    priceItem.getId(), 10);
                            break;
                        case 3:
                            PriceChecker.deposit(player, childButton,
                                    priceItem.getId(), player.getInventory().getCount(priceItem.getId()));
                            break;
                    }
                }
                break;
            /*case Trade.SECOND_TRADE_SCREEN:
                switch (button) {
                    case 13:
                        Trade.acceptTrade(player, 2);
                        break;
                    case 14:
                        player.getRequestManager().cancelRequest();
                        break;
                }
            case Trade.TRADE_INVENTORY_INTERFACE:
                if (childButton == 65535) {
                    switch (button) {
                        case 10:
                            Trade.acceptTrade(player, 1);
                            break;
                    }
                } else {
                    Item tradeItem = player.getTrade().get(childButton);
                    if (tradeItem != null && tradeItem.getDefinition() != null) {
                        switch (packet.getOpcode()) {
                            case 255:
                                Trade.removeItem(player, childButton, tradeItem.getId(),
                                        1);
                                break;
                            case 149:
                                Trade.removeItem(player, childButton, tradeItem.getId(),
                                        5);
                                break;
                            case 194:
                                Trade.removeItem(player, childButton, tradeItem.getId(),
                                        10);
                                break;
                            case 159:
                                Trade.removeItem(player, childButton, tradeItem.getId(),
                                        player.getTrade().getCount(tradeItem.getId()));
                                break;
                            case 148:
                                player.getInterfaceState().openEnterAmountInterface(Trade.TRADE_INVENTORY_INTERFACE, childButton, tradeItem.getId());
                                break;
                        }
                    }
                }
                break;
            case Trade.PLAYER_INVENTORY_INTERFACE:
                Item tradeItem = player.getInventory().get(childButton);
                if (tradeItem != null && tradeItem.getDefinition() != null) {
                    switch (packet.getOpcode()) {
                        case 255:
                            Trade.offerItem(player, childButton, tradeItem.getId(), 1);
                            break;
                        case 149:
                            Trade.offerItem(player, childButton, tradeItem.getId(), 5);
                            break;
                        case 194:
                            Trade.offerItem(player, childButton, tradeItem.getId(), 10);
                            break;
                        case 159:
                            Trade.offerItem(player, childButton, tradeItem.getId(), player.getInventory().getCount(tradeItem.getId()));
                            break;
                        case 148:
                            player.getInterfaceState().openEnterAmountInterface(Trade.PLAYER_INVENTORY_INTERFACE, childButton, tradeItem.getId());
                            break;
                    }
                }
                break;*/
            case Bank.PLAYER_INVENTORY_INTERFACE:
				switch (button) {
					case 3:
						if (menuIndex == 0 && childButton2 == 11941) {
							Item lootingBag = player.getInventory().get(childButton);
							if (lootingBag != null) {
								player.getActionSender().sendConfig(115, 2);
								return;
							}
						}
						if (childButton >= 0 && childButton < Inventory.SIZE) {
							switch (menuIndex) {
								case 1:
									Bank.depositInventory(player, childButton, childButton2, 1);
									break;
								case 2:
									Bank.depositInventory(player, childButton, childButton2, 5);
									break;
								case 3:
									Bank.depositInventory(player, childButton, childButton2, 10);
									break;
								case 10:
									Bank.depositInventory(player, childButton, childButton2, player.getSettings().getLastWithdrawnValue());
									break;
								case 5:
									player.getInterfaceState().openEnterAmountInterface(interfaceId, childButton, childButton2);
									break;
								case 6:
									Bank.depositInventory(player, childButton, childButton2, player.getInventory().getCount(childButton2));
									break;
								case 8:
									Item item = player.getInventory().get(childButton);
									if (item != null && item.getId() == 12791) {
										runePouchService.emptyPouch(player);
									}
									break;
							}
						}
						break;
					case 5:
						for (int i = 0; i < LootingBagServiceImpl.SIZE; i++) {
							Item item = player.getLootingBag().get(i);
							if (item != null) {
								lootingBagService.depositBank(player, i, item.getId(), player.getLootingBag().getCount(item.getId()));
							}
						}
						break;
					case 10:
						switch (menuIndex) {
							case 0:
								lootingBagService.depositBank(player, childButton, childButton2, 1);
								break;
							case 1:
								lootingBagService.depositBank(player, childButton, childButton2, 5);
								break;
							case 2:
								lootingBagService.depositBank(player, childButton, childButton2, player.getLootingBag().getCount(childButton2));
								break;

						}
						break;
				}
                break;
            case 201:
                if (childButton == 0) {
                    player.getEquipment().fireItemChanged(Equipment.SLOT_WEAPON);
                    player.getActionSender().sendSidebarInterfaces();
                    player.getActionSender().sendString(593, 2, "Combat lvl: " + player.getSkills().getCombatLevel());
                    return;
                }
                int idk = (player.getCombatState().getSpellBook() == SpellBook.MODERN_MAGICS.getSpellBookId() ? (childButton - 1) : childButton);
                switch (idk) {
                    case 0:
                        MagicCombatAction.setAutocast(player, Spell.WIND_STRIKE, config, false);
                        break;
                    case 1:
                        MagicCombatAction.setAutocast(player, Spell.WATER_STRIKE, config, false);
                        break;
                    case 2:
                        MagicCombatAction.setAutocast(player, Spell.EARTH_STRIKE, config, false);
                        break;
                    case 3:
                        MagicCombatAction.setAutocast(player, Spell.FIRE_STRIKE, config, false);
                        break;
                    case 4:
                        MagicCombatAction.setAutocast(player, Spell.WIND_BOLT, config, false);
                        break;
                    case 5:
                        MagicCombatAction.setAutocast(player, Spell.WATER_BOLT, config, false);
                        break;
                    case 6:
                        MagicCombatAction.setAutocast(player, Spell.EARTH_BOLT, config, false);
                        break;
                    case 7:
                        MagicCombatAction.setAutocast(player, Spell.FIRE_BOLT, config, false);
                        break;
                    case 8:
                        MagicCombatAction.setAutocast(player, Spell.WIND_BLAST, config, false);
                        break;
                    case 9:
                        MagicCombatAction.setAutocast(player, Spell.WATER_BLAST, config, false);
                        break;
                    case 10:
                        MagicCombatAction.setAutocast(player, Spell.EARTH_BLAST, config, false);
                        break;
                    case 11:
                        MagicCombatAction.setAutocast(player, Spell.FIRE_BLAST, config, false);
                        break;
                    case 12:
                        MagicCombatAction.setAutocast(player, Spell.WIND_WAVE, config, false);
                        break;
                    case 13:
                        MagicCombatAction.setAutocast(player, Spell.WATER_WAVE, config, false);
                        break;
                    case 14:
                        MagicCombatAction.setAutocast(player, Spell.EARTH_WAVE, config, false);
                        break;
                    case 15:
                        MagicCombatAction.setAutocast(player, Spell.FIRE_WAVE, config, false);
                        break;
                    case 16: //Cancel
                        player.getEquipment().fireItemChanged(Equipment.SLOT_WEAPON);
                        break;
                    case 17://magic dart
                        MagicCombatAction.setAutocast(player, Spell.MAGIC_DART, config, false);
                        break;
                    case 31:
                        MagicCombatAction.setAutocast(player, Spell.SMOKE_RUSH, config, false);
                        break;
                    case 32:
                        MagicCombatAction.setAutocast(player, Spell.SHADOW_RUSH, config, false);
                        break;
                    case 33:
                        MagicCombatAction.setAutocast(player, Spell.BLOOD_RUSH, config, false);
                        break;
                    case 34:
                        MagicCombatAction.setAutocast(player, Spell.ICE_RUSH, config, false);
                        break;
                    case 35:
                        MagicCombatAction.setAutocast(player, Spell.SMOKE_BURST, config, false);
                        break;
                    case 36:
                        MagicCombatAction.setAutocast(player, Spell.SHADOW_BURST, config, false);
                        break;
                    case 37:
                        MagicCombatAction.setAutocast(player, Spell.BLOOD_BURST, config, false);
                        break;
                    case 38:
                        MagicCombatAction.setAutocast(player, Spell.ICE_BURST, config, false);
                        break;
                    case 39:
                        MagicCombatAction.setAutocast(player, Spell.SMOKE_BLITZ, config, false);
                        break;
                    case 40:
                        MagicCombatAction.setAutocast(player, Spell.SHADOW_BLITZ, config, false);
                        break;
                    case 41:
                        MagicCombatAction.setAutocast(player, Spell.BLOOD_BLITZ, config, false);
                        break;
                    case 42:
                        MagicCombatAction.setAutocast(player, Spell.ICE_BLITZ, config, false);
                        break;
                    case 43:
                        MagicCombatAction.setAutocast(player, Spell.SMOKE_BARRAGE, config, false);
                        break;
                    case 44:
                        MagicCombatAction.setAutocast(player, Spell.SHADOW_BARRAGE, config, false);
                        break;
                    case 45:
                        MagicCombatAction.setAutocast(player, Spell.BLOOD_BARRAGE, config, false);
                        break;
                    case 46:
                        MagicCombatAction.setAutocast(player, Spell.ICE_BARRAGE, config, false);
                        break;

                    default:
                        Logger.getAnonymousLogger().info("Unhandled action button : " + interfaceId + " - " + button);
                        break;
                }
                break;
            case 593:
                Item weapon = player.getEquipment().get(Equipment.SLOT_WEAPON);
                String name;
                if (weapon == null) {
                    name = "Unarmed";
                } else {
                    name = weapon.getDefinition2().name;
                }
                String genericName = player.filterWeaponName(name).trim();
                switch (button) {
                    case 3:
                        if (name.equals("Unarmed")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (name.endsWith("whip") || name.contains("mouse") || name.endsWith("tentacle")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if ((name.contains("bow") || name.contains("ballista") || name.equals("seercull")) && !name.contains("karil") && !name.contains("c'bow")) {
                            player.getCombatState().setAttackType(AttackType.RANGE);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.contains("scimitar") || name.equals("excalibur") || name.equals("katana") || name.endsWith("light") || (genericName.contains("sword") && !name.contains("2h") && !name.contains("god") && !name.contains("saradomin"))) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (name.contains("staff") || name.contains("wand")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                            MagicCombatAction.setAutocast(player, null, -1, false);
                        } else if (genericName.startsWith("dart") || genericName.endsWith("knife") || genericName.endsWith("thrownaxe") || genericName.endsWith("javelin") || name.equals("toktz-xil-ul") || name.equals("Toxic blowpipe")) {
                            player.getCombatState().setAttackType(AttackType.RANGE);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.contains("mace") || name.endsWith("flail") || name.endsWith("anchor")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.startsWith("dagger") || name.contains("abyssal dagger")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.startsWith("pickaxe")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.startsWith("maul") || genericName.endsWith("warhammer") || name.endsWith("hammers") || name.equalsIgnoreCase("tzhaar-ket-om")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (name.contains("2h") || name.contains("godsword") || name.equals("saradomin sword")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.contains("axe") || genericName.contains("battleaxe")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.contains("claws")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.ACCURATE);
                        } else if (genericName.startsWith("halberd")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_1);
                        } else if (genericName.contains("spear") || genericName.contains("hasta")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_1);
                        }
                        player.getActionSender().sendConfig(108, 0);
                        break;
                    case 7:
                        if (name.equals("Unarmed")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (name.endsWith("whip") || name.contains("mouse") || name.endsWith("tentacle")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_2);
                        } else if ((name.contains("bow") || name.contains("ballista") || name.equals("seercull")) && !name.contains("karil") && !name.contains("c'bow")) {
                            player.getCombatState().setAttackType(AttackType.RANGE);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.contains("scimitar") || name.equals("excalibur") || name.equals("katana") || name.endsWith("light") || (genericName.contains("sword") && !name.contains("2h") && !name.contains("god") && !name.contains("saradomin"))) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (name.contains("staff") || name.contains("wand")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                            MagicCombatAction.setAutocast(player, null, -1, false);
                        } else if (genericName.startsWith("dart") || genericName.endsWith("knife") || genericName.endsWith("thrownaxe") || genericName.endsWith("javelin") || name.equals("toktz-xil-ul") || name.equals("Toxic blowpipe")) {
                            player.getCombatState().setAttackType(AttackType.RANGE);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.contains("mace") || name.endsWith("flail") || name.endsWith("anchor")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.startsWith("dagger") || name.contains("abyssal dagger")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.startsWith("pickaxe")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.startsWith("maul") || genericName.endsWith("warhammer") || name.endsWith("hammers") || name.equalsIgnoreCase("tzhaar-ket-om")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (name.contains("2h") || name.contains("godsword") || name.equals("saradomin sword")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.contains("axe") || genericName.contains("battleaxe")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.contains("claws")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.startsWith("halberd")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_1);
                        } else if (genericName.contains("spear") || genericName.contains("hasta")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_2);
                        }
                        player.getActionSender().sendConfig(108, 0);
                        break;
                    case 11:
                        if (genericName.contains("scimitar") || name.equals("excalibur") || name.equals("katana") || name.endsWith("light") || (genericName.contains("sword") && !name.contains("2h") && !name.contains("god") && !name.contains("saradomin"))) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_3);
                        } else if (genericName.contains("mace") || name.endsWith("flail") || name.endsWith("anchor")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_3);
                        } else if (genericName.startsWith("dagger") || name.contains("abyssal dagger")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_2);
                        } else if (genericName.startsWith("pickaxe")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_2);
                        } else if (name.contains("2h") || name.contains("godsword") || name.equals("saradomin sword")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_2);
                        } else if (genericName.contains("axe") || genericName.contains("battleaxe")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.AGGRESSIVE_2);
                        } else if (genericName.contains("claws")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_2);
                        } else if (genericName.contains("spear") || genericName.contains("hasta")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.CONTROLLED_3);
                        }
                        player.getActionSender().sendConfig(108, 0);
                        break;
                    case 15:
                        if (genericName.contains("scimitar") || name.equals("excalibur") || name.equals("katana") || name.endsWith("light") || (genericName.contains("sword") && !name.contains("2h") && !name.contains("god") && !name.contains("saradomin"))) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (name.equals("Unarmed")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (name.endsWith("whip") || name.contains("mouse") || name.endsWith("tentacle")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (name.contains("staff") || name.contains("wand")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                            MagicCombatAction.setAutocast(player, null, -1, false);
                        } else if (genericName.contains("mace") || name.endsWith("flail") || name.endsWith("anchor")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.startsWith("halberd")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if ((name.contains("bow") || name.contains("ballista") || name.equals("seercull")) && !name.contains("karil") && !name.contains("c'bow")) {
                            player.getCombatState().setAttackType(AttackType.RANGE);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.startsWith("maul") || genericName.endsWith("warhammer") || name.endsWith("hammers") || name.equalsIgnoreCase("tzhaar-ket-om")) {
                            player.getCombatState().setAttackType(AttackType.CRUSH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.startsWith("dagger") || name.contains("abyssal dagger")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.startsWith("pickaxe")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.startsWith("dart") || genericName.endsWith("knife") || genericName.endsWith("thrownaxe") || genericName.endsWith("javelin") || name.equals("toktz-xil-ul") || name.equals("Toxic blowpipe")) {
                            player.getCombatState().setAttackType(AttackType.RANGE);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (name.contains("2h") || name.contains("godsword") || name.equals("saradomin sword")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.contains("axe") || genericName.contains("battleaxe")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.contains("claws")) {
                            player.getCombatState().setAttackType(AttackType.SLASH);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        } else if (genericName.contains("spear") || genericName.contains("hasta")) {
                            player.getCombatState().setAttackType(AttackType.STAB);
                            player.getCombatState().setCombatStyle(CombatStyle.DEFENSIVE);
                        }
                        player.getActionSender().sendConfig(108, 0);
                        break;
                    case 30://special bar
                        player.getCombatState().inverseSpecial();
                        player.getActionSender().updateSpecialConfig();
                        if (player.getEquipment().get(Equipment.SLOT_WEAPON) != null && player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 1377) {
                            if (player.getCombatState().getSpecialEnergy() == 100) {
                                player.setForceChat("Raarrrrrgggggghhhhhhh!");
                                player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.FORCED_CHAT);
                                player.playAnimation(Animation.create(1056));
                                player.playGraphics(Graphic.create(246));
                                int modification = (int) Math.floor(5 + (player.getSkills().getLevelForExperience(Skills.STRENGTH) * 0.15));
                                player.getSkills().increaseLevelToMaximumModification(Skills.STRENGTH, modification);
                                player.getCombatState().decreaseSpecial(100);
                            }
                            player.getCombatState().inverseSpecial();
                            player.getActionSender().updateSpecialConfig();
                            return;
                        }
						 if (player.getEquipment().get(Equipment.SLOT_WEAPON) != null && player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 6739) {
                            if (player.getCombatState().getSpecialEnergy() == 100) {
                                player.setForceChat("Chop chop!");
                                player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.FORCED_CHAT);
                               // player.playAnimation(Animation.create(1056));
                               // player.playGraphics(Graphic.create(246));
                                player.getSkills().increaseLevelToMaximumModification(Skills.WOODCUTTING, 3);
                                player.getCombatState().decreaseSpecial(100);
                            }
                            player.getCombatState().inverseSpecial();
                            player.getActionSender().updateSpecialConfig();
                            return;
                        }
						if (player.getEquipment().get(Equipment.SLOT_WEAPON) != null && player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 11920) {
                            if (player.getCombatState().getSpecialEnergy() == 100) {
                                player.setForceChat("Smashing!");
                                player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.FORCED_CHAT);
                                player.playAnimation(Animation.create(7138));
                               // player.playGraphics(Graphic.create(246));
                                player.getSkills().increaseLevelToMaximumModification(Skills.MINING, 3);
                                player.getCombatState().decreaseSpecial(100);
                            }
                            player.getCombatState().inverseSpecial();
                            player.getActionSender().updateSpecialConfig();
                            return;
                        }
                        if (player.getActiveCombatAction() != null && (player.getActiveCombatAction().canSpecial(player, player.getInteractingEntity()))) {
                            player.getActiveCombatAction().special(player, player.getEquipment().get(Equipment.SLOT_WEAPON));
                        }
                        break;
                    case 24:
                        player.getCombatState().setQueuedSpell(null);
                        /*if (player.getAutocastSpell() != null) {
                            MagicCombatAction.setAutocast(player, null, -1, false);
                            return;
                        }*/
                        if (player.getEquipment().get(Equipment.SLOT_WEAPON) == null) {
                            player.getActionSender().sendMessage("You must be wielding a staff in order to autocast.");
                            return;
                        }
                        //if (player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 6914) {
                        //    return;
                        //}
                        int tab = (int) player.getAttribute("tabmode") == 161 ? 61 : (int) player.getAttribute("tabmode") == 164 ? 59 : 63;
                        switch (SpellBook.forId(player.getCombatState().getSpellBook())) {
                            case MODERN_MAGICS:
                                if (player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 4170 || player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 21255 || player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 11791 || player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 12904) {
                                    player.getActionSender().sendSidebarInterface(tab, 201);
                                    player.getActionSender().sendCS2Script(235, new Object[]{4170}, "o");
                                    player.getActionSender().sendAccessMask(2, 548, 48, -1, -1);
                                    player.getActionSender().sendAccessMask(2, 201, 0, 0, 47);
                                } else {
                                    if (player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 4675) {
                                        player.getActionSender().sendMessage("You can't autocast normal magics with an ancient staff.");
                                        return;
                                    }
                                    player.getActionSender().sendSidebarInterface(tab, 201);
                                    player.getActionSender().sendCS2Script(235, new Object[]{1409}, "o");
                                    player.getActionSender().sendAccessMask(2, 548, 48, -1, -1);
                                    player.getActionSender().sendAccessMask(2, 201, 0, 0, 47);
                                }
                                break;
                            case ANCIENT_MAGICKS:
                                if (player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 4675 || player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 21006 || player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == 6914 || CombatFormulae.fullAhrimDamned(player)) {
                                    player.getActionSender().sendSidebarInterface(tab, 201);
                                    player.getActionSender()
                                            .sendAccessMask(2, 201, 0, 0, 47)
                                            .sendAccessMask(2, 548, 48, -1, -1)
                                            .sendCS2Script(235, new Object[]{4675, 21006, 6914}, "o");
                                } else {
                                    player.getActionSender().sendMessage("You need a ancient's staff or a kodai wand to autocast ancient magicks.");
                                }
                                break;
                            case LUNAR_MAGICS:
                            	break;
                            case ARCEUUS_MAGICS:
                                break;
                        }
                        player.getInterfaceState().setOpenAutocastType(1);
                        break;
                    case 27:
                        player.getSettings().setAutoRetaliate(!player.getSettings().isAutoRetaliating());
                        player.getActionSender().updateAutoRetaliateConfig();
                        break;
                }
                break;
            case Bank.BANK_INVENTORY_INTERFACE:
                switch (button) {
                
                    case 25:
						player.getActionSender().sendConfig(115, 0);
                        boolean searching = player.getAttribute("bank_searching") != null;
                        player.setAttribute("bank_searching", !searching);
                        return;
                    case 29://27 116 - 29
                        player.getInventory().setFiringEvents(false);
                        player.getBank().setFiringEvents(false);
                        try {
                            for (int i = 0; i < Inventory.SIZE; i++) {
                                Item item = player.getInventory().get(i);
                                if (item != null) {
                                    Bank.depositInventory(player, i, item.getId(), player.getInventory().getCount(item.getId()), false);
                                }
                            }
                        } finally {
                            player.getInventory().setFiringEvents(true);
                            player.getBank().setFiringEvents(true);

                            player.getBank().fireItemsChanged();
                            player.getInventory().fireItemsChanged();
                        }
                        break;
                    case 31://29 116 - 31
                        for (int i = 0; i < Equipment.SIZE; i++) {
                            Item item = player.getEquipment().get(i);
                            if (item != null) {
                                Bank.depositEquipment(player, i, item.getId(), player.getEquipment().getCount(item.getId()));
                            }
                        }
                        break;
                    case 10:
                        if (packet.getOpcode() == 0) {
                            int tab = childButton - 9;
                            player.getBanking().collapseTab(tab);
                            player.getBank().fireItemsChanged();
                            break;
                        }
                        if (packet.getOpcode() == 255) {
                            int tab = childButton - 10;
							PlayerVariable lootVarp = PlayerVariable.of(4139);
							PlayerVariable tabVarp = PlayerVariable.of(4150);
							variableService.set(player, lootVarp, 0);
							variableService.set(player, tabVarp, tab);
							variableService.send(player, lootVarp);
							variableService.send(player, tabVarp);
                            player.getBanking().setCurrentTab(tab);
                            if (player.getAttribute("bank_searching") != null) {
                                player.getActionSender().sendCS2Script(101, new Object[]{11}, "i");
                                player.removeAttribute("bank_searching");
                            }
                        }
                        break;
                    case 16:
                    case 18:
                        player.getSettings().setSwapping(button == 16);
                        break;
                    case 21:
                    case 23:
                        player.getSettings().setWithdrawAsNotes(button == 23);
                        break;
                    case 34://Incinerator
                    	
                        break;
                    case 39://'Deposit worn items' button
                    	player.getActionSender().sendConfig(1053, player.getSettings().isEnablingWornItems() ? 1073741824 : 0);
                    	break;
                }
                player.getActionSender().updateBankConfig();
                if (childButton >= 0 && childButton < Bank.SIZE) {
                    switch (menuIndex) {
                        case 0:
                            Bank.withdraw(player, childButton, childButton2, 1);
                            break;
                        case 1:
                            Bank.withdraw(player, childButton, childButton2, 5);
                            break;
                        case 2:
                            Bank.withdraw(player, childButton, childButton2, 10);
                            break;
                        case 3:
                            Bank.withdraw(player, childButton, childButton2, player.getSettings().getLastWithdrawnValue());
                            break;
                        case 10:
                            player.getInterfaceState().openEnterAmountInterface(interfaceId, childButton, childButton2);
                            break;
                        case 5:
                            Bank.withdraw(player, childButton, childButton2, player.getBank().getCount(childButton2));
                            break;
                        case 6:
                            Bank.withdraw(player, childButton, childButton2, player.getBank().getCount(childButton2) - 1);
                            break;
                    }
                }
                break;
            case 261:
				if (button >= 24 && button <= 40) {
					int volume;
					if (button >= 24 && button <= 28) {
						volume = (28 - button);
						settings.setPlayerMusicVolume(volume);
					}
					if (button >= 30 && button <= 34) {
						volume = (34 - button);
						settings.setPlayerSoundEffectVolume(volume);
					}
					if (button >= 36 && button <= 40) {
						volume = (40 - button);
						settings.setPlayerAreaSoundVolume(volume);
					}
					player.getActionSender().updateSoundVolume();
					return;
				}
                switch (button) {
                    case 15:
                        settings.setPlayerScreenBrightness(1);
                        break;
                    case 16:
                        settings.setPlayerScreenBrightness(2);
                        break;
                    case 17:
                        settings.setPlayerScreenBrightness(3);
                        break;
                    case 18:
                        settings.setPlayerScreenBrightness(4);
                        break;
                    case 21:
                        player.getActionSender().sendInterface(60, false);
                        break;
                    case 67://66
                        player.getWalkingQueue().setRunningToggled(!player.getWalkingQueue().isRunningToggled());
                        player.getActionSender().updateRunningConfig();
                        break;
                    case 44:
                        player.getSettings().setSplitPrivateChat(!player.getSettings().splitPrivateChat());
                        player.getActionSender().updateSplitPrivateChatConfig();
                        break;
                    case 83:
                        settings.setPlayerAttackPriority(childButton - 1);
                        player.getActionSender().updateClickPriority();
                        break;
					case 84:
						settings.setNpcAttackPriority(childButton - 1);
						player.getActionSender().updateClickPriority();
						break;
					case 61:
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "Follower priority options have not <br>been implemented yet.");
						break;
					case 63:
						player.getActionSender().sendInterface(121, false);
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "Keybinding has not been implemented yet.");
						break;
					case 50://Notifications
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "Notifications has not been implemented yet.");
						break;
					case 75://House Options 68
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "House Options has not been implemented yet.");
						break;
					case 77://Bonds 70
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 13190, null, "Oldschool Bonds has not been implemented yet.");
						break;
					case 70:
						player.getSettings().setAcceptAid(!player.getSettings().isAcceptingAid());
						player.getActionSender().updateAcceptAidConfig();
						break;
					case 72://Run
						player.getWalkingQueue().setRunningToggled(!player.getWalkingQueue().isRunningToggled());
                        player.getActionSender().updateRunningConfig();
                        break;
                }
                break;
            case 60:
                switch (button) {
                    case 4:// transparent tabs
                        player.getActionSender().sendConfig(1055, 1);
                        break;
                    case 12:// transparent chatbox and tabs
                        player.setAttribute("transparentChat", (Boolean) player.getAttribute("transparentChat") ? false : true);
                        player.getActionSender().sendConfig(1055, (Boolean) player.getAttribute("transparentChat") ? 800 : 0);
                        break;
                    case 15:
                        if ((int) player.getAttribute("tabmode") != 548) {
                            if (player.getAttribute("tabArranged") != null) {
                                player.setAttribute("tabArranged", (Boolean) player.getAttribute("tabArranged") ? false : true);
                            } else {
                                player.setAttribute("tabArranged", true);
                            }
                            player.setAttribute("tabmode", (Boolean) player.getAttribute("tabArranged") ? TabMode.RESIZE.getPane() : TabMode.REARRANGED.getPane());
                            player.getActionSender().sendWindowPane((boolean) player.getAttribute("tabArranged") ? 161 : 164);
                            player.getActionSender().sendSidebarInterfaces();
                            player.getActionSender().sendAreaInterface(null, player.getLocation());
                        } else {
                            player.getActionSender().sendMessage("You must be in resizable before switching.");
                        }
                        break;
                }
                break;
            case 182:
                switch (button) {
                    case 1:
                    	//DialogueManager.openDialogue(player, 6969);
                    	
                		int pane = player.getAttribute("tabmode");
                    	int tabId = pane == 548 ? 73 : pane == 161 ? 73 : 73;
                    	player.getActionSender().sendSidebarInterface(tabId, 69);
//                    	sendAccessMask(1, 69, 7, 0, 419);// 17
//                    	sendAccessMask(0, 69, 14, 0, 420);
                    	player.getActionSender().sendConfig(477, 0);
        				//player.getActionSender().sendCS2Script(747, new Object[] {}, "");
        				//player.getActionSender().sendCS2Script(748, new Object[] {}, "");
            			player.sendAccess(Access.of(69, 7, NumberRange.of(0, 1), AccessBits.WORLDSWITCH));//419
            			player.sendAccess(Access.of(69, 14, NumberRange.of(0, 1), AccessBits.WORLDSWITCH));//420
                    	
//                    	int pane = player.getAttribute("tabmode");
//                    	int tabId = pane == 548 ? 73 : pane == 161 ? 73 : 73;
//                        player.getActionSender().sendSidebarInterface(tabId, 69);
//                    	//player.getActionSender().sendAccessMask(1, 69, 7, 0, 419);// 17
//                        player.sendAccess(Access.of(69, 7, NumberRange.of(0, 419), AccessBits.ALLOW_LEFT_CLICK));
//                        player.getActionSender().sendCS2Script(747, new Object[] {}, "");
//                        player.getActionSender().sendCS2Script(748, new Object[] {}, "");
//        				player.getActionSender().sendConfig(477, 0);
        				break;
                    case 6:
                        player.getActionSender().sendLogout();
                        break;
                }
                break;
            case 77:
                switch (button) 
                {
                    case 4:
                        Prayers.setQuickPrayer(player, childButton);
                        break;
                    case 5:
                        player.getActionSender().removeAllInterfaces().removeInterface2().sendSidebarInterfaces();
                        break;
                }
                break;
            case 160:
                switch (button) {
                    case 1:
                        switch (menuIndex) {
                             case 0:
                                if (player.getAttribute("orbOpen") == null) {
                                    player.setAttribute("orbOpen", true);
                                } else {
                                    player.setAttribute("orbOpen", !(Boolean) player.getAttribute("orbOpen"));
                                }

                                int pane = player.getAttribute("tabmode");
                                int tabId = pane == 548 ? 15 : pane == 161 ? 7 : 7;
                                if ((Boolean) player.getAttribute("orbOpen")) {
                                    player.getActionSender().sendInterface(pane, tabId, 122, true);
                                    player.getActionSender().sendConfig(1227, player.getSettings().getXPPosition());
                                } else {
                                    player.getActionSender().removeInterfaces(pane, tabId);
                                }
                                break;
                            case 1:
                                player.getActionSender().sendConfig(0, 638)
                                        .sendConfig(0, 262)
                                        .sendConfig(0, 261)
                                        .sendConfig(0, 638)
                                        .sendCS2Script(917, new Object[]{-1, -1}, "ii")
                                        .sendInterface(137, false)
                                        .sendGEAccess(1, 3, 137, 50, 2)
                                        .sendGEAccess(1, 3, 137, 51, 2)
                                        .sendGEAccess(1, 4, 137, 52, 2)
                                        .sendGEAccess(1, 32, 137, 53, 2)
                                        .sendGEAccess(1, 32, 137, 54, 2)
                                        .sendGEAccess(1, 8, 137, 55, 2)
                                        .sendGEAccess(1, 2, 137, 56, 2)
                                        .sendGEAccess(1, 3, 137, 57, 2)
                                        .sendGEAccess(0, 24, 137, 16, 2);
                                break;
                        }
                        break;
					/*case 4:
						if (player.getCombatState().getPoisonDamage() > 0) {
							Container con = player.getInventory();
							int anti = con.contains(179) ? 179 : con.contains(177) ? 177 : con.contains(175) ? 175 : con.contains(2446) ? 2446 : -1;
							if (anti != -1) {
								int slot = con.getSlotById(anti);
								Item antiPot = new Item(anti);
								player.getActionQueue().addAction(new ConsumeItemAction(player, antiPot, slot));
								return;
							}
						}
						break;*/
                    case 22:
                        player.getWalkingQueue().setRunningToggled(!player.getWalkingQueue().isRunningToggled());
                        player.getActionSender().updateRunningConfig();
                        break;
                    case 14:
                        switch (menuIndex) {
                            case 0: //activate
								if (player.getInterfaceState().getCurrentInterface() != -1) {
									player.getActionSender().sendMessage("Please finish what you're doing before proceeding.");
									return;
								}
                                player.getSettings().setQuicksActivated(!player.getSettings().getQuicksActive());
                                player.getCombatState().resetPrayers();
                                player.getActionSender().playSound(Sound.PRAYER_OFF);
                                if (!player.getSettings().getQuicksActive()) {
                                    player.getActionSender().sendConfig(375, 0);
                                    break;
                                }
                                for (int i = 0; i < player.getCombatState().getQuickPrayers().length; i++) {
                                    if (player.getCombatState().getQuickPrayers()[i] || player.getCombatState().getPrayers()[i]) {
                                        Prayers.activatePrayer(player, i);
                                    }
                                }
                                player.getActionSender().sendConfig(375, 1);
                                break;
                            case 1: //set
                                int pane = player.getAttribute("tabmode");
                                int tabId = pane == 548 ? 68 : pane == 161 ? 64 : 62;
                                player.getActionSender().sendSidebarInterface(tabId, 77);
                                player.getActionSender().sendAccessMask(2, 548, tabId, -1, -1)
                                        .sendAccessMask(2, 77, 4, 0, 26)
                                        .sendCS2Script(115, new Object[]{5}, "i")
                                        .switchTab(3);
                                break;
                        }
                        break;
                    //			case 1:
                    //				player.getSettings().setChatEffects(!player.getSettings().chatEffects());
                    //				player.getActionSender().updateChatEffectsConfig();
                    //				break;
                    //			case 2:
                    //				player.getSettings().setSplitPrivateChat(!player.getSettings().splitPrivateChat());
                    //				player.getActionSender().updateSplitPrivateChatConfig();
                    //				break;
                    //			case 3:
                    //				player.getSettings().setTwoMouseButtons(!player.getSettings().twoMouseButtons());
                    //				player.getActionSender().updateMouseButtonsConfig();
                    //				break;
                    //			case 4:
                    //				player.getSettings().setAcceptAid(!player.getSettings().isAcceptingAid());
                    //				player.getActionSender().updateAcceptAidConfig();
                    //				break;
                }
                break;
            case 541://271
                switch (button) {
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                    case 29:
                    case 30:
                    case 31:
                    case 32:
                        int buttonId = button - 4;
                        //int prayerButton = buttonId > 0 ? buttonId / 2 : buttonId;
                        Prayers.activatePrayer(player, buttonId);
                        break;
                }
                break;
            case 84:

                int slot = -1;

                if (button >= 11 && button <= 16) {
                    slot = button - 11;
                } else if (button == 17) {
                    slot = 7;
                } else if (button == 18) {
                    slot = 9;
                } else if (button == 19) {
                    slot = 10;
                } else if (button == 20) {
                    slot = 12;
                } else if (button == 21) {
                    slot = 13;
                }

                if (slot != -1) {
                    Item item = player.getEquipment().get(slot);

                    if (player.getInventory().add(item)) {
                        player.getEquipment().set(slot, null);
                        for (int i = 0; i < item.getEquipmentDefinition().getBonuses().length; i++) {
                            player.getCombatState().setBonus(i, player.getCombatState().getBonus(i) - item.getEquipmentDefinition().getBonus(i));
                        }
                        player.getActionSender().sendBonuses();
                    }
                }
                break;
            case Equipment.INTERFACE:
                switch (menuIndex) {
                	case 9:
                		if (button >= 6 && button < 17) {
                		if (packet.getOpcode() == 46) {
                			int id = 0;
                			Item item = new Item(id);
                			//if (item.getDefinition() != null) {
                				player.getActionSender().sendMessage("Nothing interesting happens.");//item.getDefinition().getExamine()
                		//	}
                            break;
                        }
                		}
                	break;
                    case 0:
                        if (button >= 6 && button < 17) {
                            Container equipment = player.getEquipment();
                            int index = 0;
                            if (button == 12) {
                                index = 7;
                            } else if (button == 15) {
                                index = 12;
                            } else if (button == 13) {
                                index = 9;
                            } else if (button == 14) {
                                index = 10;
                            } else if (button == 11) {
                                index = 5;
                            } else if (button == 16) {
                                index = 13;
                            }
                            Item item = equipment.get(index == 0 ? button - 6 : index);

                            if (player.getInventory().add(item)) {
								Talisman talisman = Talisman.getTalismanByTiara(item.getId());
								if (talisman != null) {
									if(item.getId() == talisman.getTiaraId()){
										player.getActionSender().sendConfig(491, 0);
									}
								}
                                player.getEquipment().set(
                                        index == 0 ? button - 6 : index, null);
                                player.getActionSender().sendBonuses();
                            } else {
                                player.getActionSender().sendMessage(
                                        "You're inventory is too full.");
                            }
                        }
                        switch (button) {
                           case 17:
                                player.getActionSender().sendInterfaceInventory(149);
                                player.getActionSender().sendInterface(84, false);
                                player.getActionSender().sendBonuses();
                                break;
                            case 19:
                                PriceChecker.open(player);
                                break;
                            case 21:
                            	//player.getActionSender().sendInterface(226, false);
                            	itemsKeptOnDeath = itemService.getItemsKeptOnDeath(player);
                                int count = 3;
                                if (player.getCombatState().getPrayer(Prayers.PROTECT_ITEM)) { // protect
                                    // item
                                    count++;
                                }
                                if (player.getCombatState().getSkullTicks() > 0) {
                                    count -= 3;
                                }

                                Object[] keptItems = new Object[]{
                                        -1,
                                        -1,
                                        "1010100 gp",
                                        0,
                                        0,
                                        (itemsKeptOnDeath[0].size() >= 4 && itemsKeptOnDeath[0]
                                                .get(3) != null) ? itemsKeptOnDeath[0].get(
                                                3).getId() : -1,
                                        (itemsKeptOnDeath[0].size() >= 3 && itemsKeptOnDeath[0]
                                                .get(2) != null) ? itemsKeptOnDeath[0].get(
                                                2).getId() : -1,
                                        (itemsKeptOnDeath[0].size() >= 2 && itemsKeptOnDeath[0]
                                                .get(1) != null) ? itemsKeptOnDeath[0].get(
                                                1).getId() : -1,
                                        (itemsKeptOnDeath[0].size() >= 1 && itemsKeptOnDeath[0]
                                                .get(0) != null) ? itemsKeptOnDeath[0].get(
                                                0).getId() : -1, count, 0};
                                player.getActionSender()
                                        .sendAccessMask(2, 4, 18, 0, 4)
                                        .sendAccessMask(2, 4, 21, 0, 42)
                                        .sendCS2Script(118, keptItems, "iiooooiisii")
                                        .sendInterface(4, true);//102
                                break;
                            case 23:
                            	player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null, "Call follower has not been implemented yet.");
                            	break;
                        }
                        break;
                    case 1:
                        switch (button) {
                            case 6: // Helmet check
                                Item helmet = player.getEquipment().get(Equipment.SLOT_HELM);
                                if (helmet == null) {
                                    break;
                                }

                                if (helmet.getId() == 11864 || helmet.getId() == 11865 || helmet.getId() == 19639 || helmet.getId() == 19643 || helmet.getId() == 19647 || helmet.getId() == 19641 || helmet.getId() == 19645 || helmet.getId() == 19649 || helmet.getId() == 21264 || helmet.getId() == 21266) {
                                    slayerService.sendCheckTaskMessage(player);
                                } else if (helmet.getId() == 12931 || helmet.getId() == 13199) {
                                    int charges = itemService.getCharges(player, helmet);

                                    if (charges < 0) {
                                        charges = 0;
                                    }

                                    player.getActionSender().sendMessage("Your "+helmet.getDefinition2().name+" has " + charges + " remaining charges.");
                                }
                                break;

                            case 8:
                                Item equip = player.getEquipment().get(Equipment.SLOT_AMULET);
                                if (equip == null) {
                                    break;
                                }
                                if (equip.getId() >= 1706 && equip.getId() <= 1712) {
                                    int charges = equip.getId() - 1704;
                                    player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                    player.getJewellery().gemTeleport(player, Location.create(3089, 3496, 0));
                                } else if (equip.getId() == 11976 || equip.getId() == 11978) {
                                    int charges = equip.getId() - 11966;
                                    player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                    player.getJewellery().gemTeleport(player, Location.create(3089, 3496, 0));
                                }
                                if (equip.getId() >= 3853 && equip.getId() <= 3867) {
                                    int charges = equip.getId() - 3851;
                                    int divided = charges / 2;
                                    player.getJewellery().setGem(GemType.GAMES_NECKLACE, 9 - divided, true);
                                    player.getJewellery().gemTeleport(player, Location.create(2926, 3559, 0));
                                }
                                break;
                            case 9:
                                weapon = player.getEquipment().get(Equipment.SLOT_WEAPON);
                                if (weapon != null && weapon.getId() == 12926) {
                                    int charges = itemService.getCharges(player, weapon);

                                    if (charges < 0) {
                                        charges = 0;
                                    }

                                    player.getActionSender().sendMessage("Your "+weapon.getDefinition2().name+" has " + charges + " remaining charges.");
                                } else if (weapon.getId() == 12006) {// Abyssal tentacle
                                    player.getActionSender().sendMessage("Your abyssal tentacle can perform "
                                            + NumberFormat.getInstance(Locale.ENGLISH).format(itemService.getCharges(player, weapon)) + " more attacks.");
                                }
                                break;
							case 11:
								equip = player.getEquipment().get(Equipment.SLOT_SHIELD);
								if (equip != null) {
									switch (equip.getId()) {
										case 11283:
										case 11284:
											if (player.getInteractingEntity() == null) {
												return;
											}
											DragonfireShield.dfsSpec(player,
													player.getInteractingEntity());
											break;
									}
								}
								break;

                            case 15:
                                equip = player.getEquipment().get(Equipment.SLOT_RING);
                                if (equip == null) {
                                    break;
                                }
								if (equip.getId() == 2550) {
									player.getActionSender().sendMessage(
													"<col=7f00ff>Your Ring of Recoil can deal "
															+ player.getCombatState()
															.getRingOfRecoil()
															+ " more points of damage before shattering.");
								}
                                if (equip.getId() >= 2552 && equip.getId() <= 2566) {
                                    int charges = equip.getId() - 2550;
                                    int divided = charges / 2;
                                    player.getJewellery().setGem(GemType.RING_OF_DUELING, 9 - divided, true);
                                    player.getJewellery().gemTeleport(player, Location.create(3316, 3235, 0));
                                } else if (equip.getId() >= 11866 && equip.getId() <= 11873) {// Slayer ring
                                    slayerService.sendCheckTaskMessage(player);
                                }
                                break;
                        }
                        break;
                    case 2:
                        switch (button) {
                            case 8:
                                Item equip = player.getEquipment().get(Equipment.SLOT_AMULET);
                                if (equip == null) {
                                    break;
                                }
                                if (equip.getId() >= 1706 && equip.getId() <= 1712) {
                                    int charges = equip.getId() - 1704;
                                    player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                    player.getJewellery().gemTeleport(player, Location.create(2918, 3176, 0));
                                } else if (equip.getId() == 11976 || equip.getId() == 11978) {
                                    int charges = equip.getId() - 11966;
                                    player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                    player.getJewellery().gemTeleport(player, Location.create(2918, 3176, 0));
                                }

                                if (equip.getId() >= 3853 && equip.getId() <= 3867) {
                                    int charges = equip.getId() - 3851;
                                    int divided = charges / 2;
                                    player.getJewellery().setGem(GemType.GAMES_NECKLACE, 9 - divided, true);
                                    player.getJewellery().gemTeleport(player, Location.create(2525, 3576, 0));
                                }

                                break;
							case 7:
								Item cape = player.getEquipment().get(Equipment.SLOT_CAPE);

								if (cape == null) {
									break;
								}
								if (cape.getId() == 9780 || cape.getId() == 9781 || Constants.isMaxCape(cape.getId())) {
									player.teleport(Location.create(2933, 3285), 0, 0, false);
								}
								if (cape.getId() == 13124) {
									player.teleport(Location.create(2661, 3371, 0), 0, 0, false);
								}
								break;
							case 11:
								equip = player.getEquipment().get(Equipment.SLOT_SHIELD);
								if (equip != null) {
									switch (equip.getId()) {
										case 11283:
										case 11284:
											player.getActionSender().sendMessage("Your Dragonfire Shield currently has " + player.dfsCharges + " Charges.");
											break;
									}
								}
								break;
                            case 15:
                                equip = player.getEquipment().get(Equipment.SLOT_RING);
                                if (equip == null) {
                                    break;
                                }
                                if (equip.getId() >= 2552 && equip.getId() <= 2566) {
                                    int charges = equip.getId() - 2550;
                                    int divided = charges / 2;
                                    player.getJewellery().setGem(GemType.RING_OF_DUELING, 9 - divided, true);
                                    player.getJewellery().gemTeleport(player, Location.create(2440, 3089, 0));
								} else if (equip.getId() >= 11866 && equip.getId() <= 11873) {
									if (Jewellery.rubItem(player, Equipment.SLOT_RING, equip.getId(), true)) {
										return;
									}
								}
                                break;
                        }
                        break;
                    case 3:
                        switch (button) {
                            case 8:
                                Item equip = player.getEquipment().get(Equipment.SLOT_AMULET);
                                if (equip == null) {
                                    break;
                                }
                                if (equip.getId() >= 1706 && equip.getId() <= 1712) {
                                    int charges = equip.getId() - 1704;
                                    player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                    player.getJewellery().gemTeleport(player, Location.create(3105, 3249, 0));
                                } else if (equip.getId() == 11976 || equip.getId() == 11978) {
                                    int charges = equip.getId() - 11966;
                                    player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                    player.getJewellery().gemTeleport(player, Location.create(3105, 3249, 0));
                                }
                                break;
                        }
                        break;
                    case 5:
                        switch (button) {
                            case 6:
                                Item helm = player.getEquipment().get(Equipment.SLOT_HELM);
                                if (helm == null) {
                                    break;
                                }

                                String itemName = helm.getDefinition2().getName();
                                if (itemName != null && itemName.contains("Slayer helmet")) {
                                    slayerService.openSlayerLog(player);
                                }
                                break;
                            case 15:// Ring
                                Item ring = player.getEquipment().get(Equipment.SLOT_RING);
                                if (ring == null) {
                                    break;
                                }
                               /* if (ring.getId() >= 2552 && ring.getId() <= 2566) {
                                    int charges = ring.getId() - 2550;
                                    int divided = charges / 2;
                                    player.getJewellery().setGem(GemType.RING_OF_DUELING, 9 - divided, true);
                                    player.getJewellery().gemTeleport(player, Location.create(3369, 3169, 0));
                                }*/
                                if (ring.getId() >= 11866 && ring.getId() <= 11873) {// Slayer ring
                                    slayerService.openSlayerLog(player);
                                }
                                break;
                        }
                        break;
                    case 10:
                        switch (button) {
                            case 8:
                                Item equip = player.getEquipment().get(Equipment.SLOT_AMULET);
                                if (equip == null) {
                                    break;
                                }
                                switch (equip.getId()) {
                                    case 1712:
                                    case 1710:
                                    case 1708:
                                    case 1706:
                                        int charges = equip.getId() - 1704;
                                        player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                        player.getJewellery().gemTeleport(player, Location.create(3293, 3163, 0));
                                        break;
                                    case 19707:
                                        int Echarges = equip.getId() - 19707;
                                        player.getJewellery().setGem(GemType.GLORY, Echarges / 1000, true);
                                        player.getJewellery().gemTeleport(player, Location.create(3293, 3163, 0));
                                        break;
                                    case 11976:
                                    case 11978:
                                        charges = equip.getId() - 11966;
                                        player.getJewellery().setGem(GemType.GLORY, charges / 2, true);
                                        player.getJewellery().gemTeleport(player, Location.create(3293, 3163, 0));
                                        break;
                                }
                                break;
                        }
                        break;
                }
                break;
            case 218:
                switch (button) {
                case 134:
                    Spell veng = Spell.forId(button, SpellBook.forId(player.getCombatState().getSpellBook()));
                    if (veng != null) {
                    MagicCombatAction.executeSpell(veng, player, player);
                    }
                    break;
                    case 1:
                    case 93://92
                    case 95://94
                    case 141:
						Spell teleport = Spell.forId(button, SpellBook.forId(player.getCombatState().getSpellBook()));
						
						if(button == 1)
							teleport = Spell.HOME_TELEPORT_MODERN;
						
						if (teleport != null && !BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "TutorialIsland")) {
							if(permissionService.is(player, PermissionService.PlayerPermissions.DONATOR))
								player.teleport(Constants.HOME_TELEPORT, 1, 1, true);
							else
							MagicCombatAction.executeSpell(teleport, player, player);
						} else {
							player.sendMessage("You cannot teleport off of Tutorial Island; "
									+ "please talk to Skippy to leave and claim your starter items if you're eligible.");
						}
                    	break;
                    	
                       // if (player.isInWilderness()) {
                       //     if (player.getCombatState().getLastHitTimer() > System
                       //             .currentTimeMillis() && player.getInventory().hasItem(new Item(563, 1)) && Location.getWildernessLevel(player, player.getLocation()) <= 20 && !player.getDatabaseEntity().getPlayerSettings().isTeleBlocked()) {
                       //         player.getInventory().remove(new Item(563, 1));
                        //        player.teleport(Constants.HOME_TELEPORT, 2, 3, false);
                        //    } else if (player.getCombatState().getLastHitTimer() < System
                        //            .currentTimeMillis() && Location.getWildernessLevel(player, player.getLocation()) <= 20) {
                        //        player.teleport(Constants.HOME_TELEPORT, 2, 3, false);
                        //    } else if (Location.getWildernessLevel(player, player.getLocation()) > 20) {
                        //        player.getActionSender().sendMessage("You cannot teleport above level 20 wilderness.");
                        //    } else {
                       //         player.getActionSender().sendMessage("You may not use the home teleport in combat.");
                        //    }
                      //  } else {
                      //      player.teleport(Constants.HOME_TELEPORT, 3, 3, false);
                     //   }
                      //  break;
                    case 4:
                        switch (SpellBook.forId(player.getCombatState().getSpellBook())) {
                            case MODERN_MAGICS:
                                player.getActionSender().sendBoltEnchantInterface();
                                break;
                        }
                        break;
                    //case 138://Arceuus home tele
                    //
                    //break;
                    case 172://Barrows teleport 169
                    	Teleporting.teleport(player, Spell.BARROWS_TELEPORT);
                    	break;
                    	
                    case 154://Respawn Teleport
                    	Teleporting.teleport(player, Spell.RESPAWN_TELEPORT);
                    	break;
                    	
                    case 16:
                        Teleporting.teleport(player, Spell.VARROCK_TELEPORT);
                        break;
                    case 19:
                        Teleporting.teleport(player, Spell.LUMBRIDGE_TELEPORT);
                        break;
                    case 22:
                        Teleporting.teleport(player, Spell.FALADOR_TELEPORT);
                        break;
                    case 27:
                        Teleporting.teleport(player, Spell.CAMELOT_TELEPORT);
                        break;
                    case 33:
                        Teleporting.teleport(player, Spell.ARDOUGNE_TELEPORT);
                        break;
                    case 38:
                        Teleporting.teleport(player, Spell.WATCHTOWER_TELEPORT);
                        break;
                    case 45:
                        Teleporting.teleport(player, Spell.TROLLHEIM_TELEPORT);
                        break;
					case 63:
					case 91:
					case 122:
						Spell home_teleport = Spell.forId(button, SpellBook.forId(player.getCombatState().getSpellBook()));
						if (home_teleport != null) {
							MagicCombatAction.executeSpell(home_teleport, player, player);
						}
						break;
                    case 83:
                    case 84:
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case 90:
                        Spell spell = Spell.forId(button, SpellBook.ANCIENT_MAGICKS);
                        if (spell != null) {
                            Teleporting.teleport(player, spell);
                        }
                        break;
                }
                if (button > 93 && button <= 134) {//LUNARS   button > 93 && button <= 134
                    Spell spell = Spell.forId(button - 1, SpellBook.forId(player.getCombatState().getSpellBook()));
                    if (spell != null) {
                        MagicCombatAction.executeSpell(spell, player, player);
                        return;
                    }
                }
                break;
            case 589:
                switch (button) {
                    case 9:
                        if (World.getWorld().clanIsRegistered(player.getName())) {
                            player.getActionSender().sendString(590, 29, player.getPrivateChat().getChannelName());
                        }
                        player.getActionSender().sendString(590, 31, player.getPrivateChat().getEntryRank().getText());
                        player.getActionSender().sendString(590, 33, player.getPrivateChat().getTalkRank().getText());
                        player.getActionSender().sendString(590, 35, player.getPrivateChat().getKickRank().getText());
                       // player.getActionSender().sendConfig(1083, (player.getPrivateChat().isCoinSharing() ? 1 : 0) << 18 | (player.getSettings().isLootsharing() ? 0 : 1));
                        player.getActionSender().sendInterface(590, true);
                        break;
                }
                break;
            case 590:
                switch (button) {
                    case 29:
                        switch (menuIndex) {
                            case 0:
                                player.getInterfaceState().openEnterTextInterface(590, "Enter chat prefix:");
                                break;
                            case 1:
                                player.getPrivateChat().setChannelName("");
                                player.getActionSender().sendString(590, 30, "Chat disabled");
                                break;
                        }
                        break;
                    case 31:
                        switch (menuIndex) {
                            case 0:
                                player.getPrivateChat().setEntryRank(EntryRank.ANYONE);
                                break;
                            case 1:
                                player.getPrivateChat().setEntryRank(EntryRank.ANY_FRIENDS);
                                break;
                            case 2:
                                player.getPrivateChat().setEntryRank(EntryRank.RECRUIT);
                                break;
                            case 3:
                                player.getPrivateChat().setEntryRank(EntryRank.CORPORAL);
                                break;
                            case 4:
                                player.getPrivateChat().setEntryRank(EntryRank.SERGEANT);
                                break;
                            case 5:
                                player.getPrivateChat().setEntryRank(EntryRank.LIEUTENANT);
                                break;
                            case 6:
                                player.getPrivateChat().setEntryRank(EntryRank.CAPTAIN);
                                break;
                            case 7:
                                player.getPrivateChat().setEntryRank(EntryRank.GENERAL);
                                break;
                            case 8:
                                player.getPrivateChat().setEntryRank(EntryRank.ONLY_ME);
                                break;
                        }
                        player.getActionSender().sendString(590, 31, player.getPrivateChat().getEntryRank().getText());
                        break;
                    case 33:
                        switch (menuIndex) {
                            case 0:
                                player.getPrivateChat().setTalkRank(TalkRank.ANYONE);
                                break;
                            case 1:
                                player.getPrivateChat().setTalkRank(TalkRank.ANY_FRIENDS);
                                break;
                            case 2:
                                player.getPrivateChat().setTalkRank(TalkRank.RECRUIT);
                                break;
                            case 3:
                                player.getPrivateChat().setTalkRank(TalkRank.CORPORAL);
                                break;
                            case 4:
                                player.getPrivateChat().setTalkRank(TalkRank.SERGEANT);
                                break;
                            case 5:
                                player.getPrivateChat().setTalkRank(TalkRank.LIEUTENANT);
                                break;
                            case 6:
                                player.getPrivateChat().setTalkRank(TalkRank.CAPTAIN);
                                break;
                            case 7:
                                player.getPrivateChat().setTalkRank(TalkRank.GENERAL);
                                break;
                            case 8:
                                player.getPrivateChat().setTalkRank(TalkRank.ONLY_ME);
                                break;
                        }
                        player.getActionSender().sendString(590, 33, player.getPrivateChat().getTalkRank().getText());
                        break;
                    case 35:
                        switch (menuIndex) {
                            case 0:
                                player.getPrivateChat().setKickRank(KickRank.CORPORAL);
                                break;
                            case 1:
                                player.getPrivateChat().setKickRank(KickRank.SERGEANT);
                                break;
                            case 2:
                                player.getPrivateChat().setKickRank(KickRank.LIEUTENANT);
                                break;
                            case 3:
                                player.getPrivateChat().setKickRank(KickRank.CAPTAIN);
                                break;
                            case 4:
                                player.getPrivateChat().setKickRank(KickRank.GENERAL);
                                break;
                            case 5:
                                player.getPrivateChat().setKickRank(KickRank.ONLY_ME);
                                break;
                        }
                        player.getActionSender().sendString(590, 35, player.getPrivateChat().getKickRank().getText());
                        break;
                }
                break;

            default:
                //logger.info("Unhandled action button : " + interfaceId + " - " + button + " - " + childButton);
                break;
        }

    }

}
