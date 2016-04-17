package net.buddat.ludumdare.ld35.entity;

/**
 * Provides attraction specifics
 */
public abstract class AttractionProvider {
	public abstract float getAcceleration(float distance);

	public abstract float getMaxRange();

	public AttractantType getType() {
		return AttractantType.GENERAL;
	}
}
