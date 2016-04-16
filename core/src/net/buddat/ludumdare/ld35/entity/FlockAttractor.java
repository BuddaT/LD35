package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;

/**
 * Attracts flock entities
 */
public class FlockAttractor implements Component {
	private final AttractionProvider provider;
	public FlockAttractor(AttractionProvider provider) {
		this.provider = provider;
	}
	public float getAcceleration(float distance) {
		return provider.getAcceleration(distance);
	}

	public float getMaxRange() {
		return provider.getMaxRange();
	}

	public abstract static class AttractionProvider {
		public abstract float getAcceleration(float distance);

		public abstract float getMaxRange();
	}
}
