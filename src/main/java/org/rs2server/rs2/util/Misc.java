package org.rs2server.rs2.util;

import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Skills;

import java.util.*;

public class Misc {

	public static final String[] NUMBERWORD = {
		"zero", "one", "two", "three", "four", "five",
		"six", "seven", "eight", "nine", "ten", "eleven", "twelve" };

	/**
	 * The {@link Random} instance used for randomizing a value.
	 */
	private static Random r = new Random();

	public static int random(int i) {
		return r.nextInt(i + 1);
	}

	public static byte directionDeltaX[] = new byte[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	
	public static byte directionDeltaY[] = new byte[] { 1, 1, 0, -1, -1, -1, 0, 1 };
	
	public static int random(int min, int max) {
		final int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random(n));
	}

	public static int[] convertIntegers(List<Integer> integers)
	{
		int[] ret = new int[integers.size()];
		Iterator<Integer> iterator = integers.iterator();
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = iterator.next();
		}
		return ret;
	}

	public static int getModIconForPerm(PermissionService.PlayerPermissions perm) {
		switch (perm) {
			case ADMINISTRATOR:
				return 1;
			case MODERATOR:
				return 0;
			case HELPER:
				return 46;
//			case PVP:
//				return 9;
			case IRON_MAN:
				return 2;
			case ULTIMATE_IRON_MAN:
				return 3;
			case HARDCORE_IRON_MAN:
				return 10;
		}
		return -1;
	}

	public static Optional<Integer> forSkillName(String name) {
		List<String> list = Arrays.asList(Skills.SKILL_NAME);
		if (list.contains(name)) {
			return Optional.of(list.indexOf(name));
		}
		return Optional.empty();
	}


	
	public static int getDistance(Location loc1, Location loc2) {
		int deltaX = loc2.getX() - loc1.getX();
		int deltaY = loc2.getY() - loc1.getY();
		return ((int) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)));
	}

	public static String formatCurrency(int amount) {
		String ShopAdd = "";
		if (amount >= 1000 && amount < 1000000) {
			ShopAdd = (amount / 1000) + "K";
		} else if (amount >= 1000000) {
			ShopAdd = (amount / 1000000) + " million";
		} else {
			ShopAdd = amount + "";
		}
		return ShopAdd;
	}

	public static String commaCurrency(int amount) {
		String ShopAdd = "";
		if (amount >= 1000 && amount < 1000000) {
			ShopAdd = (amount / 1000) + "K";
		} else if (amount >= 1000000) {
			ShopAdd = (amount / 1000000) + " million";
		} else {
			ShopAdd = amount + "";
		}
		return ShopAdd;
	}
	
	public static String upperFirst(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static String withPrefix(String string) {
		return ((string.startsWith("a") || string.startsWith("e") || string.startsWith("i") || string.startsWith("o") || string.startsWith("u")) ? "an " : "a ") + string;
	}
}