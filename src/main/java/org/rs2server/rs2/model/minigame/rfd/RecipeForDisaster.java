package org.rs2server.rs2.model.minigame.rfd;

import com.google.common.collect.Iterables;
import org.rs2server.rs2.model.Entity;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender.DialogueType;
import org.rs2server.rs2.tickable.Tickable;

import java.util.Iterator;

public class RecipeForDisaster {
	
	private Player player;
	private RFDWave wave;
	private int startTime = -1;
	private boolean startedFightWave;
	private boolean started;
	
	public RecipeForDisaster(Player player) {
		this.player = player;
	}
	
	public RFDWave getWave() {
		return wave;
	}
	
	public void setWave(RFDWave wave) {
		this.wave = wave;
	}
	
	public void start() {
		player.setTeleportTarget(Location.create(1899, 5365, 2));
		player.setMultiplayerDisabled(true);
		player.getCombatState().resetPrayers();
		World.getWorld().submit(new Tickable(1) {
			public void execute() {
				stop();
				//DialogueManager.openDialogue(player, 22);
				if (player.getSettings().getRFDState() == 14) {
					player.getActionSender().sendMessage("You've already completed this minigame.");
					return;
				}
				started = true;
				if (wave == null) {
					wave = new RFDWave();
					wave.set(player.getSettings().getBestRFDState());
				}
				//wave.set(player.getSettings().getRFDState());
			}
		});
	}
	
	public void tick() {
		if (!started) {
			return;
		}
		if (!startedFightWave) {
			startedFightWave = true;
			startTime = 10;
		}
		if (startTime > 0) {
			startTime--;
		} else if (startTime == 0) {
			startTime = -1;
			if (wave == null) {
				wave = new RFDWave();
				wave.set(1);
			}
			//player.getActionSender().sendMessage("Now starting wave " + wave.getStage() + ".");

			int[] spawns = wave.spawns();

			for (int spawn : spawns) {
				Location l = Location.create(1899, 5360, 2);
				NPC npc = new NPC(spawn, l, Location.create(3572, 3293), Location.create(3578, 3301), 0);
				player.getInstancedNPCs().add(npc);
				npc.instancedPlayer = player;
				World.getWorld().register(npc);
				npc.getCombatState().startAttacking(player, player.isAutoRetaliating());
			}
		} else if (startTime == -1) {
			if (player.isMultiplayerDisabled() && wave != null) {
				Iterator<NPC> it = player.getInstancedNPCs().iterator();
				while (it.hasNext()) {
					NPC n = it.next();
					if (n.getAttributes().containsKey("died")) {
						it.remove();
					} else {
						if (n.getInteractingEntity() != player)
							n.getCombatState().startAttacking(player, player.isAutoRetaliating());
					}
				}
				if (player.getInstancedNPCs().isEmpty()) {
					if (wave.getStage() == 5) {
						player.getActionSender().sendDialogue("", DialogueType.MESSAGE, -1, null, 
	            				"You have completed <col=ff0000>Recipe for Disaster</col>! <br>You now have"
	            				+ "Full access to the Culinaromancer's Chest.");
						player.getInterfaceState().setNextDialogueId(0, -1);
						player.setTeleportTarget(Entity.HOME);
						player.getSettings().setRFDState(10);
                        player.getSettings().setBestRFDState(4);
						player.setMultiplayerDisabled(false);
						started = false;
						player.setAttribute("defeated_rfd", true);
					} else {
						//player.getActionSender().sendMessage("Finished wave " + wave.getStage() + "; starting the next wave soon.");
						wave.set(wave.getStage() + 1);
                        int currentWave = wave.getStage() - 1;
                        if (currentWave > player.getSettings().getBestRFDState()) {
                            player.getSettings().setBestRFDState(currentWave);
                        }
						startTime = 10;
					}
				}
			}
		}
	}

	public void stop() {
		Iterables.consumingIterable(player.getInstancedNPCs()).forEach(World.getWorld()::unregister);
		/*for (int i = 0; i < player.getInstancedNPCs().size(); i++) {
			if (player.getInstancedNPCs().get(i) == null) {
				continue;
			}
			World.getWorld().unregister(player.getInstancedNPCs().get(i));
		}*/
		startTime = -1;
		//player.getSettings().setRFDState(wave.getStage() - 1);
        int currentWave = wave.getStage() - 1;
        if (currentWave > player.getSettings().getBestRFDState()) {
            player.getSettings().setBestRFDState(currentWave);
        }
		player.setMultiplayerDisabled(false);
		player.getInstancedNPCs().clear();
		wave = null;
		started = false;
		//if (!player.getAttributes().containsKey("defeated_rfd"))
			//player.getActionSender().sendMessage("Your session has ended.");
		player.setTeleportTarget(Entity.HOME);
	}
	
	public void appendDeath() {
		Iterables.consumingIterable(player.getInstancedNPCs()).forEach(World.getWorld()::unregister);
	/*	for (int i = 0; i < player.getInstancedNPCs().size(); i++) {
			if (player.getInstancedNPCs().get(i) == null) {
				continue;
			}
			World.getWorld().unregister(player.getInstancedNPCs().get(i));
		}*/
		startTime = -1;
		//player.getSettings().setRFDState(wave.getStage() - 1);
        int currentWave = wave.getStage() - 1;
        if (currentWave > player.getSettings().getBestRFDState()) {
            player.getSettings().setBestRFDState(currentWave);
        }
		player.setMultiplayerDisabled(false);
		player.getInstancedNPCs().clear();
		wave = null;
		//if (!player.getAttributes().containsKey("defeated_rfd"))
			//player.getActionSender().sendMessage("Your session has ended.");
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public void setStarted(boolean b) {
		this.started = b;
	}

	public void setStartedWave(boolean b) {
		this.startedFightWave = b;
	}

}
