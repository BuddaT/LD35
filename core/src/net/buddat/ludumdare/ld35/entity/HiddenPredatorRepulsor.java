package net.buddat.ludumdare.ld35.entity;

/**
 * There's something not quite right about that one...
 */
public class HiddenPredatorRepulsor implements Attractor {
	// Distance to calculate (^ 2 to avoid sqrt)
	private static final float RANGE = 10*10;

	@Override
	public float getBaseSpeed(float distance) {
		return -60f * RANGE /(distance*distance);
	}

	@Override
	public float getMaxRange() {
		return RANGE;
	}

	@Override
	public AttractorType getAttractorType() {
		return AttractorType.HIDDEN_PREDATOR;
	}
}
