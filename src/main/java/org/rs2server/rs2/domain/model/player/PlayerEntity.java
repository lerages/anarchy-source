package org.rs2server.rs2.domain.model.player;

import lombok.Getter;
import lombok.Setter;
import org.rs2server.rs2.content.Jewellery;
import org.rs2server.rs2.domain.dao.MongoEntity;
import org.rs2server.rs2.domain.model.player.treasuretrail.PlayerTreasureTrail;
import org.rs2server.rs2.domain.service.api.PermissionService;
import org.rs2server.rs2.domain.service.api.content.GemBagService;
import org.rs2server.rs2.domain.service.api.skill.RunecraftingService;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a persisted player entity.
 *
 * @author tommo
 */

public final
@Setter @Getter
class PlayerEntity extends MongoEntity {

	/**
	 * The account username, used to logging in.
	 */
	private String accountName;

	/**
	 * The ingame display name.
	 */
	private String displayName;

	/**
	 * The previous ingame display name, if it existed.
	 * Defaults to an empty string.
	 */
	private String previousDisplayName;

	/**
	 * The X location of the player.
	 */
	private int locationX;

	/**
	 * The Y location of the player.
	 */
	private int locationY;

	/**
	 * The Z location of the player.
	 */
	private int locationZ;

	private EnumSet<PermissionService.PlayerPermissions> permissions;

	private PlayerStatisticsEntity statistics;

	private PlayerSkillsEntity skills;

	private PlayerSkillSlayerEntity slayerSkill;

	private PlayerSkillFarmingEntity farmingSkill;

	private PlayerDeadmanStateEntity deadmanState;

	private PlayerZulrahStateEntity zulrahState;

	private PlayerSettingsEntity playerSettings;

	private PlayerBankEntity bank;

	private PlayerTreasureTrail treasureTrail;
	
	private boolean ownedPerks[] = {false, false, false, false, false, false, false, false, false, false, false, false};

	private PlayerEquipmentEntity equipment;
	private PlayerBountyHunterEntity bountyHunter;
	private PlayerCombatEntity combatEntity;

	private int coalBagAmount;
	private Map<GemBagService.Gems, Integer> gemBag;
	private Map<RunecraftingService.PouchType, Integer> runePouches;

	private PlayerPrivateChatEntity privateChat;
	
	public void setOwnedPerks(int index, boolean state)
	{
		ownedPerks[index] = state;
	}
	
	public boolean[] getOwnedPerks()
	{
		return ownedPerks;
	}
	
	public PlayerSettingsEntity getPlayerSettings() {
		return playerSettings;
	}
	public void setPlayerSettings(PlayerSettingsEntity playerSettings) {
		this.playerSettings = playerSettings;
	}

	public EnumSet<PermissionService.PlayerPermissions> getPermissions() {
		return permissions;
	}

	public void setPermissions(EnumSet<PermissionService.PlayerPermissions> permissions) {
		this.permissions = permissions;
	}

	public PlayerStatisticsEntity getStatistics() {
		return statistics;
	}

	public void setStatistics(PlayerStatisticsEntity statistics) {
		this.statistics = statistics;
	}

	public PlayerSkillsEntity getSkills() {
		return skills;
	}

	public void setSkills(PlayerSkillsEntity skills) {
		this.skills = skills;
	}

	public int getLocationZ() {
		return locationZ;
	}

	public void setLocationZ(int locationZ) {
		this.locationZ = locationZ;
	}

	public int getLocationY() {
		return locationY;
	}

	public void setLocationY(int locationY) {
		this.locationY = locationY;
	}

	public int getLocationX() {
		return locationX;
	}

	public void setLocationX(int locationX) {
		this.locationX = locationX;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getPreviousDisplayName() {
		return previousDisplayName;
	}

	public void setPreviousDisplayName(String previousDisplayName) {
		this.previousDisplayName = previousDisplayName;
	}

	public PlayerSkillSlayerEntity getSlayerSkill() {
		return slayerSkill;
	}

	public void setSlayerSkill(PlayerSkillSlayerEntity slayerSkill) {
		this.slayerSkill = slayerSkill;
	}

	public PlayerSkillFarmingEntity getFarmingSkill() {
		return farmingSkill;
	}

	public void setFarmingSkill(PlayerSkillFarmingEntity farmingSkill) {
		this.farmingSkill = farmingSkill;
	}

	public PlayerDeadmanStateEntity getDeadmanState() {
		return deadmanState;
	}

	public void setDeadmanState(PlayerDeadmanStateEntity deadmanState) {
		this.deadmanState = deadmanState;
	}

	public PlayerZulrahStateEntity getZulrahState() {
		return zulrahState;
	}

	public void setZulrahState(PlayerZulrahStateEntity zulrahState) {
		this.zulrahState = zulrahState;
	}

	public PlayerBankEntity getBank() {
		return bank;
	}

	public void setBank(PlayerBankEntity bank) {
		this.bank = bank;
	}

	public PlayerTreasureTrail getTreasureTrail() {
		return treasureTrail;
	}

	public void setTreasureTrail(PlayerTreasureTrail treasureTrail) {
		this.treasureTrail = treasureTrail;
	}

	public PlayerEquipmentEntity getEquipment() {
		return equipment;
	}

	public void setEquipment(PlayerEquipmentEntity equipment) {
		this.equipment = equipment;
	}

	public PlayerBountyHunterEntity getBountyHunter() {
		return bountyHunter;
	}

	public void setBountyHunter(PlayerBountyHunterEntity bountyHunter) {
		this.bountyHunter = bountyHunter;
	}

	public PlayerCombatEntity getCombatEntity() {
		return combatEntity;
	}

	public void setCombatEntity(PlayerCombatEntity combatEntity) {
		this.combatEntity = combatEntity;
	}

	public int getCoalBagAmount() {
		return coalBagAmount;
	}

	public void setCoalBagAmount(int coalBagAmount) {
		this.coalBagAmount = coalBagAmount;
	}

	public Map<GemBagService.Gems, Integer> getGemBag() {
		return gemBag;
	}

	public void setGemBag(Map<GemBagService.Gems, Integer> gemBag) {
		this.gemBag = gemBag;
	}

	public Map<RunecraftingService.PouchType, Integer> getRunePouches() {
		return runePouches;
	}

	public void setRunePouches(Map<RunecraftingService.PouchType, Integer> runePouches) {
		this.runePouches = runePouches;
	}

	public PlayerPrivateChatEntity getPrivateChat() {
		return privateChat;
	}

	public void setPrivateChat(PlayerPrivateChatEntity privateChat) {
		this.privateChat = privateChat;
	}
}
