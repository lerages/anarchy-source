package org.rs2server.rs2.packet;

import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.cache.format.CacheObjectDefinition;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.action.Action;
import org.rs2server.rs2.action.impl.ClimbLadderAction;
import org.rs2server.rs2.content.Doors;
import org.rs2server.rs2.content.MageArenaGodPrayer;
import org.rs2server.rs2.content.RaidingParties;
import org.rs2server.rs2.content.api.GameObjectActionEvent;
import org.rs2server.rs2.content.api.GameObjectSpellEvent;
import org.rs2server.rs2.content.areas.CoordinateEvent;
import org.rs2server.rs2.content.misc.Levers;
import org.rs2server.rs2.content.misc.SpiderWeb;
import org.rs2server.rs2.content.misc.WaterSourceAction;
import org.rs2server.rs2.domain.service.api.HookService;
import org.rs2server.rs2.domain.service.api.PathfindingService;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.domain.service.api.content.ItemService;
import org.rs2server.rs2.domain.service.api.content.LootingBagService;
import org.rs2server.rs2.domain.service.impl.content.BankDepositBoxServiceImpl;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.Mob.InteractionMode;
import org.rs2server.rs2.model.cm.Content;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction;
import org.rs2server.rs2.model.combat.impl.MagicCombatAction.SpellBook;
import org.rs2server.rs2.model.container.Bank;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.container.Inventory;
import org.rs2server.rs2.model.container.PriceChecker;
import org.rs2server.rs2.model.container.Trade;
import org.rs2server.rs2.model.event.ClickEventManager;
import org.rs2server.rs2.model.event.EventListener.ClickOption;
import org.rs2server.rs2.model.gameobject.GameObjectCardinality;
import org.rs2server.rs2.model.gameobject.GameObjectType;
import org.rs2server.rs2.model.map.RegionClipping;
import org.rs2server.rs2.model.map.path.DefaultPathFinder;
import org.rs2server.rs2.model.map.path.ObjectPathFinder;
import org.rs2server.rs2.model.map.path.ObjectPathFinder.Orientation;
import org.rs2server.rs2.model.map.path.PathFinder;
import org.rs2server.rs2.model.map.path.PrimitivePathFinder;
import org.rs2server.rs2.model.map.path.astar.ObjectReachedPrecondition;
import org.rs2server.rs2.model.minigame.barrows.Barrows;
import org.rs2server.rs2.model.minigame.warriorsguild.WarriorsGuild;
import org.rs2server.rs2.model.npc.NPC;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.region.Region;
import org.rs2server.rs2.model.skills.Agility;
import org.rs2server.rs2.model.skills.Agility.Obstacle;
import org.rs2server.rs2.model.skills.AltarAction;
import org.rs2server.rs2.model.skills.CballMakingAction;
import org.rs2server.rs2.model.skills.Cooking;
import org.rs2server.rs2.model.skills.Cooking.CookingItem;
import org.rs2server.rs2.model.skills.Cooking.CookingMethod;
import org.rs2server.rs2.model.skills.Mining;
import org.rs2server.rs2.model.skills.Mining.Rock;
import org.rs2server.rs2.model.skills.Smithing;
import org.rs2server.rs2.model.skills.ThievingAction;
import org.rs2server.rs2.model.skills.ThievingAction.ThievingStalls;
import org.rs2server.rs2.model.skills.ThievingAction.ThievingChests;
import org.rs2server.rs2.model.skills.UnsiredAction;
import org.rs2server.rs2.model.skills.Woodcutting;
import org.rs2server.rs2.model.skills.Woodcutting.Tree;
import org.rs2server.rs2.model.skills.runecrafting.Runecrafting;
import org.rs2server.rs2.model.skills.smithing.DragonfireShieldAction;
import org.rs2server.rs2.model.skills.smithing.Smelting;
import org.rs2server.rs2.model.skills.smithing.SmithingUtils.ForgingBar;
import org.rs2server.rs2.net.ActionSender;
import org.rs2server.rs2.net.Packet;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.util.Misc;

/**
 * Object option packet handler.
 *
 * @author Graham Edgecombe
 */
public class ObjectOptionPacketHandler implements PacketHandler {

    private static final int OPTION_1 = 166, OPTION_2 = 188, OPTION_3 = 218, ITEM_ON_OBJECT = 238, OPTION_EXAMINE = 101,
            OPTION_SPELL = 156;

    /**
     * The array of possible crystal chest rewards.
     */
    private static final Item[] CRYSTAL_CHEST_REWARDS = new Item[]{new Item(372, 5), new Item(995, 5000),
            new Item(554, 50), new Item(555, 50), new Item(556, 50), new Item(557, 50), new Item(558, 50),
            new Item(559, 50), new Item(560, 50), new Item(561, 50), new Item(562, 50), new Item(563, 50),
            new Item(222, 35), new Item(236, 35), new Item(240, 35), new Item(231, 35), new Item(226, 35),
            new Item(454, 100), new Item(1079, 1), new Item(1093, 1), new Item(441, 150), new Item(2364, 3),
            new Item(242, 35)};
    private static final Item[] UNSIRED_REWARD = new Item[]{new Item(4151, 1), new Item(7979, 1), new Item(7979, 1)
            , new Item(13262, 1), new Item(13275, 1), new Item(13274, 1), new Item(13265, 1), new Item(13276, 1)
            , new Item(13277, 1), new Item(13277, 1), new Item(13277, 1), new Item(13277, 1), new Item(13277, 1)};
    private static final Item[] EASY_WILD = new Item[]{new Item(11940, 10), new Item(20581, 1), new Item(20582, 1), new Item(989, 1), new Item(995, 5500), new Item(533, 25), new Item(554, 100), new Item(555, 100), new Item(556, 200), new Item(560, 75), new Item(564, 50), new Item(830, 10), new Item(1333, 1), new Item(1359, 1), new Item(1392, 10), new Item(1522, 200), new Item(1518, 100), new Item(1127, 1), new Item(1093, 1), new Item(1079, 1), new Item(1201, 1), new Item(4129, 1), new Item(200, 5), new Item(202, 10), new Item(204, 10), new Item(206, 10), new Item(224, 25), new Item(222, 15), new Item(226, 10), new Item(378, 25)};
    private static final Item[] MED_WILD = new Item[]{new Item(208, 10), new Item(210, 10), new Item(212, 10), new Item(7460, 1), new Item(7461, 1), new Item(372, 45), new Item(378, 40), new Item(360, 75), new Item(1515, 100), new Item(232, 40), new Item(1319, 1), new Item(7840, 3), new Item(995, 15000), new Item(8836, 30), new Item(11940, 30), new Item(1624, 20), new Item(554, 150), new Item(555, 150), new Item(556, 250), new Item(563, 25), new Item(562, 100), new Item(561, 75), new Item(565, 75), new Item(384, 25), new Item(378, 40), new Item(1215, 1), new Item(1618, 12), new Item(1622, 20), new Item(1620, 20)};
    private static final Item[] HARD_WILD = new Item[]{new Item(208, 20), new Item(210, 20), new Item(212, 20), new Item(7460, 1), new Item(7461, 1), new Item(372, 75), new Item(378, 60), new Item(360, 150), new Item(1515, 150), new Item(232, 200), new Item(1319, 1), new Item(537, 25), new Item(995, 15000), new Item(8836, 80), new Item(11940, 80), new Item(1624, 35), new Item(554, 500), new Item(555, 500), new Item(556, 750), new Item(563, 50), new Item(562, 250), new Item(561, 150), new Item(565, 100), new Item(384, 75), new Item(378, 150), new Item(1215, 1), new Item(1618, 30), new Item(1622, 40), new Item(1620, 40), new Item(1305, 1), new Item(232, 200), new Item(1514, 75)};
    private static final Item[] ELITE_WILD = new Item[]{new Item(208, 30), new Item(210, 30), new Item(212, 30), new Item(7461, 1), new Item(372, 300), new Item(378, 300), new Item(360, 350), new Item(1515, 250), new Item(232, 200), new Item(7158, 1), new Item(537, 75), new Item(995, 35000), new Item(8836, 150), new Item(11940, 150), new Item(1624, 50), new Item(554, 750), new Item(555, 750), new Item(556, 900), new Item(563, 95), new Item(562, 450), new Item(561, 250), new Item(565, 200), new Item(384, 125), new Item(378, 250), new Item(1215, 1), new Item(1618, 45), new Item(1622, 60), new Item(1620, 60), new Item(1305, 1), new Item(232, 200), new Item(11840, 1), new Item(1514, 250), new Item(220, 30), new Item(218, 30), new Item(216, 30), new Item(214, 30)};

    private static final Location GNOME_LADDER_UP_1 = Location.create(2445, 3434, 0);
    private static final Location GNOME_LADDER_UP_2 = Location.create(2444, 3414, 0);
    private static final Location GNOME_LADDER_DOWN_1 = Location.create(2445, 3434, 1);
    private static final Location GNOME_LADDER_DOWN_2 = Location.create(2445, 3415, 1);

    private final HookService hookService;
    private final PathfindingService pathfindingService;
    private final PermissionService permissionService;
    private final ItemService itemService;
    private final LootingBagService lootingBagService;

    public ObjectOptionPacketHandler() {
        this.hookService = Server.getInjector().getInstance(HookService.class);
        this.pathfindingService = Server.getInjector().getInstance(PathfindingService.class);
        this.permissionService = Server.getInjector().getInstance(PermissionService.class);
        this.itemService = Server.getInjector().getInstance(ItemService.class);
        this.lootingBagService = Server.getInjector().getInstance(LootingBagService.class);
    }

    @Override
    public void handle(Player player, Packet packet) {
        if (player.getAttribute("busy") != null) {
            return;
        }
        if (player.isLighting()) {
            return;
        }
        player.getActionSender().removeChatboxInterface();
        if (player.getCombatState().isDead()) {
            return;
        }
        if (player.getInterfaceState().isInterfaceOpen(Equipment.SCREEN)
                || player.getInterfaceState().isInterfaceOpen(Bank.BANK_INVENTORY_INTERFACE)
                || player.getInterfaceState().isInterfaceOpen(Trade.TRADE_INVENTORY_INTERFACE)
                || player.getInterfaceState().isInterfaceOpen(Shop.SHOP_INVENTORY_INTERFACE)
                || player.getInterfaceState().isInterfaceOpen(PriceChecker.PRICE_INVENTORY_INTERFACE)) {
            player.getActionSender().removeInventoryInterface();
        }
        player.getInterfaceState().setOpenShop(-1);
        player.getActionQueue().clearAllActions();
        player.getActionManager().stopAction();
        switch (packet.getOpcode()) {
            case OPTION_1:
                handleOption1(player, packet);
                break;
            case OPTION_2:
                handleOption2(player, packet);
                break;
            case OPTION_3:
                handleOption3(player, packet);
                break;
            case OPTION_EXAMINE:
                handleOptionExamine(player, packet);
                break;
            case ITEM_ON_OBJECT:
                handleOptionItem(player, packet);
                break;
            case OPTION_SPELL:
                handleOptionSpell(player, packet);
                break;
        }
    }

    private void handleOptionSpell(Player player, Packet packet) {
        int spellId = packet.getInt1();
        int objectId = packet.getShort();
        int z = packet.getByteS();
        int x = packet.getShortA();
        int y = packet.getLEShort();
        int f = packet.getLEShort();
        final Location loc = Location.create(x, y, z);
        Region r = player.getRegion();
        final GameObject obj = r.getGameObject(loc, objectId);
        if (obj == null || obj.getId() != objectId) {
            return;
        }
        CacheObjectDefinition def = CacheObjectDefinition.forID(objectId);
        player.getCombatState().setQueuedSpell(null);
        player.resetInteractingEntity();
        player.getActionQueue().clearAllActions();
        player.getActionSender().removeAllInterfaces();
        pathfindingService.travelToObject(player, obj);
        player.faceObject(obj);
        // System.out.println(spellId);
        final Action action = new Action(player, 0) {

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

            @Override
            public void execute() {
                this.stop();
                hookService.post(new GameObjectSpellEvent(player, obj, spellId));
            }
        };
        double dist = player.getLocation().distance(loc);
        if (dist <= 1) {
            player.getActionQueue().addAction(action);
        } else {
            World.getWorld().submitAreaEvent(player,
                    new CoordinateEvent(player, x, y, def.getSizeX(), def.getSizeY(), obj.getDirection()) {

                        @Override
                        public void execute() {
                            player.getActionQueue().addAction(action);
                        }

                    });
        }
    }

    private void handleOptionExamine(Player player, Packet packet) {
        int objectId = packet.getLEShort();

        // CacheObjectDefinition def = CacheObjectDefinition.forID(objectId);
        //
        // if (def != null) {
        // player.getActionSender().sendMessage(def.description.toString());
        // }
    }

    /**
     * Handles the option 1 packet.
     *
     * @param player The player.
     * @param packet The packet.
     */
    private void handleOption1(final Player player, Packet packet) {
        int x = packet.getShort();
        int y = packet.getShort();
        packet.getByteS();
        final int objectId = packet.getLEShort();
        int z = player.getLocation().getZ();
        if (player.getAttribute("temporaryHeight") != null) {
            z = player.getAttribute("temporaryHeight");
        }
        final Location loc = Location.create(x, y, z);
        Region r = player.getRegion();
        final GameObject obj = r.getGameObject(loc, objectId);
        if (obj == null || obj.getId() != objectId) {
            return;
        }
        CacheObjectDefinition def = CacheObjectDefinition.forID(objectId);
        if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
            System.out.println("Object Debug1: " + obj.getId() + ", Location: " + obj.getLocation().toString());// basically
            // if
            // ur
            // frozen
            // u
            // cant
            // click
            // obelisks
            // or
            // ladders
            // or
            // even
            // cut
            // webs
            // in
            // wild
        }

        // if (!player.getCombatState().canMove() && distance > 1) {
        // return;
        // }
        player.getCombatState().setQueuedSpell(null);
        player.resetInteractingEntity();
        player.getActionQueue().clearAllActions();
        player.getActionSender().removeAllInterfaces();// .removeInterface2();

        if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
            player.getActionSender()
                    .sendMessage("<col=880000>Object Option Handler - " + obj.getId() + " Location: " + obj.getLocation() + "");
        }

        // if (objectId == 24051 && x == 2972 && y == 3383) {
        // return;
        // }

        // pretty sure this is the issue. there is a delay though? i never
        // noticed :P
        pathfindingService.travelToObject(player, obj);

        Action action = null;

        action = new Action(player, 0) {
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

            @Override
            public void execute() {
                this.stop();
                if (def.getName().toLowerCase().contains("altar") && !containsGWD(def)
                        && def.getOptions()[0].toLowerCase().contains("pray") && def.getId() != 6552) {
                    player.getSkills().getPrayer().prayAltar(loc);
                    return;
                }

                hookService.post(new GameObjectActionEvent(player, GameObjectActionEvent.ActionType.OPTION_1, obj));

                switch (objectId) {
                    case 25268:
                        if (!player.getInventory().contains(11323)) {
                            player.getInventory().add(new Item(11323));
                            player.sendMessage("You find a strong fishing rod under the bed.");
                        } else {
                            player.sendMessage("You don't find anything under the bed.");
                        }
                        break;
                    case 2407://Lost City Objects
                        player.setTeleportTarget(Location.create(2819, 3374, 0));
                        break;
                    case 2408:
                        player.setTeleportTarget(Location.create(2873, 9750, 0));
                        break;

                    /**
                     * Raids objects start:
                     */
                    case 29789:
                        Location obor6 = player.getLocation();
                        if (obor6.getX() == 3307) {
                            player.setTeleportTarget(Location.create(3307, 5204, 0));
                        } else if (obor6.getX() == 3308) {
                            player.setTeleportTarget(Location.create(3308, 5208, 0));
                        } else if (obor6.getX() == 3311) {
                            player.setTeleportTarget(Location.create(3311, 5275, 0));
                        } else if (obor6.getX() == 3312) {
                            player.setTeleportTarget(Location.create(3312, 5279, 0));
                        } else if (obor6.getY() == 5307) {
                            player.setTeleportTarget(Location.create(3311, 5311, 0));
                        } else if (obor6.getY() == 5311) {
                            player.setTeleportTarget(Location.create(3312, 5307, 0));
                        } else if (obor6.getY() == 5341) {
                            player.setTeleportTarget(Location.create(3312, 5344, 0));
                        } else if (obor6.getY() == 5344) {
                            player.setTeleportTarget(Location.create(3311, 5340, 0));
                        } else if (obor6.getX() == 3310) {
                            player.setTeleportTarget(Location.create(3311, 5373, 0));
                        } else if (obor6.getY() == 5373) {
                            player.setTeleportTarget(Location.create(3310, 5369, 0));
                        } else if (obor6.getY() == 3318) {
                            player.setTeleportTarget(Location.create(3319, 5403, 0));
                        } else if (obor6.getY() == 3319) {
                            player.setTeleportTarget(Location.create(3318, 5399, 0));
                        }
                        break;
                    /**
                     * Raids objects end.
                     */

                    case 17385: //obelisk ladders
                        //if (obj.getLocation().equals(Location.create(3088, 9971))
                        //	|| obj.getLocation().equals(Location.create(2842, 9824))) {
                        final Location surface_location = Location.create(Vector2.of(obj.getLocation()).minus(0, 6399)
                                .plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                        player.getActionQueue().addAction(new ClimbLadderAction(player, surface_location));
                        //} else {
                        //	final Location location = Location.create(Vector2.of(obj.getLocation()).plus(0, 6400)
                        //		.plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                        //	player.getActionQueue().addAction(new ClimbLadderAction(player, location));

                        //}
                        break;
                    case 16679:
                        player.setTeleportTarget(Location.create(player.getLocation().getX(), player.getLocation().getY(), 0));
                        break;
                    case 26762:
                        player.setTeleportTarget(Location.create(3235, 10334, 0));
                        break;
                    case 26763:
                        player.setTeleportTarget(Location.create(3232, 3950, 0));
                        break;
                    case 26709:
                        player.setTeleportTarget(Location.create(2444, 9825, 0));
                        break;
                    case 26710:
                        player.setTeleportTarget(Location.create(2431, 3424, 0));
                        break;
                    case 16539:
                        Agility.tackleObstacle(player, Obstacle.FREMMY_DUNGEON_CREVICE, obj);
                        break;
                    case 14918:
                        Agility.tackleObstacle(player, Obstacle.LAVA_DRAGON_STEP, obj);
                        break;
                    case 10586:
                        Agility.tackleObstacle(player, Obstacle.VARROCK_WALL_CLIMB, obj);
                        break;
                    case 10587:
                        Agility.tackleObstacle(player, Obstacle.VARROCK_CLOTHES_LINE, obj);
                        break;
                    case 10777:
                        player.sendMessage("is this shit working");
                        //Agility.tackleObstacle(player, Obstacle.VARROCK_BALANCE_WALL, obj);
                        break;
                    case 10642:
                        Agility.tackleObstacle(player, Obstacle.VARROCK_LEAP_GAP_1, obj);
                        break;
                    case 29776:
                        RaidingParties.handleRaidParty(player);
                        break;
                        /*case 15477:
                        case 15478:
						case 15479:
						case 15480:
						case 15481:
						case 15482:
							player.getConstruction().enterHouse(true);
							break;*/
                    case 677://corp
                        player.setTeleportTarget(Location.create(2974, 4384, 2));
                        break;
                    case 27979:
                        if (player.getInventory().contains(13445)) {
                            player.getActionSender().sendMessage("A dark power energizes the Essence block.");
                            player.getSkills().addExperience(Skills.RUNECRAFTING, 2.5);
                            player.getInventory().remove(new Item(13445, 1));
                            player.getInventory().add(new Item(13446, 1));
                        }
                        break;

                    case 29315:// Wintertodt Sprouting roots
                        if (System.currentTimeMillis() - player.getLastHarvest() > 600) {
                            player.setLastHarvest(System.currentTimeMillis());
                            if (player.getInventory().add(new Item(20698, 1))) {
                                player.playAnimation(Animation.create(2280));
                                player.getSkills().addExperience(Skills.FARMING, 2);
                                player.getInventory().add(new Item(20527, 1));
                                player.getActionSender().sendMessage("You manage to harvest a Bruma herb...");
                                if (Misc.random(6) == 0) {
                                    World.getWorld().replaceObject(obj, null, 10);// 10 = number of cycles
                                    player.getActionSender().sendMessage("<col=ff0000>The Sprouting roots need to replenish before harvesting again...");
                                }
                            }
                        }
                        break;
                    case 28900:
                        if (!player.getInventory().contains(19685)) {
                            player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
                                    19685, null, "You need a Dark totem.");
                            return;
                        }
                        player.getInventory().remove(new Item(19685, 1));
                        player.setTeleportTarget(Location.create(1693, 9886, 0));
                        //player.getActionSender().sendMessage("<col=ff0000>Welcome to Skotizo");
                        break;
                    case 28686:
                        player.setTeleportTarget(Location.create(2128, 5647, 0));
                        break;
                    case 28687:
                        player.setTeleportTarget(Location.create(2027, 5611, 0));
                        break;
                    case 2640:
                    case 28566:
                    case 26365:
                    case 26364:
                    case 26363:
                    case 26366:
                        player.playAnimation(Animation.create(645));
                        player.getCombatState().resetPrayers();
                        break;
                    case 29150:
                        DialogueManager.openDialogue(player, 20000);
                        break;
                    case 29147:
                        player.getActionSender().sendConfig(439, 1);
                        player.getCombatState()
                                .setSpellBook(MagicCombatAction.SpellBook.ANCIENT_MAGICKS.getSpellBookId());
                        player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
                                4675, null, "An ancient wisdom fills your mind...");
                        break;
                    case 29148:
                        player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
                                9084, null, "Lunar spells activated!");
                        player.getActionSender().sendConfig(439, 2);
                        player.getCombatState()
                                .setSpellBook(MagicCombatAction.SpellBook.LUNAR_MAGICS.getSpellBookId());
                        break;
                    case 29149:
                        player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
                                1381, null, "Your magic book has been changed to the Regular spellbook.");
                        player.getActionSender().sendConfig(439, 0);
                        player.getCombatState()
                                .setSpellBook(MagicCombatAction.SpellBook.MODERN_MAGICS.getSpellBookId());
                        break;
                    case 20877:
                        if (!player.getInventory().contains(995)) {
                            player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT,
                                    995, null, "You need 785 gp to enter this dungeon.");
                            return;
                        }
                        player.getInventory().remove(new Item(995, 785));
                        player.setTeleportTarget(Location.create(2713, 9564, 0));
                        break;
                    case 20878:
                        player.setTeleportTarget(Location.create(2744, 3152, 0));
                        break;
                    case 21731:
                        player.setTeleportTarget(Location.create(2689, 9564, 0));
                        break;
                    case 21732:
                        player.setTeleportTarget(Location.create(2683, 9570, 0));
                        break;
                    case 21734:
                        player.setTeleportTarget(Location.create(2676, 9479, 0));
                        break;
                    case 21735:
                        player.setTeleportTarget(Location.create(2695, 9482, 0));
                        break;
                    case 21738:
                        player.setTeleportTarget(Location.create(2647, 9557, 0));
                        break;
                    case 4485:
                    	player.setTeleportTarget(Location.create(2515, 4632, 0));
                    	break;
                    case 4413:
                    player.getActionQueue().addAction(new ClimbLadderAction(player, Location.create(2515, 4629, 1)));
                    	break;
                    case 1579: //edgeville dungeon trapdoor
                        player.setAttribute("busy", true);
                        player.playAnimation(Animation.create(827));
                        World.getWorld().submit(new Tickable(1) {
                            @Override
                            public void execute() {
                                this.stop();
                                player.setTeleportTarget(Constants.EDGEVILLE_DUNGEON);
                                player.removeAttribute("busy");
                            }

                        });
                        break;
                    case 6552:
                        player.playAnimation(Animation.create(645));
                        player.setAttribute("busy", true);
                        World.getWorld().submit(new Tickable(2) {

                            @Override
                            public void execute() {
                                this.stop();
                                int spellbook = player.getCombatState().getSpellBook();
                                if (spellbook == SpellBook.LUNAR_MAGICS.getSpellBookId()
                                        || spellbook == SpellBook.MODERN_MAGICS.getSpellBookId()) {
                                    player.getActionSender().sendMessage("An ancient wisom fills your mind...");
                                    spellbook = SpellBook.ANCIENT_MAGICKS.getSpellBookId();
                                } else {
                                    player.sendMessage("You revert to modern magic.");
                                    spellbook = SpellBook.MODERN_MAGICS.getSpellBookId();
                                }
                                int config = spellbook == SpellBook.MODERN_MAGICS.getSpellBookId() ? 0 : 1;
                                player.removeAttribute("busy");
                                player.getCombatState().setSpellBook(spellbook);
                                player.getActionSender().sendConfig(439, config);
                            }

                        });
                        break;
                    case 28857:
                        player.setTeleportTarget(Location.create(
                                player.getLocation().getX() - 2, player.getLocation().getY(), 2
                        ));
                        break;
                    case 28858:
                        player.setTeleportTarget(Location.create(
                                player.getLocation().getX() + 2, player.getLocation().getY(), 0
                        ));
                        break;
                    case 29317:
                        player.getInventory().add(new Item(5605, 1));
                        break;

                    case 29319:
                        player.getInventory().add(new Item(590, 1));
                        break;
                    // case 27029:
                    // DialogueManager.openDialogue(player, 660);
                    // break;

                    case 27057:
                        DialogueManager.openDialogue(player, 550);
                        break;

                    case 29318:
                        player.getInventory().add(new Item(1351, 1));
                        break;

                    case 29316:
                        player.getInventory().add(new Item(2347, 1));
                        break;

                    case 8720:
                    case 26813:
                        player.getActionSender().sendInterface(345, false);
                        player.getActionSender().sendConfig(375, 8);
                        player.getActionSender().sendCS2Script(917, new Object[]{-1, -1}, "ii")
                                .sendCS2Script(603,
                                        new Object[]{"Loading...", 22609929, 22609930, 22609931, 22609921,
                                                "<img=35>OS-Anarchy Information"},
                                        "sIIIIs")
                                .sendCS2Script(604, new Object[]{"", 22609928, 22609927}, "IIs")
                                .sendCS2Script(604, new Object[]{"", 22609926, 22609925}, "IIs")
                                .sendCS2Script(604, new Object[]{"", 22609924, 22609923}, "IIs")
									/*.sendCS2Script(609,
											new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623,
													"<img=35>Kronos is #1." },
											"siidfiiI")
									.sendCS2Script(610,
											new Object[] { 22609930, "http://Kronos.com/forums",
													"Click here to visit the forums." },
											"ssI")
									.sendCS2Script(610,
											new Object[] { 22609930, "http://Kronos.com/forums",
													"Click here to visit the store." },
											"ssI")
									.sendCS2Script(610,
											new Object[] { 22609930,
													"http://Kronos.com/forums/index.php?/forum/5-development/",
													"Click here to visit Kronos's server development." },
											"ssI")
									.sendCS2Script(610,
											new Object[] { 22609930,
													"http://Kronos.com/forums/index.php?/forum/6-game-updates/",
													"Click here to visit Kronos's latest game updates." },
											"ssI")
									.sendCS2Script(610,
											new Object[] { 22609930,
													"http://Kronos.com/forums/index.php?/forum/7-behind-the-scenes/",
													"Click here to visit Kronos's behind the scenes" },
											"ssI")
									.sendCS2Script(610,
											new Object[] { 22609930,
													"http://Kronos.com/forums/index.php?/forum/8-site-updates/",
													"Click here to visit Kronos's site updates" },
											"ssI")
									.sendCS2Script(609, new Object[] { 22609930, 5, 12, 495, 495, 0, 16750623, "" },
											"siidfiiI")*/
                                .sendCS2Script(618, new Object[]{1, 22609929, 22609931, 22609930}, "III1")
                                // .sendCS2Script(604, new
                                // Object[]{"History", 22609928, 22609927},
                                // "IIs")
                                // .sendCS2Script(604, new
                                // Object[]{"Refresh", 22609926, 22609925},
                                // "IIs")
                                .sendString(345, 2, "<img=35>Website: http://www.OS-Anarchy.com")
                        // .sendCS2Script(604, new Object[]{"Vote",
                        // 22609924, 22609923}, "IIs")
                        ;
                        break;
                    case 29332:
                        player.setTeleportTarget(Location.create(1630, 3968, 0));
                        break;
                    case 29777:
                        int pane = player.getAttribute("tabmode");
                        int tabId = pane == 548 ? 65 : pane == 161 ? 56 : 56;
                        player.getActionSender().sendSidebarInterface(tabId, 500);// Raid
                        // Sidebar
                        // interface
                        player.getActionSender().sendConfig(1055, 8768);// Changes
                        // tab
                        // icon
                        // sprite
                        player.getActionSender().sendConfig(1430, 1336071168);// Raid
                        // Preferred
                        // combat
                        // level
                        // 90
                        // +
                        // Preferred
                        // skill
                        // total
                        // of
                        // 1000
                        player.getActionSender().sendConfig(1432, 1);// Raid
                        // party
                        // size
                        player.getActionSender().sendMessage("<col=E172E5>A new test raid has begun!");
                        player.setTeleportTarget(Location.create(3299, 5188, 0));

                      //  World.getWorld().sendWorldMessage(
                            //    "<col=ff0000>News: " + player.getName() + " Has just entered the raids dungeon.");
                        break;
                    case 29778:
                        player.setTeleportTarget(Location.create(1234, 3571, 0));
                        player.getActionSender().closeAll();
                        break;
                    case 29312:
                        if (!player.getInventory().contains(590)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need a tinderbox.");
                            return;
                        }
                        // player.getSkills().addExperience(Skills.FIREMAKING,
                        // 110);
                        if (Misc.random(1) == 0) {
                            player.playAnimation(Animation.create(733));
                            World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 29314,
                                    obj.getType(), obj.getDirection(), false), 60);
                            // World.getWorld().replaceObject(obj, 29314,
                            // 10);//10 = number of cycles
                            player.getActionSender().sendMessage("<col=ff0000>You light the brazier.");
                            player.getSkills().addExperience(Skills.FIREMAKING, 90);
                        }
                        break;
                    case 29314:
                        if (!player.getInventory().contains(20695)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need a Bruma root.");
                            return;
                        }
                        player.getSkills().addExperience(Skills.FIREMAKING, 110);
                        player.getInventory().remove(new Item(20695, 1));
                        player.getInventory().add(new Item(20527, 18));
                        if (Misc.random(6) == 0) {
                            World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 29312,
                                    obj.getType(), obj.getDirection(), false), 300000);
                            // World.getWorld().replaceObject(obj, null,
                            // 10);//10 = number of cycles
                            player.getActionSender()
                                    .sendMessage("<col=ff0000>You feed the bruma root to the brazier.");
                        }
                        break;
                    case 28579://mine wall 1
                        if (!player.getInventory().contains(1755)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need a chisel.");
                            return;
                        }
                        if (!player.getInventory().contains(13573)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need something to blast the ore out!");
                            return;
                        }
                        player.getSkills().addExperience(Skills.CRAFTING, 8510);
                        player.getInventory().remove(new Item(454, 1));
                        player.playAnimation(Animation.create(7199));
                        player.getActionSender().sendMessage("You chipped away at the wall, and placed dynamite into it!");
                        if (Misc.random(2) == 0) {
                            World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 28583,
                                    obj.getType(), obj.getDirection(), false), 30);
                        }
                        break;
                    case 28580:// mine wall 2
                        if (!player.getInventory().contains(1755)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need a chisel.");
                            return;
                        }
                        if (!player.getInventory().contains(13573)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need something to blast the ore out!");
                            return;
                        }
                        player.getSkills().addExperience(Skills.CRAFTING, 8510);
                        player.getInventory().add(new Item(454, 1));
                        player.playAnimation(Animation.create(7199));
                        player.getActionSender().sendMessage("You chipped away at the wall, and placed dynamite into it!");
                        if (Misc.random(2) == 0) {
                            World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 28583,
                                    obj.getType(), obj.getDirection(), false), 30);
                        }
                        break;
                    case 28582://Blast mining crevice into dynamite
                        if (!player.getInventory().contains(1755)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need a chisel.");
                            return;
                        }
                        if (!player.getInventory().contains(13573)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need something to blast the ore out!");
                            return;
                        }
                        player.getInventory().remove(new Item(13573, 1));
                        player.playAnimation(Animation.create(833));
                        player.getActionSender().sendMessage("You placed dynamite in the cavity.");
                        if (Misc.random(4) == 0) {
                            World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 28583,
                                    obj.getType(), obj.getDirection(), false), 20);
                        }


                    case 28583://Blast mining lighting dynamite
                        if (!player.getInventory().contains(590)) {
                            player.getActionSender().sendMessage("<col=ff0000>You need a tinderbox to light this!");
                            return;
                        }
                        player.getInventory().remove(new Item(454, 5));
                        player.getInventory().remove(new Item(13573, 1));

                        player.playAnimation(Animation.create(833));
                        player.playGraphics(Graphic.create(157));
                        player.getActionSender().sendMessage("You carefully light the dynamite.");
                        World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 28588,
                                obj.getType(), obj.getDirection(), false), 20);

                        break;
                    case 28588://Blast mining shattered
                        if (Misc.random(2) == 0) {
                            World.getWorld().replaceObject(obj, new GameObject(obj.getLocation(), 28580,
                                    obj.getType(), obj.getDirection(), false), 5);
                        }
                        break;
                    case 26273:
                        player.getActionSender().sendInterface(206, false);
                        // player.getActionSender().sendAccessMask(1054,
                        // 206, 1, 0, 100);
                        break;
                    case 6948:
                    case 29104:
                    case 26254:
                        if (permissionService.is(player, PermissionService.PlayerPermissions.ULTIMATE_IRON_MAN)) {
                            player.getActionSender().sendMessage("You are an ultimate ironman and cannot use the bank deposit box.");
                            player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, 12813, null, "You are an ultimate ironman and cannot use the bank deposit box.");
                            return;
                        } else {
                            BankDepositBoxServiceImpl bankDeposit = Server.getInjector()
                                    .getInstance(BankDepositBoxServiceImpl.class);
                            bankDeposit.openDepositBox(player);
                        }
                        break;

                    case 29486:
                    case 29487:
                        player.setTeleportTarget(Location.create(3091, 9814, 0));
                        break;
                    case 29488:
                    case 29489:
                        player.setTeleportTarget(Location.create(3096, 9832, 0));
                        break;
                    case 29491:
                        Location obor = player.getLocation();
                        if (obor.getY() == 9804) {
                            player.setTeleportTarget(Location.create(3092, 9807, 0));
                        } else if (obor.getY() == 9807) {
                            player.setTeleportTarget(Location.create(3092, 9804, 0));
                        }
                        break;

                    case 27095:
                        player.setTeleportTarget(Location.create(3327, 4751, 0).transform(Misc.random(1), 0, 0));
                        break;
                    case 26646:
                        player.resetInteractingEntity();
                        player.getCombatState().getDamageMap().reset();
                        player.getCombatState().resetPrayers();
                        player.getSkills().resetStats();
                        player.removeAttribute("venom");
                        player.venomDamage = 6;
                        player.getActionQueue().clearAllActions();
                        player.getDatabaseEntity().getCombatEntity().setVenomDamage(0);
                        player.getDatabaseEntity().getPlayerSettings().setTeleBlockTimer(0);
                        player.getDatabaseEntity().getPlayerSettings().setTeleBlocked(false);
                        player.getUpdateFlags().flag(UpdateFlags.UpdateFlag.APPEARANCE);
                        player.getActionSender().removeWalkableInterface();
                        player.getCombatState().setPoisonDamage(0, null);
                        player.setTeleportTarget(Entity.HOME_LOCATION);
                        break;
                    /** Lighthouse objects **/
                    case 4568:
                        if (obj.getLocation().equals(Location.create(2506, 3640, 0))) {
                            player.setTeleportTarget(Location.create(2505, 3641, 1));
                        }
                        break;
                    case 4569:
                        if (obj.getLocation().equals(Location.create(2506, 3640, 1))) {
                            player.setTeleportTarget(Location.create(2505, 3641, 2));
                        }
                        break;
                    case 4570:
                        if (obj.getLocation().equals(Location.create(2506, 3641, 2))) {
                            player.setTeleportTarget(Location.create(2505, 3641, 1));
                        }
                        break;
                    case 534:
                        if (obj.getLocation().equals(Location.create(3748, 5760, 0))) {
                            player.setTeleportTarget(Location.create(2356, 9782, 0));
                        }
                        break;
                    case 154:
                        if (obj.getLocation().equals(Location.create(2356, 9783, 0))) {
                            player.setTeleportTarget(Location.create(3748, 5761, 0));
                        }
                        break;
                    case 535:
                        if (obj.getLocation().equals(Location.create(3722, 5798, 0))) {
                            player.setTeleportTarget(Location.create(3677, 5775, 0));
                        }
                        break;
                    case 536:
                        if (obj.getLocation().equals(Location.create(3678, 5775, 0))) {
                            player.setTeleportTarget(Location.create(3723, 5798, 0));
                        }
                        break;
                    case 10562:
                    case 4483:
                    case 10058:
                    case 29321:
                    case 28861:
                    case 30087:
                    case 16642:
                    case 21301:
                        Bank.open(player);
                        break;
                    case 10061:
                        player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null,
                                "This feature is not enabled at this time.");
                        break;
                    case 26502:
                        Location gwdLoc = player.getLocation();
                        if (gwdLoc.getY() == 5294) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX(), gwdLoc.getY() + 2, loc.getZ()));
                        } else if (gwdLoc.getY() == 5296) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX(), gwdLoc.getY() - 2, loc.getZ()));
                        }
                        break;
                    case 26503:
                        gwdLoc = player.getLocation();
                        if (gwdLoc.getX() == 2862) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX() + 2, gwdLoc.getY(), loc.getZ()));
                        } else if (gwdLoc.getX() == 2864) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX() - 2, gwdLoc.getY(), loc.getZ()));
                        }
                        break;
                    case 26504:
                        gwdLoc = player.getLocation();
                        if (gwdLoc.getX() == 2909) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX() - 2, gwdLoc.getY(), loc.getZ()));
                        } else if (gwdLoc.getX() == 2907) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX() + 2, gwdLoc.getY(), loc.getZ()));
                        }
                        break;
                    case 26505:
                        gwdLoc = player.getLocation();
                        if (gwdLoc.getY() == 5333) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX(), gwdLoc.getY() - 2, loc.getZ()));
                        } else if (gwdLoc.getY() == 5331) {
                            player.setTeleportTarget(Location.create(gwdLoc.getX(), gwdLoc.getY() + 2, loc.getZ()));
                        }
                        break;

                    case 11735:
                        if (player.getX() >= 3188 && player.getX() <= 3193 && player.getY() == 3961
                                || player.getY() == 3962) {
                            Bank.open(player);
                        }
                        break;
                    case 20925:
                        if (obj.getLocation().equals(Location.create(2611, 3394, 0))) {
                            if (player.getSkills().getLevel(Skills.FISHING) < 68) {
                                player.getActionSender()
                                        .sendMessage("You need a Fishing level of 68 to enter the Fishing Guild.");
                                return;
                            }
                            player.setAttribute("busy", true);
                            int yOff = player.getY() == 3394 ? -1 : 1;
                            GameObject replace = new GameObject(obj.getLocation(), 20925, obj.getType(), 2, false);
                            replace.setLocation(obj.getLocation());
                            player.getActionSender().removeObject(obj);
                            player.getActionSender().sendObject(replace);
                            Agility.forceWalkingQueue(player, player.getWalkAnimation(), player.getX(),
                                    player.getY() + yOff, 0, 1, true);
                            World.getWorld().submit(new Tickable(2) {

                                @Override
                                public void execute() {
                                    this.stop();
                                    player.getActionSender().removeObject(replace);
                                    player.getActionSender().sendObject(obj);
                                }
                            });
                        }
                        break;
                    case 5167:
                        if (obj.getLocation().equals(Location.create(3578, 3527, 0))) {
                            player.setTeleportTarget(Location.create(3577, 9927, 0));
                        }
                        break;
						/* Dwarf mine Ladder */
                    case 11867:
                        if (obj.getLocation().equals(Location.create(3019, 3450, 0))) {
                            final Location location = Location.create(Vector2.of(obj.getLocation()).plus(0, 6400)
                                    .plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));
                        }
                        break;
						/* Mining guild ladder */
                    case 7451:
                        if (player.getSkills().getLevel(Skill.MINING.getId()) >= 60) {
                            final Location location = Location.create(Vector2.of(obj.getLocation()).plus(0, 6400)
                                    .plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));

                        } else {
                            DialogueManager.openDialogue(player, 103364);
                            player.sendMessage("You need a mining level 60 to go down there.");
                        }
                        break;
                    case 17387:
                        if (obj.getLocation().equals(Location.create(3578, 9927, 0))) {
                            player.setTeleportTarget(Location.create(3579, 3527, 0));
                        } else if (obj.getLocation().equals(Location.create(2892, 9907))) {
                            final Location location = Location.create(2893, 3507);
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));
                        }
                        break;
						/* Burthrope Games Room Stairs */
                    case 4622:
                        if (obj.getLocation().equals(Location.create(2207, 4935, 0))) {
                            player.setTeleportTarget(Location.create(2899, 3565));
                        }
                        break;
                    case 4624:
                        if (obj.getLocation().equals(Location.create(2899, 3566, 0))) {
                            player.setTeleportTarget(Location.create(2208, 4938));
                        }
                        break;

                    case 10068:
                        if (player.getContentManager().getActiveContent(Content.ZULRAH) != null) {
                            return;
                        }
                        player.getContentManager().start(Content.ZULRAH);
                        break;
                    case 17384:
                        if (obj.getLocation().equals(Location.create(2892, 3507))
                                || obj.getLocation().equals(Location.create(3116, 3452))
                                || obj.getLocation().equals(Location.create(2842, 3424))) {
                            final Location location = Location.create(Vector2.of(obj.getLocation()).plus(0, 6400)
                                    .plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));
                        }
                        break;

						/* Mining guild ladder down */
                    case 7452:
                        if (obj.getLocation().equals(Location.create(3020, 3339, 0))
                                || obj.getLocation().equals(Location.create(3019, 3340, 0))
                                || obj.getLocation().equals(Location.create(3018, 3339, 0))
                                || obj.getLocation().equals(Location.create(3019, 3338, 0))) {
                            final Location location = Location.create(Vector2.of(obj.getLocation()).plus(0, 6400)
                                    .plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));
                        }
                        break;
                    case 10229:
                    	final Location dag_location = Location.create(1910, 4367);
                        player.getActionQueue().addAction(new ClimbLadderAction(player, dag_location));
                        break;
                    case 2641:
                        player.setTeleportTarget(
                                Location.create(player.getX(), player.getY(), player.getZ() == 0 ? 1 : 0));
                        break;
                    case 26720:
                    case 26721:
                        World.getWorld().unregister(obj, true);
                        RegionClipping.removeClipping(obj);
                        break;
                    case 11794:
                        if (obj.getLocation().equals(Location.create(3551, 9689, 0))) {
                            player.setTeleportTarget(Constants.BARROWS);
                            player.getActionSender().removeChatboxInterface();
                            World.getWorld().submit(new Tickable(1) {
                                @Override
                                public void execute() {
                                    this.stop();
                                    player.getActionSender().updateMinimap(ActionSender.NO_BLACKOUT);
                                }
                            });
                        }
                        break;
                    case 881:
                        World.getWorld().unregister(obj, true);
                        World.getWorld().register(
                                new GameObject(obj.getLocation(), 882, obj.getType(), obj.getDirection(), false));
                        break;
                    case 882:
                        player.setTeleportTarget(Location.create(3237, 9858));
                        break;
                    case 11806:
                        player.setTeleportTarget(Location.create(3236, 3458, 0));
                        break;
                    case 27029:
                        if (!player.getInventory().contains(13273)) {
                            player.getActionSender().sendMessage("You have no buisiness in the Font.");
                            return;
                        }
                        player.setAttribute("busy", true);
                        player.playAnimation(Animation.create(827));
                        player.playGraphics(Graphic.create(1294));
                        World.getWorld().submit(new Tickable(1) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");
                                player.getInventory().remove(new Item(13273, 1));
                                Inventory.addDroppable(player,
                                        UNSIRED_REWARD[Misc.random(UNSIRED_REWARD.length - 1)]);
                            }

                        });
                        break;
                    case 27785:
                        if (!player.getInventory().contains(13273)) {
                            player.getActionSender().sendMessage("Welcome to Great Kourend.");
                            return;
                        }
                        player.setAttribute("busy", true);
                        World.getWorld().submit(new Tickable(1) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");

                            }

                        });
                        break;
                    case 172:
                        if (!player.getInventory().contains(989)) {
                            player.getActionSender().sendMessage("The chest won't open without a key.");
                            return;
                        }
                        player.setAttribute("busy", true);
                        player.playAnimation(Animation.create(833));
                        World.getWorld().submit(new Tickable(1) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");
                                player.getInventory().remove(new Item(989, 1));
                                Inventory.addDroppable(player, new Item(1631, 1));
                                Inventory.addDroppable(player,
                                        CRYSTAL_CHEST_REWARDS[Misc.random(CRYSTAL_CHEST_REWARDS.length - 1)]);
                                Inventory.addDroppable(player,
                                        CRYSTAL_CHEST_REWARDS[Misc.random(CRYSTAL_CHEST_REWARDS.length - 1)]);
                            }

                        });
                        break;
                    case 27271:
                        if (!player.getInventory().contains(6792)) {
                            player.getActionSender().sendMessage("The chest won't open without a key.");
                            return;
                        }
                        player.setAttribute("busy", true);
                        player.getActionSender().sendMessage("You open the chest with your key...");
                        player.playAnimation(Animation.create(881));
                        World.getWorld().submit(new Tickable(1) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");
                                player.getInventory().remove(new Item(6792, 1));
                                Inventory.addDroppable(player,
                                        EASY_WILD[Misc.random(EASY_WILD.length - 1)]);
                                Inventory.addDroppable(player,
                                        EASY_WILD[Misc.random(EASY_WILD.length - 1)]);
                            }

                        });
                        break;
                    case 27282:
                        if (!player.getInventory().contains(2400)) {
                            player.getActionSender().sendMessage("The chest won't open without a key.");
                            return;
                        }
                        player.setAttribute("busy", true);
                        player.getActionSender().sendMessage("You open the chest with your key...");
                        player.playAnimation(Animation.create(881));
                        World.getWorld().submit(new Tickable(1) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");
                                player.getInventory().remove(new Item(2400, 1));
                                Inventory.addDroppable(player,
                                        MED_WILD[Misc.random(MED_WILD.length - 1)]);
                                Inventory.addDroppable(player,
                                        MED_WILD[Misc.random(MED_WILD.length - 1)]);
                            }

                        });
                        break;
						/*case 27284:
							if (!player.getInventory().contains(2399)) {
								player.getActionSender().sendMessage("The chest won't open without a key.");
								return;
							}
							player.setAttribute("busy", true);
							player.getActionSender().sendMessage("You open the chest with your key...");
							player.playAnimation(Animation.create(881));
							World.getWorld().submit(new Tickable(1) {

								@Override
								public void execute() {
									this.stop();
									player.removeAttribute("busy");
									player.getInventory().remove(new Item(2399, 1));
									Inventory.addDroppable(player,
											HARD_WILD[Misc.random(HARD_WILD.length - 1)]);
									Inventory.addDroppable(player,
											HARD_WILD[Misc.random(HARD_WILD.length - 1)]);
								}

							});
							break;*/
                    case 27288:
                        if (!player.getInventory().contains(6754)) {
                            player.getActionSender().sendMessage("The chest won't open without a key.");
                            return;
                        }
                        player.setAttribute("busy", true);
                        player.getActionSender().sendMessage("You open the chest with your key...");
                        player.playAnimation(Animation.create(881));
                        World.getWorld().submit(new Tickable(1) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");
                                player.getInventory().remove(new Item(6754, 1));
                                Inventory.addDroppable(player,
                                        ELITE_WILD[Misc.random(ELITE_WILD.length - 1)]);
                                Inventory.addDroppable(player,
                                        ELITE_WILD[Misc.random(ELITE_WILD.length - 1)]);
                            }

                        });
                        break;
                    case 26374:
                        NPC npc = player.getAttribute("currentlyFightingBrother");
                        if (npc != null && npc.getSkills().getLevel(Skills.HITPOINTS) > 0) {
                            World.getWorld().unregister(npc);
                            player.removeAttribute("currentlyFightingBrother");
                        }
                        player.setTeleportTarget(Constants.BARROWS);
                        World.getWorld().unregister(obj, true);
                        break;
                    case 25938:
                    case 25939:
                        Location spinningDown = Location.create(2715, 3470, 0);
                        Location spinningUp = Location.create(2715, 3470, 1);
                        if (obj.getLocation().equals(spinningDown) || obj.getLocation().equals(spinningUp)) {
                            player.setTeleportTarget(Location.create(2715, 3471, player.getZ() == 1 ? 0 : 1));
                        }
                        break;
                    case 11834:
                        player.getFightCave().stop();
                        break;
                    case 23969:
                        if (obj.getLocation().equals(Location.create(3059, 9776))) {
                            player.setTeleportTarget(Location.create(3061, 3376));
                        }
                        break;
                    case 7434:// clock at home
                        player.getActionSender().sendMessage("There is currently 0:00 of Boosted Experience left on the clock.");
                        break;
                    case 29625:// rack in house
                        player.getActionSender().sendMessage("It doensn't look like I can wear this..");
                        break;
                    case 16664:
                        if (obj.getLocation().equals(Location.create(3058, 3376))) {
                            player.setTeleportTarget(Location.create(3058, 9776));
                        }
                        break;
                    case 12356:
                        player.getRFD().stop();
                        break;
                    case 24306:
                    case 24309:
                        if (WarriorsGuild.IN_GAME.contains(player)) {
                            player.getWarriorsGuild().handleDoorClick(WarriorsGuild.GAME_DOOR_1);
                        } else {
                            DialogueManager.openDialogue(player, 2461);
                        }
                        break;
                    case 16675:// Tree gnome bank (up)
                        if (obj.getLocation().equals(GNOME_LADDER_UP_1)) {
                            player.setTeleportTarget(Location.create(2445, 3433, 1));
                        } else if (obj.getLocation().equals(GNOME_LADDER_UP_2)) {
                            player.setTeleportTarget(Location.create(2445, 3416, 1));
                        }
                        break;
                    case 16677:// Tree gnome bank (down)
                        if (obj.getLocation().equals(GNOME_LADDER_DOWN_1)) {
                            player.setTeleportTarget(Location.create(2445, 3433, 0));
                        } else if (obj.getLocation().equals(GNOME_LADDER_DOWN_2)) {
                            player.setTeleportTarget(Location.create(2445, 3416, 0));
                        }
                        break;
                    case 4879:// ape atoll dungeon trapdoor
                        player.setTeleportTarget(Location.create(2807, 9201, 0));
                        break;
                    case 4881:// ape atoll dungeon rope up
                        player.setTeleportTarget(Location.create(2806, 2785, 0));
                        break;
                    case 9582:// crafting guild up
                        player.setTeleportTarget(Location.create(2933, 3282, 1));
                        break;
                    case 9584:// crafcting guild down
                        player.setTeleportTarget(Location.create(2932, 3281, 0));
                        break;
                    case 19003:// stonghold lvl 0 portal
                        player.setTeleportTarget(Location.create(1860, 5244, 0));
                        break;
                    case 10596:// asgarnian dungeon to wyverns
                        player.setTeleportTarget(Location.create(3056, 9555, 0));
                        break;
                    case 10595:// sgarnian dungeon to ice
                        player.setTeleportTarget(Location.create(3056, 9562, 0));
                        break;
                    case 1738:// asgarnia ice dungeon down
                        player.setTeleportTarget(Location.create(3008, 9550, 0));
                        break;
                    case 20786:// stonghold lvl 1 portal
                        player.setTeleportTarget(Location.create(2039, 5240, 0));
                        break;
                    case 10230:// down to dag kings
                        player.setTeleportTarget(Location.create(2900, 4449, 0));
                        break;
                    case 20785:// stonghold lvl 1 ladder
                        player.setTeleportTarget(Location.create(2042, 5245, 0));
                        break;
                    case 19005:// stonghold lvl 2 portal
                        player.setTeleportTarget(Location.create(2120, 5258, 0));
                        break;
                    case 23705:// stonghold lvl 2 ladder
                        player.setTeleportTarget(Location.create(2123, 5252, 0));
                        break;
                    case 23707:// stonghold lvl 3 portal
                        player.setTeleportTarget(Location.create(2365, 5212, 0));
                        break;
                    case 23732:// stonghold lvl 3 ladder
                        player.setTeleportTarget(Location.create(2123, 5252, 0));
                        break;
                    case 11807:// stairs up super zone
                        player.setTeleportTarget(Location.create(1614, 3665, 1));
                        break;
                    case 11799:// stairs down super zone
                        player.setTeleportTarget(Location.create(1618, 3666, 0));
                        break;
                    case 16671:// warrior guild level 1
                    case 16672:// warrior guild level 2
                        player.setTeleportTarget(Location.create(player.getX(), player.getY(), player.getZ() + 1));
                        break;
                    case 16673:
                        player.setTeleportTarget(Location.create(player.getX(), player.getY(), player.getZ() - 1));
                        break;
                    case 24303:// warrior guild level 3
                        player.setTeleportTarget(Location.create(player.getX(), player.getY(), player.getZ() - 1));
                        break;
                    case 11835:
                        player.setTeleportTarget(Constants.TZHAAR_CITY);
                        break;
                    case 11836:
                        player.setTeleportTarget(Location.create(2862, 9572, 0));
                        break;
                    case 11441:
                        player.setTeleportTarget(Constants.KARAMJA_VOLCANO_BOTTOM);
                        break;
                    case 18969:
                        player.setTeleportTarget(Constants.KARAMJA_VOLCANO_TOP);
                        break;
                    case 2120:
                    case 2119:
                        Location toLoc = player.getLocation().getZ() == 2
                                ? Location.create(3412, player.getLocation().getY(), 1)
                                : Location.create(3417, player.getLocation().getY(), 2);
                        player.setTeleportTarget(toLoc);
                        break;
                    case 2114:
                    case 2118:
                        toLoc = player.getLocation().getZ() == 1
                                ? Location.create(3438, player.getLocation().getY(), 0)
                                : Location.create(3433, player.getLocation().getY(), 1);
                        player.setTeleportTarget(toLoc);
                        break;
                    case 2100:
                        player.doorOpenClose(obj, 0, 1, 0);
                        break;
                    case 11726:
                        if (player.getDatabaseEntity().getPlayerSettings().isTeleBlocked()) {
                            player.getActionSender()
                                    .sendMessage("You can not enter there while teleblocked.");
                            return;
                        }
                        if (obj.getLocation().equals(Location.create(3190, 3957, 0))) {
                            if (player.getLocation().equals(Location.create(3190, 3958, 0))
                                    || player.getLocation().equals(Location.create(3190, 3957, 0))) {
                                int yOff = player.getY() == 3958 ? -1 : 1;
                                player.doorOpenClose(obj, 0, yOff, 0);
                            }
                        } else if (obj.getLocation().equals(Location.create(3191, 3963, 0))) {
                            if (player.getLocation().equals(Location.create(3191, 3962, 0))
                                    || player.getLocation().equals(Location.create(3191, 3963, 0))) {
                                int yOff = player.getY() == 3963 ? -1 : 1;
                                player.doorOpenClose(obj, 0, yOff, 0);
                            }
                        }
                        break;
                    case 2102:
                        int yOff = player.getLocation().getY() == 3556 ? -1 : 1;
                        player.doorOpenClose(obj, 0, yOff, 0);
                        break;
                    case 7111:
                    case 7108:
                        if (obj.getLocation().equals(Location.create(2577, 9882))
                                || obj.getLocation().equals(Location.create(2576, 9882))) {
                            player.setTeleportTarget(
                                    Location.create(player.getLocation().getX(), player.getLocation().getY() == 9883
                                            ? player.getLocation().getY() - 1 : player.getLocation().getY() + 1));
                        } else if (obj.getLocation().equals(Location.create(2577, 9884))
                                || obj.getLocation().equals(Location.create(2576, 9884))) {
                            player.setTeleportTarget(
                                    Location.create(player.getLocation().getX(), player.getLocation().getY() == 9885
                                            ? player.getLocation().getY() - 1 : player.getLocation().getY() + 1));
                        } else if (obj.getLocation().equals(Location.create(2564, 9881))
                                || obj.getLocation().equals(Location.create(2565, 9881))) {
                            player.setTeleportTarget(
                                    Location.create(player.getLocation().getX(), player.getLocation().getY() == 9882
                                            ? player.getLocation().getY() - 1 : player.getLocation().getY() + 1));
                        }
                        break;
                    case 7158:
                        // Doors.handleLumbridgeGate(player, obj);
                        break;

                    case 14880:
                        if (obj.getLocation() == Location.create(3209, 3216, 0)) {
                            player.setTeleportTarget(Location.create(3210, 9616, 0));
                        }
                        player.sendMessage("lol");
                        break;
                    case 16680:
                        if (obj.getLocation().equals(Location.create(3088, 3571))) {
                            final Location location = Location.create(Vector2.of(obj.getLocation()).plus(0, 6400)
                                    .plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));
                        }
                        break;
						
						/*case 17385:
							 Mining guild ladder up 
							if (obj.getLocation().equals(Location.create(3020, 9739))
									|| obj.getLocation().equals(Location.create(3019, 9740))
									|| obj.getLocation().equals(Location.create(3018, 9739))
									|| obj.getLocation().equals(Location.create(3019, 9738))
									|| obj.getLocation().equals(Location.create(3116, 9852))
									|| obj.getLocation().equals(Location.create(3088, 9971))
									|| obj.getLocation().equals(Location.create(2842, 9824))) {
								final Location location = Location.create(Vector2.of(obj.getLocation()).minus(0, 6400)
										.plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
								player.getActionQueue().addAction(new ClimbLadderAction(player, location));
								break;
							}*/
							/*if (obj.getLocation().equals(Location.create(3097, 9867))) {
								final Location location = Location.create(Vector2.of(obj.getLocation()).minus(0, 6399)
										.plus(GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
								player.getActionQueue().addAction(new ClimbLadderAction(player, location));
							}*/
							
							/*if (obj.getLocation().equals(Location.create(3209, 9616, 0))) {
								player.playAnimation(Animation.create(828));
								player.getActionSender().sendMessage("You climb the ladder.");
								World.getWorld().submit(new Tickable(2) {
									@Override
									public void execute() {
										this.stop();
										player.setTeleportTarget(Location.create(3210, 3216, 0));
									}

								});
							}
							break;*/

                    case 23271:
                        Agility.tackleObstacle(player, Obstacle.WILDERNESS_DITCH, obj);
                        break;
                    case 28849:
                        Agility.tackleObstacle(player, Obstacle.TAVERLY_FENCE, obj);
                        break;
                    case 16529:
                        player.playAnimation(Animation.create(2589));
                        player.setTeleportTarget(Location.create(3142, 3513));
                        player.playAnimation(Animation.create(2591));
                        break;
                    case 16530:
                        player.playAnimation(Animation.create(2589));
                        player.setTeleportTarget(Location.create(3137, 3516));
                        player.playAnimation(Animation.create(2591));
                        break;
                    case 19044:
                    	final Location LADDER_LOCATION_BOTTOM = Location.create(3755, 5672, 0);
                    	final Location LADDER_LOCATION_TOP = Location.create(3755, 5675, 0);
                    	
                    	if (player.getSkills().getLevel(Skill.MINING.getId()) < 72) {
    						player.getActionSender().sendMessage("You need a mining level of 72 to mine at the upper level.");
    						break;
    					}
    					
    					if (player.getLocation().getY() > 5674){
    						player.setTeleportTarget(LADDER_LOCATION_BOTTOM);
    					} else if (player.getLocation().getY() < 5674) {
    						player.getActionQueue().addAction(new ClimbLadderAction(player, LADDER_LOCATION_TOP));
    					} else {
    						player.sendMessage("invalid");
    					}
                    	break;
                    	
                    case 2623:
                        int xOff = 0;
                        yOff = 0;
                        if (player.getLocation().equals(Location.create(2923, 9803, 0))) {
                            xOff = 1;
                        } else if (player.getLocation().equals(Location.create(2924, 9803, 0))) {
                            xOff = -1;
                        }
                        player.doorOpenClose(obj, xOff, yOff, 1);
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        if (player.getAttribute("cannon") != null) {
                            Cannon cannon = (Cannon) player.getAttribute("cannon");
                            if (cannon.getGameObject().getLocation().equals(loc)) {
                                if (objectId == 6) {
                                    int cannonBalls = cannon.getCannonBalls();
                                    if (cannonBalls < 30) {
                                        int newCannonBalls = 30 - cannonBalls;
                                        if (newCannonBalls > 30) {
                                            newCannonBalls = 30;
                                        }
                                        if (newCannonBalls + cannonBalls > 30) {
                                            newCannonBalls = 30 - cannonBalls;
                                        }
                                        if (newCannonBalls < 1) {
                                            return;
                                        }
                                        player.getInventory().remove(new Item(2, newCannonBalls));
                                        cannon.addCannonBalls(newCannonBalls);
                                        player.getActionSender().sendMessage("You load " + newCannonBalls + " cannonball"
                                                + (newCannonBalls > 1 ? "s" : "") + " into your cannon.");
                                    }
                                    cannon.fire();
                                } else {
                                    cannon.destroy();
                                }
                            } else {
                                player.getActionSender().sendMessage("This is not your cannon.");
                            }
                        } else {
                            player.getActionSender().sendMessage("This is not your cannon.");
                        }
                        break;
                    case 450:
                    case 451:
                    case 452:
                    case 453:
                        player.getActionSender().sendMessage("There is no ore currently available in this rock.");
                        return;
                    case 2878:
                        if (player.getSettings().completedMageArena()) {
                            player.getActionSender()
                                    .sendMessage("You feel a magical energy running through you...");
                            player.setAttribute("busy", true);
                            World.getWorld().submit(new Tickable(2) {

                                @Override
                                public void execute() {
                                    this.stop();
                                    player.removeAttribute("busy");
                                    player.setTeleportTarget(Location.create(2509, 4689));
                                }

                            });
                        } else {
                            player.sendMessage("You step into the pool and nothing happens");
                        }
                        break;
                    case 2879:
                        player.getActionSender().sendMessage("You feel a magical energy running through you...");
                        player.setAttribute("busy", true);
                        World.getWorld().submit(new Tickable(2) {

                            @Override
                            public void execute() {
                                this.stop();
                                player.removeAttribute("busy");
                                player.setTeleportTarget(Location.create(2542, 4718));
                            }

                        });
                        break;
                    case 7179:
                        World.getWorld().unregister(obj, true);
                        World.getWorld().register(
                                new GameObject(obj.getLocation(), 7182, obj.getType(), obj.getDirection(), false));
                        break;
                    case 7182:
                        if (obj.getLocation().equals(Location.create(3097, 3468))) {
                            final Location location = Location
                                    .create(Vector2.of(obj.getLocation()).minus(1, 0).plus(0, 6400).plus(
                                            GameObjectCardinality.forFace(obj.getDirection()).getFaceVector()));
                            player.getActionQueue().addAction(new ClimbLadderAction(player, location));
                            break;
                        }
                        break;
                    case 7407:
                    case 7408:
                        World.getWorld().unregister(obj, true);
                        RegionClipping.removeClipping(obj);
                        break;
                    case 18987:
                        player.setTeleportTarget(Location.create(3069, 10255));
                        break;
                    case 18988:// kbd to wild
                        player.setTeleportTarget(Location.create(3016, 3849));
                        break;

                    case 11833:
                        // player.getActionSender().sendMessage("Coming
                        // soon..");
                        player.getFightCave().start();
                        break;
                    case 26384:
                        if (player.getSkills().getLevel(Skills.STRENGTH) < 70) {
                            player.getActionSender()
                                    .sendMessage("You need a Strength level of 70 to bang this door down.");
                        } else if (player.getInventory().getCount(2347) < 1) {
                            player.getActionSender().sendMessage("You need a hammer to bang this door down.");
                        } else {
                            player.getActionQueue().addAction(new Action(player, 3) {
                                @Override
                                public void execute() {
                                    if (player.getLocation().getX() == 2851) {
                                        player.setTeleportTarget(Location.create(player.getLocation().getX() - 1,
                                                player.getLocation().getY(), player.getLocation().getZ()));
                                    } else if (player.getLocation().getX() == 2850) {
                                        player.setTeleportTarget(Location.create(player.getLocation().getX() + 1,
                                                player.getLocation().getY(), player.getLocation().getZ()));
                                    }
                                    this.stop();
                                }

                                @Override
                                public AnimationPolicy getAnimationPolicy() {
                                    return AnimationPolicy.RESET_NONE;
                                }

                                @Override
                                public CancelPolicy getCancelPolicy() {
                                    return CancelPolicy.ALWAYS;
                                }

                                @Override
                                public StackPolicy getStackPolicy() {
                                    return StackPolicy.NEVER;
                                }
                            });
                            player.playAnimation(Animation.create(7002));
                        }
                        break;
                    default:
                        if (obj.getDefinition() != null) {
                            if (obj.getDefinition().getName().toLowerCase().contains("bank")) {
                                NPC closestBanker = null;
                                int closestDist = 10;
                                for (NPC banker : World.getWorld().getRegionManager().getLocalNpcs(player)) {
                                    if (banker.getDefinition().getName().toLowerCase().contains("banker")) {
                                        if (obj.getLocation().distanceToPoint(banker.getLocation()) < closestDist) {
                                            closestDist = obj.getLocation().distanceToPoint(banker.getLocation());
                                            closestBanker = banker;
                                        }
                                    }
                                }
                                if (closestBanker != null) {
                                    player.setInteractingEntity(InteractionMode.TALK, closestBanker);
                                    closestBanker.setInteractingEntity(InteractionMode.TALK, player);
                                    DialogueManager.openDialogue(player, 0);
                                }
                                return;
                            }
                        }
                        // String scriptName = "objectOptionOne" + id;
                        // if (!ScriptManager
                        // .getScriptManager()
                        // .invokeWithFailTest(scriptName, player, obj)) {
                        // player.getActionSender().sendMessage(
                        // "Nothing interesting happens.");
                        // }
                        break;
                }
                if (ClickEventManager.getEventManager().handleObjectOption(player, obj.getId(), obj,
                        obj.getLocation(), ClickOption.FIRST)) {
                    return;
                } else if (Levers.handle(player, obj)) {
                    return;
                } else if (SpiderWeb.slash(player, obj)) {
                    return;
                        /*
                         * } else if (Obelisks.handle(player, obj.getId(), loc))
						 * { return;
						 */
                } else if (Runecrafting.handleObject(player, obj)) {
                    return;
                } else if (Barrows.stairInteraction(player, obj.getId())) {
                    return;
                    // } else if (DoorManager.handleDoor(player, obj)) {
                    // return;
                } else if (Doors.manageDoor(obj)) {
                    return;
                } else if (MageArenaGodPrayer.godPrayer(player, obj)) {
                    return;
                }
            }

        };
        Tree tree = Tree.forId(objectId);
        Rock rock = Rock.forId(objectId);
        final Obstacle obstacle = Obstacle.forLocation(loc);
        if (tree != null) {
            // player.getActionQueue().addAction(new Woodcutting(player, obj));
            action = new Woodcutting(player, obj);
        } else if (rock != null) {
            action = new Mining(player, obj);
        } else if (obstacle != null) {
            Agility.tackleObstacle(player, obstacle, obj);
        }
        final Action submit = action;
        double dist = player.getLocation().distance(loc);
        if (dist <= 1 || (obj.getId() == 26646 && dist <= 3)) {
            player.getActionQueue().addAction(submit);
        } else {
            World.getWorld().submitAreaEvent(player,
                    new CoordinateEvent(player, x, y, def.getSizeX(), def.getSizeY(), obj.getDirection()) {

                        @Override
                        public void execute() {
                            player.getActionQueue().addAction(submit);
                        }

                    });
        }
    }

    /**
     * Handles the option 2 packet.
     *
     * @param player The player.
     * @param packet The packet.
     */
    private void handleOption2(final Player player, Packet packet) {
        int z = packet.getByteC();
        int x = packet.getLEShort();
        int y = packet.getShortA();
        int objectId = packet.getShort();
        Location loc = Location.create(x, y, player.getLocation().getZ());
        Region r = player.getRegion();
        final GameObject obj = r.getGameObject(loc, objectId);
        if (obj == null || obj.getId() != objectId) {
            System.out.println("Object null.");
            return;
        }
        CacheObjectDefinition def = CacheObjectDefinition.forID(objectId);
        player.getCombatState().setQueuedSpell(null);
        player.resetInteractingEntity();
        player.getActionQueue().clearAllActions();
        player.getActionSender().removeAllInterfaces();// .removeInterface2();
        Action action;
        if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
            player.getActionSender().sendMessage(
                    "<col=6e9118>ObjDebug2 - Id: " + objectId + ", Location: " + obj.getLocation().toString());
        }
        ThievingStalls stall = ThievingAction.THIEVING_STALLS.get(objectId);
        ThievingChests chest = ThievingAction.THIEVING_CHESTS.get(objectId);        // WallSafe safe = ThievingAction.WALL_SAFE.get(objectId);

        pathfindingService.travelToObject(player, obj);

        if (stall != null) {
            action = new ThievingAction(player, obj);
        } else if (chest != null) {
            action = new ThievingAction(player, obj);
        } else {
            action = new Action(player, 0) {
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

                @Override
                public void execute() {
                    hookService.post(new GameObjectActionEvent(player, GameObjectActionEvent.ActionType.OPTION_2, obj));
                    if (Doors.manageDoor(obj)) {
                        return;
                    }
                    switch (objectId) {
                        case 4569:
                            if (obj.getLocation().equals(Location.create(2506, 3640, 1))) {
                                player.setTeleportTarget(Location.create(2505, 3641, 2));
                            }
                            break;
                        case 1581: //edgeville dungeon trapdoor
                            player.sendMessage("Trapdoor");
                            player.setAttribute("busy", true);
                            World.getWorld().submit(new Tickable(1) {

                                @Override
                                public void execute() {
                                    this.stop();
                                    player.playAnimation(Animation.create(827));
                                    World.getWorld().unregister(obj, true);
                                    // ObjectManager.removeCustomObject(obj.getLocation().getX(), obj.getLocation().getY(), obj.getLocation().getZ(), obj.getType());
                                    GameObject toReplace = new GameObject(obj.getLocation(), 1579, obj.getType(), obj.getDirection(), true);
                                    World.getWorld().register(toReplace);
                                    player.setTeleportTarget(Location.create(3097, 9868, 0));
                                    player.removeAttribute("busy");
                                }

                            });
                            break;
                        case 6552:
                            player.playAnimation(Animation.create(645));
                            player.setAttribute("busy", true);
                            World.getWorld().submit(new Tickable(2) {

                                @Override
                                public void execute() {
                                    this.stop();
                                    int spellbook = player.getCombatState().getSpellBook();
                                    if (spellbook == SpellBook.LUNAR_MAGICS.getSpellBookId()
                                            || spellbook == SpellBook.MODERN_MAGICS.getSpellBookId()) {
                                        player.getActionSender().sendMessage("An ancient wisdom fills your mind...");
                                        spellbook = SpellBook.ANCIENT_MAGICKS.getSpellBookId();
                                    } else {
                                        player.sendMessage("You revert to modern magic.");
                                        spellbook = SpellBook.MODERN_MAGICS.getSpellBookId();
                                    }
                                    int config = spellbook == SpellBook.MODERN_MAGICS.getSpellBookId() ? 0 : 1;
                                    player.removeAttribute("busy");
                                    player.getCombatState().setSpellBook(spellbook);
                                    player.getActionSender().sendConfig(439, config);
                                }

                            });
                            break;
                        case 14911:
                            player.playAnimation(Animation.create(645));
                            player.setAttribute("busy", true);
                            World.getWorld().submit(new Tickable(2) {

                                @Override
                                public void execute() {
                                    this.stop();
                                    int spellbook = player.getCombatState().getSpellBook();
                                    if (spellbook == SpellBook.ANCIENT_MAGICKS.getSpellBookId()
                                            || spellbook == SpellBook.MODERN_MAGICS.getSpellBookId()) {
                                        player.getActionSender().sendMessage("Lunar spells activated!");
                                        spellbook = SpellBook.LUNAR_MAGICS.getSpellBookId();
                                    } else {
                                        player.sendMessage("You revert to modern magic.");
                                        spellbook = SpellBook.MODERN_MAGICS.getSpellBookId();
                                    }
                                    int config = spellbook == SpellBook.MODERN_MAGICS.getSpellBookId() ? 0 : 2;
                                    player.removeAttribute("busy");
                                    player.getCombatState().setSpellBook(spellbook);
                                    player.getActionSender().sendConfig(439, config);
                                }

                            });
                            break;
                        case 25824:
                        case 7132:
                            player.getActionSender().sendInterface(459, false);
                            break;
                        case 16672:
                            player.setTeleportTarget(Location.create(player.getX(), player.getY(), player.getZ() + 1));
                            break;
                        case 14896:
                            if (System.currentTimeMillis() - player.getLastHarvest() > 600) {
                                player.setLastHarvest(System.currentTimeMillis());
                                if (player.getInventory().add(new Item(1779, 1))) {
                                    player.playAnimation(Animation.create(827));
                                    player.getActionSender().sendMessage("You manage to pick some Flax...");
                                    player.getActionSender().playSound(Sound.PICK_FLAX);
                                    if (Misc.random(4) == 0) {
                                        World.getWorld().replaceObject(obj, null, 30);
                                    }
                                }
                            }
                            break;
                        case 11748:
                        case 11744:
                        case 24101:
                        case 25808:
                        case 16700:
                        case 18491:
                        case 27249:
                        case 27718:
                        case 27719:
                        case 27720:
                        case 27721:
                        case 12121:
                        case 14886:
                        case 27259:
                        case 14367:
                        case 6943:
                        case 27264:
                        case 28861:
                        case 7478:
                        case 7409:
                        case 6944:
                        case 27291:
                        case 10060:
                        case 28546:
                        case 28549:
                        case 28547:
                        case 28548:
                            Bank.open(player);
                            break;
                        case 24009:
                        case 16469:
                        case 26300:
                            Smelting.furnaceInteraction(player);
                            break;
                        case 6:
                            if (player.getAttribute("cannon") != null) {
                                Cannon cannon = (Cannon) player.getAttribute("cannon");
                                if (cannon.getGameObject().getLocation().equals(loc)) {
                                    cannon.destroy();
                                } else {
                                    player.getActionSender().sendMessage("This is not your cannon.");
                                }
                            } else {
                                player.getActionSender().sendMessage("This is not your cannon.");
                            }
                            break;
                    }
                    this.stop();
                }
            };
        }
        if (action != null) {
            final Action submit = action;
            double dist = player.getLocation().distance(loc);
            if (dist <= 1) {
                player.getActionQueue().addAction(submit);
            } else {
                World.getWorld().submitAreaEvent(player,
                        new CoordinateEvent(player, x, y, def.getSizeX(), def.getSizeY(), obj.getDirection()) {

                            @Override
                            public void execute() {
                                player.getActionQueue().addAction(submit);
                            }

                        });
            }
        }
    }

    private void handleOption3(final Player player, Packet packet) {
        int objectId = packet.getLEShortA();
        int x = packet.getShortA();
        int z = packet.get();
        int y = packet.getLEShortA();
        Location loc = Location.create(x, y, player.getZ());
        Region r = player.getRegion();
        final GameObject obj = r.getGameObject(loc, objectId);
        if (obj == null || obj.getId() != objectId) {
            System.out.println("Object null.");
            return;
        }
        CacheObjectDefinition def = CacheObjectDefinition.forID(objectId);
        player.getCombatState().setQueuedSpell(null);
        player.resetInteractingEntity();
        player.getActionQueue().clearAllActions();
        player.getActionSender().removeAllInterfaces();// .removeInterface2();
        Action action;
        if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
            player.getActionSender()
                    .sendMessage("[Object Debug3] Id: " + objectId + ", Location: " + obj.getLocation().toString());
        }

        player.faceObject(obj);
        pathfindingService.travelToObject(player, obj);
        action = new Action(player, 0) {
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

            @Override
            public void execute() {
                switch (objectId) {
                    case 4569:
                        if (obj.getLocation().equals(Location.create(2506, 3640, 1))) {
                            player.setTeleportTarget(Location.create(2505, 3640, 0));
                        }
                        break;
                    case 16672:// warrior guild level 2
                        player.setTeleportTarget(Location.create(player.getX(), player.getY(), player.getZ() - 1));
                        break;
                    case 10177:
                        player.setTeleportTarget(Location.create(2900, 4449, 0));
                        break;
                    case 10060:
                    case 10061:
                        player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE, -1, null,
                                "This feature is not enabled at this time.");
                        break;
                }
                this.stop();
            }
        };
        if (action != null) {
            final Action submit = action;
            World.getWorld().submitAreaEvent(player,
                    new CoordinateEvent(player, x, y, def.getSizeX(), def.getSizeY(), obj.getDirection()) {

                        @Override
                        public void execute() {
                            player.getActionQueue().addAction(submit);
                        }

                    });
            // boolean blocked = false;
            // int xOff = player.getX() > obj.getX() ? player.getX() -
            // obj.getX() : obj.getX() - player.getX();
            // int yOff = player.getY() > obj.getY() ? player.getY() -
            // obj.getY() : obj.getY() - player.getY();
            // for (int i = 0; i < xOff; i++) {
            // for (int i2 = 0; i2 < yOff; i2++) {
            // if (reachedWall(player.getX(), player.getY(), player.getX() + i,
            // player.getY() + i2, obj.getZ(), obj.getDirection(),
            // obj.getDefinition().getSurroundings()) ||
            // reachedDecoration(player.getX(), player.getY(), player.getX() +
            // i, player.getY() + i2, obj.getZ(), obj.getDirection(),
            // obj.getDefinition().getSurroundings())) {
            // blocked = true;
            // }
            // }
            // }
            // if (distance > 1 || blocked) {
            // player.addCoordinateAction(player.getWidth(),
            // player.getHeight(), loc, width, height, 1, action);
            // PathFinder finder = null;
            // if (usesDefaultPath(def)) {
            // finder = new DefaultPathFinder();// had idea
            // } else if (usesPrimitivePath(def)) {
            // finder = new PrimitivePathFinder();
            // } else {
            // finder = new ObjectPathFinder(obj, 2, def.getSizeX(),
            // def.getSizeY(), obj.getDirection(), obj.getType());
            // }
            // World.getWorld().doPath(finder, player, x, y, false, true);
            // } else if (distance <= 1 && !blocked) {
            // player.getActionQueue().addAction(action);
            // }
        }
    }

    /**
     * Handles the item on object packet.
     *
     * @param player The player.
     * @param packet The packet.
     */
    private void handleOptionItem(final Player player, Packet packet) {
        int y = packet.getShortA();
        int itemId = packet.getShort();
        int c = packet.getByteS();
        int x = packet.getShortA();
        int objectId = packet.getLEShortA();
        int slot = packet.getShort();
        int g = packet.getInt();
        int z = player.getLocation().getZ();

        if (player.getAttribute("temporaryHeight") != null) {
            z = player.getAttribute("temporaryHeight");
        }
        final Location loc = Location.create(x, y, z);
        final Item item = player.getInventory().get(slot);
        if (item == null) {
            return;
        }
        final GameObject obj = player.getRegion().getGameObject(loc, objectId);
        if (obj == null) {
            return;
        }
        if (permissionService.is(player, PermissionService.PlayerPermissions.ADMINISTRATOR)) {
            player.getActionSender().sendMessage("Loc: " + obj.getLocation() + ", ID: " + obj.getId() + ", Type: "
                    + obj.getType() + ", Rotation: " + obj.getDirection() + ", Loaded: " + obj.isLoadedInLandscape());
        }
        CacheObjectDefinition def = CacheObjectDefinition.forID(objectId);
        int width = 1;
        int height = 1;
        if (def != null) {
            if (obj.getDirection() != 1 && obj.getDirection() != 3) {
                width = def.getSizeX();
                height = def.getSizeY();
            } else {
                width = def.getSizeY();
                height = def.getSizeX();
            }
        }
        player.faceObject(obj);
        pathfindingService.travelToObject(player, obj);
        int distance = obj.getLocation().distanceToEntity(obj, player);
        player.getActionSender().sendDebugPacket(packet.getOpcode(), "ItemOnObject",
                new Object[]{"ID: " + objectId, "Loc: " + loc});
        player.getCombatState().setQueuedSpell(null);
        player.resetInteractingEntity();
        player.getActionQueue().clearAllActions();
        player.getActionSender().removeAllInterfaces();// .removeInterface2();
        Action action = null;
        action = new Action(player, 0) {
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

            @Override
            public void execute() {
                this.stop();
                hookService.post(
                        new GameObjectActionEvent(player, GameObjectActionEvent.ActionType.ITEM_ON_OBJECT, obj, item));
                if (player.getWarriorsGuild().handleItemOnObject(item, objectId, loc)) {
                    return;
                } else if (DragonfireShieldAction.handleItemOnObject(player, item, obj)) {
                    return;
                } else if (AltarAction.handleItemOnObject(player, obj, item)) {
                    return;
                } else if (CballMakingAction.handleItemOnObject(player, obj, item)){
                	return;
                }
                switch (obj.getId()) {
                    case 11748:
                    case 11744:
                    case 24101:
                    case 14886:
                    case 25808:
                    case 16700:
                    case 18491:
                    case 27249:
                    case 27718:
                    case 27719:
                    case 27720:
                    case 27721:
                    case 12121:
                    case 27259:
                    case 14367:
                    case 6943:
                    case 27264:
                    case 7478:
                    case 7409:
                    case 6944:
                    case 4483:
                    case 10058:
                    case 26707:
                        PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
                        if (item.getDefinition() == null/*
													 * || !permissionService.is(
													 * player,
													 * PermissionService.
													 * PlayerPermissions.
													 * ULTIMATE_IRON_MAN)
													 */) {
                            return;
                        }
                        if (item.getId() == 11941) {
                            lootingBagService.redeemBag(player);
                            return;
                        }
                        if (item.getDefinition().isNoted()) {
                            itemService.exchangeToUnNote(player, item);
                        } else {
                            itemService.exchangeToNote(player, item);
                        }
                        break;
                    case 24004:
                    case 874:
                    case 27707:
                    case 27708:
                        WaterSourceAction.Fillables fill = WaterSourceAction.Fillables.forId(item.getId());
                        if (fill != null) {
                            player.getActionQueue().addAction(new WaterSourceAction(player, fill));
                        }
                        break;
                    case 6:
                        if (player.getAttribute("cannon") != null) {
                            Cannon cannon = (Cannon) player.getAttribute("cannon");
                            if (loc.equals(cannon.getGameObject().getLocation())) {
                                if (item.getId() == 2) {
                                    int cannonBalls = cannon.getCannonBalls();
                                    if (cannonBalls >= 30) {
                                        player.getActionSender().sendMessage("Your cannon is already full.");
                                        return;
                                    }
                                    int newCannonBalls = item.getCount();
                                    if (newCannonBalls > 30) {
                                        newCannonBalls = 30;
                                    }
                                    if (newCannonBalls + cannonBalls > 30) {
                                        newCannonBalls = 30 - cannonBalls;
                                    }
                                    if (newCannonBalls < 1) {
                                        return;
                                    }
                                    player.getInventory().remove(new Item(2, newCannonBalls));
                                    cannon.addCannonBalls(newCannonBalls);
                                    player.getActionSender().sendMessage("You load " + newCannonBalls + " cannonball"
                                            + (newCannonBalls > 1 ? "s" : "") + " into your cannon.");
                                }
                            }
                        }
                        break;
                    case 7:
                        if (player.getAttribute("cannon") != null) {
                            Cannon cannon = (Cannon) player.getAttribute("cannon");
                            if (loc.equals(cannon.getGameObject().getLocation())) {
                                if (item.getId() == 8) {
                                    cannon.addPart(new Item(8, 1));
                                }
                            }
                        }
                        break;
                    case 8:
                        if (player.getAttribute("cannon") != null) {
                            Cannon cannon = (Cannon) player.getAttribute("cannon");
                            if (loc.equals(cannon.getGameObject().getLocation())) {
                                if (item.getId() == 10) {
                                    cannon.addPart(new Item(10, 1));
                                }
                            }
                        }
                        break;
                    case 9:
                        if (player.getAttribute("cannon") != null) {
                            Cannon cannon = (Cannon) player.getAttribute("cannon");
                            if (loc.equals(cannon.getGameObject().getLocation())) {
                                if (item.getId() == 12) {
                                    cannon.addPart(new Item(12, 1));
                                }
                            }
                        }
                        break;
                }
                if (item.getDefinition2() != null && item.getDefinition2().name != null
                        && item.getDefinition2().name.contains("armour set")) {
                    if (item.getDefinition().getStorePrice() > 0) {
                        switch (item.getDefinition2().name) {
                            case "Guthan's armour set":
                                if (player.getInventory().add(new Item(4724, 1))
                                        && player.getInventory().add(new Item(4726, 1))
                                        && player.getInventory().add(new Item(4728, 1))
                                        && player.getInventory().add(new Item(4730, 1))) {
                                    player.getInventory().remove(item);
                                    player.getActionSender().sendMessage("You exchange your kit for the full set.");
                                }
                                break;
                            case "Verac's armour set":
                                if (player.getInventory().add(new Item(4753, 1))
                                        && player.getInventory().add(new Item(4755, 1))
                                        && player.getInventory().add(new Item(4757, 1))
                                        && player.getInventory().add(new Item(4759, 1))) {
                                    player.getInventory().remove(item);
                                    player.getActionSender().sendMessage("You exchange your kit for the full set.");
                                }
                                break;
                            case "Dharok's armour set":
                                if (player.getInventory().add(new Item(4716, 1))
                                        && player.getInventory().add(new Item(4718, 1))
                                        && player.getInventory().add(new Item(4720, 1))
                                        && player.getInventory().add(new Item(4722, 1))) {
                                    player.getInventory().remove(item);
                                    player.getActionSender().sendMessage("You exchange your kit for the full set.");
                                }
                                break;
                            case "Torag's armour set":
                                if (player.getInventory().add(new Item(4745, 1))
                                        && player.getInventory().add(new Item(4747, 1))
                                        && player.getInventory().add(new Item(4749, 1))
                                        && player.getInventory().add(new Item(4751, 1))) {
                                    player.getInventory().remove(item);
                                    player.getActionSender().sendMessage("You exchange your kit for the full set.");
                                }
                                break;
                            case "Ahrim's armour set":
                                if (player.getInventory().add(new Item(4708, 1))
                                        && player.getInventory().add(new Item(4710, 1))
                                        && player.getInventory().add(new Item(4712, 1))
                                        && player.getInventory().add(new Item(4714, 1))) {
                                    player.getInventory().remove(item);
                                    player.getActionSender().sendMessage("You exchange your kit for the full set.");
                                }
                                break;
                            case "Karil's armour set":
                                if (player.getInventory().add(new Item(4732, 1))
                                        && player.getInventory().add(new Item(4734, 1))
                                        && player.getInventory().add(new Item(4736, 1))
                                        && player.getInventory().add(new Item(4738, 1))) {
                                    player.getInventory().remove(item);
                                    player.getActionSender().sendMessage("You exchange your kit for the full set.");
                                }
                                break;
                        }
                    }
                }
                if (Cooking.getCookingItem(item.getId()) != null && Cooking.getCookingMethod(obj) != null) {
                    CookingItem cookItem = Cooking.getCookingItem(item.getId());
                    CookingMethod method = Cooking.getCookingMethod(obj);
                    if (Cooking.canCook(method, cookItem)) {
                        player.setInterfaceAttribute("cookItem", cookItem);
                        player.setInterfaceAttribute("cookMethod", method);
                        player.getActionSender().sendChatboxInterface(307);
                        player.getActionSender().sendItemOnInterface(307, 2, item.getId(), 160);
                        player.getActionSender().sendString(307, 6,
                                "<br><br><br><br>" + item.getDefinition2().getName());
                    } else {
                        player.getActionSender().sendMessage("You cannot cook that on a fire!");
                    }
                }
                if (obj.getDefinition().getName().equalsIgnoreCase("Anvil")) {
                	if(item.getId() == 2366 || item.getId() == 2368)
                	{
                		if(player.getInventory().hasItem(new Item(2366)) && player.getInventory().hasItem(new Item(2368)))
                		{
                			if(player.getSkills().getLevel(Skills.SMITHING) > 60)
                			{
                				if(player.getInventory().contains(2347))
                				{
                				player.playAnimation(Animation.create(898));
                				player.setAttribute("busy", true);
                				World.getWorld().submit(new Tickable(1) {
                                    @Override
                                    public void execute() {
                                        this.stop();
                                        player.playAnimation(Animation.create(898));
                                        player.getInventory().remove(new Item(2366));
                             			player.getInventory().remove(new Item(2368));
                             			player.getInventory().add(new Item(1187));
                             			player.getSkills().addExperience(Skills.SMITHING, 75);
                                        player.removeAttribute("busy");
                                        player.sendMessage("You have restored the dragon square shield to its former glory.");
                                     }

                                 });
                				} 
                				else
                				{
                					player.sendMessage("You need a hammer to smith this.");
                				}
                			} else {
                				player.sendMessage("You need a smithing level of 60 to do this.");
                			}                			
                		}
                	}
                    ForgingBar bar = ForgingBar.forId(item.getId());
                    if (bar != null) {
                        Smithing.openSmithingInterface(player, bar);
                    }
                }
                if (obj.getDefinition().getName().equalsIgnoreCase("Furnace")) {
                    if (item.getId() == 2357) {
                        player.getActionSender().sendCS2Script(917, new Object[]{-1, -1}, "ii").sendInterface(446,
                                false);
                    }
                }
				/*
				 * if (obj.getDefinition().getName().equalsIgnoreCase("Anvil"))
				 * { Bar bar = Bar.forId(item.getId()); if (bar != null) {
				 * Smithing.openSmithingInterface(player, bar); } } else if
				 * (Cooking.getCookingItem(item.getId()) != null &&
				 * Cooking.getCookingMethod(obj) != null) { CookingItem cookItem
				 * = Cooking.getCookingItem(item.getId()); CookingMethod method
				 * = Cooking.getCookingMethod(obj); if (Cooking.canCook(method,
				 * cookItem)) { player.setInterfaceAttribute("cookItem",
				 * cookItem); player.setInterfaceAttribute("cookMethod",
				 * method); player.getActionSender().sendChatboxInterface(307);
				 * player.getActionSender().sendItemOnInterface(307, 2,
				 * item.getId(), 160); player.getActionSender().sendString( 307,
				 * 6, "<br><br><br><br>" + item.getDefinition().getName()); }
				 * else { player.getActionSender().sendMessage(
				 * "You cannot cook that on a fire!"); } } else { switch
				 * (objectId) { case 6: if (player.getAttribute("cannon") !=
				 * null) { Cannon cannon = (Cannon) player
				 * .getAttribute("cannon"); if
				 * (loc.equals(cannon.getGameObject().getLocation())) { if
				 * (item.getId() == 2) { int cannonBalls =
				 * cannon.getCannonBalls(); if (cannonBalls >= 30) {
				 * player.getActionSender().sendMessage(
				 * "Your cannon is already full."); return; } int newCannonBalls
				 * = item.getCount(); if (newCannonBalls > 30) { newCannonBalls
				 * = 30; } if (newCannonBalls + cannonBalls > 30) {
				 * newCannonBalls = 30 - cannonBalls; } if (newCannonBalls < 1)
				 * { return; } player.getInventory().remove( new Item(2,
				 * newCannonBalls)); cannon.addCannonBalls(newCannonBalls);
				 * player.getActionSender().sendMessage( "You load " +
				 * newCannonBalls + " cannonball" + (newCannonBalls > 1 ? "s" :
				 * "") + " into your cannon."); } } } break; case 7: if
				 * (player.getAttribute("cannon") != null) { Cannon cannon =
				 * (Cannon) player .getAttribute("cannon"); if
				 * (loc.equals(cannon.getGameObject().getLocation())) { if
				 * (item.getId() == 8) { cannon.addPart(new Item(8, 1)); } } }
				 * break; case 8: if (player.getAttribute("cannon") != null) {
				 * Cannon cannon = (Cannon) player .getAttribute("cannon"); if
				 * (loc.equals(cannon.getGameObject().getLocation())) { if
				 * (item.getId() == 10) { cannon.addPart(new Item(10, 1)); } } }
				 * break; case 9: if (player.getAttribute("cannon") != null) {
				 * Cannon cannon = (Cannon) player .getAttribute("cannon"); if
				 * (loc.equals(cannon.getGameObject().getLocation())) { if
				 * (item.getId() == 12) { cannon.addPart(new Item(12, 1)); } } }
				 * break; } }
				 */
            }
        };
        if (action != null) {
            final Action submit = action;
            World.getWorld().submitAreaEvent(player,
                    new CoordinateEvent(player, x, y, def.getSizeX(), def.getSizeY(), obj.getDirection()) {

                        @Override
                        public void execute() {
                            player.getActionQueue().addAction(submit);
                        }

                    });
            // boolean blocked = false;
            // int xOff = player.getX() > obj.getX() ? player.getX() -
            // obj.getX() : obj.getX() - player.getX();
            // int yOff = player.getY() > obj.getY() ? player.getY() -
            // obj.getY() : obj.getY() - player.getY();
            // for (int i = 0; i < xOff; i++) {
            // for (int i2 = 0; i2 < yOff; i2++) {
            // if (reachedWall(player.getX(), player.getY(), player.getX() + i,
            // player.getY() + i2, obj.getZ(), obj.getDirection(),
            // obj.getDefinition().getSurroundings()) ||
            // reachedDecoration(player.getX(), player.getY(), player.getX() +
            // i, player.getY() + i2, obj.getZ(), obj.getDirection(),
            // obj.getDefinition().getSurroundings())) {
            // blocked = true;
            // }
            // }
            // }
            // if (distance > 1 || blocked) {
            // player.addCoordinateAction(player.getWidth(),
            // player.getHeight(), loc, width, height, 1, action);
            // PathFinder finder = null;
            // if (usesDefaultPath(def)) {
            // finder = new DefaultPathFinder();// had idea
            // } else if (usesPrimitivePath(def)) {
            // finder = new PrimitivePathFinder();
            // } else {
            // finder = new ObjectPathFinder(obj, 2, def.getSizeX(),
            // def.getSizeY(), obj.getDirection(), obj.getType());
            // }
            // World.getWorld().doPath(finder, player, x, y, false, true);
            // } else if (distance <= 1 && !blocked) {
            // player.getActionQueue().addAction(action);
            // }
        }
    }

    public Location bestWalkablePath(GameObject obj) {
        CacheObjectDefinition def = obj.getDefinition();
        int width = 1;
        int height = 1;
        if (def != null) {
            if (obj.getDirection() != 1 && obj.getDirection() != 3) {
                width = def.getSizeX();
                height = def.getSizeY();
            } else {
                width = def.getSizeY();
                height = def.getSizeX();
            }
        }
        int toX = obj.getSpawnLocation().getX();
        int toY = obj.getSpawnLocation().getY();
        for (int dx = -width; dx <= width; dx++) {
            for (int dy = -height; dy <= height; dy++) {
                if (RegionClipping.isPassable(toX + dx, toY + dy, obj.getLocation().getZ()) && obj.getSpawnLocation()
                        .isWithinDistance(width, height, Location.create(toX + dx, toY + dy), 1, 1, 1)) {
                    toX = toX + dx;
                    toY = toY + dy;
                    break;
                }
            }
        }
        return Location.create(toX, toY, obj.getSpawnLocation().getZ());
    }

    public boolean reachedWall(int initialX, int initialY, int finalX, int finalY, int z, int orientation, int type) {
        if (initialX == finalX && initialY == finalY) {
            return true;
        }
        int clipping = RegionClipping.getClippingMask(initialX, initialY, z);
        if (type == 0) {
            if (orientation == Orientation.NORTH) {
                if (initialX == finalX - 1 && initialY == finalY) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1 && (clipping & 0x1280120) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1 && (clipping & 0x1280102) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.EAST) {
                if (initialX == finalX && initialY == finalY + 1) {
                    return true;
                } else if (initialX == finalX - 1 && initialY == finalY && (clipping & 0x1280108) == 0) {
                    return true;
                } else if (initialX == finalX + 1 && initialY == finalY && (clipping & 0x1280180) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.SOUTH) {
                if (initialX == finalX + 1 && initialY == finalY) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1 && (clipping & 0x1280120) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1 && (clipping & 0x1280102) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.WEST) {
                if (initialX == finalX && initialY == finalY - 1) {
                    return true;
                } else if (initialX == finalX - 1 && initialY == finalY && (clipping & 0x1280108) == 0) {
                    return true;
                } else if (initialX == finalX + 1 && initialY == finalY && (clipping & 0x1280180) == 0) {
                    return true;
                }
            }
        }

        if (type == 2) {
            if (orientation == Orientation.NORTH) {
                if (initialX == finalX - 1 && initialY == finalY) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1) {
                    return true;
                } else if (initialX == finalX + 1 && initialY == finalY && (clipping & 0x1280180) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1 && (clipping & 0x1280102) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.EAST) {
                // UNLOADED_TILE | BLOCKED_TILE | UNKNOWN | OBJECT_TILE |
                // WALL_EAST
                if (initialX == finalX - 1 && initialY == finalY && (clipping & 0x1280108) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1) {
                    return true;
                } else if (initialX == finalX + 1 && initialY == finalY) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1 && (clipping & 0x1280102) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.SOUTH) {
                if (initialX == finalX - 1 && initialY == finalY && (clipping & 0x1280108) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1 && (clipping & 0x1280120) == 0) {
                    return true;
                } else if (initialX == finalX + 1 && initialY == finalY) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1) {
                    return true;
                }
            } else if (orientation == Orientation.WEST) {
                if (initialX == finalX - 1 && initialY == finalY) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1 && (clipping & 0x1280120) == 0) {
                    return true;
                } else if (initialX == finalX + 1 && initialY == finalY && (clipping & 0x1280180) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1) {
                    return true;
                }
            }
        }

        if (type == 9) {
            if (initialX == finalX && initialY == finalY + 1 && (clipping & ObjectPathFinder.WALL_SOUTH) == 0) {
                return true;
            } else if (initialX == finalX && initialY == finalY - 1 && (clipping & ObjectPathFinder.WALL_NORTH) == 0) {
                return true;
            } else if (initialX == finalX - 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_EAST) == 0) {
                return true;
            } else if (initialX == finalX + 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_WEST) == 0) {
                return true;
            }
        }

        return false;
    }

    public boolean reachedDecoration(int initialY, int initialX, int finalX, int finalY, int z, int type,
                                     int orientation) {
        if (initialX == finalX && initialY == finalY) {
            return true;
        }
        int clipping = RegionClipping.getClippingMask(initialX, initialY, z);
        if (type == 6 || type == 7) {
            if (type == 7) {
                orientation = orientation + 2 & 3;
            }

            if (orientation == Orientation.NORTH) {
                if (initialX == finalX + 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_WEST) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1
                        && (clipping & ObjectPathFinder.WALL_NORTH) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.EAST) {
                if (initialX == finalX - 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_EAST) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY - 1
                        && (clipping & ObjectPathFinder.WALL_NORTH) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.SOUTH) {
                if (initialX == finalX - 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_EAST) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1
                        && (clipping & ObjectPathFinder.WALL_SOUTH) == 0) {
                    return true;
                }
            } else if (orientation == Orientation.WEST) {
                if (initialX == finalX + 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_WEST) == 0) {
                    return true;
                } else if (initialX == finalX && initialY == finalY + 1
                        && (clipping & ObjectPathFinder.WALL_SOUTH) == 0) {
                    return true;
                }
            }
        }

        if (type == 8) {
            if (initialX == finalX && initialY == finalY + 1 && (clipping & ObjectPathFinder.WALL_SOUTH) == 0) {
                return true;
            } else if (initialX == finalX && initialY == finalY - 1 && (clipping & ObjectPathFinder.WALL_NORTH) == 0) {
                return true;
            } else if (initialX == finalX - 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_EAST) == 0) {
                return true;
            } else if (initialX == finalX + 1 && initialY == finalY && (clipping & ObjectPathFinder.WALL_WEST) == 0) {
                return true;
            }
        }

        return false;
    }

    public boolean containsGWD(CacheObjectDefinition def) {
        if (def == null || def.getName() == null) {
            return false;
        }
        String name = def.getName().toLowerCase();
        return name.contains("armadyl") || name.contains("bandos") || name.contains("sara") || name.contains("zamorak");
    }

    public boolean usesDefaultPath(CacheObjectDefinition def) {
        if (def == null || def.getName() == null) {
            return true;
        }
        String name = def.getName().toLowerCase();
        return name.contains("wilderness") || name.contains("flax") || name.contains("lever");
    }

    public boolean usesPrimitivePath(CacheObjectDefinition def) {
        if (def == null || def.getName() == null) {
            return true;
        }
        String name = def.getName().toLowerCase();
        return name.contains("tunnel") || name.contains("sacrificial boat") || name.contains("web")
                || name.contains("obstacle pipe") || name.contains("staircase") || name.contains("door")
                || name.contains("gate") || name.contains("fence");
    }
}
