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
 * Created by Tim on 10/20/2015.
 */
public class AltarAction extends ProductionAction 
{

    private Mob mob;
    private BoneType type;
    private int amount;
    static PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
   	static int cost = 2500;

    public AltarAction(Mob mob, BoneType type, int amount) {
        super(mob);
        this.type = type;
        this.amount = amount;
    }

    public static boolean handleItemOnObject(Player player, GameObject obj, Item item) {
        BoneType type = BoneType.forId(item.getId());
        
        if(permissionService.is(player, PermissionService.PlayerPermissions.DONATOR))
        	cost = 0;

        if (type == null || obj.getId() != 409) {
            return false;
        }
        if (type != null && obj.getId() == 409 && player.getInventory().getCount(995) < cost) {
           	player.sendMessage("In addition to bones, you must supply 2,500 coins to appease the gods with your offering.");
            return false;
        }

        final PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
//        if (permissionService.isNotAny(player, PermissionServiceImpl.SPECIAL_PERMISSIONS)) {
//            if (Misc.random(4) == 0) {
//                player.getActionSender().sendMessage("This feature is currently only available for donators.");
//            }
//            return false;
//        }
        player.getActionQueue().addAction(new AltarAction(player, type, player.getInventory().getCount(type.getId())));
        return false;
    }


    private enum BoneType {

        NORMAL_BONES(526, 4),
        BURNT_BONES(528, 4),
        BAT_BONES(530, 5),
        BIG_BONES(532, 15),
        BABYDRAGON_BONES(534, 30),
        DRAGON_BONES(536, 72),
        LAVA_DRAGON_BONES(11943, 85),
        WOLF_BONES(2859, 4),
        JOGRE_BONES(3125, 15),
        DAGANNOTH_BONES(6729, 125),
        WYVERN_BONES(6812, 72),
        SHAIKAHAN_BONES(3123, 25),
        OURG_BONES(4834, 140);

        private int id;
        private int xp;

        public int getId() {
            return id;
        }

        public int getXp() {
            return (int)((xp * 3.5));
        }

        private BoneType(int id, int xp) {
            this.id = id;
            this.xp = xp;
        }

        private static Map<Integer, BoneType> bones = new HashMap<Integer, BoneType>();


        public static BoneType forId(int bone) {
            return bones.get(bone);
        }

        static {
            for (BoneType type : BoneType.values()) {
                bones.put(type.getId(), type);
            }
        }

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
        return null;
    }

    @Override
    public Item[] getConsumedItems() {
        return new Item[] {new Item(type.getId(), 1), cost > 0 ? new Item(995, cost) : null};
    }

    @Override
    public int getSkill() {
        return Skills.PRAYER;
    }

    @Override
    public int getRequiredLevel() {
        return 0;
    }

    @Override
    public double getExperience() {
        return type.getXp();
    }

    @Override
    public String getLevelTooLowMessage() {
        return null;
    }

    @Override
    public String getSuccessfulProductionMessage() {
        return "The gods are very pleased with your offering.";
    }

    @Override
    public Animation getAnimation() {
        return Animation.create(713);
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
		return Sound.BONES_ON_ALTAR;
	}
}
