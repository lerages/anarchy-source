package org.rs2server.rs2.model.npc.impl.cerberus;

import com.google.common.collect.ImmutableList;
import org.rs2server.Server;
import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.rs2.content.Following;
import org.rs2server.rs2.domain.model.player.PlayerSettingsEntity;
import org.rs2server.rs2.domain.service.api.content.cerberus.CerberusService;
import org.rs2server.rs2.model.*;
import org.rs2server.rs2.model.boundary.BoundaryManager;
import org.rs2server.rs2.model.cm.Content;
import org.rs2server.rs2.model.combat.CombatState;
import org.rs2server.rs2.model.container.Equipment;
import org.rs2server.rs2.model.map.Directions;
import org.rs2server.rs2.model.npc.NPCLoot;
import org.rs2server.rs2.model.npc.NPCLootTable;
import org.rs2server.rs2.model.npc.Pet;
import org.rs2server.rs2.model.npc.impl.CombatNpc;
import org.rs2server.rs2.model.npc.impl.IdleCombatState;
import org.rs2server.rs2.model.npc.impl.NpcCombatState;
import org.rs2server.rs2.model.npc.impl.cerberus.ghosts.*;
import org.rs2server.rs2.model.npc.impl.cerberus.ghosts.tickables.CerberusGhostRegisterTick;
import org.rs2server.rs2.model.npc.impl.cerberus.styles.CerberusMagicAttackStyle;
import org.rs2server.rs2.model.npc.impl.cerberus.styles.CerberusMeleeAttackStyle;
import org.rs2server.rs2.model.npc.impl.cerberus.styles.CerberusRangedAttackStyle;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.StoppingTick;
import org.rs2server.rs2.util.Misc;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * @author Twelve
 */
public final class Cerberus extends CombatNpc<Cerberus> {

	public static final int NPC_ID = 5862;

	private static final Animation BLOCK_ANIMATION = Animation.create(4489);
	private static final Animation DEATH_ANIMATION = Animation.create(4495);
	private static final Animation HOWL_ANIMATION = Animation.create(4485);

	private static final int[] BONUSES = {0, 50, 0, 50, 50, 50, 100, 25, 100, 100, 0, 0, 0};
	public static final int MAX_HEALTH = 600;
	private static final String GHOST_SPAWN_TEXT = "Aaarrrooooooo";
	private static final String LAVA_SPAWN_TEXT = "Grrrrrrrrrrrrrr";

	private final Player challenger;
	private List<CerberusGhost> ghosts;
	private int performedAttacks;

	private final NpcCombatState<Cerberus> magicAttackStyle;
	private final NpcCombatState<Cerberus> rangedAttackStyle;
	private final NpcCombatState<Cerberus> meleeAttackStyle;

	private boolean canSpawnGhosts = true;
	private boolean canSpawnLavaPools = true;
	private boolean canAttackPlayer;

	public Cerberus(Player challenger, Location loc) {
		super(NPC_ID, loc);
		this.challenger = challenger;
		this.magicAttackStyle = new CerberusMagicAttackStyle<>(this);
		this.rangedAttackStyle = new CerberusRangedAttackStyle<>(this);
		this.meleeAttackStyle = new CerberusMeleeAttackStyle<>(this);

		this.getSkills().setLevel(Skills.ATTACK, 220);
		this.getSkills().setLevel(Skills.DEFENCE, 100);
		this.getSkills().setLevel(Skills.STRENGTH, 220);
		this.getSkills().setLevel(Skills.RANGE, 220);
		this.getSkills().setLevel(Skills.MAGIC, 220);
		this.getSkills().setLevel(Skills.HITPOINTS, MAX_HEALTH);
		this.getCombatState().setBonuses(BONUSES);
		this.setDirection(Directions.NormalDirection.WEST.npcIntValue());

		this.random = new Random();

		setInteractingEntity(InteractionMode.ATTACK, challenger);
		transition(new IdleCombatState<>(this));
	}

	@Override
	public boolean isAutoRetaliating() {
		return false;
	}

	@Override
	public void tick() {
		if (getCombatState().isDead()) {
			return;
		}
		if (!canAttackPlayer && this.getCombatState().getDamageMap().getTotalDamages().containsKey(challenger)) {
			canAttackPlayer = true;
		}
		if (canAttackPlayer) {
			double distance = getLocation().distance(challenger.getLocation());
			if (distance >= 13) {
				Following.combatFollow(this, challenger);
				return;
			}
			if (performedAttacks == 0) {
				transition(magicAttackStyle);
			} else if (performedAttacks == 1) {
				transition(rangedAttackStyle);
			} else if (performedAttacks == 2) {
				transition(meleeAttackStyle);
				World.getWorld().submit(new StoppingTick(67) {
					@Override
					public void executeAndStop() {
						performedAttacks = 0;
					}
				});
			} else {

				int currentHitpoints = getSkills().getLevel(Skills.HITPOINTS);
				if (currentHitpoints <= 0) {
					return;
				}

				float lavaPoolProbability = random.nextFloat();
				if (currentHitpoints <= 200 && canSpawnLavaPools && lavaPoolProbability >= 0.95) {
					canSpawnLavaPools = false;
					forceChat(LAVA_SPAWN_TEXT);
					World.getWorld().submit(new LavaPoolTickable(this, challenger.getLocation()));
				}

				float ghostProbability = random.nextFloat();

				if (canSpawnGhosts && ghostProbability >= 0.95 && currentHitpoints <= 400) {
					spawnGhosts();
				} else {
					float styleProbability = random.nextFloat();

					if (styleProbability <= 0.33) {
						transition(magicAttackStyle);
					} else if (styleProbability <= 0.66) {
						transition(rangedAttackStyle);
					} else {
						transition(meleeAttackStyle);
					}
				}
			}
		} else {
			transition(new IdleCombatState<>(this));
		}
	}

	public void spawnGhosts() {
		if (ghosts == null || !canSpawnGhosts || !BoundaryManager.isWithinBoundaryNoZ(challenger.getLocation(), "Cerberus")) {
			return;
		}

		playAnimation(HOWL_ANIMATION);
		forceChat(GHOST_SPAWN_TEXT);
		ghosts.forEach(g -> {
			challenger.getInstancedNPCs().add(g);
			g.register();
		});
		canSpawnGhosts = false;
		World.getWorld().submit(new CerberusGhostRegisterTick(this));
	}

	@Override
	public final Animation getDefendAnimation() {
		return BLOCK_ANIMATION;
	}

	@Override
	public final Animation getDeathAnimation() {
		return DEATH_ANIMATION;
	}

	@Override
	public boolean isTurn() {
		return getCombatState().getAttackDelay() == 0;
	}

	public void destroySelf(boolean stop) {
		challenger.getInstancedNPCs().remove(this);
		if (stop) {
			ghosts.forEach(g -> {
				g.unregister();
				challenger.getInstancedNPCs().remove(g);
			});
			ghosts = null;
			challenger.getInstancedNPCs().forEach(World.getWorld()::unregister);
			challenger.getInstancedNPCs().clear();
		}
	}

	@Override
	public void dropLoot(Mob killer) {
		final double chance = challenger.getEquipment().get(Equipment.SLOT_RING) != null && challenger.getEquipment().get(Equipment.SLOT_RING).getId() == 2572 ? 1.1 : 1.0;
		for (final NPCLoot loot : NPCLootTable.forID(this).getGeneratedLoot(chance)) {
			if (loot != null) {
				final Item item = new Item(loot.getItemID(), Misc.random(loot.getMinAmount(), loot.getMaxAmount()));
				Pet.Pets pets = Pet.Pets.from(item.getId());
				if (pets != null) {
					if (challenger.getPet() != null) {
						continue;
					} else {
						PlayerSettingsEntity settings = challenger.getDatabaseEntity().getPlayerSettings();
						Pet pet = new Pet(challenger, pets.getNpc());
						challenger.setPet(pet);
						settings.setPetSpawned(true);
						settings.setPetId(pets.getNpc());
						World.getWorld().register(pet);
						World.getWorld().sendWorldMessage("<col=884422><img=33> News:" + challenger.getName() + " has just received " + item.getCount() + "x " + item.getDefinition2().getName() + ".");
						continue;
					}
				}
				GroundItemDefinition g = new GroundItemDefinition(challenger.getName(), challenger.getLocation(), item.getId(), item.getCount());
				CacheItemDefinition def = CacheItemDefinition.get(loot.getItemID());
				if (def != null && def.getName() != null) {
					String name = def.isNoted() ? CacheItemDefinition.get(loot.getItemID() - 1).getName() : def.getName();
					if (loot.getHitRollCeil() <= 3) {
						World.getWorld().sendWorldMessage("<col=884422><img=33> News:" + challenger.getName() + " has just received " + g.getCount() + "x " + name + ".");
					} else {
						challenger.getActionSender().sendMessage("<col=884422><img=33> News:" + challenger.getName() + " has just received " + g.getCount() + "x " + name + ".");
					}
				}
				World.getWorld().createGroundItem(g, challenger);
			}
		}

	}

	public void setGhosts(ImmutableList<CerberusGhost> ghosts) {
		this.ghosts = ghosts;
	}

	public List<CerberusGhost> getGhosts() {
		return ghosts;
	}

	public Player getChallenger() {
		return challenger;
	}

	public void incrementPerformedAttacks() {
		performedAttacks++;
	}

	public void setCanSpawnGhosts(boolean canSpawnGhosts) {
		this.canSpawnGhosts = canSpawnGhosts;
	}

	public void setCanSpawnLavaPools(boolean canSpawnLavaPools) {
		this.canSpawnLavaPools = canSpawnLavaPools;
	}

	public boolean canAttackPlayer() {
		return canAttackPlayer;
	}
}
