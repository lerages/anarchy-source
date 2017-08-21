package org.rs2server.rs2.packet;

import org.rs2server.Server;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.action.impl.SkillAction;
import org.rs2server.rs2.content.TeleportManager;
import org.rs2server.rs2.content.api.GameNpcActionEvent;
import org.rs2server.rs2.content.areas.CoordinateEvent;
import org.rs2server.rs2.domain.model.claim.ClaimType;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.ItemService;
import org.rs2server.rs2.domain.service.api.content.MonsterExamineService;
import org.rs2server.rs2.domain.service.api.content.PestControlService;
import org.rs2server.rs2.domain.service.api.content.ResourceArenaService;
import org.rs2server.rs2.domain.service.api.content.TournamentSuppliesService;
import org.rs2server.rs2.domain.service.api.content.bounty.BountyHunterService;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.Mob.InteractionMode;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction.Spell;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction.SpellBook;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction.SpellType;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.PriceChecker;
import org.rs2server.rs2.model.container.Trade;
import org.rs2server.rs2.model.map.path.SizedPathFinder;
import org.rs2server.rs2.model.map.path.astar.NpcReachedPrecondition;
import org.rs2server.rs2.model.map.path.astar.PlayerReachedPrecondition;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.npc.NPCDefinition;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.quests.impl.CooksAssistant;
import org.rs2server.rs2.model.quests.impl.CooksAssistantState;
import org.rs2server.rs2.model.quests.impl.DTStates;
import org.rs2server.rs2.model.quests.impl.DesertTreasure;
//import org.rs2server.rs2.model.quests.impl.LostCity;
//import org.rs2server.rs2.model.quests.impl.LostCityStates;
import org.rs2server.rs2.model.quests.impl.LunarDiplomacy;
import org.rs2server.rs2.model.quests.impl.LunarStates;
import org.rs2server.rs2.model.skills.ThievingAction;
import org.rs2server.rs2.model.skills.ThievingAction.PickpocketableNPC;
import org.rs2server.rs2.model.skills.fish.Fishing;
import org.rs2server.rs2.model.skills.hunter.PuroPuro;
import org.rs2server.rs2.net.Packet;
import org.rs2server.rs2.tickable.Tickable;

/**
 * Remove item options.
 *
 * @author Graham Edgecombe
 */
public class NPCOptionPacketHandler implements PacketHandler {

	private static final int OPTION_1 = 136, OPTION_2 = 212, OPTION_3 = 208, OPTION_TRADE = 52, OPTION_ATTACK = 45, OPTION_SPELL = 121, OPTION_ITEM_ON_NPC = 232, OPTION_EXAMINE = 202;

	private final ClaimService claimService;
	private final PlayerService playerService;
	private final SlayerService slayerService;
	private final HookService hookService;
	private final PathfindingService pathfindingService;
	private final PermissionService permissionService;
	private final BountyHunterService bountyHunterService;
	private final TournamentSuppliesService tournamentSuppliesService;
	private final PestControlService pestControlService;
	private final ItemService itemService;
	private final MonsterExamineService examineService;

	public NPCOptionPacketHandler() 
	{
		claimService = Server.getInjector().getInstance(ClaimService.class);
		playerService = Server.getInjector().getInstance(PlayerService.class);
		slayerService = Server.getInjector().getInstance(SlayerService.class);
		hookService = Server.getInjector().getInstance(HookService.class);
		pathfindingService = Server.getInjector().getInstance(PathfindingService.class);
		permissionService = Server.getInjector().getInstance(PermissionService.class);
		bountyHunterService = Server.getInjector().getInstance(BountyHunterService.class);
		tournamentSuppliesService = Server.getInjector().getInstance(TournamentSuppliesService.class);
		pestControlService = Server.getInjector().getInstance(PestControlService.class);
		itemService = Server.getInjector().getInstance(ItemService.class);
		examineService = Server.getInjector().getInstance(MonsterExamineService.class);
	}

	@Override
	public void handle(Player player, Packet packet) {
		if (player.getAttribute("cutScene") != null) {
			return;
		}
		if (player.getInterfaceAttribute("fightPitOrbs") != null) {
			return;
		}
		player.getActionSender().removeChatboxInterface();
		if (player.isLighting()) {
			return;
		}
		if (player.getInterfaceState().isInterfaceOpen(Equipment.SCREEN) || player.getInterfaceState().isInterfaceOpen(Bank.BANK_INVENTORY_INTERFACE) || player.getInterfaceState().isInterfaceOpen(Trade.TRADE_INVENTORY_INTERFACE) || player.getInterfaceState().isInterfaceOpen(Shop.SHOP_INVENTORY_INTERFACE) || player.getInterfaceState().isInterfaceOpen(PriceChecker.PRICE_INVENTORY_INTERFACE)) {
			player.getActionSender().removeInventoryInterface();
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();
		player.getInterfaceState().setOpenShop(-1);
		switch (packet.getOpcode()) {
			case OPTION_1:
				handleOption1(player, packet);
				break;
			case OPTION_2:
				handleOption2(player, packet);
				break;
			case OPTION_3:
				handleOption3(player, packet);
				break;
			case OPTION_TRADE:
				handleOptionTrade(player, packet);
				break;
			case OPTION_ATTACK:
				handleOptionAttack(player, packet);
				break;
			case OPTION_SPELL:
				handleOptionSpell(player, packet);
				break;
			case OPTION_ITEM_ON_NPC:
				handleOptionItemOnNpc(player, packet);
				break;
			case OPTION_EXAMINE:
				handleOptionExamine(player, packet);
				break;
		}
	}

	private void handleOptionItemOnNpc(Player player, Packet packet) {
		int a = packet.getInt();
		int itemId = packet.getLEShortA();
		int c = packet.getByte();
		int id = packet.getLEShort();
		int slot = packet.getLEShortA();
		Item item = player.getInventory().get(slot);
		if (id < 0 || id >= Constants.MAX_NPCS || item == null) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();

		final NPC npc = (NPC) World.getWorld().getNPCs().get(id);// where is combat following xd thats
		//need some sort of system to decide which tile to go to, to interact with the npc. ye find the object walking code object only works because you can't clip through it.
		int followX = npc.getLocation().getX();
		int followY = npc.getLocation().getY();
		if (player.getLocation().getY() < npc.getLocation().getY()) {
			followY--;
		} else if (player.getLocation().getY() > npc.getLocation().getY()) {
			followY++;
		} else if (player.getLocation().getX() < npc.getLocation().getX()) {
			followX--;
		} else if (player.getLocation().getX() > npc.getLocation().getX()) {
			followX++;
		}
		World.getWorld().doPath(new SizedPathFinder(true), player, followX, followY);

		if (npc != null) {
			Action action = null;

			action = new Action(player, 0) {// could just use the following
				@Override
				public AnimationPolicy getAnimationPolicy() {
					return AnimationPolicy.RESET_ALL;
				}

				@Override
				public CancelPolicy getCancelPolicy() {
					return CancelPolicy.ALWAYS;
				}

				@Override
				public StackPolicy getStackPolicy() {
					return StackPolicy.NEVER;
				}

				// bit till we reach the npc
				// then endGame following. ye
				@Override
				public void execute() {
					if (player.getCombatState().isDead()) {
						this.stop();
						return;
					}
					hookService.post(new GameNpcActionEvent(player, GameNpcActionEvent.ActionType.ITEM_ON_NPC, npc, item));
					switch (npc.getId()) {
						case 13:
							ResourceArenaService resourceArenaService = Server.getInjector().getInstance(ResourceArenaService.class);
							resourceArenaService.handleItemOnNPC(player, npc, item);
							break;
						case 1755:
							if (item.getId() == 6731 || item.getId() == 6733 || item.getId() == 6735 || item.getId() == 6737) {
								DialogueManager.openDialogue(player, 1755);
								player.setInterfaceAttribute("ring", item.getId());
							}
							break;
						case 401:
						case 402:
						case 403:
						case 404:
						case 6797:
						case 405:
							if (item.getId() == 11864) {
								DialogueManager.openDialogue(player, 11864);
							}
							break;

					}
					this.stop();
				}
			};

			if (action != null) {
				player.addCoordinateAction(player.getWidth(), player.getHeight(), npc.getLocation(), 1, 1, 1, action);
			}
		}
	}

	/**
	 * Handles npc option 1.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleOption1(final Player player, Packet packet) {
		packet.getByteC();
		int id = packet.getShort() & 0xFFFF;
		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();

		final NPC npc = (NPC) World.getWorld().getNPCs().get(id);
		Fishing fishing = Fishing.isAction(player, npc, 1);

		Pet.Pets pets = Pet.Pets.fromNpc(npc.getId());
		if (pets != null && npc.getInstancedPlayer() != player) {
			return;
		}
		player.setInteractingEntity(InteractionMode.TALK, npc);

		Action action;

		action = new Action(player, 0) {// could just use the following
			// bit till we reach the npc
			// then endGame following. ye
			@Override
			public void execute() {
				if (player.getCombatState().isDead()) {
					this.stop();
					return;
				}
				hookService.post(new GameNpcActionEvent(player, GameNpcActionEvent.ActionType.OPTION_1, npc));
				if (npc.getDefinition().getOptions()[0]
						.startsWith("Talk")) {
					player.setAttribute("talkingNpc", npc.getId());
					if (npc.getDefinition().getName().toLowerCase()
							.contains("banker")) {
						DialogueManager.openDialogue(player, 3227);
					}
				}
				if (fishing != null) {
					fishing.execute();
					Player player = (Player) getMob();
					player.submitTick("skill_action_tick", fishing, true);
				}
				switch (npc.getId()) {
				case 7050:
					DialogueManager.openDialogue(player, 21000);
					break;
				case 7374:
					Shop.open(player, 22, 0);
					break;
				case 1635:
				case 1636:
				case 1637:
				case 1638:
				case 1639:
				case 1640:
				case 1641:
				case 1642:
				case 1643:
				case 1644:					
					PuroPuro.catchImpling(player, npc);
		            break;
				case 3308:
					DialogueManager.openDialogue(player, 3308);
					break;
				//case 5514://Horvik
				//	DialogueManager.openDialogue(player, 14250);
				//	break;
		           
				case 5937: // Jarvald
					player.teleport(Location.create(2550, 3759, 0), 0 , 0 , false);
				break;
				case 1043: // Silk merchant
					DialogueManager.openDialogue(player, 16100);
					break;
				case 3194:
					Bank.open(player);
					break;
		         //IRONMEN SHOPS START
				case 6562:
					break;
				case 535://Horvik
					DialogueManager.openDialogue(player, 1425);
					break;
				case 1308://Diango
					//DialogueManager.openDialogue(player, 1425);
					break;
				//case 1400://Nulodian
					//DialogueManager.openDialogue(player, 1425);
					//break;
				case 536: //LOWE
					DialogueManager.openDialogue(player, 1435);
					break; 	
				case 532: //ZAFF
					DialogueManager.openDialogue(player, 1445);
					break;
				case 527: //ZEKE
					DialogueManager.openDialogue(player, 1415);
					break;
				case 505: //BOB
					DialogueManager.openDialogue(player, 1405);
					break;
				case 4920: //ALFONSE
					DialogueManager.openDialogue(player, 1475);
					break;
				//IRONMEN SHOPS END	
				case 1306: //MAKEOVER
					DialogueManager.openDialogue(player, 1455);
					break;
				case 508:
					Shop.open(player, 0, 0);	
					break;
					
				case 3220:
					Shop.open(player, 25, 0);
					break;
				case 4474:
					Shop.open(player, 11, 0);
					break;
				case 7502:
					Shop.open(player, 8, 0);
					break; 
				case 5419:
					DialogueManager.openDialogue(player, 7000);
					break;
				
				case 6586://Edmond / Untradables
					Shop.open(player, 11, 0);
					break;
				
					case 2180:
						DialogueManager.openDialogue(player, 2180);
						break;
					case 5919:
						DialogueManager.openDialogue(player, 5919);
						break;
					case 342:
						Shop.open(player, 19, 0);
						break;
					case 1833:
//						if (permissionService.isAny(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN, PermissionService.PlayerPermissions.IRON_MAN)) {
//							player.getActionSender().sendMessage("Sorry iron men may not use this store.");
//							return;
//						}
						Shop.open(player, 42, 0);
						break;
					case 1755:
						pestControlService.openShop(player);
						break;
					case 6481:
						DialogueManager.openDialogue(player, 6481);
						break;
					case 2914:
						DialogueManager.openDialogue(player, 2914);
						break;

					case 5074:
						DialogueManager.openDialogue(player, 5074);
						break;
					case 2040:
						DialogueManager.openDialogue(player, 2040);
						break;
					case 822:
						DialogueManager.openDialogue(player, 822);
						break;
					case 1340:
						DialogueManager.openDialogue(player, 1340);
						break;
					case 1602:
						if (player.getSettings().completedMageArena()) {
							Shop.open(player, 31, 0);
						} else {
							player.getActionSender().sendMessage("He doesn't seem to be interested in speaking with you.");
						}
						break;
					case 1603:
						DialogueManager.openDialogue(player, 1603);
						break;
					case 2460:
					case 3216:
					case 2473:
					case 6059:
					case 2578:
					case 2658:
					case 1044:
					case 118:
					case 5045:
					case 3193:
					case 5810:
					case 2913:
					case 3249:
					case 3343:
					case 4733:
					case 3226:
					//case 405:
					case 3363:
					case 5832:
					case 637:
						DialogueManager.openDialogue(player, 2000);
						break;
					case 4626:
						DialogueManager.openDialogue(player, 687);
						break;
					case 1158:
					//	LostCity lost = (LostCity) player.getQuests().get(LostCity.class);
					//	if (lost == null) {
					//		lost = new LostCity(player, LostCityStates.NOT_STARTED);
					//		player.getQuests().put(LostCity.class, lost);
					//	}
					//	lost.updateProgress();
					//	player.setAttribute("questnpc", true);
					//	break;
					case 684:
					case 1902:
						DesertTreasure quest = (DesertTreasure) player.getQuests().get(DesertTreasure.class);
						if (quest == null) {
							quest = new DesertTreasure(player, DTStates.NOT_STARTED);
							player.getQuests().put(DesertTreasure.class, quest);
						}
						quest.updateProgress();
						player.setAttribute("questnpc", true);
						break;
						
					case 3855:
					case 3839:
						LunarDiplomacy lunar = (LunarDiplomacy) player.getQuests().get(LunarDiplomacy.class);
						if (lunar == null) {
							lunar = new LunarDiplomacy(player, LunarStates.NOT_STARTED);
							player.getQuests().put(LunarDiplomacy.class, lunar);
						}
						lunar.updateProgress();
						player.setAttribute("questnpc", true);
						break;
					case 2461:
						if (player.getX() <= npc.getX()) {
							DialogueManager.openDialogue(player, 2461);
						}
						break;
					case 306:
						DialogueManager.openDialogue(player, 306);
						break;
					case 2182:
					case 1600:
						Bank.open(player);
						break;
					case 3231:
						player.getActionSender().sendTanningInterface();
						break;
					case 6092:
						//TeleportManager.handleTeleport(player);
						DialogueManager.openDialogue(player, 70); //OPTION 1
						break;
					case 7297:
					case 4159:
						TeleportManager.handleTeleport(player);
						break;
						/*skippy*/
					case 3320:
						DialogueManager.openDialogue(player, 101370);
						break;
					/* adam (regular ironman) */
					case 311:
						DialogueManager.openDialogue(player, 101360);
						break;
					/* juan (hardcore ironman) */
					case 3369:
						DialogueManager.openDialogue(player, 111360);
						break;
					/* paul (ultimate ironman) */
					case 317:
						DialogueManager.openDialogue(player, 1360);
						/*final boolean normalIronMan = permissionService.is(player, PermissionService.PlayerPermissions.IRON_MAN);
						final boolean ultimateIronMan = permissionService.is(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN);
						if (!normalIronMan && !ultimateIronMan) {
							DialogueManager.openDialogue(player, 1360);
							break;
						} else {
							if (claimService.hasClaimed(player, ClaimType.IRONMAN_ARMOUR)) {
								DialogueManager.openDialogue(player, 1359);
							} else {
								DialogueManager.openDialogue(player, 1358);
								if (normalIronMan) {
									playerService.giveItem(player, new Item(12810, 1), true);
									playerService.giveItem(player, new Item(12811, 1), true);
									playerService.giveItem(player, new Item(12812, 1), true);
								} else {
									playerService.giveItem(player, new Item(12813, 1), true);
									playerService.giveItem(player, new Item(12814, 1), true);
									playerService.giveItem(player, new Item(12815, 1), true);
								}
								claimService.claim(player, ClaimType.IRONMAN_ARMOUR);
							}
						}*/
						
						//DialogueManager.openDialogue(player, 1360);
						break;

					/* Ranged combat tutor */
					case 3217:
						if (claimService.hasClaimedByIpAddress(player, ClaimType.STARTER_KIT_RANGED)) {
							DialogueManager.openDialogue(player, 1350);
						} else {
							DialogueManager.openDialogue(player, 1349);
							playerService.giveItem(player, new Item(1167, 1), true);
							playerService.giveItem(player, new Item(1129, 1), true);
							playerService.giveItem(player, new Item(1095, 1), true);
							playerService.giveItem(player, new Item(1063, 1), true);
							playerService.giveItem(player, new Item(841, 1), true);
							playerService.giveItem(player, new Item(882, 250), true);
							playerService.giveItem(player, new Item(1478, 1), true);
							claimService.claim(player, ClaimType.STARTER_KIT_RANGED);
						}
						break;

					/* Magic combat tutor */
					case 3218:
						if (claimService.hasClaimedByIpAddress(player, ClaimType.STARTER_KIT_MAGIC)) {
							DialogueManager.openDialogue(player, 1352);
						} else {
							DialogueManager.openDialogue(player, 1351);
							playerService.giveItem(player, new Item(558, 50), true);
							playerService.giveItem(player, new Item(556, 250), true);
							playerService.giveItem(player, new Item(555, 125), true);
							playerService.giveItem(player, new Item(554, 125), true);
							playerService.giveItem(player, new Item(557, 125), true);
							playerService.giveItem(player, new Item(1727, 1), true);
							playerService.giveItem(player, new Item(577, 1), true);
							playerService.giveItem(player, new Item(579, 1), true);
							playerService.giveItem(player, new Item(1011, 1), true);
							claimService.claim(player, ClaimType.STARTER_KIT_MAGIC);
						}
						break;

					case 401:
					case 403:
					case 490:
					case 402:
					case 6797:
					case 404:
					case 405:
					case 6798:
						DialogueManager.openDialogue(player, 6990);
						break;
					case 3666://super mem
						DialogueManager.openDialogue(player, 3666);
						break;
					case 5979:
						DialogueManager.openDialogue(player, 64);
						break;
					case 514:
						DialogueManager.openDialogue(player, 36);
						break;
					case 516:
						DialogueManager.openDialogue(player, 40);
						break;
					case 518:
						DialogueManager.openDialogue(player, 44);
						break;
					case 519:
						DialogueManager.openDialogue(player, 51);
						break;
					case 659: //FlowerGame
						DialogueManager.openDialogue(player, 13371);
						break;
					case 315:
						DialogueManager.openDialogue(player, 55);
						break;
						
						/* PET DIALOGUE - MOD ZAROS*/
					case 5536: //Vet'ion Jr. (Vet'ion)			
						DialogueManager.openDialogue(player, 16000);
						break;
					case 497: //Callisto Cub (Callisto)		
						DialogueManager.openDialogue(player, 16050);
						break;
					case 495: //Venenatis Spiderling (Venenatis)		
						DialogueManager.openDialogue(player, 16060);
						break;
					case 5547: //Scorpia's Offpsring (Scorpia)		
						DialogueManager.openDialogue(player, 16064);
						break;
					case 6652: //Prince black dragon (King Black Dragon)		
						DialogueManager.openDialogue(player, 16071);
						break;
					case 5907: //Pet Chaos Elemental (Chaos Elemental)		
						DialogueManager.openDialogue(player, 16075);
						break;	
					case 2127:
					case 2128: //Pet Snakeling (Zulrah)
					case 2129:		
						DialogueManager.openDialogue(player, 16078);
						break;
					case 6643: //Pet kree'arra (Kree'arra)	
						DialogueManager.openDialogue(player, 16085);
						break;	
					case 6644: //Pet general graardor (General Graardor)	
						DialogueManager.openDialogue(player, 16092);
						break;	
					case 6646: //Pet zilyana (Commander Zilyana)	
						DialogueManager.openDialogue(player, 16095);
						break;	
					case 6647: //Pet k'ril tsutsaroth (K'ril Tsutsaroth)
						DialogueManager.openDialogue(player, 16500);
						break;
					case 6626: //Pet dagannoth supreme (Dagannoth Supreme)
						DialogueManager.openDialogue(player, 16550);
						break;	
					case 6627: //Pet dagannoth prime (Dagannoth Prime)
						DialogueManager.openDialogue(player, 16600);
						break;	
					case 6641: //Pet dagannoth rex (Dagannoth Rex)
						DialogueManager.openDialogue(player, 16650);
						break;
					case 6651: //Baby Mole (Giant Mole)
						DialogueManager.openDialogue(player, 16700);
						break;
					case 6653: //kalphite princess (Kalphite Queen)
					case 6654:
						DialogueManager.openDialogue(player, 16750);
						break;
					case 388: //Dark Core (Corporeal Beast)
						DialogueManager.openDialogue(player, 16800);
						break;
					case 7519: //Olmlet (The Great Olm)
						DialogueManager.openDialogue(player, 16850);
						break;
					case 7368: //Phoenix (Wintertodt)
						DialogueManager.openDialogue(player, 16900);
						break;
					case 6642: //Penance Pet (Barbarian Assault Gamble)
						DialogueManager.openDialogue(player, 16950);
						break;
					case 5892: //TzRek-Jad (TzTok-Jad // Firecape Gamble)
						DialogueManager.openDialogue(player, 17000);
						break;
					case 6656: //Pet kraken (Kraken)
						DialogueManager.openDialogue(player, 17050);
						break;
					case 6655: //pet smoke devil (Thermonuclear Smoke Devil)
						DialogueManager.openDialogue(player, 17100);
						break;
					case 964: //Hellpuppy (Cerberus)
						DialogueManager.openDialogue(player, 17150);
						break;
					case 5883: //Abyssal orphan (Abyssal Sire)
						DialogueManager.openDialogue(player, 17200);
						break;
					case 425: //Skotos (Skotizo)
						DialogueManager.openDialogue(player, 17250);
						break;
					case 4001: //Chompy Chick (Chompy Bird hunting)
						DialogueManager.openDialogue(player, 17300);
						break;
					case 7334: //Giant squirrel (Agility)
						DialogueManager.openDialogue(player, 17350);
						break;
					case 7336: //Rocky (Thieving)
						DialogueManager.openDialogue(player, 17400);
						break;
					case 7335: //Tangleroot (Farming)
						DialogueManager.openDialogue(player, 17450);
						break;
					case 6716: //Rock golem (Mining)
						DialogueManager.openDialogue(player, 17500);
						break;
					case 6715: //Heron (Fishing)
						DialogueManager.openDialogue(player, 17550);
						break;
					case 6717: //Beaver (Woodcutting)
						DialogueManager.openDialogue(player, 17600);
						break;
					case 7337:
					case 7338:
					case 7339:
					case 7340:
					case 7341:
					case 7342:
					case 7343:
					case 7344: //Rift Guardian (Runecrafting)
					case 7345:
					case 7346:
					case 7347:
					case 7348:
					case 7349:
					case 7350:
						DialogueManager.openDialogue(player, 17650);
						break;	
					case 6718:
					case 6719: //Baby chinchompa (hunting)
					case 6720:
					case 6721:
						DialogueManager.openDialogue(player, 17700);
						break;	
					case 8296: //Bloodhound (Master Clue Scroll)		
						DialogueManager.openDialogue(player, 17750);
						break;						
				}
				this.stop();
			}

			@Override
			public AnimationPolicy getAnimationPolicy() {
				return AnimationPolicy.RESET_ALL;
			}

			@Override
			public CancelPolicy getCancelPolicy() {
				return CancelPolicy.ALWAYS;
			}

			@Override
			public StackPolicy getStackPolicy() {
				return StackPolicy.NEVER;
			}
		};
//		if (fishing != null && pathfindingService.travelToNpc(player, npc)) {
//            interactFishing(player, fishing, npc);
//			//player.addCoordinateAction(player.getWidth(), player.getHeight(), npc.getLocation(), 1, 1, 1, action, fishing);
//		} else {
		interact(player, action, npc);
		//}
	}

	/**
	 * Handles npc option 2.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleOption2(final Player player, Packet packet) {
		int id = packet.getShortA() & 0xFFFF;
		packet.getByteS();
		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();

		if (player.getAttribute("isStealing") != null) {
			return;
		}
		final NPC npc = (NPC) World.getWorld().getNPCs().get(id);

		player.getActionSender().sendDebugPacket(packet.getOpcode(), "NpcOpt2", new Object[]{"ID: " + npc.getDefinition().getId(), "Index: " + id});
		player.setInteractingEntity(InteractionMode.TALK, npc);

		Action action;

		PickpocketableNPC npcP = ThievingAction.NPCS.get(npc.getDefinition().getId());
		if (npcP != null) {
			action = new ThievingAction(player, npc);
		} else {
			action = new Action(player, 0) {
				@Override
				public void execute() {
					if (player.getCombatState().isDead()) {
						this.stop();
						return;
					}
					hookService.post(new GameNpcActionEvent(player, GameNpcActionEvent.ActionType.OPTION_2, npc));
					//						if(npc.getDefinition().getOptions()[3].startsWith("Bank")) {
					//							Bank.open(player);
					//						} else if (npc.getDefinition().getOptions()[3].startsWith("Trade")) {
					//							String scriptName = "tradeWith" + npc.getDefinition().getId();
					//							if(!ScriptManager.getScriptManager().invokeWithFailTest(scriptName, player, npc)) {
					//								player.getActionSender().sendMessage(npc.getDefinedName() + " does not want to trade.");
					//							} else {
					//								npc.setInteractingEntity(InteractionMode.TALK, player);
					//							}
					//						}
					//						if (npc.getDefinition().getOptions()[3].startsWith("Trade")) {
					//							npc.setInteractingEntity(InteractionMode.TALK, player);
					//						}
					switch (npc.getId()) {
					/*case 6904:
					case 535://Horvik
						Shop.open(player, 45, 0);
						break;*/
					case 505: //BOB
						Shop.open(player, 17,  0);
						break;
					case 514://Shopkeeper
					case 515:
					case 508:
					case 509:
						Shop.open(player, 1, 0);//Supplies
						break;
					case 6904:
					case 317: //Paul
						DialogueManager.openDialogue(player, 101362);
						break;
					case 311: //adam
						DialogueManager.openDialogue(player, 101361);
						break;
					case 3369: //Jaun
						DialogueManager.openDialogue(player, 101363);
						break;
					case 535://Horvik
						Shop.open(player, 4, 0);
						break;
					case 7374:
						Shop.open(player, 22, 0);
						break;
					case 6092: //last teleport
						//TeleportManager.handleTeleport(player);
						boolean donator = permissionService.is(player, PermissionService.PlayerPermissions.DONATOR);
						if(donator || !donator && player.getInventory().getCount(995) >= 10000)
						{
							if(player.getSettings().getLastLocation() !=null)
							{
								player.teleport(player.getSettings().getLastLocation(), 0, 0, true);
								if(!donator)
								{
									player.getInventory().remove(new Item(995, 10000));
									player.sendMessage("The gnome glider charges you 10,000 coins to take you to your previous destination.");
								}
								else if(donator)
									player.sendMessage("The gnome glider takes you to your previous destination for free.");
							} else {
								player.sendMessage("You don't have a previous location that you can teleport to.");
							}
						
						} else {
							player.sendMessage("You need 10,000 coins to teleport to your previous location.");
						}
						break;
					case 1306:
						DialogueManager.openDialogue(player, 1458);
						break;
					case 7492:
						Shop.open(player, 6, 0);
						break; 
					case 502:
						Shop.open(player, 14, 0);
						break;
					case 403:
					case 490:
					case 405:
					case 402:
					case 404:
					case 401:
					case 6798:
					case 6797:
						Shop.open(player, 12, 0);
						break;
					case 6562:
						//Shop.open(player, 25, 0);
						break;
					case 7608:
						Shop.open(player, 10, 0);
						break; 
					case 7502:
						Shop.open(player, 9, 0);
						break; 
						
					case 534://Thessalia
						player.getActionSender().sendInterface(269, false);
						break;
//					case 534://Thessalia
//						player.getActionSender().sendMessage("Nothing interesting happens.");
//						break;
//					case 6904:
//						Shop.open(player, 45, 0);
//						break;
						case 687:
							//Shop.open(player, 23, 2);
							break;
//						case 535:
//							//Shop.open(player, 16, 0);
//							break;
						case 2182:
						case 1600:
							Bank.open(player);
							break;
						case 5419:
							DialogueManager.openDialogue(player, 7000);
							break;
						case 637:
							npc.playAnimation(Animation.create(722));
							npc.playGraphics(Graphic.create(343, 0, 0));
							npc.forceChat("Senventior Disthine Molenko!");
							World.getWorld().submit(new Tickable(2) {

								@Override
								public void execute() {
									this.stop();
									player.teleport(Location.create(2899, 4818, 0), 0, 0, true);
								}

							});
							break;
					}
					this.stop();
				}

				@Override
				public AnimationPolicy getAnimationPolicy() {
					return AnimationPolicy.RESET_ALL;
				}

				@Override
				public CancelPolicy getCancelPolicy() {
					return CancelPolicy.ALWAYS;
				}

				@Override
				public StackPolicy getStackPolicy() {
					return StackPolicy.NEVER;
				}
			};
		}

		interact(player, action, npc);
	}


	public void handleOption3(Player player, Packet packet) {
		int id = packet.getShort() & 0xFFFF;
		packet.get();

		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();
		
		

		if (player.getAttribute("isStealing") != null) {
			return;
		}
		final NPC npc = (NPC) World.getWorld().getNPCs().get(id);
		
		if (npc != null) {
			player.setInteractingEntity(InteractionMode.TALK, npc);

			Action action = new Action(player, 0) {
				@Override
				public void execute() {
					this.stop();
					hookService.post(new GameNpcActionEvent(player, GameNpcActionEvent.ActionType.OPTION_3, npc));
					switch (npc.getId()) {
					case 317: //Paul
						DialogueManager.openDialogue(player, 101362);
						break;
					case 311: //adam
						DialogueManager.openDialogue(player, 101361);
						break;
					case 3369: //Jaun
						DialogueManager.openDialogue(player, 101363);
						break;
					
					
					case 7608:
						Shop.open(player, 9, 0);
						break;
					case 508:
						Shop.open(player, 17, 0);
						break;
					}
					if (npc.getId() == 315) {// emblem trader skulling
						DialogueManager.openDialogue(player, 101059);
						//player.getCombatState().setSkullTicks(1000);
						//player.getActionSender().sendMessage("The Emblem Trader marks you with a skull.");
					} else if (npc.getId() == 402 || npc.getId() == 403 || npc.getId() == 490 || npc.getId() == 6797 || npc.getId() == 401 || npc.getId() == 6798 || npc.getId() == 404 || npc.getId() == 405) {// slayer master rewards interface
						slayerService.openRewardsScreen(player);
					}
				}
				
				@Override
				public AnimationPolicy getAnimationPolicy() {
					return AnimationPolicy.RESET_ALL;
				}

				@Override
				public CancelPolicy getCancelPolicy() {
					return CancelPolicy.ALWAYS;
				}

				@Override
				public StackPolicy getStackPolicy() {
					return StackPolicy.NEVER;
				}
			};
			if (player.getLocation().distanceToEntity(player, npc) > 1) {
				player.addCoordinateAction(player.getWidth(), player.getHeight(), npc.getLocation(), 1, 1, 1, action);
				int followX = npc.getLocation().getX();
				int followY = npc.getLocation().getY();
				if (player.getLocation().getY() < npc.getLocation().getY()) {
					followY--;
				} else if (player.getLocation().getY() > npc.getLocation().getY()) {
					followY++;
				} else if (player.getLocation().getX() < npc.getLocation().getX()) {
					followX--;
				} else if (player.getLocation().getX() > npc.getLocation().getX()) {
					followX++;
				}
				World.getWorld().doPath(new SizedPathFinder(true), player, followX, followY);//this is what steven told me 2 implement but it walks under the npc ;l

			} else {
				player.getActionManager().appendAction(action);
			}
		}
	}

	private void handleOptionTrade(final Player player, Packet packet) {
		int id = packet.getLEShort() & 0xFFFF;
		packet.getByteC();
		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();

		if (player.getAttribute("isStealing") != null) {
			return;
		}
		final NPC npc = (NPC) World.getWorld().getNPCs().get(id);
		

		//pathfindingService.travelToNpc(player, npc);
		Fishing fishing = Fishing.isAction(player, npc, 2);
		player.getActionSender().sendDebugPacket(packet.getOpcode(), "NpcOptTrade", new Object[]{"ID: " + npc.getDefinition().getId(), "Index: " + id});
		Pet.Pets pets = Pet.Pets.fromNpc(npc.getId());
		if (pets != null && npc.getInstancedPlayer() != player) {
			return;
		}
		player.setInteractingEntity(InteractionMode.TALK, npc);
		Action action;
		PickpocketableNPC npcP = ThievingAction.NPCS.get(npc.getDefinition().getId());
		if (npcP != null) {
			action = new ThievingAction(player, npc);
		} else {
			action = new Action(player, 0) {
				@Override
				public void execute() {
					if (player.getCombatState().isDead()) {
						this.stop();
						return;
					}
					Pet.Pets pets = Pet.Pets.fromNpc(npc.getId());
					if (pets != null && player.getInventory().size() < player.getInventory().capacity()) {
						PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
						if (player.getPet() != null && npc.getInstancedPlayer() == player) {
							player.playAnimation(Animation.create(827));
							player.getActionSender().playSound(Sound.PICKUP);
							if (player.getInventory().add(new Item(pets.getItem()))) {
								World.getWorld().unregister(player.getPet());
								settings.setPetSpawned(false);
								player.setPet(null);
							}
							return;
						}
					}
					hookService.post(new GameNpcActionEvent(player, GameNpcActionEvent.ActionType.OPTION_TRADE, npc));
					/*if (npc.getDefinition().getOptions()[2].startsWith("Bank")) {
						Bank.open(player);
					}*/
					if (fishing != null) {
						fishing.execute();
						Player player = (Player) getMob();
						player.submitTick("skill_action_tick", fishing, true);
					}
					switch (npc.getId()) {
						case 1833:
//							if (permissionService.isAny(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN, PermissionService.PlayerPermissions.IRON_MAN)) {
//								player.getActionSender().sendMessage("Sorry iron men may not use this store.");
//								return;
//							}
//							Shop.open(player, 42, 0);
							break;
						case 2180:
							player.setInterfaceAttribute("gamble_firecape", true);
							player.getActionSender().sendEnterAmountInterface();
							break;
						case 1308://Diango
							Shop.open(player, 2, 0);
							break;
						case 403:
						case 490:
						case 405:
						case 402:
						case 404:
						case 401:
						case 6798:
						case 6797:
							player.setAttribute("talkingNpc", npc.getId());
							DialogueManager.openDialogue(player, 512);
							break;
						case 2462:
							Shop.open(player, 23, 0);
							break;
						case 502:
							Shop.open(player, 15, 0);
							break;
						case 315:
							player.getActionSender().sendInterface(178, true);
							break;
						case 1755:
							pestControlService.openShop(player);
							break;
						case 394:
						case 3194:
							Bank.open(player);
							break;
						case 3193:
							Shop.open(player, 18, 0);
							break;
						case 5919:
							Shop.open(player, 16, 0);
							break;
							
						case 7492:
							Shop.open(player, 7, 0);
							break; 
						case 17:
							Shop.open(player, 17, 0);
							break;
						case 7608:
							Shop.open(player, 8, 0);
							break; 
							
						case 7502:
							Shop.open(player, 10, 0);
						break;
							
						/*case 315://Emblem Trader (BH Shop)
							Shop.open(player, 12, 0);
							break;*/
						case 6964:
							Shop.open(player, 39, 0);
							break;
						case 2183:
							Shop.open(player, 22, 0);
							break;
						case 2184:
							Shop.open(player, 21, 0);
							break;
						case 2185:
							Shop.open(player, 24, 0);
							break;
//						case 535:
//							Shop.open(player, 16, 0);
//							break;
						case 1602:
							if (player.getSettings().completedMageArena()) {
								Shop.open(player, 31, 0);
							} else {
								player.getActionSender().sendMessage("He doesn't seem to be interested in speaking with you.");
							}
							break;
						case 1043: // Silk merchant
							DialogueManager.openDialogue(player, 16100);
							break;
						case 1601:
							Shop.open(player, 14, 0);
							break;
						case 3935:
							Shop.open(player, 13, 0);
							break;
						case 1051:
							Shop.open(player, 34, 0);
							break;
						case 1301:
							Shop.open(player, 28, 0);
							break;
						case 637:
							Shop.open(player, 13, 0);
							break;
					    //IRONMEN SHOPS START
						case 6562:
							break;
						case 535://Horvik
							Shop.open(player, 3, 0);
							break;
						case 1400://NULODIAN
							Shop.open(player, 1, 0);
							break;
						case 536: //LOWE
							Shop.open(player, 5, 0);
							break; 	
						case 532: //ZAFF
							Shop.open(player, 7, 0);
							break;
						case 527: //ZEKE
							Shop.open(player, 8, 0);
							break;
						case 505: //BOB
							Shop.open(player, 9,  0);
							break;
						case 4920: //ALFONSE
							Shop.open(player, 11,  0);
							break;
						//IRONMEN SHOPS END	
						case 1306://Makeover mage
							DialogueManager.openDialogue(player, 1455);
							break;
						case 3231:
							player.getActionSender().sendTanningInterface();
							break;
						case 1045:
							Shop.open(player, 11, 0);
							break;
						case 537:
							Shop.open(player, 21, 0);
							break;
						case 6092:
							//TeleportManager.handleTeleport(player);
							DialogueManager.openDialogue(player, 74); //right click
							break;
						case 514:
						case 515:
						case 508:
						case 509:
							Shop.open(player, 0, 1);
							break;
						case 516:
							Shop.open(player, 1, 1);
							break;
						case 518:
							Shop.open(player, 2, 1);
							break;
						case 519:
							Shop.open(player, 3, 1);
							break;
						case 534://Thessalia
							//player.getActionSender().sendInterface(269, false);
							break;
					}
					this.stop();
				}

				@Override
				public AnimationPolicy getAnimationPolicy() {
					return AnimationPolicy.RESET_ALL;
				}

				@Override
				public CancelPolicy getCancelPolicy() {
					return CancelPolicy.ALWAYS;
				}

				@Override
				public StackPolicy getStackPolicy() {
					return StackPolicy.NEVER;
				}
			};
		}

//		if (fishing != null && pathfindingService.travelToNpc(player, npc)) {
//            interactFishing(player, fishing, npc);
//			//player.addCoordinateAction(player.getWidth(), player.getHeight(), npc.getLocation(), 1, 1, 1, action, fishing);
//		} else {
		interact(player, action, npc);
		//}
	}

	/**
	 * Handles npc attack option.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleOptionAttack(final Player player, Packet packet) {
		boolean bool = (packet.getByteS() & 0xFF) == 1;
		final int id = packet.getShortA() & 0xFFFF;
		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();
		player.getActionSender().removeChatboxInterface();

		final NPC npc = (NPC) World.getWorld().getNPCs().get(id);
		if (npc != null) {
			if (npc.getCombatState().isDead()) {
				return;
			}
			if (npc.getInstancedPlayer() != null && npc.getInstancedPlayer() != player) {
				player.getActionSender().sendMessage("This NPC was not spawned for you.");
				return;
			}
			player.getCombatState().setQueuedSpell(null);
			player.getCombatState().startAttacking(npc, true);//in this case its an npc. thats what  i just said
		}
	}

	/**
	 * Handles npc spell option.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleOptionSpell(final Player player, Packet packet) {
		int a = packet.getByteA();
		int interfaceHash = packet.getLEInt();
		int interfaceId = (interfaceHash >> 16);
		int childButton = interfaceHash & 0xFFFF;
		int id = packet.getLEShort();
		packet.getShort();
		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearRemovableActions();

		NPC npc = (NPC) World.getWorld().getNPCs().get(id);

		int spellOffset = player.getCombatState().getSpellBook() == SpellBook.ANCIENT_MAGICKS.getSpellBookId() ? -2 : -1;
		int spellId = (childButton) + (spellOffset);
		player.getActionSender().sendDebugPacket(packet.getOpcode(), "NpcSpell", new Object[]{"ID: " + npc.getDefinition().getId(), "Index: " + id, "Spell Id: " + spellId});
		Spell spell = Spell.forId(spellId, SpellBook.forId(player.getCombatState().getSpellBook()));
		if (spell != null) {
			if (spell.getSpellType() == SpellType.NON_COMBAT) {
				return;
			}
			player.setAttribute("magicMove", true);
			player.setAttribute("castSpell", spell);
			//MagicCombatAction.setAutocast(player, null, -1, false);
			player.getCombatState().setQueuedSpell(spell);
			player.getCombatState().startAttacking(npc, false);
		}
	}

	/**
	 * Handles npc option examine.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleOptionExamine(Player player, Packet packet) {
		int id = packet.getLEShort() & 0xFFFF;
		if (id < 0 || id >= Constants.MAX_NPCS) {
			return;
		}
		if (player.getCombatState().isDead()) {
			return;
		}

		player.getActionSender().sendDebugPacket(packet.getOpcode(), "NpcExamine", new Object[]{"ID: " + id});

		//examineService.openMonsterExamine(player, id);
		NPCDefinition npcDef = NPCDefinition.forId(id);
		if (npcDef != null) {
			if(permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR))
				player.getActionSender().sendMessage(npcDef.getDescription() + " (ID: " + id + ")");
				else
					player.getActionSender().sendMessage(npcDef.getDescription());
		}
	}

	private void interact(Player player, Action action, NPC npc) {
		final NpcReachedPrecondition reached = new NpcReachedPrecondition(npc);

		if (reached.targetReached(player.getX(), player.getY(), npc.getX(), npc.getY())) {
			player.getActionQueue().addAction(action);
			return;
		}


		if (pathfindingService.travelToNpc(player, npc)) {
			final Action submit = action;
			final WalkingQueue.Point target = player.getWalkingQueue().isEmpty() ?
					new WalkingQueue.Point(player.getLocation().getX(), player.getLocation().getY(), -1) : player.getWalkingQueue().getWaypoints().getLast();

			// Npc location can change since we start pathing, so we copy it here
			final Location npcTarget = npc.getLocation().transform(0, 0, 0);
			int size = npc.getSize();
			World.getWorld().submitAreaEvent(player, new CoordinateEvent(player, target.getX(), target.getY(), size) {
				@Override
				public void execute() {
					if (npc.getId() == 4626 || reached.targetReached(player.getLocation().getX(), player.getLocation().getY(), npcTarget.getX(), npcTarget.getY())) {
						player.getActionQueue().addAction(submit);
					}
				}
			});
		}
	}
}
