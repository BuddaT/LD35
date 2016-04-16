package net.buddat.ludumdare.ld35.entity;

import com.badlogic.gdx.math.Vector3;

/**
 * Translates screen x y to world x y
 */
public interface ProjectionTranslator {
	Vector3 unproject(int x, int y);
}
