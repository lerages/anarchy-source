package org.rs2server.rs2.model.event.impl.object;

import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.GameObject;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.event.ClickEventManager;
import org.rs2server.rs2.model.event.EventListener;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.Tickable;


public class StrongholdObjectListener extends EventListener {

    /**
     * The opening a door animation.
     */
    private static final Animation OPEN_ANIM = Animation.create(4282);

    /**
     * The closing a door animation.
     */
    private static final Animation CLOSE_ANIM = Animation.create(4283);

    /**
     * The climbing a ladder animation.
     */
    private static final Animation CLIMB_LADDER = Animation.create(828);

    @Override
    public void register(ClickEventManager manager) {
    }

    @Override
    public boolean objectAction(final Player player, int objectId, GameObject gameObject, Location location, ClickOption option) {
        if (climbLadder(player, objectId, location)) {
            return true;
        }
        if (usePortal(player, objectId, location)) {
            return true;
        }
        if (lootChest(player, objectId, location)) {
            return true;
        }
        Location toTeleport = player.getLocation();
        switch (gameObject.getDirection()) {
            case 0:
                toTeleport = player.getLocation().getX() >= location.getX() ? location.transform(-1, 0, 0) : location.transform(0, 0, 0);
                break;
            case 1:
                toTeleport = player.getLocation().getY() > location.getY() ? location.transform(0, 0, 0) : location.transform(0, 1, 0);
                break;
            case 2:
                toTeleport = player.getLocation().getX() > location.getX() ? location.transform(0, 0, 0) : location.transform(1, 0, 0);
                break;
            default:
                toTeleport = player.getLocation().getY() >= location.getY() ? location.transform(0, -1, 0) : location.transform(0, 0, 0);
                break;
        }
        player.face(toTeleport);
        player.setAttribute("cantMove", Boolean.TRUE);
        player.setAttribute("busy", Boolean.TRUE);
        final Location destination = toTeleport;
        player.playAnimation(OPEN_ANIM);
        World.getWorld().submit(new Tickable(1) {
            @Override
            public void execute() {
                player.setTeleportTarget(destination);
                World.getWorld().submit(new Tickable(1) {
                    @Override
                    public void execute() {
                        player.playAnimation(CLOSE_ANIM);
                        player.removeAttribute("busy");
                        player.removeAttribute("cantMove");
                        stop();
                    }
                });
                stop();
            }
        });
        return true;
    }

    /**
     * Tries to use a portal in the stronghold.
     *
     * @param player   The player.
     * @param objectId The object id.
     * @param location The location.
     * @return {@code True} if the player used a portal, {@code false} if not.
     */
    private boolean usePortal(Player player, int objectId, Location location) {
        Location loc = player.getLocation();
        int floor = 0;
        if (objectId == 16150) {
            loc = Location.create(1910, 5220, 0);
        } else if (objectId == 16082) {
            loc = Location.create(2024, 5213, 0);
            floor = 1;
        } else if (objectId == 16116) {
            loc = Location.create(2146, 5278, 0);
            floor = 2;
        } else if (objectId == 16050) {
            loc = Location.create(2342, 5213, 0);
            floor = 3;
        } else {
            return false;
        }
        if (!player.getSettings().getStrongholdChest()[floor]) {
            player.getActionSender().sendMessage("You can't use this portal without looting the chest on this floor first.");
            return true;
        }
        player.setTeleportTarget(loc);
        return true;
    }

    /**
     * Attempts to loot a reward chest in the stronghold of security.
     *
     * @param player   The player.
     * @param objectId The object id.
     * @param location The location.
     * @return {@code True} if the object action was to loot a chest, {@code false} if not.
     */
    private boolean lootChest(final Player player, final int objectId, Location location) {
        String firstMessage = "You open the gift of peace...";
        String message = "..and find 2000 coins.";
        int slot = 0;
        Item loot = null;
        if (objectId == 16135) {
            loot = new Item(995, 2000);
        } else if (objectId == 16077) {
            slot = 1;
            loot = new Item(995, 3000);
            firstMessage = "You search the grain of plenty...";
            message = "... and find 3000 coins.";
        } else if (objectId == 16118) {
            slot = 2;
            loot = new Item(995, 5000);
            firstMessage = "You open the box of health...";
            message = "...it restored your life and prayer points and you find 5000 coins.";
        } else if (objectId == 16047) {
            slot = 3;
            firstMessage = "You open the cradle of life...";
            message = null;
        } else {
            return false;
        }
        if (player.getSettings().getStrongholdChest()[slot]) {
            player.getActionSender().sendMessage("You've already looted this chest.");
            return true;
        }
        player.getActionSender().sendMessage(firstMessage);
        player.getSettings().getStrongholdChest()[slot] = true;
        final Item item = loot;
        final String msg = message;
        World.getWorld().submit(new Tickable(1) {
            @Override
            public void execute() {
                stop();
                if (item != null) {
                    if (!player.getInventory().add(item)) {
                        return;
                    }
                }
                if (objectId == 16118) {
                    player.getSkills().increaseLevelToMaximum(Skills.HITPOINTS, player.getSkills().getLevelForExperience(Skills.HITPOINTS));
                    //TODO: ((Skills) player.getSkills()).setPrayerPoints(990, true);
                } else if (objectId == 16047) {
                    player.getActionSender().sendChatboxInterface(131);
                    player.getActionSender().sendString(131, 1, "You can choose between these two pairs of boots.");
                    player.getActionSender().sendItemOnInterface(131, 0, 9005, 175);
                    player.getActionSender().sendItemOnInterface(131, 2, 9006, 175);
                }
                player.getActionSender().sendMessage(msg);
            }
        });
        return true;
    }

    /**
     * Climbs a ladder in the stronghold of security.
     *
     * @param player   The player.
     * @param objectId The object id.
     * @param location The location.
     * @return {@code True} if the option clicked was a ladder, {@code false} if not.
     */
    private boolean climbLadder(final Player player, final int objectId, Location location) {
        Location loc = player.getLocation();
        if (objectId == 20790) { //Barbarian village entrance.
            loc = Location.create(1860, 5244, 0);
        } else if (objectId == 20784) { //Climb up to barbarian village.
            loc = Location.create(3081, 3421, 0);
        } else if (objectId == 16080) {
            loc = Location.create(1903, 5222, 0);
        } else if (objectId == 16114) {
            loc = Location.create(2026, 5217, 0);
        } else if (objectId == 16049) {
            loc = Location.create(2148, 5283, 0);
        } else if (objectId == 16149) {
            loc = Location.create(2042, 5245, 0);
        } else if (objectId == 16081 || objectId == 16112) {
            loc = Location.create(2123, 5252, 0);
        } else if (objectId == 16115) {
            loc = Location.create(2358, 5215, 0);
        } else {
            return false;
        }
        player.face(location);
        player.setAttribute("cantMove", true);
        final Location destination = loc;
        if (objectId != 16154) {
            player.playAnimation(CLIMB_LADDER);
        }
        World.getWorld().submit(new Tickable(1) {
            @Override
            public void execute() {
                player.setTeleportTarget(destination);
                if (objectId == 16154) {
                    player.face(player.getLocation().transform(-1, 0, 0));
                }
                World.getWorld().submit(new Tickable(1) {
                    @Override
                    public void execute() {
                        player.playAnimation(CLIMB_LADDER);
                        player.setAttribute("cantMove", false);
                        stop();
                    }
                });
                stop();
            }
        });
        return true;
    }

}
