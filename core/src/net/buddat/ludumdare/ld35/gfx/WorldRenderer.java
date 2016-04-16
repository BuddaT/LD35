package net.buddat.ludumdare.ld35.gfx;

import java.util.HashMap;

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
import net.buddat.ludumdare.ld35.entity.ProjectionTranslator;

public class WorldRenderer implements ProjectionTranslator {

	private static final float SUN_MOVEMENT_SPEED = 0.001f;

	private Environment worldEnvironment;

	private DirectionalLight sunLight;

	private DirectionalShadowLight shadowLight;
	private ModelBatch shadowBatch;

	private ModelBatch modelBatch;
	private ModelInstance worldModelInstance, testModelInstance, wolfInstance, playerModelInstance;

	private PerspectiveCamera playerCam, sunCam;

	private final Array<ModelInstance> instances = new Array<ModelInstance>();
	private final HashMap<ModelInstance, AnimationController> animations = new HashMap<ModelInstance, AnimationController>();

	public WorldRenderer() {

	}

	public void create() {
		playerCam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		playerCam.position.set(0f, 40f, 0f);
		playerCam.lookAt(0, 0, 0);
		playerCam.near = 1f;
		playerCam.far = 300f;
		playerCam.update();

		worldEnvironment = new Environment();
		worldEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
		
		sunLight = new DirectionalLight().set(0.6f, 0.6f, 0.6f, -1f, -0.4f, -1f);
		worldEnvironment.add(sunLight);

		shadowLight = new DirectionalShadowLight(1024, 1024, 120f, 120f, 1f, 1000f);
		shadowLight.set(0.6f, 0.6f, 0.6f, -1f, -0.4f, -1f);
		worldEnvironment.add(shadowLight);
		worldEnvironment.shadowMap = shadowLight;

		modelBatch = new ModelBatch();
		shadowBatch = new ModelBatch(new DepthShaderProvider());

		worldModelInstance = ModelFactory.createBoxModel(500f, 0.2f, 500f, Color.FOREST);
		testModelInstance = ModelFactory.createSphereModel(5f, 5f, 5f, Color.FIREBRICK, 16);
		wolfInstance = ModelFactory.createCustomModel(GraphicsHandler.MDL_WOLF);
		
		playerModelInstance = ModelFactory.createCustomModel(GraphicsHandler.MDL_PLR);

		AnimationController playerAnimation = new AnimationController(playerModelInstance);
		playerAnimation.setAnimation(playerModelInstance.animations.first().id, -1);
		animations.put(playerModelInstance, playerAnimation);

		AnimationController wolfAnim = new AnimationController(wolfInstance);
		wolfAnim.setAnimation(wolfInstance.animations.first().id, -1);
		animations.put(wolfInstance, wolfAnim);

		wolfInstance.transform.setToTranslation(5f, 0f, 5f);
		playerModelInstance.transform.setToTranslation(-5f, 0f, 5f);
		GraphicsHandler.getLogicHandler().createCreatures(
				new LogicHandler.ModelInstanceProvider() {
					@Override
					public ModelInstance createModel(Vector3 position) {
						ModelInstance model = ModelFactory.createCustomModel(GraphicsHandler.MDL_SHEEP);
						model.transform.setToTranslation(position);
						instances.add(model);
						
						AnimationController anim = new AnimationController(model);
						anim.setAnimation(model.animations.first().id, -1);
						anim.update(MathUtils.random(anim.current.duration));
						animations.put(model, anim);
						
						return model;
					}
				});
		GraphicsHandler.getLogicHandler().setProjectionTranslator(this);

		instances.add(worldModelInstance);
		instances.add(testModelInstance);
		instances.add(wolfInstance);
		instances.add(playerModelInstance);
	}

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

		for (AnimationController a : animations.values())
			a.update(delta);

		playerModelInstance.transform.setTranslation(GraphicsHandler.getLogicHandler().getPlayerPosn().x, 0f,
				GraphicsHandler.getLogicHandler().getPlayerPosn().y);
		playerCam.position.set(GraphicsHandler.getLogicHandler().getPlayerPosn().x, 40f,
				GraphicsHandler.getLogicHandler().getPlayerPosn().y);
		playerCam.update();
	}

	private static final float MIN_FRAME_LEN = 1f / GraphicsHandler.FPS_CAP;
	private float timeSinceLastRender = 0;

	public void render() {
		timeSinceLastRender += Gdx.graphics.getDeltaTime();
		if (timeSinceLastRender < MIN_FRAME_LEN)
			return;

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shadowLight.begin(playerCam.position.add(0f, -60f, 0f), playerCam.direction);
		shadowBatch.begin(shadowLight.getCamera());

		shadowBatch.render(instances);

		shadowBatch.end();
		shadowLight.end();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		modelBatch.begin(playerCam);
		modelBatch.render(instances, worldEnvironment);
		modelBatch.end();

		timeSinceLastRender = 0;
	}

	public void dispose() {
		modelBatch.dispose();
	}

	public PerspectiveCamera getPlayerCamera() {
		return playerCam;
	}

	@Override
	public Vector3 unproject(int x, int y) {
		return playerCam.unproject(new Vector3(x, y, 0));
	}
}
