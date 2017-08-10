package org.rs2server.rs2.model.npc.pc;

import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.CombatNPCDefinition;
import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.map.RegionClipping;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.player.pc.PestControlInstance;
import org.rs2server.rs2.tickable.StoppingTick;
import org.rs2server.rs2.util.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A Shifter teleports it's way towards the {@link VoidKnight}, unless it gets attacked
 * in which case it teleports around it's attacker.
 * It's main goal is to attack the {@link VoidKnight}.
 * @author Twelve
 */
public class Shifter extends PestControlNpc {

	private int lastTeleported;
	private static final Animation TELEPORT_ANIMATION = Animation.create(3904);
	private static final Graphic TELEPORT_GRAPHIC = Graphic.create(654);

	public Shifter(int id, Location location, PestControlInstance instance, PestControlPortal portal) {
		super(id, location, instance, portal);
	}


	@Override
	public void tick() {
		if (getInteractingEntity() == null && getCombatState().getLastHitTimer() < (System.currentTimeMillis() + 4000)) { //Not in combat
			Optional<Player> player = instance.getPlayers().stream().filter(p -> p.getLocation().distance(this.getLocation()) <= 5).findAny();
			if (player.isPresent()) {
				getCombatState().startAttacking(player.get(), player.get().isAutoRetaliating());
			}
		}
	}

	private final Location getKnightDestination() {
		int start = Misc.random(1, 2);
		int end = Misc.random(3, 4);

		List<Location> locations = new ArrayList<>();
		Location bestLoc = getLocation();
		for (int z = start; z <= end; z++) {
			for (int x = getLocation().getX() - z; x < getLocation().getX() + z; x++) {
				for (int y = getLocation().getY() - z; y < getLocation().getY() + z; y++) {
					Location location = Location.create(x, y);
					if (RegionClipping.isPassable(location) && location.distance(instance.getKnight().getLocation()) < bestLoc.distance(instance.getKnight().getLocation())) {
						locations.add(location);
						bestLoc = location;
					}
				}
			}
		}
		if (locations.isEmpty()) {
			return getLocation();
		}
		Collections.shuffle(locations);
		return locations.get(0);
	}

	private final Location getDestination(Mob interactingEntity) {

		if (getLocation().distance(interactingEntity.getLocation()) <= 5) {
			return getLocation();
		}

		int start = Misc.random(1, 2);
		int end = Misc.random(3, 4);

		List<Location> locations = new ArrayList<>();
		for (int z = start; z <= end; z++) {
			for (int x = interactingEntity.getLocation().getX() - z; x < interactingEntity.getLocation().getX() + z; x++) {
				for (int y = interactingEntity.getLocation().getY() - z; y < interactingEntity.getLocation().getY() + z; y++) {
					Location location = Location.create(x, y);
					if (RegionClipping.isPassable(location)) {
						locations.add(location);
					}
				}
			}
		}
		if (locations.isEmpty()) {
			return getLocation();
		}
		Collections.shuffle(locations);
		return locations.get(0);
	}
}
