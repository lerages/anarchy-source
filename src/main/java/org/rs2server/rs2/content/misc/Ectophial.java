package org.rs2server.rs2.content.misc;

import org.rs2server.rs2.Constants;
import org.rs2server.rs2.model.Animation;
import org.rs2server.rs2.model.Location;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.tickable.Tickable;

/**
 * Created by Zaros
 */
public class Ectophial {
   
	private static final Location ECTOFUNTUS = Location.create(3656, 3517, 0);
	private static final int ECTOPHIAL = 4251;
	private static boolean teleporting = false;

    public static void teleport(Player player) 
    {
        if(!player.getInventory().contains(ECTOPHIAL))
        {
        	return;
        }
        if(!teleporting)
        {
        	teleporting = true;
        	player.setAttribute("busy", true);
            player.playAnimation(Animation.create(1649));
            player.sendMessage("You empty the ectophial...");
            World.getWorld().submit(new Tickable(4) {
                @Override
                public void execute() {
                    this.stop();
                    player.setTeleportTarget(ECTOFUNTUS);
                    player.removeAttribute("busy");
                    teleporting = false;
                }

            });
        }
    }
}
