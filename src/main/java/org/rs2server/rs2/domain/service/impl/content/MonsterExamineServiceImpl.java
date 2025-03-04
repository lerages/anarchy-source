package org.rs2server.rs2.domain.service.impl.content;

import org.rs2server.cache.format.CacheItemDefinition;
import org.rs2server.cache.format.CacheNPCDefinition;
import org.rs2server.rs2.domain.service.api.content.MonsterExamineService;
import org.rs2server.rs2.model.npc.NPCLootTable;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.tools.NPCExamineTool;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author Clank1337
 */
public class MonsterExamineServiceImpl implements MonsterExamineService {

	private static final int WIDGET = 275;

	@Override
	public void openMonsterExamine(@Nonnull Player player, int npcId) {

		List<NPCExamineTool.ItemRarity> rarities = NPCLootTable.examineOf(npcId).getRarities().stream().filter(Objects::nonNull).collect(toList());

		if (!NPCLootTable.ITEM_RARITIES_MAP.containsKey(npcId) || rarities.isEmpty()) {
			return;
		}
		Collections.sort(rarities, (o1, o2) -> o1.getRarity() > o2.getRarity() ? -1 : o1.getRarity() == o2.getRarity() ? 0 : 1);

		player.getActionSender().sendString(WIDGET, 2, "OS-Anarchy - <col=884400>" + CacheNPCDefinition.get(npcId).getName() + "</col> droptable:");
		int child = 4;
		for (int i = 0; i < rarities.size(); i++) {
			NPCExamineTool.ItemRarity rarity = rarities.get(i);
			CacheItemDefinition def = CacheItemDefinition.get(rarity.getItemId());
			boolean noted = def.isNoted();
			String name = noted ? def.getNotedName() : def.getName();
		//	player.getActionSender().sendString(WIDGET, child++, name + " -  quantity: [" + rarity.getMinAmount() + "-" + rarity.getMaxAmount() + "]:              " + rarity.getColor() + rarity.toString());
			player.getActionSender().sendString(WIDGET, child++, name + ":                       " + rarity.getColor() + rarity.toString());
		}
		for (int i = rarities.size(); i < 133; i++) {
			player.getActionSender().sendString(WIDGET, i, "");
		}

		player.getActionSender().sendInterface(WIDGET, false);
	}

}
