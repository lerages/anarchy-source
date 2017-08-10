package org.rs2server.util;

import org.rs2server.Server;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.UpdateFlags.UpdateFlag;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.player.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DonationManager {

	public static boolean rspsdata(Player player, String username) {
		try {
			username = username.replaceAll(" ", "_");
			String secret = "4dea382d82666332fb564f2e711cbc71"; // YOUR SECRET
			// KEY!
			String email = "millsd574@gmail.com"; // This is the one you use to
			// login into RSPS-PAY
			URL url = new URL(
					"http://rsps-pay.com/includes/listener.php?username="
							+ username + "&secret=" + secret + "&email="
							+ email);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String results = reader.readLine();
			final PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
			if (results.toLowerCase().contains("!error:")) {
				return false;
			} else {
				String[] ary = results.split(",");
				player.getActionSender().removeChatboxInterface();
				for (int i = 0; i < ary.length; i++) {
					switch (ary[i]) {
						case "0":
							return false;
						case "20541":
							Inventory.addDroppable(player, new Item(13204, 100));
							World.getWorld().sendWorldMessage("<img=33><col=880000>Donations: " + player.getName() +" has just Donated!");
							break;
						case "20542":
							Inventory.addDroppable(player, new Item(13204, 50));
							World.getWorld().sendWorldMessage("<img=33><col=880000>Donations: " + player.getName() +" has just Donated!");
							break;
						case "20543":
							Inventory.addDroppable(player, new Item(13204, 250));
							World.getWorld().sendWorldMessage("<img=33><col=880000>Donations: " + player.getName() +" has just Donated!");
							break;
						case "20544":
							Inventory.addDroppable(player, new Item(13204, 500));
							World.getWorld().sendWorldMessage("<img=33><col=880000>Donations: " + player.getName() +" has just Donated!");
							break;
						case "20777":
							Inventory.addDroppable(player, new Item(13204, 1000));
							World.getWorld().sendWorldMessage("<img=33><col=880000>Donations: " + player.getName() +" has just Donated!");
							
							break;
					}
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private final static String HOST = "162.212.253.190";


	public static void execute(Player player) throws SQLException {
		player.getActionSender().sendMessage("Thank you for supporting Kronos! If you have any donator feature suggestions please post them!");
		for (Player p : World.getWorld().getPlayers()) {
			if (p != null && p.getActionSender() != null) {
				p.getActionSender().sendMessage(player.getName() + " has just purchased donator status!");
			}
		}
		PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
		//permissionService.give(player, PermissionService.PlayerPermissions.DONATOR);
		player.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
		Connection conn = null;
		java.sql.Statement statement = null;
		Properties connectionProps = new Properties();
		connectionProps.put("user", "lostisle_admin2");
		connectionProps.put("password", "timsebqwe3321");
		conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/", connectionProps);
		statement = conn.createStatement();

		statement.executeUpdate("UPDATE `lostisle_ipb2`.`members` SET `member_group_id` = '9' WHERE `Name` = '" + player.getName() + "';");
	}

}
