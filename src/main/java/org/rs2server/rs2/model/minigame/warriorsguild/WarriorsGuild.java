package org.rs2server.rs2.model.minigame.warriorsguild;

import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.DialogueManager;
import org.rs2server.rs2.model.GroundItem;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.npc.MetalArmour;
import org.rs2server.rs2.model.npc.NPCDefinition;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.Agility;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.ActionSender.DialogueType;
import org.rs2server.rs2.tickable.Tickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WarriorsGuild {

	/**
	 * The java.util.Random instance used for warious things.
	 */
	private static final Random r = new Random();

	/**
	 * A list of all the players inside the Cyclops arena.
	 */
	public static final List<Player> IN_GAME = new ArrayList<Player>();

	/**
	 * The tokens we're rewarded through all games in Warriors Guild.
	 */
	public static final int TOKENS = 8851;

	/**
	 * Set of all the Armour items used for the Animation Room. (Bronze - Rune)
	 * {helm, chest, legs}
	 */
	private static final int[][] ARMOUR_SET = {
		{1155, 1117, 1075}, //Bronze
		{1153, 1115, 1067}, //Iron
		{1157, 1119, 1069}, //Steel
		{1165, 1125, 1077}, //Black
		{1159, 1121, 1071}, //Mithril
		{1161, 1123, 1073}, //Adamant
		{1163, 1127, 1079}, //Rune
	};

	/**
	 * Set of all the animated Armour, with indexes corresponding with the
	 * indexes from the 2-d array above.
	 */
	private static final int[] ANIMATED_ARMOURS = {
		2450, // Animated Bronze Armour
		2451, // Animated Iron Armour
		2452, // Animated Steel Armour
		2453, // Animated Black Armour
		2454, // Animated Mithril Armour
		2455, // Animated Adamant Armour
		2456, // Animated Rune Armour
	};

	/**
	 * The locations of the two Animator objects, to prevent client hacks. 
	 * (Simply editing the file ID)
	 */
	private static final Location ANIMATOR_1 = Location.create(2851,3536,0);
	private static final Location ANIMATOR_2 = Location.create(2857,3536,0);

	/**
	 * Locations to stand at before placing the Armour's on the animator..
	 */
	private static final Location ANIMATOR_1_STAND = Location.create(2851,3537,0);// TODO Fix walking
	private static final Location ANIMATOR_2_STAND = Location.create(2857,3537,0);

	private static final Animation BONE_BURYING_ANIMATION = Animation.create(827);

	/**
	 * Checks if a specific NPC id is an Animated Armour.
	 * @return <code>true</code> if, <code>false</code> if not.
	 */
	public static boolean isMetalArmour(int id) {
		for(int armour : ANIMATED_ARMOURS) {
			if(armour == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Constructs the Warriors guild MiniGame for a specific player.
	 * @param player The player to construct the Warriors Guild MiniGame for.
	 */
	public WarriorsGuild(Player player) {
		this.player = player;
	}

	/**
	 * Called when our animated Armour dies.
	 * This will drop the items the Armour is build of, as well as the tokens needed.
	 */
	public void dropArmour() {
		assert currentArmour != null; //Makes sure our current Armour isen't null (Really shouldn't be)
		boolean destroyed = false;
		for(int index = 0; index < ANIMATED_ARMOURS.length; index++) {
			if(ANIMATED_ARMOURS[index] == currentArmour.getDefinition().getId()) {
				for(final int armour : ARMOUR_SET[index]) {
					/*
					 * Slight chance, based on how good the Armour is,
					 * for the Armour to be destroyed..
					 */
					if(r.nextInt((index + 1) * 30) == 0 && !destroyed) {
						player.getActionSender().sendMessage("Unfortuantly your " + CacheItemDefinition.get(armour).getName().toLowerCase() + " were destroyed."); //FIXME real message pls
						destroyed = true;
					} else {
						//GroundItemController.createGroundItem(new Item(armour), player, currentArmour.getLocation());
					}					
				}
				//GroundItemController.createGroundItem(new Item(TOKENS, (index + 1) * 5) /*Lol hacks.*/, player, currentArmour.getLocation());
			}
		}
		currentArmour = null;
	}

	private Item defenderDrop = null;

	/**
	 * Called everytime this player is killing a cyclop.
	 * This will randomly drop the next RuneDefender for us.
	 */
	public void killedCyclop(Location pos) {
		/*Make sure our player is actually in game..*/
		if(IN_GAME.contains(player)) {
			if(r.nextInt(21) == 0 && defenderDrop != null) {
				//World.getWorld().register(new GroundItem(player.getName(), defenderDrop, pos), player);
				player.getInventory().add(defenderDrop);
				//player.getActionSender().sendDialogue("", DialogueType.MESSAGE, -1, null, 
						//"You've received a defender, After picking it up leave and come back in to<br>work towards the next one!");
				player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, defenderDrop.getId(), null, 
						"You've received a defender. Exit the room and re-enter to try and attain more defenders!");//6603 20251
			}
		}
	}

	/**
	 * Called once this player runs out of tokens..
	 */
	public void outOfTokens() {
		DialogueManager.openDialogue(player, 32);
		World.getWorld().submit(new Tickable(41) {

			@Override
			public void execute() {
				if(IN_GAME.contains(player)) {
					DialogueManager.openDialogue(player, 34);
					IN_GAME.remove(player);
					player.setTeleportTarget(Location.create(2844, 3539, 2));
				}
				this.stop();
			}

		});

	}

	private boolean readyToEnter = false;

	/**
	 * Called every time a player got its approval of entering the door.
	 */
	public void dialogueFinished() {
		player.getActionSender().removeInterface();
		readyToEnter = true;
	}

	/**
	 * Gets the next defender drop, based on the players
	 * currently worn defender.
	 * @return The next defender to drop.
	 */
	private Item getNextDefender(Item currentDefender) {
		if(currentDefender == null) {
			return new Item(8844);
		} else if(currentDefender.getId() == 12954) {
			return currentDefender;
		} else {
			return currentDefender.getId() == 8850 ? new Item(12954) : new Item(currentDefender.getId() + 1);
		}
	}

	private static final Location ENTER_LOC = Location.create(2847, 3540, 2);
	public boolean handleDoorClick(Location loc) {
		//final DialogueLoader kamfreena = DialogueLoader.forId(4289);
		//System.out.println(kamfreena);
		/*
		 * Game Door entrance thing ;)
		 */
		if(loc.equals(GAME_DOOR_1) || loc.equals(GAME_DOOR_2)) {
			if(IN_GAME.contains(player)) {
				player.setTeleportTarget(ENTER_LOC.transform(-1, 0, 0));
				IN_GAME.remove(player);
			} else {
				if(!readyToEnter) {
					//Make sure we have at least 100 tokens..
					if(!player.getInventory().hasItem(new Item(TOKENS, 100)) && !Constants.hasAttackCape(player)) {
						player.getActionSender().sendMessage("You don't have enough tokens to enter the room.");
					} else {
						player.setTeleportTarget(ENTER_LOC);
						IN_GAME.add(player);
						readyToEnter = false;
						defenderDrop = getNextDefender(getCurrentDefender());
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the players currently worm Defender.
	 * @return null if nothing found, else the players highest worn Defender.
	 */
	private Item getCurrentDefender() {
		Item defender = null; //The best defender so far.
		Item shield = player.getEquipment().get(Equipment.SLOT_SHIELD);
		if(shield != null && shield.getDefinition2().getName().contains("defender")) {
			defender = shield;
		}
		Item[] inv = player.getInventory().toArray();

		for(final Item item : inv) {
			if(item != null) {
				if(item.getDefinition2().getName().contains("defender")) {
					//The one with the highest ID is the best one. xD
					if(defender == null || defender.getId() < item.getId()) {
						defender = item;
					}
				}
			}
		}
		return defender;
	}

	public static final Location GAME_DOOR_1 = Location.create(2847,3540,2);
	private static final Location GAME_DOOR_2 = Location.create(2847,3541,2);

	/**
	 * Handles any item on object actions to do with Warriors Guild.
	 * @param item The item used on an object.
	 * @param objectId The object id.
	 * @param loc The location of the object.
	 * @return <code>true</code> if there was an action to handle, <code>false</code> if not.
	 */
	public boolean handleItemOnObject(final Item item, int objectId, final Location loc) {
		/*
		 * Magical Animator
		 */
		if(objectId == 23955 && (loc.equals(ANIMATOR_1) || loc.equals(ANIMATOR_2))) {
			/*
			 * The client will automatically walk to a specific point, lets wait for it. (Normal within distance thing doesn't work..)
			 */
			player.getActionQueue().clearAllActions();
			final Location walkTo = loc.equals(ANIMATOR_1) ? ANIMATOR_1_STAND : ANIMATOR_2_STAND;
			player.getActionQueue().addAction(new Action(player, 0) {

				@Override
				public void execute() {
					if(player.getLocation().equals(walkTo)) {
						/*
						 * We loop through all Armour types..
						 */
						for(int index = 0; index < ARMOUR_SET.length; index++) {
							/*
							 * We loop through the ID's for all the Armour types.
							 */
							for(int armour : ARMOUR_SET[index]) {
								/*
								 * We check if the Armour set contains the ID of the item used on an object..
								 */
								if(armour == item.getId()) {
									/*
									 * We make sure our inventory contains all the needed items..
									 */
									boolean stop = false;
									for(int armour1 : ARMOUR_SET[index]) {
										if(!player.getInventory().contains(armour1)) {
											String name = CacheItemDefinition.get(armour1).getName().toLowerCase();
											player.getActionSender().sendMessage("You're missing a" + (name.startsWith("A") ? "n " : " ") + name + " in order to summon this Armour."); //FIXME
											stop = true;
										}
									}
									if(!stop) {
										/*
										 * We make sure this is the only Armour we have spawned..
										 */
										if(currentArmour == null) {
											/*
											 * Remove the Armour set from our inventory.
											 */
											for(int armour1 : ARMOUR_SET[index]) {
												player.getInventory().remove(new Item(armour1));
											}

											/*
											 * We bend down, send a message.. //Should be an interface. :S
											 */
											player.playAnimation(BONE_BURYING_ANIMATION);
											player.setAttribute("busy", true);
											//											player.getActionSender().sendString("You place your armour on the platform where it", 216, 0);
											//											player.getActionSender().sendString("disappears....", 216, 1);
											//player.getActionSender().sendChatboxInterface(216);
											final int fIndex = index;
											World.getWorld().submit(new Event(2400) {

												@Override
												public void execute() {
													//													player.getActionSender().sendString(216, 0, "The animator hums, something appears to be working.");
													//													player.getActionSender().sendString(216, 1, "You stand back...");
													World.getWorld().submit(new Event(1200) {

														@Override
														public void execute() {
															/*
															 * We walk a few steps back..
															 */
														//	player.getWalkingQueue().reset();
															//player.getWalkingQueue().setRunningQueue(false);
															int[] forceMovementVars = {0, 0, 0, 3, 0, 20, 60, 1, 0};
															Agility.forceMovement(player, player.getWalkAnimation(), forceMovementVars, 3, false);
															//Agility.forceWalkingQueue(player, player.getWalkAnimation(), player.getLocation().getX(), player.getLocation().getY() + 3, 0, 3, false);
															//player.getWalkingQueue().addStep(player.getLocation().getX(), player.getLocation().getY() + 3);
															//player.getWalkingQueue().finish();


															World.getWorld().submit(new Event(2400) {

																@Override
																public void execute() {
																	player.removeAttribute("busy");
																	player.getActionSender().removeInterface();
																	/*
																	 * Set our currently spawned Armour to the Animated Armour matching our armour set.
																	 */
																	currentArmour = new MetalArmour(NPCDefinition.forId(ANIMATED_ARMOURS[fIndex]), loc, player);

																	/*
																	 * We face the armour..
																	 */
																	player.face(loc);
																	/*
																	 * And add it in the actual world as well.
																	 */
																	World.getWorld().register(currentArmour);

																	currentArmour.getCombatState().startAttacking(player, false);

																	/*
																	 * Place a hint icon above the NPC..
																	 */
																	//player.getActionSender().sendNPCHints(currentArmour.getIndex());
																	this.stop();
																}
															});
															this.stop();

														}

													});
													this.stop();
												}

											});
										} else {
											player.getActionSender().sendMessage("You've already spawned an Armour."); //FIXME
										}
									}
								}
							}
						}
						this.stop();
					}
					this.setTickDelay(500);
				}

				@Override
				public CancelPolicy getCancelPolicy() {
					return CancelPolicy.ALWAYS;
				}

				@Override
				public StackPolicy getStackPolicy() {
					return StackPolicy.NEVER;
				}

				@Override
				public AnimationPolicy getAnimationPolicy() {
					return AnimationPolicy.RESET_ALL;
				}

			});
			/*
			 * We handled what stuff related to the Animator, and every thing is cool. 
			 * (Missing the "nothing interesting happens" though, but I couldn't think of other
			 * ways. *Shitty ActionSystem*
			 */
			return true;
		}
		return false;
	}

	/**
	 * Gets the players Capapult defence style..
	 */
	public int getDefenceStyle() {
		return defenceStyle;
	}

	public void setDefenceStyle(int button) {
		//		int oldButton = this.defenceStyle + 9;
		//		int style = button - 9;
		//		player.getActionSender().sendString("<col=FF8040>" + WGCatapultEvent.STYLES[this.defenceStyle], 411, oldButton);
		//		player.getActionSender().sendString("<col=000000>" + WGCatapultEvent.STYLES[style], 411, button);
		//		this.defenceStyle = style;
	}

	private int defenceStyle = 0; //Magic defence by default..

	public void increaseCatapultTokens() {
		player.getInventory().add(new Item(TOKENS));
		//catapultTokens++;		
	}

	private int catapultTokens = 0; //Should this be saved?

	/**
	 * Gets the current animated Armour spawned.
	 * @return The currently spawned animation Armour.
	 */
	public MetalArmour getCurrentArmour() {
		return currentArmour;
	}

	/**
	 * The currently summoned Metal Armour.
	 */
	private MetalArmour currentArmour = null;

	/**
	 * The player we're going to handle Warriors Guild for.
	 */
	private final Player player;

	public void setCurrentArmour(MetalArmour armour) {
		this.currentArmour = armour;
	}



}

