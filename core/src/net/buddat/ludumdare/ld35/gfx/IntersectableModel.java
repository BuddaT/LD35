package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class IntersectableModel extends ModelInstance {
	
	private BoundingBox bounds = new BoundingBox();
	
	public Polygon collision;
	
	public IntersectableModel(Model model) {
		super(model);
		
		bounds = calculateBoundingBox(bounds);
		collision = new Polygon(new float[] {
				bounds.min.x, bounds.min.z, 
				bounds.max.x, bounds.min.z,
				bounds.max.x, bounds.max.z, 
				bounds.min.x, bounds.max.z
			});
		
		collision.setOrigin(bounds.getCenterX(), bounds.getCenterZ());
	}

	public boolean intersects(IntersectableModel m) {
		updateCollisions();
		m.updateCollisions();
		
		// return Intersector.overlapConvexPolygons(collision, m.collision);
		return collision.getBoundingRectangle().overlaps(m.collision.getBoundingRectangle());
	}
	
	public boolean contains(IntersectableModel m) {
		updateCollisions();
		m.updateCollisions();
		
		return collision.getBoundingRectangle().contains(m.collision.getBoundingRectangle());
	}
	
	public void updateCollisions() {
		collision.setPosition(transform.getTranslation(new Vector3()).x, transform.getTranslation(new Vector3()).z);
		collision.setRotation(transform.getRotation(new Quaternion()).getAngleAround(Vector3.Y));
		
		if (transform.getRotation(new Quaternion()).getAngleAround(Vector3.Y) != 0f)
			collision.translate(0f, bounds.getWidth());
		
		collision.setScale(transform.getScaleX(), transform.getScaleZ());
	}
}
