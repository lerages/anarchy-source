package org.rs2server.rs2.domain.service.impl.skill;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.rs2server.rs2.content.api.GameInterfaceButtonEvent;
import org.rs2server.rs2.content.api.GameNpcActionEvent;
import org.rs2server.rs2.content.api.GameObjectActionEvent;
import org.rs2server.rs2.content.api.GamePlayerLoginEvent;
import org.rs2server.rs2.content.api.GamePlayerRegionEvent;
import org.rs2server.rs2.domain.model.player.PlayerSkillFarmingEntity;
import org.rs2server.rs2.domain.service.api.HookService;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPatch;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPatchState;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPatchTreatment;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPatchType;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingPlantable;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingService;
import org.rs2server.rs2.domain.service.api.skill.farming.FarmingTool;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingClearingAction;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingCureAction;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingHarvestingAction;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingPlantingAction;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingRakeAction;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingTreatmentAction;
import org.rs2server.rs2.domain.service.api.skill.farming.action.FarmingWateringAction;
import org.rs2server.rs2.model.bit.BitConfig;
import org.rs2server.rs2.model.bit.BitConfigBuilder;
import org.rs2server.rs2.model.GameObject;
import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.Skill;
import org.rs2server.rs2.model.boundary.Area;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.util.Misc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author tommo
 */
public class FarmingServiceImpl implements FarmingService {

	private static final Logger logger = LoggerFactory.getLogger(FarmingServiceImpl.class);
	private final PermissionService permissionService;

	public static final int INTERFACE_TOOL_STORE_ID = 125;
	public static final int INTERFACE_TOOL_STORE_INVENTORY_ID = 126;

	private static final Item ITEM_SEED_DIBBER = new Item(5343, 1);
	private static final Item ITEM_WATERING_CAN = new Item(5340, 1);
	private static final Item ITEM_SUPERCOMPOST = new Item(6034, 1);

	private static final int NPC_TOOL_LEPRECHAUN_ID = 0;
	private static final int NPC_FARMER = 2664;

	//2812, 3464

	// catherby: 529
	// herb spot: sppp ppp << 24
	// allotment north: sspp pppp
	// allotment south: sspp pppp << 8

	@Inject
	FarmingServiceImpl(final HookService hookService, PermissionService permissionService) {
		hookService.register(this);
		this.permissionService = permissionService;
	}

	@Subscribe
	public void onPlayerLogin(final GamePlayerLoginEvent event) {
		final Player player = event.getPlayer();
		final PlayerSkillFarmingEntity farming = player.getDatabaseEntity().getFarmingSkill();

		farming.getPatches().entrySet().stream()
				.filter(entry -> entry.getValue() != null)
				.forEach(entry -> {
					final FarmingPatchState patch = entry.getValue();
					updatePatch(patch);
					sendPatches(player);
				});
	}

	@Subscribe
	public void onGameObjectActionEvent(final GameObjectActionEvent event) {
		final Player player = event.getPlayer();
		final GameObject object = event.getGameObject();
		final PlayerSkillFarmingEntity farming = player.getDatabaseEntity().getFarmingSkill();

		final FarmingPatch farmingPatch = FarmingPatch.forObjectIdAndLocation(object.getId(), object.getLocation());
		if (farmingPatch == null) {
			return;
		}

		if (!farming.getPatches().containsKey(farmingPatch)) {
			farming.getPatches().put(farmingPatch, new FarmingPatchState(farmingPatch));
		}
		final FarmingPatchState patchState = farming.getPatches().get(farmingPatch);

		if (event.getActionType() == GameObjectActionEvent.ActionType.OPTION_1) {
			if (isPatchFullyGrown(patchState)) {
				harvest(player, patchState, object);
			} else if (patchState.isDiseased()) {
				if (player.getInventory().contains(FarmingCureAction.ITEM_PLANT_CURE.getId())) {
					cure(player, patchState, object);
				} else {
					player.getActionSender().sendMessage("You need plant cure to cure this patch.");
				}
			} else if (patchState.isDead()) {
				clear(player, patchState, object);
			} else if (patchState.getWeedLevel() < 3) {
				rake(player, patchState, object);
			}
		} else if (event.getActionType() == GameObjectActionEvent.ActionType.OPTION_2) {
			inspect(player, patchState);
		} else if (event.getActionType() == GameObjectActionEvent.ActionType.ITEM_ON_OBJECT) {
			assert event.getItem() != null;

			// Curing
			if (event.getItem().getId() == FarmingCureAction.ITEM_PLANT_CURE.getId() && !isPatchFullyGrown(patchState)
					&& patchState.isDiseased() && player.getInventory().contains(FarmingCureAction.ITEM_PLANT_CURE.getId())) {
				cure(player, patchState, object);
			}

			// Raking
			if (event.getItem().getId() == FarmingTool.RAKE.getItemId() && patchState.getWeedLevel() < 3
					&& player.getInventory().contains(FarmingTool.RAKE.getItemId())) {
				rake(player, patchState, object);
			}

			// Treating
			if (patchState.getWeedLevel() == 3 && patchState.getTreatment() == FarmingPatchTreatment.NOT_TREATED) {
				final FarmingPatchTreatment treatment = FarmingPatchTreatment.forItemId(event.getItem().getId());

				if (treatment != null) {
					treat(player, patchState, treatment, object);
				}
			}

			// Watering
			if (patchState.getPlanted() != null && !patchState.isWatered() && event.getItem().getId() == ITEM_WATERING_CAN.getId() && patchState.getWeedLevel() == 3) {
				if (patchState.getPatch().getType().isWaterable() && !isPatchFullyGrown(patchState)
						&& !patchState.isDiseased() && !patchState.isDead()) {
					water(player, patchState, object);
				}
			}

			// Planting
			if (patchState.getWeedLevel() == 3 && patchState.getPlanted() == null) {
				final FarmingPlantable plantable = FarmingPlantable.forSeedItemId(event.getItem().getId());

				// Check if the plantable can be planted in this patch type
				if (plantable != null && farmingPatch.getType() == plantable.getType()) {
					if (player.getSkills().getLevel(Skill.FARMING.getId()) < plantable.getRequiredLevel()) {
						player.getActionSender().sendMessage("You need a Farming level of " + plantable.getRequiredLevel() + " to plant that.");
					} else if (player.getInventory().contains(ITEM_SEED_DIBBER.getId())) {
						plant(player, patchState, plantable, object);
					} else {
						player.getActionSender().sendMessage("You need a Seed dibber to plant seeds.");
					}
				}
			}
		}
	}

	private void inspect(@Nonnull Player player, @Nonnull FarmingPatchState patch) {
		final StringBuilder builder = new StringBuilder();
		builder.append("This is a ").append(patch.getPatch().getType().toString()).append(".");
		//updatePatch(patch);

		if (patch.getTreatment() == FarmingPatchTreatment.NOT_TREATED) {
			builder.append(" The soil has not been treated.");
		} else {
			builder.append(" The soil has been treated with ").append(patch.getTreatment().name().toLowerCase()).append(".");
		}

		if (patch.getPlanted() != null) {
			if (isPatchFullyGrown(patch)) {
				builder.append(" The patch is fully grown.");
			} else if (patch.isDiseased()) {
				builder.append(" The patch is diseased and needs attending to before it dies.");
			} else if (patch.isDead()) {
				builder.append(" The patch has become infected by disease and has died.");
			} else {
				builder.append(" The patch has something growing in it.");
			}
		} else {
			if (patch.getWeedLevel() < 3) {
				builder.append(" The patch needs weeding.");
			} else {
				builder.append(" The patch is empty and weeded.");
			}
		}

		player.getActionSender().sendMessage(builder.toString());
	}

	private void cure(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingCureAction(player, patch));
	}

	private void clear(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingClearingAction(player, patch));
	}

	private void treat(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull FarmingPatchTreatment treatment, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingTreatmentAction(player, patch, treatment));
	}

	private void water(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingWateringAction(player, patch));
	}

	private void harvest(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingHarvestingAction(player, patch));
	}

	private void plant(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull FarmingPlantable plantable, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingPlantingAction(player, patch, plantable));
	}

	private void rake(@Nonnull Player player, @Nonnull FarmingPatchState patch, @Nonnull GameObject object) {
		player.faceObject(object);
		player.getActionQueue().addAction(new FarmingRakeAction(player, patch));
	}

	/**
	 * Randomly decides if a patch should become diseased based on the state of it.
	 *
	 * @param patch The patch.
	 * @return true for diseased, false if not.
	 */
	private boolean randomlyDisease(@Nonnull FarmingPatchState patch) {
		if (!patch.getPatch().getType().isVulnerableToDisease()) {
			return false;
		}

		int modifier = 0;
		modifier += (patch.getTreatment().getYieldIncrease() * 3);
		modifier += (patch.isWatered() ? 3 : 0);

		return Misc.random(10 + modifier) == 1;
	}

	@Subscribe
	public void onRegionChange(final GamePlayerRegionEvent event) {
		final Player player = event.getPlayer();
		if (player.isActive()) {
			sendPatches(event.getPlayer());
		}
	}

	@Subscribe
	public void onNpcAction(final GameNpcActionEvent event) {
		final Player player = event.getPlayer();
		if (event.getActionType() == GameNpcActionEvent.ActionType.OPTION_1 || event.getActionType() == GameNpcActionEvent.ActionType.OPTION_2) {
			if (event.getNpc().getId() == NPC_TOOL_LEPRECHAUN_ID) {
				openToolInterface(player);
			}
		} else if (event.getActionType() == GameNpcActionEvent.ActionType.ITEM_ON_NPC) {
			final Item item = event.getItem();
			assert item != null;
			if (event.getNpc().getId() != NPC_TOOL_LEPRECHAUN_ID) {
				return;
			}
			final FarmingPlantable plantable = FarmingPlantable.forRewardItemId(item.getId());

			if (plantable != null) {
				final int amount = player.getInventory().getCount(plantable.getReward());
				final Item unnoted = new Item(plantable.getReward(), amount);
				final Item noted = new Item(plantable.getReward() + 1, amount);
				player.getInventory().remove(unnoted);
				player.getInventory().addItemIgnoreStackPolicy(noted);
				player.getActionSender().sendMessage("The Leprechaun exchanges your items into notes.");
			}
		}
	}

	@Subscribe
	public void onInterfaceButtonClick(final GameInterfaceButtonEvent event) {
		final Player player = event.getPlayer();
		if (event.getInterfaceId() == INTERFACE_TOOL_STORE_INVENTORY_ID) {
			final FarmingTool tool = FarmingTool.forInventoryActionButtonId(event.getButton());
			if (tool == null || !player.getInventory().contains(tool.getItemId())) {
				return;
			}

			final PlayerSkillFarmingEntity farming = player.getDatabaseEntity().getFarmingSkill();
			if (farming.getToolStore().containsKey(tool)) {
				final int stored = farming.getToolStore().get(tool);
				if (stored >= tool.getMaxAmount()) {
					player.getActionSender().sendMessage("You cannot store more than " + tool.getMaxAmount() + " "
							+ new Item(tool.getItemId()).getDefinition2().getName() + " in here.");
				} else {
					farming.getToolStore().put(tool, farming.getToolStore().get(tool) + 1);
					player.getInventory().remove(new Item(tool.getItemId(), 1));
				}
			} else {
				farming.getToolStore().put(tool, 1);
				player.getInventory().remove(new Item(tool.getItemId(), 1));
			}
		} else if (event.getInterfaceId() == INTERFACE_TOOL_STORE_ID) {
			final FarmingTool tool = FarmingTool.forStoreActionButtonId(event.getButton());
			if (tool == null) {
				return;
			}

			final PlayerSkillFarmingEntity farming = player.getDatabaseEntity().getFarmingSkill();
			if (farming.getToolStore().containsKey(tool)) {
				final int amount = farming.getToolStore().get(tool);
				if (amount > 0) {
					final Item item = new Item(tool.getItemId(), 1);
					if (!player.getInventory().hasRoomFor(item)) {
						player.getActionSender().sendMessage("Your inventory is full.");
						return;
					} else {
						player.getInventory().add(item);
						if (amount - 1 == 0) {
							farming.getToolStore().remove(tool);
						} else {
							farming.getToolStore().put(tool, amount - 1);
						}
					}
				}
			}
		}

		sendToolInterface(player);
	}

	private void sendToolInterface(@Nonnull Player player) {
		final BitConfigBuilder config = BitConfigBuilder.of(615);
		player.getDatabaseEntity().getFarmingSkill().getToolStore().entrySet().forEach(t -> {
			final FarmingTool tool = t.getKey();
			final int amount = t.getValue();

			if (tool == FarmingTool.RAKE) {
				config.or(0x1);
			} else if (tool == FarmingTool.SEED_DIBBER) {
				config.or(0x2);
			} else if (tool == FarmingTool.SPADE) {
				config.or(0x4);
			} else if (tool == FarmingTool.SECATEURS) {
				config.or(0x8);
			} else if (tool == FarmingTool.TROWEL) {
				config.or(0x100);
			} else if (tool == FarmingTool.WATERING_CAN) {
				config.or(0x30);
			} else if (tool == FarmingTool.BUCKET) {
				config.or(amount << 9);
			} else if (tool == FarmingTool.COMPOST) {
				config.or(amount << 14);
			} else if (tool == FarmingTool.SUPERCOMPOST) {
				config.or(amount << 22);
			}
		});

		player.getActionSender().sendConfig(config.build());
	}

	@Override
	public void openToolInterface(@Nonnull Player player) {
		player.getActionSender().sendInterface(INTERFACE_TOOL_STORE_ID, false);
		player.getActionSender().sendInterfaceInventory(INTERFACE_TOOL_STORE_INVENTORY_ID);

		sendToolInterface(player);
	}

	@Override
	public void clearPatch(@Nonnull Player player, @Nonnull FarmingPatchState patch) {
		patch.setGrowth(0);
		patch.setPlanted(null);
		patch.setTreatment(FarmingPatchTreatment.NOT_TREATED);
		patch.setWatered(false);
		patch.setDiseased(false);
		patch.setDead(false);
		patch.setImmune(false);
		updateAndSendPatches(player, patch);
	}

	@Override
	public void updateAndSendPatches(@Nonnull Player player, @Nonnull FarmingPatchState patch) {
		updatePatch(patch);
		sendPatches(player);
	}

	@Override
	public void updatePatch(@Nonnull FarmingPatchState patch) {
		final DateTime now = DateTime.now(DateTimeZone.UTC);

		// Check if whatever is planted can grow
		if (patch.getPlanted() != null) {
			if (!isPatchFullyGrown(patch)) {
				// Check if the current stage is ready to grow
				if (now.isAfter(patch.getLastGrowthTime().plus(patch.getPlanted().getGrowthTime()))) {
					patch.setLastGrowthTime(now);
					patch.setWatered(false);

					// Choose whether to disease, kill, or grow the crop
					if (!patch.isDead() && !patch.isDiseased() && randomlyDisease(patch) && patch.getPatch().getType() != FarmingPatchType.HERB_PATCH) {
						patch.setDiseased(true);
						//logger.info("Crop " + patch.getPlanted().name() + " diseased for " + player.getName());
					} else if (patch.isDiseased()) {
						patch.setDiseased(false);
						patch.setDead(true);
						//logger.info("Crop " + patch.getPlanted().name() + " died for " + player.getName());
					} else if (!patch.isDead()) {
						//logger.info("Growing...");
						patch.setGrowth(patch.getGrowth() + 1);
						
						if (isPatchFullyGrown(patch)) {
							// The plantable has finished growing.
							//logger.info("Crop " + patch.getPlanted().name() + " fully grown for " + player.getName());
							patch.setYield(Misc.random(patch.getPlanted().getMinYield(), patch.getPlanted().getMaxYield()) + patch.getTreatment().getYieldIncrease());
						}
					}
				}
			} else {
				// The plantable is fully grown..
			}
		} else {
			// Check if weed should grow back
			if (patch.getWeedLevel() > 0 && (patch.getLastGrowthTime() == null || now.isAfter(patch.getLastGrowthTime().plus(Duration.standardMinutes(1))))) {
				patch.setLastGrowthTime(now);
				patch.setWeedLevel(patch.getWeedLevel() - 1);
			}
		}
	}

	@Override
	public void sendPatches(@Nonnull Player player) {
		final Map<Integer, BitConfigBuilder> configMap = newHashMap();

		// We cannot send each patch config 1 by 1 since they are packed, and therefore since
		// different patches may have different config ids, we eagerly construct them.
		player.getDatabaseEntity().getFarmingSkill().getPatches().entrySet().stream()
				.filter(p -> {
					/*
					 * Oddly enough, RS uses the same config ID for all farming patches, and the config is region specific.
					 * This then inherently addresses the issue where all patches would be mirrored across Gielinor.
					/* */
					final Area patchArea = p.getKey().getAreas().get(0);
					final Location patchLocation = Location.create(patchArea.getBottomLeftX(), patchArea.getBottomLeftY());

					//return World.getWorld().getRegionManager().getRegionByLocation(patchLocation).getPlayers().contains(player);

					// TODO the above fix does not work since region changes are not detected, this is a quickfix.
					return player.getLocation().distance(patchLocation) <= 56;
				})
				.forEach(p -> {
					final FarmingPatch key = p.getKey();
					final FarmingPatchType type = key.getType();
					final FarmingPatchState patch = p.getValue();
					final BitConfigBuilder config = configMap.getOrDefault(key.getConfigId(), new BitConfigBuilder(key.getConfigId()));

					//logger.info("Sending patch " + key.name());

					if (patch.getPlanted() != null) {
						config.set(patch.getGrowth(), key.getConfigBitOffset());
					} else {
						config.set(patch.getWeedLevel(), key.getConfigBitOffset());
					}

					if (patch.isWatered()) {
						if (type == FarmingPatchType.ALLOTMENT || type == FarmingPatchType.FLOWER_PATCH) {
							config.set(1 << type.getStateBitOffset(), key.getConfigBitOffset());
						}
					} else if (patch.isDiseased()) {
						if (type == FarmingPatchType.ALLOTMENT || type == FarmingPatchType.FLOWER_PATCH) {
							config.set(2 << type.getStateBitOffset(), key.getConfigBitOffset());
						} else if (type == FarmingPatchType.HERB_PATCH) {
							// TODO fix this, doesn't work
							config.set(1 << type.getStateBitOffset(), key.getConfigBitOffset());
						}
					} else if (patch.isDead()) {
						if (type == FarmingPatchType.ALLOTMENT || type == FarmingPatchType.FLOWER_PATCH) {
							config.set(3 << type.getStateBitOffset(), key.getConfigBitOffset());
						} else if (type == FarmingPatchType.HERB_PATCH) {
							// TODO fix this, doesn't work
							config.set(0xAB, key.getConfigBitOffset());
						}
					}

					configMap.put(key.getConfigId(), config);
				});

		configMap.entrySet().stream().forEach(e -> {
			//logger.info("Config " + e.getKey() + ": " + Integer.toBinaryString(e.getValue().build().getValue()));
			final BitConfig config = e.getValue().build();
			player.getActionSender().sendConfig(config.getId(), config.getValue());
		});
	}

	private boolean isPatchFullyGrown(final FarmingPatchState patch) {
		return patch.getPlanted() != null && patch.getGrowth() >= patch.getPlanted().getMaxGrowth();
	}
}
