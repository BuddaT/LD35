package net.buddat.ludumdare.ld35.entity;

/**
 * Fixed object that repulses entities
 */
public class FixedObjectRepulsor implements Attractor {
	private static final float RANGE = 10*10;
	@Override
	public float getBaseSpeed(float distance) {
		return RANGE / (distance * distance);
	}

	@Override
	public float getMaxRange() {
		return RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.GENERAL;
	}
}
