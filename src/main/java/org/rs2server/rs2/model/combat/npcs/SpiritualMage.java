package org.rs2server.rs2.model.combat.npcs;

import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Graphic;
import org.rs2server.rs2.model.Hit;
import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.Prayers;
import org.rs2server.rs2.model.Projectile;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.combat.CombatAction;
import org.rs2server.rs2.model.combat.CombatState.AttackType;
import org.rs2server.rs2.model.combat.impl.AbstractCombatAction;
import org.rs2server.rs2.tickable.Tickable;
import org.rs2server.rs2.util.Misc;

public class SpiritualMage extends AbstractCombatAction {
	
	/**
	 * The singleton instance.
	 */
	private static final SpiritualMage INSTANCE = new SpiritualMage();
	
	/**
	 * Gets the singleton instance.
	 * @return The singleton instance.
	 */
	public static CombatAction getAction() {
		return INSTANCE;
	}
	
	@Override
	public void hit(final Mob attacker, final Mob victim) {
		super.hit(attacker, victim);
		
		if(!attacker.isNPC()) {
			return; //this should be an NPC!
		}
		
		int clientSpeed;
		int gfxDelay;
		if(attacker.getLocation().isWithinDistance(attacker, victim, 1)) {
			clientSpeed = 70;
			gfxDelay = 80;
		} else if(attacker.getLocation().isWithinDistance(attacker, victim, 5)) {
			clientSpeed = 90;
			gfxDelay = 100;
		} else if(attacker.getLocation().isWithinDistance(attacker, victim, 8)) {
			clientSpeed = 110;
			gfxDelay = 120;
		} else {
			clientSpeed = 130;
			gfxDelay = 140;
		}
		int delay = (gfxDelay / 20) - 1;
		attacker.playAnimation(Animation.create(811));
		attacker.playProjectile(Projectile.create(attacker.getCentreLocation(), victim.getCentreLocation(), -1, 45, 50, clientSpeed, 43, 35, victim.getProjectileLockonIndex(), 10, 48));
		//((MagicCombatAction) this.hitEnemy(attacker, victim, Spell.FIRE_WAVE, Graphic.create(157, gfxDelay, 100), PoisonType.NONE, false, 20, delay, 0);
		attacker.getCombatState().setSpellDelay(6);
		attacker.getCombatState().setAttackDelay(5);
		int randomHit = Misc.random(damage(19, attacker, victim, AttackType.MAGIC, Skills.MAGIC , Prayers.PROTECT_FROM_MAGIC, false, false));
		//victim.playGraphics(randomHit <= 0 ? Graphic.create(85, gfxDelay, 100) : Graphic.create(157, gfxDelay, 100));
			World.getWorld().submit(new Tickable(delay) {

				@Override
				public void execute() {
					
					int height = 0;
					if(randomHit < 1)
						height = 100;
					victim.playGraphics(Graphic.create(randomHit > 0 ? 78 : 85, 0, height));
					smite(attacker, victim, randomHit);
					recoil(attacker, victim, randomHit);
					vengeance(attacker, victim, randomHit, 0);
				}
			});
			victim.getActiveCombatAction().defend(attacker, victim, false);
	}

	@Override
	public int distance(Mob attacker) {
		return 5;
	}

}
