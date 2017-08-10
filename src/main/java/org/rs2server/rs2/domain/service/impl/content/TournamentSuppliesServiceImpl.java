package org.rs2server.rs2.domain.service.impl.content;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.content.api.GameInterfaceButtonEvent;
import org.rs2server.rs2.domain.model.player.PlayerBountyHunterEntity;
import org.rs2server.rs2.domain.service.api.GroundItemService;
import org.rs2server.rs2.domain.service.api.HookService;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.domain.service.api.PlayerVariableService;
import org.rs2server.rs2.domain.service.api.content.ItemService;
import org.rs2server.rs2.domain.service.api.content.TournamentSuppliesService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.AccessBits;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.player.Player;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class TournamentSuppliesServiceImpl implements TournamentSuppliesService {
	
	private final PlayerVariableService variableService;
	private final ItemService itemService;
	private final GroundItemService groundItemService;
	private final PlayerService playerService;
	
	private static final Random RANDOM = new Random();
	
	private static final int TOURNAMENT_WIDGET_ID = 100;
	
	private static final Access TOURNAMENT_SHOP_ACCESS = Access.of(TOURNAMENT_WIDGET_ID, 3, NumberRange.of(0, 293), AccessBits.optionBit(10));

	@Inject
	TournamentSuppliesServiceImpl(PlayerVariableService variableService, ItemService itemService, HookService hookService, GroundItemService groundItemService, PlayerService playerService) {
		this.variableService = variableService;
		this.itemService = itemService;
		this.groundItemService = groundItemService;
		this.playerService = playerService;
		hookService.register(this);
	}
	
	private static int getAmountForMenuIndex(int index) {
		switch(index) {
			case 0:
				return index;
			case 1:
				return 5;
			case 2:
				return 10;
			case 3:
				return 50;
			default:
				return 0;
		}
	}
	
	@Override
	@Subscribe
	public void onTournamentShopClick(@Nonnull GameInterfaceButtonEvent event) {
		if (event.getInterfaceId() == TOURNAMENT_WIDGET_ID) {
			Item clickedItem = new Item(event.getChildButton2(), getAmountForMenuIndex(event.getMenuIndex()));
			if (event.getMenuIndex() == 0) {
				TournamentSuppliesService.TournamentRewards.cost(clickedItem).ifPresent(i -> event.getPlayer().getActionSender().sendMessage(clickedItem.getDefinition2().getName() + " currently costs: " + NumberFormat.getNumberInstance(Locale.ENGLISH).format(i) + " Blood Money."));
				return;
			}

			if (clickedItem.getCount() >= 1) {
				Optional<Integer> costOption = TournamentSuppliesService.TournamentRewards.cost(clickedItem);

				if (costOption.isPresent()) {
					PlayerBountyHunterEntity bountyHunterEntity = event.getPlayer().getDatabaseEntity().getBountyHunter();

					int emblemCost = costOption.get();
					int cost = clickedItem.getCount() * emblemCost;

					if (bountyHunterEntity.getBountyShopPoints() >= cost && event.getPlayer().getInventory().add(clickedItem)) {
						int difference = bountyHunterEntity.getBountyShopPoints() - cost;
						bountyHunterEntity.setBountyShopPoints(difference);
						//event.getPlayer().getActionSender().sendConfig(BOUNTY_POINT_CONFIG, difference);
					} else {
						int maxAmount = (int) Math.floor(cost / bountyHunterEntity.getBountyShopPoints());
						int maxCost = maxAmount * emblemCost;

						if (bountyHunterEntity.getBountyShopPoints() >= maxCost && event.getPlayer().getInventory().add(new Item(clickedItem.getId(), maxAmount))) {
							int difference = bountyHunterEntity.getBountyShopPoints() - maxCost;
							bountyHunterEntity.setBountyShopPoints(difference);
						//	event.getPlayer().getActionSender().sendConfig(BOUNTY_POINT_CONFIG, difference);
						} else {
							event.getPlayer().getActionSender().sendMessage("Either your inventory is too full to buy an item or you don't have enough Blood Money.");
						}
					}
				} else {
					event.getPlayer().sendMessage("You can't buy that item right now.");
				}
			}
		}
	}

	@Override
	public void openTournamentShop(@Nonnull Player player) {
		player.getActionSender().sendInterface(TOURNAMENT_WIDGET_ID, false);
		//.sendCS2Script(150, Constants.BUY_PARAMETERS, Constants.TRADE_TYPE_STRING);
		player.sendAccess(TOURNAMENT_SHOP_ACCESS);
	}

}
