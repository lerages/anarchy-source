package org.rs2server.rs2.model.npc;

import org.rs2server.io.FileUtilities;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.boundary.Boundary;
import org.rs2server.rs2.model.map.Directions.NormalDirection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class NPCSpawnLoader {


	private static final Logger logger = Logger.getLogger(NPCSpawnLoader.class.getName());
	private static final Map<String, Integer> DIRECTIONS = new HashMap<String, Integer>();

	static {
		for (NormalDirection dir : NormalDirection.values())
			DIRECTIONS.put(dir.name(), dir.npcIntValue());
	}

	public static void init() {
		logger.info("Loading default npc spawns...");
		int size = 0;
		boolean ignore = false;
		try {
			for (String string : FileUtilities.readFile("data/npcspawns.txt")) {
				if (string.startsWith("//") || string.equals("")) {
					continue;
				}
				if (string.contains("/*")) {
					ignore = true;
					continue;
				}
				if (ignore) {
					if (string.contains("*/")) {
						ignore = false;
					}
					continue;
				}
				String[] spawn = string.split(" ");
				int id = Integer.parseInt(spawn[0]), x = Integer.parseInt(spawn[1]), y = Integer.parseInt(spawn[2]), z = Integer.parseInt(spawn[3]);
				String dirS = spawn[4];
				int dir = 0;
				if (DIRECTIONS.containsKey(dirS))
					dir = DIRECTIONS.get(dirS);
				else
					dir = Integer.parseInt(dirS);
				boolean doesWalk = Boolean.parseBoolean(spawn[5]);
				//String homeArea = null;
				Boundary boundary = null;
				if (spawn.length > 6) {
				//	homeArea = spawn[6];
				//	boundary = BoundaryManager.boundaryForName(homeArea).get(0); //only ones
				}
				Location spawnLoc = Location.create(x, y, z);
				Location minLoc = null, maxLoc = null;
				if (doesWalk && boundary == null) {
					minLoc = Location.create(x - 3, y - 3, z);
					maxLoc = Location.create(x + 3, y + 3, z);
				} else if (boundary != null){
					minLoc = boundary.getBottomLeft();
					maxLoc = boundary.getTopRight();
				}
				//NPC npc = new NPC(NPCDefinition.forId(id), id, spawnLoc, minLoc, maxLoc, dir);
				NPC npc = new NPC(id, spawnLoc, minLoc, maxLoc, dir);
				npc.setHomeArea(boundary);
				World.getWorld().register(npc);
				size++;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Loaded " + size + " default npc spawns.");
	}


}
