package net.buddat.ludumdare.ld35.entity;

/**
 * Attraction of predators to prey.
 */
public class PreyPredatorAttractor implements FlockAttractor {
	// Distance from predator to prey (^ 2 to avoid sqrt)
	private static final float RANGE = 30*30;

	@Override
	public float getBaseSpeed(float distance) {
		return 0.5f;
	}

	@Override
	public float getMaxRange() {
		return RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.PREY_PREDATOR;
	}
}