package net.buddat.ludumdare.ld35.gfx;

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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import net.buddat.ludumdare.ld35.GraphicsHandler;

public class WorldRenderer {

	private static final float SUN_MOVEMENT_SPEED = 0.001f;

	private Environment worldEnvironment;

	private DirectionalLight sunLight;

	private DirectionalShadowLight shadowLight;
	private ModelBatch shadowBatch;

	private ModelBatch modelBatch;
	private ModelInstance worldModelInstance, testModelInstance, testModelInstance2, testModelInstance3;

	private PerspectiveCamera playerCam, sunCam;

	private final Array<ModelInstance> instances = new Array<ModelInstance>();

	private AnimationController playerAnimation;

	public WorldRenderer() {

	}

	public void create() {
		playerCam = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		playerCam.position.set(0f, 0f, 50f);
		playerCam.lookAt(0, 0, 0);
		playerCam.near = 1f;
		playerCam.far = 300f;
		playerCam.update();

		worldEnvironment = new Environment();
		worldEnvironment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
		
		sunLight = new DirectionalLight().set(0.6f, 0.6f, 0.6f, -1f, -1f, -0.4f);
		worldEnvironment.add(sunLight);

		shadowLight = new DirectionalShadowLight(1024, 1024, 90f, 90f, 1f, 1000f);
		shadowLight.set(0.6f, 0.6f, 0.6f, -1f, -0.8f, -0.4f);
		worldEnvironment.add(shadowLight);
		worldEnvironment.shadowMap = shadowLight;

		modelBatch = new ModelBatch();
		shadowBatch = new ModelBatch(new DepthShaderProvider());

		worldModelInstance = ModelFactory.createBoxModel(500f, 500f, 0.2f, Color.FOREST);
		testModelInstance = ModelFactory.createSphereModel(5f, 5f, 5f, Color.FIREBRICK, 16);
		testModelInstance2 = ModelFactory.createSphereModel(5f, 5f, 5f, Color.BLUE, 16);
		testModelInstance3 = ModelFactory.createCustomModel(GraphicsHandler.MDL_PLR);

		testModelInstance3.transform.setToRotation(Vector3.Y, 0.5f);

		playerAnimation = new AnimationController(testModelInstance3);
		playerAnimation.setAnimation(testModelInstance3.animations.first().id, 9999);
		System.out.println(testModelInstance3.animations.first().id);

		testModelInstance2.transform.setToTranslation(5f, 5f, 0f);
		testModelInstance3.transform.setToTranslation(-5f, -5f, 0f);
		for (ModelInstance model : GraphicsHandler.getLogicHandler().getModels()) {
			instances.add(model);
		}

		instances.add(worldModelInstance);
		instances.add(testModelInstance);
		instances.add(testModelInstance2);
		instances.add(testModelInstance3);
	}

	private boolean reverseZ = false;

	private final float ballSpeed = 10f;

	public void update() {
		float delta = Gdx.graphics.getDeltaTime();
		sunLight.direction.add(delta * SUN_MOVEMENT_SPEED, delta * SUN_MOVEMENT_SPEED,
				(reverseZ ? delta * SUN_MOVEMENT_SPEED : delta * -SUN_MOVEMENT_SPEED));

		if (sunLight.direction.z < -1f)
			reverseZ = true;
		else if (sunLight.direction.z > 0f)
			reverseZ = false;

		if (sunLight.direction.y > 1f)
			sunLight.direction.y = -1f;

		if (sunLight.direction.x > 1f)
			sunLight.direction.x = -1f;

		shadowLight.setDirection(sunLight.direction);

		testModelInstance.transform.translate(((float) Math.random() - 0.5f) * delta * ballSpeed,
				((float) Math.random() - 0.5f) * delta * ballSpeed, 0);
		testModelInstance2.transform.translate(((float) Math.random() - 0.5f) * delta * ballSpeed,
				((float) Math.random() - 0.5f) * delta * ballSpeed, 0);
		testModelInstance3.transform.translate(((float) Math.random() - 0.5f) * delta * ballSpeed,
				((float) Math.random() - 0.5f) * delta * ballSpeed, 0);

		playerAnimation.update(delta);

		// TODO: Load all entity model information from Logic
		// Add to instances if not already there
		// Update all model positions and rotations
	}

	private static final float MIN_FRAME_LEN = 1f / GraphicsHandler.FPS_CAP;
	private float timeSinceLastRender = 0;

	public void render() {
		timeSinceLastRender += Gdx.graphics.getDeltaTime();
		if (timeSinceLastRender < MIN_FRAME_LEN)
			return;

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		shadowLight.begin(Vector3.Zero, playerCam.direction);
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
}
