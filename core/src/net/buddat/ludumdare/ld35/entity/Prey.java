package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;

/**
 * Contains basic prey states
 */
public class Prey implements Component {
	private boolean penned = false;
	private boolean dead;

	public void setPenned(boolean penned) {
		this.penned = penned;
	}

	public void kill() {
		this.dead = true;
	}

	public boolean isPenned() {
		return penned;
	}

	public boolean isDead() {
		return dead;
	}
}
