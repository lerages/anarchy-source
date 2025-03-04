package org.rs2server.rs2.model.player;

/*
 * IMPORTANT MESSAGE - READ BEFORE ADDING NEW METHODS/FIELDS TO THIS CLASS
 *
 * Before you create a field (variable) or method in this class, which is specific to a particular
 * skill, quest, minigame, etc, THINK! There is almost always a better way (e.g. attribute system,
 * helper methods in other classes, etc.)
 *
 * We don't want this to turn into another client.java! If you need advice on alternative methods,
 * feel free to discuss it with me.
 *
 * Graham
 */

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.joda.time.DateTime;
import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.data.Persistable;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.ActionManager;
import org.rs2server.rs2.action.impl.ConsumeItemAction;
import org.rs2server.rs2.content.HerbSack;
import org.rs2server.rs2.content.Jewellery;
import org.rs2server.rs2.content.TimedPunishment;
import org.rs2server.rs2.content.TimedPunishment.PunishmentType;
import org.rs2server.rs2.content.ZulAreth;
import org.rs2server.rs2.content.api.GamePlayerKillEvent;
import org.rs2server.rs2.content.dialogue.DialogueChain;
import org.rs2server.rs2.content.diary.AchievementDiary;
import org.rs2server.rs2.content.managers.AchievementDiaryManager;
import org.rs2server.rs2.content.misc.AbyssalCreatures;
import org.rs2server.rs2.domain.model.player.PlayerEntity;
import org.rs2server.rs2.domain.service.api.*;
import org.rs2server.rs2.domain.service.api.content.ItemService;
import org.rs2server.rs2.domain.service.api.content.RunePouchService;
import org.rs2server.rs2.domain.service.api.content.gamble.FlowerGame;
import org.rs2server.rs2.domain.service.api.content.trade.Transaction;
import org.rs2server.rs2.domain.service.impl.RunePouchServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.KrakenServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.LootingBagServiceImpl;
import org.rs2server.rs2.domain.service.impl.content.duel.Duel;
import org.rs2server.rs2.domain.service.impl.content.gamble.DiceGameTransaction;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.Skills.SkillCape;
import org.rs2server.rs2.model.UpdateFlags.UpdateFlag;
import org.rs2server.rs2.model.bit.BitConfig;
import org.rs2server.rs2.model.bit.component.Access;
import org.rs2server.rs2.model.bit.component.NumberRange;
import org.rs2server.rs2.model.boundary.BoundaryManager;
import org.rs2server.rs2.model.cm.Content;
import org.rs2server.rs2.model.cm.ContentManager;
import org.rs2server.rs2.model.combat.CombatAction;
import org.rs2server.rs2.model.combat.CombatFormulae;
import org.rs2server.rs2.model.combat.CombatState.CombatStyle;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.combat.impl.MeleeCombatAction;
import org.rs2server.rs2.model.container.*;
import org.rs2server.rs2.model.container.Container.Type;
import org.rs2server.rs2.model.minigame.Minigame;
import org.rs2server.rs2.model.minigame.barrows.Barrows.BarrowsBrother;
import org.rs2server.rs2.model.minigame.bh.BountyHunterCrater;
import org.rs2server.rs2.model.minigame.bh.BountyHunterNode;
import org.rs2server.rs2.model.minigame.fightcave.FightCave;
import org.rs2server.rs2.model.minigame.magearena.MageArena;
import org.rs2server.rs2.model.minigame.rfd.RecipeForDisaster;
import org.rs2server.rs2.model.minigame.warriorsguild.WarriorsGuild;
import org.rs2server.rs2.model.npc.MetalArmour;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.npc.impl.kraken.Kraken;
import org.rs2server.rs2.model.npc.impl.kraken.Whirlpool;
import org.rs2server.rs2.model.player.pc.PestControlInstance;
import org.rs2server.rs2.model.quests.Quest;
import org.rs2server.rs2.model.quests.impl.CooksAssistant;
import org.rs2server.rs2.model.quests.impl.CooksAssistantState;
import org.rs2server.rs2.model.quests.impl.DTStates;
import org.rs2server.rs2.model.quests.impl.DesertTreasure;
import org.rs2server.rs2.model.quests.impl.LunarDiplomacy;
import org.rs2server.rs2.model.quests.impl.LunarStates;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.model.skills.Agility;
import org.rs2server.rs2.model.skills.slayer.Slayer;
import org.rs2server.rs2.model.skills.slayer.SlayerMasterWidget;
import org.rs2server.rs2.model.skills.slayer.SlayerTask;
import org.rs2server.rs2.model.skills.slayer.SlayerTask.Master;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.ISAACCipher;
import org.rs2server.rs2.net.Packet;
import org.rs2server.rs2.net.PacketManager;
import org.rs2server.rs2.tickable.StoppingTick;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.util.IoBufferUtils;
import org.rs2server.rs2.util.NameUtils;
import org.rs2server.rs2.util.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * Represents a player character.
 *
 * @author Graham Edgecombe
 */
public class Player extends Mob implements Persistable 
{

	private static final Logger logger = LoggerFactory.getLogger(Player.class);
	private final ItemService itemService;
	private final HookService hookService;
	private int[] temporaryPin;
	private boolean enteredPinOnce;
	private Pet pet;
	private int regionId;
	private PestControlInstance pestControlInstance;
	private int monkeyTime;
	private ConsumeItemAction.Monkey monkey;
	private Player bountyTarget;
	private Bounty bounty;
	private int selectedItem = -1;
	private boolean ultimateIronMan;
	private boolean hardcoreIronMan;
	private Transaction transaction;
	private DiceGameTransaction diceGameTransaction;
	private AchievementDiaryManager achievementDiaryManager;
	private AchievementDiary achievementDiary;
	
    public int tele = 0;
	public boolean safeLogout = false;
	public boolean cantele = false;

	public Player addInstancedNpc(NPC n) {
		n.instancedPlayer = this;
		instancedNPCs.add(n);
		return this;
	}

	public Player disableMultiplayer() {
		setMultiplayerDisabled(false);
		return this;
	}

	public Player enableMultiplayer() {
		setMultiplayerDisabled(true);
		return this;
	}

	public void setTemporaryPin(int[] temporaryPin) {
		this.temporaryPin = temporaryPin;
	}

	public int[] getTemporaryPin() {
		return temporaryPin;
	}

	public void setEnteredPinOnce(boolean enteredPinOnce) {
		this.enteredPinOnce = enteredPinOnce;
	}

	public boolean isEnteredPinOnce() {
		return enteredPinOnce;
	}

	/*
	 * Attributes specific to our session.
	 */

	/**
	 * The ISAAC cipher for incoming data.
	 */
	private final ISAACCipher inCipher;

	/**
	 * The ISAAC cipher for outgoing data.
	 */
	private final ISAACCipher outCipher;

	/**
	 * The action sender.
	 */
	private final ActionSender actionSender = new ActionSender(this);

	/**
	 * A queue of pending chat messages.
	 */
	private final Queue<ChatMessage> chatMessages = new LinkedList<>();

	/**
	 * The current chat message.
	 */
	private ChatMessage currentChatMessage;

	/**
	 * Active flag: if the player is not active certain changes (e.g. items)
	 * should not send packets as that indicates the player is still loading.
	 */
	private boolean active = false;

	/**
	 * The interface state.
	 */
	private final InterfaceState interfaceState = new InterfaceState(this);

	/**
	 * A queue of packets that are pending.
	 */
	private final Queue<Packet> pendingPackets = new LinkedList<>();

	/**
	 * The request manager which manages trading and duelling requests.
	 */
	private final RequestManager requestManager = new RequestManager(this);

	/**
	 * The <code>IoSession</code>.
	 */
	private final IoSession session;

	/**
	 * The player's skill levels.
	 */
	private final Skills skills = new Skills(this);

	/**
	 * The stand animation.
	 */
	private Animation standAnimation = Animation.create(808);

	/**
	 * The run animation.
	 */
	private Animation runAnimation = Animation.create(824);

	/**
	 * The walk animation.
	 */
	private Animation walkAnimation = Animation.create(819);

	/**
	 * The stand-turn animation.
	 */
	private Animation standTurnAnimation = Animation.create(823);

	/**
	 * The turn 90 clockwise animation.
	 */
	private Animation turn90ClockwiseAnimation = Animation.create(821);

	/**
	 * The turn 90 counter clockwise animation.
	 */
	private Animation turn90CounterClockwiseAnimation = Animation.create(822);

	/**
	 * The turn 180 animation.
	 */
	private Animation turn180Animation = Animation.create(820);

	/**
	 * The amount of time the player has left as a member.
	 */
	private long membershipExpiryDate = 0;

	/**
	 * The last date that the players recovery questions were set.
	 */
	private String recoveryQuestionsLastSet = "never";
	
	private Perk perks[] = {
			new Perk("Lazy Bones", "Dragon and Dagannoth bone drops are noted", 7.50, false),
			new Perk("No Holds Barred", "Metal dragons bar drops are noted.", 5, false),
			new Perk("Gold Digger", "Coin drops are sent directly to your bank", 5, false), 
			new Perk("Superior Orders", "Superior slayer monsters are 4x more likely to appear.", 7.50, false),
			new Perk("Slayer Betrayer", "Cancelling slayer tasks costs 10 slayer points", 7.50, false),
			new Perk("Teacher's Pet", "Skilling pets rates are twice as common", 5, false),
			new Perk("Good Graces", "Increased chance at Marks of Grace from rooftop courses", 3, false),
			new Perk("Five Finger Dicount", "10% chance of receiving double loot while thieving", 5, false),
			new Perk("More Ore", "10% chance of receiving double ore while mining", 5, false),
			new Perk("Chop Shop", "10% chance of receiving double logs while woodcutting", 5, false),
			new Perk("Fish Wish", "10% chance of receiving double fish while fishing", 5, false),
			new Perk("Rune Doubloon", "10% chance of receiving double runes while runecrafting", 5, false),
			new Perk("Nest Obsessed", "Bird's Nests are twice as common and are sent straight to your bank", 5, false)
			};

	/**
	 * The last logged in time.
	 */
	private long lastLoggedIn = 0;

	/**
	 * The last connected host.
	 */
	private String lastLoggedInFrom = "";

	/**
	 * The autocasting spell.
	 */
	private MagicCombatAction.Spell autocastSpell;

	/**
	 * The imitated npc.
	 */
	private int pnpc = -1;

	/*
	 * Minigame details
	 */

	/**
	 * The minigame this player is participating in.
	 */
	private Minigame minigame = null;

	/**
	 * The fight pits winner flag.
	 */
	private boolean fightPitsWinner = false;

	private final ExperienceDropdown dropdown;
	/*
	 * Core login details.
	 */

	/**
	 * The name.
	 */
	private String name;

	/**
	 * The players previous name.
	 */
	private String previousName;

	/**
	 * The name expressed as a long.
	 */
	private long nameLong;

	/**
	 * The password.
	 */
	private String password;

	/**
	 * The members flag.
	 */
	private boolean members = true;

	private int clickPriority = 0;

	/**
	 * Has this player received the starting runes from the Magic Combat Tutor.
	 */
	private boolean receivedStarterRunes = true;

	/**
	 * The debug flag.
	 */
	private boolean debugMode = false;

	private Duel duel;

	/*
	 * Attributes.
	 */

	/**
	 * The player's appearance information.
	 */
	private final Appearance appearance = new Appearance();

	/**
	 * The player's inventory.
	 */
	private final Container inventory = new Container(Container.Type.STANDARD,
			Inventory.SIZE);

	/**
	 * The player's bank.
	 */
	private final Container bank = new Container(Container.Type.ALWAYS_STACK,
			Bank.SIZE);

	/**
	 * The player's bank.
	 */
	private final Container priceChecker = new Container(Container.Type.STANDARD,
			28);

	/**
	 * The player's trade.
	 */
	private final Container trade = new Container(Container.Type.STANDARD,
			Trade.SIZE);
	
	private final Container tourn = new Container(Container.Type.STANDARD,
			293);

	/**
	 * The player's settings.
	 */
	private final Settings settings = new Settings();

	/**
	 * The cached update block.
	 */
	private Packet cachedUpdateBlock;

	private final int userId;

	private final PlayerDetails details;

	private ZulAreth zulAreth = new ZulAreth(this);
	
	private BountyHunterNode bountyHunter;
	private FightCave fightCave = new FightCave(this);

	private final Set<NPC> instancedNPCs = new LinkedHashSet<>();
	private boolean multiplayerDisabled;


	public int pickupPenalty = -1;
	public int bountyDelay = 0;

	public long lastMagicOnItem = 0;

	private Banking banking = new Banking(this);

	private final Queue<Integer> weaponSwitchQueue = new LinkedList<>();
	private boolean queuedSwitching = true;

	private ContentManager contentManager = new ContentManager(this);

	private Map<Class<?>, Quest<?>> quests = new HashMap<>();

//    public int toxicCharges;

	private String color = "";

	private TimedPunishment punish;

	/**
	 * The player's database entity;
	 */
	private PlayerEntity databaseEntity;
	
	/**
	 * Flower Game
	 */
	
	private FlowerGame flowerGame;
	
	/**
	 * Dialogue manager
	 */
	private DialogueManager dialogueManager;

	/**
	 * The dialogue chain the player is currently in.
	 */
	private DialogueChain dialogueChain;

	/**
	 * Lord help me.
	 */
	private boolean isIronMan = false;

	private final PermissionService permissionService;

	private final GroundItemService groundItemService;
	private final RunePouchService pouchService;

	/**
	 * Creates a player based on the details object.
	 *
	 * @param details The details object.
	 */
	public Player(PlayerDetails details) {
		// super();
		this.permissionService = Server.getInjector().getInstance(PermissionService.class);
		this.itemService = Server.getInjector().getInstance(ItemService.class);
		this.groundItemService = Server.getInjector().getInstance(GroundItemService.class);
		this.hookService = Server.getInjector().getInstance(HookService.class);
		this.pouchService = Server.getInjector().getInstance(RunePouchService.class);
		this.session = details.getSession();
		this.inCipher = details.getInCipher();
		this.outCipher = details.getOutCipher();
		this.name = NameUtils.formatName(details.getName());
		this.userId = details.getUserId();
		this.nameLong = NameUtils.nameToLong(this.name);
		this.password = details.getPassword();
		this.details = details;
		this.getUpdateFlags().flag(UpdateFlag.APPEARANCE);
		this.setTeleporting(true);
		if (World.getWorld().privateExists(getName())) {
			if (!World.getWorld().privateIsRegistered(getName())) {
				if (!World.getWorld().deserializePrivate(getName())) {
					World.getWorld().getPrivateChat()
							.put(getName(), new PrivateChat(getName(), ""));
				}
			}
		} else {
			World.getWorld().getPrivateChat()
					.put(getName(), new PrivateChat(getName(), ""));
		}
		if (getPrivateChat() == null || World.getWorld().getPrivateChat().get(name) == null) {
			World.getWorld().getPrivateChat()
					.put(getName(), new PrivateChat(getName(), ""));
		}
		if (World.getWorld().getPrivateChat().containsKey(getName())) {
			World.getWorld().getPrivateChat().get(getName()).setPlayer(this);
		}
		quests.put(DesertTreasure.class, null);
		quests.put(LunarDiplomacy.class, null);
		quests.put(CooksAssistant.class, null);
		this.dropdown = new ExperienceDropdown(this);
		this.punish = new TimedPunishment(this);

	}

	public void sendBitConfig(BitConfig config) {
		actionSender.sendConfig(config.getId(), config.getValue());
	}

	public ExperienceDropdown getDropdown() {
		return dropdown;
	}

	public int getUserId() {
		return userId;
	}

	/**
	 * Gets the request manager.
	 *
	 * @return The request manager.
	 */
	public RequestManager getRequestManager() {
		return requestManager;
	}

	/**
	 * @return the privateChat
	 */
	public PrivateChat getPrivateChat() {
		return World.getWorld().getPrivateChat().get(getName());
	}

	/**
	 * @return the standAnimation
	 */
	public Animation getStandAnimation() {
		return standAnimation;
	}

	/**
	 * @param standAnimation the standAnimation to set
	 */
	public void setStandAnimation(Animation standAnimation) {
		this.standAnimation = standAnimation;
	}

	/**
	 * @return the runAnimation
	 */
	public Animation getRunAnimation() {
		return runAnimation;
	}

	/**
	 * @param runAnimation the runAnimation to set
	 */
	public void setRunAnimation(Animation runAnimation) {
		this.runAnimation = runAnimation;
	}

	/**
	 * @return the walkAnimation
	 */
	public Animation getWalkAnimation() {
		return walkAnimation;
	}

	/**
	 * @param walkAnimation the walkAnimation to set
	 */
	public void setWalkAnimation(Animation walkAnimation) {
		this.walkAnimation = walkAnimation;
	}


	/**
	 * @return the standTurnAnimation
	 */
	public Animation getStandTurnAnimation() {
		return standTurnAnimation;
	}

	/**
	 * @param standTurnAnimation the standTurnAnimation to set
	 */
	public void setStandTurnAnimation(Animation standTurnAnimation) {
		this.standTurnAnimation = standTurnAnimation;
	}

	/**
	 * @return the turn90ClockwiseAnimation
	 */
	public Animation getTurn90ClockwiseAnimation() {
		return turn90ClockwiseAnimation;
	}

	/**
	 * @param turn90ClockwiseAnimation the turn90ClockwiseAnimation to set
	 */
	public void setTurn90ClockwiseAnimation(Animation turn90ClockwiseAnimation) {
		this.turn90ClockwiseAnimation = turn90ClockwiseAnimation;
	}

	/**
	 * @return the turn90CounterClockwiseAnimation
	 */
	public Animation getTurn90CounterClockwiseAnimation() {
		return turn90CounterClockwiseAnimation;
	}

	/**
	 * @param turn90CounterClockwiseAnimation the turn90CounterClockwiseAnimation to set
	 */
	public void setTurn90CounterClockwiseAnimation(
			Animation turn90CounterClockwiseAnimation) {
		this.turn90CounterClockwiseAnimation = turn90CounterClockwiseAnimation;
	}

	/**
	 * @return the turn180Animation
	 */
	public Animation getTurn180Animation() {
		return turn180Animation;
	}

	/**
	 * @param turn180Animation the turn180Animation to set
	 */
	public void setTurn180Animation(Animation turn180Animation) {
		this.turn180Animation = turn180Animation;
	}

	/**
	 * Checks the players containers for untrimmed skillcapes.
	 */
	public void checkForSkillcapes() {
		int has99 = 0;
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			if (getSkills().getLevelForExperience(i) >= 99) {
				has99++;
				getActionSender().sendConfig(313, 511); // Activates skillcape
				// icon.
				if (has99 >= 2) {
					break;
				}
			}
		}
		if (has99 < 2) {
			return;
		}
		for (Item item : getInventory().toArray()) {
			if (item == null) {
				continue;
			}
			SkillCape cape = SkillCape.forUntrimmedId(item);
			if (cape != null && cape.getCapeTrim() != null) {// tells us that
				// this item is
				// an untrimmed
				// cape.
				getInventory().remove(item);
				getInventory().add(
						new Item(cape.getCapeTrim().getId(), item.getCount()));// make
				// sure
				// that
				// we
				// add
				// the
				// same
				// amount
				// of
				// capes
				// we
				// deleted.
			}
		}
		for (Item item : getBank().toArray()) {
			if (item == null) {
				continue;
			}
			SkillCape cape = SkillCape.forUntrimmedId(item);
			if (cape != null) {// tells us that this item is an untrimmed cape.
				getBank().remove(item);
				getBank().add(
						new Item(cape.getCapeTrim().getId(), item.getCount()));// make
				// sure
				// that
				// we
				// add
				// the
				// same
				// amount
				// of
				// capes
				// we
				// deleted.
			}
		}
	}

	/**
	 * Checks an item for a trimmed version of a skillcape.
	 */
	public Item checkForSkillcape(Item item) {
		int has99 = 0;
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			if (getSkills().getLevelForExperience(i) >= 99) {
				has99++;
				getActionSender().sendConfig(313, 511); // Activates skillcape
				// icon.
				if (has99 >= 2) {
					break;
				}
			}
		}
		if (has99 < 2) {
			return item;
		}
		SkillCape cape = SkillCape.forUntrimmedId(item);
		if (cape != null && cape.getCapeTrim() != null) {// tells us that this
			// item is an
			// untrimmed cape.
			return new Item(cape.getCapeTrim().getId(), item.getCount());// make
			// sure
			// that
			// we
			// add
			// the
			// same
			// amount
			// of
			// capes
			// we
			// deleted.
		}
		return item;
	}

	public boolean trimmed() {
		int has99 = 0;
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			if (getSkills().getLevelForExperience(i) >= 99) {
				has99++;
				//getActionSender().sendConfig(313, 511); // Activates skillcape
				// icon.
				if (has99 >= 2) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the players membership expiry date.
	 *
	 * @return The players membership expiry date.
	 */
	public long getMembershipExpiryDate() {
		return membershipExpiryDate;
	}

	/**
	 * Sets the players membership expiry date.
	 *
	 * @param membershipExpiryDate The membership expiry date to set.
	 */
	public void setMembershipExpiryDate(long membershipExpiryDate) {
		this.membershipExpiryDate = membershipExpiryDate;
	}

	/**
	 * Sets the amount of membership days.
	 *
	 * @param days The amount of membership days to set.
	 */
	public void setMembershipDays(int days) {
		this.membershipExpiryDate = System.currentTimeMillis()
				+ (days * 0x5265C00L);
	}

	/**
	 * Gets the amount of days remaining this player has left of membership.
	 *
	 * @return The amount of days remaining this player has left of membership.
	 */
	public int getDaysOfMembership() {
		return TextUtils.toJagexDateFormatCeil(membershipExpiryDate
				- System.currentTimeMillis()); // ceiled as even 1 second into a
		// day would act like you have
		// -1 day of membership.
	}

	/**
	 * @return the recoveryQuestionsLastSet
	 */
	public String getRecoveryQuestionsLastSet() {
		return recoveryQuestionsLastSet;
	}

	/**
	 * @param recoveryQuestionsLastSet the recoveryQuestionsLastSet to set
	 */
	public void setRecoveryQuestionsLastSet(String recoveryQuestionsLastSet) {
		this.recoveryQuestionsLastSet = recoveryQuestionsLastSet;
	}

	/**
	 * Gets the amount of days this player hasn't logged in since.
	 *
	 * @return The amount of days this player hasn't logged in since.
	 */
	public int getLastLoggedInDays() {
		return TextUtils.toJagexDateFormatFloor(lastLoggedIn
				- System.currentTimeMillis()); // floored as we only want it to
		// send the "yesterday" string
		// if it has actually been since
		// yesterday
	}

	/**
	 * @return the lastLoggedIn
	 */
	public long getLastLoggedIn() {
		return lastLoggedIn;
	}

	/**
	 * @param lastLoggedIn the lastLoggedIn to set
	 */
	public void setLastLoggedIn(long lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	/**
	 * @return the lastLoggedInFrom
	 */
	public String getLastLoggedInFrom() {
		return lastLoggedInFrom;
	}

	/**
	 * @param lastLoggedInFrom the lastLoggedInFrom to set
	 */
	public void setLastLoggedInFrom(String lastLoggedInFrom) {
		this.lastLoggedInFrom = lastLoggedInFrom;
	}

	/**
	 * Gets the player's name expressed as a long.
	 *
	 * @return The player's name expressed as a long.
	 */
	public long getNameAsLong() {
		return nameLong;
	}

	/**
	 * Gets the player's settings.
	 *
	 * @return The player's settings.
	 */
	public Settings getSettings() {
		return settings;
	}

	public PlayerDetails getDetails() {
		return details;
	}
	
	public Perk[] getPerks()
	{
		return perks;
	}
	
	/**
	 * Gets two containers, one being the items you keep on death, and the
	 * second being the lost items.
	 *
	 * @return Two containers, one being the items you keep on death, and the
	 * second being the lost items.
	 */
	public Container[] getItemsKeptOnDeath() {
		int count = 3;
		if (getCombatState().getPrayer(Prayers.PROTECT_ITEM)
				|| getAttribute("protectItem") == Boolean.TRUE) { // protect
			// item
			count++;
			removeAttribute("protectItem");
		}
		if (getCombatState().getSkullTicks() > 0) {
			count -= 3;
		}
		Container topItems = new Container(Type.NEVER_STACK, count);
		Container temporaryInventory = new Container(Type.STANDARD,
				Inventory.SIZE);
		Container temporaryEquipment = new Container(Type.STANDARD,
				Equipment.SIZE);
		// Add all of our items with their designated slots.
		for (int i = 0; i < Inventory.SIZE; i++) {
			Item item = getInventory().get(i);
			if (item != null) {
				temporaryInventory.add(item, i);
			}
		}
		for (int i = 0; i < Equipment.SIZE; i++) {
			Item item = getEquipment().get(i);
			if (item != null) {
				temporaryEquipment.add(item, i);
			}
		}

		/*
		 * Checks our inventory and equipment for any items that can be added to
		 * the top list.
		 */
		for (int i = 0; i < Inventory.SIZE; i++) {
			Item item = temporaryInventory.get(i);
			if (item != null) {
				item = new Item(temporaryInventory.get(i).getId(), 1);
				for (int k = 0; k < count; k++) {
					Item topItem = topItems.get(k);
					if (topItem == null
							|| (item.getDefinition2().getHighAlch() == topItem.getDefinition2().getHighAlch() ?
							item.getDefinition().getStorePrice() > topItem.getDefinition().getStorePrice() : item.getDefinition2().getHighAlch() > topItem
							.getDefinition2().getHighAlch())) {
						if (topItem != null) {
							topItems.remove(topItem);
						}
						topItems.add(item);
						temporaryInventory.remove(item);
						if (topItem != null) {
							temporaryInventory.add(topItem);
						}
						if (item.getDefinition().isStackable()
								&& temporaryInventory.getCount(item.getId()) > 0) {
							// If stackable and we still have some items
							// remaining, we need to stay on this slot to check
							// if we can add any others.
							i--;
						}
						break;
					}
				}
			}
		}
		for (int i = 0; i < Equipment.SIZE; i++) {
			Item item = temporaryEquipment.get(i);
			if (item != null) {
				item = new Item(temporaryEquipment.get(i).getId(), 1);
				for (int k = 0; k < count; k++) {
					Item topItem = topItems.get(k);
					if (topItem == null
							||(item.getDefinition2().getHighAlch() == topItem.getDefinition2().getHighAlch() ?
							item.getDefinition().getStorePrice() > topItem.getDefinition().getStorePrice() : item.getDefinition2().getHighAlch() > topItem
							.getDefinition2().getHighAlch())) {
						if (topItem != null) {
							topItems.remove(topItem);
						}
						topItems.add(item);
						temporaryEquipment.remove(item);
						if (topItem != null) {
							temporaryEquipment.add(topItem);
						}
						if (item.getDefinition().isStackable()
								&& temporaryEquipment.getCount(item.getId()) > 0) {
							// If stackable and we still have some items
							// remaining, we need to stay on this slot to check
							// if we can add any others.
							i--;
						}
						break;
					}
				}
			}
		}

		/*
		 * For the rest of the items we have, we add them to a new container.
		 */
		Container lostItems = new Container(Type.STANDARD, Inventory.SIZE
				+ Equipment.SIZE);
		for (Item lostItem : temporaryInventory.toArray()) {
			if (lostItem != null) {
				lostItems.add(lostItem);
			}
		}
		for (Item lostItem : temporaryEquipment.toArray()) {
			if (lostItem != null) {
				lostItems.add(lostItem);
			}
		}
		return new Container[]{topItems, lostItems};
	}

	/**
	 * Queues or writes a packet to the player's {@link IoSession}.
	 * <p>
	 * Flushes all queued packets if the player becomes active.
	 *
	 * @param packet The packet to write.
	 */
	public void write(Packet packet) {
		synchronized (this) {
			if (!active) {
				pendingPackets.add(packet);
			} else {
				pendingPackets.forEach(session::write);
				pendingPackets.clear();
				session.write(packet);
			}
		}
	}

	/**
	 * Gets the player's bank.
	 *
	 * @return The player's bank.
	 */
	public Container getBank() {
		return bank;
	}

	public Container getPriceChecker() {
		return priceChecker;
	}

	/**
	 * Gets the interface state.
	 *
	 * @return The interface state.
	 */
	public InterfaceState getInterfaceState() {
		return interfaceState;
	}

	/**
	 * Checks if there is a cached update block for this cycle.
	 *
	 * @return <code>true</code> if so, <code>false</code> if not.
	 */
	public boolean hasCachedUpdateBlock() {
		return cachedUpdateBlock != null;
	}

	/**
	 * Sets the cached update block for this cycle.
	 *
	 * @param cachedUpdateBlock The cached update block.
	 */
	public void setCachedUpdateBlock(Packet cachedUpdateBlock) {
		this.cachedUpdateBlock = cachedUpdateBlock;
	}

	/**
	 * Gets the cached update block.
	 *
	 * @return The cached update block.
	 */
	public Packet getCachedUpdateBlock() {
		return cachedUpdateBlock;
	}

	/**
	 * Resets the cached update block.
	 */
	public void resetCachedUpdateBlock() {
		cachedUpdateBlock = null;
	}

	/**
	 * Gets the current chat message.
	 *
	 * @return The current chat message.
	 */
	public ChatMessage getCurrentChatMessage() {
		return currentChatMessage;
	}

	/**
	 * Sets the current chat message.
	 *
	 * @param currentChatMessage The current chat message to set.
	 */
	public void setCurrentChatMessage(ChatMessage currentChatMessage) {
		this.currentChatMessage = currentChatMessage;
	}

	/**
	 * Gets the queue of pending chat messages.
	 *
	 * @return The queue of pending chat messages.
	 */
	public Queue<ChatMessage> getChatMessageQueue() {
		return chatMessages;
	}

	/**
	 * Gets the player's appearance.
	 *
	 * @return The player's appearance.
	 */
	public Appearance getAppearance() {
		return appearance;
	}

	/**
	 * Gets the action sender.
	 *
	 * @return The action sender.
	 */
	public ActionSender getActionSender() {
		return actionSender;
	}

	/**
	 * Gets the incoming ISAAC cipher.
	 *
	 * @return The incoming ISAAC cipher.
	 */
	public ISAACCipher getInCipher() {
		return inCipher;
	}

	/**
	 * Gets the outgoing ISAAC cipher.
	 *
	 * @return The outgoing ISAAC cipher.
	 */
	public ISAACCipher getOutCipher() {
		return outCipher;
	}

	/**
	 * Gets the player's name.
	 *
	 * @return The player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the player's password.
	 *
	 * @return The player's password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the player's password.
	 *
	 * @param pass The password.
	 */
	public void setPassword(String pass) {
		this.password = pass;
	}

	/**
	 * Gets the <code>IoSession</code>.
	 *
	 * @return The player's <code>IoSession</code>.
	 */
	public IoSession getSession() {
		return session;
	}

	/**
	 * Checks if this player has a member's account.
	 *
	 * @return <code>true</code> if so, <code>false</code> if not.
	 */
	public boolean isMembers() {
		if (System.currentTimeMillis() > membershipExpiryDate)
			membershipExpiryDate = 0;
		return membershipExpiryDate != 0;
	}

	/**
	 * Sets the members flag.
	 *
	 * @param members The members flag.
	 */
	public void setMembers(boolean members) {
		this.members = members;
	}

	/**
	 * @return the receivedStarterRunes
	 */
	public boolean hasReceivedStarterRunes() {
		return receivedStarterRunes;
	}

	/**
	 * @param receivedStarterRunes the receivedStarterRunes to set
	 */
	public void setReceivedStarterRunes(boolean receivedStarterRunes) {
		this.receivedStarterRunes = receivedStarterRunes;
	}

	/**
	 * @return the debugMode
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * @param debugMode the debugMode to set
	 */
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}

	@Override
	public String toString() {
		return Player.class.getName() + " [name=" + name + " rights=" + Arrays.toString(databaseEntity.getPermissions().toArray())
				+ " members=" + members + " index=" + this.getIndex() + "]";
	}

	/**
	 * Sets the active flag.
	 *
	 * @param active The active flag.
	 */
	public void setActive(boolean active) {
		synchronized (this) {
			this.active = active;
		}
	}

	/**
	 * Gets the active flag.
	 *
	 * @return The active flag.
	 */
	public boolean isActive() {
		synchronized (this) {
			return active;
		}
	}
	
	private HerbSack herbSack = new HerbSack(this);
	
	public HerbSack getHerbSack() {
		return herbSack;	
	}

	/**
	 * Gets the inventory.
	 *
	 * @return The inventory.
	 */
	@Override
	public Container getInventory() {
		return inventory;
	}
	
	public Container getTourn() {
		return tourn;
	}

	/**
	 * Gets the trade inventory.
	 *
	 * @return The trade inventory.
	 */
	public Container getTrade() {
		return trade;
	}

	/**
	 * @return the pnpc
	 */
	public int getPnpc() {
		return pnpc;
	}

	/**
	 * @param pnpc the pnpc to set
	 */
	public void setPnpc(int pnpc) {
		this.pnpc = pnpc;
	}

	/**
	 * @return the minigame
	 */
	public Minigame getMinigame() {
		return minigame;
	}

	/**
	 * @param minigame the minigame to set
	 */
	public void setMinigame(Minigame minigame) {
		this.minigame = minigame;
	}

	public boolean isFightPitsWinner() {
		return fightPitsWinner;
	}

	public void setFightPitsWinner(boolean fightPitsWinner) {
		this.fightPitsWinner = fightPitsWinner;
	}

	@Override
	public MagicCombatAction.Spell getAutocastSpell() {
		return autocastSpell;
	}

	@Override
	public void setAutocastSpell(MagicCombatAction.Spell autocastSpell) {
		this.autocastSpell = autocastSpell;
	}

	@Override
	public boolean isAutoRetaliating() {
		return getSettings().isAutoRetaliating();
	}

	@Override
	public Animation getAttackAnimation() {
		return getCombatState().getCombatStyle() == CombatStyle.AGGRESSIVE_1 ? Animation
				.create(423) : Animation.create(422);
	}

	@Override
	public Animation getDeathAnimation() {
		return Animation.create(836);
	}

	@Override
	public Animation getDefendAnimation() {
		return (getEquipment().get(Equipment.SLOT_SHIELD) != null || getEquipment()
				.get(Equipment.SLOT_WEAPON) != null) ? Animation.create(404)
				: Animation.create(424);
	}

	@Override
	public int getProjectileLockonIndex() {
		return -getIndex() - 1;
	}

	@Override
	public double getProtectionPrayerModifier() {
		return 0.6;// * 0.6 removes 40%
	}

	@Override
	public String getDefinedName() {
		return "";
	}

	@Override
	public String getUndefinedName() {
		return getName();
	}

	@Override
	public void setDefaultAnimations() {
		standAnimation = Animation.create(808);
		runAnimation = Animation.create(824);
		walkAnimation = Animation.create(819);
		standTurnAnimation = Animation.create(823);
		turn180Animation = Animation.create(820);
		turn90ClockwiseAnimation = Animation.create(821);
		turn90CounterClockwiseAnimation = Animation.create(822);
	}

	@Override
	public void dropLoot(Mob mob) {
		if (BoundaryManager.isWithinBoundaryNoZ(getLocation(), "ClanWarsFFAFull") || BoundaryManager.isWithinBoundaryNoZ(getLocation(), "PestControl") || getRFD().isStarted() || FightCave.IN_CAVES.contains(this) || permissionService.is(this, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
			return;
		}
		if (BoundaryManager.isWithinBoundaryNoZ(getLocation(), "Zulrah")) {
			this.getZulAreth().appendDeath();
			getInventory().clear();
			getEquipment().clear();
			getEquipment().fireItemsChanged();
			getInventory().fireItemsChanged();
			return;
		}
		if (BoundaryManager.isWithinBoundaryNoZ(getLocation(), "Cerberus")) {
			Content cerberusContent = getContentManager().getActiveContent(Content.CERBERUS);
			if (cerberusContent != null) {
				cerberusContent.stop();
			}
		}
		Container[] items = itemService.getItemsKeptOnDeath(this);
		Container itemsKept = items[0];
		Container itemsLost = items[1];
		try {
			getInventory().clear();
			getEquipment().clear();

			Player receiver = this;
			if (mob.isPlayer()) {
				receiver = (Player) mob;
			}

			hookService.post(new GamePlayerKillEvent(this, receiver, itemsLost));
			for (Item i : itemsLost.toArray()) {
				if (i == null || i.getId() == 11941 || i.getId() == 12019 || i.getId() == 12020) {
					continue;
				}
				boolean pvp = receiver.isPlayer() && receiver != this;
				Optional<ItemService.DegradeOnDeath> deathOptional = ItemService.DegradeOnDeath.of(i.getId());
				if (receiver != this && deathOptional.isPresent()) {
					i.setId(deathOptional.get().getToId());
				}
				if (i.getId() == 12926 || i.getId() == 12931 || i.getId() == 13197 || i.getId() == 13199 || i.getId() == 12904) {
					int charges = itemService.getCharges(this, i);
					if (charges >= 1) {
						CacheItemDefinition def = CacheItemDefinition.get(itemService.getChargedItem(this, i));

						if (def != null) {
							Item chargedItem = new Item(def.getId(), charges);
							itemService.setChargesWithItem(this, i, chargedItem, 0);
							itemService.setCharges(this, i, -1);
							GroundItemService.GroundItem itemCharges = new GroundItemService.GroundItem(new Item(12934, i.getId() == 12926 ? (charges * 3) : charges), this.getLocation(), receiver, false, pvp, !pvp);
							groundItemService.createGroundItem(receiver, itemCharges);
							if (i.getId() == 12926) {
								GroundItemService.GroundItem chargedWith = new GroundItemService.GroundItem(chargedItem, this.getLocation(), receiver, false, pvp, !pvp);
								groundItemService.createGroundItem(receiver, chargedWith);
							}
						}
					}
					if (i.getId() == 12926) {
						GroundItemService.GroundItem blowpipe = new GroundItemService.GroundItem(new Item(12924), this.getLocation(), receiver, false, pvp, !pvp);
						groundItemService.createGroundItem(receiver, blowpipe);
					} else if (i.getId() == 12931) {
						GroundItemService.GroundItem serpHelm = new GroundItemService.GroundItem(new Item(12929), this.getLocation(), receiver, false, pvp, !pvp);
						groundItemService.createGroundItem(receiver, serpHelm);
					} else if (i.getId() == 13197 || i.getId() == 13199) {
						GroundItemService.GroundItem upgradedSerp = new GroundItemService.GroundItem(new Item(i.getId() - 1), this.getLocation(), receiver, false, pvp, !pvp);
						groundItemService.createGroundItem(receiver, upgradedSerp);
					} else if (i.getId() == 12904) {
						GroundItemService.GroundItem toxicsotd = new GroundItemService.GroundItem(new Item(12902), this.getLocation(), receiver, false, pvp, !pvp);
						groundItemService.createGroundItem(receiver, toxicsotd);
					}
				} else if (i.getId() == 12791) {
					final Player finalReceiver = receiver;
					getRunePouch().stream().filter(Objects::nonNull).
							forEach(f -> groundItemService.createGroundItem(finalReceiver, new GroundItemService.GroundItem(f, getLocation(), finalReceiver, false, finalReceiver != this, finalReceiver == this)));
					groundItemService.createGroundItem(this, new GroundItemService.GroundItem(i, getLocation(), this, false));
					getRunePouch().clear();
					pouchService.updatePouchInterface(this);
				} else if (i.getId() == 12006 && receiver.isPlayer() && receiver != this) {
					int charges = itemService.getCharges(this, i);
					if (charges >= 1) {
						CacheItemDefinition def = CacheItemDefinition.get(itemService.getChargedItem(this, i));

						if (def != null) {
							Item chargedItem = new Item(def.getId(), charges);
							itemService.setChargesWithItem(this, i, chargedItem, 0);
							itemService.setCharges(this, i, -1);
						}
					}
					GroundItemService.GroundItem tentWhip = new GroundItemService.GroundItem(new Item(12004), this.getLocation(), receiver, false, pvp, !pvp);
					groundItemService.createGroundItem(receiver, tentWhip);
				} else if (Constants.playerBoundItem(i.getId()) || !i.getDefinition().isTradable()) { //Makes sure items such as Torso, Fire cape is only visible to the player which the item belongs to.
					groundItemService.createGroundItem(this, new GroundItemService.GroundItem(i, this.getLocation(), this, false, false, true));
				} else {
					GroundItemService.GroundItem groundItem = new GroundItemService.GroundItem(i, this.getLocation(), receiver, false, pvp, !pvp);
					groundItemService.createGroundItem(receiver, groundItem);
				}
			}
			groundItemService.createGroundItem(receiver, new GroundItemService.GroundItem(new Item(526), this.getLocation(), receiver, false));
			for (Item item : itemsKept.toArray()) {
				if (item != null) {
					getInventory().add(item);
				}
			}
		} finally {
			getEquipment().fireItemsChanged();
			getInventory().fireItemsChanged();
		}
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public Graphic getDrawbackGraphic() {
		return null;
	}

	@Override
	public int getProjectileId() {
		return -1;
	}

	public void faceObject(GameObject object) {
		if (object.getId() == 11374) {
			face(Location.create(2713, 3494));
			return;
		} else if (object.getId() == 11375) {
			face(object.getLocation());
			return;
		} else if (object.getId() == 11377) {
			face(Location.create(2704, 3464));
			return;
		}
		if (getLocation().equals(object.getLocation())) {
			int offX = 0;
			int offY = 0;
			switch (object.getDirection()) {
				case 0:
					offX = -1;
					break;
				case 1:
					offY = 1;
					break;
				case 2:
					offX = 1;
					break;
				case 3:
					offY = -1;
					break;
			}
			face(object.getLocation().transform(offX, offY, 0));
		} else if (object.getType() >= 9
				&& object.getType() <= 11
				&& (object.getDefinition().sizeX > 1 || object.getDefinition().sizeY > 1)) {
			face(object.getLocation().transform(
					object.getDefinition().sizeX >> 1,
					object.getDefinition().sizeY >> 1, 0));
		} else {
			face(object.getLocation());
		}
	}

	public void stun(int stunTime, String string, boolean gfx) {

		if (gfx) {
			playGraphics(Graphic.STUNNED_GRAPHIC);
			playSound(Sound.THIEVING_STUNNED);
		}
		getActionSender().sendMessage(string);
		setAttribute("stunned", true);
		World.getWorld().submit(new Tickable(stunTime) {

			@Override
			public void execute() {
				removeAttribute("stunned");
				this.stop();
			}

		});

	}

	public void stunInstantly(int stunTime, String string, boolean gfx) {
		if (gfx) {
			playGraphics(Graphic.STUNNED_GRAPHIC);
			playSound(Sound.THIEVING_STUNNED);
		}
		getActionSender().sendMessage(string);
		setAttribute("stunned", true);
	}

	public void addToPacketQueue(Packet message) {
		// packetQueue.addLast(message);
		PacketManager.getPacketManager().handle(this, message);
	}

	public void processPackets() {
		// Packet message;
		// while ((message = packetQueue.poll()) != null) {
		// PacketManager.getPacketManager().handle(this, message);
		// }
	}

	public Set<NPC> getInstancedNPCs() {
		return instancedNPCs;
	}

	public boolean isMultiplayerDisabled() {
		return multiplayerDisabled;
	}

	public void setMultiplayerDisabled(boolean multiplayerDisabled) {
		this.multiplayerDisabled = multiplayerDisabled;
	}

	@Override
	public void tick() {
		if (monkeyTime > 0) {
			monkeyTime--;
		} else if (monkeyTime == 0 && getPnpc() != -1 && monkey != null) {
			monkey = null;
			setPnpc(-1);
			getUpdateFlags().flag(UpdateFlag.APPEARANCE);
		}
		getContentManager().process();
		//getActionSender().updateQuestText();
	}

	public FightCave getFightCave() {
		return fightCave;
	}

	private Item tempItem = null;

	public void setTempItem(Item item) {
		this.tempItem = item;
	}

	public Item getTempItem() {
		return tempItem;
	}

	/**
	 * Dueling
	 */
	private Dueling dueling;

	public Dueling getDueling() {
		return dueling;
	}

	public void setDueling(Dueling dueling) {
		this.dueling = dueling;
	}

	/**
	 * The player's duelContainer.
	 */
	private final Container duelContainer = new Container(Container.Type.STANDARD,
			OldDuel.SIZE);

	private final Container runePouch = new Container(Type.STANDARD, RunePouchServiceImpl.SIZE);

	public Container getDuelContainer() {
		return duelContainer;
	}

	public BountyHunterNode getBountyHunter() {
		return bountyHunter;
	}

	public void setBountyHunter(BountyHunterNode bountyHunter) {
		this.bountyHunter = bountyHunter;
	}

	public int dfsWait = 0, dfsCharges = 0;

	public int barrelWait = 0;
	public int barrelPoints = 0;
	public boolean barrelChest = false;

	public Banking getBanking() {
		return banking;
	}

	public String filterWeaponName(String name) {
		final String[] filtered = new String[]{"Iron", "Steel", "Scythe",
				"Black", "Mithril", "Adamant", "Rune", "Granite", "Dragon",
				"Crystal", "Bronze", "Drag"};
		for (String filter : filtered) {
			name = name.replaceAll(filter, "");
		}
		return name;
	}

	private boolean lighting;

	public void setLighting(boolean lighting) {
		this.lighting = lighting;
	}

	public boolean isLighting() {
		return lighting;
	}

	private int barrowsKills;

//    public int toxicItem = -1;

	public boolean loadedIn;


	private Map<Integer, Boolean> killedBrothers = new HashMap<>();

	public void increaseKC() {
		barrowsKills++;
	}

	public int getKC() {
		return barrowsKills;
	}

	public void setKC(int kc) {
		this.barrowsKills = kc;
	}
	
	public void sellSilk(int price)
	{
		for(int i = 0; i < 28; i++)
		{
			if(getInventory().contains(950))
			{
				getInventory().remove(new Item(950));
				getInventory().add(new Item(995, price));				
			}
		}
	}
	
	public void teleport(Location location, int xOffset, int yOffset, boolean npcTeleport) {
		if (location == null) {
			return;
		}
		if(Location.getWildernessLevel(this, getLocation()) > 20)
		{
			getActionSender().sendMessage("You can't use this teleport after level 20 wilderness.");
			return;
		}
		if (getAttribute("stunned") != null) {
			getActionSender().sendMessage("You're stunned!");
			return;
		}
		if (getCombatState().isDead()) {
			return;
		}
		if (getDatabaseEntity().getPlayerSettings().isTeleBlocked()) {
			getActionSender().sendMessage("A magical force stops you from teleporting.");
			return;
		}
		if (BoundaryManager.isWithinBoundaryNoZ(getLocation(), "ClanWarsFFAFull")) {
			getActionSender().sendMessage("You can't teleport from here, please use the portal to leave.");
			return;
		}
		if (getAttribute("busy") != null) {
			return;
		}
		if (BoundaryManager.isWithinBoundaryNoZ(getLocation(), "PestControl") || BoundaryManager.isWithinBoundaryNoZ(getLocation(), "PestControlBoat")) {
			return;
		}
		if (WarriorsGuild.IN_GAME.contains(this)) {
			WarriorsGuild.IN_GAME.remove(this);
		}
		if (getRFD().isStarted() || FightCave.IN_CAVES.contains(this)) {
			getActionSender().sendMessage("You can't teleport from here!");
			return;
		}
		if (getAttribute("glorySlot") != null) {
			Item item = getInventory().get(getAttribute("glorySlot"));
			if (item == null) {
				return;
			}
			getInventory().set(getAttribute("glorySlot"), new Item(item.getId() - 2));
			removeAttribute("glorySlot");
		}
		if (hasAttribute("teleporting")) {
			return;
		}
		setCanBeDamaged(false);
		resetBarrows();
		resetInteractingEntity();
		getActionQueue().clearAllActions();
		getActionManager().stopAction();
		setAttribute("teleporting", true);
		getActionSender().removeChatboxInterface();
		int x = location.getX();
		int y = location.getY();
		int z = location.getZ();
		Sound sound = null;
		int emote;
		int gfx;
		int ticks;
		if (hasAttribute("ownedNPC")) {//player.setAttribute("ownedNPC", n);
			NPC n = (NPC) getAttribute("ownedNPC");
			if (n != null) {
				World.getWorld().unregister(n);
			}
			removeAttribute("ownedNPC");
		}
		if (!npcTeleport) {
			if (getCombatState().getSpellBook() == MagicCombatAction.SpellBook.MODERN_MAGICS.getSpellBookId()) {
				sound = Sound.MODERN_TELE;
				emote = 714;//714 / 3864
				gfx = 308;//308 / 1039
				ticks = 4;
			} else if (getCombatState().getSpellBook() == MagicCombatAction.SpellBook.ANCIENT_MAGICKS.getSpellBookId()) {
				sound = Sound.ANCIENT_END_TELE;
				emote = 1979;//1979
				gfx = 392;//392
				ticks = 4;
			} else if (getCombatState().getSpellBook() == MagicCombatAction.SpellBook.LUNAR_MAGICS.getSpellBookId()) {
				sound = Sound.MODERN_TELE;
				emote = 1816;//1816
				gfx = 747;//747
				ticks = 4;
			} else if (getCombatState().getSpellBook() == MagicCombatAction.SpellBook.ARCEUUS_MAGICS.getSpellBookId()) {
				sound = Sound.MODERN_TELE;
				emote = 1816;//1816
				gfx = 1296;//747
				ticks = 4;
			} else {
				return;
			}
		} else {
			sound = Sound.TELEPORT_MINI_ESS;
			emote = 3864;//714
			gfx = 1039;//1039
			ticks = 3;
		}
		if(sound != null)
		playSound(sound);
		playAnimation(Animation.create(emote));
		playGraphics(Graphic.create(gfx, 48, getCombatState().getSpellBook() == MagicCombatAction.SpellBook.ANCIENT_MAGICKS.getSpellBookId() ? 0 : 100));//48  0 : 100
		Random r = new Random();
		setCanBeDamaged(false);
		if(npcTeleport && location != Constants.HOME_TELEPORT)
		settings.setLastLocation(location);
		World.getWorld().submit(new Tickable(ticks) {
			public void execute() {
				resetInteractingEntity();
				setTeleportTarget(Location.create(x + (xOffset > 0 ? r.nextInt(xOffset) : 0), y + (yOffset > 0 ? r.nextInt(yOffset) : 0), z));
				playAnimation(Animation.create(-1, 0));
				setCanBeDamaged(true);
				removeAttribute("teleporting");
				if(pet != null )
				pet.tick();
				setCanBeDamaged(true);
				this.stop();
			}
		});
	}

	@Override
	public void register() {
		World.getWorld().register(this);
	}

	@Override
	public void unregister() {
		World.getWorld().unregister(this);
	}

	@Override
	public void destroy() {
		super.destroy();
		getContentManager().stopAll();

		if (pestControlInstance != null && pestControlInstance.getPlayers().remove(this)) {
			pestControlInstance.getPlayers().remove(this);
			setTeleportTarget(pestControlInstance.getBoat().getExit());
		}
		if (pet != null) {
			World.getWorld().unregister(pet);
		}
	}

	private SlayerMasterWidget slayerMasterWidget;

	public void setName(String name) {
		this.name = name;
	}

	private Slayer slayer = new Slayer();

	public int totalPrice;

	public Slayer getSlayer() {
		return slayer;
	}

	public Queue<Integer> getWeaponSwitchQueue() {
		return weaponSwitchQueue;
	}

	public boolean hasQueuedSwitching() {
		return queuedSwitching;
	}

	public void setQueuedSwitching(boolean b) {
		this.queuedSwitching = b;
	}

	public ContentManager getContentManager() {
		return contentManager;
	}

	public Map<Integer, Boolean> getKilledBrothers() {
		return killedBrothers;
	}

	public Map<Integer, Integer> essenceMap = new HashMap<Integer, Integer>();

	public void doorOpenClose(GameObject object, int xOff, int yOff, int newDirection) {

		boolean runToggled = getWalkingQueue().isRunningToggled();
		getWalkingQueue().setRunningToggled(false);
		World.getWorld().replaceObjectKeepClipping(object, new GameObject(object.getLocation(), object.getId(), newDirection, object.getType(), false), 3);
		Agility.forceWalkingQueue(this, Animation.create(getWalkAnimation().getId()), getLocation().getX() + xOff, getLocation().getY() + yOff, 1, 1, true);
		World.getWorld().submit(new StoppingTick(1) {
			@Override
			public void executeAndStop() {
				getWalkingQueue().setRunningToggled(runToggled);
			}
		});
	}

	private ActionManager actionManager = new ActionManager(this);

	public ActionManager getActionManager() {
		return actionManager;
	}

	private final Container lootingBag = new Container(Container.Type.STANDARD, 27);

	public Container getLootingBag() {
		return lootingBag;
	}

	private int[] possibleProductions;

	public void setPossibleProductions(int[] items) {
		this.possibleProductions = items;
	}

	@Override
	public void deserialize(IoBuffer buf) {
		this.name = IoBufferUtils.getRS2String(buf);
		this.nameLong = NameUtils.nameToLong(this.name);
		this.password = IoBufferUtils.getRS2String(buf);
		// this.rights = Player.Rights.getRights(buf.getUnsigned());
		//this.rights = Player.Rights.getRights(buf.get());
//		this.getSettings().setIronMan(buf.get() == 3);
		this.isIronMan = buf.get() == 3;

		//this.configureRights(this.details);
		this.members = buf.getUnsigned() == 1;

		// Skip location (x/y/z)
		buf.getUnsignedShort();
		buf.getUnsignedShort();
		buf.getUnsigned();
		//setLocation(Location.create(buf.getUnsignedShort(),
//                buf.getUnsignedShort(), buf.getUnsigned()));

		int[] look = new int[13];
		for (int i = 0; i < 13; i++) {
			look[i] = buf.getUnsigned();
		}
		appearance.setLook(look);
		for (int i = 0; i < Equipment.SIZE; i++) {
			int id = buf.getUnsignedShort();
			if (id != 65535) {
				int amt = buf.getInt();
				Item item = new Item(id, amt);
				getEquipment().set(i, item);
			}
		}
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			skills.setSkill(i, buf.getUnsigned(), buf.getDouble());
		}
		for (int i = 0; i < Inventory.SIZE; i++) {
			int id = buf.getUnsignedShort();
			if (id != 65535) {
				int amt = buf.getInt();
				Item item = new Item(id, amt);
				inventory.set(i, item);
			}
		}
		if (buf.hasRemaining()) { // backwards compat
			for (int i = 0; i < Bank.SIZE; i++) {
				int id = buf.getUnsignedShort();
				if (id != 65535) {
					int amt = buf.getInt();
					Item item = new Item(id, amt);
					bank.set(i, item);
				}
			}
		}
		if (buf.hasRemaining()) {
			getWalkingQueue().setEnergy(buf.get());
		}
		if (buf.hasRemaining()) {
			getSettings().setBrightnessSetting(buf.get());
		}
		if (buf.hasRemaining()) {
			getSettings().setTwoMouseButtons(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getSettings().setChatEffects(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getSettings().setSplitPrivateChat(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getSettings().setAcceptAid(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getWalkingQueue().setRunningToggled(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getCombatState().setCombatStyle(CombatStyle.forId(buf.get()));
		}
		if (buf.hasRemaining()) {
			setMembershipExpiryDate(buf.getLong());
		}
		if (buf.hasRemaining()) {
			setRecoveryQuestionsLastSet(IoBufferUtils.getRS2String(buf));
		}
		if (buf.hasRemaining()) {
			setLastLoggedIn(buf.getLong());
		}
		if (buf.hasRemaining()) {
			setLastLoggedInFrom(IoBufferUtils.getRS2String(buf));
		}
		if (buf.hasRemaining()) {
			getSettings().setSwapping(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getSettings().setWithdrawAsNotes(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getSettings().setRFDState(buf.get());
		}
		if (buf.hasRemaining()) {
			getSkills().setPrayerPoints(buf.getDouble(), false);
		}
		if (buf.hasRemaining()) {
			getCombatState().setSpellBook(buf.get());
		}
		if (buf.hasRemaining()) {
			getCombatState().setRingOfRecoil(buf.get());
		}
		if (buf.hasRemaining()) {
			getCombatState().setPoisonDamage(buf.get(), this);
		}
		if (buf.hasRemaining()) {
			getCombatState().setSpecialEnergy(buf.get());
		}
		if (buf.hasRemaining()) {
			getInterfaceState().setPublicChat(buf.get());
		}
		if (buf.hasRemaining()) {
			getInterfaceState().setPrivateChat(buf.get());
		}
		if (buf.hasRemaining()) {
			getInterfaceState().setTrade(buf.get());
		}
		if (buf.hasRemaining()) {
			getSettings().setAutoRetaliate(buf.get() == 1);
		}
		if (buf.hasRemaining()) {
			getCombatState().setSkullTicks(buf.getShort());
		}
		if (buf.hasRemaining()) {
			setReceivedStarterRunes(buf.get() == 1);
		}

		if (buf.hasRemaining()) {
			getSettings().setRogueBHKills(buf.getInt());
			getSettings().setHunterBHKills(buf.getInt());
		}
		if (buf.hasRemaining()) {
			byte bounty = buf.get();
			if (bounty > -1) {
				bountyHunter = new BountyHunterNode(this);
				bountyHunter.setCrater(BountyHunterCrater.values()[bounty]);
			}
		}
		if (buf.hasRemaining()) {
			for (int i = 0; i < 4; i++)
				settings.getStrongholdChest()[i] = buf.get() == 1;
		}
		if (buf.hasRemaining()) {
			this.dfsWait = buf.get();
			this.dfsCharges = buf.get();
		}
		if (buf.hasRemaining()) {
			this.pickupPenalty = buf.get();
			if (pickupPenalty < -1)
				pickupPenalty = pickupPenalty & 0xFF;
			int leavePen = buf.get();
			if (leavePen < -1)
				leavePen = leavePen & 0xFF;
			if (bountyHunter != null) {
				bountyHunter.setLeavePenalty2(leavePen);
			}
			if (buf.hasRemaining()) {
				bountyDelay = buf.get();
			}
		}
		if (buf.hasRemaining()) {
			for (int i = 0; i < banking.getTab().length; i++) {
				banking.getTab()[i] = buf.getShort();
			}
		}

		if (buf.hasRemaining()) {
			buf.getShort();//toxic charges
			buf.getShort();//toxic item
			//this.toxicCharges = buf.getShort();
			//this.toxicItem = buf.getShort();
		}

		if (buf.hasRemaining()) {
			this.queuedSwitching = buf.get() == 1;
		}

		if (buf.hasRemaining()) {
			getSettings().setPlayerClickPriority(buf.get());
		}

		if (buf.hasRemaining()) {
			getSettings().setLastWithdrawnValue(buf.getInt());
		}

		//		if (buf.hasRemaining()) {
		//			getSettings().setXPPosition(buf.get());
		//		}

		if (buf.hasRemaining()) {

			for (int i = 0; i < getCombatState().getQuickPrayers().length; i++) {
				boolean on = buf.get() == 1;


				getCombatState().setQuickPrayer(i, on);// should be good
			}
		}

		if (buf.hasRemaining()) {
			for (int i = 0; i < BarrowsBrother.values().length; i++) {
				BarrowsBrother brother = BarrowsBrother.values()[i];

				boolean dead = buf.get() == 1;

				getKilledBrothers().put(brother.getNpcId(), dead);
			}
		}

		if (buf.hasRemaining() && buf.get() == 1) {
			slayer.setSlayerTask(new SlayerTask(Master.values()[buf.get()], buf.get(), buf.getInt()));
		}

		if (buf.hasRemaining()) {
			quests.put(DesertTreasure.class, new DesertTreasure(this, DTStates.values()[buf.get()]));
		}

		if (buf.hasRemaining() && buf.get() == 5) {
			setAttribute("barrows_tunnel", BarrowsBrother.forId(buf.getShort()));// i think xd
		}

		if (buf.hasRemaining() && buf.get() == 1) {
			setAttribute("looted_barrows", true);
		}

		if (buf.hasRemaining()) {
			setKC(buf.get());
		}

		if (buf.hasRemaining()) {
			getSettings().setFightCaveState(buf.get());
		}

		if (buf.hasRemaining() && buf.get() == 1) {
			getSettings().setCompletedMageArena(true);
		}
		
		if (buf.remaining() >= 2) {
			buf.get();
			buf.get();
//			dropdown.setSelectedSkill(buf.get()).setExperienceBarSkill(buf.get());
		}
		if (buf.hasRemaining()) {
			quests.put(LunarDiplomacy.class, new LunarDiplomacy(this, LunarStates.values()[buf.get()]));
		}
		
		if (buf.hasRemaining()) {
			quests.put(CooksAssistant.class, new CooksAssistant(this, CooksAssistantState.values()[buf.get()]));
		}

		if (buf.hasRemaining()) {
			setNameColor(IoBufferUtils.getRS2String(buf));
		}

		if (buf.hasRemaining() && buf.get() == 1) {
			setPunished(true);
			
			 //new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth() + days, DateTime.now().getHourOfDay() + hours, DateTime.now().getMinuteOfHour() + minutes, DateTime.now().getSecondOfMinute()));
			 
			getPunishment().setPunishmentEnd(new DateTime(buf.getShort(), buf.getShort(), buf.getShort(), buf.getShort(), buf.getShort(), buf.getShort()));
			getPunishment().setPunishmentType(PunishmentType.getType(buf.get()));
		}

		if (buf.hasRemaining() && buf.get() == 1) {
			setAttribute("canLoot", true);
		}
		if (buf.hasRemaining()) {
			getSettings().setBestRFDState(buf.get());
		}
		if (buf.hasRemaining() && buf.get() == 1) {
			setIsIronMan(false);
			setUltimateIronMan(true);
		}
		/*if (buf.hasRemaining()) { // backwards compat
			for (int i = 0; i < LootingBagServiceImpl.SIZE; i++) {
				int id = buf.getUnsignedShort();
				if (id != 65535) {
					int amt = buf.getInt();
					Item item = new Item(id, amt);
					lootingBag.set(i, item);
				}
			}
		}*/
		/*if (buf.hasRemaining()) {
			for (int i = 0; i < RunePouchServiceImpl.SIZE; i++) {
				int id = buf.getUnsignedShort();
				if (id != 65535) {
					int amt = buf.getInt();
					Item item = new Item(id, amt);
					runePouch.set(i, item);
				}
			}
		}*/
	}

	@Override
	public void serialize(IoBuffer buf) {
		final PersistenceService persistenceService = Server.getInjector().getInstance(PersistenceService.class);
		try {
			persistenceService.savePlayer(this);
		} catch (Exception e) {
			logger.error("Error dealing with persistence for " + name, e);
		}

		//IoBufferUtils.putRS2String(buf, persistedPlayer.getAccountName());
		IoBufferUtils.putRS2String(buf, name);
		IoBufferUtils.putRS2String(buf, password);
		buf.put((byte) 0);//buf.put((byte) rights.toInteger());
		buf.put((byte) (members ? 1 : 0));
		buf.putShort((short) getLocation().getX());
		buf.putShort((short) getLocation().getY());
		buf.put((byte) getLocation().getZ());
		int[] look = appearance.getLook();
		for (int i = 0; i < 13; i++) {
			buf.put((byte) look[i]);
		}
		for (int i = 0; i < Equipment.SIZE; i++) {
			Item item = getEquipment().get(i);
			if (item == null) {
				buf.putShort((short) 65535);
			} else {
				buf.putShort((short) item.getId());
				buf.putInt(item.getCount());
			}
		}
		for (int i = 0; i < Skills.SKILL_COUNT; i++) {
			buf.put((byte) skills.getLevel(i));
			buf.putDouble(skills.getExperience(i));
		}
		for (int i = 0; i < Inventory.SIZE; i++) {
			Item item = inventory.get(i);
			if (item == null) {
				buf.putShort((short) 65535);
			} else {
				buf.putShort((short) item.getId());
				buf.putInt(item.getCount());
			}
		}
		for (int i = 0; i < Bank.SIZE; i++) {
			Item item = bank.get(i);
			if (item == null) {
				buf.putShort((short) 65535);
			} else {
				buf.putShort((short) item.getId());
				buf.putInt(item.getCount());
			}
		}
		buf.put((byte) (getWalkingQueue().getEnergy()));
		buf.put((byte) (getSettings().getBrightnessSetting()));
		buf.put((byte) (getSettings().twoMouseButtons() ? 1 : 0));
		buf.put((byte) (getSettings().chatEffects() ? 1 : 0));
		buf.put((byte) (getSettings().splitPrivateChat() ? 1 : 0));
		buf.put((byte) (getSettings().isAcceptingAid() ? 1 : 0));
		buf.put((byte) (getWalkingQueue().isRunningToggled() ? 1 : 0));
		buf.put((byte) (getCombatState().getCombatStyle().getId()));
		buf.putLong(getMembershipExpiryDate());
		IoBufferUtils.putRS2String(buf, getRecoveryQuestionsLastSet());
		buf.putLong(getLastLoggedIn());
		IoBufferUtils.putRS2String(buf, getLastLoggedInFrom());
		buf.put((byte) (getSettings().isSwapping() ? 1 : 0));
		buf.put((byte) (getSettings().isWithdrawingAsNotes() ? 1 : 0));
		buf.put((byte) (getSettings().getRFDState()));
		buf.putDouble(getSkills().getPrayerPoints());
		buf.put((byte) getCombatState().getSpellBook());
		buf.put((byte) getCombatState().getRingOfRecoil());
		buf.put((byte) getCombatState().getPoisonDamage());
		buf.put((byte) getCombatState().getSpecialEnergy());
		buf.put((byte) getInterfaceState().getPublicChat());
		buf.put((byte) getInterfaceState().getPrivateChat());
		buf.put((byte) getInterfaceState().getTrade());
		buf.put((byte) (getSettings().isAutoRetaliating() ? 1 : 0));
		buf.putShort((short) getCombatState().getSkullTicks());
		buf.put((byte) (hasReceivedStarterRunes() ? 1 : 0));

		buf.putInt(getSettings().getRogueBHKills());
		buf.putInt(getSettings().getHunterBHKills());

		buf.put((byte) (bountyHunter == null ? -1 : bountyHunter.getCrater()
				.ordinal()));

		for (boolean b : settings.getStrongholdChest())
			buf.put((byte) (b ? 1 : 0));
		buf.put((byte) dfsWait);
		buf.put((byte) dfsCharges);

		buf.put((byte) pickupPenalty);
		buf.put((byte) (bountyHunter == null ? -1 : bountyHunter
				.getLeavePenalty()));
		buf.put((byte) bountyDelay);
		for (int i : banking.getTab())
			buf.putShort((short) i);

		buf.putShort((short) 0);//toxic charges
		buf.putShort((short) 0);//toxic item
/*        buf.putShort((short) toxicCharges);
        buf.putShort((short) toxicItem);*/

		buf.put((byte) (queuedSwitching ? 1 : 0));

		buf.put((byte) getSettings().getPlayerClickPriority());

		buf.putInt((byte) getSettings().getLastWithdrawnValue());

		//buf.put((byte) getSettings().getXPPosition());

		for (int i = 0; i < getCombatState().getQuickPrayers().length; i++) {
			buf.put((byte) (getCombatState().getQuickPrayers()[i] ? 1 : 0));
		}

		for (int i = 0; i < BarrowsBrother.values().length; i++) {
			BarrowsBrother brother = BarrowsBrother.values()[i];

			buf.put((byte) (getKilledBrothers().containsKey(brother.getNpcId()) && getKilledBrothers().get(brother.getNpcId()) ? 1 : 0));
		}
		buf.put((byte) (slayer.getSlayerTask() != null ? 1 : 0));
		if (slayer.getSlayerTask() != null) {
			buf.put((byte) slayer.getSlayerTask().getMaster().ordinal());
			buf.put((byte) slayer.getSlayerTask().getTaskId());
			buf.putInt(slayer.getSlayerTask().getTaskAmount());
		}

		// will have to save quests one by one but we can store them all in the map


		Quest<DTStates> quest = (Quest<DTStates>) quests.get(DesertTreasure.class);
		if (quest == null) {
			quest = new DesertTreasure(this, DTStates.NOT_STARTED);
			getQuests().put(DesertTreasure.class, quest);
		}
		buf.put((byte) quest.getState().ordinal());// make sense? not actually gonna save this as it would be wasted space on char files but thats how we could save and load a quest. ill show loading code.

		// tbh i was just gonna write it i didnt realize u were xd

		
		Quest<CooksAssistantState> cooks = (Quest<CooksAssistantState>) quests.get(CooksAssistant.class);
		if (cooks == null) {
			cooks = new CooksAssistant(this, CooksAssistantState.NOT_STARTED);
			getQuests().put(CooksAssistant.class, cooks);
		}
		buf.put((byte) cooks.getState().ordinal());
		
		Quest<LunarStates> lunar = (Quest<LunarStates>) quests.get(LunarDiplomacy.class);
		if (lunar == null) {
			lunar = new LunarDiplomacy(this, LunarStates.NOT_STARTED);
			getQuests().put(LunarDiplomacy.class, lunar);
		}
		buf.put((byte) lunar.getState().ordinal());
		
		boolean hasTunnel = hasAttribute("barrows_tunnel");

		buf.put((byte) (hasTunnel ? 1 : 0));
		if (hasTunnel) {
			BarrowsBrother brother = getAttribute("barrows_tunnel");
			buf.putShort((short) brother.getNpcId());//ye?
		}

		boolean canLoot = hasAttribute("looted_barrows");

		buf.put((byte) (canLoot ? 1 : 0));

		buf.put((byte) getKC());

		buf.put((byte) (getSettings().getFightCaveState()));

		buf.put((byte) (getSettings().completedMageArena() ? 1 : 0));

		buf.put((byte) dropdown.getSelectedValue());
		buf.put((byte) dropdown.getExperienceBarValue());

		IoBufferUtils.putRS2String(buf, getNameColor());

		buf.put((byte) (getPunishment().getPunishmentEnd() == null ? 0 : 1));
		if (getPunishment().getPunishmentEnd() != null) {
			buf.putShort((short) getPunishment().getPunishmentEnd().getYear());
			buf.putShort((short) getPunishment().getPunishmentEnd().getMonthOfYear());
			buf.putShort((short) getPunishment().getPunishmentEnd().getDayOfMonth());
			buf.putShort((short) getPunishment().getPunishmentEnd().getHourOfDay());
			buf.putShort((short) getPunishment().getPunishmentEnd().getMinuteOfHour());
			buf.putShort((short) getPunishment().getPunishmentEnd().getSecondOfMinute());
			buf.put((byte) getPunishment().getPunishmentType().toInteger());
		}
		boolean canLootBarrows = hasAttribute("canLoot") ? getAttribute("canLoot") : false;
		buf.put((byte) (canLootBarrows ? 5 : 0));

		buf.put((byte) getSettings().getBestRFDState());
		buf.put((byte) (ultimateIronMan ? 1 : 0));
		for (int i = 0; i < LootingBagServiceImpl.SIZE; i++) {
			Item item = lootingBag.get(i);
			if (item == null) {
				buf.putShort((short) 65535);
			} else {
				buf.putShort((short) item.getId());
				buf.putInt(item.getCount());
			}
		}
		for (int i = 0; i < RunePouchServiceImpl.SIZE; i++) {
			Item item = runePouch.get(i);
			if (item == null) {
				buf.putShort((short) 65535);
			} else {
				buf.putShort((short) item.getId());
				buf.putInt(item.getCount());
			}
		}
	}

	@Override
	public void addToRegion(Region region) {
		region.addPlayer(this);
		region.addMob(this);
	}

	@Override
	public void removeFromRegion(Region region) {
		region.removePlayer(this);
		region.removeMob(this);
	}

	@Override
	public int getClientIndex() {
		return this.getIndex() + 32768;
	}

	@Override
	public Skills getSkills() {
		return skills;
	}

	@Override
	public int getHeight() {
		return 1;
	}

	@Override
	public int getWidth() {
		return 1;
	}

	@Override
	public boolean isNPC() {
		return false;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public int getCombatCooldownDelay() {
		return CombatFormulae.getCombatCooldownDelay(this);
	}

	@Override
	public CombatAction getDefaultCombatAction() {
		return null;
	}

	@Override
	public Location getCentreLocation() {
		return getLocation();
	}

	@Override
	public boolean canHit(Mob victim, boolean messages) {
		final World world = World.getWorld();
		if (victim.isNPC()) {
			NPC n = (NPC) victim;
			/*if (!n.isAttackable()) {
				return false;
			}*/
			if (n.hasAttribute("attack")) {
				return false;
			}
			if (n.getInstancedPlayer() != null && n.getInstancedPlayer() != this) {
				getActionSender().sendMessage("This NPC was not spawned for you.");
				return false;
			}
			if (n instanceof Whirlpool || n instanceof Kraken) {
				if (this.getActiveCombatAction() == MeleeCombatAction.getAction()) {
					sendMessage("You can't reach that.");
					return false;
				}
				if (n instanceof Whirlpool) {
					Whirlpool whirlpool = (Whirlpool) n;
					if (whirlpool.getTransformId() == KrakenServiceImpl.TENTACLE && whirlpool.getKraken().getTransformId() != KrakenServiceImpl.KRAKEN) {
						return false;
					}
				}
			}
		}

		if (victim.isNPC()) {
			MetalArmour current = getWarriorsGuild().getCurrentArmour();
			if (current != null && victim instanceof MetalArmour) {

				boolean allowed = victim == current;

				if (!allowed) {
					getActionSender().sendMessage("This is not your enemy.");
				}
				return allowed;
			}
		}
		if ((getAutocastSpell() == MagicCombatAction.Spell.TRIDENT_OF_THE_SEAS || getAutocastSpell() == MagicCombatAction.Spell.TRIDENT_OF_THE_SWAMP) && victim.isPlayer()) {
			return false;
		}
		if (victim.isPlayer() && BoundaryManager.isWithinBoundaryNoZ(getLocation(), "ClanWarsFFA")) {
			return true;
		}
		if (world.getType() == WorldType.STANDARD) {
			if ((isPlayer() && !isInWilderness() && victim.isPlayer())
					|| (victim.isPlayer() && !victim.isInWilderness() && isPlayer())) {
				getActionSender().sendMessage(
						"You or the other player are not in the Wilderness.");
				return false;
			}

			if (victim.isPlayer()) {
				int myWildernessLevel = 1 + (getLocation().getY() - 3520) / 8;
				int victimWildernessLevel = 1 + (victim.getLocation().getY() - 3520) / 8;
				if (getLocation().getY() > 9000) {
					myWildernessLevel -= 800;
					victimWildernessLevel -= 800;
				}
				int combatDifference = 0;
				if (getSkills().getCombatLevel() > victim.getSkills()
						.getCombatLevel()) {
					combatDifference = getSkills().getCombatLevel()
							- victim.getSkills().getCombatLevel();
				} else if (victim.getSkills().getCombatLevel() > getSkills()
						.getCombatLevel()) {
					combatDifference = victim.getSkills().getCombatLevel()
							- getSkills().getCombatLevel();
				}
				if (combatDifference > myWildernessLevel
						|| combatDifference > victimWildernessLevel) {
					if (messages) {
						getActionSender().sendMessage(
								"Your level difference is too great!");
						getActionSender().sendMessage(
								"You need to move deeper into the wilderness.");
					}
					return false;
				}
				// TODO Wilderness/duelContainer arena checks
			}
		} else if (world.getType() == WorldType.DEADMAN_MODE) {
			if (isPlayer() && victim.isPlayer()) {
				return Server.getInjector().getInstance(DeadmanService.class).canAttack(this, (Player) victim);
			}
		}

		return true;
	}

	public void sendAccess(Access access) {
		NumberRange range = access.getRange();
		actionSender.sendGEAccess(range.getStart(), range.getEnd(), access.getRoot(), access.getChild(), access.getValue());
	}

	public int[] getPossibleProductions() {
		return possibleProductions;
	}

	public int getClickPriority() {
		return clickPriority;
	}

	public void setClickPriority(int priority) {
		this.clickPriority = priority;
	}

	private WarriorsGuild wg = new WarriorsGuild(this);

	public WarriorsGuild getWarriorsGuild() {
		return wg;
	}
	
	private AbyssalCreatures ac = new AbyssalCreatures();
	
	public AbyssalCreatures getAbyssalCreatures()
	{
		return ac;
	}
	public Map<Class<?>, Quest<?>> getQuests() {
		return quests;
	}

	//	public void setSelectingOption(boolean b) {
	//		this.selectingOption = b;
	//	}

	Jewellery jewellery = new Jewellery();

	public Jewellery getJewellery() {
		return jewellery;
	}

	RecipeForDisaster rfd = new RecipeForDisaster(this);

	public RecipeForDisaster getRFD() {
		return rfd;
	}

	private long lastHarvest;

	public long getLastHarvest() {
		return lastHarvest;
	}

	public void setLastHarvest(long l) {
		this.lastHarvest = l;
	}


	private MageArena mageArena = new MageArena(this);

	public MageArena getMageArena() {
		return mageArena;
	}

	public void sendMessage(String string) {
		if (getActionSender() != null) {
			getActionSender().sendMessage(string);
		}
	}

	public void setNameColor(String color) {
		this.color = color;
		getUpdateFlags().flag(UpdateFlag.APPEARANCE);
	}

	public String getNameColor() {
		return color;
	}

	public TimedPunishment getPunishment() {
		return punish;
	}
	
	/**
	 * Music
	 */
	public boolean isLoopingMusic = true;
	public int auto = 1;

	private boolean punished;
	public int flowerBetAmount = 0;
	 public int flowerGuess = 0;
	

	public void setPunished(boolean b) {
		this.punished = b;
	}

	public boolean isPunished() {
		return punished;
	}

	public PlayerEntity getDatabaseEntity() {
		return databaseEntity;
	}

	public void setDatabaseEntity(PlayerEntity databaseEntity) {
		this.databaseEntity = databaseEntity;
	}

	public String getPreviousName() {
		return previousName;
	}

	public void setPreviousName(String previousName) {
		this.previousName = previousName;
	}

	public ZulAreth getZulAreth() {
		return zulAreth;
	}
	
	public FlowerGame getFlowerGame() {
		return flowerGame;
	}
	
	public DialogueManager getDialogueManager() {
		return dialogueManager;
	}

	public DialogueChain getDialogueChain() {
		return dialogueChain;
	}

	public void setDialogueChain(DialogueChain dialogueChain) {
		this.dialogueChain = dialogueChain;
	}

	public boolean isIronMan() {
		return isIronMan;
	}

	public void setIsIronMan(boolean isIronMan) {
		this.isIronMan = isIronMan;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public Pet getPet() {
		return pet;
	}

	public int getRegionId() {
		int xCalc = (getLocation().getRegionX() - 6) / 8;
		int yCalc = (getLocation().getRegionY() - 6) / 8;
		return yCalc + (xCalc << 8);
	}

	public void setPestControlInstance(PestControlInstance pestControlInstance) {
		this.pestControlInstance = pestControlInstance;
	}

	public void setMonkeyTime(int monkeyTime) {
		this.monkeyTime = monkeyTime;
	}

	public void setMonkey(ConsumeItemAction.Monkey monkey) {
		this.monkey = monkey;
	}

	public ConsumeItemAction.Monkey getMonkey() {
		return monkey;
	}

	public int getMonkeyTime() {
		return monkeyTime;
	}

	public void setBountyTarget(Player bountyTarget) {
		this.bountyTarget = bountyTarget;
	}

	public Player getBountyTarget() {
		return bountyTarget;
	}

	public Bounty getBounty() {
		return bounty;
	}

	public void setBounty(Bounty bounty) {
		this.bounty = bounty;
	}

	public void setSelectedItem(int selectedItem) {
		this.selectedItem = selectedItem;
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	public float getKDR() {
		int kills = databaseEntity.getBountyHunter().getKills();
		int deaths = databaseEntity.getBountyHunter().getDeaths();
		if (deaths == 0) {
			return kills;
		}
		if (kills != 0 && deaths != 0) {
			return ((float) kills / deaths);
		}
		return 0;
	}

	public String getIP() {
		String ip = getSession().getRemoteAddress().toString();
		String perf = ip.substring(0, ip.indexOf(":")).replaceAll("/", "");
		return perf;
	}

	public void setUltimateIronMan(boolean ultimateIronMan) {
		this.ultimateIronMan = ultimateIronMan;
	}

	public boolean isUltimateIronMan() {
		return ultimateIronMan;
	}
	
	public void setHardcoreIronMan(boolean hardcoreIronMan) {
		this.hardcoreIronMan = hardcoreIronMan;
	}

	public boolean isHardcoreIronMan() {
		return hardcoreIronMan;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public Container getRunePouch() {
		return runePouch;
	}

	public Duel getDuel() {
		return duel;
	}
	private Item item;
	public Item getItems() {
		return item;
	}

	public void setDuel(Duel duel) {
		this.duel = duel;
	}

	public void setDiceTransaction(DiceGameTransaction diceGameTransaction) {
		this.diceGameTransaction = diceGameTransaction;
	}

	public AchievementDiaryManager getAchievementDiaryManager() {
		return achievementDiaryManager;
	}
	
	public void setAchievementDiary(AchievementDiary achievementDiary) {
		this.achievementDiary = achievementDiary;
	}
	
	public AchievementDiary getAchievementDiary() {
		return achievementDiary;
	}
	
	public DiceGameTransaction getDiceGameTransaction() {
		return diceGameTransaction;
	}

	public void playGraphics(int i) {
		// TODO Auto-generated method stub
		
	}

	public void sendMessages(String string, String string2) {
		// TODO Auto-generated method stub
		
	}

}