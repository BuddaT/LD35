package net.buddat.ludumdare.ld35.entity;

/**
 * Crowding repels members of a flock
 */
public class CrowdingRepulsor implements FlockAttractor {
	// Distance to calculate crowding (^ 2 to avoid sqrt)
	private static final float CROWDING_RANGE = 10*10;

	@Override
	public float getBaseSpeed(float distance) {
		return -CROWDING_RANGE/(distance*distance);
	}

	@Override
	public float getMaxRange() {
		return CROWDING_RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.CROWDING;
	}
}
