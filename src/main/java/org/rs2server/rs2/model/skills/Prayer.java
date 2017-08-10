package org.rs2server.rs2.model.skills;

import org.rs2server.Server;
import org.rs2server.rs2.Constants;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.event.Event;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.Sound;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.boundary.BoundaryManager;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;

/**
 * For prayer related activities.
 *
 * @author Tyluur <ItsTyluur@gmail.com
 */
@SuppressWarnings("unused")
public class Prayer {

    public void prayAltar(Location loc) {
        PermissionService permissionService = Server.getInjector().getInstance(PermissionService.class);
        /*if (permissionService.isAny(player, PermissionService.PlayerPermissions.ADMINISTRATOR, PermissionService.PlayerPermissions.DONATOR)) {
			if (player.getSettings().getLastAltarPrayer() < 120000) {
				//player.getActionSender().sendMessage("You can only use the altar to restore your Hp/Prayer every 2 minutes");
			} else {
				player.getSkills().increaseLevelToMaximum(Skills.HITPOINTS, 99);
				player.getCombatState().increaseSpecial(100);
				player.getSettings().setLastAltarPrayer(System.currentTimeMillis());
			}*/
        /*} else if (Misc.random(4) == 0) {
            player.getActionSender().sendMessage("Did you know if you were a donator you'd restore special energy and hitpoints?");
        }*/

        if (player.getSkills().getPrayerPoints() >= player.getSkills().getLevelForExperience(Skills.PRAYER)) 
        {
            player.getActionSender().sendMessage("You already have full prayer points.");
            return;
        }
        player.getSkills().setLevel(Skills.PRAYER, player.getSkills().getLevelForExperience(Skills.PRAYER));
        player.getSkills().setPrayerPoints(player.getSkills().getLevelForExperience(Skills.PRAYER), true);
        if (player.getActionSender() != null) {
            player.getActionSender().sendSkills();
        }
        player.playAnimation(Animation.create(645));
        player.getSkills().setLevel(Skills.PRAYER, player.getSkills().getLevelForExperience(Skills.PRAYER));
        player.getActionSender().sendMessage("You pray at the altar...");
    }

    public void buryBone(final Item bone, int slot) {
        if (player.getAttribute("can_bury") == null)
            player.setAttribute("can_bury", true);
        if (!(Boolean) (player.getAttribute("can_bury"))) {
            return;
        }
        player.setAttribute("can_bury", false);
        player.playAnimation(Animation.create(827));
        player.getActionSender().sendMessage("You dig a hole in the ground.");
        player.getInventory().remove(bone, slot);
		player.getActionSender().playSound(Sound.BURY_BONES);
        World.getWorld().submit(new Event(1500) {
            public void execute() {
        		if (BoundaryManager.isWithinBoundaryNoZ(player.getLocation(), "LavaDragonIsle") && bone.getId() == 11943) {
                    player.getSkills().addExperience(Skills.PRAYER, getBoneXP(bone) * 4);
                    player.getActionSender().sendMessage("You bury the bones...");
                    player.setAttribute("can_bury", true);
                    stop();
        		} else {
                    player.getSkills().addExperience(Skills.PRAYER, getBoneXP(bone));
                    player.getActionSender().sendMessage("You bury the bones...");
                    player.setAttribute("can_bury", true);
                    stop();
        		}
            }
        });
    }

    public void altarBone(Item bone) {
        if (player.getAttribute("can_bury") == null)
            player.setAttribute("can_bury", true);
        if (!(Boolean) (player.getAttribute("can_bury"))) {
            return;
        }
        player.setAttribute("can_bury", false);
        player.playAnimation(Animation.create(713));
        player.playGraphics(Graphic.create(624));
        player.getInventory().remove(bone);
        player.getActionSender().sendMessage("You use the bones on the altar.");
        player.getSkills().addExperience(Skills.PRAYER, getAltarXP(bone));
        World.getWorld().submit(new Event(1000) {
            public void execute() {
                player.setAttribute("can_bury", true);
                stop();
            }
        });
    }

    private enum Bone {

        NORMAL_BONES(526, 4),
        BURNT_BONES(528, 4),
        BAT_BONES(530, 5),
        BIG_BONES(532, 15),
        BABYDRAGON_BONES(534, 30),
        DRAGON_BONES(536, 72),
        LAVA_DRAGON_BONES(11943, 85),
        WOLF_BONES(2859, 4),
        JOGRE_BONES(3125, 15),
        DAGANNOTH_BONES(6729, 72),
        WYVERN_BONES(6812, 50),
        SHAIKAHAN_BONES(3123, 25),
        OURG_BONES(4834, 140);

        private int id;
        private int xp;

        public int getId() {
            return id;
        }

        public int getXp() {
            return xp;
        }

        private Bone(int id, int xp) {
            this.id = id;
            this.xp = xp;
        }
    }

    private int getBoneXP(Item bone) {
        int xp = 1;
        switch (bone.getId()) {
            case 526:
            case 528:
            case 2530:
            case 2859:
                xp = 4;
                break;
            case 530:
                xp = 5;
                break;
            case 532:
            case 3125:
                xp = 15;
                break;
            case 3123:
                xp = 25;
                break;
            case 534:
                xp = 30;
                break;
            case 536:
			case 6812:
                xp = 72;
                break;
            case 11943:
                xp = 85;
                break;
            case 6729:
                xp = 125;
                break;
            case 4834:
                xp = 140;
                break;
        }
        return xp;
    }

    private int getAltarXP(Item bone) {
        return getBoneXP(bone);
    }

    private Player player;

    /**
     * Constructs a new prayer skill.
     *
     * @param player For this player.
     */
    public Prayer(Player player) {
        this.player = player;
    }
}
