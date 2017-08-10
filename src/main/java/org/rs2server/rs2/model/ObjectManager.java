package org.rs2server.rs2.model;

import org.rs2server.rs2.model.map.RegionClipping;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.model.region.RegionManager;
import org.rs2server.util.XMLController;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;


/**
 * Manages all of the in-game objects.
 * @author Graham Edgecombe
 *
 */
public class ObjectManager {

	/**
	 * Logger instance.
	 */
	private static final Logger logger = Logger.getLogger(ObjectManager.class.getName());

	/**
	 * The number of definitions loaded.
	 */
	private int definitionCount = 0;

	/**
	 * The count of objects loaded.
	 */
	private int objectCount = 0;

	public void load() throws IOException {
		logger.info("Loaded " + definitionCount + " object definitions.");
		int customObjectCount = 0;
		List<GameObject> customObjects = XMLController.readXML(new File("./data/customObjects.xml"));
		for(GameObject obj : customObjects) {
			if (obj == null) {
				continue;
			}

			GameObject object = new GameObject(obj.getLocation(), obj.getId(), obj.getType(), obj.getDirection(), obj.isLoadedInLandscape());

			World.getWorld().register(object);
			//World.getWorld().register(obj);
			RegionClipping.addClipping(object);
			customObjectCount++;
		}
//		GameObject obj = World.getWorld().getRegionManager().getGameObject(Location.create(2543, 10143), 8967);
//		System.out.println(obj);
//		if (obj != null) {
//			World.getWorld().unregister(obj, true);
//		}
		RegionClipping.removeClipping(2543, 10143, 0, 0);
		RegionClipping.removeClipping(1500, 3433, 0, 0);
		RegionClipping.removeClipping(1503, 3435, 0, 0);
		RegionClipping.removeClipping(1508, 3423, 0, 0);
		RegionClipping.removeClipping(1510, 3422, 0, 0);
		//World.getWorld().unregister(new GameObject(Location.create(2543, 10143), 8967, 10, 0, false), true);
		//World.getWorld().unregister(new GameObject(Location.create(1501, 3435), 22123, 10, 0, false), true);
		//World.getWorld().unregister(new GameObject(Location.create(1501, 3435), 22123, 10, 0, false), true);
		logger.info("Loaded " + customObjectCount+" custom objects.");
	}

}
