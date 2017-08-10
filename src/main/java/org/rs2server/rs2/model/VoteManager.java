package org.rs2server.rs2.model;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.rs2server.rs2.model.player.Player;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class VoteManager implements Runnable {

		public static final String HOST = "162.212.253.55";
		public static final String USER = "osvernox_forums";
		public static final String PASS = "Parker2017";
		public static final String DATABASE = "osvernox_vote";

		private Player player;
		private java.sql.Connection conn;
		private java.sql.Statement stmt;

		public VoteManager(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			try {
				if (!connect(HOST, DATABASE, USER, PASS)) {
					return;
				}

				String name = player.getName().replace(" ", "_");
				ResultSet rs = executeQuery("SELECT * FROM fx_votes WHERE username='"+name+"' AND claimed=0 AND callback_date IS NOT NULL");

				while (rs.next()) {
					String timestamp = rs.getTimestamp("callback_date").toString();
					String ipAddress = rs.getString("ip_address");
					int siteId = rs.getInt("site_id");


					// -- ADD CODE HERE TO GIVE TOKENS OR WHATEVER


					System.out.println("[Vote] Vote claimed by "+name+". (sid: "+siteId+", ip: "+ipAddress+", time: "+timestamp+")");

					rs.updateInt("claimed", 1); // do not delete otherwise they can reclaim!
					rs.updateRow();
				}

				destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		public boolean connect(String host, String database, String user, String pass) {
			try {
				this.conn = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+database, user, pass);
				return true;
			} catch (SQLException e) {
				System.out.println("Failing connecting to database!");
				return false;
			}
		}

		public void destroy() {
	        try {
	    		conn.close();
	        	conn = null;
	        	if (stmt != null) {
	    			stmt.close();
	        		stmt = null;
	        	}
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }

		public int executeUpdate(String query) {
	        try {
	        	this.stmt = this.conn.createStatement(1005, 1008);
	            int results = stmt.executeUpdate(query);
	            return results;
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        return -1;
	    }

		public ResultSet executeQuery(String query) {
	        try {
	        	this.stmt = this.conn.createStatement(1005, 1008);
	            ResultSet results = stmt.executeQuery(query);
	            return results;
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        return null;
	    }

	}