package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Contains velocity and rotation data
 */
public class Movement implements Component {
	public Vector3 velocity;
	public Vector3 rotation;

	public Movement(Vector3 velocity, Vector3 rotation) {
		this.velocity = new Vector3(velocity);
		this.rotation = new Vector3(rotation);
	}
}
