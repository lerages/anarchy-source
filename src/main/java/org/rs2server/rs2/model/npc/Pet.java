package org.rs2server.rs2.model.npc;

import org.rs2server.Server;
import org.rs2server.cache.format.CacheNPCDefinition;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.PathfindingService;
import org.rs2server.rs2.domain.service.api.PlayerService;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.fish.FishingSpot;
import org.rs2server.rs2.util.Misc;
import org.rs2server.util.functional.Optionals;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Twelve
 * @author Tim
 */
public class Pet extends NPC {

    public enum Pets {
        //item id, npc id
        ZULRAH_RANGE(12921, 2127, "Hisssss"),
        
        ZULRAH_MAGE(12940, 2129, "Hisssss"),

        ZULRAH_MELEE(12939, 2128, "Hisssss"),
        
        BLOODHOUND(19730, 6296, ""),
        
        CHOMPY_CHICK(13071, 4001, "Chirp"),

        DAGANNOTH_SUPREME(12643, 6626, ""),

        DAGANNOTH_PRIME(12644, 6627, ""),

        DAGANNOTH_REX(12645, 6641, ""),

        ARMADYL(12649, 6643, ""),

        BANDOS(12650, 6644, ""),

        SARADOMIN(12651, 6646, ""),

        ZAMORAK(12652, 6647, ""),

        KING_BLACK_DRAGON(12653, 6652, ""),

        CHAOS_ELEMENTAL(11995, 5907, ""),

        KALPHITE_PRINCESS_FORM_1(12654, 6653, ""),

        KRAKEN(12655, 6656, ""),

        SCORPIA(13181, 5547, ""),

        CALLISTO(13178, 497, ""),

        VETION(13179, 5536, ""),
        
        VETIONJR(13180, 5536, ""),
        
        VENENATIS(13177, 495, ""),

		JAD(13225, 5892, ""),

		SMOKE_DEVIL(12648, 6655, ""),

		BEAVER(13322, 6717, ""),

		HERON(13320, 6715, ""),

		ROCK_GOLEM(13321, 6716, ""),

		HELLPUPPY(13247, 964, ""),
		
		GIANT_SQUIRREL(20659, 7334, ""),
		
		TANGLEROOT(20661, 7335, ""),
		
		ROCKY(20663, 7336, ""),
		
		RIFT_GUARDIAN_FIRE(20665, 7337, ""),
		
		RIFT_GUARDIAN_AIR(20667, 7338, ""),
		
		RIFT_GUARDIAN_MIND(20669, 7339, ""),
		
		RIFT_GUARDIAN_WATER(20671, 7340, ""),
		
		RIFT_GUARDIAN_EARTH(20673, 7341, ""),
		
		RIFT_GUARDIAN_BODY(20675, 7342, ""),
		
		RIFT_GUARDIAN_COSMIC(20677, 7343, ""),
		
		RIFT_GUARDIAN_CHAOS(20679, 7344, ""),
		
		RIFT_GUARDIAN_NATURE(20681, 7345, ""),
		
		RIFT_GUARDIAN_LAW(20683, 7345, ""),
		
		RIFT_GUARDIAN_DEATH(20685, 7346, ""),
		
		RIFT_GUARDIAN_SOUL(20687, 7347, ""),
		
		RIFT_GUARDIAN_ASTRAL(20689, 7348, ""),
		
		RIFT_GUARDIAN_BLOOD(20691, 7349, ""),
		
		PHOENIX(20693, 7368, ""),
		
		ABBYSAL_ORPHAN(13262, 5883, ""),
		  
		BABY_MOLE(12646, 6651, ""),
		  
		KALPHITE_PRINCESS(12647, 6654, ""),
		  
		KALPHITE_PRINCESS2(12654, 6653, ""),
		  
		DARK_CORE(12816, 388, ""),
		  
		PENANCE_PET(12703, 6642, ""),
		  
		BABY_CHIN(13323, 6718, ""),
		  
		BABY_CHIN1(13324, 6719, ""),
		  
		BABY_CHIN2(13325, 6720, ""),
		  
		BABY_CHIN3(13326, 6721, ""),
		
		OLMET(20851, 7519, ""),
		
		SKOTOS(21273, 425, "")
		
		;

        private final int item;
        private final int npc;
        private final String npcText;

        Pets(int item, int npc, String npcText) {
            this.item = item;
            this.npc = npc;
            this.npcText = npcText;
        }

        private static Map<Integer, Pets> petItems = new HashMap<Integer, Pets>();
        private static Map<Integer, Pets> petNpcs = new HashMap<Integer, Pets>();

        public static Pets from(int item) {
            return petItems.get(item);
        }

        public static Pets fromNpc(int npc) {
            return petNpcs.get(npc);
        }

        static {
            for (Pets pet : Pets.values()) {
                petItems.put(pet.item, pet);
            }
            for (Pets pet : Pets.values()) {
                petNpcs.put(pet.npc, pet);
            }
        }

        public int getItem() {
            return item;
        }

        public int getNpc() {
            return npc;
        }
    }

    private final PathfindingService pathfindingService;
    private static final int MAX_DISTANCE = 13;

    public Pet(Player owner, int id) {
        super(id, owner.getLocation(), owner.getLocation(), owner.getLocation(), 0);
        this.pathfindingService = Server.getInjector().getInstance(PathfindingService.class);
        this.setInstancedPlayer(owner);
        this.setInteractingEntity(InteractionMode.FOLLOW, owner);
    }
    
    public static void skillingPet(Player player, Pet.Pets skilling_pet, int base_chance)
    {
    		int chance = player.getPerks()[5].isOwned() ? Misc.random(base_chance / 2) : Misc.random(base_chance);
    		
    		if (chance == 0) {
    			Pet.Pets pets = skilling_pet;
    			//if(Server.getInjector().getInstance(PlayerService.class).hasItemInInventoryOrBank(player, new Item(skilling_pet.getItem())))
    			if(player.getInventory().getCount(skilling_pet.getItem()) > 0 || player.getBank().getCount(skilling_pet.getItem()) > 0)
    			{
    				player.sendMessage("You have a funny feeling like you would have been followed...");
    				return;
    			}
    			if (player.getPet() != null) {
    				if(player.getPet().getId() == skilling_pet.getNpc())
    				{
    					player.sendMessage("You have a funny feeling like you would have been followed...");
    				} else {
    					player.sendMessage("You have a funny feeling like you're being followed...");
    					player.getInventory().add(new Item(skilling_pet.getItem()));
    				}
    			} else {
    				PlayerSettingsEntity settings = player.getDatabaseEntity().getPlayerSettings();
        			Pet pet = new Pet(player, pets.getNpc());
        			player.setPet(pet);
        			settings.setPetSpawned(true);
        			settings.setPetId(pets.getNpc());
        			World.getWorld().register(pet);
        			player.sendMessage("You have a funny feeling like you're being followed...");
    			}
    			World.getWorld().sendWorldMessage("<col=884422><img=35> " + player.getName() + " has just received a pet " +
    			CacheNPCDefinition.get(pets.getNpc()).getName() + ".");
    		}
    }

    @Override
    public void tick() {
        double distance = getLocation().getDistance(owner.getLocation());
        if (getLocation().equals(owner.getLocation())) {
            Optionals.nearbyFreeLocation(owner.getLocation()).ifPresent(l -> pathfindingService.travel(this, l));
        } else if (distance > MAX_DISTANCE) {
            Optionals.nearbyFreeLocation(owner.getLocation()).ifPresent(l -> {
                this.setLocation(owner.getLocation());
                this.setInteractingEntity(InteractionMode.FOLLOW, owner);
            });
        } else if (distance > 1) {
            pathfindingService.travelToPlayer(this, owner);
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }
}
