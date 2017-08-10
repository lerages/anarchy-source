package org.rs2server.rs2.model.skills;

import org.rs2server.Server;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.impl.ProductionAction;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.domain.service.impl.PermissionServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zaros on 07/08/2017.
 */
public class CballMakingAction extends ProductionAction 
{

    private Mob mob;
    private int amount;

    public CballMakingAction(Mob mob, int amount) {
        super(mob);
        this.amount = amount;
    }

    public static boolean handleItemOnObject(Player player, GameObject obj, Item item) {
        if (obj == null || item.getId() != 2353) 
        {
            return false;
        }
        else if(item.getId() == 2353 && !player.getInventory().contains(4))
        {
        	player.sendMessage("You need an ammo mould to smelt cannonballs");
        	return false;
        }

        final PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
//        if (permissionService.isNotAny(player, PermissionServiceImpl.SPECIAL_PERMISSIONS)) {
//            if (Misc.random(4) == 0) {
//                player.getActionSender().sendMessage("This feature is currently only available for donators.");
//            }
//            return false;
//        }
        player.getActionQueue().addAction(new CballMakingAction(player, player.getInventory().getCount(item.getId())));
        return false;
    }

    @Override
    public int getCycleCount() {
        return 4;
    }

    @Override
    public int getProductionCount() {
        return amount;
    }

    @Override
    public Item[] getRewards() {
        return new Item[] {new Item(2, 4)};
    }

    @Override
    public Item[] getConsumedItems() {
        return new Item[] {new Item(2353, 1)};
    }

    @Override
    public int getSkill() {
        return Skills.SMITHING;
    }

    @Override
    public int getRequiredLevel() {
        return 30;
    }

    @Override
    public double getExperience() {
        return 25.6;
    }

    @Override
    public String getLevelTooLowMessage() {
        return "You need a smithing level of 30 to smelt cannonballs.";
    }

    @Override
    public String getSuccessfulProductionMessage() {
        return "You smelt the steel bar into some cannonballs";
    }

    @Override
    public Animation getAnimation() {
        return Animation.create(3243);
    }

    @Override
    public Graphic getGraphic() {
        return null;
    }

    @Override
    public boolean canProduce() {
        return true;
    }

    @Override
    public boolean isSuccessfull() {
        return true;
    }

    @Override
    public String getFailProductionMessage() {
        return null;
    }

    @Override
    public Item getFailItem() {
        return null;
    }

	@Override
	public Sound getSound() {
		return Sound.SMELTING;
	}
}
