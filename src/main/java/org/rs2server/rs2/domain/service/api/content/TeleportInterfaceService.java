package org.rs2server.rs2.domain.service.api.content;

import javax.annotation.Nonnull;

import org.rs2server.rs2.model.player.Player;

public interface TeleportInterfaceService {
	
    void openInterface(@Nonnull Player player);


    void handleInterfaceActions(@Nonnull Player player, int button, int childButton, int childButton2, int menuIndex);

}
