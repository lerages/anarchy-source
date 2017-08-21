package org.rs2server.rs2.tickable.impl;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.GroundItemService;
import org.rs2server.rs2.domain.service.impl.GroundItemServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.task.impl.CleanupTask;
import org.rs2server.rs2.tickable.Tickable;

import java.util.Arrays;
import java.util.Objects;

public class ItemSpawnTick extends Tickable {

    /**
     * Creates the tickable to run every 60 seconds.
     */
    public ItemSpawnTick() {
        super(30);
    }

    private Region region = null;

    @Override
    public void execute() {
        GroundItemService groundItemService = Server.getInjector().getInstance(GroundItemService.class);

        for(ItemSpawn item : ItemSpawn.getSpawns()) {
            if(!groundItemService.getGroundItem(item.getItem().getId(), item.getLocation()).isPresent()) {
                groundItemService.createGroundItem(null, new GroundItemService.SpawnedGroundItem(item.getItem(), item.getLocation(),
                        "", true));
            }
        }
    }

}
