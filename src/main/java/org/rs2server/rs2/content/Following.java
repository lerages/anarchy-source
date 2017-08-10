package org.rs2server.rs2.content;

import java.util.ArrayList;
import java.util.List;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PathfindingService;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.map.RegionClipping;
import org.rs2server.rs2.model.map.path.DefaultPathFinder;
import org.rs2server.rs2.model.map.path.PrimitivePathFinder;
import org.rs2server.rs2.model.map.path.SizedPathFinder;
import org.rs2server.rs2.model.map.path.VariablePathFinder;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.player.Player;

/**
 * @author 'Mystic Flow
 */
public class Following {

	public static void combatFollow(Mob mob, Mob other) {
		if (!mob.getCombatState().canMove()) {
			return;
		}

		PathfindingService pathfindingService = Server.getInjector().getInstance(PathfindingService.class);
		int followX = other.getLocation().getX();
		int followY = other.getLocation().getY();
		if (mob.isPlayer() && other.isPlayer() || other.isNPC()) {
			if (!mob.getCentreLocation().isWithinDistance(mob, other, 1)) {
				if (other instanceof Player) {
					pathfindingService.travelToPlayer(mob, (Player) other);
				} else {
					pathfindingService.travelToNpc(mob, (NPC) other);
					//World.getWorld().doPath(new SizedPathFinder(true), mob, followX, followY);
				//	pathfindingService.travelToNpc(mob, (NPC) other);
				}
			}
	/*			if (mob.getLocation().getY() < other.getLocation().getY()) {
					followY--;
				} else if (mob.getLocation().getY() > other.getLocation().getY()) {
					followY++;
				} else if (mob.getLocation().getX() < other.getLocation().getX()) {
					followX--;
				} else if (mob.getLocation().getX() > other.getLocation().getX()) {
					followX++;
				}
				World.getWorld().doPath(new SizedPathFinder(true), mob, followX, followY);
			}*/
		} else if (mob.isNPC()) {
			Location destination = getDestination(mob, other);
			if (destination != null) {
				// Npcs should not use a path finder, else safe spotting isn't possible.
				//pathfindingService.travelToPlayer(mob, (Player) other);
                NPC n = (NPC) mob;
                boolean godwarsNPC = n.getCombatDefinition() != null && n.getCombatDefinition().getGwdPairs().get(n.getId()) != null;
                if (godwarsNPC || n.getId() == 6610 || n.getId() == 6619 || n.getId() == 2054 || n.getId() == 2045) {
					World.getWorld().doPath(new SizedPathFinder(n.getId() != 2045), mob, destination.getX(), destination.getY());
                } else {
                    World.getWorld().doPath(new PrimitivePathFinder(), mob, destination.getX(), destination.getY());
                }
			}
		}
	}

	public static Location getDestination(Mob mob, Mob victim) {//this might work.
		//If X > 0 - victim > mob, x < 0 victim < mob.
		int size = mob.getWidth();
		if (mob.isNPC()) {
			NPC n = (NPC) mob;
			size = n.getSize();
		}
		Location delta = mob.getCentreLocation().getDelta(victim.getCentreLocation());
		boolean vertical = (delta.getY() < 0 ? -delta.getY() : delta.getY()) > (delta.getX() < 0 ? -delta.getX() : delta.getX());
		List<Location> victimList = null;
		List<Location> mobList = null;
		int z = mob.getLocation().getZ();
		if (vertical) {
			if (delta.getY() > 0) { //Victim has higher Y than mob.
				victimList = getSurrounding(victim, victim.getLocation().getY(), victim.getLocation().getX(), z, -1, true);
				mobList = getSurrounding(mob, mob.getLocation().getY(), mob.getLocation().getX(), z, size, true);
			} else {
				victimList = getSurrounding(victim, victim.getLocation().getY(), victim.getLocation().getX(), z, victim.getWidth(), true);
				mobList = getSurrounding(mob, mob.getLocation().getY(), mob.getLocation().getX(), z, -1, true);
			}
		} else {
			if (delta.getX() > 0) { //Victim has higher X than mob.
				victimList = getSurrounding(victim, victim.getLocation().getX(), victim.getLocation().getY(), z, -1, false);
				mobList = getSurrounding(mob, mob.getLocation().getX(), mob.getLocation().getY(), z, size, false);
			} else {
				victimList = getSurrounding(victim, victim.getLocation().getX(), victim.getLocation().getY(), z, victim.getWidth(), false);
				mobList = getSurrounding(mob, mob.getLocation().getX(), mob.getLocation().getY(), z, -1, false);
			}
		}
		double currentDistance = 999; //Random high number so we override first in the loop.
		Location victLoc = victim.getLocation();
		for (Location sl : mobList) {
			for (Location vl : victimList) {
				double distance = sl.distance(vl);
				if (distance < currentDistance) {
					currentDistance = distance;
					victLoc = vl;
				}
			}
		}
		return victLoc;
	}

	private static List<Location> getSurrounding(Mob mob, int i, int j, int z, int size, boolean switched) {
		List<Location> list = new ArrayList<Location>();
		int x = i + size;
		for (int y = j; y < j + size; y++) {
			Location l = switched ? Location.create(y, x, z) : Location.create(x, y, z);
			if (RegionClipping.getClippingMask(l.getX(), l.getY(), l.getZ()) > 0) {
				continue;
			}
			list.add(l);
		}
		return list;
	}

}