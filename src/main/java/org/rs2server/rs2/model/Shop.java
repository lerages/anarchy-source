package org.rs2server.rs2.model;

import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.container.Container.Type;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.container.impl.InterfaceContainerListener;
import org.rs2server.rs2.model.container.impl.ShopContainerListener;
import org.rs2server.rs2.model.player.Player;
//import org.rs2server.rs2.model.quests.impl.LostCity;
//import org.rs2server.rs2.model.quests.impl.LostCityStates;
import org.rs2server.rs2.model.quests.impl.LunarDiplomacy;
import org.rs2server.rs2.model.quests.impl.LunarStates;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.tickable.StoppingTick;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.tickable.impl.ItemRestoreTick;
import org.rs2server.util.XMLController;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;


/**
 * Shopping utility class.
 *
 * @author Michael Bull
 */
public class Shop {

	private static NumberFormat format = NumberFormat.getNumberInstance(Locale.ENGLISH);

	/**
	 * The logger instance.
	 */
	private static final Logger logger = Logger.getLogger(Shop.class.getName());

	/**
	 * The list of registered shops.
	 */
	private static List<Shop> shops;

	/**
	 * Returns a shop by its ID.
	 *
	 * @param id The shop ID.
	 * @return The shop.
	 */
	public static Shop forId(int id) {
		return shops.get(id);
	}

	/**
	 * The id of the main stock.
	 */
	private int mainStockId;

	/**
	 * The shops name, as displayed
	 * on the interface.
	 */
	private String name = "Shop";

	/**
	 * The shop's type.
	 */
	private ShopType shopType;

	/**
	 * The shop's currency.
	 */
	private int currency = 995;//995  13307

	/**
	 * The starting stock of this shop.
	 */
	private Item[] mainItems;

	/**
	 * The starting stock of this shop.
	 */
	private Item[] playerItems;

	/**
	 * The default stock of the shop.
	 */
	private Container defaultStock;

	/**
	 * The stock of the shop.
	 */
	private Container mainStock;

	/**
	 * The stock of the shop.
	 */
	private Container playerStock;

	/**
	 * @return the mainStockId
	 */
	public int getMainStockId() {
		return mainStockId;
	}

	/**
	 * @return the mainItems
	 */
	public Item[] getMainItems() {
		return mainItems;
	}

	/**
	 * @return the playerItems
	 */
	public Item[] getPlayerItems() {
		return playerItems;
	}

	/**
	 * @param defaultStock the defaultStock to set
	 */
	public void setDefaultStock(Container defaultStock) {
		this.defaultStock = defaultStock;
	}

	/**
	 * @param mainStock the mainStock to set
	 */
	public void setMainStock(Container mainStock) {
		this.mainStock = mainStock;
	}

	/**
	 * @param playerStock the playerStock to set
	 */
	public void setPlayerStock(Container playerStock) {
		this.playerStock = playerStock;
	}

	/**
	 * Gets the shop's main stock.
	 *
	 * @return The shop's main stock.
	 */
	public Container getMainStock() {
		return mainStock;
	}

	/**
	 * Gets the shop's player stock.
	 *
	 * @return The shop's player stock.
	 */
	public Container getPlayerStock() {
		return playerStock;
	}

	/**
	 * Gets the shop's default stock.
	 *
	 * @return The shop's default stock.
	 */
	public Container getDefaultStock() {
		return defaultStock;
	}

	/**
	 * Gets the shop's name.
	 *
	 * @return The shop's name.
	 */
	public String getShopName() {
		return name;
	}

	/**
	 * Gets the shop's type.
	 *
	 * @return The shop's type.
	 */
	public ShopType getShopType() {
		return shopType;
	}

	/**
	 * Gets shop's currency.
	 *
	 * @return The shop's currency.
	 */
	public int getCurrency() {
		return currency;
	}

	/**
	 * Creates a new shop instance.
	 *
	 * @param name     The shop's name.
	 * @param shopType The shop's type.
	 * @param currency The shop's currency.
	 */
	public Shop(String name, int mainStockId, ShopType shopType, int currency, Item[] mainItems, Item[] playerItems) {
		this.name = name;
		this.mainStockId = mainStockId;
		this.shopType = shopType;
		this.currency = currency;
		this.mainItems = mainItems;
		this.playerItems = playerItems;
	}

	public static enum ShopType {

		/**
		 * A general store, which takes all items, buys items for 0.4 of their price
		 * and sells them for 0.8 of their price. Default stock will go to 0, and
		 * non-default will be removed if the stock is < 1.
		 */
		GENERAL_STORE,

		/**
		 * A specialist store that does buy items that are in its stock (none others though!).
		 * It will buy items for 0.6 of their price, and sell them for 1.0
		 */
		SPECIALIST_STORE_BUY,

		/**
		 * A specialist store that will not buy any stock. It will sell items for 1.0 of their
		 * price.
		 */
		SPECIALIST_STORE_NO_BUY;
	}

	public static void init() throws IOException {
		if (shops != null) {
			throw new IllegalStateException("Shops already loaded.");
		}
		logger.info("Loading Shops definitions...");
		File file = new File("data/shops.xml");
		shops = new ArrayList<Shop>();
		if (file.exists()) {
			shops = XMLController.readXML(file);
			logger.info("Loaded " + shops.size() + " shops.");
		} else {
			logger.info("Shops not found.");
		}
		//		Shop test = new Shop("474 Project is cool!", 860, ShopType.GENERAL_STORE, 995, new Item[] { new Item(4151, 5) }, new Item[] { new Item(4151, 5) });		
		//		shops.add(test);
		//		XMLController.writeXML(shops, file);
		for (Shop shop : shops) {
			Container defaultStock = new Container(Type.ALWAYS_STACK, SIZE);
			Container playerStock = new Container(Type.ALWAYS_STACK, SIZE);
			Container mainStock = new Container(Type.ALWAYS_STACK, SIZE);
			if (shop.getMainItems() != null) {
				for (Item item : shop.getMainItems()) {
					if (item != null) {
						mainStock.add(item);
						defaultStock.add(item);
					}
				}
			}
			if (shop.getPlayerItems() != null) {
				for (Item item : shop.getPlayerItems()) {
					if (item != null) {
						playerStock.add(item);
						defaultStock.add(item);
					}
				}
			}
			shop.setDefaultStock(defaultStock);
			shop.setPlayerStock(playerStock);
			shop.setMainStock(mainStock);
		}
	}

	/**
	 * The shop size.
	 */
	public static final int SIZE = 40;
	
	public static final int TOURNAMENT_SIZE = 293;
	

	/**
	 * The player inventory interface.
	 */
	public static final int PLAYER_INVENTORY_INTERFACE = 301;

	/**
	 * The shop inventory interface.
	 */
	public static final int SHOP_INVENTORY_INTERFACE = 300;//300
	
	public static final int TOURNAMENT_INVENTORY_INTERFACE = 100;

	/**
	 * The shop's main stock.
	 */
	public static final int SHOP_MAIN_STOCK = 23;//23

	/**
	 * The shop's player stock.
	 */
	public static final int SHOP_PLAYER_STOCK = 24;//24

	public static final int[] RFD_GLOVES = {7453, 7454, 7455, 7456, 7457, 7458, 7459, 7460, 7461, 7462};
	
	public static final int[] NON_IRONMAN_STOCK = {1163, 4131, 1079, 1093, 1201, 3105, 1127, 1333, 892, 890, 861, 9185,
			9143, 810, 867, 2489, 2495, 2501, 1065, 1099, 1135, 1727, 4675, 1725, 1731, 1213, 1373, 1377, 1434, 1355, 
			1357, 379, 373, 3144};

	/**
	 * Opens the shop for the specified player.
	 *
	 * @param player The player to open the shop for.
	 */
	
	
	//IIIIIIIIIIIII
	public static void open(Player player, int shopId, int stockType) {
		player.getActionSender().sendConfig(118, 17);
		player.getActionSender().sendInterface(SHOP_INVENTORY_INTERFACE, false);
		player.getActionSender().sendInterfaceInventory(PLAYER_INVENTORY_INTERFACE);
		player.getInterfaceState().setOpenShop(shopId);
		Shop shop = shops.get(player.getInterfaceState().getOpenShop());
		player.getActionSender()
				.sendCS2Script(917, new Object[]{-1, -1}, "ii")
				.sendCS2Script(1074, new Object[]{shop.getShopName(), 51}, "vs")
				.sendAccessMask(1278, 300, 2, 0, 40)//39 1054   .sendAccessMask(1278, 300, 2, 0, 40)   1278  1054 1074
				.sendCS2Script(149, Constants.SELL_PARAMETERS, "IviiiIsssss")
				.sendAccessMask(1086, 301, 0, 0, 27);
				//.sendInterfaceConfig(100, 3, true);
		player.getInterfaceState().addListener(shop.getMainStock(), new ShopContainerListener(player, shopId, -1, 64251, 51));//51   , -1, 64251, 51
		player.getInterfaceState().addListener(player.getInventory(), new InterfaceContainerListener(player, -1, 64209, 93));
		player.getInterfaceState().setOpenStockType(2);
		//player.getActionSender().sendString(SHOP_INVENTORY_INTERFACE, 76, shop.getShopName());
		final PermissionService permissionService;
		permissionService = Server.getInjector().getInstance(PermissionService.class);
		
		/*if (shopId == 13) {
			int rfdState = player.getSettings().getRFDState();
				for (int i = 0; i < rfdState; i++) 
				{
					if(i < 10)
					{
						if (shop.getMainStock().contains(RFD_GLOVES[i])) 
						{
							continue;
						}
						shop.getMainStock().add(new Item(RFD_GLOVES[i], 25));
					}
				}*/
			/*} else {
				for (int i = 0; i < questFinished; i++) {
					if (shop.getMainStock().contains(RFD_GLOVES[i])) {
						continue;
					}
					shop.getMainStock().add(new Item(RFD_GLOVES[i], 25));
				}
			}*/
		//}
		if (shopId == 32) {
			for (int i = 0; i < Constants.QUEST_SHOP.length; i++) {
				if (!Inventory.ownsItem(player, Constants.QUEST_SHOP[i])) {
					if (!shop.getMainStock().contains(Constants.QUEST_SHOP[i])) {
						shop.getMainStock().add(new Item(Constants.QUEST_SHOP[i], 1));
					}
				} else if (Inventory.ownsItem(player, Constants.QUEST_SHOP[i]) && shop.getMainStock().contains(Constants.QUEST_SHOP[i])) {
					shop.getMainStock().removeAll(new Item(Constants.QUEST_SHOP[i]));
				}
			}
		}
		
		if (shopId == 41) {
			for (Item item : shop.getMainItems()) {
				if (item == null || item.getDefinition() == null) { continue; }
				item.getDefinition().setStorePrice(getCustomPrice(item));
			}
		} else {
			ItemDefinition.loadExchangePrices();
		}
	}

	public static int getCustomPrice(Item item) {
		switch (item.getId()) {
			case 1038:
				return 15000;
			case 1040:
				return 13000;
			case 1042:
				return 15000;
			case 1044:
				return 13000;
			case 1046:
				return 13000;
			case 1048:
				return 15000;
			case 1050:
				return 10000;
			case 1053:
				return 12000;
			case 1055:
				return 12000;
			case 1057:
				return 12000;
			case 6914:
				return 7500;
			case 6889:
				return 7500;
			case 6918:
				return 5000;
			case 6916:
				return 6500;
			case 6924:
				return 6500;
			case 6920:
				return 4000;
			case 6922:
				return 2000;
			case 13124:
				return 5000;
			case 4224:
				return 7000;
			case 4212:
				return 9000;
			case 12526:
				return 4000;
		}
		return -1;
	}

	/**
	 * Sells an item.
	 *
	 * @param player The player.
	 * @param slot   The slot in the player's inventory.
	 * @param id     The item id.
	 * @param amount The amount of the item to sell.
	 */
	public static void sellItem(Player player, int slot, int id, int amount) {
		player.getActionSender().removeChatboxInterface();
		Shop shop = shops.get(player.getInterfaceState().getOpenShop());
		Item item = player.getInventory().get(slot);
		if (item == null) {
			return; // invalid packet, or client out of sync
		}
		if (item.getId() != id) {
			return; // invalid packet, or client out of sync
		}
		if (!item.getDefinition().isTradable()) {
			player.getActionSender().sendMessage("You cannot sell this item.");
			return;
		}
		int transferAmount = player.getInventory().getCount(id);
		if (amount >= transferAmount) {
			amount = transferAmount;
		} else if (transferAmount == 0) {
			return; // invalid packet, or client out of sync
		}
		boolean canSell = false;
		if (shop.getShopType() == ShopType.SPECIALIST_STORE_BUY) {
			if (shop.getMainStock().contains(item.getId()) || shop.getDefaultStock().contains(item.getId())) {
				canSell = true;
			}
		}
		if (shop.getShopType() == ShopType.GENERAL_STORE) {
			canSell = true;
		}
		if (item.getId() == shop.getCurrency()) {
			canSell = false;
		}
		if (canSell) {
			Shop.open(player, player.getInterfaceState().getOpenShop(), 2); //Forces open the player stock
			int shopSlot = shop.getMainStock().contains(item.getId()) ? shop.getMainStock().getSlotById(item.getId()) : shop.getMainStock().freeSlot();
			if (shopSlot == -1) {
				player.getActionSender().sendMessage("This shop is currently full.");
			} else {
				if (shop.getMainStock().get(shopSlot) != null) {
					if (shop.getMainStock().get(shopSlot).getCount() + amount < 1 || shop.getMainStock().get(shopSlot).getCount() + amount > Constants.MAX_ITEMS) {
						player.getActionSender().sendMessage("This shop is currently full.");
						return;
					}
				}
				long totalAmount = amount;
				long totalValue = shop.getSellValue(player, item);
				long totalPrice = (totalAmount * totalValue);
				if (totalPrice > Integer.MAX_VALUE) {
					amount = (Integer.MAX_VALUE / shop.getSellValue(player, item)) - 1;
				}
				Item reward = new Item(shop.getCurrency(), amount * shop.getSellValue(player, item));
				/*
				 * We make a temporary inventory container, why? This is because if we have 1 AGS, and 27 whips, we would have no free slot for the coins.
				 * Now on most servers, if you tried to sell the AGS, it would say "not enough inventory space", which is true, but wrong. On RS, it removes
				 * the AGS first, THEN adds the coins. However, we also want to keep our checks for inventory space in there, so we do it on a temporary
				 * inventory first.
				 */
				Container temporaryInventory = new Container(Type.STANDARD, Inventory.SIZE);
				for (Item invItem : player.getInventory().toArray()) {
					temporaryInventory.add(invItem);
				}
				temporaryInventory.remove(new Item(item.getId(), amount));
				if (!temporaryInventory.add(reward)) {
					return; //We wouldn't have enough inventory space, even after removing the sold item.
				}
				player.getInventory().remove(new Item(item.getId(), amount));
				shop.getMainStock().add(new Item(item.getId(), amount));
				player.getInventory().add(reward);
			}
		} else {
			player.getActionSender().sendMessage("This shop will not buy that item.");
		}
	}

	/**
	 * Buys an item.
	 *
	 * @param player The player.
	 * @param slot   The slot in the player's inventory.
	 * @param id     The item id.
	 * @param amount The amount of the item to buy.
	 */
	public static void buyItem(Player player, int slot, int id, int amount) {
		if (player.getInterfaceState().getOpenShop() == -1) {
			return;
		}

		final PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
		Shop shop = shops.get(player.getInterfaceState().getOpenShop());
		Item item = shop.getMainStock().get(slot - 1);
		
		if (item == null || item.getId() != id || shop.getCostValue(player, item) < 0) {
			return; // invalid packet, or client out of sync
		}
		if (shop.getShopName().equalsIgnoreCase("Donator Store") && shop.getCurrency() != 13204) {
			return;
		}
		/*if (permissionService.isAny(player, PermissionService.PlayerPermissions.IRON_MAN, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN) && player.getInterfaceState().getOpenShop() == 2) {
			player.getActionSender().sendMessage("Sorry you may not purchase from this shop.");
			return;
		}*/
		
		//if (item.getId() == 1215 || item.getId() == 1305 || item.getId() == 5698) {
		//	LostCity lost = (LostCity) player.getQuests().get(LostCity.class);
		//	if (lost == null || lost.getState() != LostCityStates.COMPLETED) {
		//		player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 772, null, "You need to complete Lost City before purchasing these items.");
				//return;
		//	}
		//}
		
		if(permissionService.is(player, PermissionService.PlayerPermissions.IRON_MAN)
				|| permissionService.is(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN)
				|| permissionService.is(player, PermissionService.PlayerPermissions.HARDCORE_IRON_MAN))
		{
			switch(player.getInterfaceState().getOpenShop())
			{
			case 3:
			case 5:
			case 7:
			case 8:
			case 9:
				for(int i = 0; i < NON_IRONMAN_STOCK.length; i++)
				{
					//shop.getMainStock().removeAll(new Item(RUNE_ITEMS[i]));
					if(item.getId() == NON_IRONMAN_STOCK[i])
					{
						player.getActionSender().sendMessage("Iron Men are not permitted to buy this item from this shop.");
						return;
					}
				}
				break;
			}
			
			if(shop.getShopType() == ShopType.GENERAL_STORE)
			{
				if(!shop.getDefaultStock().contains(item.getId()))
				{
					player.getActionSender().sendMessage("Iron Men cannot buy items other player have sold.");
					return;
				}
			}
		}

		int transferAmount = player.getInventory().freeSlots();
		if (amount >= transferAmount && (!ItemDefinition.forId(item.getId()).isStackable())) {
			amount = transferAmount;
		} else if (transferAmount == 0) {
			return; // invalid packet, or client out of sync
		}
		if (transferAmount > 200) {
			transferAmount = 200;
			player.getActionSender().sendMessage("You cannot buy more than 200 items at a time.");
		}
		if (shop.getMainStock().get(slot - 1).getCount() > 0) {
			if (amount >= shop.getMainStock().get(slot -1).getCount()) {
				amount = shop.getMainStock().get(slot - 1).getCount();
			}
			if (!shop.hasCurrency(player, item, amount)) {
				player.getActionSender().sendMessage("You don't have enough " + CacheItemDefinition.get(shop.getCurrency()).getName().toLowerCase() + ".");
			} else {
				Item reward = new Item(item.getId(), amount);
				Container temporaryInventory = new Container(Type.STANDARD, Inventory.SIZE);
				for (Item invItem : player.getInventory().toArray()) {
					temporaryInventory.add(invItem);
				}
				temporaryInventory.remove(new Item(shop.getCurrency(), amount * shop.getCostValue(player, new Item(item.getId(), amount))));
				if (!temporaryInventory.add(reward)) {
					return; //We wouldn't have enough inventory space, even after removing the currency cost.
				}
				player.getInventory().remove(new Item(shop.getCurrency(), amount * shop.getCostValue(player, new Item(item.getId(), amount))));
				player.getInventory().add(reward);
				shop.getMainStock().remove(reward);
				for (Item i : shop.getMainItems()) {
					if (reward.getId() == i.getId() && shop.getMainStock().get(slot - 1) == null) {
						shop.getMainStock().add(new Item(reward.getId(), 0));
					}
				}
				World.getWorld().submit(new ItemRestoreTick(item, shop, slot - 1));
			}
		}

	}

	public boolean hasCurrency(Player player, Item item, int amt) {
		Shop shop = shops.get(player.getInterfaceState().getOpenShop());
		int finalAmt = getCostValue(player, item);
		if (finalAmt == -1) {
			player.getActionSender().sendMessage("Currency Error.");
			return false;
		}
		finalAmt *= amt;
		return player.getInventory().getCount(shop.getCurrency()) >= finalAmt;
	}

	public static void resetAllShops() {
		try {
			Shop.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getCostValue(Player player, Item item) {
		if (player.getInterfaceState().getOpenShop() == 21 || player.getInterfaceState().getOpenShop() == 22 || player.getInterfaceState().getOpenShop() == 24) {
			return getTzhaarBuyValue(item);
		}
		switch (player.getInterfaceState().getOpenStockType()) {
			case 1: //Main stock
				switch (getShopType()) {
					case GENERAL_STORE:
						return (int) (ItemDefinition.forId(item.getId()).getStorePrice() * 0.9 < 1 ? 1 : ItemDefinition.forId(item.getId()).getStorePrice() * 0.9);
					case SPECIALIST_STORE_BUY:
					case SPECIALIST_STORE_NO_BUY:
						return (int) ItemDefinition.forId(item.getId()).getStorePrice();
				}
				break;
			case 2: //Player stock
				switch (getShopType()) {
					case GENERAL_STORE:
						return (int) (ItemDefinition.forId(item.getId()).getStorePrice() * 0.9 < 1 ? 1 : ItemDefinition.forId(item.getId()).getStorePrice() * 0.9);
					case SPECIALIST_STORE_BUY:
					case SPECIALIST_STORE_NO_BUY:
						return ItemDefinition.forId(item.getId()).getStorePrice();
				}
				break;
		}
		return 1;
	}

	public int getSellValue(Player player, Item item) {
		if (player.getInterfaceState().getOpenShop() == 21 || player.getInterfaceState().getOpenShop() == 22 || player.getInterfaceState().getOpenShop() == 24) {
			return getTzhaarSellValue(item);
		}
		switch (getShopType()) {
			case GENERAL_STORE:
				return (int) (ItemDefinition.forId(item.getId()).getStorePrice() * 0.6 < 1 ? 1 : ItemDefinition.forId(item.getId()).getStorePrice() * 0.6);
			case SPECIALIST_STORE_BUY:
			case SPECIALIST_STORE_NO_BUY:
				return (int) (ItemDefinition.forId(item.getId()).getStorePrice() * 0.6 < 1 ? 1 : ItemDefinition.forId(item.getId()).getStorePrice() * 0.6);
		}
		return 1;
	}

	private static int getTzhaarSellValue(Item item) {
		switch (item.getId()) {
			case 6522:
				return 74;
			case 6523:
				return 12000;
			case 6524:
				return 13500;
			case 6525:
				return 7500;
			case 6526:
				return 10500;
			case 6527:
				return 9000;
			case 6528:
				return 15000;
			case 6568:
				return 18000;
			case 438:
				return 1;
			case 436:
				return 1;
			case 440:
				return 2;
			case 442:
				return 11;
			case 453:
				return 6;
			case 444:
				return 22;
			case 447:
				return 24;
			case 449:
				return 60;
			case 451:
				return 480;
			case 1623:
				return 3;
			case 1621:
				return 7;
			case 1619:
				return 15;
			case 1617:
				return 30;
			case 1631:
				return 150;
			case 6571:
				return 30000;
			case 9194:
				return 150;
			case 554:
				return 0;
			case 555:
				return 0;
			case 556:
				return 0;
			case 557:
				return 0;
			case 558:
				return 0;
			case 559:
				return 0;
			case 560:
				return 18;
			case 562:
				return 9;
			default:
				return 0;
		}
	}

	private static int getTzhaarBuyValue(Item item) {
		switch (item.getId()) {
			case 6522:
				return 375;
			case 6523:
				return 60000;
			case 6524:
				return 67500;
			case 6525:
				return 37500;
			case 6526:
				return 52500;
			case 6527:
				return 45000;
			case 6528:
				return 75000;
			case 6568:
				return 90000;
			case 438:
				return 4;
			case 436:
				return 4;
			case 440:
				return 25;
			case 442:
				return 112;
			case 453:
				return 67;
			case 444:
				return 225;
			case 447:
				return 243;
			case 449:
				return 600;
			case 451:
				return 4800;
			case 1623:
				return 37;
			case 1621:
				return 75;
			case 1619:
				return 150;
			case 1617:
				return 300;
			case 1631:
				return 1500;
			case 6571:
				return 300000;
			case 9194:
				return 1500;
			case 554:
				return 6;
			case 555:
				return 6;
			case 556:
				return 6;
			case 557:
				return 6;
			case 558:
				return 4;
			case 559:
				return 4;
			case 560:
				return 45;
			case 562:
				return 270;
			default:
				return 0;
		}
	}


	public static void costItem(Player player, int slot, int id) {
		//If the shop open is returning null, return..
		if (player.getInterfaceState().getOpenShop() == -1) {
			System.out.println("Returning null.. shop is null.");
			return;
		}
		
		Shop shop = shops.get(player.getInterfaceState().getOpenShop());
		Item item = shop.getMainStock().get(slot - 1);
		
		//If the packet is sending an item that doesn't excist, return null.
//		if (item == null || item.getId() != id) {
//			System.out.println("Returning null.. client out of sync.");
//			return; 
//		}
		
		String name = item.getDefinition2().getName();
		
		if (item.getDefinition().isNoted()) {
			name = CacheItemDefinition.get(item.getId() - 1).getName();
		}
		
		player.getActionSender().sendMessage(item.getDefinition2().getName() + ": currently costs " +
		(player.getInterfaceState().getOpenShop() == 21 || player.getInterfaceState().getOpenShop() == 22
		|| player.getInterfaceState().getOpenShop() == 24 ? getTzhaarBuyValue(item) 
				: shop.getCostValue(player, item)) + " " + CacheItemDefinition.get(shop.getCurrency()).getName().toLowerCase() + ".");
	}
	

	public static void valueItem(Player player, int slot, int id) {
		Shop shop = shops.get(player.getInterfaceState().getOpenShop());
		Item item = player.getInventory().get(slot);
		
		if (item == null || item.getId() != id) {
			System.out.println("Client is out of sync.. item ID incorrect");
			return;
		}
		
		
		boolean message = false;
		
		if (shop.getShopType() == ShopType.GENERAL_STORE) {
			message = true;
		}
		
		if (shop.getShopType() == ShopType.SPECIALIST_STORE_BUY) {
			if (shop.getMainStock().contains(item.getId()) || shop.getDefaultStock().contains(item.getId())) {
				message = true;
			}
		}
		
		int finalValue = player.getInterfaceState().getOpenShop() == 21 || player.getInterfaceState().getOpenShop() == 22 || player.getInterfaceState().getOpenShop() == 24 ? getTzhaarSellValue(item) : shop.getSellValue(player, item);
		String shopAdd = "";
		
		/*if (finalValue >= 1000 && finalValue < 1000000) {
			shopAdd = "(" + (finalValue / 1000) + "K).";
		} else if (finalValue >= 1000000) {
			shopAdd = "(" + (finalValue / 1000000) + " million).";
		}*/
		
		if(item.getDefinition().isTradable())
		{
			player.getActionSender().sendMessage(message ? CacheItemDefinition.get(item.getId()).getName() +
					": shop will buy for " + finalValue + " " + CacheItemDefinition.get(shop.getCurrency()).getName().toLowerCase() + " " 
					+ shopAdd : "This shop owner has no interest in a " + item.getDefinition2().getName() + ".");
		}
		else
		{
			player.getActionSender().sendMessage("You cannot sell this item.");
		}
	}

	public static void reloadShops() {
		shops = null;
		try {
			Shop.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}