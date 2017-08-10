package org.rs2server.rs2.content;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class StarterMap {

	private static StarterMap INSTANCE = new StarterMap();

	public static StarterMap getSingleton() {
		return INSTANCE;
	}

	public List<String> starters = new ArrayList<String>();
	private final String path = "./data/starters.ini";
	private File map = new File(path);
	
	private Logger logger = Logger.getAnonymousLogger();

	public void init() {
		try {
			logger.info("Loading Starters");
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(map));
			String s;
			while ((s = reader.readLine()) != null) {
				starters.add(s);
			}
			logger.info("Loaded Starter map, There are "
					+ starters.size() + " IP's in Configuration");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void save() {
		BufferedWriter bf;
		try {
			clearMapFile();
			bf = new BufferedWriter(new FileWriter(path, true));
			for (String ip : starters) {
				bf.write(ip);
				bf.newLine();
			}
			bf.flush();
			bf.close();
		} catch (IOException e) {
			System.err.println("Error saving starter map!");
		}
	}

	private void clearMapFile() {
		PrintWriter writer;
		try {
			writer = new PrintWriter(map);
			writer.print("");
			writer.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void addIP(String ip) {
		starters.add(ip);
		save();
	}

	public int getCount(String ip) {
		int count = 0;
		for (String i : starters) {
			if (i.equals(ip))
				count++;
		}
		return count;
	}

}
