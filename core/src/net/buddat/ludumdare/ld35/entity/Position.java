package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Object rotation
 */
public class Position implements Component {
	public Vector3 position;
	public Vector3 rotation;

	public Position(float x, float y, float z, float rX, float rY, float rZ) {
		this.position = new Vector3(x, y, z);
		this.rotation = new Vector3(rX, rY, rZ);
	}
}
