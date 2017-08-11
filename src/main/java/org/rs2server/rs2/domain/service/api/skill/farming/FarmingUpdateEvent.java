package org.rs2server.rs2.domain.service.api.skill.farming;

import com.google.inject.Inject;
import org.rs2server.rs2.domain.dao.api.PlayerEntityDao;
import org.rs2server.rs2.domain.model.player.PlayerSkillFarmingEntity;
import org.rs2server.rs2.domain.service.api.PersistenceService;
import org.rs2server.rs2.domain.service.impl.skill.FarmingServiceImpl;
import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Global farming patch update event.
 * @author tommo
 */
public class FarmingUpdateEvent extends Event {
	
	private static final Logger logger = LoggerFactory.getLogger(FarmingServiceImpl.class);

	private FarmingService farmingService;
	private PlayerEntityDao playerEntityDao;

	@Inject
	FarmingUpdateEvent(final FarmingService farmingService, final PlayerEntityDao playerEntityDao) {
		super(300000); // 10 minutes
		this.farmingService = farmingService;
		this.playerEntityDao = playerEntityDao;
	}

	@Override
	public void execute() {
		final Set<String> processed = new HashSet<>();

		// First update farming patches for all online players
		World.getWorld().getPlayers().stream()
				.filter(p -> p.getDatabaseEntity().getFarmingSkill().getPatches().size() > 0)
				.forEach(p -> {
					processed.add(p.getName());
					update(p.getDatabaseEntity().getFarmingSkill());
					farmingService.sendPatches(p);
					System.out.println("Updated all farming patches");
					logger.info();
				});

		// Now update farming patches for all offline players
		playerEntityDao.findAll().stream().filter(p -> !processed.contains(p.getDisplayName())
				&& p.getFarmingSkill() != null && p.getFarmingSkill().getPatches() != null && p.getFarmingSkill().getPatches().size() > 0)
				.forEach(p -> {
					update(p.getFarmingSkill());
					playerEntityDao.save(p);
					logger.info("Updating all Farming Patches...");
				});
	}

	private void update(final PlayerSkillFarmingEntity farmingEntity) {
		farmingEntity.getPatches().entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.forEach(entry -> farmingService.updatePatch(entry.getValue()));
			logger.info("Updating all Farming Patches...");
	}

}
