package org.rs2server.rs2.content.managers;

import java.nio.ByteBuffer;

import org.rs2server.rs2.content.diary.AchievementDiary;
import org.rs2server.rs2.content.diary.DiaryType;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.smithing.SmithingUtils.ForgingBar;

public class AchievementDiaryManager {

    private final AchievementDiary[] diarys = new AchievementDiary[] { new AchievementDiary(DiaryType.KARAMJA), new AchievementDiary(DiaryType.VARROCK), new AchievementDiary(DiaryType.LUMBRIDGE) };
    
    private final Player player;
    
    public AchievementDiaryManager(Player player) {
		this.player = player;
	}
    
    public void save(ByteBuffer buffer) {
		buffer.put((byte) 1).put(((byte) diarys.length));
		for (AchievementDiary diary : diarys) {
			diary.save(buffer);
		}
		buffer.put((byte) 0);
	}
    
    public void parse(ByteBuffer buffer) {
		int opcode;
		while ((opcode = buffer.get()) != 0) {
			switch (opcode) {
			case 1:
				int length = buffer.get() & 0xFF;
				for (int i = 0; i < length; i++) {
					diarys[i].parse(buffer);
				}
				break;
			}
		}
	}
    
    public void updateTask(Player player, DiaryType type, int level, int index, boolean complete) {
		getDiary(type).updateTask(player, level, index, complete);
	}
    
    public boolean hasCompletedTask(DiaryType type, int level, int index) {
		return getDiary(type).isComplete(level, index);
	}
    
    public void setStarted(DiaryType type, int level) {
		getDiary(type).setStarted(level);
	}
    
    public void setCompleted(DiaryType type, int level, int index) {
		getDiary(type).setCompleted(level, index);
	}
    
    public AchievementDiary getDiary(DiaryType type) {
		if (type == null) {
			return null;
		}
		for (AchievementDiary diary : diarys) {
			if (diary.getType() == type) {
				return diary;
			}
		}
		return null;
	}
    
    public int getKaramjaGlove() {
		if (!hasGlove()) {
			return -1;
		}
		for (int i = 0; i < 3; i++) {
			if (player.getEquipment().containsItems(DiaryType.KARAMJA.getRewards()[i][0])) {
				return i;
			}
		}
		return -1;
	}
    
    public int getArmour() {
		if (!hasArmour()) {
			return -1;
		}
		for (int i = 0; i < 3; i++) {
			if (player.getEquipment().containsItems(DiaryType.VARROCK.getRewards()[i][0])) {
				return i;
			}
		}
		return -1;
	}

	public boolean checkMiningReward(int reward) {
		int level = player.getAchievementDiaryManager().getArmour();
		if (level == -1) {
			return false;
		}
		if (reward == 453) {
			return true;
		}
		return level == 0 && reward <= 442 || level == 1 && reward <= 447 || level == 2 && reward <= 449;
	}

	public boolean checkSmithReward(ForgingBar type) {
		int level = player.getAchievementDiaryManager().getArmour();
		if (level == -1) {
			return false;
		}
		return level == 0 && type.ordinal() <= ForgingBar.STEEL.ordinal() || level == 1 && type.ordinal() <= ForgingBar.MITHRIL.ordinal() || level == 2 && type.ordinal() <= ForgingBar.ADAMANT.ordinal();
	}

	public boolean hasGlove() {
		Item glove = player.getEquipment().get(Equipment.SLOT_GLOVES);
		return glove != null && (glove.getId() == DiaryType.KARAMJA.getRewards()[0][0].getId() || glove.getId() == DiaryType.KARAMJA.getRewards()[1][0].getId() || glove.getId() == DiaryType.KARAMJA.getRewards()[2][0].getId());
	}

	public boolean hasArmour() {
		Item plate = player.getEquipment().get(Equipment.SLOT_CHEST);
		return plate != null && (plate.getId() == DiaryType.VARROCK.getRewards()[0][0].getId() || plate.getId() == DiaryType.VARROCK.getRewards()[1][0].getId() || plate.getId() == DiaryType.VARROCK.getRewards()[2][0].getId());
	}

	public boolean isComplete(DiaryType type) {
		return diarys[type.ordinal()].isComplete();
	}

	public Player getPlayer() {
		return player;
	}

	public AchievementDiary[] getDiarys() {
		return diarys;
	}
	
}
