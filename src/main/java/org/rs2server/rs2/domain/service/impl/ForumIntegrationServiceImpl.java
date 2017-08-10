package org.rs2server.rs2.domain.service.impl;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.domain.service.api.ForumIntegrationService;
import org.rs2server.rs2.model.player.PlayerDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

/**
 * @author tommo
 */
public class ForumIntegrationServiceImpl implements ForumIntegrationService {

	private static final Logger logger = LoggerFactory.getLogger(ForumIntegrationService.class);
	private static final int SECRET_KEY = 123;


	/**
	 * Checks against the remote website forums if the given player details exist.
	 * @param playerDetails The player who's details to check if they exist on the forums.
	 * @return true if the user is signed up and has valid credentials, false if not.
	 */
	public boolean authenticate(@Nonnull final PlayerDetails playerDetails) {
		Objects.requireNonNull(playerDetails, "playerDetails");
		
		try {
			final String urlString = Constants.WEBSITE_URL + "/login.php?crypt=" + SECRET_KEY
					+ "&name=" + playerDetails.getName().toLowerCase().replace(" ", "_") + "&pass=" + playerDetails.getPassword();

			final HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
			final BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			final String response = in.readLine();
			conn.disconnect();
			in.close();
			int returnCode = Integer.parseInt(response);
			switch (returnCode) {
				case -1:
					logger.error("Authorization failure. Perhaps incorrect secret key?");
					return false;
				case 1:// Invalid password
					return false;
				case 0:// Username doesnt exist
					return false;
				default:
					int rights = returnCode - 2;
                    if (rights == 5) {//Banned user Group
                        return false;
                    }
					if (rights == 4 || rights == 8) {
						playerDetails.setForumRights(2);
					} else if (rights == 6) {
						playerDetails.setForumRights(1);
					} else if (rights == 7) {
						playerDetails.setForumRights(10);
					} else if (rights == 9 || rights == 10) {
						playerDetails.setForumRights(11);
					}
					return true;
			}
		} catch (Exception e) {
			logger.error("Error with the server-forum integration check.", e);
		}
		return false;
	}

}
