package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;

/**
 * Attracts flock entities. Negative attraction acts as repellant.
 */
public interface FlockAttractor extends Component {
	float getBaseSpeed(float distance);

	float getMaxRange();

	AttractorType getAttractorType();
}
