package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Contains velocity and rotation data
 */
public class MovementComponent implements Component {
	public Vector3 velocity;
	public Vector3 rotation;

	public MovementComponent(float x, float y, float z, float rotX, float rotY, float rotZ) {
		this.velocity = new Vector3(x, y, z);
		this.rotation = new Vector3(rotX, rotY, rotZ);
	}
}
