package org.rs2server.rs2.domain.service.api;

import org.rs2server.rs2.model.player.PlayerDetails;

import javax.annotation.Nonnull;

/**
 * Service for providing forum integration capabilities.
 *
 * @author tommo
 */
public interface ForumIntegrationService {

	/**
	 * Checks against the remote website forums if the given player details exist.
	 * @param playerDetails The player who's details to check if they exist on the forums.
	 * @return true if the user is signed up and has valid credentials, false if not.
	 */
	boolean authenticate(@Nonnull final PlayerDetails playerDetails);

}
