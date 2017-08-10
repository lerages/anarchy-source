package org.rs2server.rs2.tickable;


import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A class that manages <code>Tickable</code>s for a specific
 * <code>GameEngine</code>.
 * @author Michael Bull
 *
 */
public class TickableManager {
	
	/**
	 * The list of tickables.
	 */
	private List<Tickable> tickables = new LinkedList<>();
	
	/**
	 * @return The tickables.
	 */
	public List<Tickable> getTickables() {
		return tickables;
	}
	
	/**
	 * Submits a new tickable to the <code>GameEngine</code>.
	 * @param tickable The tickable to submit.
	 */
	public void submit(final Tickable tickable) {
		tickables.add(tickable);
	}	
	
}
