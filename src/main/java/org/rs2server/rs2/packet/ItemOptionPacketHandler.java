package org.rs2server.rs2.packet;

import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.action.impl.ConsumeItemAction;
import org.rs2server.rs2.content.BossKillLog;
import org.rs2server.rs2.content.Jewellery;
import org.rs2server.rs2.content.Magic;
import org.rs2server.rs2.content.SlayerHelmAction;
import org.rs2server.rs2.content.Teleporting;
import org.rs2server.rs2.content.Jewellery.GemType;
import org.rs2server.rs2.content.api.GameItemInventoryActionEvent;
import org.rs2server.rs2.content.api.GamePlayerItemDropEvent;
import org.rs2server.rs2.content.areas.CoordinateEvent;
import org.rs2server.rs2.content.misc.BabyImpJar;
import org.rs2server.rs2.content.misc.BronzeSet;
import org.rs2server.rs2.content.misc.Casket;
import org.rs2server.rs2.content.misc.BagFullOfGems;
import org.rs2server.rs2.content.misc.DragonfireShield;
import org.rs2server.rs2.content.misc.Ectophial;
import org.rs2server.rs2.content.misc.GodBooks;
import org.rs2server.rs2.content.misc.HerbBox;
import org.rs2server.rs2.content.misc.MysteryBox;
import org.rs2server.rs2.content.misc.SupplyCrate;
import org.rs2server.rs2.content.misc.YoungImpJar;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.model.player.treasuretrail.clue.ClueScrollType;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.ItemService;
import org.rs2server.rs2.domain.service.api.content.LootingBagService;
import org.rs2server.rs2.domain.service.api.content.MaxCapeService;
import org.rs2server.rs2.domain.service.api.content.magic.OrbChargingService;
import org.rs2server.rs2.domain.service.api.loot.LootGenerationService;
import org.rs2server.rs2.domain.service.impl.GroundItemServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.Consumables.Drink;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.container.LootingBag;
import org.rs2server.rs2.model.container.Trade;
import org.rs2server.rs2.model.event.ClickEventManager;
import org.rs2server.rs2.model.event.EventListener.ClickOption;
import org.rs2server.rs2.model.map.path.DefaultPathFinder;
import org.rs2server.rs2.model.map.path.ProjectilePathFinder;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.Firemaking;
import org.rs2server.rs2.model.skills.FletchingAction;
import org.rs2server.rs2.model.skills.FletchingAction.FletchingGroup;
import org.rs2server.rs2.model.skills.FletchingAction.FletchingItem;
import org.rs2server.rs2.model.skills.FletchingAction.FletchingType;
import org.rs2server.rs2.model.skills.PestleAndMortar;
import org.rs2server.rs2.model.skills.crafting.*;
import org.rs2server.rs2.model.skills.crafting.BoltCrafting.BoltTip;
import org.rs2server.rs2.model.skills.crafting.GemCutting.Gem;
import org.rs2server.rs2.model.skills.herblore.Herblore;
import org.rs2server.rs2.model.skills.herblore.Herblore.Herb;
import org.rs2server.rs2.model.skills.herblore.Herblore.HerbloreType;
import org.rs2server.rs2.model.skills.herblore.Herblore.PrimaryIngredient;
import org.rs2server.rs2.model.skills.herblore.Herblore.SecondaryIngredient;
import org.rs2server.rs2.model.skills.herblore.SuperCombatPotion;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.Packet;
import org.rs2server.rs2.tickable.StoppingTick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Remove item options.
 *
 * @author Graham Edgecombe
 */
public class ItemOptionPacketHandler implements PacketHandler {

	/**
	 * The logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ItemOptionPacketHandler.class);

	/**
	 * Option drop/destroy opcode.
	 */
	private static final int OPTION_DROP_DESTROY = 183;

	/**
	 * Option pickup opcode.
	 */
	private static final int OPTION_PICKUP = 5;

	/**
	 * Option examine opcode.
	 */
	private static final int OPTION_EXAMINE = 116;

	/**
	 * Option 1 opcode.
	 */
	private static final int OPTION_1 = 149;

	/**
	 * Option 2 opcode.
	 */
	private static final int OPTION_2 = 72;

	/**
	 * Option 3 opcode.
	 */
	private static final int OPTION_3 = 159;

	/**
	 * Option 4 opcode.
	 */
	private static final int OPTION_4 = 73;

	/**
	 * Option 5 opcode.
	 */
	private static final int OPTION_5 = 46;

	/**
	 * Click 1 opcode.
	 */
	private static final int CLICK_1 = 198;

	/**
	 * Item on item opcode.
	 */
	private static final int ITEM_ON_ITEM = 176;

	/**
	 * Magic on item opcode.
	 */
	private static final int MAGIC_ON_ITEM = 44;

	/**
	 * Magic on grund item
	 */
	private static final int MAGIC_ON_GROUND_ITEM = 74;

	private final PermissionService permissionService;
	private final SlayerService slayerService;
	private final PlayerService playerService;
	private final HookService hookService;
	private final ItemService itemService;
	private final PathfindingService pathfindingService;
	private final LootingBagService lootingBagService;
	private final GroundItemService groundItemService;

	public ItemOptionPacketHandler() {
		slayerService = Server.getInjector().getInstance(SlayerService.class);
		playerService = Server.getInjector().getInstance(PlayerService.class);
		hookService = Server.getInjector().getInstance(HookService.class);
		itemService = Server.getInjector().getInstance(ItemService.class);
		pathfindingService = Server.getInjector().getInstance(PathfindingService.class);
		permissionService = Server.getInjector().getInstance(PermissionService.class);
		lootingBagService = Server.getInjector().getInstance(LootingBagService.class);
		groundItemService = Server.getInjector().getInstance(GroundItemService.class);
	}

	@Override
	public void handle(Player player, Packet packet) {
		if (player.getAttribute("cutScene") != null || player.getAttribute("busy") != null) {
			return;
		}
		if (player.isLighting()) {
			return;
		}
		switch (packet.getOpcode()) {
			case OPTION_DROP_DESTROY:
				handleItemOptionDrop(player, packet);
				break;
			case OPTION_PICKUP:
				handleItemOptionPickup(player, packet);
				break;
			case OPTION_EXAMINE:
				handleItemOptionExamine(player, packet);
				break;
			case OPTION_1:
				handleItemOption1(player, packet);
				break;
			case OPTION_2:
				handleItemOption2(player, packet);
				break;
			case OPTION_3:
				handleItemOption3(player, packet);
				break;
			case OPTION_4:
				handleItemOption4(player, packet);
				break;
			case OPTION_5:
				handleItemOption5(player, packet);
				break;
			case CLICK_1:
				handleItemOptionClick1(player, packet);
				break;
			case ITEM_ON_ITEM:
				handleItemOptionItem(player, packet);
				break;
			case MAGIC_ON_ITEM:
				handleMagicOnItem(player, packet);
				break;
			case MAGIC_ON_GROUND_ITEM:
				handleMagicOnGroundItem(player, packet);
				break;
		}
	}

	private void handleMagicOnGroundItem(Player player, Packet packet) {
		/*client.encryptedBuffer.writeOpcode(74);
		client.encryptedBuffer.putByteC((KeyFocusListener.field_cn_1676[82] ? 1 : 0));
		client.encryptedBuffer.putInt((-1782496813 * Class_cc.field_iw_779));
		client.encryptedBuffer.putLEShort((client.field_if_2843 * -1524676247), (byte) 2);
		client.encryptedBuffer.putLEShort(((1693061403 * Class_q.originX) + i), (byte) 2);
		client.encryptedBuffer.putShort(((-1593604783 * Class_gf.originY) + i_4_));
		client.encryptedBuffer.putShortA(i_6_, (byte) -54);*/
		//0, 14286868, -1, 2572, 3313, 451
		packet.getByteC();
		int interfaceHash = packet.getInt();
		int interfaceId = interfaceHash >> 16;
		int child = interfaceHash & 0xFFFF;
		int c = packet.getLEShort();
		int x = packet.getLEShort();
		int y = packet.getShort();
		int itemId = packet.getShortA();
		Location location = Location.create(x, y, player.getZ());
		if (interfaceId == 218 && child == 20 && player.getCombatState().getSpellBook() == MagicCombatAction.SpellBook.MODERN_MAGICS.getSpellBookId()) {
			Action action = new Action(player, 0) {
				@Override
				public CancelPolicy getCancelPolicy() {
					return CancelPolicy.ONLY_ON_WALK;
				}

				@Override
				public StackPolicy getStackPolicy() {
					return StackPolicy.NEVER;
				}

				@Override
				public AnimationPolicy getAnimationPolicy() {
					return AnimationPolicy.RESET_ALL;
				}

				@Override
				public void execute() {
					Optional<GroundItemService.GroundItem> groundItemOptional = groundItemService.getGroundItem(itemId, location);
					if (!groundItemOptional.isPresent()) {
						this.stop();
						return;
					}
					GroundItemService.GroundItem groundItem = groundItemOptional.get();
					if (player.getLocation().distance(groundItem.getLocation()) > 6 || !ProjectilePathFinder.clearPath(player.getLocation(), groundItem.getLocation())) {
						pathfindingService.travel(player, groundItem.getLocation());
						return;
					}
					player.getWalkingQueue().reset();
					Magic magic = new Magic(player);
					magic.handleTeleGrab(groundItem);
					this.stop();
				}
			};
			player.getActionQueue().addAction(action);
		}
	}


	/**
	 * Handles item option drop.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOptionDrop(Player player, Packet packet) {
		int id = packet.getLEShort();
		int slot = packet.getLEShort();
		int interfaceHash = packet.getInt();
		int interfaceId = interfaceHash >> 16;
		if (player.getCombatState().isDead() || player.getInterfaceState().isInterfaceOpen(Bank.BANK_INVENTORY_INTERFACE)) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		player.getActionSender().sendDebugPacket(packet.getOpcode(), "ItemDrop", new Object[]{"ID: " + id});

		player.getActionSender().removeAllInterfaces().removeInterface2();
		switch (interfaceId) {
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					Item item = player.getInventory().get(slot);
					if (item != null && item.getId() != id) {
						return;
					}
					if (player.getBountyHunter() != null) {
						if (item.getDefinition().getStorePrice() >= 1000) {
							player.getActionSender().sendMessage("You can't drop an item with a value of 1,000 coins or more.");
							return;
						}
					}

					// Clue scrolls should be destroyed, not dropped.
					for (ClueScrollType clueScroll : ClueScrollType.values()) {
						if (id == clueScroll.getClueScrollItemId()) {
							player.getActionSender().sendDestroyItem(item);
							return;
						}
					}

					if (item.getId() == 12926) {
						player.getActionSender().sendMessage("Please empty this item before dropping it.");
						return;
					}
					if (item.getId() == 12019 && player.getDatabaseEntity().getCoalBagAmount() > 0) {
						player.getActionSender().sendMessage("Please empty your Coal bag before dropping it.");
						return;
					}
					if (item.getId() == 12020 && player.getDatabaseEntity().getGemBag().size() > 0) {
						player.getActionSender().sendMessage("Please empty your Gem bag before dropping it.");
						return;
					}
					if (item.getId() == 12791 && player.getRunePouch().size() > 0) {
						player.getActionSender().sendMessage("Please empty your pouch before dropping this item.");
						return;
					}
					if (item.getId() == 13197 || item.getId() == 13199) {
						int charges = itemService.getCharges(player, item);
						if (charges == 0) {
							player.getActionSender().sendMessage("Your " + item.getDefinition2().name + " is empty.");
							return;
						}
						if (charges >= 1) {
							CacheItemDefinition def = CacheItemDefinition.get(itemService.getChargedItem(player, item));
							if (def != null) {
								Item chargedItem = new Item(def.getId(), charges);
								if (player.getInventory().add(chargedItem)) {
									itemService.setChargesWithItem(player, item, chargedItem, -1);
									itemService.setCharges(player, item, -1);
									itemService.degradeItem(player, item);
									player.getActionSender().sendMessage("You unload " + charges + "x " + def.name + " from the " + item.getDefinition2().name);
								}
							}
						}
						return;
					}
					if (item.getId() == 13196) {
						DialogueManager.openDialogue(player, 12929);
						return;
					}
					if (item.getId() == 13198) {
						DialogueManager.openDialogue(player, 12932);
						return;
					}
					if (item.getId() == 12436) {
						if (player.getInventory().freeSlots() >= 3) {
							if (player.getInventory().add(new Item(6585)) && player.getInventory().add(new Item(12526))) {
								player.getInventory().remove(new Item(12436));
							}
						} else {
							player.getActionSender().sendMessage("Not enough inventory space to do this.");
						}
						return;
					}
					if (item.getId() == 11941 && player.getLootingBag().size() > 0) {
						player.getActionSender().sendMessage("Please empty the Looting bag before dropping it.");
						return;
					}

					MaxCapeService maxCapeService = Server.getInjector().getInstance(MaxCapeService.class);
					if (maxCapeService.destroyMaxCape(player, item)) {
						return;
					}

					Pet.Pets petIds = Pet.Pets.from(item.getId());
					PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
					if (petIds != null) {
						if (player.getPet() != null) {
							player.getActionSender().sendMessage("You may only have one pet out at a time.");
							return;
						} else {
							Pet pet = new Pet(player, petIds.getNpc());
							settings.setPetId(petIds.getNpc());
							settings.setPetSpawned(true);
							player.getActionSender().playSound(Sound.PICKUP);
							player.setPet(pet);
							World.getWorld().register(pet);
							player.getInventory().remove(item);
							return;
						}
					}
					if (item.getId() == 12931 || item.getId() == 12904 || item.getId() == 12899) {
						int charges = itemService.getCharges(player, item);
						if (charges == 0) {
							player.getActionSender().sendMessage("Your " + item.getDefinition2().name + " is empty.");
							return;
						}
						if (charges >= 1) {
							CacheItemDefinition def = CacheItemDefinition.get(itemService.getChargedItem(player, item));
							if (def != null) {
								Item chargedItem = new Item(def.getId(), charges);
								if (player.getInventory().add(chargedItem)) {
									itemService.setChargesWithItem(player, item, chargedItem, -1);
									itemService.setCharges(player, item, -1);
									itemService.degradeItem(player, item);
									player.getActionSender().sendMessage("You unload " + charges + "x " + def.name + " from the " + item.getDefinition2().name);
								}
							}
						}
						return;
					}


					player.getInventory().remove(item, slot);
					if(item.getId() == 995)
					{
						player.getActionSender().playSound(Sound.DROP_COINS);
					}
					else
					{
						player.getActionSender().playSound(Sound.DROP);
					}
//					GroundItemController.createGroundItem(item, player, player.getLocation());
					GroundItemService groundItemService = Server.getInjector().getInstance(GroundItemService.class);
					GroundItemService.GroundItem groundItem = new GroundItemService.GroundItem(item, player.getLocation(), player, false);
					groundItemService.createGroundItem(player, groundItem);
					hookService.post(new GamePlayerItemDropEvent(player, groundItem));
					player.playAnimation(Animation.create(-1));
					 //World.getWorld().createGroundItem(new GroundItem(player.getName(), item, player.getLocation()), player);
				}
				break;
			default:
				logger.info("Unhandled item drop option : " + interfaceId + " - " + id + " - " + slot);
				break;
		}
	}

	/**
	 * Handles item option pickup.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOptionPickup(final Player player, Packet packet) {
		int id = packet.getShortA();
		int x = packet.getLEShortA();
		packet.get();
		int y = packet.getShort();
		if (player.getCombatState().isDead()) {
			logger.info("Dead player {} cannot pickup ground item.", player.getName());
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		final Location location = Location.create(x, y, player.getLocation().getZ());
		player.resetInteractingEntity();
		Item item = new Item(id);
		if (!player.getLocation().equals(location)) {
			pathfindingService.travel(player, location);
		}
		GroundItemService groundItemService = Server.getInjector().getInstance(GroundItemService.class);
		groundItemService.pickupGroundItem(player, item, location);
//		GroundItemController.pickupGroundItem(location, id, player);
//        Action action = new Action(player, 0) {
//            @Override
//            public CancelPolicy getCancelPolicy() {
//                return CancelPolicy.ALWAYS;
//            }
//
//            @Override
//            public StackPolicy getStackPolicy() {
//                return StackPolicy.NEVER;
//            }
//
//            @Override
//            public AnimationPolicy getAnimationPolicy() {
//                return AnimationPolicy.RESET_ALL;
//            }
//
//            @Override
//            public void execute() {
//                Tile tile = player.getRegion().getTile(location);
//                boolean canLoot = true;
////                for (GroundItem g : tile.getGroundItems()) {
////                    if (isUntradable(g.getItem().getId()) && hasUntradable(player, g.getItem())) {
////                        continue;
////                    }
////                    if (player.getRights() == Rights.IRON_MAN) {
////                        if (!g.respawns() && g.isGlobal() || !g.isOwnedBy(player.getName()) || g.isPvPDrop()) {
////                            canLoot = false;
////                            continue;
////                        }
////                    }
////                    if (g.getItem().getId() == id && g.isOwnedBy(player.getName())) {
////                        if (player.getInventory().add(player.checkForSkillcape(g.getItem()))) {
////                            if (player.getBountyHunter() != null && (player.getBountyHunter().getLeavePenalty() > 0 || player.pickupPenalty > 0)) {
////                                player.getBountyHunter().setLeavePenalty(180);
////                            }
////                            if (player.getLocation().distance(location) == 1 && !player.getCombatState().canMove())
////                                player.face(location);
////                            World.getWorld().unregister(g);
////                        } else {
////                            player.getActionSender().sendMessage("Not enough space in inventory.");
////                        }
////                        break;
////                    }
////                }
////                if (!canLoot) {
////                    player.getActionSender().sendMessage("You can't loot this item.");
////                }
//                GroundItemController.pickupGroundItem(location, id, player);
//                this.endGame();
//            }
//        };
//        if (player.getLocation().equals(location) || (!diagonal(player.getLocation(), location) && player.getLocation().distance(location) == 1)) {
//            action.execute();
//        } else {
//            player.addCoordinateAction(player.getWidth(), player.getHeight(), location, 0, 0, 0, action);
//        }
	}

	private boolean diagonal(Location l, Location l2) {
		int x = Math.abs(l.getX() - l2.getX());
		int y = Math.abs(l.getY() - l2.getY());
		return x == 1 && y == 1;
	}

	/**
	 * Handles item option 1.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOption1(Player player, Packet packet) {
		int interfaceValue = packet.getInt();
		int slot = packet.getShort() & 0xFFFF;
		int id = packet.getShort() & 0xFFFF;
		int interfaceId = interfaceValue >> 16;
		int childId = interfaceValue & 0xFFFF;
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		player.getActionSender().sendMessage("Opt1");
		player.getActionSender().sendDebugPacket(packet.getOpcode(), "ItemOpt1", new Object[]{"ID: " + id, "Interface: " + interfaceId});

		Item item = null;

		switch (interfaceId) 
		{
			case Equipment.INTERFACE:
			case Equipment.SCREEN:
				if (slot >= 0 && slot < Equipment.SIZE) {
					item = player.getEquipment().get(slot);
					if (!player.canEmote()) {//stops people unequipping during a skillcape emote.
						return;
					}
					if (!Container.transfer(player.getEquipment(), player.getInventory(), slot, id)) {
						player.getActionSender().sendMessage("Not enough space in inventory.");
					} else {
						if (item != null && item.getEquipmentDefinition() != null) {
							for (int i = 0; i < item.getEquipmentDefinition().getBonuses().length; i++) {
								player.getCombatState().setBonus(i, player.getCombatState().getBonus(i) - item.getEquipmentDefinition().getBonus(i));
							}
							player.getActionSender().sendBonuses();
							if (slot == Equipment.SLOT_WEAPON) {
								player.setDefaultAnimations();
							}
						}
					}
				}
				break;
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					item = player.getInventory().get(slot);
				switch (item.getId()) {

				}
				break;
				}
			default:
				logger.info("Unhandled item option 1 : " + id + " - " + slot + " - " + interfaceId + ".");
				break;
		}
	}

	/**
	 * Handles item option 2.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOption2(Player player, Packet packet) {
		int interfaceValue = packet.getInt();
		int slot = packet.getShort() & 0xFFFF;
		int id = packet.getShort() & 0xFFFF;
		int interfaceId = interfaceValue >> 16;
		int childId = interfaceValue & 0xFFFF;
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();
		player.getActionSender().sendDebugPacket(packet.getOpcode(),
				"ItemOpt2",
				new Object[]{"ID: " + id, "Interface: " + interfaceId});

		Item item = null;
		switch (interfaceId) {
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					item = player.getInventory().get(slot);

					if (item != null) {
						hookService.post(new GameItemInventoryActionEvent(player, GameItemInventoryActionEvent.ClickType.OPTION_2, item, slot));
						if (Jewellery.rubItem(player, slot, item.getId(), false)) {
							return;
						}
						if (Constants.isMaxCape(item.getId())) {
							player.teleport(Location.create(2933, 3285), 0, 0, false);
							return;
						}
						switch (item.getId()) {
						//default:
							//player.sendMessage("Nothing interesting happens.");
							//break;
						case 3839:
						case 3841:
						case 3843:
						case 12607:
						case 12609:
						case 12611:
							GodBooks.checkBook(player, item.getId());
							break;
						case 11864:
						case 11865:
						case 19639:
						case 19647:
						case 19643:
						case 19641:
						case 19645:
						case 19649:
						case 21264:
						case 21266:
							slayerService.sendCheckTaskMessage(player);
							break;
							case 13226:
								player.getHerbSack().handleEmptySack();
								break;
								
							case 2572:
								BossKillLog.handleBossLog(player);
								break;
						
							case 4251:
								player.teleport(Location.create(3658, 3522), 0, 0, false);
						break;
							case 13124:
								player.teleport(Location.create(2661, 3371, 0), 0, 0, false);
								break;
							case 13221://music cape
								player.teleport(Location.create(2689, 354, 0), 0, 0, false);
								break;
							case 9813://quest cape
								player.teleport(Location.create(2735, 3344, 0), 0, 0, false);
								break;	
							case 13069://achievment cape
								player.teleport(Location.create(2661, 3371, 0), 0, 0, false);
								break;	
								case 13128:
									player.teleport(Location.create(3056, 3294, 0), 0, 0, false);
									break;
								case 13115:
									player.teleport(Location.create(3489, 3281, 0), 0, 0, false);
									break;
									case 13140:
									player.teleport(Location.create(2732, 3431, 0), 0, 0, false);
									break;
									case 13111:
									player.teleport(Location.create(3345, 3873, 0), 0, 0, false);
									break;
									case 13136:
									player.teleport(Location.create(3417, 2921, 0), 0, 0, false);
									break;
									case 13132:
									player.teleport(Location.create(2664, 3640, 0), 0, 0, false);
									break;
									case 13144:
									player.teleport(Location.create(2339, 3691, 0), 0, 0, false);
									break;
									case 19564:
									player.teleport(Location.create(2465, 3495, 0), 0, 0, false);
									break;
									case 9799://fishing cape
									player.teleport(Location.create(2612, 3391, 0), 0, 0, false);
									break;
									case 9751://strength cape
									player.teleport(Location.create(2871, 3546, 0), 0, 0, false);
									break;
									case 13280://Max cape
										DialogueManager.openDialogue(player, 13280);
										break;
							case 11738:
								HerbBox box = new HerbBox(player);

								Optional<List<Item>> rewards = box.getHerbRewards();

								if (rewards.isPresent()) {
									player.sendMessage("You open your herb box to unveil 10 herbs.");
									player.getInventory().remove(item);
									rewards.get().forEach(i -> player.getBank().add(new Item(i.getId() - 1, i.getCount())));
								}
								break;
							case 12926:
							case 12931:
							case 12899:
							case 13199:
							case 13197:
							case 12904:
								int charges = itemService.getCharges(player, item);

								if (charges < 0) {
									charges = 0;
								}

								player.getActionSender().sendMessage("Your " + item.getDefinition2().name + " has " + charges + " remaining charges.");
								break;
							case 11941:
								lootingBagService.open(player);
								break;
							case 9780:
							case 9781:
								if (player.getSkills().getLevelForExperience(Skills.CRAFTING) < 99) {
									return;
								}
								player.teleport(Location.create(2933, 3285), 0, 0, false);
								break;
						}
					}
				}
				break;
			case Equipment.INTERFACE:
				if (slot >= 0 && slot < Equipment.SIZE) {
					item = player.getEquipment().get(slot);
					if (item != null) {
						switch (item.getId()) {

							case 2550:
								player.getActionSender()
										.sendMessage(
												"<col=7f00ff>Your Ring of Recoil can deal "
														+ player.getCombatState()
														.getRingOfRecoil()
														+ " more points of damage before shattering.");
								break;
							default:
								player.getActionSender().sendMessage(
										"There is no way to operate that item.");
								break;
						}
					}
				}
				break;
			default:
				logger.info("Unhandled item option 2 : " + id + " - " + slot
						+ " - " + interfaceId + ".");
				break;
		}
	}

	/**
	 * Handles item option 3.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOption3(Player player, Packet packet) {
		int interfaceValue = packet.getInt();
		int slot = packet.getShort() & 0xFFFF;
		int id = packet.getShort() & 0xFFFF;
		int interfaceId = interfaceValue >> 16;
		int childId = interfaceValue & 0xFFFF;
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();
		player.getActionSender().sendDebugPacket(packet.getOpcode(),
				"ItemOpt3",
				new Object[]{"ID: " + id, "Interface: " + interfaceId});

		Item item = null;

		switch (interfaceId) {
		
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					item = player.getInventory().get(slot);

					if (item != null) {
						hookService.post(new GameItemInventoryActionEvent(player, GameItemInventoryActionEvent.ClickType.OPTION_3, item, slot));
						switch (item.getId()) {
						case 13226:
							player.getHerbSack().handleCheckSack();
						break;
					}
					}
				}
				break;
			default:
				logger.info("Unhandled item option 3 : " + id + " - " + slot
						+ " - " + interfaceId + ".");
				break;
		}
	}

	/**
	 * Handles item option 4.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOption4(Player player, Packet packet) {
		int interfaceValue = packet.getLEInt();
		int slot = packet.getLEShort() & 0xFFFF;
		int id = packet.getLEShortA() & 0xFFFF;
		int interfaceId = interfaceValue >> 16;
		int childId = interfaceValue & 0xFFFF;
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();
		player.getActionSender().sendDebugPacket(packet.getOpcode(),
				"ItemOpt4",
				new Object[]{"ID: " + id, "Interface: " + interfaceId});
		Item item = null;
		switch (interfaceId) {
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					item = player.getInventory().get(slot);

					if (item != null) {
						hookService.post(new GameItemInventoryActionEvent(player, GameItemInventoryActionEvent.ClickType.OPTION_4, item, slot));
						if (Teleporting.breakTablet(player, item)) {
							return;
						}
						if (item.getId() >= 11866 && item.getId() <= 11873) {
							slayerService.sendCheckTaskMessage(player);
							return;
						}
						if (Jewellery.rubItem(player, slot, item.getId(), false)) {
							return;
						}
						switch (item.getId()) {
							case 13226:
								player.getHerbSack().handleCheckSack();
								break;
							
							case 11283:
								DragonfireShield.empty(player);
								break;
							case 4155: // slayer gem
								slayerService.openSlayerLog(player);
								break;

							case 12006: // abyssal tentacle
								player.getActionSender().sendMessage("Your abyssal tentacle can perform "
										+ NumberFormat.getInstance(Locale.ENGLISH).format(itemService.getCharges(player, item)) + " more attacks.");
								break;
							case 12926:

								int charges = itemService.getCharges(player, item);
								if (charges == 0) {
									player.getActionSender().sendMessage("Your blowpipe is empty. Try loading some ammo.");
									return;
								}
								if (charges >= 1) {
									CacheItemDefinition def = CacheItemDefinition.get(itemService.getChargedItem(player, item));
									if (def != null) {
										Item chargedItem = new Item(def.getId(), charges);
										if (player.getInventory().add(chargedItem)) {
											Item scales = new Item(12934, charges * 3);
											itemService.setChargesWithItem(player, item, chargedItem, -1);
											itemService.setCharges(player, item, -1);
											itemService.degradeItem(player, item);
											playerService.giveItem(player, scales, true);
											player.getActionSender().sendMessage("You unload " + charges + "x " + def.name + " and " + scales.getCount() + "x " + scales.getDefinition2().name + " from the blowpipe.");
										}
									}
								}
								break;
						}
					}
				}
				break;
		}
	}

	/**
	 * Handles item option 5.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOption5(Player player, Packet packet) {
		int interfaceValue = packet.getInt();
		int slot = packet.getShort() & 0xFFFF;
		int id = packet.getShort() & 0xFFFF;
		int interfaceId = interfaceValue >> 16;
		int childId = interfaceValue & 0xFFFF;
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		player.getActionSender().sendDebugPacket(packet.getOpcode(),
				"ItemOpt5",
				new Object[]{"ID: " + id, "Interface: " + interfaceId});
		switch (interfaceId) {
			case Trade.TRADE_INVENTORY_INTERFACE:
				if (slot >= 0 && slot < Trade.SIZE) {
					player.getInterfaceState().openEnterAmountInterface(
							interfaceId, slot, id);
				}
				break;
			case Trade.PLAYER_INVENTORY_INTERFACE:
				if (slot >= 0 && slot < Trade.SIZE) {
					player.getInterfaceState().openEnterAmountInterface(
							interfaceId, slot, id);
				}
				break;
			case Bank.PLAYER_INVENTORY_INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					player.getInterfaceState().openEnterAmountInterface(
							interfaceId, slot, id);
				}
				break;
			case Bank.BANK_INVENTORY_INTERFACE:
				if (slot >= 0 && slot < Bank.SIZE) {
					player.getInterfaceState().openEnterAmountInterface(
							interfaceId, slot, id);
				}
				break;
			default:
				logger.info("Unhandled item option 5 : " + id + " - " + slot
						+ " - " + interfaceId + ".");
				break;
		}
	}

	/**
	 * Handles item option examine.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOptionExamine(Player player, Packet packet) {
		int id = packet.getLEShort() & 0xFFFF;

		player.getActionSender().sendDebugPacket(packet.getOpcode(), "ItemExamine", new Object[]{"ID: " + id});


		Item item = new Item(id);
		if (item.getDefinition() != null) {
			if(permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR))
			player.getActionSender().sendMessage(item.getDefinition().getExamine() + " (ID: " + item.getId() + ")");
			else
			player.getActionSender().sendMessage(item.getDefinition().getExamine());
		}
	}

	/**
	 * Handles item option 1.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	private void handleItemOptionClick1(final Player player, Packet packet) {
		int id = packet.getShortA() & 0xFFFF;
		int interfaceId = packet.getInt();
		int slot = packet.getShort() & 0xFFFF;
		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		player.getActionSender().sendDebugPacket(packet.getOpcode(), "ItemClick1", new Object[]{"ID: " + id, "Interface: " + interfaceId});

		Item item = null;

		switch (interfaceId) {
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					item = player.getInventory().get(slot);
					if (item == null || id != item.getId()) {
						return;
					}
					hookService.post(new GameItemInventoryActionEvent(player, GameItemInventoryActionEvent.ClickType.OPTION_1, item, slot));
					if (ClickEventManager.getEventManager().handleItemAction(player, player.getInventory().get(slot), slot, ClickOption.FIRST)) {
						return;
					}
					//				if(item.getId() >= 5509 && item.getId() <= 5515){
					//					Runecrafting.fillPouch(player, item.getId(), slot);
					//					return;
					//				}
					Herb herb = Herb.forId(id);
					if (herb != null) {
						if (player.getSkills().getLevelForExperience(Skills.HERBLORE) < herb.getRequiredLevel()) {
							player.getActionSender().sendMessage("You cannot clean this herb. You need a Herblore level of " + herb.getRequiredLevel() + " to attempt this.");
							return;
						}
						player.getActionSender().sendMessage("You clean the dirt from the " + org.rs2server.cache.format.CacheItemDefinition.get(herb.getReward()).getName().toLowerCase() + ".");
						player.getInventory().remove(new Item(herb.getId()), slot);
						player.getInventory().add(new Item(herb.getReward()), slot);
						player.getSkills().addExperience(Skills.HERBLORE, herb.getExperience());
						return;
					}
					if (Teleporting.breakTablet(player, item)) {
						return;
					}
					switch (item.getId()) {
					case 20703:
					       SupplyCrate supplycrate = new SupplyCrate(player);

					       Optional<Item> itemRewards21 = supplycrate.getRewardItem();
					       if (itemRewards21.isPresent()) {
					        if (itemRewards21.get().getId() >= 20693 && itemRewards21.get().getId() <= 20720) {
					         if (player.getInventory().contains(itemRewards21.get().getId()) || player.getBank().getCount(itemRewards21.get().getId()) > 0) {
					          player.getActionSender().sendMessage("Sorry you can't have duplicate pouches.");
					          return;
					         }
					        }
					        if (player.getInventory().add(itemRewards21.get())) {
					         player.sendMessage("You open your Supply Crate...");
					         player.getInventory().remove(item);
					        }

					       }
					       break;
						case 7509:
							player.playAnimation(Animation.create(829));
							World.getWorld().submit(new StoppingTick(1) {
								@Override
								public void executeAndStop() {
									if (player.getSkills().getLevel(Skills.HITPOINTS) > 2) {
										player.forceChat("Ow! I nearly broke a tooth!");
										player.inflictDamage(new Hit(2), player);
									}
								}
							});
							break;
						case 13226:
							player.getHerbSack().handleFillSack();
							break;
						case 13190:
							DialogueManager.openDialogue(player, 13190);
							break;
						case 11879:
							player.getInventory().remove(item);
							player.getInventory().add(new Item(228, 100));
							break;
						case 11881:
						player.getInventory().remove(item);
							player.getInventory().add(new Item(314, 100));
							break;
						case 11883:
						player.getInventory().remove(item);
							player.getInventory().add(new Item(313, 100));
							break;
						case 6199:
							MysteryBox mysteryBox = new MysteryBox(player);

							Optional<Item> itemRewards = mysteryBox.getRewardItem();
							if (itemRewards.isPresent()) {
								if (itemRewards.get().getId() >= 5509 && itemRewards.get().getId() <= 5515) {
									if (player.getInventory().contains(itemRewards.get().getId()) || player.getBank().getCount(itemRewards.get().getId()) > 0) {
										player.getActionSender().sendMessage("Sorry you can't have duplicate Runecrafting pouches.");
										return;
									}
								}
								if (player.getInventory().add(itemRewards.get())) {
									player.sendMessage("You open your mystery box...");
									player.getInventory().remove(item);
								}

							}
							break;
						case 11238:
							BabyImpJar babyimpJar = new BabyImpJar(player);

							Optional<Item> bjarRewards = babyimpJar.getRewardItem();
								if (player.getInventory().add(bjarRewards.get())) {
									player.sendMessage("You cracked open your jar...");
									player.getInventory().remove(item);
								}
						case 11240:
							YoungImpJar youngimpJar = new YoungImpJar(player);

							Optional<Item> yjarRewards = youngimpJar.getRewardItem();
								if (player.getInventory().add(yjarRewards.get())) {
									player.sendMessage("You cracked open your jar...");
									player.getInventory().remove(item);
								}
						case 19473:
							BagFullOfGems bagOfGems = new BagFullOfGems(player);
							List<Item> gems = bagOfGems.getGems();
							if(player.getInventory().freeSlots() > 5)
							{
								for(int i = 0; i < gems.size(); i++)
								{
									player.getInventory().add(gems.get(i));
								}
								player.sendMessage("You open and empty the bag of gems.");
								player.getInventory().remove(item);
							} else {
								player.sendMessage("You need 5 free inventory spaces to open a bag of gems.");
							}
							break;
						case 405:
							Casket casket = new Casket(player);

							Optional<Item> itemRewards2 = casket.getRewardItem();
							if (itemRewards2.isPresent()) {
								if (itemRewards2.get().getId() >= 5509 && itemRewards2.get().getId() <= 5515) {
									if (player.getInventory().contains(itemRewards2.get().getId()) || player.getBank().getCount(itemRewards2.get().getId()) > 0) {
										player.getActionSender().sendMessage("Sorry you can't have duplicate pouches.");
										return;
									}
								}
								if (player.getInventory().add(itemRewards2.get())) {
									player.sendMessage("You open your casket...");
									player.getInventory().remove(item);
								}

							}
							break;
						case 12960:
							BronzeSet bronzeSet = new BronzeSet(player);

							Optional<Item> setRewards = bronzeSet.getRewardItem();
							if (setRewards.isPresent()) {
								if (setRewards.get().getId() >= 5509 && setRewards.get().getId() <= 5515) {
									if (player.getInventory().contains(setRewards.get().getId()) || player.getBank().getCount(setRewards.get().getId()) > 0) {
										player.getActionSender().sendMessage("Sorry.");
										return;
									}
								}
								if (player.getInventory().add(setRewards.get())) {
									player.sendMessage("You open your armour set...");
									player.getInventory().remove(item);
								}

							}
							break;
						case 11738:
							HerbBox box = new HerbBox(player);

							Optional<List<Item>> rewards = box.getHerbRewards();

							if (rewards.isPresent()) {
								player.sendMessage("You open your herb box to unveil 10 herbs.");
								player.getInventory().remove(item);
								rewards.get().forEach(i -> playerService.giveItem(player, i, true));
							}
							break;
						case 7956: // Casket!
							player.getInventory().remove(slot, new Item(7956, 1));
							final LootGenerationService gen = Server.getInjector().getInstance(LootGenerationService.class);
							final PlayerService ps = Server.getInjector().getInstance(PlayerService.class);
							final Item loot = gen.generateCasketLoot();
							if (loot == null) {
								player.sendMessage("The casket was empty!");
							} else {
								ps.giveItem(player, loot, true);
								player.sendMessage("You found some treasure!");
							}
							break;
						case 11171:
							player.getActionSender().sendUpdateLog();
							break;
						case 4155: 
							DialogueManager.openDialogue(player, 513);
							break;
						case 11941:
							lootingBagService.check(player);
							break;
						case 12641:
							if (player.getInventory().add(new Item(12640, 100))) {
								player.getInventory().remove(new Item(12641, 1), slot);
							}
							break;
						case 12728:
							if (player.getInventory().add(new Item(556, 100))) {
								player.getInventory().remove(new Item(12728, 1), slot);
							}
							break;
						case 12730:
							if (player.getInventory().add(new Item(555, 100))) {
								player.getInventory().remove(new Item(12730, 1), slot);
							}
							break;
						case 12732:
							if (player.getInventory().add(new Item(557, 100))) {
								player.getInventory().remove(new Item(12732, 1), slot);
							}
							break;
						case 12734:
							if (player.getInventory().add(new Item(554, 100))) {
								player.getInventory().remove(new Item(12734, 1), slot);
							}
							break;
						case 12859:
							if (player.getInventory().add(new Item(222, 100))) {
								player.getInventory().remove(new Item(12859, 1), slot);
							}
							break;
						case 12736:
							if (player.getInventory().add(new Item(558, 100))) {
								player.getInventory().remove(new Item(12736, 1), slot);
							}
							break;
						case 12738:
							if (player.getInventory().add(new Item(562, 100))) {
								player.getInventory().remove(new Item(12738, 1), slot);
							}
							break;
						case 4251:
							Ectophial.teleport(player);
							break;
						case 526:
						case 528:
						case 530:
						case 532:
						case 534:
						case 536:
						case 2530:
						case 2859:
						case 3125:
						case 3123:
						case 6812:
						case 6729:
						case 4834:
						case 11943:
							player.getSkills().getPrayer().buryBone(new Item(item.getId()), slot);
							break;
						case 6:
							for (GameObject obj : player.getRegion().getGameObjects()) {
								if (obj != null && obj.getType() == 10 && obj.getLocation().equals(player.getLocation())) {
									player.getActionSender().sendMessage("You cannot set up a cannon here.");
									return;
								}
							}
							player.setAttribute("cannon", new Cannon(player, 
									Location.create(player.getLocation().getX() - 2, player.getLocation().getY() + 1)));
							break;
						default:
							Action action = new ConsumeItemAction(player, item, slot);
							action.execute();
							break;
					}
					break;
				}
		}
	}

	/**
	 * Handles item on item option.
	 *
	 * @param player The player.
	 * @param packet The packet.
	 */
	@SuppressWarnings("unused")
	private void handleItemOptionItem(final Player player, Packet packet) {
		int fromInterfaceHash = packet.getInt();
		int usedWithId = packet.getShort();
		int toInterfaceHash = packet.getInt1();
		int slot = packet.getLEShortA();
		int usedWith = packet.getShortA();
		int usedWithSlot = packet.getShortA();

		int interfaceId = fromInterfaceHash >> 16;
		int toInterfaceId = toInterfaceHash >> 16;

		if (player.getCombatState().isDead()) {
			return;
		}
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		player.getActionSender()
				.sendDebugPacket(
						packet.getOpcode(),
						"ItemOnItem",
						new Object[]{"ID: " + usedWith,
								"Interface: " + interfaceId});

		Item usedItem = null;
		Item withItem = null;
		Item withItem1 = null;
		Item withItem2 = null;
		Item withItem3 = null;

		switch (interfaceId) {
		//default:
			//player.sendMessage("Nothing interesting happens.");
			//break;
			
			case Inventory.INTERFACE:
				if (slot >= 0 && slot < Inventory.SIZE) {
					usedItem = player.getInventory().get(slot);
					withItem = player.getInventory().get(usedWithSlot);
					if (usedItem == null || withItem == null)
						return;
					if (usedWith != usedItem.getId())
						return;
					if (usedWithId != withItem.getId())
						return;
					if (player.getCombatState().isDead())
						return;
					if (LeatherCrafting.handleItemOnItem(player, usedItem, withItem)) {
						return;
					}
					if (SnakeskinCrafting.handleItemOnItem(player, usedItem, withItem)) {
						return;
					}
					MaxCapeService maxCapeService = Server.getInjector().getInstance(MaxCapeService.class);
					if (maxCapeService.addToMaxCape(player, usedItem, withItem)) {
						return;
					}
					for (int i = 0; i < Constants.stringItems.length; i++) {
						if (Constants.stringItems[i][0] == usedItem.getId() || Constants.stringItems[i][0] == withItem.getId()) {
							GemCrafting.string(player, i);
							return;
						}
					}

					if (usedItem.getId() == 11941 || withItem.getId() == 11941) {
						Item otherItem;
						int otherIndex;
						if (usedItem.getId() == 11941) {
							otherItem = withItem;
							otherIndex = usedWithSlot;
						} else {
							otherItem = usedItem;
							otherIndex = slot;
						}
						if (player.getInventory().getCount(otherItem.getId()) == 1) {
							lootingBagService.deposit(player, player.getInventory().getSlotById(otherItem.getId()), otherItem.getId(), 1);
							return;
						}
						player.setInterfaceAttribute("lootingBagItem", otherItem);
						player.setInterfaceAttribute("lootingBagIndex", otherIndex);
						DialogueManager.openDialogue(player, 11941);
						return;
					}
					
					if ((usedItem.getId() == 13196 || withItem.getId() == 13196
							&& player.getInventory().contains(13196)) || (usedItem.getId() == 13197 || withItem.getId() == 13197
							&& player.getInventory().contains(13197))) {
						Item scales;
						Item helm;
						if (usedItem.getId() == 13196 || usedItem.getId() == 13197) {
							scales = withItem;
							helm = usedItem;
						} else {
							scales = usedItem;
							helm = withItem;
						}

						itemService.upgradeItem(player, helm, scales);
						return;
					}

					if ((usedItem.getId() == 13198 || withItem.getId() == 13198
							&& player.getInventory().contains(13198)) || (usedItem.getId() == 13199 || withItem.getId() == 13199
							&& player.getInventory().contains(13199))) {
						Item scales;
						Item helm;
						if (usedItem.getId() == 13198 || usedItem.getId() == 13199) {
							scales = withItem;
							helm = usedItem;
						} else {
							scales = usedItem;
							helm = withItem;
						}

						itemService.upgradeItem(player, helm, scales);
						return;
					}
					if ((usedItem.getId() == 12902 || withItem.getId() == 12902) && player.getInventory().contains(12902)
							|| (usedItem.getId() == 12904 || withItem.getId() == 12904) && player.getInventory().contains(12904)) {
						Item with;
						Item staff;
						if (usedItem.getId() == 12902 || usedItem.getId() == 12904) {
							with = withItem;
							staff = usedItem;
						} else {
							with = usedItem;
							staff = withItem;
						}
						if (with.getId() == 12934) {
							itemService.upgradeItem(player, staff, with);
							return;
						}
					}
					if ((usedItem.getId() == 12900 || withItem.getId() == 12900) && player.getInventory().contains(12900)
							|| (usedItem.getId() == 12899 || withItem.getId() == 12899) && player.getInventory().contains(12899)) {
						Item with;
						Item staff;
						if (usedItem.getId() == 12900 || usedItem.getId() == 12899) {
							with = withItem;
							staff = usedItem;
						} else {
							with = usedItem;
							staff = withItem;
						}
						if (with.getId() == 12934) {
							itemService.upgradeItem(player, staff, with);
							return;
						}
					}
					if ((usedItem.getId() == 13231 && withItem.getId() == 11840) || (usedItem.getId() == 11840 && withItem.getId() == 13231)) {
						Item with;
						Item crystal;
						if (usedItem.getId() == 11840) {
							with = withItem;
							crystal = usedItem;
						} else {
							with = usedItem;
							crystal = withItem;
						}
						if (player.getSkills().getLevelForExperience(Skills.MAGIC) < 60 || player.getSkills().getLevelForExperience(Skills.RUNECRAFTING) < 60) {
							player.getActionSender().sendMessage("You need 60 magic and runecrafting to do this.");
							return;
						}
						itemService.upgradeItem(player, with, crystal);
						return;
					}

					if ((usedItem.getId() == 13265 && withItem.getId() == 187) || (usedItem.getId() == 187 && withItem.getId() == 13265)) {
						Item with;
						Item crystal;
						if (usedItem.getId() == 187) {
							with = withItem;
							crystal = usedItem;
						} else {
							with = usedItem;
							crystal = withItem;
						}
						itemService.upgradeItem(player, with, crystal);
						return;
					}

					if ((usedItem.getId() == 13229 && withItem.getId() == 2577) || (usedItem.getId() == 2577 && withItem.getId() == 13229)) {
						Item with;
						Item crystal;
						if (usedItem.getId() == 2577) {
							with = withItem;
							crystal = usedItem;
						} else {
							with = usedItem;
							crystal = withItem;
						}
						if (player.getSkills().getLevelForExperience(Skills.MAGIC) < 60 || player.getSkills().getLevelForExperience(Skills.RUNECRAFTING) < 60) {
							player.getActionSender().sendMessage("You need 60 magic and runecrafting to do this.");
							return;
						}
						itemService.upgradeItem(player, with, crystal);
						return;
					}

					if ((usedItem.getId() == 13227 && withItem.getId() == 6920) || (usedItem.getId() == 6920 && withItem.getId() == 13227)) {
						Item with;
						Item crystal;
						if (usedItem.getId() == 6920) {
							with = withItem;
							crystal = usedItem;
						} else {
							with = usedItem;
							crystal = withItem;
						}
						if (player.getSkills().getLevelForExperience(Skills.MAGIC) < 60 || player.getSkills().getLevelForExperience(Skills.RUNECRAFTING) < 60) {
							player.getActionSender().sendMessage("You need 60 magic and runecrafting to do this.");
							return;
						}
						itemService.upgradeItem(player, with, crystal);
						return;
					}

					if ((usedItem.getId() == 13233 && withItem.getId() == 6739) || (usedItem.getId() == 6739 && withItem.getId() == 13233)) {
						Item with;
						Item stone;
						if (usedItem.getId() == 6739) {
							with = withItem;
							stone = usedItem;
						} else {
							with = usedItem;
							stone = withItem;
						}
						if (player.getSkills().getLevelForExperience(Skills.FIREMAKING) < 85) {
							player.getActionSender().sendMessage("You need a Firemaking level of 85 to do this.");
							return;
						}
						itemService.upgradeItem(player, with, stone);
						return;
					}

					if ((usedItem.getId() == 13233 && withItem.getId() == 11920) || (usedItem.getId() == 11920 && withItem.getId() == 13233)) {
						if (player.getSkills().getLevelForExperience(Skills.SMITHING) < 85) {
							player.getActionSender().sendMessage("You need 85 Smithing to do this.");
							return;
						}
						if (player.getInventory().contains(13233) && player.getInventory().contains(11920)) {
							player.getInventory().remove(new Item(13233, 1));
							player.getInventory().remove(new Item(11920, 1));
							player.getInventory().add(new Item(13243));
						}
						return;
					}

					if ((usedItem.getId() == 12929 || withItem.getId() == 12929
							&& player.getInventory().contains(12929)) || (usedItem.getId() == 12931 || withItem.getId() == 12931
							&& player.getInventory().contains(12931))) {
						Item with;
						Item helm;
						if (usedItem.getId() == 12927 || usedItem.getId() == 12929) {
							with = withItem;
							helm = usedItem;
						} else {
							with = usedItem;
							helm = withItem;
						}

						if (helm.getId() == 12929 && with.getId() == 13201 && player.getInventory().remove(helm) > 0) {
							player.getInventory().add(new Item(13198));
							player.getInventory().remove(new Item(13201));
						} else if (helm.getId() == 12929 && with.getId() == 13200 && player.getInventory().remove(helm) > 0) {
							player.getInventory().add(new Item(13196));
							player.getInventory().remove(new Item(13200));
						} else {
							if (helm.getId() == 12929 && player.getInventory().contains(12931) || player.getBank().getCount(12931) > 0) {
								return;
							}
							itemService.upgradeItem(player, helm, with);
						}
						return;
					}
					if (usedItem.getId() == 11791 || withItem.getId() == 11791) {
						Item otherItem;
						if (usedItem.getId() == 11791) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						if (otherItem.getId() == 12932) {
							player.getInventory().remove(new Item(11791));
							player.getInventory().remove(new Item(12932));
							player.getInventory().add(new Item(12902));
						}
					}
					if (usedItem.getId() == 11335 && withItem.getId() == 12538 || usedItem.getId() == 12538 && withItem.getId() == 11335) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12417));
					}
					if (usedItem.getId() == 3140 && withItem.getId() == 12534 || usedItem.getId() == 12534 && withItem.getId() == 3140) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12414));
					}
					if (usedItem.getId() == 12337 && withItem.getId() == 1042 || usedItem.getId() == 1042 && withItem.getId() == 12337) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12399));
					}
					if (usedItem.getId() == 12432 && withItem.getId() == 12353 || usedItem.getId() == 12353 && withItem.getId() == 12432) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12434));
					}
					//spirit shield on holy exilir
					if (usedItem.getId() == 12833 && withItem.getId() == 12829 || usedItem.getId() == 12829 && withItem.getId() == 12833) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12831));
					}
					//spirit shield on ely sigil
					if (usedItem.getId() == 12819 && withItem.getId() == 12831 || usedItem.getId() == 12831 && withItem.getId() == 12819) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12817));
					}
					//spirit shield on spectral
					if (usedItem.getId() == 12831 && withItem.getId() == 12823 || usedItem.getId() == 12823 && withItem.getId() == 12831) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12821));
					}}
					//raw eel
					/*if (usedItem.getId() == 5605 && withItem.getId() == 13339 || usedItem.getId() == 13339 && withItem.getId() == 5605) {
						if (player.getSkills().getLevel(Skills.COOKING) < 75) {
							player.getActionSender().sendMessage("You need 75 cooking to harvest this.");
						}
					} else {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12934, 6));
						player.playAnimation(Animation.create(6702));
						player.getSkills().addExperience(Skills.COOKING, 50);
					} */
					//spirit shield on arcane
					if (usedItem.getId() == 12827 && withItem.getId() == 12831 || usedItem.getId() == 12831 && withItem.getId() == 12827) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12825));
					}
					if (usedItem.getId() == 1187 && withItem.getId() == 12532 || usedItem.getId() == 12532 && withItem.getId() == 1187) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(12418));
					}
					if (usedItem.getId() == 12536 || withItem.getId() == 12536) {
						Item otherItem;
						if (usedItem.getId() == 12536) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						if (otherItem.getId() == 4087 || otherItem.getId() == 4585) {
							player.getInventory().remove(usedItem);
							player.getInventory().remove(withItem);
							player.getInventory().add(new Item(otherItem.getId() == 4087 ? 12415 : 12416));
						}
					}
					if (usedItem.getId() == 12530 || withItem.getId() == 12530) {
						Item otherItem;
						if (usedItem.getId() == 12530) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						if (otherItem.getId() == 6918 || otherItem.getId() == 6916 || otherItem.getId() == 6924) {
							if (player.getInventory().containsItems(6916, 6918, 6924, 12530)) {
								player.getInventory().removeItems(6916, 6918, 6924, 12530);
								player.getInventory().addItems(12419, 12420, 12421);
							}
						}
					}
					if (usedItem.getId() == 12528 || withItem.getId() == 12528) {
						Item otherItem;
						if (usedItem.getId() == 12528) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						if (otherItem.getId() == 6918 || otherItem.getId() == 6916 || otherItem.getId() == 6924) {
							if (player.getInventory().containsItems(6916, 6918, 6924, 12528)) {
								player.getInventory().removeItems(6916, 6918, 6924, 12528);
								player.getInventory().addItems(12457, 12458, 12459);
							}
						}
					}
					if (usedItem.getId() == 11907 || withItem.getId() == 11907) {
						Item otherItem;
						if (usedItem.getId() == 11907) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						if (otherItem.getId() == 12932) {
							player.getInventory().remove(new Item(11907));
							player.getInventory().remove(new Item(12932));
							player.getInventory().add(new Item(12900));
						}
					}
					if (usedItem.getId() == 12526 && withItem.getId() == 6585 || usedItem.getId() == 6585 && withItem.getId() == 12526) {
						player.getInventory().remove(new Item(12526));
						player.getInventory().remove(new Item(6585));
						player.getInventory().add(new Item(12436));
					}
					if ((usedItem.getId() == 12924 || withItem.getId() == 12924
							&& player.getInventory().contains(12924)) || (usedItem.getId() == 12926 || withItem.getId() == 12926
							&& player.getInventory().contains(12926))) {

						Item darts;
						Item pipe;
						if (usedItem.getId() == 12924 || usedItem.getId() == 12926) {
							darts = withItem;
							pipe = usedItem;
						} else {
							darts = usedItem;
							pipe = withItem;
						}

						if (darts.getId() == 12934) {
							return;
						}

						int chargedWith = itemService.getChargedItem(player, pipe);

						if (chargedWith != darts.getId() && chargedWith > 0) {
							CacheItemDefinition def = CacheItemDefinition.get(chargedWith);
							if (def != null && itemService.getCharges(player, pipe) > 0) {
								player.getActionSender().sendMessage("Your blowpipe is already charged with " + def.name + ". Please unload.");
								return;
							}
						} else {

							int scaleAmount = player.getInventory().getCount(12934);
							int requiredScales = darts.getCount() * 3;

							if (scaleAmount >= requiredScales) {
								if (pipe.getId() == 12924 && player.getInventory().contains(12926) || player.getBank().getCount(12926) > 0) {
									return;
								}
								itemService.upgradeItem(player, pipe, darts);
								player.getInventory().remove(new Item(12934, requiredScales));
							} else if (scaleAmount >= 3) {
								if (pipe.getId() == 12924 && player.getInventory().contains(12926) || player.getBank().getCount(12926) > 0) {
									return;
								}
								int dartsNeeded = (int) Math.floor(scaleAmount / 3);
								Item requiredDarts = new Item(darts.getId(), dartsNeeded);
								itemService.upgradeItem(player, pipe, requiredDarts);
								player.getInventory().remove(new Item(12934, dartsNeeded * 3));
							} else {
								player.getActionSender().sendMessage("You do not have enough items to charge your blowpipe.");
								player.getActionSender().sendMessage("One charge is equivalent to 3 Zulrah scales and 1 dart of any type.");
							}
						}
						return;
					}
					if (usedItem.getId() == 985 && withItem.getId() == 987 || usedItem.getId() == 987
							&& withItem.getId() == 985) {
						player.getInventory().remove(new Item(985, 1));
						player.getInventory().remove(new Item(987, 1));
						player.getInventory().add(new Item(989, 1));
						player.sendMessage("You join the two halves of the key together");
						return;
					}
					/*if (usedItem.getId() == 11235 || withItem.getId() == 11235) {
						Item otherItem = null;
						if (usedItem.getId() == 11235) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}

					}*/
					if (usedItem.getId() == 4151 || withItem.getId() == 4151) {
						Item otherItem = null;
						if (usedItem.getId() == 4151) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						if (otherItem.getId() == 12004) {
							DialogueManager.openDialogue(player, 4151);
						} else if (otherItem.getId() == 12769) {
							player.getInventory().remove(new Item(4151));
							player.getInventory().remove(new Item(12769));
							player.getInventory().add(new Item(12774));
						} else if (otherItem.getId() == 12771) {
							player.getInventory().remove(new Item(4151));
							player.getInventory().remove(new Item(12771));
							player.getInventory().add(new Item(12773));
						}
						return;
					}
					if (usedItem.getId() == 12954 || withItem.getId() == 20143) {
						DialogueManager.openDialogue(player, 12954);
					}
					if (usedItem.getId() == 11920 || withItem.getId() == 12800) {
						DialogueManager.openDialogue(player, 11920);
					}
					if (usedItem.getId() == 11335 || withItem.getId() == 12538) {
						DialogueManager.openDialogue(player, 11335);
					}
					if (usedItem.getId() == 1187 || withItem.getId() == 12532) {
						DialogueManager.openDialogue(player, 1187);
					}
					if (usedItem.getId() == 11787 || withItem.getId() == 12798) {
						DialogueManager.openDialogue(player, 11787);
					}
					if (usedItem.getId() == 11924 || withItem.getId() == 12802) {
						DialogueManager.openDialogue(player, 11924);
					}
					if (usedItem.getId() == 11926 || withItem.getId() == 12802) {
						DialogueManager.openDialogue(player, 11927);
					}
					if (usedItem.getId() == 4587 || withItem.getId() == 20002) {
						DialogueManager.openDialogue(player, 4587);
					}
					if (usedItem.getId() == 19553 || withItem.getId() == 20062) {
						DialogueManager.openDialogue(player, 19553);
					}
					if (usedItem.getId() == 12002 || withItem.getId() == 20065) {
						DialogueManager.openDialogue(player, 12002);
					}
					if (usedItem.getId() == 11804 || withItem.getId() == 20071) {
						DialogueManager.openDialogue(player, 11804);
					}
					if (usedItem.getId() == 11806 || withItem.getId() == 20074) {
						DialogueManager.openDialogue(player, 11807);
					}
					if (usedItem.getId() == 11808 || withItem.getId() == 20077) {
						DialogueManager.openDialogue(player, 11813);
					}
					if (usedItem.getId() == 11802 || withItem.getId() == 20068) {
						DialogueManager.openDialogue(player, 11816);
					}
					if (usedItem.getId() == 11235 || withItem.getId() == 12757) {
						DialogueManager.openDialogue(player, 11819);
					}
					if (usedItem.getId() == 11235 || withItem.getId() == 12759) {
						DialogueManager.openDialogue(player, 11822);
					}
					if (usedItem.getId() == 11235 || withItem.getId() == 12761) {
						DialogueManager.openDialogue(player, 11825);
					}
					if (usedItem.getId() == 11235 || withItem.getId() == 12763) {
						DialogueManager.openDialogue(player, 11828);
					}
					if (usedItem.getId() == 233 || withItem.getId() == 233) {
						Item otherItem = null;
						if (usedItem.getId() == 233) {
							otherItem = withItem;
						} else {
							otherItem = usedItem;
						}
						PestleAndMortar.Pestle pestle = PestleAndMortar.Pestle.forId(otherItem.getId());
						if (pestle != null) {
							player.getActionSender().sendItemOnInterface(309, 2,
									pestle.getNext(), 130);
							String itemName = CacheItemDefinition
									.get(pestle.getNext()).getName();
							player.getActionSender().sendString(309, 6,
									"<br><br><br><br>" + itemName);
							player.getActionSender().sendInterface(162, 546, 309, false);
							player.setInterfaceAttribute("pestle_type",
									pestle);
						}
					}
					if (usedItem.getId() == 11818 && withItem.getId() == 11820
							|| usedItem.getId() == 11818
							&& withItem.getId() == 11822
							|| usedItem.getId() == 11820
							&& withItem.getId() == 11818
							|| usedItem.getId() == 11820
							&& withItem.getId() == 11822
							|| usedItem.getId() == 11822
							&& withItem.getId() == 11818
							|| usedItem.getId() == 11822
							&& withItem.getId() == 11820) {
						if (player.getInventory().contains(11818)
								&& player.getInventory().contains(11820)
								&& player.getInventory().contains(11822)) {
							if (player.getSkills().getLevel(Skills.SMITHING) < 80) {
								player.getActionSender()
										.sendMessage(
												"You need a Smithing level of 80 to create a blade.");
								return;
							}
							DialogueManager.openDialogue(player, 23);
							return;
						} else {
							player.getActionSender()
									.sendMessage(
											"You don't have the pieces you need to create a blade!");
							return;
						}
					}
					if (SlayerHelmAction.handleItemOnItem(player, usedItem, withItem)) {
						return;
					}
					if (usedItem.getId() == 590 || withItem.getId() == 590
							&& !player.isLighting()) {
						Item logItem = null;
						if (usedItem.getId() == 590) {
							logItem = withItem;
						} else {
							logItem = usedItem;
						}
						if (logItem != null) {// you were right it sets the location
							// you should be in. kk
							Firemaking firemaking = new Firemaking(player);
							firemaking.light(firemaking.findLog(logItem));
						}
						return;
					}
					FletchingItem item = FletchingAction.getItemForId(
							usedItem.getId(), withItem.getId(), true);
					if (item != null) {
						Item[] materials = item.getMaterials();
						//System.out.println(item.getType());
						if (item.getType() == FletchingType.CUTTING) {
							FletchingGroup group = FletchingAction.groups
									.get(materials[0].getId());
							int iId = 305;
							if (group != FletchingGroup.LOGS) {
								iId = 304;
							}
							player.setInterfaceAttribute("fletch_group", group);
							for (int i = 0; i < group.getPossibleCreations().length; i++) {
								if (group == FletchingGroup.MAGIC_LOGS) {
									iId = 303;
									player.getActionSender().sendItemOnInterface(iId,
											2 + i,
											group.getPossibleCreations()[i].getId(),
											160);
									player.getActionSender()
											.sendString(
													iId,
													(iId - 296) + (i * 4),
													"<br><br><br><br>"
															+ group.getPossibleCreations()[i].getDefinition2().getName());
								} else {
									player.getActionSender().sendItemOnInterface(iId,
											2 + i,
											group.getPossibleCreations()[i].getId(),
											160);
									player.getActionSender()
											.sendString(
													iId,
													(iId - 296) + (i * 4),
													"<br><br><br><br>"
															+ group.getPossibleCreations()[i].getDefinition2().getName());
								}
							}
							player.getActionSender().sendChatboxInterface(iId);
						} else {
							int iId = item.getType() == FletchingType.STRINGING ? 309 : 582;
							player.setInterfaceAttribute("fletch_item", item);
							player.getActionSender().sendItemOnInterface(iId, 2,
									item.getProducedItem()[0].getId(), 150);
							player.getActionSender().sendString(
									iId,
									iId == 309 ? 6 : 5,
									"<br><br><br><br>"
											+ CacheItemDefinition.get(
											item.getProducedItem()[0]
													.getId()).getName());
							player.getActionSender().sendChatboxInterface(iId);
						}
					}
					if (usedItem.getId() == 11798 && withItem.getId() == 11810 || usedItem.getId() == 11810 && withItem.getId() == 11798) {//Armadyl Godsword
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11802));
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11802, null, "You attach the hilt to the blade and make a Armadyl godsword.");
						//player.sendMessage("You attach the hilt to the blade and make a Armadyl godsword.");
						return;
					}
					if (usedItem.getId() == 11798 && withItem.getId() == 11812 || usedItem.getId() == 11812 && withItem.getId() == 11798) {//Bandos Godsword
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11804));
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11804, null, "You attach the hilt to the blade and make a Bandos godsword.");
						//player.sendMessage("You attach the hilt to the blade and make a Bandos godsword.");
						return;
					}
					if (usedItem.getId() == 11798 && withItem.getId() == 11814 || usedItem.getId() == 11814 && withItem.getId() == 11798) {//Sara Godsword
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11806));
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11806, null, "You attach the hilt to the blade and make a Saradomin godsword.");
						//player.sendMessage("You attach the hilt to the blade and make a Saradomin godsword.");
						return;
					}
					if (usedItem.getId() == 11798 && withItem.getId() == 11816 || usedItem.getId() == 11816 && withItem.getId() == 11798) {//Zamorak Godsword
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11808));
						player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 11808, null, "You attach the hilt to the blade and make a Zamorak godsword.");
						//player.sendMessage("You attach the hilt to the blade and make a Zamorak godsword.");
						return;
					}
					if (usedItem.getId() == 11818 && withItem.getId() == 11820 || usedItem.getId() == 11820 && withItem.getId() == 11818) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11794));
						player.sendMessage("You attach the shards together to make a shard 1 & 2");
						return;
					}
					if (usedItem.getId() == 13571 && withItem.getId() == 1931 || usedItem.getId() == 1931 && withItem.getId() == 13571) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().removeItems(13421, 1931);
						player.getInventory().add(new Item(13572));
						player.sendMessage("You mix the items together to create a Dynamite Pot.");
						return;
					}
					if (usedItem.getId() == 1759 && withItem.getId() == 13572 || usedItem.getId() == 13572 && withItem.getId() == 1759) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(13573));
						return;
					}
					if (usedItem.getId() == 11818 && withItem.getId() == 11822 || usedItem.getId() == 11822 && withItem.getId() == 11818) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11796));
						player.sendMessage("You attach the shards together to make a shard 1 & 3");
						return;
					}
					if (usedItem.getId() == 11820 && withItem.getId() == 11822 || usedItem.getId() == 11822 && withItem.getId() == 11820) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11800));
						player.sendMessage("You attach the shards together to make a shard 2 & 3");
						return;
					}
					if (usedItem.getId() == 11794 && withItem.getId() == 11822 || usedItem.getId() == 11822 && withItem.getId() == 11794) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11798));
						player.sendMessage("You attach the shards together to make a Godsword blade.");
						return;
					}
					if (usedItem.getId() == 21043 && withItem.getId() == 6914) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(21006));
						player.sendMessage("You attach the insignia to the wand..");
						return;
					}
					if (usedItem.getId() == 11796 && withItem.getId() == 11820 || usedItem.getId() == 11820 && withItem.getId() == 11796) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11798));
						player.sendMessage("You attach the shards together to make a Godsword blade.");
						return;
					}
					if (usedItem.getId() == 11800 && withItem.getId() == 11818 || usedItem.getId() == 11818 && withItem.getId() == 11800) {
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(new Item(11798));
						player.sendMessage("You attach the shards together to make a Godsword blade.");
						return;
					}
					if (usedItem.getId() == 5605 || withItem.getId() == 5605) {
						Item other = null;
						if (usedItem.getId() == 5605) {
							other = withItem;
						} else {
							other = usedItem;
						}
						ZulrahCrafting.ZulrahItems zulrahItems = ZulrahCrafting.ZulrahItems.of(other.getId());
						if (zulrahItems != null && zulrahItems.getRequiredItem() == 5605) {
							player.getActionQueue().addAction(new ZulrahCrafting(player, zulrahItems));
						}
					}
					if (usedItem.getId() == 1755 || withItem.getId() == 1755) {
						Item uncut = null;
						Item chisel = null;
						if (usedItem.getId() == 1755) {
							uncut = withItem;
							chisel = usedItem;
						} else {
							uncut = usedItem;
							chisel = withItem;
						}
						Gem gem = Gem.forId(uncut.getId());
						BoltTip tip = BoltTip.forId(uncut.getId());
						ZulrahCrafting.ZulrahItems zulrahItems = ZulrahCrafting.ZulrahItems.of(uncut.getId());
						if (gem != null) {
							if (player.getInventory().getCount(uncut.getId()) > 1) {
								player.getActionSender().sendItemOnInterface(309, 2,
										gem.getReward(), 130);
								String itemName = CacheItemDefinition.get(gem.getReward()).getName();
								player.getActionSender().sendString(309, 6,
										"<br><br><br><br>" + itemName);
								player.getActionSender().sendInterface(162, 546, 309, false);
								player.setInterfaceAttribute("gem_index",
										gem.getReward());
								player.setInterfaceAttribute("gem_type", gem);
							} else {
								player.getActionQueue().addAction(new GemCutting(player, gem, 1));
							}
						} else if (tip != null) {
							if (player.getInventory().getCount(uncut.getId()) > 1) {
								player.getActionSender().sendItemOnInterface(309, 2,
										tip.getReward().getId(), 130);
								String itemName = CacheItemDefinition.get(tip.getReward().getId()).getName();
								player.getActionSender().sendString(309, 6,
										"<br><br><br><br>" + itemName);
								player.getActionSender().sendInterface(162, 546, 309, false);
								player.setInterfaceAttribute("tip_index",
										tip.getReward().getId());
								player.setInterfaceAttribute("tip_type", tip);
							} else {
								player.getActionQueue().addAction(new BoltCrafting(player, tip, 1));
							}
						} else if (zulrahItems != null && zulrahItems.getRequiredItem() == chisel.getId()) {
							player.getActionQueue().addAction(new ZulrahCrafting(player, zulrahItems));
						}
					}
					if (SuperCombatPotion.handleItemOnItem(player, usedItem, withItem)) {
						return;
					}
					if (usedItem.getId() == 1391 || withItem.getId() == 1391) {
						Item other;
						if (usedItem.getId() == 1391) {
							other = withItem;
						} else {
							other = usedItem;
						}
						Optional<OrbChargingService.StaffType> typeOptional = OrbChargingService.StaffType.of(other.getId());
						if (typeOptional.isPresent()) {
							OrbChargingService.StaffType type = typeOptional.get();
							if (player.getInventory().getCount(1391) > 1
									&& player.getInventory().getCount(type.getOrbId()) > 1) {
								player.getActionSender().sendItemOnInterface(309, 2, type.getStaffId(), 130);
								String itemName = CacheItemDefinition.get(type.getStaffId()).getName();
								player.getActionSender().sendString(309, 6, "<br><br><br><br>" + itemName);
								player.getActionSender().sendInterface(162, 546, 309, false);
								player.setInterfaceAttribute("staff_type", type);
							} else {
								player.getActionQueue().addAction(new OrbChargingService.BattleStaffAction(player, type, 1));
							}
						}
					}
					if ((usedItem.getId() == 227 || usedItem.getId() == 5935) || (withItem.getId() == 227 || withItem.getId() == 5935)) {
						Item primaryIngredient;
						Item vial;
						if (usedItem.getId() == 227 || usedItem.getId() == 5935) {
							primaryIngredient = withItem;
							vial = usedItem;
						} else {
							primaryIngredient = usedItem;
							vial = withItem;
						}
						PrimaryIngredient ingredient = PrimaryIngredient
								.forId(primaryIngredient.getId(), vial.getId());
						if (ingredient != null) {
							if (player.getInventory().getCount(ingredient.getVial().getId()) > 1
									&& player.getInventory().getCount(
									primaryIngredient.getId()) > 1) {
								player.getActionSender().sendItemOnInterface(309, 2,
										ingredient.getReward(), 130);
								String itemName = CacheItemDefinition.get(ingredient.getReward()).getName();
								if (vial.getId() == 227) {
									String leafName = CacheItemDefinition
											.get(ingredient.getId()).getName()
											.replaceAll(" leaf", "")
											.replaceAll(" clean", "");
									itemName = leafName + " potion (unf)";
								}
								player.getActionSender().sendString(309, 6,
										"<br><br><br><br>" + itemName);
								player.getActionSender().sendInterface(162, 546, 309, false);
								player.setInterfaceAttribute("herblore_type",
										HerbloreType.PRIMARY_INGREDIENT);
								player.setInterfaceAttribute("herblore_index",
										ingredient.getId());
								player.setInterfaceAttribute("vial_index",
										vial.getId());
							} else {
								player.getActionQueue().addAction(
										new Herblore(player, 1, ingredient, null,
												HerbloreType.PRIMARY_INGREDIENT));
							}
							return;
						}
					}
					SecondaryIngredient ingredient = null;
					for (SecondaryIngredient sIngredient : SecondaryIngredient
							.values()) {
						if (sIngredient.getId() == withItem.getId()
								&& sIngredient.getRequiredItem().getId() == usedItem
								.getId()
								|| sIngredient.getId() == usedItem.getId()
								&& sIngredient.getRequiredItem().getId() == withItem
								.getId()) {
							ingredient = sIngredient;
						}
					}
					if (ingredient != null) {
						if (player.getInventory().getCount(ingredient.getId()) > 1
								&& player.getInventory().getCount(
								ingredient.getRequiredItem().getId()) > 1) {
							player.getActionSender().sendItemOnInterface(309, 2,
									ingredient.getReward(), 130);
							player.getActionSender().sendString(
									309,
									6,
									"<br><br><br><br>"
											+ CacheItemDefinition.get(
											ingredient.getReward())
											.getName());
							player.getActionSender().sendInterface(162, 546, 309, false);
							player.setInterfaceAttribute("herblore_type",
									HerbloreType.SECONDARY_INGREDIENT);
							player.setInterfaceAttribute("herblore_index",
									ingredient.getIndex());
						} else {
							player.getActionQueue().addAction(
									new Herblore(player, 1, null, ingredient,
											HerbloreType.SECONDARY_INGREDIENT));
						}
						return;
					}

					Drink drink1 = Drink.forId(usedItem.getId());
					Drink drink2 = Drink.forId(withItem.getId());
					if (drink1 != null && drink2 != null) {
						if (drink1 != drink2) {
							player.getActionSender().sendMessage(
									"You can't combine these two potions.");
							return;
						}
						int index1 = -1;
						int index2 = -1;
						for (int i = 0; i < drink1.getIds().length; i++) {
							if (drink1.getId(i) == usedItem.getId()) {
								index1 = i + 1;
								break;
							}
						}
						for (int i = 0; i < drink2.getIds().length; i++) {
							if (drink2.getId(i) == withItem.getId()) {
								index2 = i + 1;
								break;
							}
						}
						int doses = index1 + index2;
						int amount = 0;
						Item endPotion1 = null;
						Item endPotion2 = null;
						if (doses < 5) {
							endPotion1 = new Item(drink1.getId(doses - 1), 1);
							endPotion2 = new Item(229, 1);
							amount = doses;
						} else {
							endPotion1 = new Item(drink1.getId(3), 1);
							amount = 4;
							doses -= 4;
							endPotion2 = new Item(drink1.getId(doses - 1), 1);
						}
						player.getInventory().remove(usedItem);
						player.getInventory().remove(withItem);
						player.getInventory().add(endPotion1, usedWithSlot);
						player.getInventory().add(endPotion2, slot);
						player.getActionSender().sendMessage(
								"You have combined the liquid into " + amount
										+ " doses.");
						return;
					}
					break;
				}
		}
	//}


	private void handleMagicOnItem(Player player, Packet packet) {
		int interfaceHash = packet.getBEInt();
		int interfaceId = interfaceHash >> 16;
		int childId = interfaceHash & 0xFFFF;
		int b = packet.getShort();
		int slot = packet.getLEShort();
		int itemId = packet.getShort();
		int spellHash = packet.getBEInt();
		int spellBook = spellHash >> 16;
		int spellId = spellHash & 0xFFFF;

		Item item = player.getInventory().get(slot);
		if (item == null) {
			return;
		}
		//System.out.println("Spell: " + spellId + ", Item: " + item.getId() + ", Amount: " + item.getCount());
		//player.getInventory().remove(slot, new Item(item.getId()));
		Magic magic = new Magic(player);
		magic.handleMagicOnItem(item, spellId - 3, slot);
	}


	public boolean isUntradable(int id) {
		return (id >= 9096 && id <= 9104) || id == 9084 || id == 10828 || id == 10551 || (id >= 7054 && id <= 7062);
	}

	public boolean hasUntradable(Player player, Item item) {
		return isUntradable(item.getId()) && player.getInventory().contains(item.getId()) || player.getBank().getCount(item.getId()) > 0 || player.getEquipment().contains(item.getId());
	}

	public boolean hasUntradable(Player player) {
		for (int i = 7054; i <= 7062; i++) {
			if (player.getInventory().contains(i) || player.getBank().contains(i) || player.getEquipment().contains(i)) {
				return true;
			}
		}
		for (int i = 9096; i <= 9104; i++) {
			if (player.getInventory().contains(i) || player.getBank().contains(i) || player.getEquipment().contains(i)) {
				return true;
			}
		}
		if (player.getInventory().contains(10828) || player.getBank().contains(10828) || player.getEquipment().contains(10828)) {
			return true;
		}
		if (player.getInventory().contains(9084) || player.getBank().contains(9084) || player.getEquipment().contains(9084)) {
			return true;
		}
		if (player.getInventory().contains(10551) || player.getEquipment().contains(10551) || player.getBank().contains(10551)) {
			return true;
		}
		return false;
	}
}
