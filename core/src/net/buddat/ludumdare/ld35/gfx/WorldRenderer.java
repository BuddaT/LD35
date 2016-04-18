package net.buddat.ludumdare.ld35.gfx;

import java.util.HashMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.buddat.ludumdare.ld35.GraphicsHandler;
import net.buddat.ludumdare.ld35.LogicHandler;
import net.buddat.ludumdare.ld35.LogicHandler.Predator;
import net.buddat.ludumdare.ld35.entity.Position;
import net.buddat.ludumdare.ld35.entity.ProjectionTranslator;
import net.buddat.ludumdare.ld35.game.Level;

public class WorldRenderer implements ProjectionTranslator {

	private static final float SUN_MOVEMENT_SPEED = 0.005f;
	private static final float CAMERA_HEIGHT = 45f;

	private Environment worldEnvironment;

	private DirectionalLight sunLight;

	private DirectionalShadowLight shadowLight;
	private ModelBatch shadowBatch;

	private ModelBatch modelBatch;
	private IntersectableModel worldModelInstance, testModelInstance, testModelInstance2, testModelInstance3, wolfInstance,
			playerModelInstance;

	private PerspectiveCamera playerCam, sunCam;

	private final Array<ModelInstance> instances = new Array<ModelInstance>();
	private final Array<ModelInstance> noshadowInstance = new Array<ModelInstance>();

	private final HashMap<ModelInstance, AnimationController> animations = new HashMap<ModelInstance, AnimationController>();

	private Level currentLevel;
	
	public boolean pauseLogic = true;
	public boolean levelWon = false;
	public boolean levelLost = false;
	
	public boolean justStarted = true;

	public WorldRenderer() {

	}
	
	public void newLevel(boolean increaseDifficulty) {
		levelWon = false;
		levelLost = false;
		instances.clear();
		
		playerModelInstance.transform.setToTranslation(0f, 0f, 0f);
		playerModelInstance.updateCollisions();
		
		instances.add(worldModelInstance);
		instances.add(playerModelInstance);
		
		if (increaseDifficulty) {
			currentLevel = new Level((int) (currentLevel.sheepCount * 1.5f), (int) (currentLevel.wolfCount * 1.5f), currentLevel.complexity * 1.5f);
		} else {
			currentLevel = new Level(10, 2, 1.0f);
		}
		
		instances.addAll(currentLevel.getCollisionModels());
		GraphicsHandler.getLogicHandler().createCreatures(modelInstanceProvider);
	}

	public void create() {
		playerCam = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		playerCam.position.set(0f, CAMERA_HEIGHT, 0f);
		playerCam.lookAt(0, 0, 0);
		playerCam.near = 1f;
		playerCam.far = 300f;
		playerCam.update();

		worldEnvironment = new Environment();
		worldEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
		
		sunLight = new DirectionalLight().set(0.4f, 0.4f, 0.4f, -1f, -0.4f, -1f);
		worldEnvironment.add(sunLight);

		shadowLight = new DirectionalShadowLight(1024, 1024, 120f, 120f, 1f, 1000f);
		shadowLight.set(0.4f, 0.4f, 0.4f, -1f, -0.4f, -1f);
		worldEnvironment.add(shadowLight);
		worldEnvironment.shadowMap = shadowLight;

		modelBatch = new ModelBatch();
		shadowBatch = new ModelBatch(new DepthShaderProvider());

		worldModelInstance = ModelFactory.createBoxModel(100f, 0.25f, 100f, Color.FOREST);

		playerModelInstance = ModelFactory.createCustomModel(GraphicsHandler.MDL_PLR);

		AnimationController playerAnimation = new AnimationController(playerModelInstance);
		playerAnimation.setAnimation(playerModelInstance.animations.first().id, -1);
		animations.put(playerModelInstance, playerAnimation);

		instances.add(worldModelInstance);
		instances.add(playerModelInstance);

		currentLevel = new Level(10, 2, 1.0f);

		instances.addAll(currentLevel.getCollisionModels());
		// instances.add(currentLevel.getSheepPenModel());
		// instances.addAll(currentLevel.getWolfTransformModels());

		GraphicsHandler.getLogicHandler().createCreatures(modelInstanceProvider);
		GraphicsHandler.getLogicHandler().setProjectionTranslator(this);
	}

	private IntersectableModel createCreatureModel(String modelFile, Vector3 position) {
		IntersectableModel model;
		model = ModelFactory.createCustomModel(modelFile);
		model.transform.setToTranslation(position);

		instances.add(model);

		AnimationController anim = new AnimationController(model);
		anim.setAnimation(model.animations.first().id, -1);
		anim.update(MathUtils.random(anim.current.duration));
		animations.put(model, anim);

		return model;
	}

	public LogicHandler.ModelInstanceProvider modelInstanceProvider =
			new LogicHandler.ModelInstanceProvider() {
				@Override
				public IntersectableModel createModel(Entity e, Vector3 position) {
					if (e.getComponent(Predator.class) != null)
						return createCreatureModel(GraphicsHandler.MDL_WOLF, position);
					else
						return createCreatureModel(GraphicsHandler.MDL_SHEEP, position);
				}
			};

	private boolean reverseY = false;

	public void update() {
		float delta = Gdx.graphics.getDeltaTime();
		sunLight.direction.add(delta * SUN_MOVEMENT_SPEED,
				(reverseY ? delta * SUN_MOVEMENT_SPEED : delta * -SUN_MOVEMENT_SPEED), delta * SUN_MOVEMENT_SPEED);

		if (sunLight.direction.y < -1f)
			reverseY = true;
		else if (sunLight.direction.y > 0f)
			reverseY = false;

		if (sunLight.direction.z > 1f)
			sunLight.direction.z = -1f;

		if (sunLight.direction.x > 1f)
			sunLight.direction.x = -1f;

		shadowLight.setDirection(sunLight.direction);

		LogicHandler logicHandler = GraphicsHandler.getLogicHandler();
		for (Entity entity : logicHandler.getStaleModelEntities()) {
			Vector3 position = entity.getComponent(Position.class).position;
			IntersectableModel oldModel = entity.getComponent(LogicHandler.ModelComponent.class).model;
			instances.removeValue(oldModel, true);
			IntersectableModel newModel = modelInstanceProvider.createModel(entity, position);
			entity.remove(LogicHandler.ModelComponent.class);
			entity.add(new LogicHandler.ModelComponent(newModel));
		}

		for (AnimationController a : animations.values())
			a.update(delta);

		playerModelInstance.transform.setTranslation(logicHandler.getPlayerPosn());
		playerModelInstance.updateCollisions();
		
		worldModelInstance.transform.setTranslation(logicHandler.getPlayerPosn());
		
		playerCam.position.set(logicHandler.getPlayerPosn().x, CAMERA_HEIGHT,
				logicHandler.getPlayerPosn().z);
		playerCam.update();
		
		if (currentLevel.checkWin(logicHandler.getNumPenned())) {
			pauseLogic = true;
			levelWon = true;
		} else if (currentLevel.checkLose(logicHandler.getNumDead())) {
			pauseLogic = true;
			levelLost = true;
		}
	}
	
	private static final float MIN_FRAME_LEN = 1f / GraphicsHandler.FPS_CAP;
	private float timeSinceLastRender = 0;

	@SuppressWarnings("deprecation")
	public void render() {
		timeSinceLastRender += Gdx.graphics.getDeltaTime();
		if (timeSinceLastRender < MIN_FRAME_LEN)
			return;

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shadowLight.begin(playerCam.position.cpy().add(0f, -60f, 0f), playerCam.direction);
		shadowBatch.begin(shadowLight.getCamera());

		shadowBatch.render(instances);

		shadowBatch.end();
		shadowLight.end();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(playerCam);
		modelBatch.render(instances, worldEnvironment);
		modelBatch.render(noshadowInstance, worldEnvironment);
		modelBatch.end();

		timeSinceLastRender = 0;
	}

	public void dispose() {
		modelBatch.dispose();
	}
	
	public Level getCurrentLevel() {
		return currentLevel;
	}
	
	public IntersectableModel getPlayerModel() {
		return playerModelInstance;
	}
	
	public AnimationController getAnimController(ModelInstance model) {
		return animations.get(model);
	}

	@Override
	public Vector3 unproject(int x, int y) {
		Vector3 worldPosition = playerCam.unproject(new Vector3(x, y, 0));
		worldPosition.y = 0;
		return worldPosition;
	}

}
