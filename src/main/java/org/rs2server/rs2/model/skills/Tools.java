package org.rs2server.rs2.model.skills;

import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.player.Player;

public class Tools {

	public enum Tool {
		DRAGON_AXE(6739, 61, 90, 2846, 0, 6743, 492, 6741, 1800, Skills.WOODCUTTING),
		RUNE_AXE(1359, 41, 90, 867, 0, 520, 492, 506, 427, Skills.WOODCUTTING),
		ADAMANT_AXE(1357, 31, 75, 869, 0, 518, 492, 504, 43, Skills.WOODCUTTING),
		MITHRIL_AXE(1355, 21, 60, 871, 0, 516, 492, 502, 18, Skills.WOODCUTTING),
		BLACK_AXE(1361, 6, 45, 873, 0, 514, 492, 500, 10, Skills.WOODCUTTING),
		STEEL_AXE(1353, 6, 30, 875, 0, 512, 492, 498, 0, Skills.WOODCUTTING),
		IRON_AXE(1349, 1, 15, 877, 0, 510, 492, 496, 0, Skills.WOODCUTTING),
		BRONZE_AXE(1351, 1, 0, 879, 0, 508, 492, 494, 0, Skills.WOODCUTTING),

        RUNE_PICKAXE(1275, 41, 100, 624, 3, 490, 466, 478, 1100, Skills.MINING),
		ADAMANT_PICKAXE(1271, 31, 80, 628, 4, 488, 466, 476, 107, Skills.MINING),
		MITHRIL_PICKAXE(1273, 21, 60, 629, 6, 486, 466, 474, 43, Skills.MINING),
		STEEL_PICKAXE(1269, 6, 40, 627, 7, 484, 466, 472, 14, Skills.MINING),
		IRON_PICKAXE(1267, 1, 20, 626, 9, 482, 466, 470, 0, Skills.MINING),
		BRONZE_PICKAXE(1265, 1, 0, 625, 11, 480, 466, 468, 0, Skills.MINING),
		HAMMER(2347, 1, 0, 620, 10, 480, 466, 468, 0, Skills.CONSTRUCTION);

		private int id;
		private int level;
		private int animation;
		private int bonus;
		private int essSpeed;
		private int head;
		private int handle;
		private int broken;
		private int repairCost;
		private int skill;

		private Tool(int id, int level, int bonus, int animation, int essSpeed, int head, int handle, int broken, int repairCost, int skill) {
			this.id = id;
			this.level = level;
			this.bonus = bonus;
			this.animation = animation;
			this.essSpeed = essSpeed;
			this.head = head;
			this.broken = broken;
			this.repairCost = repairCost;
			this.skill = skill;
		}

		public int getId() {
			return id;
		}

		public int getLevel() {
			return level;
		}

		public int getHead() {
			return head;
		}

		public int getAnimation() {
			return animation;
		}

		public int getBonus() {
			return bonus;
		}

		public int getEssSpeed() {
			return essSpeed;
		}

		public int getBroken() {
			return broken;
		}

		public int getSkill() {
			return skill;
		}

		public int getHandle() {
			return handle;
		}

		public int getRepairCost() {
			return repairCost;
		}

	}
	
	public static Tool getTool(Player player, int skillId) {
        for (Tool tool : Tool.values()) {
        	if (tool.getSkill() == skillId) {
        		if (player.getSkills().getLevel(skillId) >= tool.getLevel() && player.getEquipment().get(Equipment.SLOT_WEAPON).getId() == tool.getId() || player.getInventory().hasItem(new Item(tool.getId()))) {
					return tool;
				}
			}
		}
        return null;
	}

}

