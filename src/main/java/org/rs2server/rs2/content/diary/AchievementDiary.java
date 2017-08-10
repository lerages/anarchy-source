package org.rs2server.rs2.content.diary;

import java.nio.ByteBuffer;

import org.rs2server.rs2.model.npc.NPCDefinition;
import org.rs2server.rs2.model.player.Player;

public class AchievementDiary {

	public static final int DIARY_COMPONENT = 275;
	
	private int CONFIG_RED = 0;
	
	private int CONFIG_YELLOW = 1;
	
	private int CONFIG_GREEN = 2;
	
	private int CONFIG_BLUE = 3;
	
	private static final String RED = "<col=8A0808>";

	private static final String BLUE = "<col=08088A>";

	private static final String YELLOW = "<col=F7FE2E>";

	private static final String GREEN = "<col=3ADF00>";
	
	private final DiaryType type;
	
	private final boolean[] started = new boolean[3];

	private final boolean[] rewarded = new boolean[3];

	private final boolean[][] completed;

	public AchievementDiary(DiaryType type) {
		this.type = type;
		this.completed = new boolean[type.getAchievements().length][50];
	}
	
	public void open(Player player) {
		clear(player);
		sendString(player, "<red>Achievement Diary - " + type.getName(), 2);
		sendString(player, (isComplete() ? GREEN : hasStarted() ? YELLOW : "<red>") + type.getName() + " Area Tasks", 11);
		boolean complete;
		String line = "";
		int child = 13;
		for (int level = 0; level < type.getAchievements().length; level++) {
			sendString(player, getStatus(level) + getLevel(level) + "", child);
			child++;
			for (int i = 0; i < type.getAchievements(level).length; i++) {
				complete = isComplete(level, i);
				line = (complete ? "<str>" : "") + (complete ? "<str>" + type.getAchievements(level)[i] : type.getAchievements(level)[i]);
				if (line.contains("<br><br>")) {
					String[] lines = line.split("<br><br>");
					for (String l : lines) {
						sendString(player, l, child);
						child++;
					}
				} else {
					sendString(player, line, child);
					child++;
				}
			}
			child++;
		}
		//	sendString(player, builder.toString(), 11);
		//Changes the size of the scroll bar
		//player.getPacketDispatch().sendRunScript(1207, "i", new Object[] { 330 });
		//sendString(player, builder.toString(), 11);
			player.getActionSender().sendInterface(DIARY_COMPONENT, false);
	}
	
	private void clear(Player player) {
		for (int i = 0; i < 133; i++) {
			player.getActionSender().sendString(i, DIARY_COMPONENT, "");
		}
	}
	
	public void save(ByteBuffer buffer) {
		buffer.put((byte) 1);
		for (int i = 0; i < 3; i++) {
			buffer.put((byte) (started[i] ? 1 : 0));
		}
		buffer.put((byte) 2).put((byte) completed.length);
		for (int i = 0; i < completed.length; i++) {
			buffer.put((byte) type.getAchievements(i).length);
			for (int x = 0; x < type.getAchievements(i).length; x++) {
				buffer.put((byte) (completed[i][x] ? 1 : 0));
			}
		}
		buffer.put((byte) 3).put((byte) rewarded.length);
		for (int i = 0; i < rewarded.length; i++) {
			buffer.put((byte) (rewarded[i] ? 1 : 0));
		}
		buffer.put((byte) 0);
	}
	
	public void parse(ByteBuffer buffer) {
		int opcode, size;
		while ((opcode = buffer.get()) != 0) {
			switch (opcode) {
			case 1:
				for (int i = 0; i < 3; i++) {
					started[i] = buffer.get() == 1;
				}
				break;
			case 2:
				size = buffer.get() & 0xFF;
				for (int i = 0; i < size; i++) {
					int size_ = buffer.get() & 0xFF;
					for (int x = 0; x < size_; x++) {
						completed[i][x] = buffer.get() == 1;
					}
				}
				break;
			case 3:
				size = buffer.get() & 0xFF;
				for (int i = 0; i < size; i++) {
					rewarded[i] = buffer.get() == 1;
				}
				break;
			}
		}
	}
	
	public void drawStatus(Player player) {
		if (hasStarted()) {
			player.getActionSender().sendString(type.getChild(), 259, (isComplete() ? GREEN : YELLOW) + type.getName());
			for (int i = 0; i < 3; i++) {
				player.getActionSender().sendString(type.getChild() + (i + 1), 259, (isComplete(i) ? GREEN : hasStarted(i) ? YELLOW : "<col=FF0000>") + getLevel(i));
			}
		}
	}
	
	public void updateTask(Player player, int level, int index, boolean complete) {
		if (!started[level]) {
			started[level] = true;
		}
		if (!complete) {
			player.sendMessage("<col=0040ff>Well done! A " + type.getName() + " task has been updated.");
		} else {
			completed[level][index] = true;
			player.sendMessages("<col=dc143c>Well done! You have completed " + (level == 0 ? "an easy" : level == 1 ? "a medium" : "a hard") + " task in the " + type.getName() + " area. Your", "<col=dc143c>Achievement Diary has been updated.");
		}
		if (isComplete(level)) {
			player.sendMessages("<col=dc143c>You have completed all of the " + getLevel(level).toLowerCase() + " tasks in the " + type.getName() + " area.", "<col=dc143c>Speak to " + NPCDefinition.forId(type.getNpc(level)).getName() + " to claim your reward.");
		}
		drawStatus(player);
	}
	
	private void sendString(Player player, String string, int child) {
		player.getActionSender().sendString(child, DIARY_COMPONENT, string.replace("<blue>", BLUE).replace("<red>", RED));
	}
	
	public void setStarted(int level) {
		this.started[level] = true;
	}

	public void setCompleted(int level, int index) {
		this.completed[level][index] = true;
	}

	public boolean isComplete(int level) {
		for (int i = 0; i < type.getAchievements(level).length; i++) {
			if (!completed[level][i]) {
				return false;
			}
		}
		return true;
	}

	public boolean isComplete(int level, int index) {
		return completed[level][index];
	}

	public boolean isComplete() {
		for (int i = 0; i < completed.length; i++) {
			for (int x = 0; x < type.getAchievements(i).length; x++) {
				if (!completed[i][x]) {
					return false;
				}
			}
		}
		return true;
	}

	public int getLevel() {
		return isComplete(2) ? 2 : isComplete(1) ? 1 : isComplete(0) ? 0 : -1;
	}

	public String getLevel(int level) {
		return level == 0 ? "Easy" : level == 1 ? "Medium" : "Hard";
	}

	public String getStatus(int level) {
		return !hasStarted(level) ? RED : isComplete(level) ? GREEN : YELLOW;
	}
	
	public boolean hasStarted() {
		for (int i = 0; i < 3; i++) {
			if (started[i]) {
				return true;
			}
		}
		return false;
	}

	public boolean hasStarted(int level) {
		return started[level];
	}

	public void setRewarded(int level) {
		this.rewarded[level] = true;
	}

	public boolean hasReward(int level) {
		return rewarded[level];
	}

	public boolean[][] getCompleted() {
		return completed;
	}
	
	public DiaryType getType() {
		return type;
	}

	public boolean[] getStarted() {
		return started;
	}

	public boolean[] getRewarded() {
		return rewarded;
	}
	
}
