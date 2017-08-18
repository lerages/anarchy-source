package org.rs2server.rs2.model.skills.fish;

import org.rs2server.rs2.model.Animation;

import java.util.HashMap;
import java.util.Map;

public enum FishingSpot {

    NET_NET_AND_BAIT(1, 1518, Animation.create(621), 303, -1, 10000, Fish.SHRIMP, Fish.ANCHOVIES),
    BAIT_NET_AND_BAIT(2, 1518, Animation.create(622), 307, 313, 10000, Fish.SARDINE, Fish.HERRING),
    LURE_LURE_AND_BAIT(1, 1512, Animation.create(622), 309, 314, 10000, Fish.TROUT, Fish.SALMON),
    BAIT_LURE_AND_BAIT(2, 1512, Animation.create(622), 307, 313, 10000, Fish.PIKE),
    CAGE_CAGE_AND_HARPOON(1, 1510, Animation.create(619), 301, -1, 10000, Fish.LOBSTER),
    HARPOON_CAGE_AND_HARPOON(2, 1510, Animation.create(618), 311, -1, 10000, Fish.TUNA, Fish.SWORDFISH),
    BIG_NET_NET_AND_HARPOON(1, 1520, Animation.create(621), 305, -1, 10000, Fish.MACKEREL, Fish.COD, Fish.BASS),
    HARPOON_NET_AND_HARPOON(2, 1520, Animation.create(618), 311, -1, 7000, Fish.SHARK),
	BIG_NET_AND_HARPOON(1, 4316, Animation.create(621), 303, -1, 9000, Fish.MONKFISH),
	HARPOON(2, 4316, Animation.create(618), 311, -1, 10000, Fish.TUNA, Fish.SWORDFISH),
	BAIT(1, 6825, Animation.create(622), 307, 13431, 7500, Fish.ANGLER_FISH),
    CAGE(1, 1536, Animation.create(619), 301, -1, 7000, Fish.DARK_CRAB),
	KARAMBWAN_NET(1, 1517, Animation.create(621), 303, -1, 10000, Fish.KARAMBWAN),
	BARBFISH(1, 1516, Animation.create(622), 11323, 314, 11000, Fish.LEAPING_TROUT, Fish.LEAPING_SALMON, Fish.LEAPING_STURGEON),
	;
	
    private static final Map<Integer, FishingSpot> fishingSpot = new HashMap<>();

    static {
        for (FishingSpot fishSpot : FishingSpot.values()) {
            fishingSpot.put(fishSpot.getNpcId() | (fishSpot.getClick() << 24),
                    fishSpot);
        }
    }

    public static FishingSpot forId(int object) {
        return fishingSpot.get(object);
    }

    private final int npcId;
    private final int item;
    private final int bait;
    private final int click;
    private final Animation animation;
    private final Fish[] fish;
	private final int petChance;

    FishingSpot(int click, int npcId, Animation animation, int item,
				int bait, int petChance, Fish... fish) {
        this.npcId = npcId;
        this.item = item;
        this.bait = bait;
        this.fish = fish;
        this.animation = animation;
        this.click = click;
		this.petChance = petChance;
    }

    public int getNpcId() {
        return npcId;
    }

    public int getItem() {
        return item;
    }

    public int getBait() {
        return bait;
    }

    public Animation getAnimation() {
        return animation;
    }

    public int getClick() {
        return click;
    }

	public int getPetChance() { return petChance; }

    public Fish[] getHarvest() {
        return fish;
    }

}
