package net.buddat.ludumdare.ld35.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import net.buddat.ludumdare.ld35.GraphicsHandler;
import net.buddat.ludumdare.ld35.LogicHandler;
import net.buddat.ludumdare.ld35.entity.Movement;
import net.buddat.ludumdare.ld35.entity.Position;
import net.buddat.ludumdare.ld35.gfx.ModelFactory;

public class Level {

	private static final int SHEEP_PEN_W = 3, SHEEP_PEN_H = 3;

	private final int sheepCount;
	private final int wolfCount;

	private final float complexity;

	private final Array<ModelInstance> collisions = new Array<ModelInstance>();
	private final Array<BoundingBox> collisionBoxes = new Array<BoundingBox>();

	private BoundingBox sheepPen;
	private ModelInstance sheepPenModel;

	private final Engine engine;
	private Entity player;

	public Level(int sheepCount, int wolfCount, float complexity) {
		this.sheepCount = sheepCount;
		this.wolfCount = wolfCount;
		this.complexity = complexity;

		this.engine = new Engine();

		generateLevel();
	}

	private void generateLevel() {
		player = new Entity();
		player.add(new Position(new Vector3(), new Vector3()));
		player.add(new Movement(0, 0, 0, 0, 0, 0));
		player.add(new LogicHandler.Mouseable());

		engine.addEntity(player);

		createSheepPen(150f, SHEEP_PEN_W, SHEEP_PEN_H);
	}

	private ModelInstance newCollision(float x, float y, float z, float scale, float rotation, String model) {
		ModelInstance c = ModelFactory.createCustomModel(model);
		c.transform.setToTranslation(x, y, z).rotate(Vector3.Y, rotation).scale(scale, scale, scale);

		addCollisionBox(c.calculateBoundingBox(new BoundingBox()));

		return c;
	}

	public void addCollisionModel(ModelInstance c) {
		if (collisions.contains(c, false))
			return;

		collisions.add(c);
	}

	public void addCollisionBox(BoundingBox b) {
		if (collisionBoxes.contains(b, false))
			return;

		collisionBoxes.add(b);
	}

	public Array<ModelInstance> getCollisionModels() {
		return collisions;
	}

	public Array<BoundingBox> getCollisionBoxes() {
		return collisionBoxes;
	}
	
	public BoundingBox getSheepPenBoundingBox() {
		return sheepPen;
	}

	public ModelInstance getSheepPenModel() {
		return sheepPenModel;
	}

	public Entity getPlayer() {
		return player;
	}

	public Engine getEngine() {
		return engine;
	}

	public void dispose() {
		collisions.clear();
		engine.removeAllEntities();
	}

	private void createSheepPen(float mapSize, int penW, int penH) {
		float penLocX = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
		float penLocZ = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);

		BoundingBox fenceB = ModelFactory.createCustomModel(GraphicsHandler.MDL_FENCE1).model
				.calculateBoundingBox(new BoundingBox());

		sheepPen = new BoundingBox(new Vector3(penLocX, 0, penLocZ), new Vector3(
				penLocX + (penW * fenceB.getWidth()), 10, penLocZ + (penH * fenceB.getWidth())));

		sheepPenModel = ModelFactory.createBoxModel(sheepPen.getWidth(), 0.5f, sheepPen.getDepth(),
				new Color(0.5f, 0.5f, 0.5f, 1f));
		sheepPenModel.transform.setTranslation(penLocX + fenceB.getWidth(), 0, penLocZ + fenceB.getWidth());

		int entryPoint = MathUtils.random(penW - 1);
		boolean x = false;
		boolean firstDir = false;
		if (penLocZ < 0) {
			if (penLocX < 0) {
				if (penLocZ < penLocX) {
					x = true;
					firstDir = true;
				}
			} else {
				if (penLocZ * -1 > penLocX) {
					x = true;
					firstDir = true;
				} else {
					firstDir = true;
				}
			}
		} else {
			if (penLocX < 0) {
				if (penLocZ * -1 < penLocX) {
					x = true;
				}
			} else {
				if (penLocZ > penLocX) {
					x = true;
				} else {
					firstDir = true;
				}
			}
		}

		for (int i = 0; i < penW; i++) {
			if (x && i == entryPoint) {
				if (firstDir)
					addCollisionModel(newCollision(penLocX + (i * fenceB.getWidth()), 0,
							penLocZ - (fenceB.getWidth() / 2f), 1.0f, 0f, GraphicsHandler.MDL_FENCE1));
				else
					addCollisionModel(newCollision(penLocX + (i * fenceB.getWidth()), 0,
							penLocZ + (penH * fenceB.getWidth()) - (fenceB.getWidth() / 2f), 1.0f, 0f,
							GraphicsHandler.MDL_FENCE1));

				continue;
			}

			// North Fence
			addCollisionModel(newCollision(penLocX + (i * fenceB.getWidth()), 0, penLocZ - (fenceB.getWidth() / 2f),
					1.0f, 0f, GraphicsHandler.MDL_FENCE1));
			// South Fence
			addCollisionModel(newCollision(penLocX + (i * fenceB.getWidth()), 0,
					penLocZ + (penH * fenceB.getWidth()) - (fenceB.getWidth() / 2f), 1.0f, 0f,
					GraphicsHandler.MDL_FENCE1));
		}

		for (int i = 0; i < penH; i++) {
			if (!x && i == entryPoint) {
				if (firstDir)
					addCollisionModel(
							newCollision(penLocX + (penW * fenceB.getWidth()) - (fenceB.getWidth() / 2f), 0,
									penLocZ + (i * fenceB.getWidth()), 1.0f, 90f, GraphicsHandler.MDL_FENCE1));
				else
					addCollisionModel(newCollision(penLocX - (fenceB.getWidth() / 2f), 0,
							penLocZ + (i * fenceB.getWidth()), 1.0f, 90f, GraphicsHandler.MDL_FENCE1));

				continue;
			}

			// West Fence
			addCollisionModel(newCollision(penLocX - (fenceB.getWidth() / 2f), 0, penLocZ + (i * fenceB.getWidth()),
					1.0f, 90f, GraphicsHandler.MDL_FENCE1));
			// East Fence
			addCollisionModel(newCollision(penLocX + (penW * fenceB.getWidth()) - (fenceB.getWidth() / 2f), 0,
					penLocZ + (i * fenceB.getWidth()), 1.0f, 90f, GraphicsHandler.MDL_FENCE1));
		}
	}
}
