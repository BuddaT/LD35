package net.buddat.ludumdare.ld35.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import net.buddat.ludumdare.ld35.entity.Position;

/**
 * Encapsulates movement calculation code
 */
public class MovementCalculator {
	private ImmutableVector3 defaultRotation;
	private static ImmutableVector3 UP = new ImmutableVector3(0, 1, 0);

	public MovementCalculator(ImmutableVector3 defaultRotation) {
		this.defaultRotation = defaultRotation;
	}

	public Vector3 calculateNewDirection(Vector3 rotation, Vector3 movementDirection) {
		if (rotation.isZero()) {
			if (movementDirection.isZero()) {
				return defaultRotation.copy();
			}
			return movementDirection;
		} else if (movementDirection.isZero()) {
			return rotation;
		}
		Vector3 average = new Vector3(rotation.add(movementDirection)).nor();
		if (average.isZero()) {
			average = rotation.rotateRad(UP.copy(), MathUtils.PI);
		}
		return average;
	}

	// Calculates a new vector from the origin away from the target
	public Vector3 awayFrom(Position origin, Position target) {
		return new Vector3(origin.position).sub(target.position);
	}

	// Calculates a new vector from the origin to the target
	public Vector3 towards(Position origin, Position target) {
		return new Vector3(target.position).sub(origin.position);
	}
}
