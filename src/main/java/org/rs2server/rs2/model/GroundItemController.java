package org.rs2server.rs2.model;

import org.rs2server.Server;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.domain.model.player.treasuretrail.clue.ClueScrollType;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.region.Region;

import java.util.ArrayList;

/**
 * Controls all GroundItems world-wide.
 *
 * @author Martin
 * @author Brown
 */
public class GroundItemController {

    /**
     * This ArrayList contains all ground items.
     */
    private static final ArrayList<GroundItemDefinition> items = new ArrayList<GroundItemDefinition>();

    /**
     * Gets the ArrayList that contains all ground items.
     *
     * @return The ArrayList that contains all ground items.
     */
    public static ArrayList<GroundItemDefinition> getGroundItems() {
        return items;
    }

    /**
     * Checks if a specific ground item exists.
     *
     * @return True if, false if not.
     */
    public static boolean groundItemExists(Location l, int id) {
        for (GroundItemDefinition g : items) {
            if (g.getLocation().equals(l) && g.getId() == id && g.getTime() != DISAPPEAR) {
                return true;
            }
        }
        return false;
    }

    /**
     * Spawns a ground item for everyone to see.
     *
     * @param g The ground item object to spawn.
     */
    public static void spawnForEveryone(GroundItemDefinition g) {
        if (g.getDefinition().isTradable()) {
            for (final Region r : g.getRegions()) {
                for (final Player player : r.getPlayers()) {
                    if (player.getName().equals(g.getOwner()) || player.isMultiplayerDisabled()) {
						// The owner has the item spawned as he drops. If this
						// Was removed, it would spawn another item, as this
						// Creates an item for EVERYONE.
						continue;
					}

                    player.getActionSender().sendGroundItem2(g);
                }
            }
        }
    }

    /**
     * Remove a ground item for all players.
     *
     * @param g The ground item object to remove.
     */
    public static void removeGroundItemForAll(GroundItemDefinition g) {
        synchronized (items) {
            // Remove the GroundItem from the ArrayList.
            if (items.remove(g)) {
                for (final Region r : g.getRegions()) {
                    for (final Player player : r.getPlayers()) {
                        player.getActionSender().removeGroundItem2(g);
                    }
                }
            }
        }
    }

    /**
     * Gets a GroundItem according to the data provided.
     *
     * @param l  The location.
     * @param id The ID to look for.
     * @return The GroundItem object, if any.
     */
    public static GroundItemDefinition getGroundItem(Location l, int id) {
        for (GroundItemDefinition g : items) {
            if (g.getId() == id && g.getLocation().equals(l)) {
                return g;
            }
        }
        return null;
    }

    /**
     * Creates a ground item.
     *
     * @param item     The item we want to put on the floor.
     * @param entity   The name of the person who dropped it / who it belongs to.
     * @param location Where we want it to be put.
     */
    public static void createGroundItem(Item item, Entity entity, Location location) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (item.getDefinition().isStackable()) {
                //Check if theres already an item on the floor, with that id.
                GroundItemDefinition orig = getGroundItem(location, item.getId());
                if (orig != null) {
                    if (item.isPvpDrop()) {
                        orig.setPvpDrop(true);
                    }
                    //If there is, we check if the owner is the same.
                    if (orig.getOwner() != null && orig.getOwner().equals(player.getName())) {
                        //We simply increase the delay, I don't know how to handle it other ways.
                        orig.increaseCount(item.getCount());
                        //TODO: Do this a proper way :(
                        // Make the GroundItem update.
                        player.getActionSender().removeGroundItem2(orig);
                        player.getActionSender().sendGroundItem2(orig);
                        return; //Nothing else to do in here.
                    }
                }

            }

            // Create the object for the GroundItem.
            GroundItemDefinition g = new GroundItemDefinition(player.getName(), location, item.getId(), item.getCount());
            if (item.isPvpDrop()) {
                g.setPvpDrop(true);
            }
            // Add the GroundItem to the ArrayList, so it gets processed.
            items.add(g);
            // Make the GroundItem appear.
            player.getActionSender().sendGroundItem2(g);
        } else {
            //null or NPC.
            if (item.getDefinition().isStackable()) {
                //Check if theres already an item on the floor, with that id.
                GroundItemDefinition orig = getGroundItem(location, item.getId());
                if (orig != null) {
                    //If there is, we check if the owner is the same.
                    if (orig.getOwner() == null) {
                        //We simply increase the delay, I don't know how to handle it other ways.
                        orig.increaseCount(item.getCount());
                        //TODO: Do this a proper way :(
                    }
                    // Make the GroundItem update.
                    return; //Nothing else to do in here.
                }

            }
            // Create the object for the GroundItem.
            GroundItemDefinition g = new GroundItemDefinition(null, location, item.getId(), item.getCount());

            // Add the GroundItem to the ArrayList, so it gets processed.
            items.add(g);
        }


    }

	public static void createGroundItem(GroundItemDefinition item) {
		GroundItemDefinition groundItemDefinition = new GroundItemDefinition(item.getOwner(), item.getLocation(), item.getId(), item.getCount());
		items.add(groundItemDefinition);
	}

    private static final Animation BONE_BURYING_ANIMATION = Animation.create(827);

    /**
     * Picks up a GroundItem.
     *
     * @param l      The items location.
     * @param id     The id of the ground item clicked.
     * @param player The player picking up the GroundItem.
     */
    public static void pickupGroundItem(final Location l, final int id, Player player) {
        player.getActionQueue().clearAllActions();
        /*
         * Else, we wait till we're at the actual location.
		 */
        final PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
        player.getActionQueue().addAction(new Action(player, 0) {

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
                GroundItemDefinition g = getGroundItem(l, id);
                if (g == null) {
                    this.stop();
                    return;
                }
                boolean canLoot = true;
                if (permissionService.isAny(player, PermissionService.PlayerPermissions.IRON_MAN, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN)) {
                    if ((!g.respawns() && g.isGlobal()) || !g.getOwner().equals(player.getName()) || g.isPvpDrop()) {
                        canLoot = false;
                    }
                }
				if (g.getOwner() != null && g.getOwner().equals("bLofkxA")) {
					canLoot = true;
				}
                if (!canLoot) {
                    player.getActionSender().sendMessage("You can't loot this item.");
                    this.stop();
                    return;
                }
                if (!player.getCombatState().canMove()) {
                    this.stop();
                    return;
                }
                if (isUntradable(g.getId()) && hasUntradable(player, g)) {
                    this.stop();
                    return;
                }
                if (ClueScrollType.forClueScrollItemId(g.getId()) != null && Server.getInjector().getInstance(PlayerService.class).hasItemInInventoryOrBank(player, g)) {
                    this.stop();
                    return;
                }
				player.getActionSender().removeChatboxInterface();
                if (player.getLocation().withinRange(l, 0)) {
                    if (player.getInventory().add(g)) {
                        removeGroundItemForAll(g);
                    }
                    this.stop();
                } else if (player.getLocation().withinRange(l, 1) && player.getWalkingQueue().isEmpty()) {
                    if (player.getInventory().add(g)) {
                        player.face(l);
                        player.playAnimation(BONE_BURYING_ANIMATION); //Couldn't find a decent one, lol.
                        removeGroundItemForAll(g);
                    }
                    this.stop();
                }
                this.setTickDelay(600);
            }

        });

    }

    public static void refresh(Player p) {
        for (GroundItemDefinition g : items) {
            if (p.getLocation().isWithinDistance(g.getLocation()) && (g.getOwner() == null || g.getTime() <= APPEAR_FOR_EVERYONE || p.getName().equals(g.getOwner()))) {
                p.getActionSender().sendGroundItem2(g);
            }
        }
    }

    public static boolean isUntradable(int id) {
        return (id >= 9096 && id <= 9104) || id == 9084 || id == 10828 || id == 10551 || (id >= 7054 && id <= 7062);
    }

    public static boolean hasUntradable(Player player, Item item) {
        return isUntradable(item.getId()) && player.getInventory().contains(item.getId()) || player.getBank().contains(item.getId()) || player.getEquipment().contains(item.getId());
    }

    /*
     * Constants, the stages of the ground item. Total span: 4 minutes.
     */
    public static final int APPEAR_FOR_EVERYONE = 120;
    public static final int DISAPPEAR = 0;
}