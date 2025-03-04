package org.rs2server.rs2.model.minigame.fightcave;

/**
 * 
 * @author 'Mystic Flow
 */
public class Wave {

	public static final int TZ_KIH = 2189, TZ_KEK_SPAWN = 2191, TZ_KEK = 2192,
			TOK_XIL = 2193, YT_MEJKOT = 3124, KET_ZEK = 3125, TZTOK_JAD = 3127;

	private static final int[][] SPAWNS = {
			// {2734}
			// ,{2734,2734}
			// ,{2736}
			// ,{2736,2734}
			// ,{2736,2734,2734}
			// ,{2736,2736}
			// ,{2739}
			// ,{2739,2734}
			// ,{2739,2734,2734}
			// ,{2739,2736}
			// ,{2739,2736,2734}
			// ,{2739,2736,2734,2734}
			// ,{2739,2736,2736}
			// ,{2739,2739}
			// ,{2741}
			// ,{2741,2734}
			// ,{2741,2734,2734}
			// ,{2741,2736}
			// ,{2741,2736,2734}
			// ,{2741,2736,2734,2734}
			// ,{2741,2736,2736}
			// ,{2741,2739}
			// ,{2741,2739,2734}
			// ,{2741,2739,2734,2734}
			// ,{2741,2739,2736}
			// ,{2741,2739,2736,2734}
			// ,{2741,2739,2736,2734,2734}
			// ,{2741,2739,2736,2736}
			// ,{2741,2739,2739}
			// ,{2741,2741}
			// ,{2743}
			// ,{2743,2734}
			// ,{2743,2734,2734}
			// ,{2743,2736}
			// ,{2743,2736,2734}
			// ,{2743,2736,2734,2734}
			// ,{2743,2736,2736}
			// ,{2743,2739}
			// ,{2743,2739,2734}
			// ,{2743,2739,2734,2734}
			// ,{2743,2739,2736}
			// ,{2743,2739,2736,2734}
			// ,{2743,2739,2736,2734,2734}
			// ,{2743,2739,2736,2736}
			// ,{2743,2739,2739}
			// ,{2743,2741}
			// ,{2743,2741,2734}
			// ,{2743,2741,2734,2734}
			// ,{2743,2741,2736}
			// ,{2743,2741,2736,2734}
			// ,{2743,2741,2736,2734,2734}
			// ,{2743,2741,2736,2736}
			// ,{2743,2741,2739}
			// ,{2743,2741,2739,2734}
			// ,{2743,2741,2739,2734,2734}
			// ,{2743,2741,2739,2736}
			// ,{2743,2741,2739,2736,2734}
			// ,{2743,2741,2739,2736,2734,2734}
			// ,{2743,2741,2739,2736,2736}
			// ,{2743,2741,2739,2739}
			// ,{2743,2741,2741}
			// ,{2743,2743}
			// ,{2745}
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TZ_KIH },
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TZ_KIH, TZ_KIH },
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TZ_KEK },
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TZ_KEK, TZ_KIH },
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TZ_KEK, TZ_KIH, TZ_KIH },
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TZ_KEK, TZ_KEK },
			//{ KET_ZEK, YT_MEJKOT, TOK_XIL, TOK_XIL },
			//{ KET_ZEK, YT_MEJKOT, YT_MEJKOT }, { KET_ZEK, KET_ZEK },
			{ TZ_KIH },
			{ TZ_KIH, TZ_KIH },
			{ TZ_KEK },
			{ TZ_KIH, TZ_KEK },
			{ TZ_KIH, TZ_KIH, TZ_KEK },
			{ TZ_KEK, TZ_KEK },
			{ TOK_XIL },
			{ TZ_KIH, TOK_XIL },
			{ TZ_KIH, TZ_KIH, TOK_XIL },
			{ TZ_KEK, TOK_XIL },
			{ TZ_KIH, TZ_KEK, TOK_XIL},
			{ TZ_KIH, TZ_KIH, TZ_KEK, TOK_XIL},
			{ TZ_KEK, TZ_KEK, TOK_XIL },
			{ TOK_XIL, TOK_XIL },
			{ YT_MEJKOT },
			{ TZ_KIH, YT_MEJKOT },
			{ TZ_KIH, TZ_KIH, YT_MEJKOT },
			{ TZ_KEK, YT_MEJKOT },
			{ TZ_KIH, TZ_KEK, YT_MEJKOT },
			{ TZ_KIH, TZ_KIH, TZ_KEK, YT_MEJKOT },
			{ TZ_KEK, TZ_KEK, YT_MEJKOT},
			{ TOK_XIL, YT_MEJKOT },
			{ TZ_KIH, TOK_XIL, YT_MEJKOT},
			{ TZ_KIH, TZ_KIH, TOK_XIL, YT_MEJKOT},
			{ TZ_KEK, TOK_XIL, YT_MEJKOT },
			{ TZ_KIH, TZ_KEK, TOK_XIL, YT_MEJKOT},
			{ TZ_KIH, TZ_KIH, TZ_KEK, TOK_XIL, YT_MEJKOT},
			{ TZ_KEK, TZ_KEK, TOK_XIL, YT_MEJKOT},
			{ TOK_XIL, TOK_XIL, YT_MEJKOT},
			{ YT_MEJKOT, YT_MEJKOT},
			{ KET_ZEK },
			{ TZ_KIH, KET_ZEK },
			{ TZ_KIH, TZ_KIH, KET_ZEK },
			{ TZ_KEK, KET_ZEK },
			{ TZ_KIH, TZ_KEK, KET_ZEK},
			{ TZ_KIH, TZ_KIH, TZ_KEK, KET_ZEK},
			{ TZ_KEK, TZ_KEK, KET_ZEK },
			{ TOK_XIL, KET_ZEK },
			{ TZ_KIH, TOK_XIL, KET_ZEK},
			{ TZ_KIH, TZ_KIH, TOK_XIL, KET_ZEK },
			{ TZ_KEK, TOK_XIL, KET_ZEK },
			{ TZ_KIH, TZ_KEK, TOK_XIL, KET_ZEK },
			{ TZ_KIH, TZ_KIH, TZ_KEK, TOK_XIL, KET_ZEK },
			{ TZ_KEK, TZ_KEK, TOK_XIL, KET_ZEK },
			{ TOK_XIL, TOK_XIL, KET_ZEK },
			{ YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, TZ_KIH, YT_MEJKOT, KET_ZEK},
			{ TZ_KEK, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, TZ_KEK, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, TZ_KIH, TZ_KEK, YT_MEJKOT, KET_ZEK},
			{ TZ_KEK, TZ_KEK, YT_MEJKOT, KET_ZEK},
			{ TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH,  TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, TZ_KIH, TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TZ_KEK, TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, TZ_KEK, TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TZ_KIH, TZ_KIH, TZ_KEK, TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TZ_KEK, TZ_KEK, TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ TOK_XIL, TOK_XIL, YT_MEJKOT, KET_ZEK},
			{ YT_MEJKOT, YT_MEJKOT, KET_ZEK},
			{ KET_ZEK, KET_ZEK },
			{ TZTOK_JAD } };

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
