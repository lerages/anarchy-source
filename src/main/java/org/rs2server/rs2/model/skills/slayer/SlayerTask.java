package org.rs2server.rs2.model.skills.slayer;

import java.util.Arrays;

public class SlayerTask {

	/**
	 * The slayer task groups.
	 * Each group has a unique id which is assigned by Jagex for the Slayer rewards interface,
	 * therefore these ids SHOULD NOT BE TOUCHED.
	 */
	public enum TaskGroup {
		GOBLINS(2),
		BATS(8),
		HILL_GIANTS(14),
		ICE_GIANTS(15),
		FIRE_GIANTS(16),
		MOSS_GIANTS(17),
		ICE_WARRIORS(19),
		GREEN_DRAGONS(24),
		BLACK_DRAGONS(27),
		LESSER_DEMONS(28),
		GREATER_DEMONS(29),
		BLACK_DEMONS(30),
		HELLHOUNDS(31),
		DAGANNOTHS(31),
		TUROTHS(36),
		CAVE_CRAWLERS(37),
		CRAWLING_HANDS(39),
		ABERRANT_SPECTRES(41),
		ABYSSAL_DEMONS(42),
		BASILISKS(43),
		COCKATRICE(44),
		KURASKS(45),
		GARGOYLES(46),
		PYREFIENDS(47),
		BLOODVELDS(48),
		DUST_DEVILS(49),
		JELLIES(50),
		NECHRYAEL(52),
		BRONZE_DRAGONS(58),
		IRON_DRAGONS(59),
		STEEL_DRAGONS(60),
		DARK_BEASTS(66),
		SKELETAL_WYVERN(72),
		ANKOUS(79),
		CAVE_HORRORS(80),
		SPIRITUAL_CREATURES(89),
		CAVE_KRAKEN(92),
		SMOKE_DEVIL(95),
		TZHAARS(96),
		BOSSES(98),
		JAD(97),
		CREATURES(99), BLUE_DRAGON(25), BANSHEE(38);

		private int id;

		TaskGroup(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static TaskGroup forName(String taskGroup) {
			if (taskGroup == null) {
				return null;
			}
			return Arrays.stream(TaskGroup.values()).filter(g -> g.name().equals(taskGroup)).findFirst().get();
		}
	}

	public enum Master {
		TURAEL(401, 0,
				new Object[][] {
			{"Giant bat", 1, 20, 50, 32.0, TaskGroup.BATS},
			{"Goblin", 1, 10, 45, 5.0, TaskGroup.GOBLINS},
			{"Crawling Hand", 5, 10, 60, 19.0, TaskGroup.CRAWLING_HANDS},
			{"Rock Crab", 1, 10, 45, 50.0, TaskGroup.CREATURES},
			{"Hill Giant", 20, 15, 60, 35.0, TaskGroup.HILL_GIANTS},
			{"Lesser demon", 60, 25, 60, 81.0, TaskGroup.LESSER_DEMONS},
			{"Cave crawler", 5, 35, 75, 22.0, TaskGroup.CAVE_CRAWLERS},
			{"Cockatrice", 25, 50, 175, 37.0, TaskGroup.COCKATRICE},
			{"Green dragon", 35, 35, 200, 79.0, TaskGroup.GREEN_DRAGONS},
			{"Moss giant", 35, 30, 100, 60.0, TaskGroup.MOSS_GIANTS},
		}),
		
		VANNAKA(403, 4,
				new Object[][]{
						{"Giant bat", 1, 25, 50, 32.0, TaskGroup.BATS},
						{"Goblin", 1, 10, 45, 5.0, TaskGroup.GOBLINS},
						{"Crawling Hand", 5, 10, 60, 19.0, TaskGroup.CRAWLING_HANDS},
						{"Rock Crab", 1, 10, 45, 50.0, TaskGroup.CREATURES},
						{"Hill Giant", 20, 15, 60, 35.0, TaskGroup.HILL_GIANTS},
						{"Lesser demon", 60, 25, 60, 81.0, TaskGroup.LESSER_DEMONS},
						{"Cave crawler", 5, 35, 75, 22.0, TaskGroup.CAVE_CRAWLERS},
						{"Cockatrice", 25, 50, 175, 37.0, TaskGroup.COCKATRICE},
						{"Dagannoth", 40, 25, 60, 70.0, TaskGroup.DAGANNOTHS},
						{"Green dragon", 60, 35, 200, 79.0, TaskGroup.GREEN_DRAGONS},
						{"Fire giant", 62, 40, 120, 111.0, TaskGroup.FIRE_GIANTS},
						{"Moss giant", 35, 30, 100, 60.0, TaskGroup.MOSS_GIANTS},
						{"Bloodveld", 50, 60, 90, 120.0, TaskGroup.BLOODVELDS},
						{"Turoth", 55, 30, 80, 79.0, TaskGroup.TUROTHS},
						{"Jelly", 52, 30, 80, 75.0, TaskGroup.JELLIES},
						{"Pyrefiend", 45, 30, 100, 45.0, TaskGroup.PYREFIENDS},
						{"Basilisk", 40, 50, 90, 75.0, TaskGroup.BASILISKS}}),
		CHAELDAR(404, 10,
				new Object[][]{
						{"Hill Giant", 10, 15, 60, 35.0, TaskGroup.HILL_GIANTS},
						{"Lizardman", 70, 25, 60, 60.0, TaskGroup.CAVE_CRAWLERS},
						{"Lesser demon", 30, 25, 60, 81.0, TaskGroup.LESSER_DEMONS},
						{"Cockatrice", 25, 50, 175, 37.0, TaskGroup.COCKATRICE},
						{"Green dragon", 35, 35, 200, 79.0, TaskGroup.GREEN_DRAGONS},
						{"Blue dragon", 35, 35, 200, 105.0, TaskGroup.BLUE_DRAGON},
						{"Fire giant", 62, 40, 120, 111.0, TaskGroup.FIRE_GIANTS},
						{"Moss giant", 35, 30, 100, 60.0, TaskGroup.MOSS_GIANTS},
						{"Bronze dragon", 50, 30, 70, 125.0, TaskGroup.BRONZE_DRAGONS},
						{"Iron dragon", 65, 30, 70, 173.2, TaskGroup.IRON_DRAGONS},
						{"Steel dragon", 79, 30, 70, 220.4, TaskGroup.STEEL_DRAGONS},
						{"Black dragon", 75, 30, 100, 119.4, TaskGroup.BLACK_DEMONS},
						{"Abyssal demon", 85, 60, 130, 150.0, TaskGroup.ABYSSAL_DEMONS},
						{"Cave horror", 58, 50, 90, 55.0, TaskGroup.CAVE_HORRORS},
						{"TzHaar-Ket", 40, 30, 80, 140.0, TaskGroup.TZHAARS},
						{"TzHaar-Xil", 40, 30, 80, 125.0, TaskGroup.TZHAARS},
						{"Dagannoth", 40, 40, 80, 70.0, TaskGroup.DAGANNOTHS},
						{"Banshee", 15, 30, 80, 45.0, TaskGroup.BANSHEE},
						{"Bloodveld", 50, 60, 90, 120.0, TaskGroup.BLOODVELDS},
						{"Gargoyle", 75, 70, 100, 105.0, TaskGroup.GARGOYLES},
						{"Hellhound", 50, 50, 100, 116.0, TaskGroup.HELLHOUNDS},
						{"Turoth", 55, 30, 80, 79.0, TaskGroup.TUROTHS},
						{"Kurask", 70, 40, 100, 97.0, TaskGroup.KURASKS},
						{"Jelly", 52, 30, 80, 75.0, TaskGroup.JELLIES},
						{"Pyrefiend", 45, 30, 100, 45.0, TaskGroup.PYREFIENDS},
						{"Basilisk", 40, 50, 90, 75.0, TaskGroup.BASILISKS}}),

		NIEVE(6797, 12,
				new Object[][]{
						{"Fire giant", 50, 120, 190, 111.0, TaskGroup.FIRE_GIANTS},
						{"Moss giant", 35, 120, 150, 60.0, TaskGroup.MOSS_GIANTS},
						{"Bronze dragon", 62, 20, 65, 125.0, TaskGroup.BRONZE_DRAGONS},
						{"Iron dragon", 65, 25, 60, 173.2, TaskGroup.IRON_DRAGONS},
						{"Steel dragon", 79, 30, 70, 220.4, TaskGroup.STEEL_DRAGONS},
						{"Black demon", 70, 120, 185, 119.4, TaskGroup.BLACK_DEMONS},
						{"Black dragon", 77, 30, 70, 190.0, TaskGroup.BLACK_DRAGONS},
						{"Abyssal demon", 85, 120, 185, 150.0, TaskGroup.ABYSSAL_DEMONS},
						{"Hellhound", 50, 120, 185, 116.0, TaskGroup.HELLHOUNDS},
						{"Lesser demon", 30, 120, 185, 81.0, TaskGroup.LESSER_DEMONS},
						{"Greater demon", 50, 120, 185, 87.0, TaskGroup.GREATER_DEMONS},
						{"Nechryael", 80, 120, 185, 125.0, TaskGroup.NECHRYAEL},
						{"Gargoyle", 75, 120, 185, 105.0, TaskGroup.GARGOYLES},
						{"TzHaar-Ket", 40, 100, 160, 140.0, TaskGroup.TZHAARS},
						{"TzHaar-Xil", 40, 100, 160, 125.0, TaskGroup.TZHAARS},
						{"Dagannoth", 40, 120, 185, 70.0, TaskGroup.DAGANNOTHS},
						{"Turoth", 55, 50, 100, 79, TaskGroup.TUROTHS},
						{"Jelly", 52, 100, 160, 75, TaskGroup.JELLIES},
						{"Kurask", 70, 120, 180, 97, TaskGroup.KURASKS},
						{"Ankou", 1, 70, 120, 60.0, TaskGroup.ANKOUS},
						{"Bloodveld", 50, 120, 185, 120.0, TaskGroup.BLOODVELDS},
						{"Cave horror", 58, 100, 180, 55.0, TaskGroup.CAVE_HORRORS},
						{"Dust devil", 65, 120, 185, 105.0, TaskGroup.DUST_DEVILS},
						{"Smoke devil", 93, 120, 185, 185.0, TaskGroup.SMOKE_DEVIL},
                        {"Dark beast", 90, 100, 130, 225.4, TaskGroup.DARK_BEASTS},
						{"Skeletal Wyvern", 72, 30, 70, 200.0, TaskGroup.SKELETAL_WYVERN},
						{"Aberrant spectre", 60, 120, 185, 90, TaskGroup.ABERRANT_SPECTRES},
						{"Cave kraken", 87, 125, 185, 125.0, TaskGroup.CAVE_KRAKEN},
						{"Spiritual mage", 83, 120, 185, 75.0, TaskGroup.SPIRITUAL_CREATURES}
				}),
		MAZCHNA(402, 2,
				new Object[][] {
			{"Giant bat", 1, 25, 50, 32.0, TaskGroup.BATS},
			{"Goblin", 1, 10, 45, 5.0, TaskGroup.GOBLINS},
			{"Crawling Hand", 5, 10, 60, 19.0, TaskGroup.CRAWLING_HANDS},
			{"Rock Crab", 1, 10, 45, 50.0, TaskGroup.CREATURES},
			{"Hill Giant", 20, 15, 60, 35.0, TaskGroup.HILL_GIANTS},
			{"Lesser demon", 30, 25, 60, 81.0, TaskGroup.LESSER_DEMONS},
			{"Cave crawler", 5, 35, 75, 22.0, TaskGroup.CAVE_CRAWLERS},
			{"Cockatrice", 25, 50, 175, 37.0, TaskGroup.COCKATRICE},
			{"Green dragon", 35, 35, 200, 79.0, TaskGroup.GREEN_DRAGONS},
			{"Moss giant", 35, 30, 100, 60.0, TaskGroup.MOSS_GIANTS}
				}),

		DURADEL(405, 15, new Object[][] { 
				{ "Bloodveld", 50, 60, 90, 120, TaskGroup.BLOODVELDS},
                { "Gargoyle", 75, 70, 100, 105, TaskGroup.GARGOYLES},
				{ "Dark beast", 70, 90, 130, 225.4, TaskGroup.DARK_BEASTS},
				{ "Dagannoth", 40, 120, 185, 70.0, TaskGroup.DAGANNOTHS},
				{ "Greater demon", 1, 50, 90, 87, TaskGroup.GREATER_DEMONS},
				{ "Hellhound", 1, 50, 100, 116, TaskGroup.HELLHOUNDS}, 
				{ "Turoth", 55, 30, 80, 79, TaskGroup.TUROTHS},
				{ "Kurask", 70, 40, 100, 97, TaskGroup.KURASKS}, 
				{ "Cave kraken", 87, 100, 125, 125.0, TaskGroup.CAVE_KRAKEN},
				{ "Dust devil", 65, 120, 185, 105.0, TaskGroup.DUST_DEVILS},
				{ "Spiritual mage", 83, 120, 185, 75.0, TaskGroup.SPIRITUAL_CREATURES},
				{ "Jelly", 52, 30, 80, 75, TaskGroup.JELLIES},
				{ "Abyssal demon", 85, 60, 130, 150, TaskGroup.ABYSSAL_DEMONS}
				});

		private int id;
		private int taskRewardPoints;
		private Object[][] data;
		private TaskGroup group;

		Master(int id, int taskRewardPoints, Object[][] data) {
			this.id = id;
			this.taskRewardPoints = taskRewardPoints;
			this.data = data;
		}

		public static Master forId(int id) {
			for (Master master : Master.values()) {
				if (master.id == id) {
					return master;
				}
			}
			return null;
		}

		public int getId() {
			return id;
		}

		public int getTaskRewardPoints() {
			return taskRewardPoints;
		}

		public Object[][] getData() {
			return data;
		}

	}

	private Master master;
	private int taskId;
	private int taskAmount;
	private int initialAmount;

	public SlayerTask(Master master, int taskId, int taskAmount) {
		this.master = master;
		this.taskId = taskId;
		this.initialAmount = taskAmount;
		this.taskAmount = taskAmount;
	}

	public String getName() {
		return (String) master.data[taskId][0];
	}

	public int getTaskId() {
		return taskId;
	}

	public int getTaskAmount() {
		return taskAmount;
	}

	public void decreaseAmount() {
		taskAmount--;
	}

	public double getXPAmount() {
		//return Double.parseDouble(master.data[taskId][4].toString()) * 1.2;
		return Double.parseDouble(master.data[taskId][4].toString());
	}

	public int getInitialAmount() {
		return initialAmount;
	}

	public Master getMaster() {
		return master;
	}

	/*
	 1=monkeys
2=goblins
3 = rats
4 = spiders
5 = birds
6 = cows
7 = scorpions
8 = bats
9 = wolves
10 = zombies
11 = skeletons
12 = ghosts
13 = bears
14 = hill giants
15 = ice giants
16 = fire giants
17 = moss giants
18 = trolls
19 = ice warriors
20 = ogres
21 = hobgoblins
22 = dogs
23 = ghouls
24 = green dragons
25 = blue dragons
26 = red dragons
27 = black dragons
28 = lesser demons
29 = greater demons
30 = black demons
31 = hell hounds
32 = shadow warrior
33 = werewolves
34 = vampires
35 = daggannoths
36 = turoths
37 = cave crawlers
38 = banshees
39 = crawling hands
40 = infernal mages
41 = aberrant spectres
42 = abyssal demons
43 = basilisks
44 = cocatrice
45 = kurask
[02:25:01] Thomas: 45: kurasks
46: gargoyles
47: Pyrefiends
48: Bloodveld
49: Dust devils
50: jellies
51: rockslugs
52: Nechryael
53: Kalphite
54: Earth Warriors
56: Otherwordly beings
57: Dwarves
58: Bronze Dragons
59: Iron dragons
60: steel dragons
61: wall beasts
62: cave slimes
63: cave bugs
64: shades
65: crocodiles
66: dark beasts
67: mogres
68: desert lizards
69: fever spiders
70: harpie bug swarms
71: sea snakes
72: skeletal wyverns
73: killerwatts
74: mutated zygomites
75: icefiends
76: minotaurs
77: fleshcrawlers
78: catablepon
79: ankou
80: cave horrors
81: jungle horrors
82: goraks
83: suqahs
84: brine rats
85: minions of scabaras
86: terror dogs
87: molanisks
88: waterfiends
89: spiritual creatures
90: creatures
91: Creatures
92: cave kraken
93: mithril dragons
94: aviansies
95: smoke devils
96: tzhaar
97: tztok-jad
98: Bosses
99: creatures

	 */

}
