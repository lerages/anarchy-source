package org.rs2server.rs2.model.event.impl.object;

import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.GameObject;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.event.ClickEventManager;
import org.rs2server.rs2.model.event.EventListener;
import org.rs2server.rs2.model.player.Player;


public class GodwarsObjectListener extends EventListener {


	public static final int[] GWD_OBJS = new int[]{26341, 26342, 26293, 26384, 26425, 26439};

	// Some might be wrong.
	public static final int[] ZAMORAK_IDS =
		{
		6203, 6204, 6206, 6208, 6210, 6211, 6216, 6217, 6214,
		6215, 6212, 6213, 6219, 6220, 6218, 6221
		};
	public static final int[] SARADOMIN_IDS =
		{
		6247, 6248, 6250, 6252, 6254, 6255, 6256,
		6257, 6258, 6259
		};
	public static final int[] ARMADYL_IDS =
		{
		6222, 6223, 6225, 6227, 6229, 6230, 6231, 6232,
		6233, 6234, 6235, 6236, 6237, 6238, 6239, 6240, 6241, 6242, 6243,
		6244, 6245, 6246
		};
	public static final int[] BANDOS_IDS =
		{
		6260, 6261, 6263, 6265, 6268, 6269, 6270, 6271,
		6272, 6273, 6274, 6275, 6276, 6277, 6278, 6279, 6280, 6281, 6282,
		6283
		};

	public static final int[] SARA_ITEMS = new int[]{10390, 10452, 2665, 18745, 20135, 20147, 20159, 2412, 10446, 1718, 10470, 19152, 11698, 11730, 2415, 10440, 10458, 10388, 10386, 544, 2661, 20139, 20151, 20163, 3840, 2667, 10464, 10388, 542, 2663, 3479, 20143, 20155, 20167, 10384, 20054, 12185};

	public static final int[] BANDOS_ITEMS = new int[]{19376, 19437, 19457, 20135, 20147, 20159, 19370, 19394, 11061, 11696, 14679, 19364, 11724, 19384, 19453, 19428, 20139, 20151, 20163, 19613, 19440, 11726, 19455, 19388, 19431, 19434, 20143, 20155, 20167, 19451, 11728};

	public static final int[] ZAMORAK_ITEMS = new int[]{10374, 10456, 2657, 18746, 20135, 20147, 20159, 2414, 10450, 1724, 10474, 19162, 11700, 11716, 2417, 10444, 10460, 10460, 10370, 2653, 20139, 20151, 20163, 3842, 2659, 10468, 10468, 10372, 2655, 3478, 20143, 20155, 20167, 10368};

	public static final int[] ARMADYL_ITEMS = new int[]{11718, 19374, 19422, 19465, 20135, 20147, 20159, 19368, 19392, 87, 11694, 19362, 11720, 19380, 19461, 19413, 20139, 20151, 20163, 19615, 19425, 19419, 19463, 19386, 19416, 11722, 20143, 20155, 20167, 19459};

	public static final int[] ZAROS_ITEMS = new int[]{};

	public static final Location GOD_DUNGEON = Location.create(2881, 5308, 2);
	public static final Location GOD_DUNGEON_ENTRANCE = Location.create(2917, 3745, 0);

	public static final Animation SWIM_ANIM = Animation.create(772);


	@Override
	public void register(ClickEventManager manager) {
		/*manager.registerObjectListener(26425, this);*/
	}

	@Override
	public boolean objectAction(final Player player, int objectId, GameObject gameObject, Location location, ClickOption option) {
		switch (objectId) {
		case 26425:
			//if (player.getSettings().getKillCount()[Constants.BANDOS_KILL_COUNT] > 40) {
			//     player.getSettings().getKillCount()[Constants.BANDOS_KILL_COUNT] -= 40;
			//      player.sendMessage("The door devours the life-force of 40 followers of Bandos you have slain.");
			//  }
			World.getWorld().removeObjectKeepClipping(gameObject, 3);
			player.getWalkingQueue().reset();
			player.getWalkingQueue().addStep(player.getLocation().getX() == 2864 ? 2863 : 2864, 5354);
			player.getWalkingQueue().finish();
			return true;
		}
		return true;
	}

	/*	public boolean objectOption(final Player player, int objectId, GameObject gameObject, Location location, ClickOption option) {
		System.out.println(objectId + ", " + 234);
		switch (objectId) {
		case 26342:
			//  if (player.getInventory().contains(954)) {
				//      player.getInventory().deleteItem(954, 1);
				//      player.getSettings().setGodEntranceRope(true);
				//      ObjectManager.addCustomObject(player, 26341, location.getX(), location.getY(), location.getZ(), 10, 0);
				//  } else {
					//      player.sendMessage("You need a rope for this!");
			//  }
			return true;
		case 26341:
			//   if (player.getSettings().hasGodEntranceRope()) {
			//       player.teleport(GOD_DUNGEON);
			//  }
			//   return true;
		case 26293:
			//  player.teleport(GOD_DUNGEON_ENTRANCE);
			return true;
		case 26384:
			// ObjectManager.addCustomObject(0, 2851, 5333, 2, 0, 1);
			//  player.requestWalk(player.getLocation().getX() == 2851 ? 2850 : 2851, 5333);
			//  World.getWorld().submit(new Tick(2) {
			//      @Override
			//      public void execute() {
			//          ObjectManager.addCustomObject(26384, 2851, 5333, 2, 0, 0);
			//          endGame();
			//      }
			//  });
			break;
		case 26425:
			//if (player.getSettings().getKillCount()[Constants.BANDOS_KILL_COUNT] > 40) {
			//     player.getSettings().getKillCount()[Constants.BANDOS_KILL_COUNT] -= 40;
			//      player.sendMessage("The door devours the life-force of 40 followers of Bandos you have slain.");
			//  }
			World.getWorld().replaceObject(gameObject, null, 2);
			player.getWalkingQueue().reset();
			player.getWalkingQueue().addStep(player.getLocation().getX() == 2864 ? 2863 : 2864, 5354);
			player.getWalkingQueue().finish();
			return true;
		case 26439:
			//player.getLocation().getX(), player.getLocation().getY() < 5338 ? 5344 : 5331
			//   player.setAttribute("cantMove", Boolean.TRUE);
			//  player.forceMovement(SWIM_ANIM, player.getLocation().getX(), player.getLocation().getY() < 5338 ? 5337 : 5331, 1, 2, player.getLocation().getX(), player.getLocation().getY() < 5338 ? 0 : 4, true);
			//    ActionSender.sendInterface(player, 170);
			/*   World.getWorld().submit(new Tick(3) {

	                        @Override
	                        public void execute() {
	                            player.teleport(player.getLocation().getX(), player.getLocation().getY() < 5338 ? 5337 : 5331, 2);
	                            if (player.getLocation().getY() > 5338) {
	                                player.sendMessage("The extreme evil of this area leaves your Prayer drained.");
	                                player.getSkills().decreaseLevelToZero(Skills.PRAYER, player.getSkills().getLevel(Skills.PRAYER));
	                            }
	                            ActionSender.sendCloseInterface(player);
	                            endGame();
	                        }

	                    });
			break;
		}
		return true;
	}*/

}
