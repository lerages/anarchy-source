package org.rs2server.rs2;

import org.apache.mina.core.buffer.IoBuffer;
import org.rs2server.Server;
import org.rs2server.rs2.Constants.ReturnCodes;
import org.rs2server.rs2.domain.service.api.ForumIntegrationService;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.player.PlayerDetails;
import org.rs2server.rs2.util.NameUtils;
import org.rs2server.util.Streams;
import org.rs2server.util.XMLController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * An implementation of the <code>WorldLoader</code> class that saves players
 * in binary, gzip-compressed files in the <code>data/players/</code>
 * directory.
 * @author Graham Edgecombe
 *
 */
public class GenericWorldLoader implements WorldLoader {

	private static final Logger logger = LoggerFactory.getLogger(GenericWorldLoader.class);
	private static final int MAX_LOGGED_IN = 2;

	private final ForumIntegrationService forumIntegrationService;

	public GenericWorldLoader() {
		forumIntegrationService = Server.getInjector().getInstance(ForumIntegrationService.class);
	}
	
/*	@Override
	public LoginResult checkLogin(PlayerDetails pd) {
		Player player = null;
		int code = ReturnCodes.LOGIN_OK;
		File f = new File("data/savedGames/" + NameUtils.formatNameForProtocol(pd.getName()) + ".dat.gz");
		if (Constants.DEV_SERVER) {
			if (!NameUtils.isValidName(pd.getName())) {
				code = ReturnCodes.COULD_NOT_COMPLETE;
			}
			if (World.getWorld().isWorldUpdateInProgress()) {
				code = ReturnCodes.UPDATE_IN_PROGRESS;
			} else if (World.getWorld().isPlayerOnline(pd.getName())) {
				code = ReturnCodes.ALREADY_ONLINE;
			} else {
				if (f.exists()) {
					try {
						InputStream is = new GZIPInputStream(
								new FileInputStream(f));
						String name = Streams.readSavedAccountString(is);
						String pass = Streams.readSavedAccountString(is);
						if (!name.equals(NameUtils.formatName(pd.getName()))) {
							code = ReturnCodes.INVALID_PASSWORD;
						}

						if (!pass.equals(pd.getPassword())) {
							code = ReturnCodes.INVALID_PASSWORD;
						}
					} catch (IOException e) {
						code = 55;
					}
				}
			}		
		} else {
			if (!NameUtils.isValidName(pd.getName())) {
				code = ReturnCodes.COULD_NOT_COMPLETE;
			} else if (pd.getName().length() > Constants.MAX_USERNAME_COUNT) {
				code = ReturnCodes.USERNAME_TOO_LONG;
			}
			if (World.getWorld().isPlayerOnline(pd.getName())) {
				code = ReturnCodes.ALREADY_ONLINE;
			} else if (World.getWorld().isWorldUpdateInProgress()) {
				code = ReturnCodes.UPDATE_IN_PROGRESS;
			} else {
				if (f.exists()) {
					try {
						InputStream is = new GZIPInputStream(
								new FileInputStream(f));
						String name = Streams.readSavedAccountString(is);
						String pass = Streams.readSavedAccountString(is);
						if (!name.equals(NameUtils.formatName(pd.getName()))) {
							code = ReturnCodes.INVALID_PASSWORD;
						}

						if (!pass.equals(pd.getPassword())) {
							code = ReturnCodes.INVALID_PASSWORD;
						}
					} catch (IOException e) {
						code = 55;
					}
				}
			}
		}
		if (code == 2) {
			player = new Player(pd);
		}
		return new LoginResult(code, player);
	}

	@Override
	public boolean savePlayer(Player player) {
		try {
			OutputStream os = new GZIPOutputStream(new FileOutputStream("data/savedGames/" + NameUtils.formatNameForProtocol(player.getName()) + ".dat.gz"));
			IoBuffer buf = IoBuffer.allocate(1024);
			buf.setAutoExpand(true);
			player.serialize(buf);
			buf.flip();
			byte[] data = new byte[buf.limit()];
			buf.get(data);
			os.write(data);
			os.flush();
			os.close();
			World.getWorld().serializePrivate(player.getName());
			return true;
		} catch(IOException ex) {
			logger.error("Error saving player.", ex);
			return false;
		}
	}

	@Override
	public boolean loadPlayer(Player player) {
		try {
			File f = new File("./data/savedGames/" + NameUtils.formatNameForProtocol(player.getName()) + ".dat.gz");
			InputStream is = new GZIPInputStream(new FileInputStream(f));
			IoBuffer buf = IoBuffer.allocate(1024);
			buf.setAutoExpand(true);
			while(true) {
				byte[] temp = new byte[1024];
				int read = is.read(temp, 0, temp.length);
				if(read == -1) {
					break;
				} else {
					buf.put(temp, 0, read);
				}
			}
			buf.flip();
			player.deserialize(buf);
			is.close();
			return true;
		} catch(IOException ex) {
			logger.error("Error loading player.", ex);
			return false;
		}
	}

*/
	
	
		@Override
	public LoginResult checkLogin(PlayerDetails playerDetails) {

		if (!playerDetails.getName().matches("^[0-9a-zA-Z ]+")) {
			return new LoginResult(LoginResult.BANNED, null);
		}
		if (playerDetails.getName().length() < 1 || playerDetails.getName().length() > 12) {
			return new LoginResult(LoginResult.BANNED, null);
		}
		if (playerDetails.getVersion() != Server.VERSION) {
			return new LoginResult(LoginResult.VERSION_MISMATCH, null);
		}
		if (World.systemUpdate && !playerDetails.getName().equals("something")) {
			return new LoginResult(LoginResult.UPDATE_IN_PROGRESS, null);
		}

		if (playerDetails.getOnlineAccountsFromAddress().size() >= MAX_LOGGED_IN) {
			return new LoginResult(LoginResult.TOO_MANY_CONNECTIONS, null);
		}
        if (World.getWorld().isPlayerOnline(playerDetails.getName())) {
            return new LoginResult(LoginResult.ALREADY_LOGGED_IN, null);
        }

		boolean muted = false;
		final File f = new File("data/savedGames/" + NameUtils.formatNameForProtocol(playerDetails.getName()) + ".dat.gz");
		if(f.exists()) {
			try {
				final InputStream is = new GZIPInputStream(new FileInputStream(f));
				final String name = Streams.readRS2String(is);
				final String pass = Streams.readRS2String(is);
				if(!name.equalsIgnoreCase(playerDetails.getName())) {
					return new LoginResult(LoginResult.INVALID_CREDENTIALS);
				}
				if(!pass.equals(playerDetails.getPassword())) {
					return new LoginResult(LoginResult.INVALID_CREDENTIALS);
				}

				final List<String> bannedUsers = XMLController.readXML(new File("data/bannedUsers.xml"));
				for(String bannedName : bannedUsers) {
					if(bannedName.equalsIgnoreCase(playerDetails.getName())) {
						return new LoginResult(LoginResult.BANNED);
					}
				}
				final List<String> ipBannedUsers = XMLController.readXML(new File("data/ipBannedUsers.xml"));
				for(String bannedIP : ipBannedUsers) {
					if(bannedIP.equalsIgnoreCase(playerDetails.getIP())) {
						return new LoginResult(LoginResult.BANNED);
					}
				}
				final List<String> uidBans = XMLController.readXML(new File("data/uidBannedUsers.xml"));
				for(String bannedUID : uidBans) {
					if(bannedUID.equals(playerDetails.getUUID())) {
						return new LoginResult(LoginResult.BANNED);
					}
				}
				final List<String> mutedUsers = XMLController.readXML(new File("data/mutedUsers.xml"));
				for(String mutedName : mutedUsers) {
					if(mutedName.equalsIgnoreCase(playerDetails.getName())) {
						muted = true;
					}
				}
				final List<String> ipMutedUsers = XMLController.readXML(new File("data/ipMutedUsers.xml"));
				for(String mutedIP : ipMutedUsers) {
					if(mutedIP.equalsIgnoreCase(playerDetails.getIP())) {
						muted = true;
					}
				}
			} catch(IOException ex) {
				logger.error("Unexpected problem occurred.", ex);
				return new LoginResult(LoginResult.UNKNOWN_ERROR, null);
			}
		}

		final Player player = new Player(playerDetails);
		if (muted) {
			player.getSettings().setMuted(true);
		}

		return new LoginResult(LoginResult.SUCCESS, player);
	}

	@Override
	public boolean savePlayer(Player player) {
		try {
			OutputStream os = new GZIPOutputStream(new FileOutputStream("data/savedGames/" + NameUtils.formatNameForProtocol(player.getName()) + ".dat.gz"));
			IoBuffer buf = IoBuffer.allocate(1024);
			buf.setAutoExpand(true);
			player.serialize(buf);
			buf.flip();
			byte[] data = new byte[buf.limit()];
			buf.get(data);
			os.write(data);
			os.flush();
			os.close();
			World.getWorld().serializePrivate(player.getName());
			return true;
		} catch(IOException ex) {
			logger.error("Error saving player.", ex);
			return false;
		}
	}

	@Override
	public boolean loadPlayer(Player player) {
		try {
			File f = new File("./data/savedGames/" + NameUtils.formatNameForProtocol(player.getName()) + ".dat.gz");
			InputStream is = new GZIPInputStream(new FileInputStream(f));
			IoBuffer buf = IoBuffer.allocate(1024);
			buf.setAutoExpand(true);
			while(true) {
				byte[] temp = new byte[1024];
				int read = is.read(temp, 0, temp.length);
				if(read == -1) {
					break;
				} else {
					buf.put(temp, 0, read);
				}
			}
			buf.flip();
			player.deserialize(buf);
			is.close();
			return true;
		} catch(IOException ex) {
			logger.error("Error loading player.", ex);
			return false;
		}
	}
}
