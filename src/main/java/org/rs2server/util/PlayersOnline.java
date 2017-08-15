package org.rs2server.util;
import java.sql.*;
import org.rs2server.rs2.model.World;

public class PlayersOnline {
	private static final boolean localTesting = true;
	//!!!! Ensure database allows VPS IP 
	public static void updateWebPCount(String state) {
        try {
        	if (localTesting) {
        		return;
        	}
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://91.208.99.2:1186/osanarch_server", "osanarch_master", "239882");
            Statement stmt = con.createStatement();
            int activePlayers = World.getWorld().getPlayers().size();
            
            if (state == "logout") {
            	activePlayers--; //used before player is logged out.
            }
            
            stmt.executeUpdate("UPDATE `online` SET players ='" + activePlayers + "' WHERE id='1337'");
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
	
	
