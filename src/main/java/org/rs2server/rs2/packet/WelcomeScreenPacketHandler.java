package org.rs2server.rs2.packet;

import org.rs2server.Server;
import org.rs2server.rs2.content.api.GameInterfaceButtonEvent;
import org.rs2server.rs2.content.api.GameInterfaceEvent;
import org.rs2server.rs2.content.api.pestcontrol.PestControlShopEvent;
import org.rs2server.rs2.domain.service.api.HookService;
import org.rs2server.rs2.model.DialogueManager;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.skills.crafting.LeatherCrafting.LeatherChilds;
import org.rs2server.rs2.model.skills.crafting.LeatherCrafting.LeatherProduction;
import org.rs2server.rs2.model.skills.crafting.LeatherCrafting.LeatherProductionAction;
import org.rs2server.rs2.model.skills.crafting.SpinningWheel;
import org.rs2server.rs2.model.skills.crafting.SpinningWheel.SpinItem;
import org.rs2server.rs2.model.skills.crafting.Tanning;
import org.rs2server.rs2.model.skills.crafting.Tanning.Hides;
import org.rs2server.rs2.model.skills.smithing.Smelting;
import org.rs2server.rs2.model.skills.smithing.SmithingUtils.SmeltingBar;
import org.rs2server.rs2.net.Packet;

public class WelcomeScreenPacketHandler implements PacketHandler {

	@Override
	public void handle(Player player, Packet packet) {
		int interfaceHash = packet.getInt();
		int interfaceId = interfaceHash >> 16;
		int childId = interfaceHash & 0xFFFF;
		if (interfaceId == 267) {
			HookService hookService = Server.getInjector().getInstance(HookService.class);
			hookService.post(new PestControlShopEvent(player, childId));
		}

		if (interfaceId == 378 && childId == 6) {
			player.loadedIn = true;
			player.getActionSender().removeInterfaces(165, 26);
			player.getActionSender().removeInterfaces(165, 25);
			player.getActionSender().sendWindowPane(player.getAttribute("tabmode"));
			player.getActionSender().sendSidebarInterfaces();
			player.getActionSender().sendAreaInterface(null, player.getLocation());
			boolean starter = player.getAttribute("starter");
			if (starter) {
				//player.removeAttribute("busy");
				player.getActionSender().sendInterface(269, false);//269 - Character Creation, 215 - Ironman Interface
				//player.setAttribute("busy", true);
				//DialogueManager.openDialogue(player, 19000);
				World.getWorld().sendWorldMessage("[<img=22>]<col=04a00c> " + player.getName() +" has just joined OS-Anarchy!");
			}
		} else if (interfaceId == 324) {
			int amount = childId >= 148 && childId <= 155 ? 1 : childId >= 140 && childId <= 147 ? 5 : childId >= 132 && childId <= 139 ? -1 : childId >= 124 && childId <= 131 ? 28 : 0;
			if (amount < 0) {
				return;
			}
			Hides hide = Hides.forId(childId);
			if (hide != null) {
				if (Tanning.startTanning(player, hide, childId, amount)) {
					return;
				}
			}
		} else if (interfaceId == 154) {
			LeatherChilds child = LeatherChilds.forId(childId);
			if (child != null) {
				int amount = child.getAmount(childId);
				if (amount != 0) {
					LeatherProduction prod = child.getLeatherProduction();
					if (amount == -1) {
						player.getActionSender().sendEnterAmountInterface();
						player.setInterfaceAttribute("toProd", prod);
						return;
					}
					if (prod != null) {
						LeatherProduction toProduce = prod;
						if (toProduce != null) {
							LeatherProductionAction produceAction = new LeatherProductionAction(player, toProduce,  amount);
							produceAction.execute();
							if (produceAction.isRunning()) {
								player.submitTick("skill_action_tick", produceAction, true);
							}
							player.getActionSender().removeInterface2();
						}
					}
				}
			}
		} else if (interfaceId == 311) {
			SmeltingBar bar = null;
			if (childId >= 13 && childId <= 16) {
				bar = SmeltingBar.BRONZE;
			} else if (childId >= 17 && childId <= 20) {
				bar = SmeltingBar.BLURITE;
			} else if (childId >= 21 && childId <= 24) {
				bar = SmeltingBar.IRON;
			} else if (childId >= 25 && childId <= 28) {
				bar = SmeltingBar.SILVER;
			} else if (childId >= 29 && childId <= 32) {
				bar = SmeltingBar.STEEL;
			} else if (childId >= 33 && childId <= 36) {
				bar = SmeltingBar.GOLD;
			} else if (childId >= 37 && childId <= 40) {
				bar = SmeltingBar.MITHRIL;
			} else if (childId >= 41 && childId <= 44) {
				bar = SmeltingBar.ADAMANT;
			} else if (childId >= 45 && childId <= 48) {
				bar = SmeltingBar.RUNE;
			}
			if (bar != null) {
				int amount = bar.getAmount(childId);
				if (amount ==  -1) {
					player.getActionSender().removeInterfaces(162, 546);
					player.getActionSender().sendEnterAmountInterface();
					player.setAttribute("smelting_bar", bar);
					return;
				}
				if (amount != 0) {
					player.getActionQueue().addAction(new Smelting(player, bar, amount));
					player.getActionSender().removeChatboxInterface();
				}
			}
		} else if (interfaceId == 459) {
			SpinItem spin = null;
			if (childId >= 97 && childId <= 100) {
				spin = SpinItem.BALL_OF_WOOL;
			} else if (childId >= 92 && childId <= 95) {
				spin = SpinItem.BOW_STRING;
			} else if (childId >= 104 && childId <= 107) {
				spin = SpinItem.MAGIC_AMULET_STRING;
			} else if (childId >= 118 && childId <= 121 || childId >= 111 && childId <= 114) {
				spin = SpinItem.CROSSBOW_STRING;
			} else if (childId >= 125 && childId <= 128) {
				spin = SpinItem.ROPE;
			}
			
			if (spin != null) {
				int amount = getAmount(childId);
				if (amount ==  -1) {
					return;
				}
				player.getActionQueue().addAction(new SpinningWheel(player, spin, amount));
				player.getActionSender().removeAllInterfaces();
			}
		}
		
	}
	
	public int getAmount(int child) {
		return child == 100 || child == 95 || child == 107 || child == 121 
				|| child == 114 || child == 128 ? 1 : 
				child == 99 
				|| child == 94 || child == 106 || child == 120 
				|| child == 113 || child == 127 ? 5 : child == 98 
				|| child == 93 || child == 105 || child == 119
				|| child == 112 || child == 126 ? 10 : child == 97 
				|| child == 92 || child == 104 || child == 118
				|| child == 112 || child == 125 ? 28 : -1;
	}

}
