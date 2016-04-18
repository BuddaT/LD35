package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.buddat.ludumdare.ld35.GraphicsHandler;

public class ModelFactory {

	private static final ModelBuilder modelBuilder = new ModelBuilder();

	public static IntersectableModel createBoxModel(float sizeX, float sizeY, float sizeZ, Color c) {
		Model model = modelBuilder.createBox(sizeX, sizeY, sizeZ, new Material(ColorAttribute.createDiffuse(c)),
				Usage.Position | Usage.Normal);

		return new IntersectableModel("box", model);
	}

	public static IntersectableModel createSphereModel(float sizeX, float sizeY, float sizeZ, Color c, int divisions) {
		Model model = modelBuilder.createSphere(sizeX, sizeY, sizeZ, divisions, divisions,
				new Material(ColorAttribute.createDiffuse(c)), Usage.Position | Usage.Normal);

		return new IntersectableModel("sphere", model);
	}

	public static IntersectableModel createCustomModel(String modelFile) {
		Model model = GraphicsHandler.getGraphicsHandler().getAssets().get(modelFile, Model.class);

		return new IntersectableModel(modelFile, model);
	}
	
	public static boolean intersectsWith(BoundingBox boundingBox1, BoundingBox boundingBox2) {
        Vector3 otherMin = boundingBox1.min;
        Vector3 otherMax = boundingBox1.max;
        Vector3 min = boundingBox2.min;
        Vector3 max = boundingBox2.max;

        return (min.x < otherMax.x) && (max.x > otherMin.x)
            && (min.y < otherMax.y) && (max.y > otherMin.y)
            && (min.z < otherMax.z) && (max.z > otherMin.z);
    }
	
}
