package net.buddat.ludumdare.ld35.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import net.buddat.ludumdare.ld35.GraphicsHandler;
import net.buddat.ludumdare.ld35.LogicHandler;
import net.buddat.ludumdare.ld35.LogicHandler.ModelComponent;
import net.buddat.ludumdare.ld35.entity.FixedObjectRepulsor;
import net.buddat.ludumdare.ld35.entity.Movement;
import net.buddat.ludumdare.ld35.entity.Position;
import net.buddat.ludumdare.ld35.gfx.IntersectableModel;
import net.buddat.ludumdare.ld35.gfx.ModelFactory;

public class Level {

	private static final int SHEEP_PEN_W = 3, SHEEP_PEN_H = 3;

	public final int sheepCount;
	public final int wolfCount;
	private final int sheepToWin;
	private final int deadToLose;

	public final float complexity;
	private final float hiddenSheepChance;

	private final Array<IntersectableModel> collisions = new Array<IntersectableModel>();

	private IntersectableModel sheepPenModel;
	
	private Array<IntersectableModel> wolfMB;
	
	private IntersectableModel mapModel;

	private final Engine engine;
	private Entity player;

	public Level(int sheepCount, int wolfCount, float complexity) {
		this.sheepCount = sheepCount;
		this.wolfCount = wolfCount;
		this.complexity = complexity;
		
		this.sheepToWin = (int) (sheepCount / (5f / complexity));
		this.deadToLose = sheepCount - sheepToWin + 1;
		
		this.hiddenSheepChance = 0.1f * complexity;

		this.engine = new Engine();

		generateLevel(100f * complexity);
	}
	
	public boolean checkLose(int sheepDead) {
		if (sheepDead >= deadToLose)
			return true;
		
		return false;
	}
	
	public boolean checkWin(int sheepInPen) {
		if (sheepInPen >= sheepToWin)
			return true;
		
		return false;
	}

	private void generateLevel(float mapSize) {
		player = new Entity();
		player.add(new Position(new Vector3(), new Vector3()));
		player.add(new Movement(new Vector3(), new Vector3()));
		player.add(new LogicHandler.Mouseable());
		player.add(new ModelComponent(GraphicsHandler.getGraphicsHandler().getWorldRenderer().getPlayerModel()));

		engine.addEntity(player);

		createBounds(mapSize);
		
		int penSize = (int) (SHEEP_PEN_W + MathUtils.random(complexity));
		createSheepPen(mapSize, penSize, penSize);
		createWolfTransform(mapSize);
		
		for (int i = 0; i < mapSize / (10f / complexity); i++) {
			createTree(mapSize, 0.5f);
		}
		
		boolean fine;
		Vector2 testVec = new Vector2();
		for (int i = 0; i < sheepCount; i++) {
			float posX;
			float posZ;
			
			do {
				fine = true;
				
				testVec.x = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
				testVec.y = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
				
				if (sheepPenModel.lineIntersectsCollision(testVec, testVec))
					fine = false;
				for (IntersectableModel m : collisions) {
					if (m.lineIntersectsCollision(testVec, testVec))
						fine = false;
				}
			} while (!fine);
			
			posX = testVec.x;
			posZ = testVec.y;
			
			Entity sheepEntity = GraphicsHandler.getLogicHandler().createNewPrey(new Vector3(posX, 0f, posZ), new Vector3());
			
			if (MathUtils.randomBoolean(hiddenSheepChance))
				sheepEntity.add(new LogicHandler.PredatorHidden());
			
			engine.addEntity(sheepEntity);
		}
		
		for (int i = 0; i < wolfCount; i++) {
			float posX = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
			float posZ = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
			
			engine.addEntity(GraphicsHandler.getLogicHandler().createNewPredator(new Vector3(posX, 0f, posZ), new Vector3()));
		}
	}

	private IntersectableModel newCollision(float x, float y, float z, float scale, float rotation, String model) {
		IntersectableModel c = ModelFactory.createCustomModel(model);
		c.transform.setToTranslation(x, y, z).rotate(Vector3.Y, rotation).scale(scale, scale, scale);

		return c;
	}

	public void addCollisionModel(IntersectableModel c) {
		if (collisions.contains(c, false))
			return;
		
		c.updateCollisions();

		collisions.add(c);
	}

	public Array<IntersectableModel> getCollisionModels() {
		return collisions;
	}

	public IntersectableModel getSheepPenModel() {
		return sheepPenModel;
	}
	
	public Array<IntersectableModel> getWolfTransformModels() {
		return wolfMB;
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
	
	private void createTree(float mapSize, float weight) {
		boolean okay = true;
		
		float penLocX = 0;
		float penLocZ = 0;
		
		String treeType = "";
		float rotateAmnt = 0f;
		switch (MathUtils.random(2)) {
		case 0:
			treeType = GraphicsHandler.MDL_TREE1;
			rotateAmnt = 360f * MathUtils.random() - 180f;
			break;
		case 1:
			treeType = GraphicsHandler.MDL_TREE2;
			rotateAmnt = 360f * MathUtils.random() - 180f;
			break;
		case 2:
			treeType = GraphicsHandler.MDL_ROCK1;
			rotateAmnt = MathUtils.random(3) * 90f;
			break;
		}
		
		IntersectableModel treeInstance = ModelFactory.createCustomModel(treeType);
		IntersectableModel collisionModel = null;
		do {
			okay = true;
			
			penLocX = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
			penLocZ = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
			
			treeInstance.transform.setToTranslation(penLocX, 0f, penLocZ);
			
			for (IntersectableModel b : collisions)
				if (b.intersects(treeInstance)) {
					okay = false;
					break;
				}
			
			if (okay) {
				if (treeInstance.intersects(sheepPenModel))
					okay = false;
				for (IntersectableModel b : wolfMB)
					if (treeInstance.intersects(b))
						okay = false;
				
				if (treeInstance.intersects(GraphicsHandler.getGraphicsHandler().getWorldRenderer().getPlayerModel()))
					okay = false;

				collisionModel = newCollision(penLocX, 0f, penLocZ, 0.5f + MathUtils.random(1.5f), rotateAmnt, treeType);
				if (collisionModel.intersects(sheepPenModel)) {
					collisionModel = null;
					okay = false;
				}
			}
		} while (!okay);
		addFixedEntity(collisionModel, penLocX, penLocZ);
		addCollisionModel(collisionModel);
	}
	
	private void createWolfTransform(float mapSize) {
		wolfMB = new Array<IntersectableModel>();
		
		for (int i = 0; i < mapSize / 50f; i++) {
			IntersectableModel wolfTransform = ModelFactory.createBoxModel(10f, 0.5f, 10f, new Color(0.8f, 0.2f, 0.2f, 1f));
			
			do {
				float penLocX = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
				float penLocZ = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
				
				wolfTransform.transform.setToTranslation(penLocX, 0f, penLocZ);
				
			} while (wolfTransform.intersects(sheepPenModel) || !mapModel.contains(wolfTransform));
			
			wolfMB.add(wolfTransform);
		}
	}

	private void createSheepPen(float mapSize, int penW, int penH) {
		float penLocX = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
		float penLocZ = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);

		BoundingBox fenceB = ModelFactory.createCustomModel(GraphicsHandler.MDL_FENCE1).model
				.calculateBoundingBox(new BoundingBox());

		sheepPenModel = ModelFactory.createBoxModel(penW * fenceB.getWidth(), 0.5f, penH * fenceB.getWidth(), new Color(0.5f, 0.5f, 0.5f, 1f));
		
		do {
			penLocX = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
			penLocZ = ((mapSize / 2) * -1) + (MathUtils.random() * mapSize);
			
			sheepPenModel.transform.setTranslation(penLocX, 0, penLocZ);
		} while(!mapModel.contains(sheepPenModel));

		int entryPoint = MathUtils.random(penW - 1);
		boolean x = false;
		boolean firstDir = false;
		if (penLocZ < 0) {
			if (penLocX < 0) {
				if (penLocZ <= penLocX) {
					x = true;
					firstDir = true;
				}
			} else {
				if (penLocZ * -1 >= penLocX) {
					x = true;
					firstDir = true;
				} else {
					firstDir = true;
				}
			}
		} else {
			if (penLocX < 0) {
				if (penLocZ * -1 <= penLocX) {
					x = true;
				}
			} else {
				if (penLocZ >= penLocX) {
					x = true;
				} else {
					firstDir = true;
				}
			}
		}

		System.out.println(x + "," + firstDir + "," + entryPoint);
		for (int i = 0; i < penW; i++) {
			if (x && i == entryPoint) {
				if (firstDir)
					createFence(penLocX + ((i - penW / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f),
							penLocZ - ((penH / 2f) * fenceB.getWidth()), 1.0f, 0f);
				else
					createFence(penLocX + ((i - penW / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f),
							penLocZ + ((penH / 2f) * fenceB.getWidth()), 1.0f, 0f);

				continue;
			}

			// North Fence
			createFence(penLocX + ((i - penW / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f),
					penLocZ - ((penH / 2f) * fenceB.getWidth()), 1.0f, 0f);
			// South Fence
			createFence(penLocX + ((i - penW / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f),
					penLocZ + ((penH / 2f) * fenceB.getWidth()), 1.0f, 0f);
		}

		for (int i = 0; i < penH; i++) {
			if (!x && i == entryPoint) {
				if (firstDir)
					createFence(penLocX + ((penW / 2f) * fenceB.getWidth()),
							penLocZ + ((i - penH / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f), 1.0f, 90f);
				else
					createFence(penLocX - ((penW / 2f) * fenceB.getWidth()),
							penLocZ + ((i - penH / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f), 1.0f, 90f);

				continue;
			}

			// West Fence
			createFence(penLocX - ((penW / 2f) * fenceB.getWidth()),
					penLocZ + ((i - penH / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f), 1.0f, 90f);
			// East Fence
			createFence(penLocX + ((penW / 2f) * fenceB.getWidth()),
					penLocZ + ((i - penH / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f), 1.0f, 90f);
		}
	}

	private IntersectableModel createFence(float x, float z, float scale, float rotation) {
		IntersectableModel model = newCollision(x, 0, z, scale, rotation, GraphicsHandler.MDL_FENCE1);
		addCollisionModel(model);
		addFixedEntity(model, x, z);
		return model;
	}

	private void addFixedEntity(IntersectableModel model, float x, float z) {
		engine.addEntity(new Entity()
				.add(new FixedObjectRepulsor())
				.add(new ModelComponent(model))
				.add(new Position(new Vector3(x, 0, z), new Vector3())) // rotation unused
		);
	}

	private void createBounds(float mapSize) {
		mapModel = ModelFactory.createBoxModel(mapSize, 0.5f, mapSize, new Color(0.2f, 0.2f, 0.8f, 1f));
		mapModel.transform.setTranslation(0, 0, 0);
		
		mapSize *= 1.1f;
		
		BoundingBox fenceB = ModelFactory.createCustomModel(GraphicsHandler.MDL_FENCE1).model
				.calculateBoundingBox(new BoundingBox());
		
		int penW = (int) (mapSize / fenceB.getWidth());
		int penH = penW;
		
		for (int i = 0; i < penW; i++) {
			// North Fence
			createFence(((i - penW / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f),
					-((penH / 2f) * fenceB.getWidth()), 1.0f, 0f);
			// South Fence
			createFence(((i - penW / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f),
					((penH / 2f) * fenceB.getWidth()), 1.0f, 0f);
			// West Fence
			createFence(-((penW / 2f) * fenceB.getWidth()),
					((i - penH / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f), 1.0f, 90f);
			// East Fence
			createFence(((penW / 2f) * fenceB.getWidth()),
					((i - penH / 2f) * fenceB.getWidth()) + (fenceB.getWidth() / 2f), 1.0f, 90f);
		}
	}
}
