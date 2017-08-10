package org.rs2server.rs2.model.minigame.rfd;

public class RFDWave {

	public static final int AGRITH = 4880;
	public static final int FLAMBEED = 4881;
	public static final int KARAMEL = 4882;
	public static final int DESSOURT = 4883;
	public static final int CULINAROMANCER = 4878;

	private static final int[][] SPAWNS = {
		{ },
		{AGRITH}, 
		{AGRITH, FLAMBEED},
		{AGRITH, FLAMBEED, KARAMEL},
		{AGRITH, FLAMBEED, KARAMEL, DESSOURT}
	};
	
	private int stage;

	public void set(int stage) {
		this.stage = stage;
	}

	public int[] spawns() {
		return SPAWNS[stage];
	}

	public int getStage() {
		return stage;
	}

}
