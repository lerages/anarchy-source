package org.rs2server.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ItemPriceDumper {

	@SuppressWarnings("unused")
	private static final File file = new File("./pricesnew.txt");

	private static final int ITEM_AMOUNT = 13064;
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < ITEM_AMOUNT; i++) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("C:/Users/Tim/Desktop/prices.txt"), true));
			URL url = new URL("http://services.runescape.com/m=itemdb_oldschool/Abyssal_whip/viewitem?obj="+i);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			//System.out.println("Connecting for item id "+i);
			while ((line = reader.readLine()) != null) {
				if (line.contains("As a result, your IP address has been temporarily blocked.")) {
					System.out.println("IP Blocked on Attempt: " + i + " Sleeping for 10 seconds.");
					Thread.sleep(10000);
					i--;
				}
				if (line.contains("Current Guide Price")) {
					String price = line.substring(line.indexOf("'>") + 3).replaceAll(",", "").replaceAll(">", "").replaceAll("\\t", "");
					System.out.println("Dumping price for: "+ i +", Price: "+getAdditition(price));
					writer.write(i + ":" + getAdditition(price));
					writer.newLine();
				}
			}
			con.disconnect();
			reader.close();
			Thread.sleep(5000);
			writer.close();
		}

/*
		BufferedReader reader = new BufferedReader(new FileReader(file));

		int i = 0;
		String line;
		while ((line = reader.readLine()) != null) {

			if (line.contains("<td><img src=\"http://www.sublimism.com/misc/idb/img/")) {
				String price = reader.readLine();
				if (price.contains("<a href=\"http://forums.zybez.net/runescape-2007-prices/")) {
					String next = reader.readLine();	
					if (next.contains("GP")) {// this is sick lol
						String gp = next.substring("<td class=\"ipsForm_right\">".length() + 4)
								.split(" ")[0]
										.replaceAll(",", "");
						
						int length = "<td><img src=\"http://www.sublimism.com/misc/iddb/img/".length();
						String itemString = line.substring(length + 3).split(" ")[0].replaceAll("\"", "").replaceAll(".png", "");
					
					
						try {
							int itemId = Integer.parseInt(itemString);
							i++;
							System.out.println(itemId+":"+gp);// do i win?
						} catch (Exception e) {// cheap hax to ensure we only parse numbers
							continue;
						}
					}
				}
			}
		}
		System.out.println("i is "+i);*/
	}
	
	public static String getAdditition(String s) {
		return s.toLowerCase().replace(".", "").replace("k", "00").replace("m", "00000").replace("b", "00000000");
	}

}
