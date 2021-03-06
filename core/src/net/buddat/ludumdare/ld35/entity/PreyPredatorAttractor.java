package net.buddat.ludumdare.ld35.entity;

/**
 * Attraction of predators to prey.
 */
public class PreyPredatorAttractor implements Attractor {
	// Distance from predator to prey (^ 2 to avoid sqrt)
	private static final float MAX_RANGE = 50*50;
	// Will trot within this distance
	private static final float RANGE_TROT = 20*20;
	// Will sprint within this distance
	private static final float RANGE_RUN = 10*10;

	@Override
	public float getBaseSpeed(float distance) {
		if (distance <= RANGE_RUN) {
			return 36f;
		} else if (distance <= RANGE_TROT) {
			return 12f;
		} else {
			return 3f;
		}
	}

	@Override
	public float getMaxRange() {
		return MAX_RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.PREY_PREDATOR;
	}
}