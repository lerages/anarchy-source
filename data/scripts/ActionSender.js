load("nashorn:mozilla_compat.js");

importPackage(org.rs2server.rs2.model)
importPackage(org.rs2server.rs2.util)

function sendLogin(player) {
	player.getActionSender().sendMessage("Welcome to OS-Anarchy.");	
	player.getActionSender().sendMessage("Want to chat or need help? Join the '<col=ff0000>help</col>' clanchat!");	
	//player.getActionSender().sendMessage("Have any suggestions or issues? Contact any member of the <img=1>staff team.");
			//" <img=46>, " +
			//"<img=0>, + <img=1>.");
	//player.getActionSender().sendMessage("<col=ff0000><img=35> Latest Update: We've just launched! 21 Feb 2017 <img=35>");
	//player.getActionSender().sendMessage("<col=ff0000><img=1>Join the CC 'Paine' for help.<img=1>");
	player.getActionSender().sendSkillLevels();
	player.getActionSender().sendEnergy();
	player.getActionSender().updateRunningConfig();
	player.getActionSender().sendScreenBrightness();
	player.getActionSender().sendString(593, 2, "Combat lvl: " + player.getSkills().getCombatLevel());
	//player.getActionSender().updateQuestText();
	player.getActionSender().updateSplitPrivateChatConfig();
	player.getActionSender().updateAutoRetaliateConfig();
	player.getActionSender().updateClickPriority();
	player.getActionSender().updateSoundVolume();
}