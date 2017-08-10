package org.rs2server.rs2.model.skills.fish;

public enum Fish {

    SHRIMP(317, 10, 1, 0),
    SARDINE(327, 20, 5, 0),
    HERRING(345, 30, 10, 0),
    ANCHOVIES(321, 40, 15, 0),
    MACKEREL(353, 20, 16, 0),
    TROUT(335, 50, 20, 0),
    COD(341, 45, 23, 0),
    PIKE(349, 60, 25, 0),
    SALMON(331, 70, 30, 0),
    TUNA(359, 80, 35, 0),
    LOBSTER(377, 90, 40, 0),
    BASS(363, 100, 46, 0),
    SWORDFISH(371, 100, 50, 0),
    MONKFISH(7944, 120, 62, 0),
    SHARK(383, 110, 76, 0),
    SEA_TURTLE(395, 38, 79, 0),
    MANTA_RAY(389, 46, 81, 0),
	ANGLER_FISH(13439, 120, 82, 0),
    DARK_CRAB(11934, 125, 85, 0),
	KARAMBWAN(3142, 115, 65, 0),
	SACRED_EEL(13339, 130, 87, 0),
	LEAPING_TROUT(11328, 50, 48, 15),
	LEAPING_SALMON(11330, 70, 58, 30),
	LEAPING_STURGEON(11332, 80, 70, 45);

    private final int id;
    private final int xp;
    private final int level;
    private final int barb_xp;

    private Fish(int id, int xp, int level, int barb_xp) {
        this.id = id;
        this.xp = xp;
        this.level = level;
        this.barb_xp = barb_xp;
    }

    public int getId() {
        return id;
    }

    public int getXp() {
        return (int)(xp);
    }

    public int getLevel() {
        return level;
    }
    
    public int getBarbXp()
    {
    	return (int)(barb_xp);
    }

}
