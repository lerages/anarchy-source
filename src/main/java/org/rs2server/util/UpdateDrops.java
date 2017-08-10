package org.rs2server.util;
import java.sql.*;
public class UpdateDrops {
	  public static void publish(String player_name, String item_name, int item_amount, int item_id ){
	        try {
	            //jdbc:mysql://", "osanarch_master", "239882");
	            String url = "jdbc:mysql://91.208.99.2:1186/osanarch_server";
	            Connection conn = DriverManager.getConnection(url,"osanarch_master","239882");
	            Statement st = conn.createStatement();
	            st.executeUpdate("INSERT INTO drops " +
	                    "(player_name, item_name, item_amount, item_id) VALUES("
	                    + "'" + player_name +"' ,'" + item_name +"', '" + item_amount +"' ,'" + item_id +"')");
	            conn.close();
	        } catch (Exception e) {
	            System.err.println("Got an exception! ");
	            System.err.println(e.getMessage());
	        }
	    }

}
