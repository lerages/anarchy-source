package org.rs2server.rs2.model.npc;

import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.player.Player;

public class MetalArmour extends NPC {
	
	/**
	 * The minimum location this NPC can walk into.
	 */
	private static final Location minLocation = Location.create(2849,3534,0);
	/**
	 * The maximum location this NPC can walk into.
	 */
	private static final Location maxLocation = Location.create(2861,3545,0);
	
	private static final Animation RISE = Animation.create(-1); //No clue on this one.
	
	private Player owner;

	public MetalArmour(NPCDefinition def, Location location, Player owner) {
		super(def.getId(), location, minLocation, maxLocation, 1);
		this.playAnimation(RISE);
		this.forceChat("I'm ALIVE!");
		this.setAggressiveDistance(10);
		this.owner = owner;
	}
	
	@Override
	public boolean canHit(Mob victim, boolean messages) {
		return super.canHit(victim, messages) && victim == this.owner;
	}

}
