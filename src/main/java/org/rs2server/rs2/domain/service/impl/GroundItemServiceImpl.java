package org.rs2server.rs2.domain.service.impl;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.rs2server.Server;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.content.api.GamePlayerItemPickupEvent;
import org.rs2server.rs2.content.api.GamePlayerLoginEvent;
import org.rs2server.rs2.content.api.GamePlayerLogoutEvent;
import org.rs2server.rs2.content.api.GamePlayerRegionEvent;
import org.rs2server.rs2.content.areas.CoordinateEvent;
import org.rs2server.rs2.domain.model.player.treasuretrail.clue.ClueScrollType;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.ObeliskService;
import org.rs2server.rs2.domain.service.impl.skill.FarmingServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.container.*;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.model.region.RegionManager;
import org.rs2server.rs2.tickable.Tickable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.stream.Collectors.toList;
/**
 * @author Clank1337
 */
public class GroundItemServiceImpl implements GroundItemService {

	public static final Map<GroundItem, Integer> GROUND_ITEMS = new HashMap<>();

	private static final String DEFAULT_OWNER = "";
	private static final Animation BONE_BURYING_ANIMATION = Animation.create(827);

	private final PermissionService permissionService;
	private final PlayerService playerService;
	private final BankPinService bankPinService;

	@Inject
	public GroundItemServiceImpl(HookService service, PermissionService permissionService, PlayerService playerService, BankPinService bankPinService) {
		service.register(this);
		this.permissionService = permissionService;
		this.playerService = playerService;
		this.bankPinService = bankPinService;
	}

	@Override
	public void createGroundItem(@Nullable Player player, @Nonnull GroundItem groundItem) {
		Item item = groundItem.getItem();
		if (player == null) {
			Optional<GroundItem> currentOptional = getGroundItem(groundItem.getItem().getId(), groundItem.getLocation());
			if (currentOptional.isPresent() && currentOptional.get().getItem().getDefinition().isStackable()) {
				GroundItem current = currentOptional.get();
				GROUND_ITEMS.put(groundItem, current.getItem().getCount() + groundItem.getItem().getCount());
			} else {
				GROUND_ITEMS.put(groundItem, item.getCount());
			}
			return;
		}
		if (item.getDefinition().isStackable()) {
			Optional<GroundItem> optional = getGroundItem(item.getId(), groundItem.getLocation());
			if (optional.isPresent()) {

				GroundItem i = optional.get();
				if (!i.isGlobal()) {
					int newCount = GROUND_ITEMS.get(i) + item.getCount();
					if (newCount > 0) {
						player.getActionSender().removeGroundItem(i);

						GROUND_ITEMS.put(i, GROUND_ITEMS.get(i) + item.getCount());
						i.getItem().increaseCount(item.getCount());
						player.getActionSender().sendGroundItem(i);

						registerGroundItemEvent(i);
					} else {
						player.getActionSender().sendGroundItem(groundItem);
						GROUND_ITEMS.put(groundItem, item.getCount());

						if (groundItem.isGlobal()) {
							addItemForRegionalPlayers(player, groundItem);
						}

						registerGroundItemEvent(groundItem);
					}
				} else {
					player.getActionSender().sendGroundItem(groundItem);
					GROUND_ITEMS.put(groundItem, item.getCount());

					if (groundItem.isGlobal()) {
						addItemForRegionalPlayers(player, groundItem);
					}

					registerGroundItemEvent(groundItem);
				}
			} else {
				GROUND_ITEMS.put(groundItem, item.getCount());
				player.getActionSender().sendGroundItem(groundItem);

				if (groundItem.isGlobal()) {
					addItemForRegionalPlayers(player, groundItem);
				}
				registerGroundItemEvent(groundItem);
			}
		} else {
			/**
			 * 	for (int i = 0; i < item.getCount(); i++) {
			 GroundItem newGroundItem = new GroundItem(new Item(groundItem.getItem().getId(), 1), groundItem.getLocation(), groundItem.getOwner(), groundItem.isGlobal(), groundItem.isPvP(), groundItem.isHour());
			 player.getActionSender().sendGroundItem(newGroundItem);
			 GROUND_ITEMS.put(newGroundItem, i);

			 if (newGroundItem.isGlobal()) {
			 addItemForRegionalPlayers(player, newGroundItem);
			 }

			 registerGroundItemEvent(newGroundItem);
			 }
			 */
			player.getActionSender().sendGroundItem(groundItem);
			GROUND_ITEMS.put(groundItem, item.getCount());

			if (groundItem.isGlobal()) {
				addItemForRegionalPlayers(player, groundItem);
			}

			registerGroundItemEvent(groundItem);
		}
	}

	@Override
	public void removeGroundItem(@Nonnull GroundItem groundItem) {
		Optional<GroundItem> optional = getGroundItem(groundItem.getItem().getId(), groundItem.getLocation());
		if (optional.isPresent()) {
			GroundItem item = optional.get();
			final Player targetPlayer = playerService.getPlayer(item.getOwner());
			if (targetPlayer != null) {
				targetPlayer.getActionSender().removeGroundItem(item);
			}
			World.getWorld().getRegionManager().getRegionByLocation(item.getLocation()).
					getPlayers().stream().forEach(i -> i.getActionSender().removeGroundItem(item));
			GROUND_ITEMS.remove(item);
		}
	}

	@Override
	public void pickupGroundItem(@Nonnull Player player, Item item, Location location) {
		World.getWorld().submitAreaEvent(player, new CoordinateEvent(player, location.getX(), location.getY(), 1, 1, 0) {

			@Override
			public void execute() {
				Optional<GroundItem> optional = getGroundItem(item.getId(), location);
				if (optional.isPresent()) {
					GroundItem groundItem = optional.get();
					/*if (ClueScrollType.forClueScrollItemId(groundItem.getItem().getId()) != null && Server.getInjector().getInstance(PlayerService.class).hasItemInInventoryOrBank(player, groundItem.getItem())) {
						return;
					}*/
					/*if ((groundItem.getItem().getId() == 12179 || groundItem.getItem().getId() == 12029 || groundItem.getItem().getId() == 12542 || groundItem.getItem().getId() == 12073) &&
							(Inventory.ownsItem(player, 12179) || Inventory.ownsItem(player, 12029) || Inventory.ownsItem(player, 12542) || Inventory.ownsItem(player, 12073))) {
						player.getActionSender().sendMessage("Please finish your other clue before looting another.");
						return;
					}*/
					if (permissionService.isAny(player, PermissionService.PlayerPermissions.IRON_MAN, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN)) {
						if (groundItem.getItem().isPvpDrop() || (!groundItem.getOwner().equals(player.getName()) && !(groundItem instanceof SpawnedGroundItem) && !groundItem.getOwner().equals(DEFAULT_OWNER))) {
							return;
						}
					}
					player.getActionSender().removeChatboxInterface();

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

					HookService hookService = Server.getInjector().getInstance(HookService.class);
					hookService.post(new GamePlayerItemPickupEvent(player, groundItem));
					player.getInterfaceState().setOpenShop(-1);
					player.getActionSender().removeAllInterfaces().removeInterface();
					if (player.getLocation().withinRange(groundItem.getLocation(), 0)) {
						if (player.getInventory().add(groundItem.getItem())) {
							player.getActionSender().removeGroundItem(groundItem);
							GROUND_ITEMS.remove(groundItem);
							removeItemForRegionalPlayers(player, groundItem);
							player.getActionSender().playSound(Sound.PICKUP);
							player.playAnimation(Animation.create(-1));
						}
					} else if (player.getLocation().withinRange(groundItem.getLocation(), 1) && player.getWalkingQueue().isEmpty()) {
						if (player.getInventory().add(groundItem.getItem())) {
							player.face(groundItem.getLocation());
							player.playAnimation(BONE_BURYING_ANIMATION);
							player.getActionSender().removeGroundItem(groundItem);
							GROUND_ITEMS.remove(groundItem);
							removeItemForRegionalPlayers(player, groundItem);
							player.playAnimation(Animation.create(-1));
							player.getActionSender().playSound(Sound.PICKUP);
						}
					}
				}
			}

		});
	}

	@Override
	public void teleGrabItem(@Nonnull Player player, @Nonnull GroundItem item) {
		/*if ((item.getItem().getId() == 12179 || item.getItem().getId() == 12029 || item.getItem().getId() == 12542 || item.getItem().getId() == 12073) &&
				(Inventory.ownsItem(player, 12179) || Inventory.ownsItem(player, 12029) || Inventory.ownsItem(player, 12542) || Inventory.ownsItem(player, 12073))) {
			player.getActionSender().sendMessage("Please finish your other clue before looting another.");
			return;
		}*/
		if (permissionService.isAny(player, PermissionService.PlayerPermissions.IRON_MAN, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN)) {
			if (item.getItem().isPvpDrop() || (!item.getOwner().equals(player.getName()) && !(item instanceof SpawnedGroundItem) && !item.getOwner().equals(DEFAULT_OWNER))) {
				return;
			}
		}
		player.getActionSender().removeChatboxInterface();

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
		HookService hookService = Server.getInjector().getInstance(HookService.class);
		hookService.post(new GamePlayerItemPickupEvent(player, item));
		player.getInterfaceState().setOpenShop(-1);
		player.getActionSender().removeAllInterfaces().removeInterface();
		if (player.getLocation().withinRange(item.getLocation(), 6)) {
			if (player.getInventory().add(item.getItem())) {
				
				player.getActionSender().removeGroundItem(item);
				GROUND_ITEMS.remove(item);
				removeItemForRegionalPlayers(player, item);
			}
		}
	}


	@Override
	public void refresh(@Nonnull Player player) {
		GROUND_ITEMS.keySet().stream().
				filter(g -> g.getLocation().distance(player.getLocation()) <= RegionManager.REGION_SIZE).filter(i -> i.isGlobal() || i.getOwner().equalsIgnoreCase(player.getName()))
				.forEach(i -> player.getActionSender().sendGroundItem(i));
	}

	@Override
	public void addItemForRegionalPlayers(@Nonnull Player player, GroundItem item) {
		player.getRegion().getPlayers().stream().filter(p -> p != player).forEach(i -> i.getActionSender().sendGroundItem(item));
	}

	@Override
	public void removeItemForRegionalPlayers(@Nonnull Player player, GroundItem item) {
		player.getRegion().getPlayers().stream().filter(p -> p != player).forEach(i -> i.getActionSender().removeGroundItem(item));
	}

	@Override
	public void registerGroundItemEvent(GroundItem item) {
		World.getWorld().submit(new GroundItemTick(item));
	}


	@Subscribe
	public void onRegionChange(final GamePlayerRegionEvent event) {
		final Player player = event.getPlayer();
		if (player.isActive()) {
			refresh(player);
		}
	}

	@Subscribe
	public void onPlayerLogin(final GamePlayerLoginEvent event) {
		final Player player = event.getPlayer();
		refresh(player);
	}

	@Subscribe
	public void onPlayerLogout(final GamePlayerLogoutEvent event) {
		final Player player = event.getPlayer();
		if (player.isActive()) {
			GROUND_ITEMS.keySet().stream().
					filter(g -> player.getLocation().isWithinDistance(g.getLocation())).
					filter(i -> i.getOwner().equals(player.getName())).forEach(o -> player.getActionSender().removeGroundItem(o));
		}
	}


	@Override
	public Optional<GroundItem> getGroundItem(int id, Location location) {
		return GROUND_ITEMS.keySet().stream().filter(g -> g.getItem().getId() == id).filter(i -> i.getLocation().equals(location)).findAny();
	}

	@Override
	public List<GroundItem> getPlayerDroppedItems(String name) {
		return GROUND_ITEMS.keySet().stream().filter(o -> o.getOwner().equalsIgnoreCase(name)).collect(toList());
	}

}
