package org.rs2server.rs2.model.player;

/**
 * @author Twelve
 */
public final class Perk {

	private final String name;
	private final String description;
	double price;
	private boolean has;
	
	public Perk(String name, String description, double price, boolean has)
	{
		this.name = name;
		this.description = description;
		this.price = price;
		this.has = has;
	}

	public String getName()
	{
		return name;
	}
	
	public double getPrice()
	{
		return price;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public boolean isOwned()
	{
		return has;
	}
	
	public void givePerk()
	{
		has = true;
	}
	
	public void removePerk()
	{
		has = false;
	}
}
