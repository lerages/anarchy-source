package org.rs2server.rs2.domain.model.player.treasuretrail.step;

import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.util.functional.Optionals;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * A clue step which requires the player to defeat a double agent.
 *
 * @author tommo
 */
public class TreasureTrailDoubleAgentStep extends TreasureTrailClueStep {

	public static final int DOUBLE_AGENT_NPC_ID = 1778;
	public static final Graphic URI_NPC_SMOKE_GRAPHIC = Graphic.create(86);

	public void spawnDoubleAgent(@Nonnull Player player) {
//		final Location spawnLocation = player.getLocation().closestFreeTileOrSelf(player.getLocation(), 1, 1);
		final Optional<Location> spawnOptional = Optionals.nearbyFreeLocation(player.getLocation());
		final Optional<NPC> npcOptional = World.getWorld().getNPCs().stream().filter(n -> n.getId() == DOUBLE_AGENT_NPC_ID).filter(i -> i.instancedPlayer != null && i.instancedPlayer == player).findAny();
		if (!npcOptional.isPresent() && spawnOptional.isPresent()) {
			final Location spawnLocation = spawnOptional.get();
//			final NPC doubleAgent = new NPC(DOUBLE_AGENT_NPC_ID, spawnLocation, spawnLocation, spawnLocation, 0);
			final DoubleAgent doubleAgent = new DoubleAgent(DOUBLE_AGENT_NPC_ID, spawnLocation, player);
			doubleAgent.setLocation(spawnLocation);
//			doubleAgent.setInstancedPlayer(player);
			World.getWorld().register(doubleAgent);
			doubleAgent.playGraphics(URI_NPC_SMOKE_GRAPHIC);
			doubleAgent.setForceChat("I expect you to die!");
			doubleAgent.getCombatState().startAttacking(player, true);
		}
	}

	public class DoubleAgent extends NPC {

		public DoubleAgent(int id, Location location, Player player) {
			super(id, location);
			this.instancedPlayer = player;
		}

		@Override
		public void tick() {
			if (instancedPlayer == null) {
				unregister();
				return;
			}
			double distance = getLocation().distance(instancedPlayer.getLocation());
			if (distance >= 10) {
				unregister();
			}
		}

	}

}
