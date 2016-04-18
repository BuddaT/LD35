package net.buddat.ludumdare.ld35.entity;

/**
 * Repulsion of prey from predators.
 */
public class PredatorPreyRepulsor implements Attractor {
	// Distance from predator to prey (^ 2 to avoid sqrt)
	private static final float RANGE = 30*30;

	@Override
	public float getBaseSpeed(float distance) {
		return -30f;
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