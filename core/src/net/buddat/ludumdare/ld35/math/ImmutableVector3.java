package net.buddat.ludumdare.ld35.math;

import com.badlogic.gdx.math.Vector3;

/**
 * Immutable vector3 representation, prevents accidental mutation of a vec3
 */
public class ImmutableVector3 {
	private final Vector3 vector3;

	public ImmutableVector3(Vector3 vector3) {
		this.vector3 = vector3;
	}

	public ImmutableVector3(float x, float y, float z) {
		this.vector3 = new Vector3(x, y, z);
	}

	public float getX() {
		return vector3.x;
	}

	public float getY() {
		return vector3.y;
	}

	public float getZ() {
		return vector3.z;
	}

	public Vector3 copy() {
		return new Vector3(vector3);
	}
}
