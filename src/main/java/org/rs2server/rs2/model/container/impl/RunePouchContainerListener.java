package org.rs2server.rs2.model.container.impl;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.content.RunePouchService;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.container.ContainerListener;
import org.rs2server.rs2.model.player.Player;

/**
 * @author Clank1337
 */
public class RunePouchContainerListener implements ContainerListener {

	private final Player player;
	private final RunePouchService pouchService;

	public RunePouchContainerListener(Player player) {
		this.player = player;
		this.pouchService = Server.getInjector().getInstance(RunePouchService.class);
	}

	@Override
	public void itemChanged(Container container, int slot) {
		pouchService.updatePouchInterface(player);
	}

	@Override
	public void itemsChanged(Container container, int[] slots) {
		pouchService.updatePouchInterface(player);
	}

	@Override
	public void itemsChanged(Container container) {
		pouchService.updatePouchInterface(player);
	}
}
