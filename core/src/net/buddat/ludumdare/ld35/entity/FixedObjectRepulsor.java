package net.buddat.ludumdare.ld35.entity;

/**
 * Fixed object that repulses entities
 */
public class FixedObjectRepulsor implements Attractor {
	private static final float RANGE = 15*15;
	@Override
	public float getBaseSpeed(float distance) {
		if (distance == 0) {
			return Float.MIN_VALUE;
		}
		return -RANGE / (distance * distance);
	}

	@Override
	public float getMaxRange() {
		return RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.FIXED_OBSTACLE;
	}
}
