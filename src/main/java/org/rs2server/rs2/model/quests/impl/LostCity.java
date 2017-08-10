package org.rs2server.rs2.model.quests.impl;

import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.Shop;
import org.rs2server.rs2.model.Skills;
import org.rs2server.rs2.model.Animation.FacialAnimation;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.model.quests.Quest;
import org.rs2server.rs2.net.ActionSender.DialogueType;

public class LostCity /*extends Quest<LostCityStates>*/ {
	
	/*private Item DRAMEN_STAFF = new Item(772, 1);
	
	private int config = 147;
	
	public LostCity(Player player, LostCityStates state) {
		super(player, state);
	}
	
	public boolean hasRequirements() {
		return player.getSkills().getLevelForExperience(Skills.CRAFTING) > 31 || player.getSkills().getLevelForExperience(Skills.WOODCUTTING) > 36;
	}

	@Override
	public void updateProgress() {
		for(int i = 0; i < 5; i++) {
			setNextDialogueId(i, -1);
		}
		switch (state) {
		case NOT_STARTED:
			openDialogue(0);
			break;
		
		case STARTED:
			openDialogue(11);
			player.getActionSender().sendConfig(config, 1);
			break;
			
		case COMPLETED:
			openDialogue(18);
			setState(LostCityStates.COMPLETED);
			player.getActionSender().sendConfig(config, 6);
			player.getActionSender().sendConfig(101, 3);
			if (player.getAttribute("talkingNpc") != null) {
                switch ((int) player.getAttribute("talkingNpc")) {
                    case 1158:
                        openDialogue(2000);
                        break;
                }
            }
			break;
		
		}
		
		
	}
	
	public void advanceDialogue(int index) {
		int dialogueId = getNextDialogueId(index);
		if(dialogueId == -1) {
			player.getActionSender().removeChatboxInterface();
			return;
		}
		openDialogue(dialogueId);
	}

	public void openDialogue(int dialogue) {
		if(dialogue == -1) {
			return;
		}
		switch (dialogue) {
		*//**
		 * State = NOT_STARTED
		 *//*
		case 0:// not enough 
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Hello there, traveler.");
			setNextDialogueId(0, 1);
			break;
		case 1:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Why are you camped out here?");
			setNextDialogueId(0, 2);
			break;
		case 2:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Do you know any good adventurers I can go on?");
			setNextDialogueId(0, 3);
			break;
		case 3:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Well, we're on an adventure right now. Mind you, this is OUR adventure and we don't want to share it - find your own!");
			setNextDialogueId(0, 4);
			break;
		case 4:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Please tell me?");
			setNextDialogueId(0, 5);
			break;
		case 5:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "No.");
			setNextDialogueId(0, 6);
			break;
		case 6:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Please?");
			setNextDialogueId(0, 7);
			break;
		case 7:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "No!");
			setNextDialogueId(0, 8);
			break;
		case 8:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "PLEEEEEEEEEEEEEEEEEEEEEEASE???");
			setNextDialogueId(0, 9);
			break;
		case 9:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Fine...We're looking for Zanaris, would you like to help?");
			setNextDialogueId(0, 10);
			break;
		case 10:
			player.getActionSender().sendDialogue("Select an Option", DialogueType.OPTION, -1, FacialAnimation.DEFAULT, 
					"Yes i'd love to help you!|No thank you, i'm busy.");
			setNextDialogueId(0, 11);
			setNextDialogueId(1, 12);
			break;
		case 11:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Yes, i'd love to help you!");
			setState(LostCityStates.STARTED);
			player.getActionSender().sendConfig(147, 1);
			setNextDialogueId(0, 13);
			break;
		case 12:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "No thank you, i'm busy.");
			player.getActionSender().removeChatboxInterface();
			break;
		case 13:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Great, the Leprechaun told us we have to fight a Tree Spirit and get a Dramen staff?");
			setNextDialogueId(0, 14);
			break;
		case 14:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Where do i find this Tree Spirit?");
			setNextDialogueId(0, 15);
			break;
		case 15:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "You must travel to Entrana and kill the Tree Spirit and make a Dramen staff out of the tree in the dungeon.");
			setNextDialogueId(0, 16);
			break;
		case 16:
			player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Okay, ill be on my way!");
			setNextDialogueId(0, 17);
			break;
		case 17:
			sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Before you go, remember, once you get the Dramen staff, come back and talk to me again.");
			setNextDialogueId(0, 18);
			break;
		
		case 18:
			if (player.getInventory().contains(772)) {
				sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Thank you, you may enter Zanaris whenever you like.");
				player.getInventory().remove(DRAMEN_STAFF);
				setState(LostCityStates.COMPLETED);
				player.getInventory().add(new Item(1602, 3));
				player.getInventory().add(new Item(995, 10000));
				player.getActionSender().sendConfig(101, 3);
				player.getActionSender().sendConfig(147, 6);
				setNextDialogueId(0, 19);
			} else {
				sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Go get a Dramen staff then come back and talk to me.");
				setNextDialogueId(0, -1);
			}
			break;
		case 2000:
            sendDialogue("Warrior", DialogueType.NPC, 1158, FacialAnimation.DEFAULT, "Hey there, " + player.getName());
            setNextDialogueId(0, 2001);
            break;
        case 2001:
        	player.getActionSender().sendDialogue(player.getName(), DialogueType.PLAYER, -1, FacialAnimation.DEFAULT, "Can I see your shop?|Nevermind.");
            setNextDialogueId(0, 2002);
            setNextDialogueId(1, -1);
            break;
        case 2002:
            player.getActionSender().removeChatboxInterface();
            Shop.open(player, 26, 2);
            break;
		}
	}
			public void showQuestInterface() {
				player.getActionSender().sendString(275, 2, "<col=800000>Lost City");
				boolean started = state != LostCityStates.NOT_STARTED;
				if (started) {
					switch (state) {
					case NOT_STARTED:
						player.getActionSender().sendString(275, 4, "Speak to the Warrior in the Lumbridge Swamp to begin this quest");
						player.getActionSender().sendString(275, 5, "<col=800000>Requirements:");
						player.getActionSender().sendString(275, 6, "<col=000080>31 Crafting");
						player.getActionSender().sendString(275, 7, "<col=000080>36 Woodcutting");
						for (int i = 6; i <= 133; i++) {
							player.getActionSender().sendString(275, i, "");
						}
						break;
						
					case STARTED:
						player.getActionSender().sendString(275, 4, "The Warrior said i should head over to Entrana and kill");
						player.getActionSender().sendString(275, 5, "the Tree Spirit.");
						player.getActionSender().sendString(275, 6, "<col=880000>Note: Once you kill the Tree Spirit, cut");
						player.getActionSender().sendString(275, 7, "the dramen tree and fletch a dramen staff and return");
						player.getActionSender().sendString(275, 8, "to the warrior");
						for (int i = 6; i <= 133; i++) {
							player.getActionSender().sendString(275, i, "");
						}
						break;
						
					case COMPLETED:
						player.getActionSender().sendString(275, 4, "The Warrior said i should head over to Entrana and kill");
						player.getActionSender().sendString(275, 5, "the Tree Spirit.");
						player.getActionSender().sendString(275, 6, "<col=880000>Note: Once you kill the Tree Spirit, cut");
						player.getActionSender().sendString(275, 7, "the dramen tree and fletch a dramen staff and return");
						player.getActionSender().sendString(275, 8, "to the warrior");
						player.getActionSender().sendString(275, 9, "<col=ff0000>QUEST COMPLETE!");
						player.getActionSender().sendString(275, 10, "<col=800000>Reward:");
						player.getActionSender().sendString(275, 11, "<col=000080>3 Diamonds");
						player.getActionSender().sendString(275, 12, "<col=000080>Access to Zanaris");
						player.getActionSender().sendString(275, 13, "<col=000080>3 Quest points");
						player.getActionSender().sendString(275, 14, "<col=000080>Access to The Warrior's Shop");
						for (int i = 6; i <= 133; i++) {
							player.getActionSender().sendString(275, i, "");
						}
						break;
			}
		}
	}*/
}
