package net.buddat.ludumdare.ld35.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import net.buddat.ludumdare.ld35.GraphicsHandler;

public class ModelFactory {

	private static final ModelBuilder modelBuilder = new ModelBuilder();

	public static ModelInstance createBoxModel(float sizeX, float sizeY, float sizeZ, Color c) {
		Model model = modelBuilder.createBox(sizeX, sizeY, sizeZ, new Material(ColorAttribute.createDiffuse(c)),
				Usage.Position | Usage.Normal);

		return new ModelInstance(model);
	}

	public static ModelInstance createSphereModel(float sizeX, float sizeY, float sizeZ, Color c, int divisions) {
		Model model = modelBuilder.createSphere(sizeX, sizeY, sizeZ, divisions, divisions,
				new Material(ColorAttribute.createDiffuse(c)), Usage.Position | Usage.Normal);

		return new ModelInstance(model);
	}

	public static ModelInstance createCustomModel(String modelFile) {
		Model model = GraphicsHandler.getGraphicsHandler().getAssets().get(modelFile, Model.class);

		return new ModelInstance(model);
	}
}
