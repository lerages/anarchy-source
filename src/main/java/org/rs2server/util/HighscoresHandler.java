package org.rs2server.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.rs2server.rs2.model.skills.*;
import com.mysql.jdbc.Statement;
import org.rs2server.rs2.model.player.Player;

public class HighscoresHandler implements Runnable{

	/**
	 * Secret key
	 */
	final static String secret = "3cedaa0df7754c9";
	/**
	 * Username that is used for mysql connection
	 */
	final static String user = "d81b6b";


	private Player player;

	public HighscoresHandler(Player player) {
		this.player = player;
	}

	/**
	 * Function that handles everything, it inserts or updates
	 * user data in database
	 */
	@Override
	public void run() {
		/**
		 * Players username
		 */
		final String username = player.getName();
		/**
		 * Represents game mode
		 * If you want to set game modes do this:
		 */
		final int gameMode = 0;
		/**
		 * Represents overall xp
		 */
		final long overallXp = (long) player.getSkills().getTotalExperience();
		/**
		 * Represents attack xp
		 */
		final long attackXp = (long) (long) player.getSkills().getExperience(0);
		/**
		 * Represents defence xp
		 */
		final long defenceXp = (long) player.getSkills().getExperience(1);
		/**
		 * Represents strength xp
		 */
		final long strengthXp = (long) player.getSkills().getExperience(2);
		/**
		 * Represents constitution xp
		 */
		final long constitutionXp = (long) player.getSkills().getExperience(3);
		/**
		 * Represents ranged xp
		 */
		final long rangedXp = (long) player.getSkills().getExperience(4);
		/**
		 * Represents prayer xp
		 */
		final long prayerXp = (long) player.getSkills().getExperience(5);
		/**
		 * Represents magic xp
		 */
		final long magicXp = (long) player.getSkills().getExperience(6);
		/**
		 * Represents cooking xp
		 */
		final long cookingXp = (long) player.getSkills().getExperience(7);
		/**
		 * Represents woodcutting xp
		 */
		final long woodcuttingXp = (long) player.getSkills().getExperience(8);
		/**
		 * Represents fletching xp
		 */
		final long fletchingXp = (long) player.getSkills().getExperience(9);
		/**
		 * Represents fishing xp
		 */
		final long fishingXp = (long) player.getSkills().getExperience(10);
		/**
		 * Represents firemaking xp
		 */
		final long firemakingXp = (long) player.getSkills().getExperience(11);
		/**
		 * Represents crafting xp
		 */
		final long craftingXp = (long) player.getSkills().getExperience(12);
		/**
		 * Represents smithing xp
		 */
		final long smithingXp = (long) player.getSkills().getExperience(13);
		/**
		 * Represents mining xp
		 */
		final long miningXp = (long) player.getSkills().getExperience(14);
		/**
		 * Represents herblore xp
		 */
		final long herbloreXp = (long) player.getSkills().getExperience(15);
		/**
		 * Represents agility xp
		 */
		final long agilityXp = (long) player.getSkills().getExperience(16);
		/**
		 * Represents thieving xp
		 */
		final long thievingXp = (long) player.getSkills().getExperience(17);
		/**
		 * Represents slayer xp
		 */
		final long slayerXp = (long) player.getSkills().getExperience(18);
		/**
		 * Represents farming xp
		 */
		final long farmingXp = (long) player.getSkills().getExperience(19);
		/**
		 * Represents runecrafting xp
		 */
		final long runecraftingXp = (long) player.getSkills().getExperience(20);
		/**
		 * Represents hunter xp
		 */
		final long hunterXp = (long) player.getSkills().getExperience(21);
		/**
		 * Represents construction xp
		 */
		final long constructionXp = (long) player.getSkills().getExperience(22);
		/**
		 * Creates new instance of jdbc driver
		 * if that driver exists
		 */
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		/**
		 * Sets Connection variable to null
		 */
		Connection connection = null;
		/**
		 * Sets Statement variable to null
		 */
		Statement stmt = null;

		/**
		 * Attempts connecting to database
		 */
		try {
			connection = DriverManager.getConnection("jdbc:mysql://198.211.123.88:3306/admin_scores_data", user, secret);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		/**
		 * Checks if connection isnt null
		 */
		if (connection != null) {
		    try {
		    	stmt = (Statement) connection.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM `"+user+"_scores` WHERE username='" +username+ "'");
				if(rs.next()) {
					if(rs.getInt("count") > 0)  {
						stmt.executeUpdate("UPDATE `"+user+"_scores` SET overall_xp = '"+overallXp+"', attack_xp = '"+attackXp+"', defence_xp = '"+defenceXp+"', strength_xp = '"+strengthXp+"', constitution_xp = '"+constitutionXp+"', ranged_xp = '"+rangedXp+"', prayer_xp = '"+prayerXp+"', magic_xp = '"+magicXp+"', cooking_xp = '"+cookingXp+"', woodcutting_xp = '"+woodcuttingXp+"', fletching_xp = '"+fletchingXp+"', fishing_xp = '"+fishingXp+"', firemaking_xp = '"+firemakingXp+"', crafting_xp = '"+craftingXp+"', smithing_xp = '"+smithingXp+"', mining_xp = '"+miningXp+"', herblore_xp = '"+herbloreXp+"', agility_xp = '"+agilityXp+"', thieving_xp = '"+thievingXp+"', slayer_xp = '"+slayerXp+"', farming_xp = '"+farmingXp+"', runecrafting_xp = '"+runecraftingXp+"', hunter_xp = '"+hunterXp+"', construction_xp = '"+constructionXp+"' WHERE username = '"+username+"'");
					} else {
						stmt.executeUpdate("INSERT INTO `"+user+"_scores` (username, mode, overall_xp, attack_xp, defence_xp, strength_xp, constitution_xp, ranged_xp, prayer_xp, magic_xp, cooking_xp, woodcutting_xp, fletching_xp, fishing_xp, firemaking_xp, crafting_xp, smithing_xp, mining_xp, herblore_xp, agility_xp, thieving_xp, slayer_xp, farming_xp, runecrafting_xp, hunter_xp, construction_xp) VALUES ('"+username+"', '"+gameMode+"', '"+overallXp+"', '"+attackXp+"', '"+defenceXp+"', '"+strengthXp+"', '"+constitutionXp+"', '"+rangedXp+"', '"+prayerXp+"', '"+magicXp+"', '"+cookingXp+"', '"+woodcuttingXp+"', '"+fletchingXp+"', '"+fishingXp+"', '"+firemakingXp+"', '"+craftingXp+"', '"+smithingXp+"', '"+miningXp+"', '"+herbloreXp+"', '"+agilityXp+"', '"+thievingXp+"', '"+slayerXp+"', '"+farmingXp+"', '"+runecraftingXp+"', '"+hunterXp+"', '"+constructionXp+"')");
					}
				}
				stmt.close();
				connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} else {
			System.out.println("Failed to make connection!");
		}

		return;
	}
}
