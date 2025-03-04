package org.rs2server.rs2.tickable.impl;

import org.rs2server.rs2.model.Mob;
import org.rs2server.rs2.model.Prayers.Prayer;
import org.rs2server.rs2.model.Sound;
import org.rs2server.rs2.tickable.Tickable;
/**
 * A tickable which drains a mob's prayer.
 * @author Michael Bull
 *
 */
public class PrayerUpdateTick extends Tickable {

	/**
	 * The cycle time, in ticks.
	 */
	public static final int CYCLE_TIME = 1;
	
	/**
	 * The mob for who we are draining the prayer.
	 */
	public Mob mob;
	
	/**
	 * Creates the event to cycle every 2000 milliseconds (2 seconds).
	 */
	public PrayerUpdateTick(Mob mob) 
	{
		super(CYCLE_TIME);
		this.mob = mob;
	}
	
	@Override
	public void execute() {
		double amountDrain = 0;
		for(int i = 0; i < mob.getCombatState().getPrayers().length ; i++) {
			if(mob.getCombatState().getPrayer(i)) {	
				double drain = Prayer.forId(i).getDrain();
				double bonus = 0.035 * mob.getCombatState().getBonus(11);
				drain = drain * (1 + bonus);
				drain = 0.6 / drain;
				amountDrain += drain;
			}
		}
		mob.getSkills().decreasePrayerPoints(amountDrain);
		if(mob.getSkills().getPrayerPoints() <= 0 && !mob.getCombatState().isDead()) {
			mob.getCombatState().resetPrayers();
			if(mob.getActionSender() != null) {
				mob.getActionSender().sendMessage("You have run out of prayer points; you must recharge at an altar.");
				mob.getActionSender().playSound(Sound.NO_PRAYER_LEFT);
			}
		}
	}

}
