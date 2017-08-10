package org.rs2server.tools;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Clank1337
 */
public class TopVoter {

	private final static String HOST = "162.212.253.190";

	private Connection conn;
	private Statement statement;
	private Properties connectionProps;
	private ResultSet result;

	public TopVoter() {
		try {
			connectionProps = new Properties();
			connectionProps.put("user", "lostisle_player");
			connectionProps.put("password", "sebtimqwe3321");
			conn = DriverManager.getConnection("jdbc:mysql://" + HOST + "/lostisle_motivote", connectionProps);
			statement = conn.createStatement();
			ArrayList<String> voteList = new ArrayList<>();
			result = statement.executeQuery("SELECT * FROM mv_votes");
			while (result.next()) {
				voteList.add(result.getString(3));
			}
			String top = voteList.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
					.entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
					.map(Map.Entry::getKey).orElse(null);
			int occurences = Collections.frequency(voteList, top);
			if (result != null) try {
				result.close();
			} catch (SQLException ignore) {
			}
			if (statement != null) try {
				statement.close();
			} catch (SQLException ignore) {
			}
			if (conn != null) try {
				conn.close();
			} catch (SQLException ignore) {
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
