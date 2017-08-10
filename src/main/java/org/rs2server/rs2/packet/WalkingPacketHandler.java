package org.rs2server.rs2.packet;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PathfindingService;
import org.rs2server.rs2.domain.service.api.content.gamble.DiceGameService;
import org.rs2server.rs2.domain.service.api.content.trade.TradeService;
import org.rs2server.rs2.domain.service.impl.content.trade.TradeServiceImpl;
import org.rs2server.rs2.domain.service.impl.skill.FarmingServiceImpl;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Shop;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.PriceChecker;
import org.rs2server.rs2.model.container.Trade;
import org.rs2server.rs2.model.map.path.DefaultPathFinder;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.Packet;

/**
 * A packet which handles walking requests.
 * @author Graham Edgecombe
 *
 */
public class WalkingPacketHandler implements PacketHandler {

	private final PathfindingService pathfindingService = Server.getInjector().getInstance(PathfindingService.class);
//	private final TradeService tradeService = Server.getInjector().getInstance(TradeService.class);
	//private final DiceGameService diceGameService = Server.getInjector().getInstance(DiceGameService.class);

	@Override
	public void handle(Player player, Packet packet) {
		if(player.getAttribute("cutScene") != null) {
			return;
		}
		//TRADING FIX
		if (player.getInterfaceState().isInterfaceOpen(335)) {
			player.getActionSender().sendMessage("You cannot run away from a trade!");
			return;
		} ///second trade screen
		if (player.getInterfaceState().isInterfaceOpen(334)) {
			player.getActionSender().sendMessage("You cannot run away from a trade!");
			return;
		}
		if(player.getInterfaceAttribute("fightPitOrbs") != null) {
			return;
		}

		if (player.getAttribute("stunned") != null) {
			player.getActionSender().sendMessage("You're stunned!");
			return;
		}
		if (player.getAttribute("busy") != null) {
			return;
		}
		if (player.hasAttribute("questnpc")) {
			player.removeAttribute("questnpc");
		}
		//player.setAttribute("fishing", false);
		player.removeAttribute("isStealing");
		int toY = packet.getShort();
		int toX = packet.getLEShortA();
		final Location destination = Location.create(toX, toY);
		boolean running = packet.get() == 1;

		player.resetInteractingEntity();
		player.getActionQueue().clearAllActions();
		player.getActionManager().stopAction();

		player.getActionSender().removeChatboxInterface();

//		if (player.getTransaction() != null && (player.getInterfaceState().isInterfaceOpen(TradeServiceImpl.TRADE_WIDGET)
//				|| player.getInterfaceState().isInterfaceOpen(TradeServiceImpl.CONFIRMATION_WIDGET))) {
//			tradeService.endTransaction(player.getTransaction(), true);
//		}
		/*if (player.getDiceGameTransaction() != null && (player.getInterfaceState().isInterfaceOpen(TradeServiceImpl.TRADE_WIDGET)
				|| player.getInterfaceState().isInterfaceOpen(TradeServiceImpl.CONFIRMATION_WIDGET))) {
			diceGameService.endTransaction(player.getDiceGameTransaction(), true);
		}*/

		if (player.getInterfaceState().isInterfaceOpen(FarmingServiceImpl.INTERFACE_TOOL_STORE_ID)) {
			player.getActionSender().removeInventoryInterface();
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
			int tab = (int) player.getAttribute("tabmode") == 164 ? 56 : (int)player.getAttribute("tabmode") == 161 ? 58 : 60;
//			player.getActionSender().removeInterfaces(player.getAttribute("tabmode"), tab);
			player.getActionSender().removeInventoryInterface();
		}

		player.getInterfaceState().setOpenShop(-1);
		player.getActionSender().removeAllInterfaces().removeInterface();
		
		if (player.isLighting()) {
			return;
		}
		
		if (player.getAttribute("isStealing") != null && (boolean) player.getAttribute("isStealing")) {
			player.setAttribute("isStealing", false);
		}

		player.getWalkingQueue().reset();

		if(!player.getCombatState().canMove()) {
			if (player.getFrozenBy() == null || (player.getFrozenBy().getLocation().distance(player.getLocation()) >= 12)) {
				player.getCombatState().setCanMove(true);
				player.setFrozenBy(null);
			} else {
				if (player.getAttribute("moveMessage") != null) {
					long l = player.getAttribute("moveMessage");
					if (System.currentTimeMillis() - l <= 3000) {
						return;
					}
				}
				player.setAttribute("moveMessage", System.currentTimeMillis());
				player.getActionSender().sendMessage("A magical force stops you from moving.");
				return;
			}
		}
		if(!player.canEmote()) {
			return; //stops walking during skillcape animations.
		}

		pathfindingService.travel(player, destination);
	}

}
