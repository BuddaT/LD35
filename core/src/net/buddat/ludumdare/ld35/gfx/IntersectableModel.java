package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class IntersectableModel extends ModelInstance {

	// debugging
	private final String name;
	private BoundingBox bounds = new BoundingBox();
	
	public Polygon collision;

	public IntersectableModel(String name, Model model) {
		super(model);
		this.name = name;
		
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
		collision.setScale(transform.getScaleX(), transform.getScaleZ());
	}
	
	public boolean lineIntersectsCollision(Vector2 p1, Vector2 p2) {
		Rectangle r = collision.getBoundingRectangle();
		
        return lineIntersectsLine(p1, p2, new Vector2(r.x, r.y), new Vector2(r.x + r.width, r.y)) ||
               lineIntersectsLine(p1, p2, new Vector2(r.x + r.width, r.y), new Vector2(r.x + r.width, r.y + r.height)) ||
               lineIntersectsLine(p1, p2, new Vector2(r.x + r.width, r.y + r.height), new Vector2(r.x, r.y + r.height)) ||
               lineIntersectsLine(p1, p2, new Vector2(r.x, r.y + r.height), new Vector2(r.x, r.y)) ||
               (r.contains(p1) && r.contains(p2));
    }
	
    private static boolean lineIntersectsLine(Vector2 l1p1, Vector2 l1p2, Vector2 l2p1, Vector2 l2p2) {
        float q = (l1p1.y - l2p1.y) * (l2p2.x - l2p1.x) - (l1p1.x - l2p1.x) * (l2p2.y - l2p1.y);
        float d = (l1p2.x - l1p1.x) * (l2p2.y - l2p1.y) - (l1p2.y - l1p1.y) * (l2p2.x - l2p1.x);

        if(d == 0)
            return false;

        float r = q / d;

        q = (l1p1.y - l2p1.y) * (l1p2.x - l1p1.x) - (l1p1.x - l2p1.x) * (l1p2.y - l1p1.y);
        float s = q / d;

        if(r < 0 || r > 1 || s < 0 || s > 1)
            return false;

        return true;
    }
}
