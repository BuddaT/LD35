package net.buddat.ludumdare.ld35.entity;

/**
 * Cohesion
 */
public class CohesionAttractor implements Attractor {
	// Distance to calculate cohesion (^ 2 to avoid sqrt)
	private static final float COHESION_RANGE = 30*30;

	@Override
	public float getBaseSpeed(float distance) {
		return 60;
	}

	@Override
	public float getMaxRange() {
		return COHESION_RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.COHESION;
	}
}