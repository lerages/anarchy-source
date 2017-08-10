package org.rs2server.rs2.domain.service.api.content.logging;

import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.container.Container;
import org.rs2server.rs2.model.player.Player;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author Clank1337
 */
public interface ServerLoggingService {


	enum LogType {
		TRADE(System.getProperty("user.home")+ File.separator+"Dropbox/Lost-Isle Logs/trades/"),

		PVP(System.getProperty("user.home")+ File.separator+"Dropbox/Lost-Isle Logs/pvp/"),

		PVN(System.getProperty("user.home")+ File.separator+"Dropbox/Lost-Isle Logs/pvn/"),

		DROP_ITEM(System.getProperty("user.home")+ File.separator+"Dropbox/Lost-Isle Logs/dropped/"),

		PICKUP_ITEM(System.getProperty("user.home")+ File.separator+"Dropbox/Lost-Isle Logs/pickup/"),

		PRIVATE_MESSAGE(System.getProperty("user.home")+ File.separator+"Dropbox/Lost-Isle Logs/pm/");


		private final String path;

		LogType(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	void write(Player player, String info, LogType type);

}
