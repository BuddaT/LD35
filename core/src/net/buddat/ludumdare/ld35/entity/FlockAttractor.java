package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;

/**
 * Attracts flock entities. Negative attraction acts as repellant.
 */
public abstract class FlockAttractor implements Component {
	public abstract float getAcceleration(float distance);

	public abstract float getMaxRange();

	public abstract AttractorType getAttractantType();
}
