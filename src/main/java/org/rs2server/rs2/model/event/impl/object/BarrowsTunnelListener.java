package org.rs2server.rs2.model.event.impl.object;
 
import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.GameObject;
import org.rs2server.rs2.model.GroundItem;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.event.ClickEventManager;
import org.rs2server.rs2.model.event.EventListener;
import org.rs2server.rs2.model.minigame.barrows.Barrows.BarrowsBrother;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
/**
 * @author Luke132
 * @author 'Mystic Flow
 */
public class BarrowsTunnelListener extends EventListener {
 
    private static final Logger logger = LoggerFactory.getLogger(BarrowsTunnelListener.class);
 
    public static final int REWARDS_INTERFACE_ID = 364;
 
    public static final int[][] COMMON_REWARDS = {
            {561, 1, 65}, {558, 1, 200}, {562, 1, 125}, {560, 1, 100}, {565, 1, 75},
            {995, 100, 5000}, {4740, 50, 150}
    };
    public static final int[] RARE_REWARDS = {
            1149, 985, 987
    };
   
    protected static final int[] VERAC_PIECES = {
            4757, 4759, 4753, 4755
    };
    protected static final int[] KARIL_PIECES = {
            4736, 4738, 4734, 4732
    };
    protected static final int[] TORAG_PIECES = {
            4745, 4747, 4749, 4751
    };
    protected static final int[] AHRIM_PIECES = {
            4708, 4710, 4712, 4714
    };
    protected static final int[] DHAROK_PIECES = {
            4716, 4718, 4720, 4722
    };
    protected static final int[] GUTHAN_PIECES = {
            4724, 4726, 4728, 4730
    };
    protected static final int[] BROTHERS = {
            1677, 1675, 1676, 1672, 1673, 1674
    };
    protected static final int[][] BROTHERS_REWARDS = {
            VERAC_PIECES, KARIL_PIECES, TORAG_PIECES, AHRIM_PIECES, DHAROK_PIECES, GUTHAN_PIECES
    };
 
 
    protected static final int[] DOORS = {
            6747, 6741, 6735, 6739, 6746, 6745, 6737, 6735,
            6728, 6722, 6716, 6720, 6727, 6726, 6718, 6716,
            6731, 6728, 6722, 6720, 6727, 6731, 6726, 6718,
            6750, 6747, 6741, 6739, 6746, 6750, 6745, 6737,
            6742, 6749, 6748, 6743, 6744, 6740, 6742, 6738,
            6723, 6730, 6729, 6724, 6725, 6723, 6721, 6719,
            6749, 6748, 6736, 6743, 6744, 6740, 6738, 6736,
            6730, 6729, 6717, 6724, 6725, 6721, 6719, 6717,
    };
 
    protected static final int[][] DOOR_LOCATION = {
            {3569, 9684}, {3569, 9701}, {3569, 9718}, {3552, 9701}, {3552, 9684},
            {3535, 9684}, {3535, 9701}, {3535, 9718}, {3568, 9684}, {3568, 9701},
            {3568, 9718}, {3551, 9701}, {3551, 9684}, {3534, 9684}, {3534, 9701},
            {3534, 9718}, {3569, 9671}, {3569, 9688}, {3569, 9705}, {3552, 9705},
            {3552, 9688}, {3535, 9671}, {3535, 9688}, {3535, 9705}, {3568, 9671},
            {3568, 9688}, {3568, 9705}, {3551, 9705}, {3551, 9688}, {3534, 9671},
            {3534, 9688}, {3534, 9705}, {3575, 9677}, {3558, 9677}, {3541, 9677},
            {3541, 9694}, {3558, 9694}, {3558, 9711}, {3575, 9711}, {3541, 9711},
            {3575, 9678}, {3558, 9678}, {3541, 9678}, {3541, 9695}, {3558, 9695},
            {3575, 9712}, {3558, 9712}, {3541, 9712}, {3562, 9678}, {3545, 9678},
            {3528, 9678}, {3545, 9695}, {3562, 9695}, {3562, 9712}, {3545, 9712},
            {3528, 9712}, {3562, 9677}, {3545, 9677}, {3528, 9677}, {3545, 9694},
            {3562, 9694}, {3562, 9711}, {3545, 9711}, {3528, 9711},
    };
 
    protected static final int[][] DOOR_OPEN_DIRECTION = {
            {6732, 2, 4}, {6732, 2, 4}, {6732, 2, 4}, {6732, 2, 4}, {6732, 2, 4}, {6732, 2, 4}, {6732, 2, 4}, {6732, 2, 4},
            {6713, 0, 4}, {6713, 0, 4}, {6713, 0, 4}, {6713, 0, 4}, {6713, 0, 4}, {6713, 0, 4}, {6713, 0, 4}, {6713, 0, 4},
            {6713, 2, 2}, {6713, 2, 2}, {6713, 2, 2}, {6713, 2, 2}, {6713, 2, 2}, {6713, 2, 2}, {6713, 2, 2}, {6713, 2, 2},
            {6732, 4, 2}, {6732, 4, 2}, {6732, 4, 2}, {6732, 4, 2}, {6732, 4, 2}, {6732, 4, 2}, {6732, 4, 2}, {6732, 4, 2},
            {6732, 3, 3}, {6732, 3, 3}, {6732, 3, 3}, {6732, 3, 3}, {6732, 3, 3}, {6732, 3, 3}, {6732, 3, 3}, {6732, 3, 3},
            {6713, 1, 3}, {6713, 1, 3}, {6713, 1, 3}, {6713, 1, 3}, {6713, 1, 3}, {6713, 1, 3}, {6713, 1, 3}, {6713, 1, 3},
            {6732, 1, 1}, {6732, 1, 1}, {6732, 1, 1}, {6732, 1, 1}, {6732, 1, 1}, {6732, 1, 1}, {6732, 1, 1}, {6732, 1, 1},
            {6713, 3, 1}, {6713, 3, 1}, {6713, 3, 1}, {6713, 3, 1}, {6713, 3, 1}, {6713, 3, 1}, {6713, 3, 1}, {6713, 3, 1},
    };
 
    protected static final int[][] DB = {
            {3532, 9665, 3570, 9671},
            {3575, 9676, 3581, 9714},
            {3534, 9718, 3570, 9723},
            {3523, 9675, 3528, 9712},
            {3541, 9711, 3545, 9712},
            {3558, 9711, 3562, 9712},
            {3568, 9701, 3569, 9705},
            {3551, 9701, 3552, 9705},
            {3534, 9701, 3535, 9705},
            {3541, 9694, 3545, 9695},
            {3558, 9694, 3562, 9695},
            {3568, 9684, 3569, 9688},
            {3551, 9684, 3552, 9688},
            {3534, 9684, 3535, 9688},
            {3541, 9677, 3545, 9678},
            {3558, 9677, 3562, 9678},
    };
 
    private final static PlayerService playerService = Server.getInjector().getInstance(PlayerService.class);
 
 
    @Override
    public void register(ClickEventManager manager) {
        List<Integer> doors = new ArrayList<Integer>();
        for (int i : DOORS) {
            if (!doors.contains(i)) {
                doors.add(i);
            }
        }
        manager.registerObjectListener(20973, this);
    }
 
    @Override
    public boolean objectAction(Player player, int objectId, GameObject gameObject, Location location, ClickOption option) {
        if (option != ClickOption.FIRST) {
            return false;
        }
       if (objectId == 20973) {
            if (player.getAttribute("canLoot") == Boolean.TRUE) {
               lootChest(player);
            } else {
                if (player.getAttribute("currentlyFightingBrother") == null && player.getAttribute("looted_barrows") != Boolean.TRUE) {
                    BarrowsBrother brother = player.getAttribute("barrows_tunnel");
                    NPC spawnedBrother = new NPC(brother.getNpcId(), player.getLocation().transform(-1, 0, 0), null, null, 0);
                    World.getWorld().register(spawnedBrother);
                    spawnedBrother.setInstancedPlayer(player);
                    spawnedBrother.getCombatState().startAttacking(player, false);
                    player.setAttribute("currentlyFightingBrother", spawnedBrother);
                }
            }
            return true;
        }
        return true;
    }
    
    public static void lootChest(Player player)
    {
    	 List<Integer>BARROW_REWARDS = new ArrayList<Integer>();
         BARROW_REWARDS.add(12851);
         //int[] BARROW_REWARDS = {
         //      12851 // Amulet of the damned
         //};
         player.removeAttribute("canLoot");
         for(int i = 0; i < 6; i++){
             if(player.getKilledBrothers().get(BROTHERS[i])){
                 for(int var : BROTHERS_REWARDS[i]){
                     BARROW_REWARDS.add(var);
                 }
             }
         }
         player.getDatabaseEntity().getStatistics().setBarrowsChestCount(player.getDatabaseEntity().getStatistics().getBarrowsChestCount() + 1);
         logger.info("Barrows chest count increased to " + player.getDatabaseEntity().getStatistics().getBarrowsChestCount() + " for " + player.getName());
         player.getActionSender().sendMessage("<col=ff0000>Your Barrows chest count is: " + player.getDatabaseEntity().getStatistics().getBarrowsChestCount() + ".");

         final List<Item> rewards = new ArrayList<>();
         for (int[] data : COMMON_REWARDS) {
             if (player.getRandom().nextDouble() > 0.40) {
                 int id = data[0];
                 int amount = Misc.random(data[1], data[2]);
                 rewards.add(new Item(id, amount));
             }
         }
         if (Misc.random(200) == 0) {
             rewards.add(new Item(12073));
         }

         final int chance = player.getKilledBrothers().entrySet().stream()
                 .filter(Map.Entry::getValue)
                 .mapToInt(e -> 2)
                 .sum() / 6;
         player.getKilledBrothers().clear();

         int random = player.getRandom().nextInt(99);
         System.out.println(BARROW_REWARDS.size());
         System.out.println(random + " - " + (chance > 15 ? 15 : chance));
         if (random <= (chance > 15 ? 15 : chance)) {
            
             int item = BARROW_REWARDS.get(player.getRandom().nextInt(BARROW_REWARDS.size()));
             World.getWorld().sendWorldMessage("<col=884422><img=33> News: " + player.getName() + " has just received 1x " + CacheItemDefinition.get(item).getName() + ".");
             rewards.add(new Item(item, 1));
         }
         player.setKC(0);
         player.removeAttribute("barrows_tunnel");
        
         for (int i = 0; i < BarrowsBrother.values().length; i++) {
             BarrowsBrother brother = BarrowsBrother.values()[i];
             player.getKilledBrothers().put(brother.getNpcId(), false);
         }
        
         player.getActionSender().sendString(24, 9, "Kill Count: " + player.getKC());
         player.setAttribute("looted_barrows", Boolean.TRUE);

         player.getActionSender().sendInterface(REWARDS_INTERFACE_ID, false);
         player.getActionSender().sendUpdateItems(REWARDS_INTERFACE_ID, 1, 0, rewards.toArray(new Item[rewards.size()]));
         rewards.stream().forEach(i -> playerService.giveItem(player, i, true));
    }
    
    private static int[] getWalkDirections(Player p, int index, int index2, boolean betweenDoors) {
        int openDirection = DOOR_OPEN_DIRECTION[index][2];
        int[] direction = new int[2];
        if (openDirection == 0) {
            /*Nothing*/
        } else if (openDirection == 1) { // doors open east.
            direction[0] = betweenDoors ? +1 : -1;
            direction[1] = 0;
        } else if (openDirection == 2) { // doors open north.
            direction[0] = 0;
            direction[1] = betweenDoors ? +1 : -1;
        } else if (openDirection == 3) { // doors open west.
            direction[0] = betweenDoors ? -1 : +1;
            direction[1] = 0;
        } else if (openDirection == 4) { // doors open south.
            direction[0] = 0;
            direction[1] = betweenDoors ? -1 : +1;
        }
        return direction;
    }
 
    private static int getOtherDoor(int x, int y) {
        for (int i = 0; i < DOOR_LOCATION.length; i++) {
            if ((x == DOOR_LOCATION[i][0] && y + 1 == DOOR_LOCATION[i][1]) ||
                    (x + 1 == DOOR_LOCATION[i][0] && y == DOOR_LOCATION[i][1]) ||
                    (x == DOOR_LOCATION[i][0] && y - 1 == DOOR_LOCATION[i][1]) ||
                    (x - 1 == DOOR_LOCATION[i][0] && y == DOOR_LOCATION[i][1])) {
                return i;
            }
        }
        return -1;
    }
 
    private static int getDoorIndex(int doorId, int x, int y) {
        for (int i = 0; i < DOORS.length; i++) {
            if (doorId == DOORS[i]) {
                if (x == DOOR_LOCATION[i][0] && y == DOOR_LOCATION[i][1]) {
                    return i;
                }
            }
        }
        return -1;
    }
 
}