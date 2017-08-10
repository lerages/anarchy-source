package org.rs2server.rs2.event.impl;

import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Persists all online players.
 *
 * @author tim
 */
public class PlayerSaveEvent extends Event {

    private static final Logger logger = LoggerFactory.getLogger(PlayerSaveEvent.class);

    public PlayerSaveEvent() {
        super(300000); // 5 minutes
    }

    @Override
    public void execute() {
        engineService.offerToSingle(() -> World.getWorld().getPlayers().stream().filter(Objects::nonNull).forEach(p -> {
            World.getWorld().getWorldLoader().savePlayer(p);
            logger.info("Saving all players...");
        }));
    }
}
