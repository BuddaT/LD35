package net.buddat.ludumdare.ld35.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Object rotation
 */
public class PositionComponent implements Component {
	public Vector3 position;
	public Vector3 orientation;

	public PositionComponent(float x, float y, float z, float oX, float oY, float oZ) {
		this.position = new Vector3(x, y, z);
		this.orientation = new Vector3(oX, oY, oZ);
	}
}
