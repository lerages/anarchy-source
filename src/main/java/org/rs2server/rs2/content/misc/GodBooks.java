package org.rs2server.rs2.content.misc;

import org.rs2server.rs2.model.Item;
import org.rs2server.rs2.model.player.Player;
import org.rs2server.rs2.net.ActionSender;

/**
 * Created by Zaros
 */
public class GodBooks {
   
	private static final int DAMAGED_SARADOMIN_BOOK = 3839;
	private static final int DAMAGED_ZAMORAK_BOOK = 3841;
	private static final int DAMAGED_GUTHIX_BOOK = 3843;
	private static final int DAMAGED_BANDOS_BOOK = 12607;
	private static final int DAMAGED_ARMADYL_BOOK = 12609;
	private static final int DAMAGED_ANCIENT_BOOK = 12611;
	
	private static final int HOLY_BOOK = 3840;
	private static final int UNHOLY_BOOK = 3842;
	private static final int BOOK_OF_BALANCE = 3844;
	private static final int BOOK_OF_WAR = 12608;
	private static final int BOOK_OF_LAW = 12610;
	private static final int ANCIENT_BOOK = 12612;
	
	private static final int SARADOMIN_PAGES[] = {3831, 3832, 3833, 3834};
	private static final int ZAMORAK_PAGES[] = {3831, 3832, 3833, 3834};
	private static final int GUTHIX_PAGES[] = {3831, 3832, 3833, 3834};
	private static final int BANDOS_AGES[] = {3831, 3832, 3833, 3834};
	private static final int ARMADYL_PAGES[] = {3831, 3832, 3833, 3834};
	private static final int ANCIENT_PAGES[] = {3831, 3832, 3833, 3834};
	
	private static void createBook(Player player, int damaged_book, int[] pages, int completed_book)
	{
		if(player.getInventory().containsItems(pages))
    	{
    		player.getInventory().removeItems(pages);
    		player.getInventory().remove(new Item(damaged_book));
    		player.getInventory().add(new Item(completed_book));
    		player.getActionSender().sendDialogue("", ActionSender.DialogueType.MESSAGE_MODEL_LEFT, completed_book, null, 
					"You attach the four pages to the seems of the damaged book and restore the book");
    	} else {
    		player.getActionSender().sendMessage("An empty unhoy book; collect all four pages and check the book to fill it.");	
    	}
	}
	
    public static void checkBook(Player player, int book_id) 
    {
        if(!player.getInventory().contains(book_id))
        {
        	return;
        }
        switch(book_id)
        {
        case DAMAGED_SARADOMIN_BOOK:
        	createBook(player, DAMAGED_SARADOMIN_BOOK, SARADOMIN_PAGES, HOLY_BOOK);
        	break;
        case DAMAGED_ZAMORAK_BOOK:
        	createBook(player, DAMAGED_ZAMORAK_BOOK, ZAMORAK_PAGES, UNHOLY_BOOK);
        	break;
        case DAMAGED_GUTHIX_BOOK:
        	createBook(player, DAMAGED_GUTHIX_BOOK, GUTHIX_PAGES, BOOK_OF_BALANCE);
        	break;
        case DAMAGED_BANDOS_BOOK:
        	createBook(player, DAMAGED_BANDOS_BOOK, BANDOS_AGES, BOOK_OF_WAR);
        	break;
        case DAMAGED_ARMADYL_BOOK:
        	createBook(player, DAMAGED_ARMADYL_BOOK, ARMADYL_PAGES, BOOK_OF_LAW);
        	break;
        case DAMAGED_ANCIENT_BOOK:
        	createBook(player, DAMAGED_ANCIENT_BOOK, ANCIENT_PAGES, ANCIENT_BOOK);
        	break;
        }
    }
}
