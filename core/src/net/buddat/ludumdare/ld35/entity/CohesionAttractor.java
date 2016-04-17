package net.buddat.ludumdare.ld35.entity;

/**
 * Cohesion
 */
public class CohesionAttractor extends FlockAttractor {
	// Distance to calculate cohesion (^ 2 to avoid sqrt)
	private static final float COHESION_RANGE = 30*30;

	@Override
	public float getAcceleration(float distance) {
		return COHESION_RANGE;
	}

	@Override
	public float getMaxRange() {
		return COHESION_RANGE;
	}

	@Override
	public AttractorType getAttractantType() {
		return AttractorType.COHESION;
	}
}