package org.rs2server.tools;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class DropChecker implements Runnable{
	
	static Thread thread = new Thread(new DropChecker());
	private static double rate;
	int timesRan = 0;
	int timesKilled;
	private ArrayList<Integer> list = new ArrayList<>();
	static int kills = 0;
	
	public static void main(String[] args) {
//		System.out.println("Starting");
//		Scanner scanner = new Scanner(System.in);
//		System.out.println("Drop Rate ?");
//		rate = scanner.nextDouble();
//		if (rate != 0) {
//			thread.start();
//		}
	}

	@Override
	public void run() {
//		while (true) {
//			final Random random = new Random();
//			final double roll = (random.nextDouble() * 1);
//			double multiplier = 1.0;
//			if (rate * multiplier >= roll && timesRan <= 3000) {
//				// System.out.println("Drop in " + kills + " Kills.");
//				list.add(kills);
//				kills = 0;
//				timesRan++;
//				// thread.endGame();
//			} else if (timesRan == 3000) {
//				for (int i = 0; i < list.size(); i++) {
//					timesKilled += list.get(i);
//				}
//				System.out.println("Average Kills: " + timesKilled
//						/ list.size());
//				thread.endGame();
//			}
//			kills++;
//		}
	}

}
