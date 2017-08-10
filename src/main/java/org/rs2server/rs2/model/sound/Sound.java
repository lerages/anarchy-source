package org.rs2server.rs2.model.sound;

public class Sound {

	/**
	 * The id of this sound.
	 */
	private int id;
	
	/**
	 * The type of this sound.
	 */
	private byte type;
	
	/**
	 * The delay before this sound is played.
	 */
	private int delay = 0;
	
	/**
	 * The volume this sound is played at.
	 */
	private int volume = 1;
	
	public Sound(int id, byte type, int delay, int volume) 
	{
		this.id = id;
		this.type = type;
		this.delay = delay;
		this.volume = volume;
	}
	
	public static Sound create(int id, byte type, int delay, int volume) {
		return new Sound(id, type, delay, volume);
	}

	public int getId() {
		return id;
	}

	public byte getType() {
		return type;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public int getVolume() {
		return volume;
	}
}



