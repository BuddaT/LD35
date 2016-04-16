package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Object rotation
 */
public class Position implements Component {
	public Vector3 position;
	public Vector3 rotation;

	public Position(Vector3 position, Vector3 rotation) {
		this.position = new Vector3(position);
		this.rotation = new Vector3(rotation);
	}
}
