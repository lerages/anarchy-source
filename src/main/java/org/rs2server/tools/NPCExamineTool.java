package org.rs2server.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.rs2server.rs2.model.npc.NPCLootTable;

import java.io.*;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * @author Clank1337
 */
public class NPCExamineTool {

	public static class ItemRarities {
		private final List<Integer> npcIds;
		private final List<ItemRarity> rarities;

		ItemRarities(List<Integer> npcIds, List<ItemRarity> rarities) {
			this.npcIds = npcIds;
			this.rarities = rarities;
		}

		public List<Integer> getNpcIds() {
			return npcIds;
		}

		public List<ItemRarity> getRarities() {
			return rarities;
		}
	}

	public class ItemRarity {
		private final int itemId;
		private final int minAmount;
		private final int maxAmount;
		private final double rarity;

		ItemRarity(int itemId, int minAmount, int maxAmount, double rarity) {
			this.itemId = itemId;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
			this.rarity = rarity;
		}

		public String getColor() {
			switch(toString()) {
				case "Common":
					return "<col=00FF00>";
				case "Uncommon":
					return "<col=FFFF00>";
				case "Rare":
					return "<col=FF0000>";
				case "Very Rare":
					return "<col=990000>";
				case "Extremely Rare":
					return "<col=550000>";
				default:
					return "";
			}
		}

		@Override
		public String toString() {

			if (rarity == 0.25) {
				return "Common";
			}
			if (rarity == 0.5) {
				return "Uncommon";
			}
			if (rarity == 0.75) {
				return "Rare";
			}
			if (rarity == 1) {
				return "Very Rare";
			}
			if (rarity == 2) {
				return "Extremely Rare";
			}
			return "N/A";
		}

		public int getItemId() {
			return itemId;
		}
		public double getRarity() {
			return rarity;
		}

		public int getMinAmount() {
			return minAmount;
		}

		public int getMaxAmount() {
			return maxAmount;
		}
	}

	public void write() {
		final Gson gson = new Gson();
		final File dir = new File("./data/json/drops/");
		for (final File drop : dir.listFiles()) {
			try (final BufferedReader parse = new BufferedReader(
					new FileReader(drop))) {
				final NPCLootTable lootTable = gson.fromJson(parse,
						NPCLootTable.class);
				List<ItemRarities> npcList = new ArrayList<>();
				List<Integer> ids = Arrays.stream(lootTable.getNpcIdentifiers()).boxed().collect(toList());
				List<ItemRarity> rarity = new ArrayList<>();
				lootTable.getDynamicDrops().stream().filter(Objects::nonNull).forEach(i -> {
					if (i.getHitRollCeil() >= 22.0) {
						rarity.add(new ItemRarity(i.getItemID(), i.getMinAmount(), i.getMaxAmount(), 0.25));//common
					}
					if (i.getHitRollCeil() >= 11.0 && i.getHitRollCeil() < 20.0) {
						rarity.add(new ItemRarity(i.getItemID(), i.getMinAmount(), i.getMaxAmount(), 0.50));//uncommon
					}
					if (i.getHitRollCeil() >= 5.0 && i.getHitRollCeil() < 10.0) {
						rarity.add(new ItemRarity(i.getItemID(), i.getMinAmount(), i.getMaxAmount(), 0.75));//rare
					}
					if (i.getHitRollCeil() < 4.0) {
						rarity.add(new ItemRarity(i.getItemID(), i.getMinAmount(), i.getMaxAmount(), 1));//very rare gear
					}
					if (i.getHitRollCeil() < 2.0) {
						rarity.add(new ItemRarity(i.getItemID(), i.getMinAmount(), i.getMaxAmount(), 2));//only pets/mutagens/etc
					}
				});

				npcList.add(new ItemRarities(ids, rarity));

				Gson swag = new GsonBuilder().setPrettyPrinting().create();

				File file = new File("./data/json/examine/" + drop.getName().replace(".gson", ".json"));

				if (!file.exists()) {
					file.createNewFile();
				}

				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				swag.toJson(npcList, writer);
				writer.close();

				Scanner fileScanner = new Scanner(file);
				fileScanner.nextLine();
				FileWriter fileStream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fileStream);
				while (fileScanner.hasNextLine()) {
					String next = fileScanner.nextLine();
					if (next.equals("\n")) {
						out.newLine();
					} else if (fileScanner.hasNextLine()) {
						out.write(next);
					}
					out.newLine();
				}
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
